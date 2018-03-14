package com.wangjiegulu.plg.rapidmavenpush

import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator

/**
 * RapidMavenPushConstants.EXTENSION_NAME
 */
class RapidMavenPushExtension {
    Project project

    RapidMavenPushExtension(Instantiator instantiator, Project project) {
        this.project = project
    }

    boolean printProperties = false
    boolean abortOnError = false

    /**
     * RapidMavenPushConstants.EXTENSION_NAME_MAVENS
     */
    static class RapidMavenPushTypes {
//        List<RapidMavenPushType> rapidMavenPushTypes = []
        HashMap<String, RapidMavenPushType> rapidMavenPushTypes = new HashMap<>()
        Project project

        RapidMavenPushTypes(Project project) {
            this.project = project
        }

        void maven(Closure closure) {
            RapidMavenPushType type = new RapidMavenPushType()
            project.configure(type, closure)
//            rapidMavenPushTypes.add(type)
            rapidMavenPushTypes.put(type.mavenType, type)
        }
    }

    static class RapidMavenPushType {
        String mavenType
        File[] propertyFiles
        /**
         * RapidMavenPushConstants.PROPERTY_INJECT_MODE_REPLACE
         * RapidMavenPushConstants.PROPERTY_INJECT_MODE_SKIP
         */
        String propertyInjectMode = RapidMavenPushConstants.PROPERTY_INJECT_MODE_REPLACE

        String getPropertyInjectMode(String defaultValue) {
            return null == propertyInjectMode ? defaultValue : propertyInjectMode
        }


        @Override
        public String toString() {
            return "RapidMavenPushType{" +
                    "mavenType='" + mavenType + '\'' +
                    ", propertyFiles=" + Arrays.toString(propertyFiles) +
                    ", propertyInjectMode='" + propertyInjectMode + '\'' +
                    '}';
        }
    }


}


