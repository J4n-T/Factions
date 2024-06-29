package dev.tieseler.factions.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import dev.tieseler.factions.data.Faction
import dev.tieseler.factions.data.FactionPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

@CommandAlias("factions|f")
class FactionsCreateCommand : FactionsBaseCommand() {

    @Subcommand("create")
    fun onCreate(sender: CommandSender, vararg args: String) {
        // /f create <name> <displayname> <alias> <aggressive|neutral> <description>

        if (sender !is Player) {
            sender.sendMessage(messages.senderNotPlayer())
            return
        }
        val player = sender // sender is smart-casted to Player

        if (args.isEmpty()) {
            player.sendMessage(messages.missingFactionName())
            return
        }

        if (args.size < 2) {
            player.sendMessage(messages.missingFactionDisplayName())
            return
        }

        if (args.size < 3) {
            player.sendMessage(messages.missingFactionAcronym())
            return
        }

        if (args.size < 4) {
            player.sendMessage(messages.selectFactionMode())
            return
        }

        if (args.size < 5) {
            player.sendMessage(messages.missingFactionDescription())
            return
        }

        if (!validateFactionName(args[0])) {
            player.sendMessage(messages.factionNameInvalid())
            return
        }

        if (args[1].length > 20) {
            player.sendMessage(messages.factionDisplayNameToLong())
            return
        }

        if (args[2].length > 6) {
            player.sendMessage(messages.factionAcronymToLong())
            return
        }

        if (args[3] != "neutral" && args[3] != "aggressive") {
            player.sendMessage(messages.selectFactionMode())
            return
        }

        val name = args[0]
        val displayName = args[1]
        val acronym = args[2]
        val neutral = args[3] == "neutral"
        val description = args.sliceArray(4 until args.size).joinToString(" ")
        if (description.length > 100) {
            player.sendMessage(messages.factionDescriptionToLong())
            return
        }

        init(true, true, true)
        val factionPlayer = session?.get(FactionPlayer::class.java, player.uniqueId) ?: return close()
        if (factionPlayer.faction != null) {
            player.sendMessage(messages.alreadyInFaction())
            return close()
        }

        val query = session?.createQuery("FROM Faction WHERE name = :name", Faction::class.java) ?: return close()
        query.setParameter("name", args[1])
        val result = query.setMaxResults(1).uniqueResult()
        if (result != null) {
            player.sendMessage(messages.factionAlreadyExists(result.name!!))
            return close()
        }

        val faction = Faction()
        faction.id = UUID.randomUUID()
        faction.name = name
        faction.displayName = displayName
        faction.acronym = acronym
        faction.neutral = neutral
        faction.description = description
        faction.createdAt = System.currentTimeMillis()
        faction.leader = factionPlayer
        faction.members.add(factionPlayer)

        factionPlayer.faction = faction
        session!!.persist(faction)
        session!!.persist(factionPlayer)

        val announcement = Component.text("§4[Announcement] §c${player.name} §ahat sich dazu entschieden die Faction §c${faction.name} §azu gründen")
            .hoverEvent(
                HoverEvent.showText(
                    Component.text("""
                        §fName: §c${faction.name!!}
                        §fBeschreibung: §c${faction.description!!}
                        §fEinstellung: §c${if (faction.neutral) "Neutral" else "Aggressiv"}
                        §fAnführer: §c${player.name}
                        §fMitglieder: §c${faction.members.size}
                    """.trimIndent())))
        Bukkit.getServer().broadcast(announcement)

        player.sendMessage(messages.factionCreated())
        close()
    }

    private fun validateFactionName(name: String): Boolean {
        return name.matches(Regex("^[a-zA-Z0-9_]{3,16}$"))
    }

}