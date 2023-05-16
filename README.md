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
    id 'eu.xenit.enterprise-conventions.oss' version '0.1.0'
}
```

Or only apply to a particular sub-project:

```groovy
// build.gradle
plugins {
    id 'eu.xenit.enterprise-conventions.oss' version '0.1.0'
}
```

</details>

## Usage

### Repository shorthands

Both OSS and Private plugins provide additional shorthands for your `repositories {}` block:

* `sonatypeSnapshots()`: Configures https://oss.sonatype.org/ AND https://s01.oss.sonatype.org/ snapshot repositories. Additional snapshot repositories will be added when they are created.
* `xenit()`: Configures Xenit private repository (Release) with credentials from `eu.xenit.repo.username` and `eu.xenit.repo.password`
* `xenitSnapshots()`: Configures Xenit private repository (Snapshots) with credentials from `eu.xenit.repo.username` and `eu.xenit.repo.password`
* `xenitPrivate()` (**Deprecated**): Configures Xenit private artifacts server (Releases) with credentials
  from `eu.xenit.artifactory.username` and `eu.xenit.artifactory.password` properties
* `xenitPrivateSnapshots()` (**Deprecated**): Configures Xenit private artifacts server (Snapshots) with credentials
  from `eu.xenit.artifactory.username` and `eu.xenit.artifactory.password` properties

Similarly, these shorthands can also be used in `settings.gradle` in a `dependencyResolutionManagement.repositories {}` block,
which is the recommended way to configure repositories if they are used in all subprojects.

`dependencyResolutionManagement` in `settings.gradle` is supported in Gradle 6.8 and newer.

All repositories can be configured further by configuring it in a block.

<details>
<summary>Example</summary>

```groovy
repositories {
    sonatypeSnapshots()
    xenit()
    xenitSnapshots()
}
```

```groovy
repositories {
    xenit {
        // Example additional configuration.
        // See https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/repositories/MavenArtifactRepository.html
        content {
            includeGroup "eu.xenit"
        }
    }
}
```

```groovy
// settings.gradle
dependencyResolutionManagement {
  repositories {
    xenit()
  }
}
```

</details>

### Publishing shorthands

When the [`maven-publish` plugin](https://docs.gradle.org/current/userguide/publishing_maven.html) is used, additional
repository shorthands are available on the `publishing.repositories {}` block. In addition to the repositories listed
above, `sonatypeMavenCentral()` is also available, which transparently sets up
the [nexus publish plugin](https://github.com/marcphilipp/nexus-publish-plugin)
to automatically deploy to a staging repository.

Note that you should use the `sonatypeSnapshots()` repository for publishing snapshots.

<details>
<summary>Example</summary>

```groovy
publishing {
  repositories {
    // Switch which repository is used based on if the version is a snapshot
    if("${project.version}".endsWith('-SNAPSHOT')) {
      sonatypeSnapshots {
        // The default is https://oss.sonatype.org/content/repositories/snapshots/
        url = "https://s01.oss.sonatype.org/content/repositories/snapshots/"
        credentials {
          username 'XYZ'
          password 'some-password'
        }
      }
    } else {
      sonatypeMavenCentral {
        // If you need to publish to a different repository
        // The default is https://oss.sonatype.org/service/local/
        url = "https://s01.oss.sonatype.org/service/local/"
        credentials {
          username 'XYZ'
          password 'some-password'
        }
      }
    }
  }
}
```

</details>

### Publication signing

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

## Publication validation

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


## Repository blocking

In the `eu.xenit.enterprise-conventions.oss` plugin, all artifact repositories are allowed by default, except for the
Xenit private artifacts server. This is to avoid accidentally depending on this private server for open source software
that we publish.

In all cases, local `file:///` repositories are allowed and `http://` repositories are blocked. It is possible to add
additional repositories to the allow- or blocklists by adding properties to `gradle.properties` (either globally or
per-project):
`eu.xenit.enterprise-conventions.repository.allow.<hostname>=true`
or `eu.xenit.enterprise-conventions.repository.block.<hostname>=true`
Entries that are added to the blocklist in this way take priority over entries that are added to the allowlist.

<details>
<summary>Example</summary>

These properties-files can be placed in `~/.gradle/gradle.properties`, or locally in your project as `gradle.properties`
.

```properties
# Allow jcenter back, even though it is blocked by default
eu.xenit.enterprise-conventions.repository.allow.jcenter.org=true
# Block repository on example.com, even though it may be allowed by default
eu.xenit.enterprise-conventions.repository.block.example.com=true
```

</details>
