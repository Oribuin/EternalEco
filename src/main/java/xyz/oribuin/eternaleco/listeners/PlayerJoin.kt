package xyz.oribuin.eternaleco.listeners

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import xyz.oribuin.eternaleco.EternalEco
import xyz.oribuin.eternaleco.managers.ConfigManager
import xyz.oribuin.eternaleco.managers.DataManager

class PlayerJoin(private val plugin: EternalEco) : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        plugin.getManager(DataManager::class).updateBalance(Bukkit.getOfflinePlayer(event.player.uniqueId), ConfigManager.Setting.STARTING_BALANCE.int)
    }
}