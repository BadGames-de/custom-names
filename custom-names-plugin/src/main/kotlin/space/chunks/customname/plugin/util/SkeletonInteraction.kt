package space.chunks.customname.plugin.util

import io.netty.buffer.Unpooled
import io.papermc.paper.adventure.PaperAdventure
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.*
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData.DataItem
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Pose
import net.minecraft.world.phys.Vec3
import org.bukkit.Location
import org.bukkit.entity.Entity
import space.chunks.customname.plugin.CustomNameImpl
import java.lang.reflect.Constructor
import java.util.*
import java.util.List

/**
 * This classed is used for sending packets related to the interaction entity
 * sent to the client.
 */
class SkeletonInteraction(
    private val customName: CustomNameImpl
) {
    fun removePacket(): Packet<ClientGamePacketListener> {
        return ClientboundRemoveEntitiesPacket(this.customName.getNametagId())
    }

    fun syncDataPacket(): Packet<ClientGamePacketListener> {
        val data: MutableList<DataValue<*>> = ArrayList()
        data.add(
            ofData(
                DataAccessors.DATA_CUSTOM_NAME,
                Optional.ofNullable(PaperAdventure.asVanilla(this.customName.getName()))
            )
        )

        var value = (if (this.customName.isTargetEntitySneaking()) 1 shl 1 else 0).toByte()
        value = (value.toInt() or 0x20).toByte()
        data.add(ofData(DataAccessors.DATA_SHARED_FLAGS_ID, value))

        return ClientboundSetEntityDataPacket(this.customName.getNametagId(), data)
    }

    fun getRiderPacket(): Packet<ClientGamePacketListener> {
        val buf = FriendlyByteBuf(Unpooled.buffer())
        buf.writeVarInt(this.customName.getTargetEntity().getEntityId())

        val passengerIds = this.passengerIds()
        buf.writeVarIntArray(passengerIds)

        // Use reflection to access the private constructor
        val constructor: Constructor<ClientboundSetPassengersPacket> = ClientboundSetPassengersPacket::class.java
            .getDeclaredConstructor(FriendlyByteBuf::class.java)
        constructor.isAccessible = true

        return constructor.newInstance(buf)
    }

    private fun passengerIds(): IntArray {
        val passengers: kotlin.collections.List<Entity> =
            this.customName.getTargetEntity().passengers //respect passengers
        var passengerIds = IntArray(passengers.size)
        if (!this.customName.isHidden()) {
            val length = passengerIds.size
            passengerIds = IntArray(length + 1)
            passengerIds[length] = this.customName.getNametagId() //always add the entity if it is visible
        }
        for (i in passengers.indices) {
            passengerIds[i] = passengers[i].entityId
        }
        return passengerIds
    }

    fun initialSpawnPacket(): Packet<*> {
        val initialCreatePacket = ClientboundSetEntityDataPacket(
            this.customName.getNametagId(), List.of<DataValue<*>>(
                ofData<Float>(DataAccessors.DATA_WIDTH_ID, 0f),
                ofData<Float>(DataAccessors.DATA_HEIGHT_ID, this.customName.getEffectiveHeight().toFloat()),
                ofData<Pose>(DataAccessors.DATA_POSE, Pose.CROAKING),
                ofData<Boolean>(DataAccessors.DATA_CUSTOM_NAME_VISIBLE, true)
            )
        )
        val syncData = syncDataPacket()
        val afterCreateData = ClientboundSetEntityDataPacket(
            this.customName.getNametagId(), List.of<DataValue<*>>(
                ofData<Float>(DataAccessors.DATA_HEIGHT_ID, 511f)
            )
        )

        return ClientboundBundlePacket(
            List.of<Packet<in ClientGamePacketListener>>(
                createPacket(),  // Create entity
                initialCreatePacket,
                syncData,
                this.getRiderPacket(),
                afterCreateData
            )
        )
    }

    // int id, UUID uuid, double x, double y, double z, float pitch, float yaw, EntityType<?> entityType, int entityData, Vec3 velocity, double headYaw
    private fun createPacket(): Packet<ClientGamePacketListener> {
        val location: Location = this.customName.getTargetEntity().getLocation()

        return ClientboundAddEntityPacket(
            this.customName.getNametagId(),
            UUID.randomUUID(),
            location.x(),
            location.y() + this.customName.getPassengerOffset(),  // Put the entity as close as possible to prevent lerping
            location.z(),
            0f,
            0f,
            EntityType.INTERACTION,
            0,
            Vec3.ZERO,
            0.0
        )
    }

    private fun <T> ofData(data: EntityDataAccessor<T>, value: T): DataValue<T> {
        return DataItem(data, value).value()
    }

}