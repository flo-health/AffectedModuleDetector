package health.flo.affectedmoduledetector.mocks

import health.flo.affectedmoduledetector.FileLogger
import health.flo.affectedmoduledetector.GitClient

internal class MockCommandRunner(private val logger: FileLogger) : GitClient.CommandRunner {
    private val replies = mutableMapOf<String, List<String>>()

    fun addReply(command: String, response: String) {
        logger.info("add reply. cmd: $command response: $response")
        replies[command] = response.split(System.lineSeparator())
    }

    override fun execute(command: String): String {
        return replies.getOrDefault(command, emptyList())
            .joinToString(System.lineSeparator()).also {
                logger.info("cmd: $command response: $it")
            }
    }

    override fun executeAndParse(command: String): List<String> {
        return replies.getOrDefault(command, emptyList()).also {
            logger.info("cmd: $command response: $it")
        }
    }

    override fun executeAndParseFirst(command: String): String {
        return replies.getOrDefault(command, emptyList()).first().also {
            logger.info("cmd: $command response: $it")
        }
    }
}
