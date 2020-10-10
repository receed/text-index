import java.io.File
import kotlinx.cli.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

val LINES_PER_PAGE = 45
val partOfSpeech = mutableMapOf<String, String>()

class InvalidInputException(message: String) : Exception(message)

val defaultToForms =
    File("odict.csv").readLines().map { line ->
        val words = line.split(",")
        val defaultForm = words[0]
        partOfSpeech[defaultForm] = words[1]
        defaultForm to words.drop(2) + defaultForm
    }.toMap()

val formToDefault = defaultToForms.map { (default, forms) ->
    forms.map { it to default }
}.flatten().toMap()

val defaultToPages = mutableMapOf<String, MutableSet<Int>>()

fun readFile(fileName: String): List<String> {
    val file = File(fileName)
    if (!file.exists())
        throw InvalidInputException("$fileName: no such file")
    return File(fileName).readLines().filter { it.isNotBlank() }
}

fun lineToWords(line: String) = line.replace(Regex("--|[^а-яА-Я-]"), " ").split(" ")

fun getPageOfLine(line: Int) = line / LINES_PER_PAGE + 1

fun generateIndex(): String {
    return defaultToPages.toList().joinToString("\n") { (default, pages) -> "$default: ${pages.size}" }
}

fun findLines(word: String): List<String> {
    return listOf()
}

@ExperimentalCli
fun main(args: Array<String>) {
    val parser = ArgParser("example")
    val input by parser.option(ArgType.String, shortName = "i", description = "Input file name").required()
    val output by parser.option(ArgType.String, shortName = "o", description = "Output file name")

    class Index : Subcommand("index", "Create index of file") {
        override fun execute() {
            val lines = readFile(input)
            val index = Index(lines)
            File(output ?: "index.txt").writeText(Json.encodeToString(index))
        }
    }

    class Info : Subcommand("info", "Analyze file") {
        override fun execute() {

        }
    }

    class Lines : Subcommand("lines", "Find lines containing word") {
        override fun execute() {
            val lines = readFile(input)
            val words by argument(ArgType.String, description = "Words to find").vararg()
            File(output ?: "result.txt").writeText(TODO())
        }
    }
    parser.subcommands(Index(), Info(), Lines())
    try {
        parser.parse(args)
    } catch (e: InvalidInputException) {
        println(e.message)
    }

    println(formToDefault.toList().last())
}
