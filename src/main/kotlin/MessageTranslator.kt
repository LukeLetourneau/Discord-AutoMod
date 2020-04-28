// TODO WIP

val leetTable = mapOf(
    '0' to 'o',
    '1' to 'i',
    '3' to 'e',
    '5' to 's',
    '6' to 'b',
    '7' to 't',
    '9' to 'g'
)

// val characterAliasTable = mapOf()

fun fullTranslate(message: String) =
    convertLeet(
        convertUnicode(
            removeWhitespace(message)
        )
    )

fun convertLeet(message: String): String {
    var nm = message;
    leetTable.forEach { nm = message.replace(it.key, it.value) }
    return nm
}

fun convertUnicode(message: String): String {
    return message
}

fun removeWhitespace(message: String) = message.replace(Regex("\\s"), "")