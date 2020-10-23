import java.io.File
import kotlinx.cli.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.lang.IllegalStateException

// Maximum number of non-empty lines on a page
const val LINES_PER_PAGE = 45

// Exception for invalid input
class InvalidInputException(message: String) : Exception(message)

// Exception for bad dictionary format
class DictionaryError(message: String) : java.lang.Exception(message)

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
        Json.decodeFromString(file.readText())
    } catch (e: Exception) {
        throw InvalidInputException("Index file is invalid")
    }
}

// Writes lines to file or to result.txt
fun writeFile(fileName: String?, lines: List<String>) {
    File(fileName ?: "data/result.txt").writeText(lines.joinToString("\n"))
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

// Generates answers for tests using current version of program
fun generateAnswers() {
    main(arrayOf("lines", "пока", "-i", "data/index.txt", "-o", "data/lines.a"))
    main(arrayOf("common", "100", "-i", "data/index.txt", "-o", "data/common.a"))
    main(arrayOf("info", "голова", "взглянуть", "-i", "data/index.txt", "-o", "data/info.a"))
    main(arrayOf("group", "человек", "мебель", "-i", "data/index.txt", "-o", "data/group.a"))
}

// ArgParser can be used only once so we have to create it every time we call main()
// If ArgParser is initialized inside main() then input and output values will be inside main()
// And every subcommand which uses input and output will be inside main()
// To avoid this we create a class containg input, output and subcommands

class Main {
    val parser = ArgParser("TextIndex")
    val input by parser.option(ArgType.String, shortName = "i", description = "Input file name").required()
    val output by parser.option(ArgType.String, shortName = "o", description = "Output file name")

    // Subcommands for different modes
    inner class CreateIndex : Subcommand("index", "Create index of file") {
        override fun execute() {
            val lines = readFile(input)
            val dictionary = Dictionary()
            val index = Index(lines, dictionary)
            File(output ?: "index.txt").writeText(Json.encodeToString(index))
        }
    }

    inner class Common : Subcommand("common", "Get most common words") {
        val count by argument(ArgType.Int, description = "Number of most frequent words to find")
        override fun execute() {
            val index = getIndex(input)
            writeFile(output, index.getMostFrequent(count).map { (word, occurrences) -> "$word: $occurrences" })
        }
    }

    inner class Group : Subcommand("group", "Get usage data for words in a group") {
        val groups by argument(ArgType.String, description = "Groups to find").vararg()
        override fun execute() {
            val index = getIndex(input)
            readThesaurus()
            writeFile(output, groups.flatMap { index.generateGroupReport(it) })
        }
    }

    inner class Info : Subcommand("info", "Analyze usage of given words") {
        val words by argument(ArgType.String, description = "Words to find").vararg()
        override fun execute() {
            val index = getIndex(input)
            writeFile(output, words.flatMap { index.generateReport(it) })
        }
    }

    inner class Lines : Subcommand("lines", "Find lines containing word") {
        val words by argument(ArgType.String, description = "Words to find").vararg()
        override fun execute() {
            val index = getIndex(input)
            writeFile(output, words.flatMap { listOf("$it:") + index.findLines(it) })
        }
    }

    inner class GenerateAnswers : Subcommand("gen", "Generate answers for tests") {
        override fun execute() {
            generateAnswers()
        }
    }

    // Entry point
    fun main(args: Array<String>) {
        parser.subcommands(CreateIndex(), Common(), Info(), Group(), Lines(), GenerateAnswers())
        try {
            parser.parse(args)
        } catch (e: InvalidInputException) {
            println(e.message)
        } catch (e: DictionaryError) {
            println("Dictionary contains errors: ${e.message}")
        } catch (e: IllegalStateException) {
            println(e.message)
        } catch (e: Exception) {
            e.printStackTrace()
            println("Unknown error")
        }
    }
}

fun main(args: Array<String>) = Main().main(args)