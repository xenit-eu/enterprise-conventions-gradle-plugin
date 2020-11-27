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

apply plugin: eu.xenit.enterprise.PrivateInitPlugin
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

