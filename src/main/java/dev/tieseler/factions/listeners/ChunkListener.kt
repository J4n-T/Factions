package dev.tieseler.factions.listeners

import dev.tieseler.factions.Factions
import dev.tieseler.factions.data.ChunkData
import dev.tieseler.factions.data.Claim
import dev.tieseler.factions.data.FactionPlayer
import dev.tieseler.factions.util.PermissionUtil
import dev.tieseler.factions.util.UUIDUtil
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkPopulateEvent
import org.bukkit.event.world.ChunkUnloadEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*

class ChunkListener : Listener {

    @EventHandler
    fun onChunkGeneration(event: ChunkPopulateEvent) {
        if (getChunkId(event.chunk) == null) setChunkId(event.chunk, UUID.randomUUID())
    }

    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        val chunkId = getChunkId(event.chunk) ?: setChunkId(event.chunk, UUID.randomUUID())
        val session = Factions.instance.databaseConnector!!.sessionFactory!!.openSession()
        while (!session.isOpen) {
            Thread.sleep(100)
        }

        var chunkData = session.get(ChunkData::class.java, chunkId)
        if (chunkData == null) {
            chunkData = ChunkData()
            chunkData.id = chunkId
            chunkData.x = event.chunk.x
            chunkData.z = event.chunk.z

            val transaction = session.beginTransaction()
            session.persist(chunkData)
            transaction.commit()
            session.close()
        }

        Factions.instance.chunks[chunkId] = chunkData
    }

    @EventHandler
    fun onChunkUnload(event: ChunkUnloadEvent) {
        val chunkId = getChunkId(event.chunk) ?: setChunkId(event.chunk, UUID.randomUUID())
        Factions.instance.chunks.remove(chunkId)
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val chunkId = getChunkId(event.block.chunk) ?: setChunkId(event.block.chunk, UUID.randomUUID())
        val session = Factions.instance.databaseConnector!!.sessionFactory!!.openSession()

        val chunkData = session.get(ChunkData::class.java, chunkId) ?: return
        val factionMember = session.get(FactionPlayer::class.java, event.player.uniqueId) ?: return

        if (PermissionUtil().isPermitted(factionMember, chunkData, Claim.BLOCK_BREAK)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        val chunkId = getChunkId(event.block.chunk) ?: setChunkId(event.block.chunk, UUID.randomUUID())
        val session = Factions.instance.databaseConnector!!.sessionFactory!!.openSession()

        val chunkData = session.get(ChunkData::class.java, chunkId) ?: return
        val factionMember = session.get(FactionPlayer::class.java, event.player.uniqueId) ?: return

        if (PermissionUtil().isPermitted(factionMember, chunkData, Claim.BLOCK_PLACE)) return
        event.isCancelled = true
    }

    @EventHandler
    fun onInteract(event: PlayerInteractEvent) {
        if (event.clickedBlock == null) return
        val block = event.clickedBlock!!

        val chunkId = getChunkId(block.chunk) ?: setChunkId(block.chunk, UUID.randomUUID())
        val session = Factions.instance.databaseConnector!!.sessionFactory!!.openSession()

        val chunkData = session.get(ChunkData::class.java, chunkId) ?: return
        val factionMember = session.get(FactionPlayer::class.java, event.player.uniqueId) ?: return

        if (PermissionUtil().isPermitted(factionMember, chunkData, Claim.INTERACT)) return
        event.isCancelled = true
    }

    private fun getChunkId(chunk: Chunk): UUID? {
        return UUIDUtil().parse(chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING))
    }

    private fun setChunkId(chunk: Chunk, id: UUID): UUID {
        chunk.persistentDataContainer.set(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING, id.toString())
        return id
    }

}