package dev.tieseler.factions.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import dev.tieseler.factions.data.FactionPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("factions|f")
class FactionsLeaveCommand : FactionsBaseCommand() {

    @Subcommand("leave")
    fun onLeave(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(messages.senderNotPlayer())
            return
        }
        val player = sender // sender is smart-casted to Player

        init(true, true, true)
        val factionPlayer = session!!.get(FactionPlayer::class.java, player.uniqueId) ?: return close()
        val faction = factionPlayer.faction
        if (faction == null) {
            player.sendMessage(messages.playerNotInFaction())
            return close()
        }

        if (faction.leader?.id == player.uniqueId) {
            player.sendMessage(messages.factionLeaderCannotLeave())
            return close()
        }

        factionPlayer.faction = null
        factionPlayer.role = null
        session!!.persist(factionPlayer)
        player.sendMessage(messages.factionLeft())
        close()
    }

}