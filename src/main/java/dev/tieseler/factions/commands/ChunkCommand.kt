package dev.tieseler.factions.commands

import dev.tieseler.factions.Factions
import dev.tieseler.factions.data.ChunkData
import dev.tieseler.factions.data.ChunkState
import dev.tieseler.factions.data.FactionPlayer
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import java.util.*

class ChunkCommand : CommandExecutor, TabCompleter {

    val messages = Factions.instance.messages

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val player = sender as? Player ?: return false

        if (args!!.isEmpty()) return false
        when (args[0]) {
            "id" -> {
                player.sendMessage("Your chunk id is: ${player.location.chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING)}")
                return true
            }
            "info" -> {
                val session = Factions.instance.readSession
                val chunk = player.location.chunk
                val chunkId = UUID.fromString(chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING))
                val chunkData = session!!.get(ChunkData::class.java, chunkId)
                if (chunkData == null) {
                    player.sendMessage(messages.failedToFetchChunkData())
                    return true
                }

                val message = """
                    §4[Faction] §cChunk Info:§f
                    §4[Faction] §cX/Z >> §a${chunk.x}/${chunk.z}
                    §4[Faction] §cID >> §a${chunkData.id}
                    §4[Faction] §cClaim >> §c${if (chunkData.state == ChunkState.CLAIMED) chunkData.faction!!.name else "§aWildness"}
                """.trimIndent()
                player.sendMessage(message)
                return true
            }
            "claim" -> {
                val session = Factions.instance.databaseConnector!!.sessionFactory!!.openSession()
                val transaction = session.beginTransaction()
                val factionPlayer = session.get(FactionPlayer::class.java, player.uniqueId)
                if (factionPlayer?.faction == null) {
                    player.sendMessage(messages.playerNotInFaction())
                    return true
                }

                val chunk = player.location.chunk
                val chunkId = UUID.fromString(chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING))
                var chunkData = session.get(ChunkData::class.java, chunkId)
                if (chunkData?.faction != null) {
                    player.sendMessage(messages.chunkAlreadyClaimed())
                    return true
                }

                chunkData = ChunkData()
                chunkData.id = chunkId
                chunkData.faction = factionPlayer.faction
                chunkData.state = ChunkState.CLAIMED
                chunkData.x = chunk.x
                chunkData.z = chunk.z

                session.persist(chunkData)
                transaction.commit()

                player.sendMessage("§4[Faction] §aDu hast den Chunk ${chunkData.x}/${chunkData.z} (${chunkId}) geclaimt")
                return true
            }
        }

        TODO("Not yet implemented")
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        // List of all subcommands
        val subcommands = mutableListOf("id", "info", "claim")

        // If there are no arguments, suggest all subcommands
        if (args.isEmpty()) {
            return subcommands
        }

        // If the first argument is a subcommand, suggest completions for that subcommand
        /*if (subcommands.contains(args[0])) {
            when (args[0]) {
                "id" -> {
                    Bukkit.getOnlinePlayers().forEach { player ->
                        completions.add(player.name)
                    }
                }
                "subcommand2" -> {
                    // Add completions for subcommand2
                }
                "subcommand3" -> {
                    // Add completions for subcommand3
                }
            }
        }*/

        // Filter the completions based on the current input
        val iterator = subcommands.iterator()
        while (iterator.hasNext()) {
            if (!iterator.next().startsWith(args.last())) {
                iterator.remove()
            }
        }

        return subcommands
    }

}