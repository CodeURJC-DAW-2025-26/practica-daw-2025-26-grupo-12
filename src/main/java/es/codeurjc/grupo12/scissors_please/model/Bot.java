package es.codeurjc.grupo12.scissors_please.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(name = "bots")
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
public class Bot {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String description;
  private String code = "";
  private String language;
  private String image;
  private boolean isPublic;
  private int elo;

  @ManyToOne
  @JoinColumn(name = "owner_id")
  private User owner;

  @CreationTimestamp private LocalDateTime createdAt;

  @LastModifiedDate private LocalDateTime updatedAt;

  @ElementCollection
  @CollectionTable(name = "bot_tags", joinColumns = @JoinColumn(name = "bot_id"))
  @Column(name = "tag")
  private List<String> tags = new ArrayList<>();

  @PrePersist
  public void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  public void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}
