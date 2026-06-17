package health.flo.affectedmoduledetector.mocks

import health.flo.affectedmoduledetector.GitClient
import health.flo.affectedmoduledetector.Sha
import health.flo.affectedmoduledetector.commitshaproviders.CommitShaProvider

class MockCommitShaProvider : CommitShaProvider {
    private val replies = mutableListOf<Sha>()

    fun addReply(sha: Sha) {
        replies.add(sha)
    }
    override fun get(commandRunner: GitClient.CommandRunner): Sha {
        return replies.first()
    }
}
