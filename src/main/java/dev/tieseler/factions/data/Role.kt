package dev.tieseler.factions.data

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "roles", schema = "public")
class Role {

    @Id
    var id: UUID = UUID.randomUUID()

    var name: String = ""
    var acronym: String? = null

    var weight: Int = 0

    @OneToMany(mappedBy = "role")
    var players: MutableList<FactionPlayer> = mutableListOf()

    @ManyToOne
    var faction: Faction? = null

    @OneToMany(mappedBy = "role")
    var claims: MutableList<RoleClaims> = mutableListOf()

}