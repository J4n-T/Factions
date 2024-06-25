package dev.tieseler.factions.listeners

import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityCombustByEntityEvent

class TntMinecartListener : Listener {

    @EventHandler
    fun onTntMinecartExplosionTriggeredByArrow(event: EntityCombustByEntityEvent) {
        if (event.combuster.type != EntityType.MINECART_TNT) return
        if (event.entity.type != EntityType.ARROW) return
        event.isCancelled = true
    }

}