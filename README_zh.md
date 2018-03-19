# RapidMavenPushPlugin

用于上传你的 library 库到多个 Maven 仓库的 Gradle 插件。

## 1. 怎么使用

### 1.1 添加依赖

在你项目根目录的 `build.gradle` 文件中增加 `RapidMavenPushPlugin` 依赖：

**[检查最新版本](http://search.maven.org/#search%7Cga%7C1%7Crapidmavenpush)**

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

### 1.2 创建 properties files

现在你有3个 Maven 仓库（`maven type`s），所以需要创建3个 maven properties archive 文件和1个通用的 archive properties 文件（properties 文件的名字和位置可以是任意的）：

- **maven_local.properties**: 上传 archives 到本地的 maven 仓库, 默认在你电脑的 `~/.m2/repository`。
- **maven_company.properties**: 上传 archives 到你公司的 maven 仓库，他部署在你公司的服务器上面。
- **maven_central.properties**: 上传 archives 到 maven 中央库。
- **common.properties**: 上面3个 maven 仓库的通用 properties。

> **NOTE**: 当 `project.afterEvaluate` 时, 所有 properties 都会被自动注入到 `project.ext`中，所以在那之后你可以以诸如 `$POM_ARCHIVE_ID` 的方式来使用它们。

#### 1.2.1 maven_common.properties

```
# project info
POM_ARCHIVE_GROUP=com.github.wangjiegulu
#POM_ARCHIVE_VERSION_NAME=0.0.1-SNAPSHOT (command typed)
# aar 或者 jar 或者 不设置(插件会自动解析)
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

### 1.3 应用 Plugin & properties

在你 library 的 `build.gradle` 文件中, 你需要以如下方式来 apply RapidMavenPushPlugin 插件：

```gradle
apply plugin: 'com.github.wangjiegulu.plg.rapidmavenpush'

rapidMavenPush {
    // 如果是 true，会在编译时期打印被注入的 properties
    printProperties = true
    // 如果是 true，在注入 properties 发生错误时会终止编译
    abortOnError = false
    // 是否禁用 Rapid Maven Push Plugin
    disable = false
    // 如果 `POM_MAVEN_TYPE` 没有被设置，则使用默认的 maven type。
    defaultMavenType = 'local'
    mavens {
        maven {
            mavenType = 'local'
            propertyFiles = [
                    file("mavenupload/maven_common.properties"),
                    file("mavenupload/maven_local.properties")
            ]
            // Property Inject Mode: If the properties is already set, replace it or skip
            // property 注入模式：如果 properties 已经被设置过，则替换还是跳过
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

### 1.4 上传 Archives

在编译之后，rapid maven push plugin 自动创建了一个名为 `rapidUploadArchives` 的 task。

执行这个 task !

**上传 archives 到本地仓库:**

```
./gradlew clean :depmodule:rapidUploadArchives -PPOM_MAVEN_TYPE=local
```

**上传 archives to 到中央库:**

```
./gradlew clean :depmodule:rapidUploadArchives -PPOM_MAVEN_TYPE=central
```

> **NOTE**: 如果你没有在 `build.gradle` 使用 `ext.POM_MAVEN_TYPE=xxx` 的方式进行设置的话，`POM_MAVEN_TYPE` 参数是必要的。


### 1.5 支持的 parameters & properties

```
// maven type, 只能通过在 `build.gradle` 设置 `ext.POM_MAVEN_TYPE=xxx` 或者在命令行中设置 `-PPOM_MAVEN_TYPE=xxx` 或者在 `gradle.properties` 中设置 `POM_MAVEN_TYPE=xxx`
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


