package searchengine.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "site", indexes = @Index(name = "idx_site_url", columnList = "url"))
@Getter
@Setter
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusType status;

    @Column(name= "status_time", nullable = false)
    private Instant statusTime;

    @Column(name="last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(nullable = false, length = 100)
    private String url;

    @Column(nullable = false, length = 50)
    private String name;
}
