package es.codeurjc.grupo12.scissors_please.model;

import java.time.LocalDateTime;
import java.util.List;

public class Match {
  private Long id;
  private Bot bot1;
  private Bot bot2;
  private int bot1Score;
  private int bot2Score;
  private LocalDateTime timestamp;
  private String result;
  private List<Round> rounds;

  public Match() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Bot getBot1() {
    return bot1;
  }

  public void setBot1(Bot bot1) {
    this.bot1 = bot1;
  }

  public Bot getBot2() {
    return bot2;
  }

  public void setBot2(Bot bot2) {
    this.bot2 = bot2;
  }

  public int getBot1Score() {
    return bot1Score;
  }

  public void setBot1Score(int bot1Score) {
    this.bot1Score = bot1Score;
  }

  public int getBot2Score() {
    return bot2Score;
  }

  public void setBot2Score(int bot2Score) {
    this.bot2Score = bot2Score;
  }

  public LocalDateTime getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(LocalDateTime timestamp) {
    this.timestamp = timestamp;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public List<Round> getRounds() {
    return rounds;
  }

  public void setRounds(List<Round> rounds) {
    this.rounds = rounds;
  }
}
