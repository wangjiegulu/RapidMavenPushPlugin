package com.wangjiegulu.plg.rapidmavenpush

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.internal.reflect.Instantiator

import javax.inject.Inject

class RapidMavenPushPlugin implements Plugin<Project> {
    Project project

    final Instantiator instantiator

    RapidParameterParser parameterParser
    boolean printProperties
    boolean abortOnError

    @Inject
    RapidMavenPushPlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    void apply(Project project) {
        this.project = project

        parameterParser = new RapidParameterParser(project)

        project.extensions.create(RapidMavenPushConstants.EXTENSION_NAME, RapidMavenPushExtension, instantiator, project)
        project[RapidMavenPushConstants.EXTENSION_NAME].extensions.create(RapidMavenPushConstants.EXTENSION_NAME_MAVENS, RapidMavenPushExtension.RapidMavenPushTypes, project)

        project.afterEvaluate {
            RapidMavenPushExtension rapidMavenPushExtension = project[RapidMavenPushConstants.EXTENSION_NAME]
            if (rapidMavenPushExtension.disable) {
                RapidMavenPushLog.w("Rapid Maven Push Plugin is DISABLED.\n\n" +
                        "Enable plugin in `build.gradle`: \n" +
                        "rapidMavenPush {\n" +
                        "    disable = false\n" +
                        "}\n")
                return
            }

            println("+-----------------------------------------------------------------------------------+")
            println("|                      Rapid Maven Property Inject START                            |")
            println("+-----------------------------------------------------------------------------------+")

            def mvnType = parameterParser.getParameter(MavenPushPropertyKeys.POM_MAVEN_TYPE, rapidMavenPushExtension.defaultMavenType)
            if (null == mvnType) {
                RapidMavenPushLog.w("POM_MAVEN_TYPE property is null. Please `ext.POM_MAVEN_TYPE` in build.gradle or `./gradlew clean rapidUploadArchives -POM_MAVEN_TYPE=...` in command line.")
                return
            }

            RapidMavenPushLog.i("---------------------------Gradle Params Analyze START ---------------------------")
            //record gradle params
            String versionName = parameterParser.getStringParameter(MavenPushPropertyKeys.POM_ARCHIVE_VERSION_NAME)
            RapidMavenPushLog.i(MavenPushPropertyKeys.POM_ARCHIVE_VERSION_NAME + " == " + versionName)
            String userName = parameterParser.getStringParameter(MavenPushPropertyKeys.POM_OSSRH_USERNAME)
            RapidMavenPushLog.i(MavenPushPropertyKeys.POM_OSSRH_USERNAME + " == " + userName)
            String password = parameterParser.getStringParameter(MavenPushPropertyKeys.POM_OSSRH_PASSWORD)
            RapidMavenPushLog.i(MavenPushPropertyKeys.POM_OSSRH_PASSWORD + " == " + password)
            RapidMavenPushLog.i("---------------------------Gradle Params Analyze END ---------------------------")

            RapidMavenPushLog.i("defaultMavenType == " + rapidMavenPushExtension.defaultMavenType)
            RapidMavenPushLog.i(MavenPushPropertyKeys.POM_MAVEN_TYPE + " == " + mvnType)

            HashMap<String, RapidMavenPushExtension.RapidMavenPushType> rapidMavenPushTypes = rapidMavenPushExtension[RapidMavenPushConstants.EXTENSION_NAME_MAVENS].rapidMavenPushTypes
            printProperties = rapidMavenPushExtension.printProperties
            abortOnError = rapidMavenPushExtension.abortOnError

            RapidMavenPushExtension.RapidMavenPushType rapidMavenPushType = rapidMavenPushTypes.get(mvnType)

            if (null == rapidMavenPushType) {
                RapidMavenPushLog.w("RapidMavenPushType: null")
            } else {
                RapidMavenPushLog.i("RapidMavenPushType: ${rapidMavenPushType.mavenType}, Maven Property Files: ${rapidMavenPushType.getPropertyFiles()}")
                String propertyInjectMode = rapidMavenPushType.getPropertyInjectMode(RapidMavenPushConstants.PROPERTY_INJECT_MODE_REPLACE)
                RapidMavenPushLog.i("propertyInjectMode: $propertyInjectMode")
                injectMavenProperties(rapidMavenPushType.getPropertyFiles(), propertyInjectMode)
            }

            //if POM_ARCHIVE_VERSION_NAME is not set, replace with defaultVersionName
            if (parameterParser.getStringParameter(MavenPushPropertyKeys.POM_ARCHIVE_VERSION_NAME) == null) {
                if (rapidMavenPushExtension.defaultVersionName != null) {
                    RapidMavenPushLog.i("defaultVersionName = " + rapidMavenPushExtension.defaultVersionName)

                    RapidMavenPushLog.i(MavenPushPropertyKeys.POM_ARCHIVE_VERSION_NAME + " replace with " + rapidMavenPushExtension.defaultVersionName)
                    this.project.extensions.extraProperties.set(MavenPushPropertyKeys.POM_ARCHIVE_VERSION_NAME, rapidMavenPushExtension.defaultVersionName)
                }
            }

            RapidMavenPushLog.i("---------------------------Gradle Params Analyze Replace START ---------------------------")
            // if gradle params is not null, replace with gradle params
            if (versionName != null) {
                RapidMavenPushLog.i(MavenPushPropertyKeys.POM_ARCHIVE_VERSION_NAME + " replace with " + versionName)
                this.project.extensions.extraProperties.set(MavenPushPropertyKeys.POM_ARCHIVE_VERSION_NAME, versionName)
            }
            if (userName != null) {
                RapidMavenPushLog.i(MavenPushPropertyKeys.POM_OSSRH_USERNAME + " replace with " + userName)
                this.project.extensions.extraProperties.set(MavenPushPropertyKeys.POM_OSSRH_USERNAME, userName)
            }
            if (password != null) {
                RapidMavenPushLog.i(MavenPushPropertyKeys.POM_OSSRH_PASSWORD + " replace with " + password)
                this.project.extensions.extraProperties.set(MavenPushPropertyKeys.POM_OSSRH_PASSWORD, password)
            }
            RapidMavenPushLog.i("---------------------------Gradle Params Analyze Replace END ---------------------------")

            project.tasks.withType(Javadoc) {
                options.addStringOption('Xdoclint:none', '-quiet')
                options.addStringOption('encoding', 'UTF-8')
                options.addStringOption('charSet', 'UTF-8')
            }

            try {
                RapidMavenPushTask rapidMavenPushTask = new RapidMavenPushTask()
                rapidMavenPushTask.applyUploadArchives(project)
            } catch (Throwable throwable) {
                Throwable realThrowable = null != throwable.cause ? throwable.cause : throwable
                RapidMavenPushLog.e("[ERROR in 'RapidMavenPushTask']throwable: " + realThrowable.getMessage())

                if (abortOnError) {
                    throw realThrowable
                }
            }
        }
    }

    private void injectMavenProperties(File[] propertiesFiles, String propertyInjectMode) {
        boolean isSkipMode = RapidMavenPushConstants.PROPERTY_INJECT_MODE_SKIP.equalsIgnoreCase(propertyInjectMode)
        if (null != propertiesFiles) {
            for (File propertiesFile : propertiesFiles) {
                RapidMavenPushLog.i("--------- Inject POM properties file[${propertiesFile.absolutePath}] START ---------")
                Properties localProperties = new Properties()
                if (propertiesFile.exists()) {
                    localProperties.load(propertiesFile.newDataInputStream())
                    for (Map.Entry entry : localProperties.entrySet()) {
                        String key = entry.key.toString()
                        String value = entry.value
                        /*
                         * Skip this property if the property is already set and is skip mode
                         */
                        Object parameterValue = parameterParser.getParameter(key, null)
                        if (null != parameterValue) { // is already set
                            if (isSkipMode) { // skip mode
                                RapidMavenPushLog.w("[SKIPPED] The property[$key] is already set value[$parameterValue], Than SKIPPED[$value] due to propertyInjectMode is skip mode.")
                                printPOMProperty("$key = $parameterValue")
                                continue
                            } else {
                                RapidMavenPushLog.w("[REPLACED] The property[$key] is already set value[$parameterValue], Than REPLACED[$value] due to propertyInjectMode is replace mode.")
                            }
                        }
                        project.extensions.extraProperties.set(key, value)
                        printPOMProperty("$key = $value")
                    }
                } else {
                    if (abortOnError) {
                        throw new FileNotFoundException("File not found[$propertiesFile].")
                    } else {
                        RapidMavenPushLog.e("File not found[$propertiesFile].")
                    }
                }
            }
            RapidMavenPushLog.i("Inject Maven Properties to ext Finished!")
        } else {
            RapidMavenPushLog.w("Inject Maven Properties file is null!")
        }

        // signing.secretKeyRingFile
        if (parameterParser.getBooleanParameter(MavenPushPropertyKeys.POM_SIGN)) {
            String key = MavenPushPropertyKeys.POM_SIGNING_SECRET_KEY_RING_FILE
            String originValue
            if (null == (originValue = parameterParser.getParameter(MavenPushPropertyKeys.POM_SIGNING_SECRET_KEY_RING_FILE, null))) {
                String value = "${System.getProperty("user.home")}/.gnupg/secring.gpg"
                project.ext."$key" = value
                printPOMProperty("$key = $value")
            } else {
                printPOMProperty("$key = $originValue")
            }
        }

    }

    private void printPOMProperty(String propertyStr) {
        if (printProperties) {
            RapidMavenPushLog.i("POM Property --> $propertyStr")
        }
    }

}