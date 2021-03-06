# imageservice project

Image service project using quarkus.

## Configuration

### Defaults

Defaults are provided for all profiles in `src/main/resources/application.properties`. Some profiles also have profile
specific defaults.

### Override

Property `smallrye.config.locations` is provided to provide override locations.  
Properties that must be provided can be found in `./cfg/templates`. Some of them might have sane default properties
already.

#### General Notes

Make sure to include the following line at the beginning of the .properties files:  
`config_ordinal=<256-3599>`  
**NOTE:** The config ordinal must be >255 to override the defaults, but also under 3600 due to quarkus interceptors
etc.  
All files in the folder support the profile notation: `%profile.x.y.z=abc`

#### dev & test

Additional configs are picked up from the ./cfg directory (and copied to ./target/cfg). The following file overrides the
postgres url:

```
./cfg/local.properties
-------
config_ordinal=1000
%dev.quarkus.datasource.jdbc.url=jdbc:postgresql://127.0.0.1:5432/myotherdb
```

#### prod (k8s)

The kubernetes deployments sets the environment variable `SMALLRYE_CONFIG_LOCATIONS=/mnt/app-config-map,/mnt/app-secret`
.  
Templates for both locations can be found in `./cfg/templates`.  
It is recommended to pass the prod variable with `VM Options = -Dquarkus.profile=a,b,prod`, assign the highest ordinal
and prefix all properties with `%prod`.

## Dependencies

The following services are expected to run and be configured (for integration tests `wiremock` and `testcontainers` are
used).

1. Postgres db
2. Cloud Storage
3. PubSub
4. Redis
5. Keycloak

This project provides a `docker-compose.yml` file in `/src/main/docker/mock` to set up dependencies. You can of course
override e.g. the postgres url (as mentioned above) to point to another instance, e.g. a locally installed one.

## JWT from Dev Keycloak

The following command will create an access token in the dev landscape (if docker-compose is used):

`curl --insecure -X POST http://localhost:7072/auth/realms/image-service/protocol/openid-connect/token     --user image-service:secret -H 'content-type: application/x-www-form-urlencoded' -d 'username=alice&password=alice&grant_type=password' | jq --raw-output '.access_token'`

## SmallRye Config Behavior

The `smallrye.config.locations` property behavior is currently not fully documented. It behaves as follows:  
If a *directory* is specified, all .properties files are loaded. `%profile.` is respected. Config ordinal must be high
enough to override the defaults.  
If a *file* is specified (e.g. `./cfg/my.properties`), it is loaded, but also all `my-{profile}.properties`. The profile
files act like they would have been prefixed with `%profile`.

**Recommendation**: Use the directory based approach. Different files can be prefixed with the relevant profiles.  
For example one file with %prod only.

## FixMe

`src/main/resources` is no longer automatically picked up as a resource