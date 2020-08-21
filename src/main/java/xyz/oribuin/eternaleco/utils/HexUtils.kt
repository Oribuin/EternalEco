package xyz.oribuin.eternaleco.utils

import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import xyz.oribuin.eternaleco.utils.NMSUtil.versionNumber
import java.awt.Color
import java.util.*
import java.util.regex.Pattern
import java.util.stream.Collectors

object HexUtils {
    private val RAINBOW_PATTERN = Pattern.compile("<(rainbow|r)(:\\d*\\.?\\d+){0,2}>")
    private val GRADIENT_PATTERN = Pattern.compile("<(gradient|g)(:#([A-Fa-f0-9]){6})*>")
    private val HEX_PATTERNS = listOf(
            Pattern.compile("<#([A-Fa-f0-9]){6}>"),  // <#FFFFFF>
            Pattern.compile("&#([A-Fa-f0-9]){6}"),  // &#FFFFFF
            Pattern.compile("#([A-Fa-f0-9]){6}"), // #FFFFFF
            Pattern.compile("\\{#([A-Fa-f0-9])}{6}") // {#FFFFFF}
    )

    private val STOP = Pattern.compile("" +
            "<(gradient|g)(:#([A-Fa-f0-9]){6})*>|" + // <g:#hex:#hex:#hex>
            "<(rainbow|r)(:\\d*\\.?\\d+){0,2}>|" + // <rainbow:0.1-1.0>
            "(&[a-f0-9r])|<#([A-Fa-f0-9]){6}>|" + // <#Hex>
            "&#([A-Fa-f0-9]){6}|" + // &#Hex
            "#([A-Fa-f0-9]){6}|" // #Hex
            + org.bukkit.ChatColor.COLOR_CHAR)

    /**
     * Parses gradients, hex colors, and legacy color codes
     *
     * @param message The message
     * @return A color-replaced message
     */
    @JvmStatic
    fun colorify(message: String): String {
        var parsed = message
        parsed = parseRainbow(parsed)
        parsed = parseGradients(parsed)
        parsed = parseHex(parsed)
        parsed = parseLegacy(parsed)
        return parsed
    }

    private fun parseRainbow(message: String): String {
        var parsed = message
        var matcher = RAINBOW_PATTERN.matcher(parsed)

        while (matcher.find()) {
            val parsedRainbow = StringBuilder()
            val match = matcher.group()
            val tagLength = if (match.startsWith("<ra")) 8 else 2
            val indexOfClose = match.indexOf(">")
            var extraDataContent = match.substring(tagLength, indexOfClose)
            var extraData: DoubleArray

            if (extraDataContent.isNotEmpty()) {
                extraDataContent = extraDataContent.substring(1)
                extraData = Arrays.stream(extraDataContent.split(":").toTypedArray()).mapToDouble { s: String -> s.toDouble() }.toArray()
            } else {
                extraData = DoubleArray(0)
            }

            val saturation = if (extraData.isNotEmpty()) extraData[0].toFloat() else 1.0f
            val brightness = if (extraData.size > 1) extraData[1].toFloat() else 1.0f

            val stop = findStop(parsed, matcher.end())
            val content = parsed.substring(matcher.end(), stop)
            val rainbow = Rainbow(content.length, saturation, brightness)

            for (c in content.toCharArray())
                parsedRainbow.append(translateHex(rainbow.next())).append(c)

            val before = parsed.substring(0, matcher.start())
            val after = parsed.substring(stop)

            parsed = before + parsedRainbow + after
            matcher = RAINBOW_PATTERN.matcher(parsed)
        }
        return parsed
    }

    private fun parseGradients(message: String): String {
        var parsed = message
        var matcher = GRADIENT_PATTERN.matcher(parsed)

        while (matcher.find()) {
            val parsedGradient = StringBuilder()
            val match = matcher.group()
            val tagLength = if (match.startsWith("<gr")) 10 else 3
            val indexOfClose = match.indexOf(">")
            val hexContent = match.substring(tagLength, indexOfClose)

            val hexSteps = Arrays.stream(hexContent.split(":").toTypedArray()).map { nm: String? -> Color.decode(nm) }.collect(Collectors.toList())

            val stop = findStop(parsed, matcher.end())
            val content = parsed.substring(matcher.end(), stop)
            val gradient = Gradient(hexSteps, content.length)

            for (c in content.toCharArray())
                parsedGradient.append(translateHex(gradient.next())).append(c)
            val before = parsed.substring(0, matcher.start())
            val after = parsed.substring(stop)

            parsed = before + parsedGradient + after
            matcher = GRADIENT_PATTERN.matcher(parsed)
        }
        return parsed
    }

    private fun parseHex(message: String): String {
        var parsed = message

        for (pattern in HEX_PATTERNS) {
            var matcher = pattern.matcher(parsed)
            while (matcher.find()) {
                val color = translateHex(cleanHex(matcher.group()))
                val before = parsed.substring(0, matcher.start())
                val after = parsed.substring(matcher.end())
                parsed = before + color + after
                matcher = pattern.matcher(parsed)
            }
        }
        return parsed
    }

    private fun parseLegacy(message: String): String {
        return ChatColor.translateAlternateColorCodes('&', message)
    }

    /**
     * Returns the index before the color changes
     *
     * @param content The content to search through
     * @param searchAfter The index at which to search after
     * @return the index of the color stop, or the end of the string index if none is found
     */
    private fun findStop(content: String, searchAfter: Int): Int {
        val matcher = STOP.matcher(content)
        while (matcher.find()) {
            if (matcher.start() > searchAfter) return matcher.start()
        }
        return content.length - 1
    }

    private fun cleanHex(hex: String): String {
        return when {
            hex.startsWith("<") -> {
                hex.substring(1, hex.length - 1)
            }
            hex.startsWith("&") -> {
                hex.substring(1)
            }
            else -> {
                hex
            }
        }
    }

    /**
     * Finds the closest hex or ChatColor value as the hex string
     *
     * @param hex The hex color
     * @return The closest ChatColor value
     */
    private fun translateHex(hex: String): String {
        return if (versionNumber >= 16) ChatColor.of(hex).toString() else translateHex(Color.decode(hex))
    }

    private fun translateHex(color: Color): String {
        if (versionNumber >= 16) return ChatColor.of(color).toString()
        var minDist = Int.MAX_VALUE
        var legacy = ChatColor.WHITE

        for (mapping in ChatColorHexMapping.values()) {
            val r = mapping.red - color.red
            val g = mapping.green - color.green
            val b = mapping.blue - color.blue
            val dist = r * r + g * g + b * b
            if (dist < minDist) {
                minDist = dist
                legacy = mapping.chatColor
            }
        }
        return legacy.toString()
    }

    /**
     * Maps hex codes to ChatColors
     */
    private enum class ChatColorHexMapping(hex: Int, val chatColor: ChatColor) {
        BLACK(0x000000, ChatColor.BLACK),
        DARK_BLUE(0x0000AA, ChatColor.DARK_BLUE),
        DARK_GREEN(0x00AA00, ChatColor.DARK_GREEN),
        DARK_AQUA(0x00AAAA, ChatColor.DARK_AQUA),
        DARK_RED(0xAA0000, ChatColor.DARK_RED),
        DARK_PURPLE(0xAA00AA, ChatColor.DARK_PURPLE),
        GOLD(0xFFAA00, ChatColor.GOLD),
        GRAY(0xAAAAAA, ChatColor.GRAY),
        DARK_GRAY(0x555555, ChatColor.DARK_GRAY),
        BLUE(0x5555FF, ChatColor.BLUE),
        GREEN(0x55FF55, ChatColor.GREEN),
        AQUA(0x55FFFF, ChatColor.AQUA),
        RED(0xFF5555, ChatColor.RED),
        LIGHT_PURPLE(0xFF55FF, ChatColor.LIGHT_PURPLE),
        YELLOW(0xFFFF55, ChatColor.YELLOW),
        WHITE(0xFFFFFF, ChatColor.WHITE);

        val red: Int = hex shr 16 and 0xFF
        val green: Int = hex shr 8 and 0xFF
        val blue: Int = hex and 0xFF

    }

    /**
     * Allows generation of a multi-part gradient with a fixed number of steps
     */
    class Gradient(colors: List<Color>, totalColors: Int) {
        private val colors: List<Color>
        private val stepSize: Int
        private var step: Int
        private var stepIndex: Int

        /**
         * @return the next color in the gradient
         */
        operator fun next(): Color {
            // Gradients will use the first color of the entire spectrum won't be available to preserve prettiness
            if (versionNumber < 16) return colors[0]
            val color: Color
            color = if (stepIndex + 1 < colors.size) {
                val start = colors[stepIndex]
                val end = colors[stepIndex + 1]
                val interval = step.toFloat() / stepSize
                getGradientInterval(start, end, interval)
            } else {
                colors[colors.size - 1]
            }
            step += 1
            if (step >= stepSize) {
                step = 0
                stepIndex++
            }
            return color
        }

        companion object {
            /**
             * Gets a color along a linear gradient between two colors
             *
             * @param start The start color
             * @param end The end color
             * @param interval The interval to get, between 0 and 1 inclusively
             * @return A Color at the interval between the start and end colors
             */
            fun getGradientInterval(start: Color, end: Color, interval: Float): Color {
                require(!(0 > interval || interval > 1)) { "Interval must be between 0 and 1 inclusively." }
                val r = (end.red * interval + start.red * (1 - interval)).toInt()
                val g = (end.green * interval + start.green * (1 - interval)).toInt()
                val b = (end.blue * interval + start.blue * (1 - interval)).toInt()
                return Color(r, g, b)
            }
        }

        init {
            require(colors.size >= 2) { "Must provide at least 2 colors" }
            require(totalColors >= 1) { "Must have at least 1 total color" }
            this.colors = colors
            stepSize = totalColors / (colors.size - 1)
            stepIndex = 0
            step = stepIndex
        }
    }

    /**
     * Allows generation of a rainbow gradient with a fixed numbef of steps
     */
    class Rainbow @JvmOverloads constructor(totalColors: Int, saturation: Float = 1.0f, brightness: Float = 1.0f) {
        private val hueStep: Float
        private val saturation: Float
        private val brightness: Float
        private var hue: Float

        /**
         * @return the next color in the gradient
         */
        operator fun next(): Color {
            val color = Color.getHSBColor(hue, saturation, brightness)
            hue += hueStep
            return color
        }

        init {
            require(totalColors >= 1) { "Must have at least 1 total color" }
            require(!(0.0f > saturation || saturation > 1.0f)) { "Saturation must be between 0.0 and 1.0" }
            require(!(0.0f > brightness || brightness > 1.0f)) { "Saturation must be between 0.0 and 1.0" }
            hueStep = 1.0f / totalColors
            this.saturation = saturation
            this.brightness = brightness
            hue = 0f
        }
    }
}
object NMSUtil {
    private var cachedVersion: String? = null
    private var cachedVersionNumber = -1

    /**
     * Gets the server version
     *
     * @return The server version
     */
    private val version: String?
        get() {
            if (cachedVersion == null) {
                val name = Bukkit.getServer().javaClass.getPackage().name
                cachedVersion = name.substring(name.lastIndexOf('.') + 1) + "."
            }
            return cachedVersion
        }

    /**
     * Gets the server version major release number
     *
     * @return The server version major release number
     */
    @JvmStatic
    val versionNumber: Int
        get() {
            if (cachedVersionNumber == -1) {
                val name = version!!.substring(3)
                cachedVersionNumber = name.substring(0, name.length - 4).toInt()
            }
            return cachedVersionNumber
        }

    /**
     * @return true if the server is running Spigot or a fork, false otherwise
     */
    val isSpigot: Boolean
        get() = try {
            Class.forName("org.spigotmc.SpigotConfig")
            true
        } catch (e: ClassNotFoundException) {
            false
        }
}