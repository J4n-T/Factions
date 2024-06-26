package dev.tieseler.factions.util

import dev.tieseler.factions.data.*
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType

class PermissionUtil {

    fun isPermitted(player: FactionPlayer?, chunk: ChunkData?, claim: Claim): Boolean {
        if (player == null) return false
        if (chunk == null) return false

        if (Bukkit.getPlayer(player.id!!)!!.persistentDataContainer.has(NamespacedKey.fromString("bypass")!!, PersistentDataType.BYTE)) return true

        //Chunk is unclaimed so everyone can do whatever they want
        if (chunk.state == ChunkState.WILDERNESS) return true
        if (chunk.faction == null) return true
        val chunkFaction = chunk.faction!!

        //Player is leader of the faction, so he can do whatever he wants
        if (chunkFaction.leader == player) return true

        //Player is not in the same faction as the chunk, so he can't do anything
        if (player.faction != chunkFaction) return false

        //Player has no role or no claims, so he can't do anything
        val role = player.role ?: return false
        val claims = role.claims
        val relevantClaims = claims.filter { it.claim == claim }
        if (relevantClaims.isEmpty()) return false


        //Check if there is a specific claim for this chunk (Specific claim overrides general claim) If the specific claim is false, cancel the event
        val specificClaim = relevantClaims.firstOrNull { it.chunkData == chunk }
        if (specificClaim == null) return false
        if (!specificClaim.value) return false


        //If there is no specific claim, check if there is a general claim for the role. If the general claim is false, cancel the event
        if (relevantClaims.isEmpty()) return false
        return relevantClaims.first().value
    }

}