package dev.tieseler.factions.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Subcommand
import dev.tieseler.factions.data.FactionInvite
import dev.tieseler.factions.data.FactionPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

@CommandAlias("factions|f")
class FactionsInviteCommand : FactionsBaseCommand() {

    @CommandAlias("fi")
    @Subcommand("invite|i")
    @CommandCompletion("@players")
    fun onInvite(sender: CommandSender, vararg args: String) {
        if (args.isEmpty()) {
            sender.sendMessage(messages.missingPlayerName())
            return
        }

        if (sender !is Player) {
            sender.sendMessage(messages.senderNotPlayer())
            return
        }
        val player = sender // sender is smart-casted to Player

        init(true, true, true)
        val factionPlayer = session!!.get(FactionPlayer::class.java, player.uniqueId) ?: return close()
        if (factionPlayer.faction == null) {
            player.sendMessage(messages.playerNotInFaction())
            return close()
        }

        val targetPlayer = Bukkit.getPlayer(args[0])
        if (targetPlayer == null) {
            player.sendMessage(messages.inviteFailedPlayerNotFound(args[0]))
            return close()
        }

        if (targetPlayer.uniqueId == player.uniqueId) {
            player.sendMessage(messages.inviteFailedCannotInviteYourself())
            return close()
        }

        val targetFactionPlayer = session!!.get(FactionPlayer::class.java, targetPlayer.uniqueId)
        if (targetFactionPlayer == null) {
            player.sendMessage(messages.failedToFetchPlayerData(targetPlayer.displayName()))
            return close()
        }

        val faction = factionPlayer.faction
        if (faction == null) {
            player.sendMessage(messages.failedToFetchFactionData())
            return close()
        }

        if (targetFactionPlayer.faction == faction) {
            player.sendMessage(messages.inviteFailedPlayerAlreadyInYourFaction(targetPlayer.displayName()))
            close()
        }

        val invite = FactionInvite()
        invite.id = UUID.randomUUID()
        invite.faction = faction
        invite.sender = factionPlayer
        invite.target = targetFactionPlayer
        invite.createdAt = System.currentTimeMillis()
        if (args.size > 2) invite.message = args.sliceArray(2 until args.size).joinToString(" ")

        session!!.persist(invite)
        player.sendMessage(messages.invited(targetPlayer.displayName()))

        val targetMessage = Component.text("§4[Faction] §aDu wurdest von §c${player.name} §ain die Faction §c${faction.name} §aeingeladen! ")
            .append(Component.text("§aAnnehmen §7| ").clickEvent(ClickEvent.runCommand("/fia ${faction.name}")))
            .append(Component.text("§cAblehnen").clickEvent(ClickEvent.runCommand("/fid ${faction.name}")))
        targetPlayer.sendMessage(targetMessage)
        return close()
    }

    @CommandAlias("fia")
    @Subcommand("accept|a")
    @CommandCompletion("@factionsInvites")
    fun onAccept(sender: CommandSender, vararg args: String) {
        if (args.isEmpty()) {
            sender.sendMessage(messages.missingFactionName())
            return
        }

        if (sender !is Player) {
            sender.sendMessage(messages.senderNotPlayer())
            return
        }
        val player = sender // sender is smart-casted to Player

        init(true, true, true)
        val factionPlayer = session!!.get(FactionPlayer::class.java, player.uniqueId) ?: return close()
        if (factionPlayer.faction != null) {
            player.sendMessage(messages.inviteFailedAlreadyInFaction())
            return close()
        }

        val invite = session!!.createQuery("FROM FactionInvite WHERE target = :target", FactionInvite::class.java)
            .setParameter("target", factionPlayer)
            .uniqueResult()

        if (invite == null) {
            player.sendMessage(messages.inviteNotFound())
            return close()
        }

        factionPlayer.faction = invite.faction
        session!!.remove(invite)
        player.sendMessage(messages.joinedFaction(invite.faction!!.name()))
        return close()
    }

    @CommandAlias("fid")
    @Subcommand("decline|d")
    @CommandCompletion("@factionsInvites")
    fun onDecline(sender: CommandSender, vararg args: String) {
        if (args.isEmpty()) {
            sender.sendMessage(messages.missingFactionName())
            return
        }

        if (sender !is Player) {
            sender.sendMessage(messages.senderNotPlayer())
            return
        }
        val player = sender // sender is smart-casted to Player

        init(true, true, true)
        val factionPlayer = session!!.get(FactionPlayer::class.java, player.uniqueId) ?: return close()
        if (factionPlayer.faction != null) {
            player.sendMessage(messages.inviteFailedAlreadyInFaction())
            return close()
        }

        val invite = session!!.createQuery("FROM FactionInvite WHERE target = :target", FactionInvite::class.java)
            .setParameter("target", factionPlayer)
            .uniqueResult()

        if (invite == null) {
            player.sendMessage(messages.inviteNotFound())
            return close()
        }

        session!!.remove(invite)
        player.sendMessage(messages.inviteDeclined(invite.faction!!.name()))
        return close()
    }

}