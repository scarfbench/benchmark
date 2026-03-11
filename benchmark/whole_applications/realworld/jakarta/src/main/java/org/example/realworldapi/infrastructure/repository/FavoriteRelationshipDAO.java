package org.example.realworldapi.infrastructure.repository;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.FavoriteRelationship;
import org.example.realworldapi.domain.model.article.FavoriteRelationshipRepository;
import org.example.realworldapi.infrastructure.repository.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.entity.FavoriteRelationshipEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Dependent
public class FavoriteRelationshipDAO extends AbstractDAO<FavoriteRelationshipEntity, Long> implements FavoriteRelationshipRepository {

    @Inject
    private EntityUtils entityUtils;

    @Override
    public boolean isFavorited(Article article, UUID currentUserId) {
        Query query = em.createNamedQuery("FVREisFavorited");
        query.setParameter("articleId", article.getId());
        query.setParameter("currentUserId", currentUserId);
        Long qty = (Long) query.getSingleResult();
        return (qty > 0);
    }

    @Override
    public long favoritesCount(Article article) {
        Query query = em.createNamedQuery("FVREfavoritesCount");
        query.setParameter("articleId", article.getId());
        return (Long) query.getSingleResult();
    }

    @Override
    public Optional<FavoriteRelationship> findByArticleIdAndUserId(UUID articleId, UUID currentUserId) {
        Query query = em.createNamedQuery("FVREfindByArticleAndUserID");
        query.setParameter("articleId", articleId);
        query.setParameter("currentUserId", currentUserId);

        List<FavoriteRelationshipEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            FavoriteRelationship f = entityUtils.favoriteRelationship(resultList.get(0));
            return Optional.of(f);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void save(FavoriteRelationship favoriteRelationship) {
        final var userEntity = findUserEntityById(favoriteRelationship.getUser().getId());
        final var articleEntity = findArticleEntityById(favoriteRelationship.getArticle().getId());
        em.persist(new FavoriteRelationshipEntity(userEntity, articleEntity));
    }

    @Override
    public void delete(FavoriteRelationship favoriteRelationship) {
        final var favoriteRelationshipEntity = findFavoriteRelationshipEntityByKey(favoriteRelationship);
        em.remove(favoriteRelationshipEntity);
        em.flush();
    }
}
