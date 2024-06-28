package dev.tieseler.factions.commands

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import dev.tieseler.factions.data.ChunkData
import dev.tieseler.factions.data.ChunkState
import dev.tieseler.factions.data.FactionPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("factions|f")
class FactionsChunkCommand : FactionsBaseCommand() {

    @CommandAlias("fci")
    @Subcommand("chunk|c info|i")
    fun onChunkId(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(messages.senderNotPlayer())
            return
        }
        val player = sender // sender is smart-casted to Player

        val chunk = sender.location.chunk
        val chunkId = parseUUIDFromPersistentDataContainer(chunk.persistentDataContainer, "chunk_id")
        if (chunkId == null) {
            sender.sendMessage(messages.failedToFetchChunkData())
            return
        }

        init(true, true, false)
        val chunkData = session!!.get(ChunkData::class.java, chunkId)
        if (chunkData == null) {
            sender.sendMessage(messages.failedToFetchChunkData())
            return close()
        }

        val faction = chunkData.faction
        player.sendMessage(messages.chunkInfo(chunk.x, chunk.z, chunkData.id, faction?.name() ?: Component.text("Wilderness").color(NamedTextColor.GREEN)))
        close()
    }

    @CommandAlias("fcc")
    @Subcommand("chunk|c claim|c")
    fun onChunkClaim(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(messages.senderNotPlayer())
            return
        }
        val player = sender // sender is smart-casted to Player

        val chunk = sender.location.chunk
        val chunkId = parseUUIDFromPersistentDataContainer(chunk.persistentDataContainer, "chunk_id")
        if (chunkId == null) {
            sender.sendMessage(messages.failedToFetchChunkData())
            return
        }

        init(true, true, true)
        val chunkData = session!!.get(ChunkData::class.java, chunkId)
        if (chunkData == null) {
            sender.sendMessage(messages.failedToFetchChunkData())
            return close()
        }

        val factionPlayer = session!!.get(FactionPlayer::class.java, player.uniqueId)
        if (factionPlayer?.faction == null) {
            sender.sendMessage(messages.playerNotInFaction())
            return close()
        }

        if (chunkData.isClaimed()) {
            sender.sendMessage(messages.chunkAlreadyClaimed())
            return close()
        }

        if (factionPlayer.faction?.leader != factionPlayer) {
            sender.sendMessage(messages.playerNotFactionLeader())
            return close()
        }

        chunkData.state = ChunkState.CLAIMED
        chunkData.faction = factionPlayer.faction
        session!!.persist(chunkData)
        sender.sendMessage(messages.chunkClaimedMessage(chunk))
        close()
    }

    @CommandAlias("fcuc")
    @Subcommand("chunk|c unclaim|uc")
    fun onChunkUnclaim(sender: CommandSender) {
        if (sender !is Player) {
            sender.sendMessage(messages.senderNotPlayer())
            return
        }
        val player = sender // sender is smart-casted to Player

        val chunk = sender.location.chunk
        val chunkId = parseUUIDFromPersistentDataContainer(chunk.persistentDataContainer, "chunk_id")
        if (chunkId == null) {
            sender.sendMessage(messages.failedToFetchChunkData())
            return
        }

        init(true, true, true)
        val chunkData = session!!.get(ChunkData::class.java, chunkId)
        if (chunkData == null) {
            sender.sendMessage(messages.failedToFetchChunkData())
            return close()
        }

        val factionPlayer = session!!.get(FactionPlayer::class.java, player.uniqueId)
        if (factionPlayer?.faction == null) {
            sender.sendMessage(messages.playerNotInFaction())
            return close()
        }

        if (!chunkData.isClaimed()) {
            sender.sendMessage(messages.chunkNotClaimed())
            return close()
        }

        if (chunkData.faction != factionPlayer.faction) {
            sender.sendMessage(messages.chunkNotClaimedByPlayersFaction())
            return close()
        }

        if (factionPlayer.faction?.leader != factionPlayer) {
            sender.sendMessage(messages.playerNotFactionLeader())
            return close()
        }

        chunkData.state = ChunkState.WILDERNESS
        chunkData.faction = null
        session!!.persist(chunkData)
        sender.sendMessage(messages.chunkUnclaimed())
        close()
    }

}