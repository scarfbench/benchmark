package org.example.realworldapi.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.realworldapi.domain.model.tag.Tag;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "TAGS")
@NamedQueries({
        @NamedQuery(name = "TagEntity.findByNamesIgnoreCase", query = "select t from TagEntity t where upper(t.name) in :names")
})
public class TagEntity {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "tag")
    private List<TagRelationshipEntity> articlesTags;

    public TagEntity(Tag tag) {
        this.id = tag.getId();
        this.name = tag.getName();
    }
}
