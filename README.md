# RapidMavenPushPlugin

A Gradle plugin : Upload your library Artifacts to Multi Maven Repository.

[中文版本](README_zh.md)

## 1. How to use

### 1.1 Dependencies

Add `RapidMavenPushPlugin` dependencies in `build.gradle` of your root project:

**[Check Newest Version](http://search.maven.org/#search%7Cga%7C1%7Crapidmavenpush)**

```gradle
buildscript {

    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:3.0.1'
        classpath('com.github.wangjiegulu:rapidmavenpush:x.x.x') {
            exclude group: 'com.android.tools.build', module: 'gradle'
        }

    }
}
```

### 1.2 Create properties files

Now you have 3 `maven type`s, so create 3 maven upload archive properties files & 1 common archive properties file (properties file's name & location can be arbitrary):

- **maven_local.properties**: Upload archives to local maven repository, default in your computer is `~/.m2/repository`.
- **maven_company.properties**: Upload archives to your company's maven repository which deploy in your company server.
- **maven_central.properties**: Upload archives to maven central repository.
- **common.properties**: Common properties for 3 maven types above.

> **NOTE**: When `project.afterEvaluate`, all properties are automatically injected into `project.ext`, so you can use it like `$POM_ARCHIVE_ID` after that.

#### 1.2.1 maven_common.properties

```
# project info
POM_ARCHIVE_GROUP=com.github.wangjiegulu
#POM_ARCHIVE_VERSION_NAME=0.0.1-SNAPSHOT (command typed)
# aar or jar or unset(parse automatically by plugin)
#POM_PACKAGING=aar
POM_DESC=test-mavenpush-plugin
POM_URL=https://github.com/wangjiegulu
POM_SCM_URL=scm:git@github.com:wangjiegulu
POM_SCM_CONNECTION=scm:git@github.com:wangjiegulu
POM_SCM_DEV_CONNECTION=git@github.com:wangjiegulu
POM_LICENCE_NAME=The Apache Software License, Version 2.0
POM_LICENCE_URL=http://www.apache.org/licenses/LICENSE-2.0.txt
POM_LICENCE_DIST=wangjie
POM_DEVELOPER_ID=wangjie
POM_DEVELOPER_NAME=Wang Jie
POM_DEVELOPER_EMAIL=tiantian.china.2@gmail.com
```

#### 1.2.2 maven_local.properties

```
# maven repository
POM_REPOSITORY_URL=/Users/wangjie/.m2/repository
POM_REPOSITORY_URL_SNAPSHOT=/Users/wangjie/.m2/repository
POM_SIGN=false

# project info
POM_ARCHIVE_ID=mavenpush-plugin-depmodule-local

```

#### 1.2.3 maven_central.properties

```
POM_OSSRH_USERNAME=username
POM_OSSRH_PASSWORD=password

# maven repository
POM_REPOSITORY_URL=https://oss.sonatype.org/service/local/staging/deploy/maven2/
POM_REPOSITORY_URL_SNAPSHOT=https://oss.sonatype.org/content/repositories/snapshots/

POM_SIGN=true
# Already configure in ~/.gradle/gradle.properties
#signing.keyId=
#signing.password=
#signing.secretKeyRingFile=

# project info
POM_ARCHIVE_ID=mavenpush-plugin-depmodule
```

### 1.3 Apply Plugin & properties

In `build.gradle` of your library, you need to apply RapidMaven PushPlugin as below:

```gradle
apply plugin: 'com.github.wangjiegulu.plg.rapidmavenpush'

rapidMavenPush {
    // If true, print injected properties in compile time
    printProperties = true
    // If true, abort compile when inject properties error
    abortOnError = false
    // If disable Rapid Maven Push Plugin
    disable = false
    // Default maven type if `POM_MAVEN_TYPE` is not set.
    defaultMavenType = 'local'
    mavens {
        maven {
            mavenType = 'local'
            propertyFiles = [
                    file("mavenupload/maven_common.properties"),
                    file("mavenupload/maven_local.properties")
            ]
            // Property Inject Mode: If the properties is already set, replace it or skip
            propertyInjectMode = 'replace'
        }
        maven {
            mavenType = 'company'
            propertyFiles = [
                    file("mavenupload/maven_common.properties"),
                    file("mavenupload/maven_company.properties")
            ]
            propertyInjectMode = 'replace'
        }
        maven {
            mavenType = 'central'
            propertyFiles = [
                    file("mavenupload/maven_common.properties"),
                    file("mavenupload/maven_central.properties")
            ]
            propertyInjectMode = 'replace'
        }
    }
}
```

### 1.4 Upload Archives

After compile, rapid maven push plugin created a task automatically named `rapidUploadArchives`.

Just run this task!

**Upload archives to local repository:**

```
./gradlew clean :depmodule:rapidUploadArchives -PPOM_MAVEN_TYPE=local
```

**Upload archives to central repository:**

```
./gradlew clean :depmodule:rapidUploadArchives -PPOM_MAVEN_TYPE=central
```

> **NOTE**: `POM_MAVEN_TYPE` parameter is necessary if you haven't set it in `build.gradle` with `ext.POM_MAVEN_TYPE=xxx`.

### 1.5 Supported parameters & properties

```
// maven type, only `ext.POM_MAVEN_TYPE=xxx` in build.gradle or `-PPOM_MAVEN_TYPE=xxx` in command or `POM_MAVEN_TYPE=xxx` in `gradle.properties`.
POM_MAVEN_TYPE

// maven repository parameters
POM_REPOSITORY_URL
POM_REPOSITORY_URL_SNAPSHOT
POM_OSSRH_USERNAME
POM_OSSRH_PASSWORD

// sign parameters
POM_SIGN
signing.keyId
signing.password
signing.secretKeyRingFile

// archive parameters
POM_ARCHIVE_GROUP
POM_ARCHIVE_ID
POM_ARCHIVE_VERSION_NAME
POM_PACKAGING
POM_DESC
POM_URL
POM_SCM_URL
POM_SCM_CONNECTION
POM_SCM_DEV_CONNECTION
POM_LICENCE_NAME
POM_LICENCE_URL
POM_LICENCE_DIST
POM_DEVELOPER_ID
POM_DEVELOPER_NAME
POM_DEVELOPER_EMAIL
```

License
=======

```
Copyright 2018 Wang Jie

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```


