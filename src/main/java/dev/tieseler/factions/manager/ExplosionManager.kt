package dev.tieseler.factions.manager

import dev.tieseler.factions.Factions
import dev.tieseler.factions.data.ChunkData
import dev.tieseler.factions.data.Faction
import dev.tieseler.factions.data.FactionPlayer
import dev.tieseler.factions.util.UUIDUtil
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.persistence.PersistentDataType
import org.hibernate.Session
import java.util.UUID

class ExplosionManager(
    val plugin: Factions
) {

    fun explodableBlocks(reason: UUID, blocks: MutableList<Block>): MutableList<Block> {
        val session = getSesson() ?: return mutableListOf()
        val factionPlayer = session.get(FactionPlayer::class.java, reason)
        if (factionPlayer?.faction == null) return unclaimedBlocks(session, blocks)
        val result = mutableListOf<Block>()
        result.addAll(unclaimedBlocks(session, blocks))
        result.addAll(claimedByOwnFactionBlocks(session, factionPlayer.faction!!, blocks))
        return result
    }

    private fun claimedByOwnFactionBlocks(session: Session, faction: Faction, blocks: MutableList<Block>): MutableList<Block> {
        val longStarted = System.currentTimeMillis()
        val result = blocks.filter { block ->
            val chunkId = UUIDUtil().parse(block.chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING))
            val chunkData = session.get(ChunkData::class.java, chunkId)
            if (chunkData?.faction == null) return@filter false
            chunkData.faction == faction
        }.toMutableList()
        val longEnded = System.currentTimeMillis()
        println("Time: ${longEnded - longStarted}ms")
        return result
    }

    private fun unclaimedBlocks(session: Session, blocks: MutableList<Block>): MutableList<Block> {
        val longStarted = System.currentTimeMillis()
        val result = blocks.filter { block ->
            val chunkId = UUIDUtil().parse(block.chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING))
            val chunkData = session.get(ChunkData::class.java, chunkId)
            chunkData?.faction == null
        }.toMutableList()
        val longEnded = System.currentTimeMillis()
        println("Time: ${longEnded - longStarted}ms")
        return result
    }

    private fun getSesson(): Session? {
        return plugin.databaseConnector?.sessionFactory?.openSession() ?: return null
    }

}