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

Additionally, the `eu.xenit.enterprise-conventions.private.init` plugin should be applied to Xenit private CI servers.

<details>
<summary>Configure Xenit CI server</summary>

Locate your `~/.gradle/init.d/` folder for configuration:

* On Windows: A (hidden) `.gradle` folder is located in your user folder.
* On Linux: You can browse to the `~/.gradle/init.d/` folder.

Create a new file in this folder named `xenit-enterprise-conventions.gradle` with the following contents:

```groovy
initscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath 'eu.xenit.gradle:enterprise-conventions-plugin:+'
    }
}

apply plugin: eu.xenit.gradle.enterprise.conventions.PrivateInitPlugin
```

</details>

## Usage

### Repository shorthands

Both OSS and Private plugins provide additional shorthands for your `repositories {}` block:

* `sonatypeSnapshots()`: Configures https://oss.sonatype.org/content/repositories/snapshots/ repository
* `xenitPrivate()`: Configures Xenit private artifacts server (Releases) with credentials
  from `eu.xenit.artifactory.username` and `eu.xenit.artifactory.password` properties
* `xenitPrivateSnapshots()`: Configures Xenit private artifacts server (Snapshots) with credentials
  from `eu.xenit.artifactory.username` and `eu.xenit.artifactory.password` properties

All repositories can be configured further by configuring it in a block.

<details>
<summary>Example</summary>

```groovy
repositories {
    sonatypeSnapshots()
    xenitPrivate()
    xenitPrivateSnapshots()
}
```

```groovy
repositories {
    xenitPrivate {
        // Example additional configuration.
        // See https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/repositories/MavenArtifactRepository.html
        content {
            includeGroup "eu.xenit"
        }
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

<details>
<summary>Example</summary>

```groovy
publishing {
    repositories {
        sonatypeMavenCentral {
            credentials {
                username 'XYZ'
                password 'some-password'
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
  for [in-memory signing](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:in-memory-keys)
* If the `signing.keyId`, `signing.password` and `signing.secretKeyRingFile` properties are present, these will be used
  for [default signatory credentials](https://docs.gradle.org/current/userguide/signing_plugin.html#sec:signatory_credentials)

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
./gradlew publish -Psigning.keyId=01234 -Psigning.password=YYYYY -P signing.secretKeyRingFile=~/.gnupg/secring.gpg
```

</details>

## Repository blocking

In the `eu.xenit.enterprise-conventions.oss` plugin, all artifact repositories are allowed by default, except for the
Xenit private artifacts server. This is to avoid accidentally depending on this private server for open source software
that we publish.

In the `eu.xenit.enterprise-conventions.private` plugin, only select artifact repositories are allowed: Maven Central,
Gradle Plugin Portal and all artifact repositories that are proxied by the Xenit private artifacts server. These
repositories are selected as trusted sources because they verify groupId ownership and don't allow artifacts to be
replaced or delete after publication.

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
