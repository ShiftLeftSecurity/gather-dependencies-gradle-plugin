package io.shiftleft

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.BuildResult

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


// Stolen and adapted from:
// https://github.com/square/wire/blob/master/wire-library/wire-gradle-plugin/src/test/kotlin/com/squareup/wire/gradle/WirePluginTest.kt
internal class GatherDependenciesPluginIntegrationTest {
    private lateinit var gradleRunner: GradleRunner

    @TempDir
    @JvmField
    var tmpFolder: File? = null

    @BeforeEach
    fun setUp() {
        val projectDir = Files.createDirectory(Paths.get(tmpFolder?.path, "project-dir"))
        val gradleHomeDir = Files.createDirectory(Paths.get(tmpFolder?.path, "gradle-home"))
        gradleRunner = GradleRunner.create()
            .withPluginClasspath()
            // Ensure individual tests are isolated and not reusing each other's previous outputs
            // by setting project dir and gradle home directly.
            .withProjectDir(projectDir.toFile())
            .withArguments(
                "-g",
                gradleHomeDir.toAbsolutePath().toString(),
                "gatherDependencies",
                "--stacktrace",
                "--info",
                "--configuration-cache",
            )
            .withDebug(true)

    }

    @AfterEach
    fun clearOutputs() {
        getOutputDirectories(File("src/test/projects")).forEach(::unsafeDelete)
    }

    @Test
    fun testMissingPlugin() {
        val fixtureRoot = File("src/test/projects/missing-plugin")
        val result = gradleRunner.runFixture(fixtureRoot) { buildAndFail() }
        assertThat(result.task(":gatherDependencies")).isNull()
        assertThat(result.output).contains("Missing Android Gradle plugin.")
    }

    @Test
    fun testMissingAndroidPlugin() {
        val fixtureRoot = File("src/test/projects/missing-android-plugin")
        val result = gradleRunner.runFixture(fixtureRoot) { buildAndFail() }
        assertThat(result.task(":gatherDependencies")).isNull()
        assertThat(result.output).contains("Missing Android Gradle plugin.")
    }

    @Test
    fun testSimpleAndroidAppCustomOutputDirectory() {
        val fixtureRoot = File("src/test/projects/android-custom-output-directory")
        val result = gradleRunner.runFixture(fixtureRoot) { buildAndFail() }
        assertThat(result.task(":gatherDependencies")).isNotNull()
        assertThat(result.task(":gatherDependencies_androidApis")).isNotNull()
        assertThat(result.task(":gatherDependencies_variantDependencies")).isNotNull()
        val defaultOutPath = Paths.get(fixtureRoot.path, "build", "customOutputDirectory") // same as in `build.gradle`
        assertThat(defaultOutPath.toFile().listFiles().size).isNotEqualTo(0)
    }

    @Test
    fun testSimpleAndroidAppCustomOutputDirectoryKts() {
        val fixtureRoot = File("src/test/projects/android-custom-output-directory-kts")
        val result = gradleRunner.runFixture(fixtureRoot) { buildAndFail() }
        assertThat(result.task(":gatherDependencies")).isNotNull()
        assertThat(result.task(":gatherDependencies_androidApis")).isNotNull()
        assertThat(result.task(":gatherDependencies_variantDependencies")).isNotNull()
        val defaultOutPath = Paths.get(fixtureRoot.path, "build", "customOutputDirectory") // same as in `build.gradle.kts`
        assertThat(defaultOutPath.toFile().listFiles().size).isNotEqualTo(0)
    }

    @Disabled //re-enable after choosing a directory which cannot be created on SL's Jenkins instances
    fun testSimpleAndroidAppOutputDirectoryPointingToInvalidPath() {
        val fixtureRoot = File("src/test/projects/android-inexistent-output-directory")
        val result = gradleRunner.runFixture(fixtureRoot) { buildAndFail() }
        assertThat(result.task(":gatherDependencies")).isNull()
    }

    @Test
    fun testAndroidInexistentVariant() {
        val fixtureRoot = File("src/test/projects/android-inexistent-variant")
        val result = gradleRunner.runFixture(fixtureRoot) { buildAndFail() }
        assertThat(result.task(":gatherDependencies")).isNull()
        assertThat(result.output).contains("Variant name").contains("not found.")
    }

    @Test
    fun testSimpleAndroidAppNoConfigurationValuesSpecified() {
        val fixtureRoot = File("src/test/projects/android-no-config-specified")
        val result = gradleRunner.runFixture(fixtureRoot) { buildAndFail() }
        assertThat(result.task(":gatherDependencies")).isNotNull()
        assertThat(result.task(":gatherDependencies_androidApis")).isNotNull()
        assertThat(result.task(":gatherDependencies_variantDependencies")).isNotNull()
        val defaultOutPath = Paths.get(fixtureRoot.path, "build", "gatheredDependencies")
        assertThat(defaultOutPath.toFile().listFiles().size).isNotEqualTo(0)
    }

    @Test
    fun testSimpleAndroidAppNoConfigurationValuesSpecifiedKts() {
        val fixtureRoot = File("src/test/projects/android-no-config-specified-kts")
        val result = gradleRunner.runFixture(fixtureRoot) { buildAndFail() }
        assertThat(result.task(":gatherDependencies")).isNotNull()
        assertThat(result.task(":gatherDependencies_androidApis")).isNotNull()
        assertThat(result.task(":gatherDependencies_variantDependencies")).isNotNull()
        val defaultOutPath = Paths.get(fixtureRoot.path, "build", "gatheredDependencies")
        assertThat(defaultOutPath.toFile().listFiles().size).isNotEqualTo(0)
    }

    @Test
    fun testAndroidCustomVariant() {
        val fixtureRoot = File("src/test/projects/android-custom-variant")
        val result = gradleRunner.runFixture(fixtureRoot) { buildAndFail() }
        assertThat(result.task(":gatherDependencies")).isNotNull()
        assertThat(result.task(":gatherDependencies_androidApis")).isNotNull()
        assertThat(result.task(":gatherDependencies_variantDependencies")).isNotNull()
        val defaultOutPath = Paths.get(fixtureRoot.path, "build", "gatheredDependencies")
        assertThat(defaultOutPath.toFile().listFiles().size).isNotEqualTo(0)
    }

    @Test
    fun testAndroidWithAarDependency() {
        val fixtureRoot = File("src/test/projects/android-with-aar-dependency")
        val fixtureLibsFolder = Paths.get(fixtureRoot.path, "libs")
        val aarFilesBefore = fixtureLibsFolder.toFile().listFiles().filter { it.extension.endsWith("aar") }
        assertThat(aarFilesBefore.size).isNotEqualTo(0)

        val result = gradleRunner.runFixture(fixtureRoot) { buildAndFail() }
        assertThat(result.task(":gatherDependencies")).isNotNull()
        assertThat(result.task(":gatherDependencies_androidApis")).isNotNull()
        assertThat(result.task(":gatherDependencies_variantDependencies")).isNotNull()
        val defaultOutPath = Paths.get(fixtureRoot.path, "build", "gatheredDependencies")
        assertThat(defaultOutPath.toFile().listFiles().size).isNotEqualTo(0)

        val aarFilesAfter = fixtureLibsFolder.toFile().listFiles().filter { it.extension.endsWith("aar") }
        assertThat(aarFilesAfter.size).isNotEqualTo(0)
    }

    @Test
    fun testAndroidCustomVariantKts() {
        val fixtureRoot = File("src/test/projects/android-custom-variant-kts")
        val result = gradleRunner.runFixture(fixtureRoot) { buildAndFail() }
        assertThat(result.task(":gatherDependencies")).isNotNull()
        assertThat(result.task(":gatherDependencies_androidApis")).isNotNull()
        assertThat(result.task(":gatherDependencies_variantDependencies")).isNotNull()
        val defaultOutPath = Paths.get(fixtureRoot.path, "build", "gatheredDependencies")
        assertThat(defaultOutPath.toFile().listFiles().size).isNotEqualTo(0)
    }

    @Test
    fun testMinimalKotlinApp() {
        val fixtureRoot = File("src/test/projects/minimal-kotlin-app")
        val result = gradleRunner.runFixture(fixtureRoot) { buildAndFail() }
        assertThat(result.task(":gatherDependencies")).isNotNull()
        assertThat(result.task(":gatherDependencies_androidApis")).isNull()
        assertThat(result.task(":gatherDependencies_variantDependencies")).isNull()
        val defaultOutPath = Paths.get(fixtureRoot.path, "build", "gatheredDependencies")
        assertThat(defaultOutPath.toFile().listFiles().size).isNotEqualTo(0)
    }

    private fun GradleRunner.runFixture(
        root: File,
        action: GradleRunner.() -> BuildResult
    ): BuildResult {
        var generatedSettings = false
        val settings = File(root, "settings.gradle")
        var generatedGradleProperties = false
        val gradleProperties = File(root, "gradle.properties")
        return try {
            if (!settings.exists()) {
                settings.createNewFile()
                generatedSettings = true
            }

            if (!gradleProperties.exists()) {
                val rootGradleProperties = File("../gradle.properties")
                if (!rootGradleProperties.exists()) {
                    fail("Root gradle.properties doesn't exist at $rootGradleProperties.")
                }
                val versionName = rootGradleProperties.useLines { lines ->
                    lines.firstOrNull { it.startsWith("VERSION_NAME") }
                }
                if (versionName == null) {
                    fail("Root gradle.properties is missing the VERSION_NAME entry.")
                }
                gradleProperties.createNewFile()
                gradleProperties.writeText(versionName)
                generatedGradleProperties = true
            } else {
                gradleProperties.useLines { lines ->
                    if (lines.none { it.startsWith("VERSION_NAME") }) {
                        fail("Fixture's gradle.properties has to include the VERSION_NAME entry.")
                    }
                }
            }

            withProjectDir(root).action()
        } finally {
            if (generatedSettings) settings.delete()
            if (generatedGradleProperties) gradleProperties.delete()
        }
    }

    companion object {
        private val OUTPUT_DIRECTORY_NAMES = arrayOf("build", "custom")

        private fun getOutputDirectories(root: File): List<File> {
            if (!root.isDirectory) return emptyList()
            if (root.isDirectory && root.name in OUTPUT_DIRECTORY_NAMES) return listOf(root)
            return root.listFiles()!!.flatMap { getOutputDirectories(it) }
        }

        // This follows symlink so don't use it at home.
        @Throws(IOException::class) fun unsafeDelete(f: File) {
            if (f.isDirectory) {
                for (c in f.listFiles()!!) unsafeDelete(c)
            }
            f.delete()
        }
    }
}