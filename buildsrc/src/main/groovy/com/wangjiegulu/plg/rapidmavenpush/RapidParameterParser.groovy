package com.wangjiegulu.plg.rapidmavenpush

import org.gradle.api.Project

class RapidParameterParser {
    Project project

    RapidParameterParser(Project project) {
        this.project = project
    }

    def getStringParameter(String key) {
        Object result = getParameter(key, null)
        return null == result ? null : String.valueOf(result)
    }

    def getBooleanParameter(String key, boolean defaultValue) {
        Object result = getParameter(key, defaultValue)
        return Boolean.valueOf(result)
    }

    def getBooleanParameter(String key) {
        return getBooleanParameter(key, false)
    }

    def getParameter(String key, Object defaultValue) {
        Object result = defaultValue
        // -D
        String value = System.getProperty(key)
        if (null != value && value.length() > 0) {
            result = value
        } else
        // -P
        if (project.hasProperty(key)) {
            result = project.getProperty(key)
        }
//        println "[RapidMavenPushTask] getParameter, key: $key, value: $result"
        return result
    }


}