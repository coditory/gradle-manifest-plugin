# Manifest Gradle Plugin

[![Build](https://github.com/coditory/gradle-manifest-plugin/actions/workflows/build.yml/badge.svg)](https://github.com/coditory/gradle-manifest-plugin/actions/workflows/build.yml)
[![Coverage Status](https://coveralls.io/repos/github/coditory/gradle-manifest-plugin/badge.svg?branch=master)](https://coveralls.io/github/coditory/gradle-manifest-plugin?branch=master)
[![Gradle Plugin Portal](https://img.shields.io/badge/Plugin_Portal-v0.2.5-green.svg)](https://plugins.gradle.org/plugin/com.coditory.manifest)

**Single responsibility** gradle plugin for generating project metadata
to [jar manifest file](https://docs.oracle.com/javase/tutorial/deployment/jar/manifestindex.html).

- Runs `manifest` task
  after [`processResources`](https://docs.gradle.org/current/userguide/java_plugin.html#sec:java_tasks)
- `manifest` task creates manifest file in `build/resources/main/META-INF/MANIFEST.MF`

## Installing the plugin

Add to your `build.gradle`:

```gradle
plugins {
  id 'com.coditory.manifest' version '0.2.5'
}
```

## Generated manifest

Sample `MANIFEST.MF` generated for default configuration:

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

## Usage

This plugin automatically adds metadata to manifest file after processResources. There is nothing you need to do.

For debug purposes you can run `manifest` task with some flags:

```sh
# Generates manifest file to build/resources/main/META-INF/MANIFEST.MF
./gradlew manifest

# Generates manifest and prints its content
./gradlew manifest --print

# Generates manifest to src/main/resources/META-INF/MANIFEST.MF
./gradlew manifest --main
```

## Overriding generated attributes

To disable or override generated manifest attributes configure the plugin in `build.gradle`

```gradle
manifest {
    buildAttributes = false
    implementationAttributes = true
    scmAttributes = false
    attributes = [
        "Custom-1": "Custom-Value",
        "Custom-2": 123
    ]
}
```

## Generating classpath attribute

To generate class path attribute with all compile dependencies prefixed with a custom directory use `classpathPrefix`:

```gradle
manifest {
    classpathPrefix = "my-jars"
}
```

See Java tutorial
on [Adding Classes and Jars to Jar File's Classpath](https://docs.oracle.com/javase/tutorial/deployment/jar/downman.html)

## Reading `MANIFEST.MF` from project

Take a look at a [gradle-manifest-plugin-sample](https://github.com/coditory/gradle-manifest-plugin-sample).
You can copy [ManifestReader.java](https://github.com/coditory/gradle-manifest-plugin-sample/blob/master/src/main/java/com/coditory/sandbox/ManifestReader.java) (with [tests](https://github.com/coditory/gradle-manifest-plugin-sample/blob/master/src/test/groovy/com/coditory/sandbox/ManifestReaderTest.groovy)) to you own project.

Reading `MANIFEST.MF` is tricky. There can be multiple `MANIFEST.MF` files on the classpath that comes from libraries.
To read the correct `MANIFEST.MF` you need to filter them. Most often `Implementation-Title` is used to find the correct one.

