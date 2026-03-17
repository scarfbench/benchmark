package org.example.realworldapi.infrastructure.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.article.ArticleFilter;
import org.example.realworldapi.domain.model.article.ArticleRepository;
import org.example.realworldapi.domain.model.article.PageResult;
import org.example.realworldapi.infrastructure.repository.entity.ArticleEntity;
import org.example.realworldapi.infrastructure.repository.entity.EntityUtils;

import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class ArticleDAO extends AbstractDAO<ArticleEntity, UUID> implements ArticleRepository {

    @Inject
    private EntityUtils entityUtils;

    @Override
    public boolean existsBySlug(String slug) {
        String jpql = "SELECT a FROM ArticleEntity a WHERE UPPER(a.slug) = :slug";
        TypedQuery<ArticleEntity> query = em.createQuery(jpql, ArticleEntity.class);
        query.setParameter("slug", slug.toUpperCase().trim());

        List<ArticleEntity> resultList = query.getResultList();
        return !resultList.isEmpty();
    }

    @Override
    public void save(Article article) {
        final var author = findUserEntityById(article.getAuthor().getId());
        em.persist(new ArticleEntity(article, author));
        em.flush();
    }

    @Override
    public Optional<Article> findArticleById(UUID id) {
        ArticleEntity a = findArticleEntityById(id);
        if (a != null) {
            Article art = entityUtils.article(a);
            return Optional.of(art);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Optional<Article> findBySlug(String slug) {

        String jpql = "SELECT a FROM ArticleEntity a WHERE UPPER(a.slug) = :slug";
        TypedQuery<ArticleEntity> query = em.createQuery(jpql, ArticleEntity.class);
        query.setParameter("slug", slug.toUpperCase().trim());

        List<ArticleEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            Article article = entityUtils.article(resultList.get(0));
            return Optional.of(article);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void update(Article article) {
        final var articleEntity = findArticleEntityById(article.getId());
        articleEntity.update(article);
    }

    @Override
    public Optional<Article> findByAuthorAndSlug(UUID authorId, String slug) {
        Query query = em.createNamedQuery("ArticleEntity.findBySlugIgnoreCaseAndAuthor_Id");
        query.setParameter("slug", slug.toUpperCase().trim());
        query.setParameter("authorId", authorId);

        List<ArticleEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            Article article = entityUtils.article(resultList.get(0));
            return Optional.of(article);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void delete(Article article) {
        final var articleEntity = findArticleEntityById(article.getId());
        em.remove(articleEntity);
    }

    @Override
    public PageResult<Article> findMostRecentArticlesByFilter(ArticleFilter articleFilter) {
        String jpql = "SELECT a FROM ArticleEntity a " + "JOIN a.author author " + "JOIN author.followedBy followedBy " + "WHERE followedBy.user.id = :loggedUserId " + "ORDER BY a.createdAt DESC, a.updatedAt DESC";

        // Create the query and set parameters
        Query query = em.createQuery(jpql);
        query.setParameter("loggedUserId", articleFilter.getLoggedUserId());

        // Handle paging
        int offset = articleFilter.getOffset();
        int limit = articleFilter.getLimit();
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        // Execute the query and fetch the results
        List<ArticleEntity> articlesEntity = query.getResultList();

        // Map the results to Article objects
        List<Article> articlesResult = articlesEntity.stream().map(entityUtils::article).collect(Collectors.toList());

        // Calculate the total count (you can create a separate method for this)
        long total = countTotalArticles(articleFilter.getLoggedUserId());

        return new PageResult<>(articlesResult, total);
    }

    private long countTotalArticles(UUID loggedUserId) {
        // Create a count query
        String countJpql = "SELECT COUNT(a) FROM ArticleEntity a " + "JOIN a.author author " + "JOIN author.followedBy followedBy " + "WHERE followedBy.user.id = :loggedUserId";

        // Create the query and set parameters
        Query countQuery = em.createQuery(countJpql);
        countQuery.setParameter("loggedUserId", loggedUserId);

        // Execute the count query
        return (long) countQuery.getSingleResult();
    }

    @Override
    public PageResult<Article> findArticlesByFilter(ArticleFilter filter) {
        Map<String, Object> params = new LinkedHashMap<>();
        SimpleQueryBuilder findArticlesQueryBuilder = new SimpleQueryBuilder();
        SimpleQueryBuilder countArticlesQueryBuilder = new SimpleQueryBuilder();
        findArticlesQueryBuilder.addQueryStatement("select a from ArticleEntity a");
        countArticlesQueryBuilder.addQueryStatement("select count(a) from ArticleEntity a");
        configFilterFindArticlesQueryBuilder(findArticlesQueryBuilder, filter.getTags(), filter.getAuthors(), filter.getFavorited(), params);

        configFilterFindArticlesQueryBuilder(countArticlesQueryBuilder, filter.getTags(), filter.getAuthors(), filter.getFavorited(), params);

        String jpql = findArticlesQueryBuilder.toQueryString();
        String countJpql = countArticlesQueryBuilder.toQueryString();
        jpql = jpql + " ORDER BY a.createdAt DESC, a.updatedAt DESC";
        Query query = em.createQuery(jpql);
        Query countQuery = em.createQuery(countJpql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }

        int offset = filter.getOffset();
        int limit = filter.getLimit();
        query.setFirstResult(offset);
        query.setMaxResults(limit);

        List<ArticleEntity> articlesEntity = query.getResultList();
        long countArticleEntities = (long) countQuery.getSingleResult();

        final var articlesResult = articlesEntity.stream().map(entityUtils::article).toList();
        return new PageResult<>(articlesResult, countArticleEntities);
    }

    private void configFilterFindArticlesQueryBuilder(SimpleQueryBuilder findArticlesQueryBuilder, List<String> tags, List<String> authors, List<String> favorited, Map<String, Object> params) {

        List<String> tagsUpper = new ArrayList<>();
        List<String> authorsUpper = new ArrayList<>();
        List<String> favUpper = new ArrayList<>();

        if (isNotEmpty(tags)) {
            tagsUpper = toUpperCase(tags);
        }

        if (isNotEmpty(authors)) {
            authorsUpper = toUpperCase(authors);
        }

        if (isNotEmpty(favorited)) {
            favUpper = toUpperCase(favorited);
        }

        List<String> finalTagsUpper = tagsUpper;
        List<String> finalAuthorsUpper = authorsUpper;
        List<String> finalFavsUpper = favUpper;
        findArticlesQueryBuilder.updateQueryStatementConditional(isNotEmpty(tags), "inner join a.tags t inner join t.tag as tag", "upper(tag.name) in :tags", () -> params.put("tags", finalTagsUpper));

        findArticlesQueryBuilder.updateQueryStatementConditional(isNotEmpty(authors), "inner join a.author as authors", "upper(authors.username) in :authors", () -> params.put("authors", finalAuthorsUpper));

        findArticlesQueryBuilder.updateQueryStatementConditional(isNotEmpty(favorited), "inner join a.favorites as favorites inner join favorites.user as user", "upper(user.username) in :favorites", () -> params.put("favorites", finalFavsUpper));
    }
}
