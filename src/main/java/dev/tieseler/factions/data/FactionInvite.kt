package dev.tieseler.factions.data

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.util.*

@Entity
@Table(name = "faction_invites", schema = "public")
class FactionInvite {

    @Id
    var id: UUID? = null

    @ManyToOne
    var faction: Faction? = null

    @ManyToOne
    var target: FactionPlayer? = null

    @ManyToOne
    var sender: FactionPlayer? = null

    var message: String? = null
    var createdAt: Long? = null

}