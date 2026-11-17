package health.flo.affectedmoduledetector.commitshaproviders

import health.flo.affectedmoduledetector.GitClient
import health.flo.affectedmoduledetector.Sha

class SpecifiedBranchCommit(private val branch: String) : CommitShaProvider {

    override fun get(commandRunner: GitClient.CommandRunner): Sha {
        return commandRunner.executeAndParseFirst("git rev-parse $branch")
    }
}
