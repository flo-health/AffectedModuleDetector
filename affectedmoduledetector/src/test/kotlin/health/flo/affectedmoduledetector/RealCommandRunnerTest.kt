package health.flo.affectedmoduledetector

import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class RealCommandRunnerTest {
    @Rule
    @JvmField
    val tmpFolder = TemporaryFolder()

    @Test
    fun `failing git command reports command, working dir, exit code and stderr`() {
        val workingDir = tmpFolder.root
        val runner = RealCommandRunner(workingDir, null)
        runner.execute("git init")

        val command = "git rev-parse --verify does-not-exist"
        val error = assertThrows(IllegalStateException::class.java) {
            runner.execute(command)
        }

        assertThat(error).hasMessageThat().contains(command)
        assertThat(error).hasMessageThat().contains(workingDir.toString())
        assertThat(error).hasMessageThat().contains("(128)")
        assertThat(error).hasMessageThat().contains("fatal:")
    }
}
