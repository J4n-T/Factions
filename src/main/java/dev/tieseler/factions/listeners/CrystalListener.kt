package dev.tieseler.factions.listeners

import org.bukkit.NamespacedKey
import org.bukkit.entity.EnderCrystal
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityPlaceEvent
import org.bukkit.persistence.PersistentDataType

class CrystalListener : Listener {

    @EventHandler
    fun onEntitySpawningEntity(event: EntityPlaceEvent) {
        if (event.entity !is EnderCrystal) return
        if (event.player == null) return
        event.entity.persistentDataContainer.set(NamespacedKey.fromString("pvp")!!, PersistentDataType.BOOLEAN, false)
    }

    @EventHandler
    fun onCrystalDamage(event: EntityDamageByEntityEvent) {
        if (event.damager !is EnderCrystal) return
        if (event.entity !is Player) return
        if (event.damager.persistentDataContainer.get(NamespacedKey.fromString("pvp")!!, PersistentDataType.BOOLEAN) == false) {
            event.isCancelled = true
        }
    }

}