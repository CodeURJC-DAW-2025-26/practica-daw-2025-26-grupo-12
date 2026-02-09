package es.codeurjc.grupo12.scissors_please.model;

public class Round {
  private int roundNumber;
  private String bot1Move;
  private String bot2Move;
  private String result;

  public Round() {}

  public int getRoundNumber() {
    return roundNumber;
  }

  public void setRoundNumber(int roundNumber) {
    this.roundNumber = roundNumber;
  }

  public String getBot1Move() {
    return bot1Move;
  }

  public void setBot1Move(String bot1Move) {
    this.bot1Move = bot1Move;
  }

  public String getBot2Move() {
    return bot2Move;
  }

  public void setBot2Move(String bot2Move) {
    this.bot2Move = bot2Move;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }
}
