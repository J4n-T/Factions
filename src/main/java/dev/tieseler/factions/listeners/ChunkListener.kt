package dev.tieseler.factions.listeners

import dev.tieseler.factions.Factions
import dev.tieseler.factions.data.ChunkData
import dev.tieseler.factions.data.ChunkState
import dev.tieseler.factions.data.Claim
import dev.tieseler.factions.data.FactionPlayer
import dev.tieseler.factions.util.UUIDUtil
import org.bukkit.Chunk
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
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
        val session = Factions.instance.readSession!!
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
        val chunkData = Factions.instance.readSession?.get(ChunkData::class.java, chunkId) ?: return

        if (chunkData.state == ChunkState.WILDERNESS) return

        val faction = chunkData.faction ?: return
        val factionMember = Factions.instance.readSession?.get(FactionPlayer::class.java, event.player.uniqueId) ?: return
        if (factionMember.faction != faction) {
            event.isCancelled = true
        }

        //Fetch claims and check if there are any relevant claims
        val claims = factionMember.role!!.claims
        val relevantClaims = claims.filter { it.claim == Claim.BLOCK_BREAK }
        if (relevantClaims.isEmpty()) {
            event.isCancelled = true
        }

        //Check if there is a specific claim for this chunk (Specific claim overrides general claim) If the specific claim is false, cancel the event
        val specificClaim = relevantClaims.firstOrNull { it.chunkData == chunkData }
        if (specificClaim != null && !specificClaim.value) {
            event.isCancelled = true
        }

        //If there is no specific claim, check if there is a general claim for the role. If the general claim is false, cancel the event
        if (relevantClaims.isNotEmpty() && !relevantClaims.first().value) {
            event.isCancelled = true
        }

        //If there is any relevant claim left, the user is allowed to break the block
    }

    private fun getChunkId(chunk: Chunk): UUID? {
        return UUIDUtil().parse(chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING))
    }

    private fun setChunkId(chunk: Chunk, id: UUID): UUID {
        chunk.persistentDataContainer.set(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING, id.toString())
        return id
    }

}