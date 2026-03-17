package org.example.realworldapi.infrastructure.repository;

import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.example.realworldapi.domain.model.tag.Tag;
import org.example.realworldapi.domain.model.tag.TagRepository;
import org.example.realworldapi.infrastructure.repository.entity.EntityUtils;
import org.example.realworldapi.infrastructure.repository.entity.TagEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Dependent
public class TagDAO extends AbstractDAO<TagEntity, UUID>
        implements TagRepository {

    @Inject
    private EntityUtils entityUtils;

    @Override
    public List<Tag> findAllTags() {
        String jpql = "SELECT t FROM TagEntity t";
        TypedQuery<TagEntity> query = em.createQuery(jpql, TagEntity.class);

        List<TagEntity> resultList = query.getResultList();
        return resultList.stream().map(entityUtils::tag).collect(Collectors.toList());
    }

    @Override
    public Optional<Tag> findByName(String name) {
        String jpql = "SELECT t FROM TagEntity t WHERE UPPER(t.name) = :name";
        TypedQuery<TagEntity> query = em.createQuery(jpql, TagEntity.class);
        query.setParameter("name", name.toUpperCase().trim());

        List<TagEntity> resultList = query.getResultList();
        if (!resultList.isEmpty()) {
            Tag t = entityUtils.tag(resultList.get(0));
            return Optional.of(t);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public void save(Tag tag) {
        em.persist(new TagEntity(tag));
    }

    @Override
    public List<Tag> findByNames(List<String> names) {

        Query query = em.createNamedQuery("TagEntity.findByNamesIgnoreCase");
        query.setParameter("names", toUpperCase(names));

        List<TagEntity> resultList = query.getResultList();
        return resultList.stream().map(entityUtils::tag).collect(Collectors.toList());
    }
}
