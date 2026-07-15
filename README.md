# Xenit enterprise conventions Gradle plugins

[![CI](https://github.com/xenit-eu/enterprise-conventions-gradle-plugin/workflows/CI/badge.svg)](https://github.com/xenit-eu/enterprise-conventions-gradle-plugin/actions?query=workflow%3ACI+branch%3Amaster)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/eu/xenit/enterprise-conventions/oss/eu.xenit.enterprise-conventions.oss.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=eu.xenit.enterprise-conventions.oss)](https://plugins.gradle.org/plugin/eu.xenit.enterprise-conventions.oss)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/eu/xenit/enterprise-conventions/private/eu.xenit.enterprise-conventions.private.gradle.plugin/maven-metadata.xml.svg?colorB=007ec6&label=eu.xenit.enterprise-conventions.private)](https://plugins.gradle.org/plugin/eu.xenit.enterprise-conventions.private)

These Gradle plugins apply general conventions for Xenit projects. There is a different set of conventions that is
applied to open source projects and to private projects.

## Installation

Open source projects should apply the `eu.xenit.enterprise-conventions.oss` plugin. Private projects should apply
the `eu.xenit.enterprise-conventions.private` plugin.

These plugins can be applied to individual Gradle projects in `build.gradle`, or for the whole build
in `settings.gradle`

<details>
<summary>Example</summary>

Apply for all projects in a build:

```groovy
// settings.gradle
plugins {
    id 'eu.xenit.enterprise-conventions.oss' version ...
}
```

Or only apply to a particular sub-project:

```groovy
// build.gradle
plugins {
    id 'eu.xenit.enterprise-conventions.oss' version ...
}
```

</details>

## Usage

### Publishing to Maven Central

When using the `eu.xenit.enterprise-conventions.oss` and the [`maven-publish`](https://docs.gradle.org/current/userguide/publishing_maven.html) plugin;
releases to Maven Central using the [Central Portal Publish API](https://central.sonatype.org/publish/publish-portal-api/) are automatically set up using [NMCP](https://gradleup.com/nmcp/).

Set the Gradle properties `mavenCentralPublishUsername` and `mavenCentralPublishPassword` to configure the publication username and password.
Publication can be done using the `publish` task.

You can set these properties in 2 ways, using command-line properties, or using environment variables.

| Command-line properties                                                                 | Environment variables                                                                                                                    |
|-----------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------------------|
| `./gradlew publish -PmavenCentralPublishUsername=XXX -PmavenCentralPublishPassword=YYY` | `export ORG_GRADLE_PROJECT_mavenCentralPublishUsername=XXX;export ORG_GRADLE_PROJECT_mavenCentralPublishPassword=YYY; ./gradlew publish` |


Note that Maven Central requires publication signing and complete POMs for releases; which needs to be set up as well.

<details>
<summary>Full configuration example</summary>

```groovy
// settings.gradle
plugins {
    id 'eu.xenit.enterprise-conventions.oss' version ...
}
```

```groovy
// root build.gradle
allprojects {
    pluginManager.withPlugin('maven-publish') {
        // Always apply signing plugin; signing is required for publishing to maven central
        apply plugin: 'signing'
        
        // Configure POM with all required fields for publishing to maven central
        publishing {
            publications {
                all {
                    pom {
                        url = ...
                        name = project.name
                        description = project.description

                        scm {
                            connection = ...
                            developerConnection = ...
                            url = ...
                        }

                        developers {
                            developer {
                                name = ...
                                organization = ...
                            }
                        }

                        licenses {
                            license {
                                name = ...
                                url = ...
                            }
                        }
                    }
                }
            }
        }
        
        // Configure publication of the library when using java-library plugin
        pluginManager.withPlugin('java-library') {
            publishing {
                publications {
                    library(MavenPublication) {
                        from components.java
                    }
                }
            }
        }
        
        // Configure publication of BOMs/platforms when using the platform plugin
        pluginManager.withPlugin('java-platform') {
            publishing {
                publications {
                    platform(MavenPublication) {
                        from components.javaPlatform
                    }
                }
            }
        }
    }
    
    // Optionally; make check task always include the maven central requirements check
    pluginManager.withPlugin('base') {
        tasks.named('check').configure {
            dependsOn('checkMavenCentralRequirements')
        }
    }
}
```
</details>

#### Publication signing

When the `eu.xenit.enterprise-conventions.oss`,
the [`maven-publish`](https://docs.gradle.org/current/userguide/publishing_maven.html) and
the [`signing` plugin](https://docs.gradle.org/current/userguide/signing_plugin.html) plugins are used together, signing
is automatically configured for all publications.

Which GPG key to use for signing artifacts can be automatically configured:

* If the `SIGNING_PRIVATE_KEY` and `SIGNING_PASSWORD` environment variables are present, these will be used
  for [in-memory signing](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:in-memory-keys). Optionally,
  the `SIGNING_SUBKEY_ID` environment variable can be used to select the OpenPGP subkey to use for signing.
* If the `signing.keyId`, `signing.password` and `signing.secretKeyRingFile` properties are present, these will be used
  for [default signatory credentials](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials)
* If the `signing.gnupg.keyName` property is present, it will be used for
  the [GnuPG signer](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:using_gpg_agent).

<details>
<summary>Example usages</summary>

**These are just examples, use your CI's method to insert secure environment variables instead of hardcoding them in CI
configuration**

With environment variables:

```commandline
export SIGNING_PRIVATE_KEY=XXXXXX # ascii-armored private key
export SIGNING_PASSWORD=YYYYY # password to unlock secret key
./gradlew publish
```

With properties:

```commandline
./gradlew publish -Psigning.keyId=01234 -Psigning.password=YYYYY -Psigning.secretKeyRingFile=~/.gnupg/secring.gpg
```

</details>

#### Publication validation

When the `eu.xenit.enterprise-conventions.oss` plugin is applied,
adherence to the [Maven Central requirements](https://central.sonatype.org/publish/requirements/#answer) is validated when publishing
to the Sonatype OSS repositories, also when `SNAPSHOT` builds are published to the snapshot repository.

This prevents the annoying occurrence when your fully finished and tagged release is rejected when closing your staging repository,
because it did not adhere to all requirements.

Not all requirements can be checked automatically, only those that can are checked here:

 * Presence of `javadoc.jar` and `sources.jar` when a `jar` is published. (There is no such requirement when publishing other artifact types, like Alfresco `.amp`)
 * All artifacts must be signed with GPG
 * POM contains following metadata:
   * `name`, `description`, `url`
   * At least one `license`, every license must contain `name` and `url`
   * At least one `developer`, every developer must contain `name`
   * `scm` must contain `connection`, `developerConnection` and `url`


### Docker image source labels

When building docker images with the [Gradle Docker plugin](https://github.com/bmuschko/gradle-docker-plugin), [Alfresco Docker Gradle plugin](https://github.com/xenit-eu/alfresco-docker-gradle-plugin) or the [Spring Boot Gradle Plugin](https://docs.spring.io/spring-boot/docs/current/gradle-plugin/reference/htmlsingle/),
some predefined [OCI annotations](https://github.com/opencontainers/image-spec/blob/main/annotations.md) are automatically applied:

* `org.opencontainers.image.source`: Set to the repository URL (if available from a supported source provider)
* `org.opencontainers.image.revision`: Set to the current commit (if available from a supported source provider)
* `org.opencontainers.image.version`: Set to the version of the Gradle project the Docker image is built in
* `org.opencontainers.image.title`: Set to the name of the Gradle project the Docker image is built in
* `org.opencontainers.image.description`: Set to the description of the Gradle project the Docker image is built in

#### Supported source providers

* GitHub Actions: information is read from environment variables set by GitHub Actions
* Supporting other sources: provide a jar containing a [`BuildContextInformationSupplier](src/main/java/eu/xenit/gradle/enterprise/conventions/extensions/dockerimagelabels/BuildContextInformationSupplier.java) SPI

### Multi-arch Docker images

Plugin id: `eu.xenit.enterprise-conventions.ext.docker-multiarch`

[Buildpacks](https://docs.spring.io/spring-boot/gradle-plugin/packaging-oci-image.html) (`bootBuildImage`)
build a single architecture per run, so multi-arch support is provided as separate per-architecture images.
For every project with the Spring Boot plugin, this extension registers two extra tasks:

* `bootBuildLinuxAmd64Image` and `bootBuildLinuxArm64Image` build (and with `--publishImage`, publish) the
  image configured on `bootBuildImage` for that specific platform. The image name and tags are derived from
  `bootBuildImage`, with an architecture suffix (e.g. `my-image:1.0-amd64`).
* The amd64 image also carries the unsuffixed name and tags, so existing consumers of those keep working
  and amd64 is only built once.
* `bootBuildImage` itself is untouched: it remains the host-architecture build for local development.
* The `docker.publishRegistry` credentials and the buildpack `environment` configured on `bootBuildImage`
  apply to the per-arch tasks too; no additional configuration is needed.

The derived configuration is: `archiveFile`, `imageName`, `tags`, `environment` and `docker.publishRegistry`.
Other `bootBuildImage` customizations (`builder`, `runImage`, `bindings`, ...) are not derived; configure
those on the per-arch tasks explicitly if a project uses them.

Requires Spring Boot >= 3.4 (`imagePlatform` support); on older versions the tasks fail when they are used.
When upgrading a project that already registers its own `bootBuildLinuxAmd64Image`/`bootBuildLinuxArm64Image`
tasks, remove those local definitions: the names would collide with the tasks this extension registers.

#### CI configuration

Building the non-native architecture uses emulation, so the CI runner needs QEMU. A typical publish job:

```yaml
      - name: Set up QEMU for cross-architecture image builds
        if: ${{ startsWith(github.ref, 'refs/heads/main') || startsWith(github.ref, 'refs/tags/v') }}
        uses: docker/setup-qemu-action@96fe6ef7f33517b61c61be40b68a1882f3264fb8 # v4.2.0
      - name: Push docker images
        if: ${{ startsWith(github.ref, 'refs/heads/main') || startsWith(github.ref, 'refs/tags/v') }}
        run: ./gradlew bootBuildLinuxAmd64Image --publishImage bootBuildLinuxArm64Image --publishImage
        env:
          ORG_GRADLE_PROJECT_DOCKER_PUBLISH_REGISTRY_URL: docker.xenit.eu
          ORG_GRADLE_PROJECT_DOCKER_PUBLISH_REGISTRY_USERNAME: ${{ secrets.HARBOR_USER }}
          ORG_GRADLE_PROJECT_DOCKER_PUBLISH_REGISTRY_PASSWORD: ${{ secrets.HARBOR_PASSWORD }}
```

(The `ORG_GRADLE_PROJECT_*` environment variables assume the project wires `docker.publishRegistry` from
Gradle properties, as is the convention; adjust to the project's own credential configuration otherwise.)
