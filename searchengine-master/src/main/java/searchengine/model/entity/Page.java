package searchengine.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "page", indexes = @Index(name = "idx_path", columnList = "path"))
@Getter
@Setter
public class Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name= "site_id", nullable = false)
    private SiteEntity site;

    @Column(name = "path", length = 500, nullable = false)
    private String path;

    @Column(nullable = false)
    private Integer code;

    @Lob
    @Column(nullable = false, columnDefinition = "MEDIUMTEXT")
    private String content;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SearchIndex> indexes;

}
