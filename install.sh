#!/bin/bash

#
# Install iot commands by copying them to /usr/bin/, or provided directory.
#

project_dir=$(pwd)

source "$project_dir"/install_lib.sh

# default install target dir is specified on install lib
install_dir=$(get_install_iot_tools_dir)

# -- directories to install, for organization
register="register"

# Check if install dir was provided
if [ $# -eq 1 ]; then
	install_dir="$1"
fi

echo "Using install directory: $install_dir"

# rebuild jars
#cd $register/simple-aes-encrypter && mvn clean package && cd - > /dev/null
#cd progress-bar && mvn clean package && cd - > /dev/null
#sudo cp "$register"/simple-aes-encrypter/target/simple-aes-encrypter-*.jar ./simple-aes-encrypter.jar # name will then be used by script
#sudo cp progress-bar/target/progress-bar-*.jar ./progress-bar.jar # name will then be used by script

tmp_install_script="install.tmp"
touch $tmp_install_script
echo "
	sudo echo # prompt for password before progress bar appears
	source $project_dir/install_lib.sh

	sudo cp progress-bar.jar $install_dir/          # jar
	install_script_iot_tools progress-bar/progress         # wrapper script

	# install_script_iot_tools function gives execution permission and moves executable to instllation target folder
	install_script_iot_tools iot-connect
	install_script_iot_tools iot-push
	install_script_iot_tools iot-pull

	install_script_iot_tools $register/deregister-service
	install_script_iot_tools $register/register-service
	install_script_iot_tools $register/register-service-json
	install_script_iot_tools $register/retrieve-all-services
	install_script_iot_tools $register/jq-field
	install_script_iot_tools $register/retrieve-service-field
" > $tmp_install_script

pwd
# progress bar launches it's own shell that will prompt for password also

java -jar -DCOLUMNS=$(tput cols) ./progress-bar.jar $tmp_install_script || rm -f $tmp_install_script
rm -f $tmp_install_script
