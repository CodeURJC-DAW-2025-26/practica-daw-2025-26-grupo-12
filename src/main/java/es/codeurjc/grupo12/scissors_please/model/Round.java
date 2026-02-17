package es.codeurjc.grupo12.scissors_please.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name="rounds")
@Data
@Entity
public class Round {
  private int roundNumber;
  private String bot1Move;
  private String bot2Move;
  private String result;

}