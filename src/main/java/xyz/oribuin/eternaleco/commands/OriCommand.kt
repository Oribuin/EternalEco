package xyz.oribuin.eternaleco.commands

import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import xyz.oribuin.eternaleco.EternalEco

abstract class OriCommand(open val plugin: EternalEco, // Get Command Name
                          private val name: String) : TabExecutor {
    fun register() {
        val cmd = Bukkit.getPluginCommand(name)
        if (cmd != null) {
            cmd.setExecutor(this)
            cmd.tabCompleter = this
            cmd.aliases = plugin.config.getStringList("aliases")
        }
    }

    abstract fun executeCommand(sender: CommandSender, args: Array<String>)

    abstract fun tabComplete(sender: CommandSender, args: Array<String>): List<String>?

    // Register Command
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        executeCommand(sender, args)
        return true
    }

    // Register command tab complete
    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<String>): List<String>? {
        return tabComplete(sender, args)
    }

    val command: Command?
        get() = plugin.getCommand(name)

}