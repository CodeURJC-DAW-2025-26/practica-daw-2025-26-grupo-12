package es.codeurjc.grupo12.scissors_please.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name="matches")
@Entity
@Data
public class Match {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  private Bot bot1;

  @ManyToOne
  private Bot bot2;

  private int bot1Score;
  private int bot2Score;
  private LocalDateTime timestamp;
  private String result;

  @OneToMany(cascade=CascadeType.ALL)
  private List<Round> rounds;
}