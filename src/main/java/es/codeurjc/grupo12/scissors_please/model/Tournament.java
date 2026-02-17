package es.codeurjc.grupo12.scissors_please.model;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "tournaments")
@Data
@Entity
public class Tournament {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;
  private String description;
  private String status;
  private User creator;

  @OneToMany
  private List<Bot> participants;

  @OneToMany(cascade=CascadeType.ALL)
  private List<Match> matches;

}
