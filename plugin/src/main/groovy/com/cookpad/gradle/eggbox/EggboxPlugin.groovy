package com.cookpad.gradle.eggbox

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import com.android.builder.model.AndroidProject
import groovy.mock.interceptor.MockFor
import groovy.xml.MarkupBuilder
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import groovy.transform.CompileStatic
import org.gradle.api.artifacts.ComponentMetadata
import org.gradle.api.artifacts.ComponentMetadataDetails
import org.gradle.api.artifacts.repositories.ArtifactRepository
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.internal.artifacts.dsl.dependencies.DefaultDependencyHandler
import org.gradle.api.internal.artifacts.query.DefaultArtifactResolutionQuery
import org.gradle.api.internal.artifacts.repositories.DefaultMavenArtifactRepository
import org.gradle.api.internal.project.DefaultProject

// see also:
// http://www.gradle.org/docs/current/userguide/custom_plugins.html
// PrepareDependenciesTask.groovy

@CompileStatic
public class EggboxPlugin implements Plugin<Project> {

    @Override
    void apply(final Project project) {
        project.extensions.add(EggboxExtension.NAME, EggboxExtension as Object)

        final lockFile = project.file('eggbox.xml')
        final deployDir = project.file('.eggbox')

        final android = project.extensions.findByType(AppExtension)
        if (!android) {
            throw new GradleException("No android extension found in ${project}")
        }

        if (lockFile.exists()) {
            project.logger.info("[eggbox] use locked dependencies in ${lockFile}")

            def deps = new XmlParser().parse(lockFile)

            deps.each { Node artifact ->
                def buildType = artifact.attribute('build-type').toString()
                def flavor = artifact.attribute('flavor')

                def decl = flavor ? "${flavor}${buildType.capitalize()}Compile" : "${buildType}Compile"

                artifact.each { Node item ->
                    if (item.name() == "compile") {
                        def value = item.text().trim()
                        println("${decl}('${value}')")
                        project.dependencies.invokeMethod(decl, value)
                    } else if (item.name() == "test") {

                    } else {
                        project.logger.error("Unexpected node: $item")
                    }

                }
            }
        } else {
            project.logger.info("[eggbox] lock dependencies")

            project.afterEvaluate {
                final xml = new XmlParser().parseText('<dependency />')

                android.applicationVariants.all { ApplicationVariant variant ->
                    def buildType = variant.buildType.name
                    def flavor = variant.flavorName

                    def attributes = new HashMap<String, String>()
                    if (buildType) {
                        attributes['build-type'] = buildType
                    }
                    if (flavor) {
                        attributes['flavor'] = flavor
                    }

                    def buildNode = xml.appendNode('artifact', attributes)
                    variant.compileLibraries.each { File file ->
                        if (file.toString().contains('/.gradle/caches/')) {
                            buildNode.appendNode('compile', artifactId(file))
                        }
                    }

                    def testVariant = variant.testVariant
                    if (testVariant) {
                        def testNode = buildNode.appendNode('test')
                        testVariant.compileLibraries.each { File file ->
                            if (file.toString().contains('/.gradle/caches/')) {
                                testNode.appendNode('compile', artifactId(file))
                            }
                        }
                    }
                }

                final writer = new FileWriter(lockFile)
                final xmlNodePrinter = new XmlNodePrinter(new PrintWriter(writer))
                xmlNodePrinter.print(xml)
                writer.close()
            }
        }

        project.repositories.all { DefaultMavenArtifactRepository a ->
            println("repository: ${a.name} ${a.url}")
        }

    }

    static class ArtifactId {
        String groupName
        String artifactName
        String version

        public ArtifactId(String groupId, String artifactName, String version) {
            this.groupName = groupId
            this.artifactName = artifactName
            this.version = version
        }

        @Override
        public String toString() {
            return "$groupName:$artifactName:$version"
        }
    }

    static ArtifactId artifactId(File file) {
        println("file: $file")
        // file is something like $cache/io.reactivex/rxjava/1.0.4/a607b0e12d2de769dc219e00b60a9b3d9e730d/rxjava-1.0.4.jar
        def version = file.parentFile.parentFile.name
        def artifactName = file.parentFile.parentFile.parentFile.name
        def groupName = file.parentFile.parentFile.parentFile.parentFile.name

        return new ArtifactId(groupName, artifactName, version)
    }
}
