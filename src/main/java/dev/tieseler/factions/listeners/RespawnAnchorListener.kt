package dev.tieseler.factions.listeners

import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByBlockEvent

class RespawnAnchorListener : Listener {

    @EventHandler
    fun onRespawnAnchorExplode(event: EntityDamageByBlockEvent) {
        val block = event.damagerBlockState?.type ?: event.damager?.type
        if (block != Material.RESPAWN_ANCHOR) return
        event.isCancelled = true
    }

}