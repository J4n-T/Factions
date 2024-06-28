package dev.tieseler.factions.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Subcommand
import dev.tieseler.factions.data.FactionPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("factions|f")
class FactionsKickCommand : FactionsBaseCommand() {

    @Subcommand("kick")
    @CommandCompletion("@factionsMembers")
    fun onKick(sender: CommandSender, vararg args: String) {
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

        val target = Bukkit.getPlayer(args[0])
        if (target == null) {
            player.sendMessage(messages.failedToFetchPlayerData(Component.text(args[0])))
            return close()
        }

        if (target == player) {
            player.sendMessage(messages.cannotKickYourself())
            return close()
        }

        val targetFactionPlayer = session!!.get(FactionPlayer::class.java, target.uniqueId)
        if (targetFactionPlayer == null) {
            player.sendMessage(messages.failedToFetchPlayerData(target.displayName()))
            return close()
        }

        val faction = factionPlayer.faction
        if (faction == null) {
            player.sendMessage(messages.failedToFetchFactionData())
            return close()
        }

        if (faction.leader != factionPlayer) {
            player.sendMessage(messages.playerNotFactionLeader())
            return close()
        }

        if (targetFactionPlayer.faction != faction) {
            player.sendMessage(messages.targetNotSameFaction(target.displayName()))
            return close()
        }

        faction.members.remove(targetFactionPlayer)
        targetFactionPlayer.faction = null
        targetFactionPlayer.role = null

        player.sendMessage(Component.text("§4[Faction] §aDer Spieler §c${target.name} §awurde aus deiner Faction gekickt!"))
        target.sendMessage(Component.text("§4[Faction] §cDu wurdest aus der Faction §a${faction.name} §cgekickt!"))
        Bukkit.getServer().broadcast(Component.text("§4[Announcement] §cDer Spieler §4${target.name} §cwurde aus der Faction §4${faction.name} §cgekickt!"))
        close()
    }

}