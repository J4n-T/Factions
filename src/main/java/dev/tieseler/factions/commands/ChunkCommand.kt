package dev.tieseler.factions.commands

import dev.tieseler.factions.Factions
import dev.tieseler.factions.data.ChunkData
import dev.tieseler.factions.data.ChunkState
import dev.tieseler.factions.data.FactionPlayer
import dev.tieseler.factions.util.UUIDUtil
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
        return when (args[0]) {
            "id" -> {
                player.sendMessage("Your chunk id is: ${player.location.chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING)}")
                true
            }
            "info" -> {
                val session = Factions.instance.databaseConnector!!.sessionFactory!!.openSession()
                val chunk = player.location.chunk
                val chunkId = UUID.fromString(chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING))
                val chunkData = session!!.get(ChunkData::class.java, chunkId)
                if (chunkData == null) {
                    player.sendMessage(messages.failedToFetchChunkData())
                    session.close()
                    return true
                }

                val faction = chunkData.faction

                val message = """
                    §4[Faction] §cChunk Info:§f
                    §4[Faction] §cX/Z >> §a${chunk.x}/${chunk.z}
                    §4[Faction] §cID >> §a${chunkData.id}
                    §4[Faction] §cClaim >> §c${if (chunkData.state == ChunkState.CLAIMED) faction!!.name else "§aWildness"}
                """.trimIndent()
                player.sendMessage(message)
                session.close()
                true
            }
            "claim" -> {
                val session = Factions.instance.databaseConnector!!.sessionFactory!!.openSession()
                val transaction = session.beginTransaction()
                val factionPlayer = session.get(FactionPlayer::class.java, player.uniqueId)
                if (factionPlayer?.faction == null) {
                    player.sendMessage(messages.playerNotInFaction())
                    session.close()
                    return true
                }

                val chunk = player.location.chunk
                val chunkId = UUID.fromString(chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING))
                var chunkData = session.get(ChunkData::class.java, chunkId)
                if (chunkData == null) {
                    chunkData = ChunkData()
                    chunkData.id = chunkId
                    chunkData.x = chunk.x
                    chunkData.z = chunk.z
                }

                if (chunkData.state == ChunkState.CLAIMED) {
                    player.sendMessage(messages.chunkAlreadyClaimed())
                    session.close()
                    return true
                }

                val faction = factionPlayer.faction!!
                chunkData.state = ChunkState.CLAIMED
                chunkData.faction = factionPlayer.faction!!
                faction.chunks.add(chunkData)

                session.persist(faction)
                session.persist(chunkData)
                transaction.commit()
                session.close()

                player.sendMessage(messages.chunkClaimedMessage(chunk))
                true
            }
            "unclaim" -> {
                val session = Factions.instance.databaseConnector?.sessionFactory?.openSession() ?: return false
                val transaction = session.beginTransaction()
                val factionPlayer = session.get(FactionPlayer::class.java, player.uniqueId) ?: return false
                if (factionPlayer.faction == null) {
                    player.sendMessage(messages.playerNotInFaction())
                    session.close()
                    return true
                }

                val chunk = player.location.chunk
                val chunkId = UUIDUtil().parse(chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING))
                if (chunkId == null) {
                    player.sendMessage(messages.chunkNotClaimed())
                    session.close()
                    return true
                }

                val chunkData = session.get(ChunkData::class.java, chunkId)
                if (chunkData == null) {
                    player.sendMessage(messages.failedToFetchChunkData())
                    session.close()
                    return true
                }

                if (chunkData.faction != factionPlayer.faction) {
                    player.sendMessage(messages.chunkNotClaimedByPlayersFaction())
                    session.close()
                    return true
                }

                if (factionPlayer.faction!!.leader != factionPlayer) {
                    player.sendMessage(messages.playerNotFactionLeader())
                    session.close()
                    return true
                }

                session.remove(chunkData)
                transaction.commit()
                session.close()

                player.sendMessage(messages.chunkUnclaimed())
                true
            }
            "bypass" -> {
                if (player.hasPermission("factions.bypass")) {
                    if (player.persistentDataContainer.has(NamespacedKey.fromString("bypass")!!, PersistentDataType.BYTE)) {
                        player.sendMessage(messages.bypassModeDisabled())
                        player.persistentDataContainer.remove(NamespacedKey.fromString("bypass")!!)
                        return true
                    } else {
                        player.sendMessage(messages.bypassModeEnabled())
                        player.persistentDataContainer.set(NamespacedKey.fromString("bypass")!!, PersistentDataType.BYTE, 1)
                        return true
                    }
                } else {
                    player.sendMessage(messages.notPermitted())
                    return true
                }
            }
            else -> {
                //TODO: Implement
                false
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
        val subcommands = mutableListOf("id", "info", "claim", "unclaim", "bypass")

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