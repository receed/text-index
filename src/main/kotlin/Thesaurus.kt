import java.io.File

val syntacticCategoryNames = mapOf('n' to "noun", 'v' to "verb", 'a' to "adj", 'r' to "adverb")

data class Synset(val offset: Int, val category: Char)

val wordSynsets = mutableMapOf<String, MutableList<Synset>>()
val synsetHyponyms = mutableMapOf<Synset, List<Synset>>()
val wordsInCategory = mutableMapOf<String, List<String>>()
val hyponymSymbols = setOf("~", "-c")

fun readThesaurus() {
    for ((categorySymbol, category) in syntacticCategoryNames) {
        // Format of each line:
        // lemma  pos  synset_cnt  p_cnt  [ptr_symbol...]  sense_cnt  tagsense_cnt   synset_offset  [synset_offset...]
        File("rwn3/index.$category").forEachLine { line ->
            val items = line.split(" ").filter { it.isNotBlank() }
            val (lemma, _, synsetCnt) = items
            wordSynsets.getOrDefault(lemma, mutableListOf())
                .addAll(items.takeLast(synsetCnt.toInt()).map { Synset(it.toInt(), categorySymbol) })
        }
        // Format of each line:
        // synset_offset  lex_filenum  ss_type  w_cnt  word  lex_id  [word  lex_id...]  p_cnt  [ptr...]  [frames...]  |   gloss
        // Format of each ptr:
        // pointer_symbol  synset_offset  pos  source/target
        File("rwn3/data.$category").forEachLine { line ->
            val items = line.split(" ").filter { it.isNotBlank() }
            val wordCount = items[3].toInt()
            val pointerCount = items[4 + wordCount * 2].toInt()
            val synset = Synset(items[0].toInt(), categorySymbol)
            synsetHyponyms[synset] = items.asSequence().drop(5 + wordCount * 2).chunked(4).take(pointerCount)
                .filter { it[0] in hyponymSymbols }
                .map { (_, synsetOffset, pointerCategory, _) -> Synset(synsetOffset.toInt(), pointerCategory[0]) }
                .toList()
        }
    }
}