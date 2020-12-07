# Xenit enterprise conventions Gradle plugins

These Gradle plugins apply general conventions for Xenit projects.
There is a different set of conventions that is applied to open source projects and to private projects.

## Installation

Open source projects should apply the `eu.xenit.enterprise.oss` plugin. Private projects should apply the `eu.xenit.enterprise.private` plugin.

These plugins can be applied to individual Gradle projects in `build.gradle`, or for the whole build in `settings.gradle`

<details>
<summary>Example</summary>

Apply for all projects in a build:

```groovy
// settings.gradle
plugins {
    id 'eu.xenit.enterprise.oss' version '0.1.0'
}
```

Or only apply to a particular sub-project:
```groovy
// build.gradle
plugins {
    id 'eu.xenit.enterprise.oss' version '0.1.0'
}
```

</details>

Additionally, Xenit engineers should apply the `eu.xenit.enterprise.private.init` plugin in their global Gradle initscript.

<details>
<summary>Example</summary>

Locate your `~/.gradle/init.d/` folder for configuration:
 * On Windows: A (hidden) `.gradle` folder is located in your user folder.
 * On Linux: You can browse to the `~/.gradle/init.d/` folder.
 
Create a new file in this folder named `xenit-enterprise.gradle` with the following contents:

```groovy
initscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath 'eu.xenit.gradle:enterprise-plugin:+'
    }
}

apply plugin: eu.xenit.gradle.enterprise.PrivateInitPlugin
```

</details>

## Usage

### Repository shorthands

Both OSS and Private plugins provide additional shorthands for your `repositories {}` block:
    
 * `sonatypeSnapshots()`: Configures https://oss.sonatype.org/content/repositories/snapshots/ repository
 * `xenitPrivate()`: Configures Xenit private artifacts server (Releases) with credentials from `eu.xenit.artifactory.username` and `eu.xenit.artifactory.password` properties
 * `xenitPrivateSnapshots()`: Configures Xenit private artifacts server (Snapshots) with credentials from `eu.xenit.artifactory.username` and `eu.xenit.artifactory.password` properties
    
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

When the [`maven-publish` plugin](https://docs.gradle.org/current/userguide/publishing_maven.html) is used,
additional repository shorthands are available on the `publishing.repositories {}` block. In addition to the repositories
listed above, `sonatypeMavenCentral()` is also available, which transparently sets up the [nexus publish plugin](https://github.com/marcphilipp/nexus-publish-plugin)
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

## Repository blocking

In the `eu.xenit.enterprise.oss` plugin, all artifact repositories are allowed by default, except for the Xenit private artifacts server.
This is to avoid accidentally depending on this private server for open source software that we publish.

In the `eu.xenit.enterprise.private` plugin, only select artifact repositories are allowed: Maven Central, Gradle Plugin Portal and all artifact repositories that are proxied by the Xenit private artifacts server.
These repositories are selected as trusted sources because they verify groupId ownership and don't allow artifacts to be replaced or delete after publication.

In all cases, local `file:///` repositories are allowed and `http://` repositories are blocked.
It is possible to add additional repositories to the allow- or blocklists by adding properties to `gradle.properties` (either globally or per-project):
`eu.xenit.enterprise.repository.allow.<hostname>=true` or `eu.xenit.enterprise.repository.block.<hostname>=true`
Entries that are added to the blocklist in this way take priority over entries that are added to the allowlist.

<details>
<summary>Example</summary>

These properties-files can be placed in `~/.gradle/gradle.properties`, or locally in your project as `gradle.properties`.

```properties
# Allow jcenter back, even though it is blocked by default
eu.xenit.enterprise.repository.allow.jcenter.org=true

# Block repository on example.com, even though it may be allowed by default
eu.xenit.enterprise.repository.block.example.com=true
```

</details>
