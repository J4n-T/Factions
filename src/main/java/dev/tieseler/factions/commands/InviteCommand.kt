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

    val messages = Factions.instance.messages

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val player = sender as? Player ?: return false
        if (args.isNullOrEmpty()) {
            player.sendMessage(messages.inviteCommandPlayerNameOrSubCommandRequired())
            return true
        }

        val session = Factions.instance.databaseConnector?.sessionFactory?.openSession()
        val transaction = session!!.beginTransaction()
        val factionPlayer = session.get(FactionPlayer::class.java, player.uniqueId) ?: return true

        when (args[0].lowercase()) {
            "accept" -> {
                if (args.size < 2) {
                    player.sendMessage(messages.factionNameMissing())
                    return true
                }

                if (factionPlayer.faction != null) {
                    player.sendMessage(messages.alreadyInFaction())
                    session.close()
                    return true
                }

                val invite = factionPlayer.invites.stream().filter { it.faction!!.name == args[1] }.findFirst().orElse(null)
                if (invite == null) {
                    player.sendMessage(messages.inviteNotFound())
                    session.close()
                    return true
                }

                val faction = invite.faction
                if (faction == null) {
                    player.sendMessage(messages.failedToFetchFactionData())
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

                player.sendMessage(messages.joinedFaction(faction.name!!))
                Bukkit.getServer().broadcast(Component.text("§4[Announcement] §aDer Spieler §c${player.name} §aist der Faction §c${faction.name} §abeigetreten!"))
                return true

            }
            "deny" -> {
                if (args.size < 2) {
                    player.sendMessage(messages.factionNameMissing())
                    return true
                }

                val invite = factionPlayer.invites.stream().filter { it.faction!!.name == args[1] }.findFirst().orElse(null)
                if (invite == null) {
                    player.sendMessage(messages.inviteNotFound())
                    session.close()
                    return true
                }

                val factionName = invite.faction!!.name!!

                session.remove(invite)
                transaction.commit()
                session.close()

                player.sendMessage(messages.inviteDeclined(factionName))
                return true

            }
            else -> {
                if (factionPlayer.faction == null) {
                    player.sendMessage(messages.playerNotInFaction())
                    session.close()
                    return true
                }

                val targetPlayer = Bukkit.getPlayer(args[0])
                if (targetPlayer == null) {
                    player.sendMessage(messages.inviteFailedPlayerNotFound(args[0]))
                    session.close()
                    return true
                }

                if (targetPlayer.uniqueId == player.uniqueId) {
                    player.sendMessage(messages.inviteFailedCannotInviteYourself())
                    return true
                }

                val targetFactionPlayer = session.get(FactionPlayer::class.java, targetPlayer.uniqueId)
                if (targetFactionPlayer == null) {
                    player.sendMessage(messages.failedToFetchPlayerData(targetPlayer.displayName()))
                    session.close()
                    return true
                }

                val faction = factionPlayer.faction
                if (faction == null) {
                    player.sendMessage(messages.failedToFetchFactionData())
                    session.close()
                    return true
                }

                if (targetFactionPlayer.faction == faction) {
                    player.sendMessage(messages.inviteFailedPlayerAlreadyInYourFaction(targetPlayer.displayName()))
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

                player.sendMessage(messages.invited(targetPlayer.displayName()))

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