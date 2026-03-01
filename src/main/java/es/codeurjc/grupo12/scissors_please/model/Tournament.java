package es.codeurjc.grupo12.scissors_please.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Table(name = "tournaments")
@Getter
@Setter
@Entity
public class Tournament {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  private Image image;

  private String description;

  @Enumerated(EnumType.STRING)
  private TournamentStatus status;

  private LocalDate startDate;
  private int slots;
  @OneToMany private List<Bot> participants;

  @OneToMany(cascade = CascadeType.ALL)
  private List<Match> matches;

  public boolean isUpcoming() {
    return status == TournamentStatus.UPCOMING;
  }

  public boolean isOpen() {
    return status == TournamentStatus.REGISTRATION_OPEN;
  }

  public boolean isScheduled() {
    return status == TournamentStatus.SCHEDULED;
  }

  public boolean isCompleted() {
    return status == TournamentStatus.COMPLETED;
  }

  public boolean isInProgress() {
    return status == TournamentStatus.IN_PROGRESS;
  }
}
