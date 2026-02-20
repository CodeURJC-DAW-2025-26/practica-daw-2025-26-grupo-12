package es.codeurjc.grupo12.scissors_please.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Table(name = "bots")
@Entity
@Data
public class Bot {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String description;
  private String code;
  private String language;
  private String image;
  private boolean isPublic;
  private int elo;

  @ManyToOne
  @JoinColumn(name = "owner_id")
  private User owner;

  @ElementCollection
  @CollectionTable(name = "bot_tags", joinColumns = @JoinColumn(name = "bot_id"))
  @Column(name = "tag")
  private List<String> tags = new ArrayList<>();
}
