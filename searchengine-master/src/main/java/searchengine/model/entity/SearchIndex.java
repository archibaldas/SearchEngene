package searchengine.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "`index`", indexes = {
        @Index(name = "idx_index_lemma", columnList = "lemma_id"),
        @Index(name = "idx_index_page", columnList = "page_id"),
        @Index(name = "idx_index_lemma_page", columnList = "lemma_id,page_id")
} )
@Getter
@Setter
public class SearchIndex {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "page_id", nullable = false)
    private Page page;

    @ManyToOne
    @JoinColumn(name = "lemma_id", nullable = false)
    private Lemma lemma;

    @Column(name = "`rank`", nullable = false)
    private Float rank = 0.0f;
}
