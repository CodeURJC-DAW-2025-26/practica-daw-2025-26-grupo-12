package es.codeurjc.grupo12.scissors_please.views;

import es.codeurjc.grupo12.scissors_please.model.Tournament;
import java.util.List;

public record TournamentJoinPage(
    Tournament tournament,
    List<BotOptionView> botOptions,
    boolean canSubmit,
    int participants,
    String startDate,
    String registrationOpenDate,
    String format,
    String availabilityMessage,
    String availabilityMessageClass,
    boolean showCreateBotAction) {}
