package dev.tieseler.factions.commands

import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class ChunkCommand : CommandExecutor, TabCompleter {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val player = sender as? Player ?: return false

        if (args!!.isEmpty()) return false
        when (args[0]) {
            "id" -> {
                player.sendMessage("Your chunk id is: ${player.location.chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING)}")
                return true
            }
            "info" -> {

                val message = """
                |Chunk Info:
                | ID: ${player.location.chunk.persistentDataContainer.get(NamespacedKey.fromString("chunk_id")!!, PersistentDataType.STRING)}
                | X: ${player.location.chunk.x}
                | Z: ${player.location.chunk.z}
                | 
                """.trimIndent()
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
        val subcommands = mutableListOf("id", "info")

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