package dev.tieseler.factions.listeners

import dev.tieseler.factions.Factions
import dev.tieseler.factions.data.FactionPlayer
import net.kyori.adventure.text.Component
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class PlayerListener : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val session = Factions.instance.databaseConnector!!.sessionFactory!!.openSession()
        val transaction = session.beginTransaction()
        var factionPlayer = session.get(FactionPlayer::class.java, event.player.uniqueId)
        if (factionPlayer == null) {
            factionPlayer = FactionPlayer()
            factionPlayer.id = event.player.uniqueId
            session.persist(factionPlayer)
            transaction.commit()
        }

        event.joinMessage(Component.text("§7[§a+§7] Willkommen zurück, §a").append(event.player.displayName()))

        val faction = factionPlayer.faction
        if (faction == null) {
            session.close()
            return
        }

        if (faction.motd != null) {
            event.player.sendMessage(Component.text("§7[${if (faction.displayName == null) faction.name!! else faction.displayName!!}§7] ${faction.motd}"))
        }

        session.close()
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.quitMessage(Component.text("§7[§c-§7] Auf Wiedersehen, §c").append(event.player.displayName()))
    }

}