package dev.tieseler.factions.commands

import dev.tieseler.factions.Factions
import dev.tieseler.factions.data.Faction
import dev.tieseler.factions.data.FactionPlayer
import dev.tieseler.factions.data.Role
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentIteratorType
import net.kyori.adventure.text.event.HoverEvent
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player
import java.util.*

class FactionCommand : CommandExecutor, TabCompleter {

    val messages = Factions.instance.messages

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val player = sender as? Player ?: return false
        if (args!!.isEmpty()) return false

        val session = Factions.instance.databaseConnector!!.sessionFactory!!.openSession()
        val transaction = session.beginTransaction()

        val factionPlayer = session.get(FactionPlayer::class.java, player.uniqueId)
        if (factionPlayer == null) {
            player.sendMessage(messages.failedToFetchPlayerData())
            session.close()
            return true
        }

        when (args[0]) {
            "create" -> {
                if (factionPlayer.faction != null) {
                    player.sendMessage(messages.alreadyInFaction())
                    session.close()
                    return true
                }

                if (args.size < 2) {
                    player.sendMessage(messages.missingFactionName())
                    session.close()
                    return true
                }

                if (args.size < 3 || (args[2].equals("neutral", true) && args[2].equals("aggressiv", true))) {
                    player.sendMessage(messages.selectFactionMode())
                    session.close()
                    return true
                }

                if (args.size < 4) {
                    player.sendMessage(messages.missingFactionDescription())
                    session.close()
                    return true
                }

                if (!validateFactionName(args[1])) {
                    player.sendMessage(messages.factionNameInvalid())
                    session.close()
                    return true
                }

                val query = session.createQuery("FROM Faction WHERE name = :name", Faction::class.java)
                query.setParameter("name", args[1])
                val result = query.setMaxResults(1).uniqueResult()
                if (result != null) {
                    player.sendMessage(messages.factionAlreadyExists(result.name!!))
                    session.close()
                    return true
                }

                val faction = Faction()
                faction.id = UUID.randomUUID()
                faction.name = args[1]
                faction.neutral = args[2].equals("neutral", true)
                faction.description = args.sliceArray(3 until args.size).joinToString(" ")
                faction.createdAt = System.currentTimeMillis()
                faction.leader = factionPlayer
                faction.members.add(factionPlayer)

                factionPlayer.faction = faction
                session.persist(faction)
                session.persist(factionPlayer)
                transaction.commit()
                session.close()

                val announcement = Component.text("§4[Announcement] §c${player.name} §ahat sich dazu entschieden die Faction §c${faction.name} §azu gründen")
                    .hoverEvent(HoverEvent.showText(Component.text("""
                        §fName: §c${faction.name!!}
                        §fBeschreibung: §c${faction.description!!}
                        §fEinstellung: §c${if (faction.neutral) "Neutral" else "Aggressiv"}
                        §fAnführer: §c${player.name}
                        §fMitglieder: §c${faction.members.size}
                    """.trimIndent())))
                Bukkit.getServer().broadcast(announcement)

                player.sendMessage(messages.factionCreated())
            }
            "disband" -> {
                if (factionPlayer.faction == null) {
                    player.sendMessage(messages.playerNotInFaction())
                    session.close()
                    return true
                }

                val faction = factionPlayer.faction
                if (faction == null) {
                    player.sendMessage(messages.failedToFetchFactionData())
                    session.close()
                    return true
                }

                if (faction.leader != factionPlayer) {
                    player.sendMessage(messages.playerNotFactionLeader())
                    session.close()
                    return true
                }

                session.remove(faction)

                faction.members.forEach { member ->
                    member.faction = null
                    session.persist(member)
                }

                transaction.commit()
                session.close()

                player.sendMessage(messages.factionDisbanded())
                Bukkit.getServer().broadcast(Component.text("§4[Announcement] §cDie Faction §4${faction.name} §cwurde aufgelöst!"))
                return true
            }
            "kick" -> {
                if (args.size < 2) {
                    player.sendMessage(Component.text("§4[Faction] §cDu hast keinen Spieler angegeben :/"))
                    session.close()
                    return true
                }

                if (factionPlayer.faction == null) {
                    player.sendMessage(messages.playerNotInFaction())
                    session.close()
                    return true
                }

                val target = Bukkit.getPlayer(args[1])
                if (target == null) {
                    player.sendMessage(Component.text("§4[Faction] §cDer Spieler §a${args[1]} §ckonnte nicht gekickt werden, da er nicht existiert oder nicht online ist :("))
                    session.close()
                    return true
                }

                val targetFactionPlayer = session.get(FactionPlayer::class.java, target.uniqueId)
                if (targetFactionPlayer == null) {
                    player.sendMessage(messages.failedToFetchPlayerData(target.displayName()))
                    session.close()
                    return true
                }

                val faction = factionPlayer.faction
                if (faction == null) {
                    player.sendMessage(messages.failedToFetchFactionData())
                    session.close()
                    return true
                }

                if (faction.leader != factionPlayer) {
                    player.sendMessage(messages.playerNotFactionLeader())
                    session.close()
                    return true
                }

                if (targetFactionPlayer.faction != faction) {
                    player.sendMessage(messages.targetNotSameFaction(target.displayName()))
                    session.close()
                    return true
                }

                faction.members.remove(targetFactionPlayer)
                targetFactionPlayer.faction = null
                session.persist(faction)
                session.persist(targetFactionPlayer)
                transaction.commit()
                session.close()

                player.sendMessage(Component.text("§4[Faction] §aDer Spieler §c${target.name} §awurde aus deiner Faction gekickt!"))
                target.sendMessage(Component.text("§4[Faction] §cDu wurdest aus der Faction §a${faction.name} §cgekickt!"))
                Bukkit.getServer().broadcast(Component.text("§4[Announcement] §cDer Spieler §4${target.name} §cwurde aus der Faction §4${faction.name} §cgekickt!"))
                return true
            }
            "motd" -> {
                val faction = factionPlayer.faction
                if (faction == null) {
                    player.sendMessage(messages.playerNotInFaction())
                    session.close()
                    return true
                }

                if (args.size < 2) {
                    player.sendMessage(messages.missingMotd())
                    session.close()
                    return true
                }
            }
            "edit" -> {
                if (args.size < 2) {
                    player.sendMessage(Component.text("§4[Faction] §cDu musst einen Unterbefehl angeben!"))
                    session.close()
                    return true
                }
                val faction = factionPlayer.faction
                if (faction == null) {
                    player.sendMessage(messages.playerNotInFaction())
                    session.close()
                    return true
                }

                if (faction.leader != factionPlayer) {
                    player.sendMessage(messages.playerNotFactionLeader())
                    session.close()
                    return true
                }

                when (args[1]) {
                    "name" -> {
                        if (args.size < 3) {
                            player.sendMessage(messages.missingFactionName())
                            session.close()
                            return true
                        }

                        if (!validateFactionName(args[2])) {
                            player.sendMessage(messages.factionNameInvalid())
                            session.close()
                            return true
                        }

                        val name = args[2]
                        faction.name = name
                        session.persist(faction)
                        transaction.commit()
                        session.close()

                        player.sendMessage(messages.factionNameChanged())
                        return true
                    }
                    "displayname" -> {
                        if (args.size < 3) {
                            player.sendMessage(messages.missingFactionName())
                            session.close()
                            return true
                        }

                        val displayName = args[2]
                        if (displayName.length > 32) {
                            player.sendMessage(messages.factionDisplayNameToLong())
                            session.close()
                            return true
                        }

                        faction.displayName = displayName
                        session.persist(faction)
                        transaction.commit()
                        session.close()

                        player.sendMessage(messages.factionDisplayNameChanged())
                        return true
                    }
                    "acronym" -> {
                        if (args.size < 3) {
                            player.sendMessage(messages.missingFactionAcronym())
                            session.close()
                            return true
                        }

                        val acronym = args[2]
                        if (acronym.length > 6) {
                            player.sendMessage(messages.factionAcronymToLong())
                            session.close()
                            return true
                        }

                        faction.acronym = acronym
                        session.persist(faction)
                        transaction.commit()
                        session.close()

                        player.sendMessage(messages.factionAcronymChanged())
                        return true
                    }
                    "description" -> {
                        if (args.size < 3) {
                            player.sendMessage(messages.missingFactionDescription())
                            session.close()
                            return true
                        }

                        val description = args.sliceArray(2 until args.size).joinToString(" ")
                        faction.description = description
                        session.persist(faction)
                        transaction.commit()
                        session.close()

                        player.sendMessage(messages.factionDescriptionChanged())
                        return true
                    }
                    "motd" -> {
                        if (args.size < 3) {
                            player.sendMessage(messages.missingMotd())
                            session.close()
                            return true
                        }

                        if (factionPlayer.faction!!.leader != factionPlayer) {
                            player.sendMessage(messages.playerNotFactionLeader())
                            session.close()
                            return true
                        }

                        val motd = args.sliceArray(2 until args.size).joinToString(" ")
                        faction.motd = motd
                        session.persist(faction)
                        transaction.commit()
                        session.close()

                        player.sendMessage(messages.motdChanged())
                        return true
                    }
                }
            }
            "roles" -> {
                if (args.size < 2) {
                    val faction = factionPlayer.faction
                    if (faction == null) {
                        player.sendMessage(messages.playerNotInFaction())
                        session.close()
                        return true
                    }

                    val roles = faction.roles
                    var response: Component? = null
                    var firstLoop = true
                    roles.forEach { role ->
                        if (firstLoop) {
                            response = messages.role(role.name, role.acronym!!, role.weight, role.players.size)
                            firstLoop = false
                        } else response!!.appendNewline().append(messages.role(role.name, role.acronym!!, role.weight, role.players.size))
                    }

                    session.close()
                    player.sendMessage(response!!)
                    return true
                }

                when (args[1]) {
                    "create" -> {
                        if (args.size < 3) {
                            player.sendMessage(messages.missingRoleName())
                            session.close()
                            return true
                        }

                        if (args.size < 4) {
                            player.sendMessage(messages.missingRoleAcronym())
                            session.close()
                            return true
                        }

                        if (args.size < 5) {
                            player.sendMessage(messages.missingRoleWeight())
                            session.close()
                            return true
                        }

                        val faction = factionPlayer.faction
                        if (faction == null) {
                            player.sendMessage(messages.playerNotInFaction())
                            session.close()
                            return true
                        }

                        val roleName = args[2]
                        if (!validateRoleName(roleName)) {
                            player.sendMessage(messages.invalidRoleName())
                            session.close()
                            return true
                        }

                        val roleAcronym = args[3]
                        if (roleAcronym.length > 6) {
                            player.sendMessage(messages.invalidRoleAcronym())
                            session.close()
                            return true
                        }

                        val roleWeight = args[4].toIntOrNull()
                        if (roleWeight == null) {
                            player.sendMessage(messages.invalidRoleWeight())
                            session.close()
                            return true
                        }

                        val role = Role()
                        role.name = roleName
                        role.acronym = roleAcronym
                        role.weight = roleWeight
                        role.faction = faction

                        faction.roles.add(role)
                        session.persist(role)
                        session.persist(faction)
                        transaction.commit()
                        session.close()

                        player.sendMessage(messages.roleCreated(roleName))
                        return true
                    }
                    else -> {
                        if (args.size < 3) {
                            player.sendMessage(messages.missingSubCommand())
                            session.close()
                            return true
                        }

                        val faction = factionPlayer.faction
                        if (faction == null) {
                            player.sendMessage(messages.playerNotInFaction())
                            session.close()
                            return true
                        }

                        val roles = faction.roles
                        val role = roles.firstOrNull { it.name == args[1] }
                        if (role == null) {
                            player.sendMessage(messages.roleNotFound(args[1]))
                            session.close()
                            return true
                        }

                        when (args[1]) {
                            "add" -> {
                                if (args.size < 4) {
                                    player.sendMessage(messages.missingPlayerName())
                                    session.close()
                                    return true
                                }

                                val target = Bukkit.getPlayer(args[3])
                                if (target == null) {
                                    player.sendMessage(messages.failedToFetchPlayerData(Component.text(args[3])))
                                    session.close()
                                    return true
                                }

                                val targetFactionPlayer = session.get(FactionPlayer::class.java, target.uniqueId)
                                if (targetFactionPlayer == null) {
                                    player.sendMessage(messages.failedToFetchPlayerData(target.displayName()))
                                    session.close()
                                    return true
                                }

                                role.players.add(targetFactionPlayer)
                                targetFactionPlayer.role = role
                                session.persist(role)
                                session.persist(targetFactionPlayer)
                                transaction.commit()
                                session.close()

                                player.sendMessage(messages.playerAddedToRole(target.displayName(), role.name))
                                return true
                            }
                        }
                    }
                }
            }
        }
        session.close()
        return false
    }

    private fun validateFactionName(name: String): Boolean {
        return name.matches(Regex("^[a-zA-Z0-9_]{3,16}$"))
    }

    private fun validateRoleName(name: String): Boolean {
        return name.matches(Regex("^[a-zA-Z0-9_]{3,16}$"))
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>
    ): MutableList<String> {
        // List of all subcommands
        val subcommands = mutableListOf("create", "disband", "invite", "kick", "edit", "motd", "roles")
        var completions = subcommands

        // If there are no arguments, suggest all subcommands
        if (args.isEmpty()) {
            return completions
        }

        // If the first argument is a subcommand, suggest completions for that subcommand
        if (subcommands.contains(args[0])) {
            when (args[0]) {
                "create" -> {
                    if (args.size == 3) {
                        completions = mutableListOf("neutral", "aggressiv")
                    } else return mutableListOf()
                }
                "kick" -> {
                    if (args.size == 2) {
                        val session = Factions.instance.databaseConnector!!.sessionFactory!!.openSession()
                        val factionPlayer = session.get(FactionPlayer::class.java, (sender as Player).uniqueId)
                        completions = mutableListOf()
                        factionPlayer.faction!!.members.forEach { member ->
                            completions.add(Bukkit.getOfflinePlayer(member.id!!).name!!)
                            completions.remove(sender.name)
                        }
                        session.close()
                    } else return mutableListOf()
                }
                "edit" -> {
                    if (args.size == 2) {
                        completions = mutableListOf("name", "displayname", "acronym", "description", "motd")
                    } else return mutableListOf()
                }
                "roles" -> {
                    if (args.size == 2) {
                        completions = mutableListOf("create")
                    }

                    if (args.size == 3) {
                        val session = Factions.instance.databaseConnector!!.sessionFactory!!.openSession()
                        val factionPlayer = session.get(FactionPlayer::class.java, (sender as Player).uniqueId)
                        completions = mutableListOf()
                        factionPlayer.faction!!.roles.forEach { role ->
                            completions.add(role.name)
                        }
                        session.close()
                    }

                    if (args.size == 4) {
                        completions = mutableListOf("add")
                    }

                    if (args.size == 5) {
                        completions = mutableListOf()
                        Bukkit.getOnlinePlayers().forEach { player ->
                            completions.add(player.name)
                        }
                    }

                    return mutableListOf()
                }
                else -> return mutableListOf()
            }
        }

        // Filter the completions based on the current input
        val iterator = completions.iterator()
        while (iterator.hasNext()) {
            if (!iterator.next().startsWith(args.last())) {
                iterator.remove()
            }
        }

        return completions
    }

}