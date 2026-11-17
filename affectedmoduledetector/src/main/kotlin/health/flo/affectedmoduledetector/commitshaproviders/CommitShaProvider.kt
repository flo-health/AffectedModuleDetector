package health.flo.affectedmoduledetector.commitshaproviders

import health.flo.affectedmoduledetector.GitClient
import health.flo.affectedmoduledetector.Sha
import java.io.Serializable

interface CommitShaProvider : Serializable {
    fun get(commandRunner: GitClient.CommandRunner): Sha
}

data class CommitShaProviderConfiguration(
    val type: String,
    val specifiedBranch: String? = null,
    val specifiedSha: String? = null,
    val top: Sha,
    val includeUncommitted: Boolean
) : Serializable
