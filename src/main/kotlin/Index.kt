import kotlinx.serialization.Serializable

// Represents a word and number of its occurrences
data class WordFrequency(val word: String, val occurrences: Int)

// Represents index of a text
@Serializable
class Index(private val lines: List<String>) {
    // Numbers of lines containing given form
    private val formToLines = mutableMapOf<String, MutableSet<Int>>()

    // Numbers of pages containing given lemma
    private val lemmaToPages = mutableMapOf<String, MutableSet<Int>>()

    // Numbers of lines containing given lemma
    private val lemmaToLines = mutableMapOf<String, MutableSet<Int>>()

    // Number of occurrences of given lemma
    private val lemmaToOccurrences = mutableMapOf<String, Int>()

    // Forms corresponding to given lemma
    private val lemmaToForms = mutableMapOf<String, MutableSet<String>>()

    // Lemma corresponding to given form
    private val formToLemma = mutableMapOf<String, String>()

    // Creates the index using the dictionary of word forms
    constructor(lines: List<String>, dictionary: Dictionary) : this(lines) {
        for ((lineNumber, line) in lines.withIndex())
            for (word in lineToWords(line)) {
                formToLines.getOrPut(word, { mutableSetOf() }).add(lineNumber)
                val lemma = dictionary.formToLemma[word]
                lemma?.let {
                    lemmaToForms.getOrPut(it, { mutableSetOf() }).add(word)
                    lemmaToPages.getOrPut(it, { mutableSetOf() }).add(getPageOfLine(lineNumber))
                    lemmaToLines.getOrPut(it, { mutableSetOf() }).add(lineNumber)
                    lemmaToOccurrences[it] = lemmaToOccurrences.getOrDefault(it, 0) + 1
                    formToLemma[word] = it
                }
            }
    }

    // Returns [count] most frequent words
    fun getMostFrequent(count: Int): List<WordFrequency> =
        lemmaToOccurrences.map { (word, occurrences) -> WordFrequency(word, occurrences) }
            .sortedByDescending { it.occurrences }.take(count)

    // Returns lines (with prepended numbers) containing given word
    fun findLines(word: String): List<String> {
        return (formToLemma[word]?.let {
            lemmaToLines[it]
        } ?: formToLines[word]).orEmpty().sorted().map { "${it + 1}: ${lines[it]}" }
    }

    // Returns formatted number of occurrences of a word, list of used forms of it, and numbers of pages where it
    // is found
    fun generateReport(word: String): List<String> {
        if (word !in lemmaToOccurrences)
            return listOf("$word: no occurrences")
        return listOf("$word: ${lemmaToOccurrences.getOrDefault(word, 0)} occurrences") +
                wrap("used forms: " + (lemmaToForms[word]?.joinToString(", ") ?: "")) +
                wrap("found on pages: " + (lemmaToPages[word]?.joinToString(", ") ?: ""))
    }

    fun generateGroupReport(group: String): List<String> {
        val frequencies = getHyponyms(group).mapNotNull {
            val occurrences = lemmaToOccurrences.getOrDefault(it, 0)
            if (occurrences > 0) WordFrequency(it, occurrences) else null
        }.sortedByDescending { it.occurrences }
        val totalOccurrences = frequencies.sumOf { it.occurrences }
        return listOf("$group: total $totalOccurrences occurrences") + frequencies.map { (word, occurrences) -> "  $word: $occurrences occurrences" }
    }
}