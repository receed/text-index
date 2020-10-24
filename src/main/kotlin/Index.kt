import kotlinx.serialization.Serializable

// Represents a word and number of its occurrences
data class WordFrequency(val word: String, val occurrences: Int)

// Represents index of a text
@Serializable
class Index(private val lines: List<String>) {
    // Numbers of lines containing given word form
    private val wordFormToLines = mutableMapOf<String, MutableSet<Int>>()

    // Numbers of pages containing given lemma
    private val lemmaToPages = mutableMapOf<String, MutableSet<Int>>()

    // Numbers of lines containing given lemma
    private val lemmaToLines = mutableMapOf<String, MutableSet<Int>>()

    // Number of occurrences of given lemma
    private val lemmaToOccurrences = mutableMapOf<String, Int>()

    // Word forms corresponding to given lemma
    private val lemmaToWordForms = mutableMapOf<String, MutableSet<String>>()

    // Lemma corresponding to given word form
    private val wordFormToLemma = mutableMapOf<String, String>()

    // Creates the index using the dictionary of word forms
    constructor(lines: List<String>, dictionary: Dictionary) : this(lines) {
        for ((lineNumber, line) in lines.withIndex())
            for (word in lineToWords(line)) {
                wordFormToLines.getOrPut(word, { mutableSetOf() }).add(lineNumber)
                val lemma = dictionary.wordFormToLemma[word]
                lemma?.let {
                    lemmaToWordForms.getOrPut(it, { mutableSetOf() }).add(word)
                    lemmaToPages.getOrPut(it, { mutableSetOf() }).add(getPageOfLine(lineNumber))
                    lemmaToLines.getOrPut(it, { mutableSetOf() }).add(lineNumber)
                    lemmaToOccurrences[it] = lemmaToOccurrences.getOrDefault(it, 0) + 1
                    wordFormToLemma[word] = it
                }
            }
    }

    // Returns [count] most frequent words
    fun getMostFrequent(count: Int): List<WordFrequency> =
        lemmaToOccurrences.map { (word, occurrences) -> WordFrequency(word, occurrences) }
            .sortedByDescending { it.occurrences }.take(count)

    // Returns lines (with prepended numbers) containing given word
    fun findLines(word: String): List<String> {
        return (wordFormToLemma[word]?.let {
            lemmaToLines[it]
        } ?: wordFormToLines[word]).orEmpty().sorted().map { "${it + 1}: ${lines[it]}" }
    }

    // Returns formatted number of occurrences of a word, list of used forms of it, and numbers of pages where it
    // is found
    fun generateReport(word: String): List<String> {
        if (word !in lemmaToOccurrences)
            return listOf("$word: no occurrences")
        return listOf("$word: ${lemmaToOccurrences.getOrDefault(word, 0)} occurrences") +
                wrap("used forms: " + (lemmaToWordForms[word]?.joinToString(", ") ?: "")) +
                wrap("found on pages: " + (lemmaToPages[word]?.joinToString(", ") ?: ""))
    }

    fun generateGroupReport(group: String): List<String> {
        val frequencies = getGroupMembers(group).mapNotNull {
            val occurrences = lemmaToOccurrences.getOrDefault(it, 0)
            if (occurrences > 0) WordFrequency(it, occurrences) else null
        }.sortedByDescending { it.occurrences }
        val totalOccurrences = frequencies.sumOf { it.occurrences }
        return listOf("$group: total $totalOccurrences occurrences") + frequencies.map { (word, occurrences) -> "  $word: $occurrences occurrences" }
    }
}