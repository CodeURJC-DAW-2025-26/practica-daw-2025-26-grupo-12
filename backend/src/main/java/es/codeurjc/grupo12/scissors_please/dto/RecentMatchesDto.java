package es.codeurjc.grupo12.scissors_please.dto;

import es.codeurjc.grupo12.scissors_please.views.UserMatchItem;
import java.util.List;

public record RecentMatchesDto(List<UserMatchItem> matches) {}
