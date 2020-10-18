import java.io.File

// Parts of speech which should be ignored in index
val ignoredParts = setOf("межд.", "союз", "част.", "предл.", "мс-п")

// Parses dictionary of forms from odict.ru
// Uses odict.csv file
class Dictionary {
    // Part of speech of the given word
    private val partOfSpeech = mutableMapOf<String, String>()

    // Words which should be ignored in index. Needed because one word may belong to several parts of speech
    private val ignoredWords = mutableSetOf("я", "он")

    // List of possible forms of given default form. Initialization fills partOfSpeech and ignoredWords
    private val lemmaToForms: Map<String, List<String>> =
        File("odict.csv").readLines(charset("cp1251")).map { line ->
            val words = line.toLowerCase().split(",")
            val defaultForm = words[0]
            partOfSpeech[defaultForm] = words[1]
            if (words[1] in ignoredParts)
                ignoredWords.add(defaultForm)
            defaultForm to words.drop(2) + defaultForm
        }.toMap().filterKeys { it !in ignoredWords } // removes function words

    // List of forms corresponding to given default form
    val formToLemma =
        lemmaToForms.map { (default, forms) ->
            forms.map { it to default }
        }.flatten().toMap()
}