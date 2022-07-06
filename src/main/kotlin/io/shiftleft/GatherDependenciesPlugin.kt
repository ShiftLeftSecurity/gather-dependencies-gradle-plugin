package io.shiftleft

import com.android.build.gradle.AppExtension
import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicBoolean

interface GatherDependenciesExtension {
    val outputDirectory: Property<String>
    val androidVariantName: Property<String>
    val configurationName: Property<String>
}

class GatherDependenciesPlugin : Plugin<Project> {
    private lateinit var extension: GatherDependenciesExtension
    private lateinit var project: Project
    private val android = AtomicBoolean(false)

    private val DEFAULT_OUTPUT_DIRECTORY_NAME = "gatheredDependencies"
    private val DEFAULT_ANDROID_VARIANT_NAME = "release"
    private val PLUGIN_NAME = "GradleDependenciesPlugin"
    private val ROOT_TASK = "gatherDependencies"

    private fun defineTask(name: String, outputDirectoryPath: String, project: Project, variant: ApplicationVariant) {
        val firstSubtaskName = name + "_variantDependencies"
        project.tasks.register(firstSubtaskName, Copy::class.java) {
            project.logger.debug("Running first subtask of the `$PLUGIN_NAME` plugin.")
            val allFiles = variant.getCompileClasspath(null).files
            allFiles.forEach {
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
                description = "Copy all resource jars for the compile classpath of a specific Android variant into a directory"
                from(it)
                into(outputDirectoryPath)
            }
        }
        val secondSubtaskName = name + "_androidApis"
        project.tasks.register(secondSubtaskName, Copy::class.java) {
            project.logger.debug("Running second subtask of the `$PLUGIN_NAME` plugin.")
            val androidApisConfigurationName = "androidApis"
            val relevantConfiguration = project.configurations.find { it.name.equals(androidApisConfigurationName) }
            if (relevantConfiguration == null) {
                project.logger.warn("No configuration with name `$androidApisConfigurationName found. Won't copy its dependencies.")
            }
            description = "Copy all resource jars from the `androidApis` configuration into into a directory"
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
            from(relevantConfiguration)
            into(outputDirectoryPath)
        }
        project.tasks.register(name) {
            description = "Copy all resource jars needed to compile a specific version of this project into a directory"
            dependsOn(firstSubtaskName)
            dependsOn(secondSubtaskName)
        }
    }

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

    private fun Project.setupGatherDependenciesTasks(afterAndroid: Boolean) {
        if (android.get() && !afterAndroid) return

        check(android.get()) {
            "Missing Android Gradle plugin."
        }

        project.extensions.configure<AppExtension>("android") {
            val androidVariantName = extension.androidVariantName.getOrElse(DEFAULT_ANDROID_VARIANT_NAME)
            project.logger.info("Using `androidVariantName`: '${androidVariantName}'.")

            val applicationVariantNames = mutableListOf<String>()
            applicationVariants.all {
                applicationVariantNames += this.name
            }

            var hasVariant = false
            applicationVariants.all { if (this.name == androidVariantName) hasVariant = true }
            check(hasVariant) {
                "Variant name `$androidVariantName` not found. Defined variants: `$applicationVariantNames`"
            }
            val defaultOutputDirectory = Paths.get(rootProject.buildDir.path, DEFAULT_OUTPUT_DIRECTORY_NAME)
            val outputDirectoryPath = extension.outputDirectory.getOrElse(defaultOutputDirectory.toString())
            project.logger.info("Using `outputDirectory`: '${outputDirectoryPath}'.")

            // WARNING: Do not attempt to use `forEach` instead of `all`, the variants will be empty
            applicationVariants.all {
                if (this.name == androidVariantName)
                    defineTask(ROOT_TASK, outputDirectoryPath, project, this)
            }
        }
    }

    override fun apply(project: Project) {
        project.logger.info("Applying `GradleDependenciesPlugin`...")
        this.extension = project.extensions.create("gatherDependencies", GatherDependenciesExtension::class.java)
        this.project = project

        project.afterEvaluate {
            val shouldSetUpForAndroid = extension.configurationName.orNull == null
            if (shouldSetUpForAndroid) {
                val androidPluginHandler = { _: Plugin<*> ->
                    android.set(true)
                    project.afterEvaluate {
                        project.setupGatherDependenciesTasks(afterAndroid = true)
                    }
                }
                project.plugins.withId("com.android.application", androidPluginHandler)
                project.setupGatherDependenciesTasks(afterAndroid = false)
            } else {
                val configName = extension.configurationName.get()
                project.setupGatherDependenciesTasks(configName)
            }
        }
    }
}
