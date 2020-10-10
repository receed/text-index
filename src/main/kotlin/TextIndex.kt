import java.io.File
import kotlinx.cli.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*
import java.lang.IllegalStateException

val LINES_PER_PAGE = 45
val ignoredParts = setOf("межд.", "союз", "част.", "предл.", "мс-п")


class InvalidInputException(message: String) : Exception(message)

class Dictionary {
    val partOfSpeech = mutableMapOf<String, String>()
    val ignoredWords = mutableSetOf<String>("я", "он")
    val defaultToForms =
        File("odict.csv").readLines().map { line ->
            val words = line.toLowerCase().split(",")
            val defaultForm = words[0]
            partOfSpeech[defaultForm] = words[1]
            if (words[1] in ignoredParts)
                ignoredWords.add(defaultForm)
            defaultForm to words.drop(2) + defaultForm
        }.filterNotNull().toMap().filterKeys { it !in ignoredWords }

    val formToDefault =
        defaultToForms.map { (default, forms) ->
            forms.map { it to default }
        }.flatten().toMap()
}

fun readFile(fileName: String): List<String> {
    val file = File(fileName)
    if (!file.exists())
        throw InvalidInputException("$fileName: no such file")
    return File(fileName).readLines().filter { it.isNotBlank() }
}

fun lineToWords(line: String) = line.toLowerCase().replace(Regex("--|[^а-я-]"), " ").split(" ").filter { it.isNotBlank() }

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

fun writeFile(fileName: String?, lines: List<String>) {
    File(fileName ?: "result.txt").writeText(lines.joinToString("\n"))
}

@ExperimentalCli
fun main(args: Array<String>) {
    val parser = ArgParser("example")
    val input by parser.option(ArgType.String, shortName = "i", description = "Input file name").required()
    val output by parser.option(ArgType.String, shortName = "o", description = "Output file name")

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

    class Info : Subcommand("info", "Analyze file") {
        override fun execute() {
            val index = getIndex(input)
            TODO()
        }
    }

    class Lines : Subcommand("lines", "Find lines containing word") {
        val words by argument(ArgType.String, description = "Words to find").vararg()
        override fun execute() {
            val index = getIndex(input)
            writeFile(output, (words.flatMap { listOf("$it:") + index.findLines(it) }))
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

