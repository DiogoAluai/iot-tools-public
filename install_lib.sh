

# default install target dir
install_dir="/usr/bin/"
get_install_iot_tools_dir() {
	echo "$install_dir"
}


install_script_iot_tools() {
  if [ $# -ne 1 ]; then
    echo "Number of arguments provided for install function were... unreasonable"
    echo "Should've gotten one argument, instead got $#:"
    for arg in "$@"
    do
      echo -n "  $arg"
    done
    echo
    exit 1
  fi

  script_file="$1"
  echo "Installing $script_file"
  sudo chmod +x "$script_file"
  sudo cp "$script_file" "$install_dir" || {
    echo "[ERROR] Could not copy, aborting install"
    exit 1
  }
}
