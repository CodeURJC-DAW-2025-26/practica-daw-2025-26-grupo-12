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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Table(name = "bots")
@Entity
@EntityListeners(AuditingEntityListener.class)
// As we don't need all the methods of Data we can use this for making lighter
@Getter
@Setter
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

  @Column(name = "owner_id", nullable = false)
  private Long ownerId;

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
