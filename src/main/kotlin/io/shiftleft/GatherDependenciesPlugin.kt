package io.shiftleft

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

interface GatherDependenciesExtension {
    val outputDirectory: Property<String>
    val configurationName: Property<String>
}

class GatherDependenciesPlugin : Plugin<Project> {
    private lateinit var extension: GatherDependenciesExtension
    private lateinit var project: Project

    private val DEFAULT_OUTPUT_DIRECTORY_NAME = "gatheredDependencies"
    private val DEFAULT_CONFIGURATION_NAME = "compileClasspath"
    private val PLUGIN_NAME = "GradleDependenciesPlugin"
    private val ROOT_TASK = "gatherDependencies"

    private fun Project.setupGatherDependenciesTasks(configurationName: String) {
        val config = project.configurations.findByName(configurationName)
        check(config != null) {
            "Could not find configuration with name $configurationName."
        }

        val defaultOutputDirectory = Paths.get(rootProject.buildDir.path, DEFAULT_OUTPUT_DIRECTORY_NAME)
        val outputDirectoryPath = extension.outputDirectory.getOrElse(defaultOutputDirectory.toString())
        project.logger.info("Using `outputDirectory`: '${outputDirectoryPath}'.")

        project.tasks.register(ROOT_TASK, Copy::class.java) {
            description = "Copy all resource jars from a specific configuration into a directory"
            from(config)
            into(outputDirectoryPath)
        }
    }

    override fun apply(project: Project) {
        project.logger.info("Applying `GradleDependenciesPlugin`...")
        this.extension = project.extensions.create("gatherDependencies", GatherDependenciesExtension::class.java)
        this.project = project

        project.afterEvaluate {
            val configName = extension.configurationName.getOrElse(DEFAULT_CONFIGURATION_NAME)
            this.configurations.all {
              if (this.name == configName) {
                project.setupGatherDependenciesTasks(this.name)
              }
            }
        }
    }
}
