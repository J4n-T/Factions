package dev.tieseler.factions.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import dev.tieseler.factions.data.FactionPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("factions|f")
class FactionsMotdCommand : FactionsBaseCommand() {

    @Subcommand("motd")
    fun onMotd(sender: CommandSender, vararg args: String) {
        if (sender !is Player) {
            sender.sendMessage(messages.senderNotPlayer())
            return
        }
        val player = sender // sender is smart-casted to Player

        if (args.isEmpty()) {
            init(true, true, false)
            val factionPlayer = session!!.get(FactionPlayer::class.java, player.uniqueId) ?: return close()
            if (factionPlayer.faction?.motd == null) {
                player.sendMessage(messages.noMotd())
                return close()
            } else {
                player.sendMessage(messages.motd(factionPlayer.faction!!.name(), factionPlayer.faction!!.motd()))
                return close()
            }
        }

        val motd = args.joinToString(" ")
        if (motd.length > 100) {
            player.sendMessage(messages.motdTooLong())
            return close()
        }

        init(true, true, true)
        val factionPlayer = session!!.get(FactionPlayer::class.java, player.uniqueId) ?: return close()
        if (factionPlayer.faction == null) {
            player.sendMessage(messages.playerNotInFaction())
            return close()
        }

        if (factionPlayer.faction!!.leader?.id != player.uniqueId) {
            player.sendMessage(messages.playerNotFactionLeader())
            return close()
        }

        factionPlayer.faction!!.motd = motd
        session!!.persist(factionPlayer.faction)
        player.sendMessage(messages.motdChanged())
        close()
    }

}