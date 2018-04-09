package com.wangjiegulu.plg.rapidmavenpush

import com.android.build.gradle.LibraryPlugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.Upload
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.javadoc.Javadoc
import org.gradle.plugins.signing.SigningExtension

class RapidMavenPushTask {
    private Project project
    private RapidParameterParser parameterParser

    void applyUploadArchives(Project project) {
        this.project = project

        parameterParser = new RapidParameterParser(project)

        project.plugins.apply('maven')

        boolean pomSign = parameterParser.getBooleanParameter(MavenPushPropertyKeys.POM_SIGN)

        if (pomSign) {
//            project.apply { plugin: 'signing' }
//            project.pluginManager.apply('signing')
            project.plugins.apply('signing')

//            if(null == parameterParser.getParameter(MavenPushPropertyKeys.POM_SIGNING_SECRET_KEY_RING_FILE, null)){
//                project.ext."$MavenPushPropertyKeys.POM_SIGNING_SECRET_KEY_RING_FILE" = "${System.getProperty("user.home")}/.gnupg/secring.gpg"
//            }

        }

        //定义GroupID和Version，ArtefactID会自动使用Project名
        project.group = parameterParser.getStringParameter(MavenPushPropertyKeys.POM_ARCHIVE_GROUP)
        project.version = parameterParser.getStringParameter(MavenPushPropertyKeys.POM_ARCHIVE_VERSION_NAME)

        // packaging -> aar or jar
        String originPackaging = parameterParser.getStringParameter(MavenPushPropertyKeys.POM_PACKAGING)
        RapidMavenPushLog.i("Origin Packaging: $originPackaging")
        def pomPackaging = parseRealPackaging(originPackaging)
        RapidMavenPushLog.i("Parsed Packaging: $pomPackaging")

        boolean openSource = parameterParser.getBooleanParameter(MavenPushPropertyKeys.POM_OPEN_SOURCE, true)

        // archives
        if (RapidMavenPushConstants.PACKAGING_AAR.equalsIgnoreCase(pomPackaging)) {
            if (openSource) {
                prepareAARArtifacts(project)
            }
        } else if (RapidMavenPushConstants.PACKAGING_JAR.equalsIgnoreCase(pomPackaging)) {
            if (openSource) {
                prepareJARArtifacts(project)
            }
        } else {
            throw new RuntimeException("Unknown POM packaging: " + pomPackaging)
        }

        if (pomSign) {
            project.extensions.getByType(SigningExtension).sign(project.configurations.archives)
//            signing {
//                sign configurations.archives
//            }
        }

        project.getTasks().create([name: 'rapidUploadArchives', type: Upload, overwrite: true, group: 'Upload']) {

            setConfiguration(project.configurations.getByName('archives')) // :depmodule:archives

            repositories {
                mavenDeployer {
                    if (pomSign) {
                        //为Pom文件做数字签名
                        beforeDeployment { MavenDeployment deployment -> project.extensions.getByType(SigningExtension).signPom(deployment) }
                    }

                    def pomRepositoryUrl = parameterParser.getStringParameter(MavenPushPropertyKeys.POM_REPOSITORY_URL)
                    def pomRepositoryUrlSnapshot = parameterParser.getStringParameter(MavenPushPropertyKeys.POM_REPOSITORY_URL_SNAPSHOT)

                    String scheme = URI.create(pomRepositoryUrl).scheme
                    if (null == scheme
                            || "" == scheme.trim()
                            || "file".equalsIgnoreCase(scheme)
                    ) {
                        repository(url: project.uri(pomRepositoryUrl)) {
                            authentication(userName: parameterParser.getStringParameter(MavenPushPropertyKeys.POM_OSSRH_USERNAME), password: parameterParser.getStringParameter(MavenPushPropertyKeys.POM_OSSRH_PASSWORD))
                        }
                        snapshotRepository(url: project.uri(pomRepositoryUrlSnapshot)) {
                            authentication(userName: parameterParser.getStringParameter(MavenPushPropertyKeys.POM_OSSRH_USERNAME), password: parameterParser.getStringParameter(MavenPushPropertyKeys.POM_OSSRH_PASSWORD))
                        }
                    } else {
                        repository(url: pomRepositoryUrl) {
                            authentication(userName: parameterParser.getStringParameter(MavenPushPropertyKeys.POM_OSSRH_USERNAME), password: parameterParser.getStringParameter(MavenPushPropertyKeys.POM_OSSRH_PASSWORD))
                        }
                        snapshotRepository(url: pomRepositoryUrlSnapshot) {
                            authentication(userName: parameterParser.getStringParameter(MavenPushPropertyKeys.POM_OSSRH_USERNAME), password: parameterParser.getStringParameter(MavenPushPropertyKeys.POM_OSSRH_PASSWORD))
                        }
                    }

//            repository(url: uri('/Users/wangjie/.m2/repository'))

                    pom.project {
                        name parameterParser.getStringParameter(MavenPushPropertyKeys.POM_ARCHIVE_ID)
                        artifactId parameterParser.getStringParameter(MavenPushPropertyKeys.POM_ARCHIVE_ID)
                        packaging pomPackaging
                        description parameterParser.getStringParameter(MavenPushPropertyKeys.POM_DESC)
                        url parameterParser.getStringParameter(MavenPushPropertyKeys.POM_URL)

                        scm {
                            url parameterParser.getStringParameter(MavenPushPropertyKeys.POM_SCM_URL)
                            connection parameterParser.getStringParameter(MavenPushPropertyKeys.POM_SCM_CONNECTION)
                            developerConnection parameterParser.getStringParameter(MavenPushPropertyKeys.POM_SCM_DEV_CONNECTION)
                        }

                        licenses {
                            license {
                                name parameterParser.getStringParameter(MavenPushPropertyKeys.POM_LICENCE_NAME)
                                url parameterParser.getStringParameter(MavenPushPropertyKeys.POM_LICENCE_URL)
                                distribution parameterParser.getStringParameter(MavenPushPropertyKeys.POM_LICENCE_DIST)
                            }
                        }

                        developers {
                            developer {
                                id parameterParser.getStringParameter(MavenPushPropertyKeys.POM_DEVELOPER_ID)
                                name parameterParser.getStringParameter(MavenPushPropertyKeys.POM_DEVELOPER_NAME)
                                email parameterParser.getStringParameter(MavenPushPropertyKeys.POM_DEVELOPER_EMAIL)
                            }
                        }
                    }

                }
            }
        }

    }

    private String parseRealPackaging(String pomPackaging) {
        if (null == pomPackaging || pomPackaging.length() <= 0) {
            // parse automatically
            if (project.plugins.hasPlugin(LibraryPlugin)) {
                return RapidMavenPushConstants.PACKAGING_AAR
            } else if (project.plugins.hasPlugin(JavaPlugin)) {
                return RapidMavenPushConstants.PACKAGING_JAR
            }
        }
        return pomPackaging
    }

    private void prepareJARArtifacts(Project project) {
        Task classesTask = project.tasks.getByName('classes')
        Jar sourcesJar = project.task("sourcesJar", type: Jar, dependsOn: classesTask) {
            classifier = 'sources'
            from project.sourceSets.main.allSource
        }

        Task javadocTask = project.tasks.getByName('javadoc')
        Jar javadocJar = project.task("javadocJar", type: Jar, dependsOn: javadocTask) {
            classifier = 'javadoc'
            from javadocTask.destinationDir
        }

        project.artifacts {
            archives sourcesJar, javadocJar
        }
    }

    private void prepareAARArtifacts(Project project) {
        Javadoc androidJavadocs = project.getTasks().create([name: "androidJavadocs", type: Javadoc]) {
            source = project.android.sourceSets.main.java.srcDirs
            classpath += project.files(project.android.getBootClasspath().join(File.pathSeparator))
        }
//            Task androidJavadocsJar = project.getTasks().create([name: "androidJavadocsJar", type: Jar, dependsOn: project.tasks.getByName('androidJavadocs')]) {
        Jar androidJavadocsJar = project.getTasks().create([name: "androidJavadocsJar", type: Jar, dependsOn: androidJavadocs]) {
            classifier = 'javadoc'
//                from project.tasks.getByName('androidJavadocs').destinationDir
            from androidJavadocs.destinationDir
        }
        Jar androidSourcesJar = project.getTasks().create([name: "androidSourcesJar", type: Jar]) {
            classifier = 'sources'
            from project.android.sourceSets.main.java.srcDirs
        }

        project.artifacts {
            archives androidSourcesJar, androidJavadocsJar
        }
    }


}