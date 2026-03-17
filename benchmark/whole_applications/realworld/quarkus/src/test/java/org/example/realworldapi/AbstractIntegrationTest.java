package org.example.realworldapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import org.example.realworldapi.infrastructure.provider.JwtTokenProvider;
import org.example.realworldapi.infrastructure.repository.hibernate.entity.*;
import org.example.realworldapi.util.UserEntityUtils;
import org.junit.jupiter.api.AfterEach;

public class AbstractIntegrationTest {

  @Inject protected ObjectMapper objectMapper;
  @Inject protected JwtTokenProvider tokenProvider;
  @Inject protected Slugify slugify;
  @Inject EntityManager em;
  @Inject DatabaseIntegrationTest db;

  @AfterEach
  public void afterEach() {
    db.truncate();
  }

  @Transactional
  protected UserEntity createUserEntity(
      String username, String email, String bio, String image, String password) {
    final var userEntity = UserEntityUtils.create(username, email, password, bio, image);
    em.persist(userEntity);
    return userEntity;
  }

  protected String token(UserEntity userEntity) {
    return tokenProvider.createUserToken(userEntity.getId().toString());
  }

  @Transactional
  protected void follow(UserEntity currentUser, UserEntity... followers) {
    final var user = em.find(UserEntity.class, currentUser.getId());

    for (UserEntity follower : followers) {
      FollowRelationshipEntityKey key = new FollowRelationshipEntityKey();
      key.setUser(user);
      key.setFollowed(follower);

      FollowRelationshipEntity followRelationshipEntity = new FollowRelationshipEntity();
      followRelationshipEntity.setPrimaryKey(key);
      em.persist(followRelationshipEntity);
    }

    em.persist(user);
  }

  @Transactional
  protected TagEntity createTagEntity(String name) {
    final var tag = new TagEntity();
    tag.setId(UUID.randomUUID());
    tag.setName(name);
    em.persist(tag);
    return tag;
  }

  @Transactional
  protected List<TagRelationshipEntity> createArticlesTags(
      List<ArticleEntity> articles, TagEntity... tags) {
    final var resultList = new LinkedList<TagRelationshipEntity>();

    for (ArticleEntity article : articles) {

      final var managedArticle = em.find(ArticleEntity.class, article.getId());

      for (TagEntity tag : tags) {
        final var managedTag = em.find(TagEntity.class, tag.getId());

        final var articlesTagsEntityKey = new TagRelationshipEntityKey();
        articlesTagsEntityKey.setArticle(managedArticle);
        articlesTagsEntityKey.setTag(managedTag);

        final var articlesTagsEntity = new TagRelationshipEntity();
        articlesTagsEntity.setPrimaryKey(articlesTagsEntityKey);

        em.persist(articlesTagsEntity);
        resultList.add(articlesTagsEntity);
      }
    }

    return resultList;
  }

  protected List<ArticleEntity> createArticles(
      UserEntity author, String title, String description, String body, int quantity) {
    final var articles = new LinkedList<ArticleEntity>();
    for (int articleIndex = 0; articleIndex < quantity; articleIndex++) {
      articles.add(
          createArticleEntity(
              author,
              title + "_" + articleIndex,
              description + "_" + articleIndex,
              body + "_" + articleIndex));
    }
    return articles;
  }

  @Transactional
  protected ArticleEntity createArticleEntity(
      UserEntity author, String title, String description, String body) {
    final var article = new ArticleEntity();
    article.setId(UUID.randomUUID());
    article.setTitle(title);
    article.setSlug(slugify.slugify(title));
    article.setDescription(description);
    article.setBody(body);
    article.setAuthor(author);
    em.persist(article);
    return article;
  }

  @Transactional
  protected ArticleEntity findArticleEntityById(UUID id) {
    return em.find(ArticleEntity.class, id);
  }

  @Transactional
  protected CommentEntity findCommentEntityById(UUID id) {
    return em.find(CommentEntity.class, id);
  }

  @Transactional
  protected FavoriteRelationshipEntity favorite(ArticleEntity article, UserEntity user) {
    final var favoriteRelationshipEntity = favoriteRelationshipEntity(article, user);
    em.persist(favoriteRelationshipEntity);
    return favoriteRelationshipEntity;
  }

  @Transactional
  protected CommentEntity createComment(UserEntity author, ArticleEntity article, String body) {
    final var comment = new CommentEntity();
    comment.setId(UUID.randomUUID());
    comment.setBody(body);
    comment.setArticle(article);
    comment.setAuthor(author);
    em.persist(comment);
    return comment;
  }

  private FavoriteRelationshipEntity favoriteRelationshipEntity(
      ArticleEntity article, UserEntity loggedUser) {
    final var favoriteRelationshipEntityKey = favoriteRelationshipEntityKey(article, loggedUser);
    final var favoriteRelationshipEntity = new FavoriteRelationshipEntity();
    favoriteRelationshipEntity.setPrimaryKey(favoriteRelationshipEntityKey);
    return favoriteRelationshipEntity;
  }

  private FavoriteRelationshipEntityKey favoriteRelationshipEntityKey(
      ArticleEntity article, UserEntity loggedUser) {
    final var favoriteRelationshipEntityKey = new FavoriteRelationshipEntityKey();
    favoriteRelationshipEntityKey.setArticle(article);
    favoriteRelationshipEntityKey.setUser(loggedUser);
    return favoriteRelationshipEntityKey;
  }
}
