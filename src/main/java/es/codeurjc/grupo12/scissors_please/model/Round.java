package es.codeurjc.grupo12.scissors_please.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name="rounds")
@Data
@Entity
public class Round {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  private int roundNumber;
  private String bot1Move;
  private String bot2Move;
  private String result;

}