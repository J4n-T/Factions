package dev.tieseler.factions.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import dev.tieseler.factions.data.FactionPlayer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("factions|f")
class FactionsDisbandCommand : FactionsBaseCommand() {

    @Subcommand("disband")
    fun onDisband(sender: CommandSender) {
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

        if (faction.leader?.id != player.uniqueId) {
            player.sendMessage(messages.playerNotFactionLeader())
            return close()
        }

        faction.members.forEach { member ->
            member.faction = null
            member.role = null
            session!!.persist(member)
        }

        session!!.remove(faction)
        player.sendMessage(messages.factionDisbanded())
        Bukkit.getServer().broadcast(Component.text("§4[Announcement] §cDie Faction §4${faction.name} §cwurde aufgelöst!"))
        close()
    }

}