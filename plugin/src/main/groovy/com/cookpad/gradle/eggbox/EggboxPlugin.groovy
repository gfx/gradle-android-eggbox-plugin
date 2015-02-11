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

        final android = project.extensions.findByType(AppExtension)
        if (!android) {
            throw new GradleException("No android extension found in ${project}")
        }

        project.repositories.all { DefaultMavenArtifactRepository a ->
            println("repository: ${a.name} ${a.url}")
        }

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

                def artifact = { File file ->
                    // file is like $cache/io.reactivex/rxjava/1.0.4/a607b0e12d2de769dc219e00b60a9b3d9e730d/rxjava-1.0.4.jar
                    def version = file.parentFile.parentFile.name
                    def artifactId = file.parentFile.parentFile.parentFile.name
                    def groupId = file.parentFile.parentFile.parentFile.parentFile.name
                    return "${groupId}:${artifactId}:${version}"
                }

                def compileNode = xml.appendNode('compile', attributes)
                variant.compileLibraries.each { File file ->
                    compileNode.appendNode('artifact', artifact(file))
                }
            }


            final writer = new FileWriter("${project.rootDir}/eggbox.xml")
            final xmlNodePrinter = new XmlNodePrinter(new PrintWriter(writer))
            xmlNodePrinter.print(xml)
            writer.close()
        }
    }
}
