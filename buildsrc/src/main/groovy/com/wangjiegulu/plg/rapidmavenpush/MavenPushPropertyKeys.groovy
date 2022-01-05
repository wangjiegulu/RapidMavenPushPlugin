package com.wangjiegulu.plg.rapidmavenpush

class MavenPushPropertyKeys {
    // maven type, `ext.POM_MAVEN_TYPE=xxx` in build.gradle or `-PPOM_MAVEN_TYPE=xxx` in command.
    static String POM_MAVEN_TYPE = "POM_MAVEN_TYPE"

    // maven repository parameters
    static String POM_REPOSITORY_URL = "POM_REPOSITORY_URL"
    static String POM_REPOSITORY_URL_SNAPSHOT = "POM_REPOSITORY_URL_SNAPSHOT"
    static String POM_OSSRH_USERNAME = "POM_OSSRH_USERNAME"
    static String POM_OSSRH_PASSWORD = "POM_OSSRH_PASSWORD"

    // sign parameters
    static String POM_SIGN = "POM_SIGN"
    static String POM_SIGNING_KEY_ID = "signing.keyId"
    static String POM_SIGNING_PASSWORD = "signing.password"
    static String POM_SIGNING_SECRET_KEY_RING_FILE = "signing.secretKeyRingFile"

    // archive parameters
    // is open source, default true
    static String POM_OPEN_SOURCE = "POM_OPEN_SOURCE"
    static String POM_ARCHIVE_GROUP = "POM_ARCHIVE_GROUP"
    static String POM_ARCHIVE_ID = "POM_ARCHIVE_ID"
    static String POM_ARCHIVE_VERSION_NAME = "POM_ARCHIVE_VERSION_NAME"
    static String POM_PACKAGING = "POM_PACKAGING"
    static String POM_DESC = "POM_DESC"
    static String POM_URL = "POM_URL"
    static String POM_SCM_URL = "POM_SCM_URL"
    static String POM_SCM_CONNECTION = "POM_SCM_CONNECTION"
    static String POM_SCM_DEV_CONNECTION = "POM_SCM_DEV_CONNECTION"
    static String POM_LICENCE_NAME = "POM_LICENCE_NAME"
    static String POM_LICENCE_URL = "POM_LICENCE_URL"
    static String POM_LICENCE_DIST = "POM_LICENCE_DIST"
    static String POM_DEVELOPER_ID = "POM_DEVELOPER_ID"
    static String POM_DEVELOPER_NAME = "POM_DEVELOPER_NAME"
    static String POM_DEVELOPER_EMAIL = "POM_DEVELOPER_EMAIL"
}