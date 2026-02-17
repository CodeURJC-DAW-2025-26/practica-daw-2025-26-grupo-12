package es.codeurjc.grupo12.scissors_please.model;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name="bots")
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
  private List<String> tags;

}