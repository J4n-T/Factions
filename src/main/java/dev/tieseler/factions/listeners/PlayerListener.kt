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
        val factionPlayer = session.get(FactionPlayer::class.java, event.player.uniqueId)
        if (factionPlayer == null) {
            val newFactionPlayer = FactionPlayer()
            newFactionPlayer.id = event.player.uniqueId
            session.persist(newFactionPlayer)
            transaction.commit()
        }
        session.close()

        event.joinMessage(Component.text("§7[§a+§7] Willkommen zurück, §a").append(event.player.displayName()))
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        event.quitMessage(Component.text("§7[§c-§7] Auf Wiedersehen, §c").append(event.player.displayName()))
    }

}