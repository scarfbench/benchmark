package org.example.realworldapi.infrastructure.repository;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import org.example.realworldapi.domain.model.article.Article;
import org.example.realworldapi.domain.model.comment.Comment;
import org.example.realworldapi.domain.model.comment.CommentRepository;
import org.example.realworldapi.infrastructure.repository.entity.CommentEntity;
import org.example.realworldapi.infrastructure.repository.entity.EntityUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Dependent
public class CommentDAO extends AbstractDAO<CommentEntity, UUID>
        implements CommentRepository {

    @Inject
    private EntityUtils entityUtils;

    @Override
    public void save(Comment comment) {
        final var authorEntity = findUserEntityById(comment.getAuthor().getId());
        final var articleEntity = findArticleEntityById(comment.getArticle().getId());
        em.persist(new CommentEntity(authorEntity, articleEntity, comment));
    }

    @Override
    public Optional<Comment> findByIdAndAuthor(UUID commentId, UUID authorId) {
        Query query = em.createNamedQuery("CommentEntity.findByIdAndAuthor_Id");
        query.setParameter("commentId", commentId);
        query.setParameter("authorId", authorId);

        List<CommentEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            Comment c = entityUtils.comment(resultList.get(0));
            return Optional.of(c);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void delete(Comment comment) {
        final var commentEntity = findCommentEntityById(comment.getId());
        em.remove(commentEntity);
    }

    @Override
    public List<Comment> findCommentsByArticle(Article article) {
        Query query = em.createNamedQuery("CommentEntity.findByArticle_Id");
        query.setParameter("articleId", article.getId());

        List<CommentEntity> resultList = query.getResultList();
        return resultList.stream().map(entityUtils::comment).collect(Collectors.toList());
    }
}
