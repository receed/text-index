import kotlinx.serialization.Serializable

// Represents a word and number of its occurrences
data class WordFrequency(val word: String, val occurrences: Int)

// Represents index of a text
@Serializable
class Index(private val lines: List<String>) {
    // Numbers of lines containing given form
    private val formToLines = mutableMapOf<String, MutableSet<Int>>()

    // Numbers of pages containing given default form
    private val defaultToPages = mutableMapOf<String, MutableSet<Int>>()

    // Numbers of lines containing given default form
    private val defaultToLines = mutableMapOf<String, MutableSet<Int>>()

    // Number of occurrences of given default form
    private val defaultToOccurrences = mutableMapOf<String, Int>()

    // Forms corresponding to given default form
    private val defaultToForms = mutableMapOf<String, MutableSet<String>>()

    // Default form corresponding to given form
    private val formToDefault = mutableMapOf<String, String>()

    // Creates the index using the dictionary of word forms
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

    // Returns [count] most frequent words
    fun getMostFrequent(count: Int): List<WordFrequency> =
        defaultToOccurrences.map { (word, occurrences) -> WordFrequency(word, occurrences) }
            .sortedByDescending { it.occurrences }.take(count)

    // Returns lines (with prepended numbers) containing given word
    fun findLines(word: String): List<String> {
        return (formToDefault[word]?.let {
            defaultToLines[it]
        } ?: formToLines[word]).orEmpty().sorted().map { "${it + 1}: ${lines[it]}" }
    }

    // Returns formatted number of occurrences of a word, list of used forms of it, and numbers of pages where it
    // is found
    fun generateReport(word: String): List<String> {
        if (word !in defaultToOccurrences)
            return listOf("$word: no occurrences")
        return listOf("$word: ${defaultToOccurrences.getOrDefault(word, 0)} occurrences") +
                wrap("used forms: " + (defaultToForms[word]?.joinToString(", ") ?: "")) +
                wrap("found on pages: " + (defaultToPages[word]?.joinToString(", ") ?: ""))
    }

    fun generateGroupReport(group: String): List<String> {
        var totalOccurrences = 0
        val report = getHyponyms(group).mapNotNull {
            val occurrences = defaultToOccurrences.getOrDefault(it, 0)
            totalOccurrences += occurrences
            if (occurrences > 0) "  $it: $occurrences occurrences" else null
        }
        return listOf("$group: total $totalOccurrences occurrences") + report
    }
}