import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

data class WordFrequency(val word: String, val occurrences: Int)

@Serializable
class Index(val lines: List<String>) {
    val formToLines = mutableMapOf<String, MutableList<Int>>()
    val defaultToPages = mutableMapOf<String, MutableSet<Int>>()
    val defaultToOccurrences = mutableMapOf<String, Int>()
    val defaultToForms = mutableMapOf<String, MutableSet<String>>()

    init {
        for ((lineNumber, line) in lines.withIndex())
            for (word in lineToWords(line)) {
                formToLines.getOrPut(word, { mutableListOf() }).add(lineNumber)
                val default = formToDefault[word]
                default?.let {
                    defaultToForms.getOrPut(it, { mutableSetOf() }).add(word)
                    defaultToPages.getOrPut(it, { mutableSetOf() }).add(getPageOfLine(lineNumber))
                    defaultToOccurrences[it] = defaultToOccurrences.getOrDefault(it, 0) + 1
                }
            }
    }

    fun getMostFrequent(count: Int): List<WordFrequency> =
        defaultToOccurrences.map { (word, occurrences) -> WordFrequency(word, occurrences) }
            .sortedByDescending { it.occurrences }.take(count)
}