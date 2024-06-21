package dev.tieseler.factions.listeners

import dev.tieseler.factions.Factions
import dev.tieseler.factions.data.ChunkData
import dev.tieseler.factions.data.ChunkState
import dev.tieseler.factions.data.Claim
import dev.tieseler.factions.data.FactionPlayer
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkPopulateEvent
import org.bukkit.persistence.PersistentDataType
import java.util.*

class ChunkListener : Listener {

    @EventHandler
    fun onChunkGeneration(event: ChunkPopulateEvent) {
        var chunkId = event.chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING)
        if (chunkId == null) {
            chunkId = UUID.randomUUID().toString()
            event.chunk.persistentDataContainer.set(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING, chunkId)
        }
    }

    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        val chunkId = event.chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING)
        if (chunkId == null) {
            event.chunk.persistentDataContainer.set(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING, UUID.randomUUID().toString())
        }
    }

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val chunkId = event.block.chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING)
        if (chunkId == null) {
            event.block.chunk.persistentDataContainer.set(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING, UUID.randomUUID().toString())
            return
        }

        val chunkData = Factions.instance.readSession?.get(ChunkData::class.java, UUID.fromString(chunkId))
        if (chunkData == null) {
            event.block.chunk.persistentDataContainer.set(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING, UUID.randomUUID().toString())
            return
        }

        if (chunkData.state == ChunkState.WILDERNESS) {
            return
        }

        val faction = chunkData.faction
        val factionMember = Factions.instance.readSession?.get(FactionPlayer::class.java, event.player.uniqueId)
        if (factionMember == null || factionMember.faction != faction) {
            event.isCancelled = true
        }

        //Fetch claims and check if there are any relevant claims
        val claims = factionMember!!.role!!.claims
        val relevantClaims = claims.filter { it.claim == Claim.BLOCK_BREAK }
        if (relevantClaims.isEmpty()) {
            event.isCancelled = true
        }

        //Check if there is a specific claim for this chunk (Specific claim overrides general claim) If the specific claim is false, cancel the event
        val specificClaim = relevantClaims.filter { it.chunkData == chunkData }.firstOrNull()
        if (specificClaim != null && !specificClaim.value) {
            event.isCancelled = true
        }

        //If there is no specific claim, check if there is a general claim for the role. If the general claim is false, cancel the event
        if (relevantClaims.isNotEmpty() && !relevantClaims.first().value) {
            event.isCancelled = true
        }

        //If there is any relevant claim left, the user is allowed to break the block
    }


}