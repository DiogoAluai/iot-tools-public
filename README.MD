# Conventions
Identity files for ssh authentication are assumed to be named: serviceName_id.
The "_id" prefix will be added programmatically to access it under the .ssh folder.

# Instalation
The "instal.sh" script is provided for this purpose. It copies scripts and dependencies to "/usr/bin/"

# Registering service
To register a service in the registry, you can use the provided script.
This will generate a temp json file to create a service with arguments that were passed.
Take care to pass a parseable service type. It will be implemented in some services-api module.
Also, either private or public ip should be set, and should be consistent with the type, i.e. if it's a public service it should then have a public IP, wouldn't you agree??
Also, careful with escape characters!

There is a register-service script that takes 5 arguments, the 5 fields. Then there's the register-service-json
that will take the json as argument. Registration is centralized in the latter. The former will create a json and pass to it.

# Deregistering service
Just use the script, passing the name as argument

# Dependencies
Uses environment variables: COOL_REGISTRY_URL.
