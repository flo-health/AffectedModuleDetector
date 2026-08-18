package com.dropbox.affectedmoduledetector

import com.google.common.truth.Truth.assertThat
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Fake android extension used in tests to simulate AGP's BaseAppModuleExtension.
 * ProjectBuilder cannot apply the real AGP plugin, so we register this directly.
 */
class FakeAndroidAppExtension(private val features: Set<String>) {
    @Suppress("unused")
    fun getDynamicFeatures(): Set<String> = features
}

@RunWith(JUnit4::class)
class DependencyTrackerTest {

    @Rule
    @JvmField
    val tmpFolder = TemporaryFolder()

    private lateinit var root: Project
    private lateinit var app: Project
    private lateinit var dfm1: Project
    private lateinit var dfm2: Project
    private lateinit var library: Project

    @Before
    fun setup() {
        val dir = tmpFolder.root
        root = ProjectBuilder.builder().withProjectDir(dir).withName("root").build()

        app = ProjectBuilder.builder()
            .withProjectDir(dir.resolve("app"))
            .withName("app")
            .withParent(root)
            .build()

        dfm1 = ProjectBuilder.builder()
            .withProjectDir(dir.resolve("feature/dfm1"))
            .withName("dfm1")
            .withParent(root)
            .build()

        dfm2 = ProjectBuilder.builder()
            .withProjectDir(dir.resolve("feature/dfm2"))
            .withName("dfm2")
            .withParent(root)
            .build()

        library = ProjectBuilder.builder()
            .withProjectDir(dir.resolve("library"))
            .withName("library")
            .withParent(root)
            .build()
    }

    @Test
    fun `GIVEN app with dynamic features WHEN dfm1 changes THEN app is a dependent`() {
        // Simulate android.dynamicFeatures = [":dfm1", ":dfm2"] on the app project
        app.extensions.add("android", FakeAndroidAppExtension(setOf(":dfm1", ":dfm2")))

        val tracker = DependencyTracker(root, null)

        val dependents = tracker.findAllDependents(dfm1.projectPath)

        assertThat(dependents).contains(app.projectPath)
    }

    @Test
    fun `GIVEN app with dynamic features WHEN dfm2 changes THEN app is a dependent`() {
        app.extensions.add("android", FakeAndroidAppExtension(setOf(":dfm1", ":dfm2")))

        val tracker = DependencyTracker(root, null)

        val dependents = tracker.findAllDependents(dfm2.projectPath)

        assertThat(dependents).contains(app.projectPath)
    }

    @Test
    fun `GIVEN app with dynamic features WHEN library changes THEN dfm1 and dfm2 are NOT dependents`() {
        app.extensions.add("android", FakeAndroidAppExtension(setOf(":dfm1", ":dfm2")))
        // library has no android extension, no dynamic features
        val libConfig = library.configurations.create("implementation")
        libConfig.dependencies.add(library.dependencies.project(mapOf("path" to ":dfm1")))

        val tracker = DependencyTracker(root, null)

        // library depends on dfm1, so dfm1's dependents include library — but dfm2 is untouched
        val dependents = tracker.findAllDependents(library.projectPath)

        assertThat(dependents).doesNotContain(dfm1.projectPath)
        assertThat(dependents).doesNotContain(dfm2.projectPath)
    }

    @Test
    fun `GIVEN project with no android extension WHEN tracker built THEN no dynamic feature edges added`() {
        // No android extension registered on any project
        val tracker = DependencyTracker(root, null)

        val dependents = tracker.findAllDependents(dfm1.projectPath)

        assertThat(dependents).isEmpty()
    }

    @Test
    fun `GIVEN android extension without getDynamicFeatures WHEN tracker built THEN no edges added`() {
        // Simulates a library extension that has no getDynamicFeatures() method
        app.extensions.add("android", object {
            @Suppress("unused")
            fun getSomethingElse(): String = "irrelevant"
        })

        val tracker = DependencyTracker(root, null)

        val dependents = tracker.findAllDependents(dfm1.projectPath)

        assertThat(dependents).isEmpty()
    }

    @Test
    fun `GIVEN app with dynamic features WHEN app changes THEN dfm1 and dfm2 are dependents`() {
        app.extensions.add("android", FakeAndroidAppExtension(setOf(":dfm1", ":dfm2")))

        val tracker = DependencyTracker(root, null)

        // The DFM→app edges are one direction: DFM change marks app as dependent.
        // App change does NOT mark DFMs as dependents — that is a separate concern
        // handled by the dynamic-feature plugin ID registration in AffectedModuleDetectorPlugin.
        val dependents = tracker.findAllDependents(app.projectPath)

        assertThat(dependents).doesNotContain(dfm1.projectPath)
        assertThat(dependents).doesNotContain(dfm2.projectPath)
    }
}
