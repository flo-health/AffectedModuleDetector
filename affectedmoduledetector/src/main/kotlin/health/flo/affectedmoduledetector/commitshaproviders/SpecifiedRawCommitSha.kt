package health.flo.affectedmoduledetector.commitshaproviders

import health.flo.affectedmoduledetector.GitClient
import health.flo.affectedmoduledetector.Sha

class SpecifiedRawCommitSha(private val commitSha: String) : CommitShaProvider {
    override fun get(commandRunner: GitClient.CommandRunner): Sha {
        return commitSha
    }
}
