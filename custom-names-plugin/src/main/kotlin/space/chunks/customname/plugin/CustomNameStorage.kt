package space.chunks.customname.plugin

import java.util.*

object CustomNameStorage {

    private val customPlayerNameMap: MutableMap<UUID, CustomNameImpl> = HashMap()

    fun getCustomPlayerName(uuid: UUID): CustomNameImpl? {
        return customPlayerNameMap[uuid]
    }

    fun registerNew(entityId: UUID, name: CustomNameImpl) {
        customPlayerNameMap[entityId] = name
    }

    fun remove(uuid: UUID): CustomNameImpl? {
        val name = customPlayerNameMap.remove(uuid)
        name?.close()

        return name
    }

}