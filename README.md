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

A good overview for required dependencies can be found in the `/cfg/templates` folder. The following services are
expected to run and be configured (for integration tests `wiremock` and `testcontainers` are used).

1. Postgres db
2. S3
3. SQS
4. Redis

Example for local development

```
docker run --ulimit memlock=-1:-1 -it --rm=true --memory-swappiness=0 --name redis_ratelimit_test -p 6379:6379 redis:5.0.6
```

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