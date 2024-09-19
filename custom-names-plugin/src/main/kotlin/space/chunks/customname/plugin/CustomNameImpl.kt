package space.chunks.customname.plugin

import net.kyori.adventure.text.Component
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.entity.CraftEntity
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.jetbrains.annotations.Nullable
import space.chunks.customname.api.CustomName
import space.chunks.customname.plugin.util.SkeletonInteraction
import java.util.function.Consumer

class CustomNameImpl(
    private val plugin: JavaPlugin,
    private val targetEntity: Entity
) : CustomName {

    private val interaction = SkeletonInteraction(this)

    // Target entity constants
    private var effectiveHeight = 0.0
    private var passengerOffset = 0.0

    // Custom name constants
    private var nametagEntityId = 0

    // States
    private var targetEntitySneaking = false

    @Nullable
    private var name: Component? = null
    private var hidden = false

    private var task: BukkitTask? = null

    init {
        this.nametagEntityId = Bukkit.getUnsafe().nextEntityId()

        val nmsEntity: net.minecraft.world.entity.Entity = (targetEntity as CraftEntity).handle
        val ridingOffset = nmsEntity.getPassengerRidingPosition(null).subtract(nmsEntity.position()).y
        val nametagOffset: Float = nmsEntity.type.dimensions.height + 0.5f

        // First, negate the riding offset to get to the bounding of the entity's bounding box
        // Negate the natural nametag offset of interaction entities (0.5)
        // Add the actual offset of the nametag
        this.effectiveHeight = -ridingOffset - 0.5 + nametagOffset
        this.passengerOffset = ridingOffset

        this.task = object : BukkitRunnable() {
            override fun run() {
                val riderPacket: Packet<ClientGamePacketListener> = this@CustomNameImpl.interaction.getRiderPacket()
                for (player in targetEntity.trackedPlayers) {
                    (player as CraftPlayer).handle.connection.send(riderPacket)
                }
            }
        }.runTaskTimer(plugin, 20, 20)
    }

    override fun setName(name: Component?) {
        this.name = name
        this.syncData()
    }

    override fun setTargetEntitySneaking(targetEntitySneaking: Boolean) {
        this.targetEntitySneaking = targetEntitySneaking
        this.syncData()
    }

    fun sendToClient(entity: Player) {
        if (this.hidden) {
            return
        }

        (entity as CraftPlayer).handle.connection.send(interaction.initialSpawnPacket())
    }

    fun removeFromClient(entity: Player) {
        (entity as CraftPlayer).handle.connection.send(interaction.removePacket())
    }

    override fun setHidden(hidden: Boolean) {
        this.hidden = hidden
        this.runOnTrackers { player ->
            if (hidden) {
                this.removeFromClient(player)
            } else {
                this.sendToClient(player)
            }
        }
    }

    @Nullable
    override fun getName(): Component? {
        return this.name
    }

    override fun getNametagId(): Int {
        return this.nametagEntityId
    }

    override fun getTargetEntity(): Entity {
        return this.targetEntity
    }

    override fun isTargetEntitySneaking(): Boolean {
        return this.targetEntitySneaking
    }

    override fun getEffectiveHeight(): Double {
        return this.effectiveHeight
    }

    override fun getPassengerOffset(): Double {
        return this.passengerOffset
    }

    // Utilities
    private fun syncData() {
        if (this.hidden) {
            return
        }

        val dataPacket: Packet<ClientGamePacketListener> = interaction.syncDataPacket()
        this.runOnTrackers { player ->
            (player as CraftPlayer).handle.connection.send(dataPacket)
        }
    }

    private fun runOnTrackers(consumer: Consumer<Player>) {
        for (player in targetEntity.trackedBy) {
            consumer.accept(player)
        }
    }

    fun close() {
        task!!.cancel()
    }

    override fun isHidden(): Boolean {
        return this.hidden
    }
}