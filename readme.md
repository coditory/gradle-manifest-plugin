# Manifest Gradle Plugin

[![Join the chat at https://gitter.im/coditory/gradle-manifest-plugin](https://badges.gitter.im/coditory/gradle-manifest-plugin.svg)](https://gitter.im/coditory/gradle-integration-test-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.com/coditory/gradle-manifest-plugin.svg?branch=master)](https://travis-ci.com/coditory/gradle-manifest-plugin)
[![Coverage Status](https://coveralls.io/repos/github/coditory/gradle-manifest-plugin/badge.svg)](https://coveralls.io/github/coditory/gradle-manifest-plugin)
[![Gradle Plugin Portal](https://img.shields.io/badge/Plugin_Portal-v0.1.7-green.svg)](https://plugins.gradle.org/plugin/com.coditory.manifest)

**Zero configuration**, **single responsibility** gradle plugin for generating project metadata to [jar manifest file](https://docs.oracle.com/javase/tutorial/deployment/jar/manifestindex.html).

- Runs `manifest` task after [`processResources`](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks)
- `manifest` task creates manifest file in `build/resources/main/META-INF/MANIFEST.MF` - [default output for java resources](https://docs.gradle.org/current/userguide/java_plugin.html#sec:source_set_properties)

## Enabling the plugin

Add to your `build.gradle`:

```gradle
plugins {
  id 'com.coditory.manifest' version '0.1.7'
}
```

## Usage
This plugin automatically adds metadata to manifest file after processResources.
There is nothing you need to do.

For debug purposes you can run `manifest` task with some flags:
```sh
# Generates manifest file to build/resources/main/META-INF/MANIFEST.MF
./gradlew manifest

# Generates manifest and prints its content
./gradlew manifest --print

# Generates manifest to src/main/resources/META-INF/MANIFEST.MF
# Sometimes it's useful for debugging
./gradlew manifest --main
```

## Overriding generated attributes
To override generated manifest attributes specify your own values in `build.gradle`

```gradle
jar {
  manifest {
    attributes(
      'Implementation-Title': 'hello-world'
      'Main-Class': 'hello.HelloWorld'
    )
  }
}
```

## Generated manifests

Sample generated manifest:

```
Manifest-Version: 1.0
Main-Class: com.coditory.Application
Implementation-Title: sample-project
Implementation-Group: com.coditory
Implementation-Version: 0.0.1-SNAPSHOT
Built-By: john.doe
Built-Host: john-pc
Built-Date: 2020-03-25T20:46:59Z
Built-OS: Linux 4.15.0-91-generic amd64
Built-JDK: 12.0.2 AdoptOpenJDK
SCM-Repository: git@github.com:coditory/gradle-manifest-plugin.git
SCM-Branch: refs/heads/master
SCM-Commit-Message: Very important commit
SCM-Commit-Hash: ef2c3dcabf1b0a87a90e098d1a6f0341f0ae1adf
SCM-Commit-Author: John Doe <john.doe@acme.com>
SCM-Commit-Date: 2020-03-24T19:46:03Z
```
