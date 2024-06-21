package dev.tieseler.factions.commands

import dev.tieseler.factions.Factions
import dev.tieseler.factions.data.FactionInvite
import dev.tieseler.factions.data.FactionPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.*

class InviteCommand : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val player = sender as? Player ?: return false
        if (args.isNullOrEmpty()) {
            player.sendMessage("§4[Factions] §cBitte gib einen Spieler oder einen Unterbefehl ein an.")
            return true
        }

        val session = Factions.instance.databaseConnector?.sessionFactory?.openSession()
        val transaction = session!!.beginTransaction()
        val factionPlayer = session.get(FactionPlayer::class.java, player.uniqueId) ?: return true

        when (args[0].lowercase()) {
            "accept" -> {
                if (args.size < 2) {
                    player.sendMessage("§4[Factions] §cBitte gib den Namen der Faction an.")
                    return true
                }

                if (factionPlayer.faction != null) {
                    player.sendMessage(Component.text("§4[Faction] §cDu bist bereits in einer Faction!"))
                    session.close()
                    return true
                }

                val invite = factionPlayer.invites.stream().filter { it.faction!!.name == args[2] }.findFirst().orElse(null)
                if (invite == null) {
                    player.sendMessage(Component.text("§4[Faction] §cDie Einladung konnte nicht gefunden werden :/"))
                    session.close()
                    return true
                }

                val faction = invite.faction
                if (faction == null) {
                    player.sendMessage(Component.text("§4[Faction] §cDie Daten der Faction konnten nicht abgerufen werden :c"))
                    session.close()
                    return true
                }

                faction.members.add(factionPlayer)
                factionPlayer.faction = faction

                session.persist(faction)
                session.persist(factionPlayer)
                session.remove(invite)
                transaction.commit()
                session.close()

                player.sendMessage(Component.text("§4[Faction] §aDu bist der Faction §c${faction.name} §abeigetreten!"))
                Bukkit.getServer().broadcast(Component.text("§4[Announcement] §aDer Spieler §c${player.name} §aist der Faction §c${faction.name} §abeigetreten!"))
                return true

            }
            "deny" -> {
                if (args.size < 2) {
                    player.sendMessage("§4[Factions] §cBitte gib den Namen der Faction an.")
                    return true
                }

                val invite = factionPlayer.invites.stream().filter { it.faction!!.name == args[2] }.findFirst().orElse(null)
                if (invite == null) {
                    player.sendMessage(Component.text("§4[Faction] §cDie Einladung konnte nicht gefunden werden :/"))
                    session.close()
                    return true
                }

                session.remove(invite)
                transaction.commit()
                session.close()

                val sendersName = Bukkit.getOfflinePlayer(invite.sender!!.id!!).name
                player.sendMessage(Component.text("§4[Faction] §aDu hast die Einladung von §c$sendersName §c(${invite.faction!!.name!!}) §aabgelehnt!"))
                return true

            }
            else -> {
                if (factionPlayer.faction == null) {
                    player.sendMessage(Component.text("§4[Faction] §cUm einen anderen Spieler einladen zu können, müsstest du selbst in einer Faction sein :P"))
                    session.close()
                    return true
                }

                val targetPlayer = Bukkit.getPlayer(args[1])
                if (targetPlayer == null) {
                    player.sendMessage(Component.text("§4[Faction] §cDer Spieler §a${args[1]} §ckonnte nicht eingeladen werden, da er nicht existiert oder nicht online ist :("))
                    session.close()
                    return true
                }

                if (targetPlayer.uniqueId == player.uniqueId) {
                    player.sendMessage(Component.text("§4[Faction] §cDu bist absolut einzigartig :3! Dich kann es nicht 2x in einer Faction geben <3"))
                    return true
                }

                val targetFactionPlayer = session.get(FactionPlayer::class.java, targetPlayer.uniqueId)
                if (targetFactionPlayer == null) {
                    player.sendMessage(Component.text("§4[Faction] Die Daten des Spielers konnten nicht abgerufen werden :c"))
                    session.close()
                    return true
                }

                val faction = factionPlayer.faction
                if (faction == null) {
                    player.sendMessage(Component.text("§4[Faction] Die Daten deiner Faction konnten nicht abgerufen werden :c"))
                    session.close()
                    return true
                }

                val invite = FactionInvite()
                invite.id = UUID.randomUUID()
                invite.faction = faction
                invite.sender = factionPlayer
                invite.target = targetFactionPlayer
                invite.createdAt = System.currentTimeMillis()
                if (args.size > 2) invite.message = args.sliceArray(2 until args.size).joinToString(" ")

                session.persist(invite)
                transaction.commit()
                session.close()

                player.sendMessage(Component.text("§4[Faction] §aDu hast §c${targetPlayer.name} §ain deine Faction eingeladen"))

                val targetMessage = Component.text("§4[Faction] §aDu wurdest von §c${player.name} §ain die Faction §c${faction.name} §aeingeladen! ")
                    .append(Component.text("§aAnnehmen §7| ").clickEvent(ClickEvent.runCommand("/invite accept ${faction.name}")))
                    .append(Component.text("§cAblehnen").clickEvent(ClickEvent.runCommand("/invite deny ${faction.name}")))
                targetPlayer.sendMessage(targetMessage)
                return true
            }
        }
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        // List of all subcommands
        val subcommands = mutableListOf("accept", "deny")
        subcommands.addAll(Bukkit.getOnlinePlayers().map { it.name })
        var completions = subcommands

        // If there are no arguments, suggest all subcommands
        if (args.isEmpty()) {
            return completions
        }

        // If the first argument is a subcommand, suggest completions for that subcommand
        if (subcommands.contains(args[0]) && args[0] == "accept" || args[0] == "deny") {
            val session = Factions.instance.databaseConnector!!.sessionFactory!!.openSession()
            val factionPlayer = session.get(FactionPlayer::class.java, (sender as Player).uniqueId)
            completions = factionPlayer.invites.stream().map { it.faction!!.name!! }.toList().toMutableList()
            session.close()
        }

        if (args.size >= 2) {
            return mutableListOf()
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