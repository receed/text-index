import java.io.File
import kotlinx.cli.*

val LINES_PER_PAGE = 45
val partOfSpeech = mutableMapOf<String, String>()

val defaultToForms =
    File("odict.csv").readLines().map { line ->
        val words = line.split(",")
        val defaultForm = words[0]
        partOfSpeech[defaultForm] = words[1]
        defaultForm to words.drop(2) + defaultForm
    }.toMap()

val formToDefault = defaultToForms.map {(default, forms) ->
    forms.map { it to default }
}.flatten().toMap()

val formToLines = mutableMapOf<String, MutableList<Int>>()
val defaultToPages = mutableMapOf<String, MutableSet<Int>>()

fun readFile(fileName: String): List<String> {
    return File(fileName).readLines().filter { it.isNotBlank() }
}

fun lineToWords(line: String) = line.replace(Regex("--|[^а-яА-Я-]"), " ").split(" ")

fun getPageOfLine(line: Int) = line / LINES_PER_PAGE + 1

fun generateIndex(): String {
    return defaultToPages.toList().joinToString("\n") { (default, pages) -> "$default: ${pages.size}" }
}

fun main(args: Array<String>) {
    val parser = ArgParser("example")
    val input by parser.option(ArgType.String, shortName = "i", description = "Input file name").required()
    val output by parser.option(ArgType.String, shortName = "o", description = "Output file name")

    class Index: Subcommand("index", "Create index of file") {
        override fun execute() {

        }
    }
    class Info: Subcommand("info", "Analyze file") {
        override fun execute() {

        }
    }
    class Lines: Subcommand("lines", "Find lines containing word") {
        override fun execute() {

        }
    }
    parser.subcommands(Index(), Info(), Lines())
    parser.parse(args)
    val lines = readFile(input)
    for ((lineNumber, line) in lines.withIndex())
        for (word in lineToWords(line)) {
            formToLines.getOrPut(word, { mutableListOf() }).add(lineNumber)
            val default = formToDefault[word]
            default?.let {
                defaultToPages.getOrPut(it, { mutableSetOf() }).add(getPageOfLine(lineNumber))
            }
        }
    println(formToDefault.toList().last())
}
