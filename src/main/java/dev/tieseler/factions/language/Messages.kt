package dev.tieseler.factions.language

import net.kyori.adventure.text.Component
import org.bukkit.Chunk
import java.util.UUID

interface Messages {

    fun senderNotPlayer(): Component

    fun failedToFetchPlayerData(): Component
    fun failedToFetchPlayerData(playerName: Component): Component
    fun failedToFetchFactionData(): Component
    fun failedToFetchFactionData(factionName: String): Component
    fun failedToFetchChunkData(): Component

    fun missingFactionName(): Component
    fun missingFactionDisplayName(): Component
    fun missingFactionAcronym(): Component
    fun missingFactionDescription(): Component
    fun missingRoleName(): Component
    fun missingRoleAcronym(): Component
    fun missingRoleWeight(): Component
    fun missingSubCommand(): Component
    fun missingPlayerName(): Component

    fun invalidSubCommand(): Component
    fun invalidRoleName(): Component
    fun invalidRoleAcronym(): Component
    fun invalidRoleWeight(): Component

    fun role(name: String, acronym: String, weight: Int, members: Int): Component
    fun roleCreated(roleName: String): Component
    fun roleNotFound(roleName: String): Component
    fun roleDeleted(roleName: String): Component
    fun noRoles(): Component
    fun rolesListHeader(): Component

    fun bypassModeEnabled(): Component
    fun bypassModeDisabled(): Component

    fun alreadyInFaction(): Component
    fun selectFactionMode(): Component
    fun factionAlreadyExists(factionName: String): Component
    fun factionCreated(): Component
    fun factionDisbanded(): Component
    fun factionNameChanged(): Component
    fun factionDescriptionChanged(): Component
    fun factionDescriptionToLong(): Component
    fun factionNameInvalid(): Component
    fun factionDisplayNameToLong(): Component
    fun factionDisplayNameChanged(): Component
    fun factionAcronymToLong(): Component
    fun factionAcronymChanged(): Component
    fun factionNameMissing(): Component
    fun factionLeaderCannotLeave(): Component
    fun factionLeft(): Component

    fun targetNotSameFaction(targetName: Component): Component

    fun playerNotFactionLeader(): Component
    fun playerNotInFaction(): Component
    fun playerAddedToRole(playerName: Component, roleName: String): Component

    fun chunkClaimedMessage(chunk: Chunk): Component
    fun chunkAlreadyClaimed(): Component
    fun chunkNotClaimed(): Component
    fun chunkNotClaimedByPlayersFaction(): Component
    fun chunkUnclaimed(): Component
    fun chunkInfo(x: Int, z: Int, id: UUID, state: Component): Component

    fun invited(displayName: Component): Component
    fun inviteNotFound(): Component
    fun joinedFaction(factionName: Component): Component
    fun inviteDeclined(factionName: Component): Component
    fun inviteFailedPlayerNotFound(displayName: String): Component
    fun inviteFailedPlayerAlreadyInvited(displayName: Component): Component
    fun inviteFailedCannotInviteYourself(): Component
    fun inviteFailedPlayerAlreadyInYourFaction(displayName: Component): Component
    fun inviteCommandPlayerNameOrSubCommandRequired(): Component
    fun inviteFailedAlreadyInFaction(): Component

    fun motd(factionName: Component, motd: Component): Component
    fun noMotd(): Component
    fun missingMotd(): Component
    fun motdChanged(): Component
    fun motdTooLong(): Component

    fun notPermitted(): Component

    fun cannotKickYourself(): Component
}
