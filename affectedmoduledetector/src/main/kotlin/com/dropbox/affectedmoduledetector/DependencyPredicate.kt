package com.dropbox.affectedmoduledetector

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import java.io.Serializable

/**
 * Predicate used while building the project dependency graph to decide whether a single
 * project-dependency edge should be considered.
 *
 * Returning `false` drops that edge only; other dependencies on the same [Configuration] are still
 * evaluated. Multiple predicates registered on
 * [AffectedModuleConfiguration.dependencyPredicates] are AND'd together; an empty list includes
 * every edge.
 *
 * Implementations must be [Serializable] so they can be retained on
 * [AffectedModuleConfiguration] when it is stored in the build service.
 */
fun interface DependencyPredicate : Serializable {
    /**
     * @param configuration the configuration that declares [dependency]
     * @param dependent the project that declares the dependency
     * @param dependency the project dependency under consideration
     * @return `true` if the edge should be included in the dependency graph
     */
    fun test(
        configuration: Configuration,
        dependent: Project,
        dependency: ProjectDependency,
    ): Boolean
}
