package dev.tieseler.factions.commands

import co.aikar.commands.annotation.CommandAlias
import dev.tieseler.factions.data.FactionPlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("factions|f")
class FactionsEditCommand : FactionsBaseCommand() {

    @CommandAlias("edit|e name")
    fun onEditName(sender: CommandSender, name: String) {
        if (sender !is Player) {
            sender.sendMessage(messages.senderNotPlayer())
            return
        }
        val player = sender // sender is smart-casted to Player

        if (!validateFactionName(name)) {
            player.sendMessage(messages.factionNameInvalid())
            return
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

        factionPlayer.faction!!.name = name
        session!!.persist(factionPlayer.faction)
        player.sendMessage(messages.factionNameChanged())
        close()
    }

    @CommandAlias("edit|e displayname")
    fun onEditDisplayName(sender: CommandSender, displayName: String) {
        if (sender !is Player) {
            sender.sendMessage(messages.senderNotPlayer())
            return
        }
        val player = sender // sender is smart-casted to Player

        if (displayName.length > 20) {
            player.sendMessage(messages.factionDisplayNameToLong())
            return
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

        factionPlayer.faction!!.displayName = displayName
        session!!.persist(factionPlayer.faction)
        player.sendMessage(messages.factionDisplayNameChanged())
        close()
    }

    @CommandAlias("edit|e description")
    fun onEditDescription(sender: CommandSender, description: String) {
        if (sender !is Player) {
            sender.sendMessage(messages.senderNotPlayer())
            return
        }
        val player = sender // sender is smart-casted to Player

        if (description.length > 100) {
            player.sendMessage(messages.factionDescriptionToLong())
            return
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

        factionPlayer.faction!!.description = description
        session!!.persist(factionPlayer.faction)
        player.sendMessage(messages.factionDescriptionChanged())
        close()
    }

    @CommandAlias("edit|e acronym")
    fun onEditAcronym(sender: CommandSender, acronym: String) {
        if (sender !is Player) {
            sender.sendMessage(messages.senderNotPlayer())
            return
        }
        val player = sender // sender is smart-casted to Player

        if (acronym.length > 6) {
            player.sendMessage(messages.factionAcronymToLong())
            return
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

        factionPlayer.faction!!.acronym = acronym
        session!!.persist(factionPlayer.faction)
        player.sendMessage(messages.factionAcronymChanged())
        close()
    }

    private fun validateFactionName(name: String): Boolean {
        return name.matches(Regex("^[a-zA-Z0-9_]{3,16}$"))
    }

}