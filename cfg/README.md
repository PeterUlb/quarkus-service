# Local Development Settings
If you need to override the defaults provided by the %dev profile, drop a `local.properties` file into this directory.  
Maven is configured to copy the `.properties` files into the `target/cfg` folder. SmallRye Config will search in this folder for configuration in `%dev` and `%test` mode.  
A good indicator for required settings is the `templates` folder. Combine both files into a `.properties` files as required

# Productive Usage
For productive usage, create a `configmap` and `secret` with the defined names. They will be automatically mounted and picked up. Use the templates from the `template` directory.