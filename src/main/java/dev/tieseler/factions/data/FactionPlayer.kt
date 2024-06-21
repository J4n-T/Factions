package dev.tieseler.factions.data

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "faction_players")
class FactionPlayer {

    @Id
    var id: UUID? = null

    @ManyToOne
    @JoinColumn(name = "faction_id")
    var faction: Faction? = null

    @OneToMany(mappedBy = "target")
    var invites: MutableList<FactionInvite> = mutableListOf()

    @ManyToOne
    @JoinColumn(name = "role_id")
    var role: Role? = null

}