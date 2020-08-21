package xyz.oribuin.eternaleco

import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import xyz.oribuin.eternaleco.commands.EconomyCmd
import xyz.oribuin.eternaleco.hooks.PlaceholderAPIHook
import xyz.oribuin.eternaleco.hooks.PlaceholderExp
import xyz.oribuin.eternaleco.listeners.PlayerJoin
import xyz.oribuin.eternaleco.managers.ConfigManager
import xyz.oribuin.eternaleco.managers.DataManager
import xyz.oribuin.eternaleco.managers.Manager
import xyz.oribuin.eternaleco.managers.MessageManager
import kotlin.reflect.KClass

class EternalEco : JavaPlugin() {
    private val managers = mutableMapOf<KClass<out Manager>, Manager>()

    override fun onEnable() {
        this.getManager(ConfigManager::class)
        this.getManager(DataManager::class)
        this.getManager(MessageManager::class)

        EconomyCmd(this).register()

        Bukkit.getPluginManager().registerEvents(PlayerJoin(this), this)

        if (PlaceholderAPIHook.enabled())
            PlaceholderExp(this).register()


        this.reload()
    }
    fun <M : Manager> getManager(managerClass: KClass<M>): M {
        synchronized(this.managers) {
            @Suppress("UNCHECKED_CAST")
            if (this.managers.containsKey(managerClass))
                return this.managers[managerClass] as M

            return try {
                val manager = managerClass.constructors.first().call(this)
                manager.reload()
                this.managers[managerClass] = manager
                manager
            } catch (ex: ReflectiveOperationException) {
                error("Failed to load manager for ${managerClass.simpleName}")
            }
        }
    }

    fun reload() {
        this.disableManagers()
        this.server.scheduler.cancelTasks(this)
        this.managers.values.forEach { manager -> manager.reload() }
    }

    override fun onDisable() {
        this.disableManagers()
    }

    private fun disableManagers() {
        this.managers.values.forEach { manager -> manager.disable() }
    }
}