package es.codeurjc.grupo12.scissors_please.views;

public record TournamentRegistrationState(
    boolean registrationOpen,
    boolean hasAvailableSlots,
    boolean registrationStarted,
    boolean startsInFuture,
    boolean upcoming,
    boolean alreadyRegistered,
    boolean hasOwnedBots,
    boolean hasSelectableBots,
    boolean showJoinButton,
    int registeredParticipants,
    String joinMessage,
    String joinMessageClass) {}
