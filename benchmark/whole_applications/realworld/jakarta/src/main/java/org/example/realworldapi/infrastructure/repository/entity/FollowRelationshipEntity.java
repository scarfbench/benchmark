package org.example.realworldapi.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "FOLLOW_RELATIONSHIP")
@NamedQueries({
        @NamedQuery(name = "FREisFollowing", query = "select f from FollowRelationshipEntity f where f.user.id = :currentUserId and f.followed.id = :followedUserId"),
        @NamedQuery(name = "FRE.findByPrimaryKey_UserAndPrimaryKey_Followed", query = "select f from FollowRelationshipEntity f where f.user = :loggedUserEntity and f.followed = :followedUserEntity")
})
public class FollowRelationshipEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(updatable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(updatable = false)
    private UserEntity followed;

    public FollowRelationshipEntity(UserEntity user, UserEntity followed) {
        this.followed = followed;
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        FollowRelationshipEntity that = (FollowRelationshipEntity) o;
        return Objects.equals(user, that.user) && Objects.equals(followed, that.followed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, followed);
    }
}
