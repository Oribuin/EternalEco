package xyz.oribuin.eternaleco.commands

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import xyz.oribuin.eternaleco.EternalEco
import xyz.oribuin.eternaleco.managers.ConfigManager
import xyz.oribuin.eternaleco.managers.DataManager
import xyz.oribuin.eternaleco.managers.MessageManager
import xyz.oribuin.eternaleco.utils.HexUtils
import xyz.oribuin.eternaleco.utils.StringPlaceholders

class EconomyCmd(override val plugin: EternalEco) : OriCommand(plugin, "eternaleco") {
    private val messageManager = plugin.getManager(MessageManager::class)
    private val data = plugin.getManager(DataManager::class)

    // args[0] = set = args.size == 1
    // args[1] = player = args.size == 2
    // args[2] = total = args.size == 3

    override fun executeCommand(sender: CommandSender, args: Array<String>) {
        if (args.isEmpty()) {
            this.onHelpCommand(sender)
            return
        }

        if (args.size == 1) {
            when (args[0].toLowerCase()) {
                "help" -> {
                    this.onHelpCommand(sender)
                }

                "reload" -> {
                    this.onReloadCommand(sender)
                }

                "balance" -> {
                    this.onBalanceCommand(sender, null)
                }

                else -> {
                    messageManager.sendMessage(sender, "unknown-command")
                }
            }

            return
        }

        if (args.size == 2) {

            val player = Bukkit.getPlayer(args[1])?.uniqueId?.let { Bukkit.getOfflinePlayer(it) }

            if (player == null) {
                messageManager.sendMessage(sender, "invalid-player")
                return
            }

            when (args[0].toLowerCase()) {

                "reset" -> {
                    this.onResetCommand(sender, player)
                }

                "balance" -> {
                    this.onBalanceCommand(sender, player)
                }

                else -> {
                    messageManager.sendMessage(sender, "unknown-command")
                }
            }

            return
        }

        if (args.size >= 3) {
            try {
                val player = Bukkit.getPlayer(args[1])?.uniqueId?.let { Bukkit.getOfflinePlayer(it) }
                val amount = args[2].toInt()

                if (player == null) {
                    messageManager.sendMessage(sender, "invalid-player")
                    return
                }

                when (args[0].toLowerCase()) {
                    "add" -> {
                        this.onAddCommand(sender, player, amount)
                    }

                    "remove" -> {
                        this.onRemoveCommand(sender, player, amount)
                    }

                    "set" -> {
                        this.onSetCommand(sender, player, amount)
                    }

                    else -> {
                        messageManager.sendMessage(sender, "unknown-command")
                    }

                }

            } catch (ex: NumberFormatException) {
                messageManager.sendMessage(sender, "invalid-arguments")
            }
        }
    }

    private fun onHelpCommand(sender: CommandSender) {
        if (!sender.hasPermission("eternaleco.help")) {
            messageManager.sendMessage(sender, "invalid-permission")
            return
        }

        for (string in messageManager.messageConfig.getStringList("help-message")) {
            sender.sendMessage(HexUtils.colorify(string))
        }

        if (sender is Player) {
            sender.playSound(sender.location, Sound.ENTITY_ARROW_HIT_PLAYER, 50f, 1f)
        }
    }

    private fun onReloadCommand(sender: CommandSender) {
        val messageManager = plugin.getManager(MessageManager::class)

        if (!sender.hasPermission("eternaleco.reload")) {
            messageManager.sendMessage(sender, "invalid-permission")
            return
        }

        this.plugin.reload()
        messageManager.sendMessage(sender, "reload", StringPlaceholders.single("version", this.plugin.description.version))
    }

    private fun onBalanceCommand(sender: CommandSender, player: OfflinePlayer?) {
        if (!sender.hasPermission("eternaleco.balance")) {
            messageManager.sendMessage(sender, "invalid-permission")
            return
        }

        if (player == null) {
            if (sender !is Player) {
                messageManager.sendMessage(sender, "player-only")
                return
            }


            val placeholders = StringPlaceholders.builder()
                    .addPlaceholder("balance", data.getBalance(Bukkit.getOfflinePlayer(sender.uniqueId)))
                    .addPlaceholder("currency", MessageManager.MsgSettings.CURRENCY_NAME.string)
                    .build()

            messageManager.sendMessage(sender, "commands.balance", placeholders)
        } else {

            if (!sender.hasPermission("eternaleco.balance.other")) {
                messageManager.sendMessage(sender, "invalid-permission")
                return
            }

            val placeholders = StringPlaceholders.builder()
                    .addPlaceholder("balance", data.getBalance(Bukkit.getOfflinePlayer(player.uniqueId)))
                    .addPlaceholder("currency", MessageManager.MsgSettings.CURRENCY_NAME.string)
                    .build()

            messageManager.sendMessage(sender, "commands.balance", placeholders)
        }
    }

    private fun onResetCommand(sender: CommandSender, player: OfflinePlayer?) {
        if (!sender.hasPermission("eternaleco.reset")) {
            messageManager.sendMessage(sender, "invalid-permission")
            return
        }

        if (player == null) {
            if (sender !is Player) {
                messageManager.sendMessage(sender, "player-only")
                return
            }

            data.updateBalance(Bukkit.getOfflinePlayer(sender.uniqueId), ConfigManager.Setting.STARTING_BALANCE.int)
            messageManager.sendMessage(sender, "commands.reset-balance")
        } else {

            if (!sender.hasPermission("eternaleco.reset.other")) {
                messageManager.sendMessage(sender, "invalid-permission")
                return
            }

            data.updateBalance(player, ConfigManager.Setting.STARTING_BALANCE.int)
            messageManager.sendMessage(sender, "commands.reset-balance-other", StringPlaceholders.single("player", player.name))
        }
    }

    private fun onRemoveCommand(sender: CommandSender, player: OfflinePlayer, amount: Int) {
        if (!sender.hasPermission("eternaleco.remove")) {
            messageManager.sendMessage(sender, "invalid-permission")
            return
        }

        if (data.getBalance(player) < amount) {
            messageManager.sendMessage(sender, "insufficient-funds")
            return
        }

        if (amount <= 0) {
            messageManager.sendMessage(sender, "positive-only")
            return
        }

        val placeholders = StringPlaceholders.builder()
                .addPlaceholder("amount", amount)
                .addPlaceholder("currency", MessageManager.MsgSettings.CURRENCY_NAME.string)
                .addPlaceholder("player", player.name)
                .addPlaceholder("sender", sender.name)
                .addPlaceholder("balance", data.getBalance(player) - amount)
                .build()


        data.updateBalance(player, data.getBalance(player) - amount)
        messageManager.sendMessage(sender, "commands.money-removed-other", placeholders)
        player.player?.let { messageManager.sendMessage(it, "commands.money-removed", placeholders) }
    }

    private fun onAddCommand(sender: CommandSender, player: OfflinePlayer, amount: Int) {
        if (!sender.hasPermission("eternaleco.add")) {
            messageManager.sendMessage(sender, "invalid-permission")
            return
        }

        if (amount <= 0) {
            messageManager.sendMessage(sender, "positive-only")
            return
        }

        val placeholders = StringPlaceholders.builder()
                .addPlaceholder("amount", amount)
                .addPlaceholder("currency", MessageManager.MsgSettings.CURRENCY_NAME.string)
                .addPlaceholder("player", player.name)
                .addPlaceholder("sender", sender.name)
                .addPlaceholder("balance", data.getBalance(player) + amount)
                .build()

        data.updateBalance(player, data.getBalance(player) + amount)
        messageManager.sendMessage(sender, "commands.money-add", placeholders)
        player.player?.let { messageManager.sendMessage(it, "commands.money-received", placeholders) }
    }

    private fun onSetCommand(sender: CommandSender, player: OfflinePlayer, amount: Int) {
        if (!sender.hasPermission("eternaleco.set")) {
            messageManager.sendMessage(sender, "invalid-permission")
            return
        }

        val placeholders = StringPlaceholders.builder()
                .addPlaceholder("amount", amount)
                .addPlaceholder("currency", MessageManager.MsgSettings.CURRENCY_NAME.string)
                .addPlaceholder("player", player.name)
                .addPlaceholder("sender", sender.name)
                .build()


        data.updateBalance(player, amount)
        messageManager.sendMessage(sender, "commands.money-set-other", placeholders)
        player.player?.let { messageManager.sendMessage(it, "commands.money-set", placeholders) }
    }

    override fun tabComplete(sender: CommandSender, args: Array<String>): List<String>? {
        val suggestions: MutableList<String> = ArrayList()
        if (args.isEmpty() || args.size == 1) {
            val subCommand = if (args.isEmpty()) "" else args[0]
            val commands = mutableListOf<String>()

            if (sender.hasPermission("eternaleco.help"))
                commands.add("help")

            if (sender.hasPermission("eternaleco.reload"))
                commands.add("reload")

            if (sender.hasPermission("eternaleco.balance"))
                commands.add("balance")

            if (sender.hasPermission("eternaleco.reset"))
                commands.add("reset")

            if (sender.hasPermission("eternaleco.remove"))
                commands.add("remove")

            if (sender.hasPermission("eternaleco.add"))
                commands.add("add")

            if (sender.hasPermission("eternaleco.set"))
                commands.add("set")


            StringUtil.copyPartialMatches(subCommand, commands, suggestions)
        } else if (args.size == 2) {

            val players: MutableList<String> = ArrayList()
            Bukkit.getOnlinePlayers().stream().filter { player -> !player.hasPermission("vanished") }.forEach { player -> players.add(player.name) }


            if (args[0].toLowerCase() == "balance" && sender.hasPermission("eternaleco.balance.other")
                    || args[0].toLowerCase() == "reset" && sender.hasPermission("eternaleco.reset.other")) {
                StringUtil.copyPartialMatches(args[1].toLowerCase(), players, suggestions)
            }

            when (args[0].toLowerCase()) {
                "add" -> {
                    if (sender.hasPermission("eternaleco.add"))
                        StringUtil.copyPartialMatches(args[1].toLowerCase(), players, suggestions)
                }

                "set" -> {
                    if (sender.hasPermission("eternaleco.set"))
                        StringUtil.copyPartialMatches(args[1].toLowerCase(), players, suggestions)
                }

                "remove" -> {
                    if (sender.hasPermission("eternaleco.remove"))
                        StringUtil.copyPartialMatches(args[1].toLowerCase(), players, suggestions)
                }
            }

        } else if (args.size == 3) {
            when (args[0].toLowerCase()) {
                "add" -> {
                    if (sender.hasPermission("eternaleco.add"))
                        StringUtil.copyPartialMatches(args[1].toLowerCase(), listOf("<number>"), suggestions)
                }

                "set" -> {
                    if (sender.hasPermission("eternaleco.set"))
                        StringUtil.copyPartialMatches(args[1].toLowerCase(), listOf("<number>"), suggestions)
                }

                "remove" -> {
                    if (sender.hasPermission("eternaleco.remove"))
                        StringUtil.copyPartialMatches(args[1].toLowerCase(), listOf("<number>"), suggestions)
                }
            }
        } else {
            return emptyList()
        }

        return suggestions
    }
}