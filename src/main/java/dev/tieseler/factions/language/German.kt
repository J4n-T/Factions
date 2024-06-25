package dev.tieseler.factions.language

import dev.tieseler.factions.Factions
import net.kyori.adventure.text.Component

import net.kyori.adventure.text.Component.*
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.Chunk

class German : Messages {

    private val prefix = Factions.instance.prefix

    override fun failedToFetchPlayerData(): Component {
        return prefix.append(text(" Dein Profil konnte nicht abgerufen werden").color(RED))
    }

    override fun failedToFetchPlayerData(playerName: Component): Component {
        return prefix.append(text(" Das Profil von ").color(RED).append(playerName).append(text(" konnte nicht abgerufen werden").color(RED)))
    }

    override fun failedToFetchFactionData(): Component {
        return prefix.append(text(" Die Daten deiner Faction konnten nicht abgerufen werden").color(RED))
    }

    override fun failedToFetchFactionData(factionName: String): Component {
        return prefix.append(text(" Die Daten der Faction ").color(RED).append(text(factionName).append(text(" konnten nicht abgerufen werden").color(RED))))
    }

    override fun failedToFetchChunkData(): Component {
        return prefix.append(text(" Die Daten über den Chunk konnten nicht abgerufen werden").color(RED))
    }

    override fun alreadyInFaction(): Component {
        return prefix.append(text(" Du bist bereits in einer Faction").color(RED))
    }

    override fun missingFactionName(): Component {
        return prefix.append(text(" Wir sind hier nicht bei \"No Game No Life\"! Bitte gib einen Namen (ohne Leerzeichen) an!").color(RED))
    }

    override fun missingFactionDisplayName(): Component {
        //TODO: Bessere Nachricht
        return prefix.append(text(" Bitte gib einen Anzeigenamen für deine Faction ein").color(RED))
    }

    override fun missingFactionAcronym(): Component {
        return prefix.append(text(" Bitte gib ein Kürzel für deine Faction ein").color(RED))
    }

    override fun selectFactionMode(): Component {
        return prefix.append(text(" Bitte entscheide dich dafür, ob deine Faction neutral oder aggressiv sein soll!").color(RED))
    }

    override fun missingFactionDescription(): Component {
        return prefix.append(text(" Bitte gib eine Beschreibung für deine Faction ein").color(RED))
    }

    override fun factionAlreadyExists(factionName: String): Component {
        return prefix.append(text(" Die Faction ").color(RED).append(text(factionName).color(GREEN).append(text(" existiert bereits").color(RED))))
    }

    override fun playerNotInFaction(): Component {
        return prefix.append(text(" Du bist in keiner Factio").color(RED))
    }

    override fun chunkClaimedMessage(chunk: Chunk): Component {
        return prefix.append(text(" Du hast den Chunk ").color(GREEN).append(text("${chunk.x}/${chunk.z}").color(GOLD).append(text(" geclaimt").color(GREEN))))
    }

    override fun chunkAlreadyClaimed(): Component {
        return prefix.append(text(" Dieser Chunk wurde bereits geclaimt").color(RED))
    }

    override fun chunkNotClaimed(): Component {
        return prefix.append(text(" Dieser Chunk kann nicht unclaimed werden, da er nicht geclaimt wurde").color(RED))
    }

    override fun chunkNotClaimedByPlayersFaction(): Component {
        return prefix.append(text(" Du kannst nicht den Chunk einer anderen Faction unclaimen").color(RED))
    }

    override fun chunkUnclaimed(): Component {
        return prefix.append(text(" Der Chunk wurde unclaimed").color(GREEN))
    }

    override fun invited(displayName: Component): Component {
        return prefix.append(text(" Du hast ").color(GREEN).append(displayName).append(text(" eingeladen").color(GREEN)))
    }

    override fun inviteNotFound(): Component {
        return prefix.append(text(" Die Einladung konnte nicht gefunden werden").color(RED))
    }

    override fun joinedFaction(factionName: String): Component {
        return prefix.append(text(" Du bist der Faction ").color(GREEN).append(text(factionName).color(GOLD).append(text(" beigetreten").color(GREEN))))
    }

    override fun inviteDeclined(factionName: String): Component {
        return prefix.append(text(" Du hast die Einladung von ").color(RED).append(text(factionName).color(GOLD).append(text(" abgelehnt").color(RED))))
    }

    override fun inviteFailedPlayerNotFound(displayName: String): Component {
        return prefix.append(text(" Der Spieler $displayName").color(RED).append(text(" konnte nicht gefunden werden, da er entweder offline ist oder es ihn nicht gibt").color(RED)))
    }

    override fun inviteFailedPlayerAlreadyInvited(displayName: Component): Component {
        return prefix.append(text(" Der Spieler ").color(RED).append(displayName).append(text(" wurde bereits eingeladen").color(RED)))
    }

    override fun inviteFailedCannotInviteYourself(): Component {
        return prefix.append(text(" Du bist absolut einzigartig :3! Dich kann es nicht 2x in einer Faction geben <3"))
    }

    override fun inviteFailedPlayerAlreadyInYourFaction(displayName: Component): Component {
        return prefix.append(text(" Der Spieler ").color(RED).append(displayName).append(text(" ist bereits in deiner Faction").color(RED)))
    }

    override fun inviteCommandPlayerNameOrSubCommandRequired(): Component {
        return prefix.append(text(" Bitte gib den Namen eines Spielers oder einen Subcommand an").color(RED))
    }

    override fun motd(factionName: String, motd: String): Component {
        return text("§7[$factionName§7] Nachricht des Tages: ").color(GRAY).append(text(motd).color(GREEN))
    }

    override fun noMotd(): Component {
        return prefix.append(text(" Es gibt keine Nachricht des Tages").color(RED))
    }

    override fun missingMotd(): Component {
        return prefix.append(text(" Bitte gib eine Nachricht des Tages an").color(RED))
    }

    override fun motdChanged(): Component {
        return prefix.append(text(" Die Nachricht des Tages wurde geändert").color(GREEN))
    }

    override fun factionCreated(): Component {
        return prefix.append(text(" Deine Faction wurde gegründet").color(GREEN))
    }

    override fun playerNotFactionLeader(): Component {
        return prefix.append(text(" Du bist nicht der Anführer der Faction").color(RED))
    }

    override fun factionDisbanded(): Component {
        return prefix.append(text(" Deine Faction wurde aufgelöst").color(GREEN))
    }

    override fun factionNameChanged(): Component {
        return prefix.append(text(" Der Name deiner Faction wurde geändert").color(GREEN))
    }

    override fun factionDescriptionChanged(): Component {
        return prefix.append(text(" Die Beschreibung deiner Faction wurde geändert").color(GREEN))
    }

    override fun factionNameInvalid(): Component {
        return prefix.append(text(" Der angegebene Name ist ungültig").color(RED))
    }

    override fun factionDisplayNameToLong(): Component {
        return prefix.append(text(" Der angegebene Anzeigename ist zu lang").color(RED))
    }

    override fun factionDisplayNameChanged(): Component {
        return prefix.append(text(" Der Anzeigename deiner Faction wurde geändert").color(GREEN))
    }

    override fun factionAcronymToLong(): Component {
        return prefix.append(text(" Das angegebene Kürzel ist zu lang").color(RED))
    }

    override fun factionAcronymChanged(): Component {
        return prefix.append(text(" Das Kürzel deiner Faction wurde geändert").color(GREEN))
    }

    override fun factionNameMissing(): Component {
        return prefix.append(text(" Bitte gib den Namen der Faction an").color(RED))
    }

    override fun targetNotSameFaction(targetName: Component): Component {
        return prefix.append(text(" Der Spieler ").color(RED).append(targetName).append(text(" ist nicht in deiner Faction").color(RED)))
    }

}