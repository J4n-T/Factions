package dev.tieseler.factions.data

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "factions", schema = "public")
class Faction {

    @Id
    var id: UUID? = null

    @Column(unique = true)
    var name: String? = null

    var acronym: String? = null
    var displayName: String? = null
    var description: String? = null

    var motd: String? = null

    @OneToOne
    var leader: FactionPlayer? = null

    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "faction_allies",
        joinColumns = [JoinColumn(name = "faction_id")],
        inverseJoinColumns = [JoinColumn(name = "ally_id")]
    )
    var allies: MutableList<Faction>? = mutableListOf()

    @ManyToMany(cascade = [CascadeType.ALL])
    @JoinTable(
        name = "faction_enemies",
        joinColumns = [JoinColumn(name = "faction_id")],
        inverseJoinColumns = [JoinColumn(name = "enemy_id")]
    )
    var enemies: MutableList<Faction> = mutableListOf()

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "faction")
    var members: MutableList<FactionPlayer> = mutableListOf()

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "faction")
    var invites: MutableList<FactionInvite> = mutableListOf()

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "faction")
    var roles: MutableList<Role> = mutableListOf()

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "faction")
    var chunks: MutableList<ChunkData> = mutableListOf()

    var createdAt: Long? = null

    var neutral: Boolean = false

    @Transient
    var state: FactionState = FactionState.NAME_REQUIRED

}