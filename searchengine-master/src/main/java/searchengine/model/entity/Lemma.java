package searchengine.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table (name = "lemma", indexes = {
        @Index(name = "idx_lemma", columnList = "lemma")
})
@Getter
@Setter
public class Lemma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false)
    private SiteEntity site;

    @Column(nullable = false)
    private String lemma;

    @Column
    private Integer frequency;

    @OneToMany(mappedBy = "lemma", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SearchIndex> indexes;
}
