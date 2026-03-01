package es.codeurjc.grupo12.scissors_please.model;

public enum TournamentStatus {
  UPCOMING,
  SCHEDULED,
  REGISTRATION_OPEN,
  IN_PROGRESS,
  COMPLETED;

  public String getDisplayName() {
    return switch (this) {
      case UPCOMING -> "Upcoming";
      case SCHEDULED -> "Scheduled";
      case REGISTRATION_OPEN -> "Registration Open";
      case IN_PROGRESS -> "In Progress";
      case COMPLETED -> "Completed";
    };
  }

  @Override
  public String toString() {
    return getDisplayName();
  }

  public static TournamentStatus fromDisplayName(String text) {
    if (text == null) return null;
    for (TournamentStatus s : TournamentStatus.values()) {
      if (s.getDisplayName().equalsIgnoreCase(text)
          || s.name().equalsIgnoreCase(text.replace(" ", "_"))) {
        return s;
      }
    }
    return null;
  }
}
