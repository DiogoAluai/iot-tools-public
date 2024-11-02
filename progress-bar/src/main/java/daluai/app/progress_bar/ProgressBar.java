package daluai.app.progress_bar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Progress bar for script.
 * It reads and executes bash script, weighing each line the same.
 * It requires column env/pro to be passed, example: "java -jar -DCOLUMNS=$COLUMNS target/progress-bar-1.0.jar ex.sh"
 * jdk17
 */
public class ProgressBar {

    private static final String COMMAND_FINISHED = "COMMAND_FINISHED_DSJA8374";
    private static String emptyLine = null;

    private static final String ENV_COLUMNS = "COLUMNS";
    private static final String VM_PROP_COLUMNS = "COLUMNS";
    private static final Character CHAR_PROGRESS_BAR_PREFIX = '[';
    private static final Character CHAR_PROGRESS_BAR_SUFFIX = ']';
    private static final Character CHAR_PROGRESS_BAR_FILLING = '=';
    private static final Character CHAR_PROGRESS_BAR_HEAD = '>';
    private static final Character CHAR_PROGRESS_BAR_MISSING = ' ';

    public static void main(String[] args) throws Exception {
        int consoleWidth = getConsoleWidth();
        List<String> scriptLines = getShellScriptContents(args);
        List<String> linesToExecute = scriptLines.stream()
                .filter(l -> !l.strip().startsWith("#") && !l.isBlank())
                .map(l -> l.split("#")[0])
                .toList();

        int statementCount = linesToExecute.size();
        int progressBarFillingWidth = (consoleWidth / 2);
        float step = (float) progressBarFillingWidth / statementCount;
        int progressBarWidth = progressBarFillingWidth + 2 + 7; // prefix char, suffix char and appended percentage

        // would this work without bash prefix in Linux?
        Process process = new ProcessBuilder("bash")
                .redirectError(ProcessBuilder.Redirect.INHERIT)   // Forward error to System.err
                .start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));

        for (int i = 0; i < linesToExecute.size(); i++) {
            int currentProgress = Math.min(progressBarFillingWidth, Math.round((i + 1) * step));
            int missingProgress = progressBarFillingWidth - currentProgress;
            String progressBar = buildProgressBar(currentProgress, missingProgress, progressBarFillingWidth);
            runProcess(reader, writer, linesToExecute.get(i), progressBar, progressBarWidth);
        }
        System.out.println();
        System.exit(0);
    }

    private static String buildProgressBar(int currentProgress, int missingProgress, int progressBarFillingWidth) {
        float donePercentage = (float) currentProgress / progressBarFillingWidth * 100;
        Character headCharacter = missingProgress == 0 ? CHAR_PROGRESS_BAR_FILLING : CHAR_PROGRESS_BAR_HEAD;
        return CHAR_PROGRESS_BAR_PREFIX
                + CHAR_PROGRESS_BAR_FILLING.toString().repeat(currentProgress - 1) // head is part of progress
                + headCharacter
                + CHAR_PROGRESS_BAR_MISSING.toString().repeat(missingProgress)
                + CHAR_PROGRESS_BAR_SUFFIX
                + " " + String.format("%.2f", donePercentage) + "%";
    }

    /**
     * Execute string and return exit code.
     * Clears line (progress bar) once, then prints all
     */
    private static void runProcess(BufferedReader reader, BufferedWriter writer, String command, String progressBar, int progressBarWidth) throws IOException {
        writer.write(command + ";echo " + COMMAND_FINISHED + "\n");
        writer.flush();
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.contains(COMMAND_FINISHED)) {
                // manual detection of command end, as otherwise we'd be waiting forever
                break;
            }
            // Clear the line, because some terminals won't clear it all by passing the '\r' char
            System.out.print("\r" + getEmptyLine(progressBarWidth) + "\r");
            System.out.println(line);
            System.out.print("\r" + progressBar);
        }
    }

    private static int getConsoleWidth() {
        String columnsEnv = Optional.ofNullable(System.getenv(ENV_COLUMNS))
                .orElse(System.getProperty(VM_PROP_COLUMNS));
        if (columnsEnv == null) {
            System.out.println("No COLUMNS env variable set. Maybe you forgot \"export COLUMNS\"?");
            System.exit(1);
        }
        return Integer.parseInt(columnsEnv);
    }

    /**
     * Returns script lines.
     */
    private static List<String> getShellScriptContents(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: command <shell_script>");
            System.exit(1);
        }
        String shellScriptPath = args[0];
        File shellScriptFile = new File(shellScriptPath);
        try (FileInputStream inputStream = new FileInputStream(shellScriptFile)) {
            String[] lines = new String(inputStream.readAllBytes()).split("\n");
            return Arrays.stream(lines)
                    .map(l -> l.endsWith("\r") ? l.substring(0, l.length() - 1) : l)
                    .toList();
        }
    }

    /**
     * Initializes if needed. Provide line with spaces to clear
     */
    private static String getEmptyLine(int columnWidth) {
        if (emptyLine != null) {
            return emptyLine;
        }
        emptyLine = " ".repeat(columnWidth);
        return emptyLine;
    }
}
