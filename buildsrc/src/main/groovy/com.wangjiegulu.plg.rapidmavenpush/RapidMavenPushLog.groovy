package com.wangjiegulu.plg.rapidmavenpush

class RapidMavenPushLog {
    static void i(String message) {
        println "[RapidMavenPushPlugin][INFO]$message"
    }

    static void w(String message) {
        println "[RapidMavenPushPlugin][WARN]$message"
    }

    static void e(String message) {
        println "[RapidMavenPushPlugin][ERROR]$message"
    }
    static void e(String message, Throwable throwable) {
        println "[RapidMavenPushPlugin][ERROR]$message : ${throwable.message}"
    }
}