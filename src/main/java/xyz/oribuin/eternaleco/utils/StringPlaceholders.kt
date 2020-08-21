package xyz.oribuin.eternaleco.utils

import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Esophose
 */
class StringPlaceholders {
    private val placeholders: MutableMap<String, String> = mutableMapOf()

    fun addPlaceholder(placeholder: String, value: Any?) {
        placeholders[placeholder] = objectToString(value)
    }

    fun apply(string: String): String {
        var string = string

        for (key in placeholders.keys)
            string = string.replace(Pattern.quote("%$key%").toRegex(), Matcher.quoteReplacement(placeholders[key]))

        return string
    }

    fun getPlaceholders(): Map<String, String> {
        return Collections.unmodifiableMap(placeholders)
    }

    class Builder() {
        private val stringPlaceholders: StringPlaceholders = StringPlaceholders()

        constructor(placeholder: String, value: Any) : this() {
            stringPlaceholders.addPlaceholder(placeholder, objectToString(value))
        }

        fun addPlaceholder(placeholder: String, value: Any?): Builder {
            stringPlaceholders.addPlaceholder(placeholder, objectToString(value))
            return this
        }

        fun apply(string: String): String {
            return stringPlaceholders.apply(string)
        }

        fun build(): StringPlaceholders {
            return stringPlaceholders
        }

    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }

        fun builder(placeholder: String, value: Any?): Builder {
            return Builder(placeholder, objectToString(value))
        }

        @JvmStatic
        fun empty(): StringPlaceholders {
            return builder().build()
        }

        fun single(placeholder: String, value: Any?): StringPlaceholders {
            return builder(placeholder, value).build()
        }

        fun objectToString(`object`: Any?): String {
            return `object`?.toString() ?: "null"
        }
    }

}