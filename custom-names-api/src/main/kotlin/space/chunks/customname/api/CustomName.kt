package space.chunks.customname.api

import net.kyori.adventure.text.Component
import org.bukkit.entity.Entity
import org.jetbrains.annotations.Nullable

interface CustomName {

    /**
     * Sets the custom name for the entity.
     *
     * @param name The custom name to set, or null to remove the custom name.
     */
    fun setName(name: Component?)

    /**
     * Sets whether the target entity is sneaking.
     *
     * @param targetEntitySneaking True if the entity is sneaking, false otherwise.
     */
    fun setTargetEntitySneaking(targetEntitySneaking: Boolean)

    /**
     * Sets whether the custom name is hidden.
     *
     * @param hidden True to hide the custom name, false to show it.
     */
    fun setHidden(hidden: Boolean)

    /**
     * Gets the current custom name.
     *
     * @return The current custom name, or null if not set.
     */
    @Nullable
    fun getName(): Component?

    /**
     * Gets the ID of the nametag entity.
     *
     * @return The nametag entity ID.
     */
    fun getNametagId(): Int

    /**
     * Gets the target entity associated with this custom name.
     *
     * @return The target entity.
     */
    fun getTargetEntity(): Entity

    /**
     * Checks if the target entity is sneaking.
     *
     * @return True if the target entity is sneaking, false otherwise.
     */
    fun isTargetEntitySneaking(): Boolean

    /**
     * Gets the effective height of the custom name above the entity.
     *
     * @return The effective height.
     */
    fun getEffectiveHeight(): Double

    /**
     * Gets the passenger offset for the custom name.
     *
     * @return The passenger offset.
     */
    fun getPassengerOffset(): Double

    /**
     * Checks if the custom name is hidden.
     *
     * @return True if the custom name is hidden, false otherwise.
     */
    fun isHidden(): Boolean

}
