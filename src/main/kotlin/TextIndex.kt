import java.io.File
import kotlinx.cli.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.lang.IllegalStateException

// Maximum number of non-empty lines on a page
val LINES_PER_PAGE = 45

// Parts of speech which should be ignored in index
val ignoredParts = setOf("межд.", "союз", "част.", "предл.", "мс-п")

// Exception for invalid input
class InvalidInputException(message: String) : Exception(message)

// Parses dictionary of forms from odict.ru
// Uses odict.csv file in utf-8 charset
class Dictionary {
    // Part of sppech of the given word
    val partOfSpeech = mutableMapOf<String, String>()

    // Words which should be ignored in index. Needed because one word may belong to several parts of speech
    val ignoredWords = mutableSetOf<String>("я", "он")

    // List of possible forms of given default form. Initialization fills partOfSpeech and ignoredWords
    val defaultToForms: Map<String, List<String>> =
        File("odict.csv").readLines().map { line ->
            val words = line.toLowerCase().split(",")
            val defaultForm = words[0]
            partOfSpeech[defaultForm] = words[1]
            if (words[1] in ignoredParts)
                ignoredWords.add(defaultForm)
            defaultForm to words.drop(2) + defaultForm
        }.filterNotNull().toMap().filterKeys { it !in ignoredWords } // removes function words

    // List of forms corresponding to given default form
    val formToDefault =
        defaultToForms.map { (default, forms) ->
            forms.map { it to default }
        }.flatten().toMap()
}

// Reads file with text, returns list of non-blank lines
fun readFile(fileName: String): List<String> {
    val file = File(fileName)
    if (!file.exists())
        throw InvalidInputException("$fileName: no such file")
    return File(fileName).readLines().filter { it.isNotBlank() }
}

// Finds words consisting of Russian letters and hyphens in the given line
fun lineToWords(line: String) =
    line.toLowerCase().replace(Regex("--|[^а-я-]"), " ").split(" ").filter { it.isNotBlank() }

fun getPageOfLine(line: Int) = line / LINES_PER_PAGE + 1

fun getIndex(fileName: String): Index {
    val file = File(fileName)
    if (!file.exists())
        throw InvalidInputException("$fileName: no such file")
    return try {
        Json.decodeFromString<Index>(file.readText())
    } catch (e: Exception) {
        throw InvalidInputException("Index file is invalid")
    }
}

// Writes lines to file or to result.txt
fun writeFile(fileName: String?, lines: List<String>) {
    val a = lines[1][0]
    File(fileName ?: "result.txt").writeText(lines.joinToString("\n"))
}

// Wraps long lines. Returns list of lines not longer than [width]
fun wrap(line: String, width: Int = 120): List<String> {
    val words = line.split(" ")
    val result = mutableListOf<StringBuilder>()
    for (word in words) {
        if (result.isEmpty() || result.last().length + 1 + word.length > width)
            result.add(StringBuilder())
        else
            result.last().append(' ')
        result.last().append(word)
    }
    return result.map { it.toString() }
}

// Entry point
@ExperimentalCli
fun main(args: Array<String>) {
    val parser = ArgParser("example")
    val input by parser.option(ArgType.String, shortName = "i", description = "Input file name").required()
    val output by parser.option(ArgType.String, shortName = "o", description = "Output file name")

    // Subcommands for different modes
    class Index : Subcommand("index", "Create index of file") {
        override fun execute() {
            val lines = readFile(input)
            val dictionary = Dictionary()
            val index = Index(lines, dictionary)
            File(output ?: "index.txt").writeText(Json.encodeToString(index))
        }
    }

    class Common : Subcommand("common", "Get most common words") {
        val count by argument(ArgType.Int, description = "Number of most frequent words to find")
        override fun execute() {
            val index = getIndex(input)
            writeFile(output, index.getMostFrequent(count).map { (word, occurences) -> "$word: $occurences" })
        }
    }

    class Info : Subcommand("info", "Analyze usage of given words") {
        val words by argument(ArgType.String, description = "Words to find").vararg()
        override fun execute() {
            val index = getIndex(input)
            writeFile(output, words.flatMap { index.generateReport(it) })
        }
    }

    class Lines : Subcommand("lines", "Find lines containing word") {
        val words by argument(ArgType.String, description = "Words to find").vararg()
        override fun execute() {
            val index = getIndex(input)
            writeFile(output, words.flatMap { listOf("$it:") + index.findLines(it) })
        }
    }
    parser.subcommands(Index(), Common(), Info(), Lines())
    try {
        parser.parse(args)
    } catch (e: InvalidInputException) {
        println(e.message)
    } catch (e: IllegalStateException) {
        println(e.message)
    } catch (e: Exception) {
        println("Unknown error")
    }
}

