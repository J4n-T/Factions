package dev.tieseler.factions.commands

import dev.tieseler.factions.Factions
import net.kyori.adventure.text.Component
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Pig
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

import org.bukkit.Material.*
import org.bukkit.block.data.type.Fence

class PepoSitCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val player = sender as? Player ?: return false
        if (player.vehicle != null || player.fallDistance > 0) {
            return true
        }

        val location = player.location.subtract(0.0, 0.8, 0.0)
        if (location.block.blockData is Fence || location.block.type == END_ROD || player.location.block.isLiquid) {
            player.sendMessage(Component.text("§cNö"))
            return true
        }

        val pig = player.world.spawnEntity(location, EntityType.PIG) as Pig
        Factions.instance.pigs[pig.uniqueId] = pig

        pig.isInvisible = true
        pig.isInvulnerable = true
        pig.isSilent = true
        pig.isCollidable = false
        pig.setGravity(false)
        pig.setAI(false)
        pig.addPassenger(player)
        pig.setRotation(player.location.yaw, player.location.pitch)
        pig.persistentDataContainer.set(NamespacedKey.fromString("pepo_sit")!!, PersistentDataType.BYTE, 1)

        return true
    }
}