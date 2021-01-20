import org.junit.Assert.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag

@Tag("unitTest")
class IndexTest {
    companion object {
        val dictionary = Dictionary()
        val lines = listOf("форма форме формы", "тесты форм, двойка", "двойка стол диван")
        val index = Index(lines, dictionary)

        init {
            readThesaurus()
        }
    }

    @Test
    fun getMostFrequent() {
        assertEquals(
            listOf(WordFrequency("форма", 4), WordFrequency("двойка", 2)),
            index.getMostFrequent(2)
        )
    }

    @Test
    fun findLines() {
        assertEquals(listOf("1: форма форме формы", "2: тесты форм, двойка"), index.findLines("форма"))
    }

    @Test
    fun generateReport() {
        assertEquals(
            listOf("форма: 4 occurrences", "used forms: форма, форме, формы, форм", "found on pages: 1"),
            index.generateReport("форма")
        )
    }

    @Test
    fun generateGroupReport() {
        assertEquals(listOf("мебель: total 2 occurrences", "  стол: 1 occurrences", "  диван: 1 occurrences"),
            index.generateGroupReport("мебель"))
    }
}