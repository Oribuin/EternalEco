package xyz.oribuin.eternaleco.managers

import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import xyz.oribuin.eternaleco.EternalEco
import xyz.oribuin.eternaleco.hooks.PlaceholderAPIHook
import xyz.oribuin.eternaleco.utils.FileUtils.createFile
import xyz.oribuin.eternaleco.utils.HexUtils.colorify
import xyz.oribuin.eternaleco.utils.StringPlaceholders
import xyz.oribuin.eternaleco.utils.StringPlaceholders.Companion.empty
import java.io.File

class MessageManager(plugin: EternalEco) : Manager(plugin) {
    lateinit var messageConfig: FileConfiguration

    override fun reload() {
        createFile(plugin, MESSAGE_CONFIG)
        messageConfig = YamlConfiguration.loadConfiguration(File(plugin.dataFolder, MESSAGE_CONFIG))

        for (value in MsgSettings.values()) {
            if (messageConfig.get(value.key) == null) {
                messageConfig.set(value.key, value.defaultValue)
            }
            value.load(messageConfig)
        }

        messageConfig.save(File(plugin.dataFolder, MESSAGE_CONFIG))
    }


    @JvmOverloads
    fun sendMessage(sender: CommandSender, messageId: String, placeholders: StringPlaceholders = empty()) {
        if (messageConfig.getString(messageId) == null) {
            sender.spigot().sendMessage(*TextComponent.fromLegacyText(colorify("#ff4072$messageId is null in messages.yml")))
            return
        }

        if (messageConfig.getString(messageId)!!.isNotEmpty()) {
            val msg = messageConfig.getString("prefix") + placeholders.apply(messageConfig.getString(messageId)!!)
            sender.spigot().sendMessage(*TextComponent.fromLegacyText(colorify(parsePlaceholders(sender, msg))))
        }
    }

    private fun parsePlaceholders(sender: CommandSender, message: String): String {
        return if (sender is Player)
            PlaceholderAPIHook.apply(sender, message)
        else
            message
    }

    companion object {
        private const val MESSAGE_CONFIG = "messages.yml"
    }

    override fun disable() {
        // Unused
    }

    enum class MsgSettings(val key: String, val defaultValue: Any) {
        // Misc Stuff
        PREFIX("prefix", "<rainbow:0.7>EternalEco &f» "),
        RELOAD("reload", "&bYou have reloaded EternalEco (&f%version%&b)"),
        CURRENCY_NAME("currency-name", "Shards"),

        // Command success messages
        CMD_BALANCE("commands.balance", "&bYou have &f%balance%&b %currency%!"),
        CMD_MONEY_RECEIVED("commands.money-received", "&bYou have gained &9%amount%&b %currency%!"),
        CMD_MONEY_ADD("commands.money-add", "&bYou given &f%player% &9%amount%&b &b%currency%!"),
        CMD_MONEY_REMOVED("commands.money-removed", "&bYou have lost &9%amount%&b %currency%"),
        CMD_MONEY_REMOVED_OTHER("commands.money-removed-other", "&bYou have removed &9%amount% &b%currency% from %player%&b's balance.s"),
        CMD_MONEY_SET("commands.money-set", "&bYour %currency% balance has been set to %amount%"),
        CMD_MONEY_SET_OTHER("commands.money-set-other", "&bYou have set &9%player%&b's balance to %amount%"),
        CMD_RESET_BALANCE("commands.reset-balance", "&bYou have reset your balance."),
        CMD_RESET_BALANCE_OTHER("commands.reset-balance-other", "&bYou have reset %player%'s balance"),

        // Help Menu
        HELP_MESSAGE("help-message", listOf(
                " ",
                " <rainbow:0.7>EternalEco &f» &bCommands",
                " &f• &b/eternaleco &fhelp #8E54E9- &bShow the help page.",
                " &f• &b/eternaleco &fbalance [Player] #8E54E9- &bGet balance",
                " &f• &b/eternaleco &fadd [Player] [Total] #8E54E9- &bGive a player's Shards.",
                " &f• &b/eternaleco &fremove [Player] [Total] #8E54E9- &bTake a player's Shards.",
                " &f• &b/eternaleco &freset [Player] #8E54E9- &bReset a player's Shards",
                " &f• &b/eternaleco &fset [Player] [Total] #8E54E9- &bSet a player's Shards.",
                " &f• &b/eternaleco &freload #8E54E9- &bReload plugin's configuration files.",
                " ",
                " &f» &bPlugin created by <g:#4776E6:#8E54E9>Oribuin",
                " "
        )),

        // Error Messages
        INVALID_PERMISSION("invalid-permission", "&cYou do not have permission for this command."),
        INVALID_PLAYER("invalid-player", "&cThat is not a valid player."),
        INVALID_ARGUMENTS("invalid-arguments", "&cYou have provided invalid arguments."),
        INSUFFICIENT_FUNDS("insufficient-funds", "&cThey do not have enough Shards for this."),
        HAS_BYPASS("has-bypass", "&cYou cannot report this player."),
        PLAYER_ONLY("only-player", "&cOnly a player can execute this command."),
        POSITIVE_ONLY("positive-only", "&cPlease inter a positive number."),

        UKNOWN_COMMAND("unknown-command", "&cAn unknown command was entered.");


        private var value: Any? = null

        /**
         * Gets the setting as a boolean
         *
         * @return The setting as a boolean
         */
        val boolean: Boolean
            get() = value as Boolean

        /**
         * @return the setting as a String
         */
        val string: String
            get() = value as String

        /**
         * @return the setting as a string list
         */
        val stringList: List<*>
            get() = value as List<*>

        /**
         * Loads the value from the config and caches it
         */
        fun load(config: FileConfiguration) {
            value = config[key]
        }

    }


}