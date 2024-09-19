package space.chunks.customname.api

import org.bukkit.entity.Entity

interface CustomNameManager {

    /**
     * Creates a new custom name for the given entity.
     * @param entity The entity to create a custom name for.
     * @return The custom name.
     */
    fun forEntity(entity: Entity): CustomName

    /**
     * Unregisters the custom name for the given entity.
     * @param entity The entity to unregister the custom name for.
     */
    fun unregister(entity: Entity)

}
