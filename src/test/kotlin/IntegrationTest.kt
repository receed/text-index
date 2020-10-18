import kotlinx.cli.ExperimentalCli
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.io.File

@Tag("integrationTest")
@ExperimentalCli
class IntegrationTest {
    companion object {
        init {
            main(arrayOf("index", "-i", "data/Childhood.txt", "-o", "data/index.txt"))
        }
    }
    @Test
    fun lines() {
        main(arrayOf("lines", "пока", "-i", "data/index.txt", "-o", "data/result.txt"))
        assertEquals(File("data/lines.a").readText(), File("data/result.txt").readText())
    }
    @Test
    fun common() {
        main(arrayOf("common", "100", "-i", "data/index.txt", "-o", "data/result.txt"))
        assertEquals(File("data/common.a").readText(), File("data/result.txt").readText())
    }
    @Test
    fun info() {
        main(arrayOf("info", "голова", "взглянуть", "-i", "data/index.txt", "-o", "data/result.txt"))
        assertEquals(File("data/info.a").readText(), File("data/result.txt").readText())
    }
    @Test
    fun group() {
        main(arrayOf("group", "человек", "мебель", "-i", "data/index.txt", "-o", "data/result.txt"))
        assertEquals(File("data/group.a").readText(), File("data/result.txt").readText())
    }
}