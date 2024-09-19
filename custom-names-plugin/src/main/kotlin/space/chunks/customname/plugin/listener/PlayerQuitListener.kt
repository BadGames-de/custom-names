package space.chunks.customname.plugin.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import space.chunks.customname.plugin.CustomNameStorage

class PlayerQuitListener(
    private val plugin: JavaPlugin
) : Listener {

    @EventHandler
    fun playerQuit(event: PlayerQuitEvent) {
        object : BukkitRunnable() {
            override fun run() {
                CustomNameStorage.remove(event.player.uniqueId) // Be safe, remove long enough so the entity is removed
            }
        }.runTaskLater(this.plugin, 5)
    }

}