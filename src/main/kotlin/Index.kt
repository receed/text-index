import kotlinx.serialization.Serializable

data class WordFrequency(val word: String, val occurrences: Int)

@Serializable
class Index(private val lines: List<String>) {
    private val formToLines = mutableMapOf<String, MutableSet<Int>>()
    private val defaultToPages = mutableMapOf<String, MutableSet<Int>>()
    private val defaultToLines = mutableMapOf<String, MutableSet<Int>>()
    private val defaultToOccurrences = mutableMapOf<String, Int>()
    private val defaultToForms = mutableMapOf<String, MutableSet<String>>()
    private val formToDefault = mutableMapOf<String, String>()

    constructor(lines: List<String>, dictionary: Dictionary) : this(lines) {
        for ((lineNumber, line) in lines.withIndex())
            for (word in lineToWords(line)) {
                formToLines.getOrPut(word, { mutableSetOf() }).add(lineNumber)
                val default = dictionary.formToDefault[word]
                default?.let {
                    defaultToForms.getOrPut(it, { mutableSetOf() }).add(word)
                    defaultToPages.getOrPut(it, { mutableSetOf() }).add(getPageOfLine(lineNumber))
                    defaultToLines.getOrPut(it, { mutableSetOf() }).add(lineNumber)
                    defaultToOccurrences[it] = defaultToOccurrences.getOrDefault(it, 0) + 1
                    formToDefault[word] = it
                }
            }
    }

    fun getMostFrequent(count: Int): List<WordFrequency> =
        defaultToOccurrences.map { (word, occurrences) -> WordFrequency(word, occurrences) }
            .sortedByDescending { it.occurrences }.take(count)

    fun findLines(word: String): List<String> {
        return (formToDefault[word]?.let {
            defaultToLines[it]
        } ?: formToLines[word]).orEmpty().sorted().map { "${it + 1}: ${lines[it]}" }
    }

    fun generateReport(word: String): List<String> {
        if (word !in defaultToOccurrences)
            return listOf("$word: no occurrences")
        return listOf("$word: ${defaultToOccurrences.getOrDefault(word, 0)} occurrences",
            "used forms: " + (defaultToForms[word]?.joinToString(", ") ?: ""),
            "found on pages: " + (defaultToPages[word]?.joinToString(", ") ?: ""))
    }
}