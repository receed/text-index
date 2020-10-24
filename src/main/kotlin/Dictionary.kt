import java.io.File

// Parts of speech which should be ignored in index
val ignoredParts = setOf("межд.", "союз", "част.", "предл.", "мс-п")

// Parses dictionary of word forms from odict.ru
// Uses odict.csv file
class Dictionary {
    // Part of speech of the given word
    private val partOfSpeech = mutableMapOf<String, String>()

    // Words which should be ignored in index. Needed because one word may belong to several parts of speech
    private val ignoredWords = mutableSetOf("я", "он")

    // List of possible word forms of given lemma. Initialization fills partOfSpeech and ignoredWords
    private val lemmaToWordForms: Map<String, List<String>> =
        File("odict.csv").readLines(charset("cp1251")).map { line ->
            val words = line.toLowerCase().split(",")
            val lemma = words[0]
            partOfSpeech[lemma] = words[1]
            if (words[1] in ignoredParts)
                ignoredWords.add(lemma)
            lemma to words.drop(2) + lemma
        }.toMap().filterKeys { it !in ignoredWords } // removes function words

    // List of word forms corresponding to given lemma
    val wordFormToLemma =
        lemmaToWordForms.map { (default, wordForms) ->
            wordForms.map { it to default }
        }.flatten().toMap()
}