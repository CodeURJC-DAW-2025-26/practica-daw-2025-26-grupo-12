package es.codeurjc.grupo12.scissors_please.model;

import java.util.List;

public class Tournament {
  private Long id;
  private String name;
  private String description;
  private String status;
  private User creator;
  private List<Bot> participants;

  public Tournament() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public User getCreator() {
    return creator;
  }

  public void setCreator(User creator) {
    this.creator = creator;
  }

  public List<Bot> getParticipants() {
    return participants;
  }

  public void setParticipants(List<Bot> participants) {
    this.participants = participants;
  }
}
