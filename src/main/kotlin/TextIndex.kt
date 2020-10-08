import java.io.File

val partOfSpeech = mutableMapOf<String, String>()

val defaultToForms =
    File("odict.csv").readLines().map { line ->
        val words = line.split(",")
        val defaultForm = words[0]
        partOfSpeech[defaultForm] = words[1]
        defaultForm to words.drop(2)
    }.toMap()

val formToDefault = partOfSpeech.map {(default, forms) ->
    forms.map { it to default }
}.flatten().toMap()


fun readFile(fileName: String): List<String> {
    return File(fileName).readLines().filter { it.isNotBlank() }
}



fun main() {
    println(formToDefault.toList().last())
}
