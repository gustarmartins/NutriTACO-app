package com.mekki.taco.utils

import java.text.Normalizer

fun String.unaccent(): String {
    // ensure composed characters (like 'ê') are single chars, not 'e' + '^'
    val nfc = Normalizer.normalize(this, Normalizer.Form.NFC)

    val map = mapOf(
        'á' to 'a', 'à' to 'a', 'ã' to 'a', 'â' to 'a', 'ä' to 'a',
        'é' to 'e', 'è' to 'e', 'ê' to 'e', 'ë' to 'e',
        'í' to 'i', 'ì' to 'i', 'î' to 'i', 'ï' to 'i',
        'ó' to 'o', 'ò' to 'o', 'õ' to 'o', 'ô' to 'o', 'ö' to 'o',
        'ú' to 'u', 'ù' to 'u', 'û' to 'u', 'ü' to 'u',
        'ç' to 'c', 'ñ' to 'n',
        'Á' to 'A', 'À' to 'A', 'Ã' to 'A', 'Â' to 'A', 'Ä' to 'A',
        'É' to 'E', 'È' to 'E', 'Ê' to 'E', 'Ë' to 'E',
        'Í' to 'I', 'Ì' to 'I', 'Î' to 'I', 'Ï' to 'I',
        'Ó' to 'O', 'Ò' to 'O', 'Õ' to 'O', 'Ô' to 'O', 'Ö' to 'O',
        'Ú' to 'U', 'Ù' to 'U', 'Û' to 'U', 'Ü' to 'U',
        'Ç' to 'C', 'Ñ' to 'N'
    )

    val sb = StringBuilder()
    for (char in nfc) {
        sb.append(map[char] ?: char)
    }
    return sb.toString()
}

/**
 * Standardizes the string for FTS indexing and searching.
 * 1. Removes accents (unaccent).
 * 2. Lowercases.
 * 3. Replaces punctuation with space (handles "Pão, de queijo").
 * 4. Collapses multiple spaces.
 * 5. Trims.
 */
fun String.normalizeForSearch(): String {
    return this.unaccent().lowercase()
        .replace(Regex("[^a-z0-9 ]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}
