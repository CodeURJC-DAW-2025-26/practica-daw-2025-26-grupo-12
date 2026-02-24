package es.codeurjc.grupo12.scissors_please.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Table(name = "bots")
@Entity
@Getter
@Setter
public class Bot {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String description;
  @JsonIgnore
  private String code;
  private String language;
  private String image;
  private boolean isPublic;
  private int elo;

  @Column(name = "owner_id", nullable = false)
  private Long ownerId;

  @ElementCollection
  @CollectionTable(name = "bot_tags", joinColumns = @JoinColumn(name = "bot_id"))
  @Column(name = "tag")
  private List<String> tags = new ArrayList<>();
}
