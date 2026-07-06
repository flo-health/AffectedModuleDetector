package com.dropbox.affectedmoduledetector

import com.dropbox.affectedmoduledetector.rules.SetupAndroidProject
import com.google.common.truth.Truth.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.Test

class ConfigurationCacheIntegrationTest {

    @Rule
    @JvmField
    val tmpFolder = SetupAndroidProject()

    private fun setupProject() {
        tmpFolder.newFile("settings.gradle").writeText("")
        tmpFolder.newFile("build.gradle").writeText(
            """plugins {
                |   id "health.flo.affectedmoduledetector"
                |}""".trimMargin()
        )
        // Init a git repo so the plugin can locate the git root without erroring
        fun git(vararg args: String) = ProcessBuilder("git", *args)
            .directory(tmpFolder.root)
            .also { it.environment()["GIT_CONFIG_NOSYSTEM"] = "1" }
            .start().waitFor()
        git("init")
        git("config", "user.email", "test@test.com")
        git("config", "user.name", "Test")
        git("commit", "--allow-empty", "-m", "init")
    }

    @Test
    fun `GIVEN configuration cache enabled WHEN plugin is applied THEN build is configuration cache compatible`() {
        // GIVEN
        setupProject()

        // WHEN — runAffectedUnitTests puts an AMD task in the execution plan, which is where
        // the notCompatibleWithConfigurationCache() call takes effect. Using `tasks` avoids AMD
        // tasks entirely and would give a false-positive pass.
        val result = GradleRunner.create()
            .withProjectDir(tmpFolder.root)
            .withPluginClasspath()
            .withArguments("runAffectedUnitTests", "--configuration-cache", "-Paffected_module_detector.enable")
            .build()

        // THEN — the cache must be stored, not discarded due to an incompatible task
        assertThat(result.output).contains("Configuration cache entry stored.")
        assertThat(result.output).doesNotContain("Configuration cache entry discarded")
    }

    @Test
    fun `GIVEN configuration cache enabled WHEN build runs twice THEN configuration cache is reused on second run`() {
        // GIVEN
        setupProject()

        val runner = GradleRunner.create()
            .withProjectDir(tmpFolder.root)
            .withPluginClasspath()
            .withArguments("runAffectedUnitTests", "--configuration-cache", "-Paffected_module_detector.enable")

        // First run — should store the configuration cache (currently does not due to incompatibility)
        runner.build()

        // WHEN — second run
        val result = runner.build()

        // THEN
        assertThat(result.output).contains("Reusing configuration cache.")
    }
}
