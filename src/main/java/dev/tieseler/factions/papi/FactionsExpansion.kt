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
        val session = Factions.instance.databaseConnector?.sessionFactory?.openSession() ?: return ""
        val factionPlayer = session.get(FactionPlayer::class.java, player!!.uniqueId) ?: return ""
        val faction = factionPlayer.faction ?: return ""
        val placeholder = params.lowercase()

        return when (placeholder) {
            "name" -> {
                faction.name
            }

            "description" -> {
                faction.description
            }

            "displayname" -> {
                faction.displayName
            }

            "acronym" -> {
                faction.acronym
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