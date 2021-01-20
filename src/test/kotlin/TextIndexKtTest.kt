import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File

@Tag("unitTest")
class TextIndexKtTest {
    @Test
    fun lineToWords() {
        assertEquals(listOf("а", "б", "вг", "д-е-ж"), lineToWords("а, б--вг д-е-ж q  "))
    }

    @Test
    fun `end of the first page`() {
        assertEquals(1, getPageOfLine(LINES_PER_PAGE - 1))
    }

    @Test
    fun `start of the second page`() {
        assertEquals(2, getPageOfLine(LINES_PER_PAGE))
    }

    @Test
    fun writeFile() {
        writeFile(null, listOf("a", "", "abc"))
        assertEquals("a\n\nabc", File("data/result.txt").readText())
    }

    @Test
    fun wrap() {
        assertEquals(listOf("aaaaa", "aa aa", "aaaaaa", "aaa", "aa"), wrap("aaaaa aa aa aaaaaa aaa aa", 5))
    }
}