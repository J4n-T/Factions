package dev.tieseler.factions.listeners

import org.bukkit.NamespacedKey
import org.bukkit.entity.Pig
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDismountEvent
import org.bukkit.persistence.PersistentDataType

class PigListener : Listener {

    @EventHandler
    fun onPigExit(event: EntityDismountEvent) {
        val pig = event.dismounted as? Pig ?: return
        if (pig.persistentDataContainer.has(NamespacedKey.fromString("pepo_sit")!!, PersistentDataType.BYTE)) {
            pig.remove()
            val player = event.entity as? Player ?: return
            player.teleportAsync(player.location.add(0.0, 0.9, 0.0))
        }
    }

    @EventHandler
    fun onPigDamage(event: EntityDamageEvent) {
        val pig = event.entity as? Pig ?: return
        if (pig.persistentDataContainer.has(NamespacedKey.fromString("pepo_sit")!!, PersistentDataType.BYTE)) {
            event.isCancelled = true
        }
    }

}