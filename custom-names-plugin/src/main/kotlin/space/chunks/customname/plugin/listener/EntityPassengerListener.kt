package space.chunks.customname.plugin.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.event.entity.EntityMountEvent
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import space.chunks.customname.plugin.CustomNameStorage

/**
 * Responsible for hiding the name on entities that have
 * vehicles on them.
 *
 * This matches vanilla behavior.
 */
class EntityPassengerListener(
    private val plugin: JavaPlugin,
) : Listener {

    @EventHandler(ignoreCancelled = true)
    fun mountEntity(event: EntityMountEvent) {
        val playerName = CustomNameStorage.getCustomPlayerName(event.mount.uniqueId)?: return

        playerName.setHidden(true)
    }

    @EventHandler(ignoreCancelled = true)
    fun dismountEntity(event: EntityDismountEvent) {
        val playerName = CustomNameStorage.getCustomPlayerName(event.dismounted.uniqueId)?: return

        if (event.dismounted.passengers.size == 1) {
            // Run 2 ticks later, we need to ensure that the game sends the packets to update the
            // passengers.
            object : BukkitRunnable() {
                override fun run() {
                    playerName.setHidden(false)
                }
            }.runTaskLater(this.plugin, 2)
        }
    }

}