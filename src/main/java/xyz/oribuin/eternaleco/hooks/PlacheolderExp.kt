package xyz.oribuin.eternaleco.hooks

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import xyz.oribuin.eternaleco.EternalEco
import xyz.oribuin.eternaleco.managers.DataManager

class PlaceholderExp(private val plugin: EternalEco) : PlaceholderExpansion() {

    override fun onPlaceholderRequest(player: Player, placeholders: String): String? {

        if (placeholders.toLowerCase() == "balance")
            return plugin.getManager(DataManager::class).getBalance(Bukkit.getOfflinePlayer(player.uniqueId)).toString()

        return null
    }

    override fun persist(): Boolean {
        return true
    }

    override fun getIdentifier(): String {
        return plugin.description.name.toLowerCase()
    }

    override fun getAuthor(): String {
        return plugin.description.authors[0]
    }

    override fun getVersion(): String {
        return plugin.description.version
    }
}
