package dev.tieseler.factions.listeners

import dev.tieseler.factions.Factions
import io.papermc.paper.event.player.AsyncChatEvent
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ChatListener : Listener {

    private val chatFormat = Factions.instance.config.getString("chatFormat")

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        if (chatFormat == null) return
        event.isCancelled = true
        val format = PlaceholderAPI.setPlaceholders(event.player, chatFormat)
        Bukkit.getServer().broadcast(Component.text(format).append(event.message()))
    }

}