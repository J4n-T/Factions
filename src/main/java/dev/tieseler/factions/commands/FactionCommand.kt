package dev.tieseler.factions.commands

import dev.tieseler.factions.Factions
import dev.tieseler.factions.data.Faction
import dev.tieseler.factions.data.FactionPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.*

class FactionCommand : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val player = sender as? Player ?: return false
        if (args!!.isEmpty()) return false

        val session = Factions.instance.databaseConnector!!.sessionFactory!!.openSession()
        val transaction = session.beginTransaction()

        val factionPlayer = session.get(FactionPlayer::class.java, player.uniqueId)
        if (factionPlayer == null) {
            player.sendMessage(Component.text("§4[Faction] §cDein Profil konnte nicht abgerufen werden :c"))
            session.close()
            return true
        }

        when (args[0]) {
            "create" -> {
                if (factionPlayer.faction != null) {
                    player.sendMessage(Component.text("§4[Faction] Du bist bereits in einer Faction :/"))
                    session.close()
                    return true
                }

                if (args.size < 2) {
                    player.sendMessage(Component.text("§4[Faction] §cWir sind hier nicht bei \"No Game No Life\"! Bitte gib deiner Faction einen Namen (ohne Leerzeichen)!"))
                    session.close()
                    return true
                }

                if (args.size < 3 || (args[2].equals("neutral", true) && args[2].equals("aggressiv", true))) {
                    player.sendMessage(Component.text("§4[Faction] §cBitte entscheide dich dafür, ob deine Faction neutral oder aggressiv sein soll!"))
                    session.close()
                    return true
                }

                if (args.size < 4) {
                    player.sendMessage(Component.text("§4[Faction] §cBitte gib eine Beschreibung für deine Faction ein!"))
                    session.close()
                    return true
                }

                val query = session.createQuery("FROM Faction WHERE name = :name", Faction::class.java)
                query.setParameter("name", args[0])
                val result = query.setMaxResults(1).uniqueResult()
                if (result != null) {
                    player.sendMessage(Component.text("§4[Faction] §cDie Faction §a${args[0]} §cexistiert bereits :/"))
                    session.close()
                    return true
                }

                val faction = Faction()
                faction.id = UUID.randomUUID()
                faction.name = args[1]
                faction.neutral = args[2].equals("neutral", true)
                faction.description = args.sliceArray(3 until args.size).joinToString(" ")
                faction.createdAt = System.currentTimeMillis()
                faction.leader = factionPlayer
                faction.members.add(factionPlayer)

                factionPlayer.faction = faction
                session.persist(faction)
                session.persist(factionPlayer)
                transaction.commit()
                session.close()

                val announcement = Component.text("§4[Announcement] §c${player.name} §ahat sich dazu entschieden die Faction §c${faction.name} zu gründen")
                    .hoverEvent(HoverEvent.showText(Component.text("""
                        §fName: §c${faction.name!!}
                        §fBeschreibung: §c${faction.description!!}
                        §fEinstellung: §c${if (faction.neutral) "Neutral" else "Aggressiv"}
                        §fAnführer: §c${player.name}
                        §fMitglieder: §c${faction.members.size}
                    """.trimIndent())))
                Bukkit.getServer().broadcast(announcement)

                player.sendMessage(Component.text("§4[Faction] §aDeine Faction wurde gegründet!"))
            }
            "disband" -> {
                if (factionPlayer.faction == null) {
                    player.sendMessage(Component.text("§cDu bist in keiner Faction!"))
                    session.close()
                    return true
                }

                val faction = factionPlayer.faction
                if (faction == null) {
                    player.sendMessage(Component.text("§cDie Daten deiner Faction konnten nicht abgerufen werden :c"))
                    session.close()
                    return true
                }

                if (faction.leader != factionPlayer) {
                    player.sendMessage(Component.text("§cDu bist nicht der Anführer der Faction!"))
                    session.close()
                    return true
                }

                session.remove(faction)

                faction.members.forEach { member ->
                    member.faction = null
                    session.persist(member)
                }

                transaction.commit()
                session.close()

                player.sendMessage(Component.text("§4[Faction] §cDeine Faction wurde aufgelöst!"))
                Bukkit.getServer().broadcast(Component.text("§4[Announcement] §cDie Faction §4${faction.name} §cwurde aufgelöst!"))
                return true
            }
            "kick" -> {
                if (args.size < 2) {
                    player.sendMessage(Component.text("§4[Faction] §cDu hast keinen Spieler angegeben :/"))
                    session.close()
                    return true
                }

                if (factionPlayer.faction == null) {
                    player.sendMessage(Component.text("§4[Faction] §cDu bist in keiner Faction!"))
                    session.close()
                    return true
                }

                val target = Bukkit.getPlayer(args[1])
                if (target == null) {
                    player.sendMessage(Component.text("§4[Faction] §cDer Spieler §a${args[1]} §ckonnte nicht gekickt werden, da er nicht existiert oder nicht online ist :("))
                    session.close()
                    return true
                }

                val targetFactionPlayer = session.get(FactionPlayer::class.java, target.uniqueId)
                if (targetFactionPlayer == null) {
                    player.sendMessage(Component.text("§4[Faction] §cDie Daten des Spielers konnten nicht abgerufen werden :c"))
                    session.close()
                    return true
                }

                val faction = factionPlayer.faction
                if (faction == null) {
                    player.sendMessage(Component.text("§4[Faction] §cDie Daten deiner Faction konnten nicht abgerufen werden :c"))
                    session.close()
                    return true
                }

                if (faction.leader != factionPlayer) {
                    player.sendMessage(Component.text("§4[Faction] §cDu bist nicht der Anführer der Faction!"))
                    session.close()
                    return true
                }

                if (targetFactionPlayer.faction != faction) {
                    player.sendMessage(Component.text("§4[Faction] §cDer Spieler §a${target.name} §cist nicht in deiner Faction!"))
                    session.close()
                    return true
                }

                faction.members.remove(targetFactionPlayer)
                targetFactionPlayer.faction = null
                session.persist(faction)
                session.persist(targetFactionPlayer)
                transaction.commit()
                session.close()

                player.sendMessage(Component.text("§4[Faction] §aDer Spieler §c${target.name} §awurde aus deiner Faction gekickt!"))
                target.sendMessage(Component.text("§4[Faction] §cDu wurdest aus der Faction §a${faction.name} §cgekickt!"))
                Bukkit.getServer().broadcast(Component.text("§4[Announcement] §cDer Spieler §4${target.name} §cwurde aus der Faction §4${faction.name} §cgekickt!"))
                return true
            }
        }
        session.close()
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        // List of all subcommands
        val subcommands = mutableListOf("create", "disband", "invite", "kick")
        var completions = subcommands

        // If there are no arguments, suggest all subcommands
        if (args.isEmpty()) {
            return completions
        }

        // If the first argument is a subcommand, suggest completions for that subcommand
        if (subcommands.contains(args[0])) {
            when (args[0]) {
                "create" -> {
                    if (args.size == 3) {
                        completions = mutableListOf("neutral", "aggressiv")
                    } else return mutableListOf()
                }
                "kick" -> {
                    if (args.size == 2) {
                        val session = Factions.instance.databaseConnector!!.sessionFactory!!.openSession()
                        val factionPlayer = session.get(FactionPlayer::class.java, (sender as Player).uniqueId)
                        completions = mutableListOf()
                        factionPlayer.faction!!.members.forEach { member ->
                            completions.add(Bukkit.getOfflinePlayer(member.id!!).name!!)
                            completions.remove(sender.name)
                        }
                        session.close()
                    } else return mutableListOf()
                }
                else -> return mutableListOf()
            }
        }

        // Filter the completions based on the current input
        val iterator = completions.iterator()
        while (iterator.hasNext()) {
            if (!iterator.next().startsWith(args.last())) {
                iterator.remove()
            }
        }

        return completions
    }

}