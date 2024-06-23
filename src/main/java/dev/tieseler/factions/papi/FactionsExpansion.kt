package dev.tieseler.factions.papi

import dev.tieseler.factions.Factions
import dev.tieseler.factions.data.FactionPlayer
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class FactionsExpansion : PlaceholderExpansion() {

    override fun getIdentifier(): String {
        return "factions"
    }

    override fun getAuthor(): String {
        return Factions.instance.pluginMeta.authors.joinToString(",")
    }

    override fun getVersion(): String {
        return Factions.instance.pluginMeta.version
    }

    override fun onPlaceholderRequest(player: Player?, params: String): String? {
        val session = Factions.instance.readSession ?: return ""
        val placeholder = params.lowercase()
        val factionPlayer = session.get(FactionPlayer::class.java, player!!.uniqueId)
        val faction = factionPlayer.faction ?: return ""

        return when (placeholder) {
            "name" -> {
                faction.name
            }

            "description" -> {
                faction.description
            }

            "mode" -> {
                if (faction.neutral) "§a*" else "§c*"
            }

            else -> {
                ""
            }
        }
    }

}