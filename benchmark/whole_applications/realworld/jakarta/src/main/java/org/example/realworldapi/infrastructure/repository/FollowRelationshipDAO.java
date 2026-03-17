package org.example.realworldapi.infrastructure.repository;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import org.example.realworldapi.domain.model.user.FollowRelationship;
import org.example.realworldapi.domain.model.user.FollowRelationshipRepository;
import org.example.realworldapi.domain.model.user.User;
import org.example.realworldapi.infrastructure.repository.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.entity.FollowRelationshipEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Dependent
public class FollowRelationshipDAO extends AbstractDAO<FollowRelationshipEntity, Long> implements FollowRelationshipRepository {

    @Inject
    private EntityUtils entityUtils;

    @Override
    public boolean isFollowing(UUID currentUserId, UUID followedUserId) {
        Query query = em.createNamedQuery("FREisFollowing");
        query.setParameter("currentUserId", currentUserId);
        query.setParameter("followedUserId", followedUserId);

        List<FollowRelationshipEntity> resultList = (List<FollowRelationshipEntity>) query.getResultList();
        return !resultList.isEmpty();
    }

    @Override
    public void save(FollowRelationship followRelationship) {
        final var userEntity = findUserEntityById(followRelationship.getUser().getId());
        final var userToFollowEntity = findUserEntityById(followRelationship.getFollowed().getId());
        em.persist(new FollowRelationshipEntity(userEntity, userToFollowEntity));
        em.flush();
    }

    @Override
    public Optional<FollowRelationship> findByUsers(User loggedUser, User followedUser) {
        return findUsersFollowedEntityByUsers(loggedUser, followedUser).map(this::followingRelationship);
    }

    @Override
    public void remove(FollowRelationship followRelationship) {
        final var usersFollowedEntity = findUsersFollowedEntityByUsers(followRelationship.getUser(), followRelationship.getFollowed()).orElseThrow();
        em.remove(usersFollowedEntity);
    }

    private Optional<FollowRelationshipEntity> findUsersFollowedEntityByUsers(User loggedUser, User followedUser) {
        final var loggedUserEntity = findUserEntityById(loggedUser.getId());
        final var followedEntity = findUserEntityById(followedUser.getId());

        Query query = em.createNamedQuery("FRE.findByPrimaryKey_UserAndPrimaryKey_Followed");
        query.setParameter("loggedUserEntity", loggedUserEntity);
        query.setParameter("followedUserEntity", followedEntity);

        List<FollowRelationshipEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            return Optional.of(resultList.get(0));
        } else {
            return Optional.empty();
        }
    }

    private FollowRelationship followingRelationship(FollowRelationshipEntity followRelationshipEntity) {
        final var user = entityUtils.user(followRelationshipEntity.getUser());
        final var followed = entityUtils.user(followRelationshipEntity.getFollowed());
        return new FollowRelationship(user, followed);
    }
}
