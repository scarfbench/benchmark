package org.example.realworldapi;

import com.github.slugify.Slugify;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.mapper.ObjectMapperType;
import io.restassured.path.json.mapper.factory.GsonObjectMapperFactory;
import org.example.realworldapi.infrastructure.provider.JwtTokenProvider;
import org.example.realworldapi.infrastructure.repository.entity.*;
import org.example.realworldapi.infrastructure.web.provider.TokenProvider;
import org.example.realworldapi.util.LocalDateTimeTypeAdapter;
import org.example.realworldapi.util.UserEntityUtils;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.microshed.testing.SharedContainerConfig;
import org.microshed.testing.jupiter.MicroShedTest;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@MicroShedTest
@SharedContainerConfig(AppDeploymentConfig.class)
@EnableAutoWeld
public class AbstractIT extends DatabaseIT {

    protected static TokenProvider tokenProvider;
    protected Slugify slugify = Slugify.builder().build();

    @BeforeAll
    public static void beforeAll() {
        tokenProvider = new JwtTokenProvider("users-service", "secret123", 10);
        RestAssured.config = RestAssured.config.objectMapperConfig(ObjectMapperConfig.objectMapperConfig().defaultObjectMapperType(ObjectMapperType.GSON).gsonObjectMapperFactory(new GsonObjectMapperFactory() {
            @Override
            public Gson create(Type type, String s) {
                return new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter()).create();
            }
        }));
    }

    @AfterEach
    public void afterEach() {
        clear();
    }

    protected UserEntity createUserEntity(
            String username, String email, String bio, String image, String password) {
        return transaction(
                () -> {
                    final var userEntity = UserEntityUtils.create(username, email, password, bio, image);
                    entityManager.persist(userEntity);
                    return userEntity;
                });
    }

    protected String token(UserEntity userEntity) {
        return tokenProvider.createUserToken(userEntity.getId().toString());
    }

    protected void follow(UserEntity currentUser, UserEntity... followers) {

        transaction(
                () -> {
                    final var user = entityManager.find(UserEntity.class, currentUser.getId());

                    for (UserEntity follower : followers) {
                        FollowRelationshipEntity followRelationshipEntity = new FollowRelationshipEntity();
                        followRelationshipEntity.setFollowed(follower);
                        followRelationshipEntity.setUser(user);
                        entityManager.persist(followRelationshipEntity);
                    }

                    entityManager.persist(user);
                });
    }

    protected TagEntity createTagEntity(String name) {
        return transaction(
                () -> {
                    final var tag = new TagEntity();
                    tag.setId(UUID.randomUUID());
                    tag.setName(name);
                    entityManager.persist(tag);
                    return tag;
                });
    }

    protected List<TagRelationshipEntity> createArticlesTags(
            List<ArticleEntity> articles, TagEntity... tags) {
        return transaction(
                () -> {
                    final var resultList = new LinkedList<TagRelationshipEntity>();

                    for (ArticleEntity article : articles) {

                        final var managedArticle = entityManager.find(ArticleEntity.class, article.getId());

                        for (TagEntity tag : tags) {
                            final var managedTag = entityManager.find(TagEntity.class, tag.getId());

                            final var articlesTagsEntity = new TagRelationshipEntity();
                            articlesTagsEntity.setArticle(managedArticle);
                            articlesTagsEntity.setTag(managedTag);

                            entityManager.persist(articlesTagsEntity);
                            resultList.add(articlesTagsEntity);
                        }
                    }

                    return resultList;
                });
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

    protected ArticleEntity createArticleEntity(
            UserEntity author, String title, String description, String body) {
        return transaction(
                () -> {
                    final var article = new ArticleEntity();
                    article.setId(UUID.randomUUID());
                    article.setTitle(title);
                    article.setSlug(slugify.slugify(title));
                    article.setDescription(description);
                    article.setBody(body);
                    article.setAuthor(author);
                    entityManager.persist(article);
                    return article;
                });
    }

    protected ArticleEntity findArticleEntityById(UUID id) {
        return transaction(() -> entityManager.find(ArticleEntity.class, id));
    }

    protected CommentEntity findCommentEntityById(UUID id) {
        return transaction(() -> entityManager.find(CommentEntity.class, id));
    }

    protected FavoriteRelationshipEntity favorite(ArticleEntity article, UserEntity user) {
        return transaction(
                () -> {
                    final var favoriteRelationshipEntity = favoriteRelationshipEntity(article, user);
                    entityManager.persist(favoriteRelationshipEntity);
                    return favoriteRelationshipEntity;
                });
    }

    protected CommentEntity createComment(UserEntity author, ArticleEntity article, String body) {
        return transaction(
                () -> {
                    final var comment = new CommentEntity();
                    comment.setId(UUID.randomUUID());
                    comment.setBody(body);
                    comment.setArticle(article);
                    comment.setAuthor(author);
                    entityManager.persist(comment);
                    return comment;
                });
    }

    ;

    private FavoriteRelationshipEntity favoriteRelationshipEntity(
            ArticleEntity article, UserEntity loggedUser) {
        final var favoriteRelationshipEntity = new FavoriteRelationshipEntity();
        favoriteRelationshipEntity.setUser(loggedUser);
        favoriteRelationshipEntity.setArticle(article);
        return favoriteRelationshipEntity;
    }
}
