package es.codeurjc.grupo12.scissors_please.controller.web;

import es.codeurjc.grupo12.scissors_please.service.TournamentService;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

  private static final int MIN_PLAYERS = 4;
  private static final int MAX_PLAYERS = 128;
  private static final int MAX_TITLE_LENGTH = 80;
  private static final int MAX_DESCRIPTION_LENGTH = 500;
  private static final int MAX_PRIZE_LENGTH = 120;
  private static final Set<String> ALLOWED_FORMATS =
      Set.of("Single Elimination", "Double Elimination", "Round Robin");

  private final TournamentService tournamentService;

  @GetMapping("/panel")
  public String adminPanel(@PageableDefault(size = 5) Pageable pageable, Model model) {
    model.addAttribute("size", Math.max(pageable.getPageSize(), 1));
    model.addAttribute("fromItem", 0);
    model.addAttribute("toItem", 0);
    model.addAttribute("totalElements", 0);
    return "admin-panel";
  }

  @GetMapping("/panel/page")
  public String adminPanelPage(@PageableDefault(size = 5) Pageable pageable, Model model) {
    TournamentService.TournamentPage tournamentPage = tournamentService.getTournamentPage(pageable);
    model.addAttribute("tournaments", tournamentPage.tournaments());
    model.addAttribute(
        "showEmpty", pageable.getPageNumber() == 0 && tournamentPage.tournaments().isEmpty());
    model.addAttribute("nextPage", tournamentPage.nextPage());
    model.addAttribute("hasMore", tournamentPage.hasMore());
    model.addAttribute("totalElements", tournamentPage.totalElements());
    model.addAttribute("fromItem", tournamentPage.fromItem());
    model.addAttribute("toItem", tournamentPage.toItem());
    return "components/admin-tournament-page-chunk";
  }

  @GetMapping("/tournament/create")
  public String adminTournamentCreate(@RequestParam(required = false) String success, Model model) {
    if (success != null) {
      model.addAttribute("successMessage", "Tournament created successfully.");
    }
    setCreateFormModel(model, "", "", "", "", "Single Elimination", "", "");
    return "admin-tournament-create";
  }

  @PostMapping("/tournament/create")
  public String createTournament(
      @RequestParam(required = false) String adminTitle,
      @RequestParam(required = false) String adminMaxPlayers,
      @RequestParam(required = false) String adminRegistrationStart,
      @RequestParam(required = false) String adminStartDate,
      @RequestParam(required = false) String adminFormat,
      @RequestParam(required = false) String adminDescription,
      @RequestParam(required = false) String adminPrize,
      Model model) {
    List<String> errors = new ArrayList<>();

    String title = safeTrim(adminTitle);
    String maxPlayersRaw = safeTrim(adminMaxPlayers);
    String registrationStartRaw = safeTrim(adminRegistrationStart);
    String startDateRaw = safeTrim(adminStartDate);
    String format = safeTrim(adminFormat);
    String description = safeTrim(adminDescription);
    String prize = safeTrim(adminPrize);

    if (title.isBlank()) {
      errors.add("Title is required.");
    } else if (title.length() > MAX_TITLE_LENGTH) {
      errors.add("Title cannot exceed " + MAX_TITLE_LENGTH + " characters.");
    }

    Integer maxPlayers = parseMaxPlayers(maxPlayersRaw, errors);
    LocalDate registrationStart =
        parseDate("Registration opens date is invalid.", registrationStartRaw, errors);
    LocalDate startDate = parseDate("Start date is invalid.", startDateRaw, errors);

    if (!ALLOWED_FORMATS.contains(format)) {
      errors.add("Format is required.");
    }
    if (description.length() > MAX_DESCRIPTION_LENGTH) {
      errors.add("Description cannot exceed " + MAX_DESCRIPTION_LENGTH + " characters.");
    }
    if (prize.length() > MAX_PRIZE_LENGTH) {
      errors.add("Prize cannot exceed " + MAX_PRIZE_LENGTH + " characters.");
    }

    if (registrationStart != null && startDate != null && registrationStart.isAfter(startDate)) {
      errors.add("Registration opens date must be before or equal to start date.");
    }

    if (!errors.isEmpty()) {
      model.addAttribute("errorMessages", errors);
      setCreateFormModel(
          model,
          title,
          maxPlayersRaw,
          registrationStartRaw,
          startDateRaw,
          format,
          description,
          prize);
      return "admin-tournament-create";
    }

    tournamentService.createTournament(
        title, description, maxPlayers, registrationStart, startDate, format, prize);
    return "redirect:/admin/tournament/create?success";
  }

  @GetMapping("/tournament/edit")
  public String adminTournamentEdit() {
    return "admin-tournament-edit";
  }

  @GetMapping("/tournament/detail")
  public String adminTournamentDetail() {
    return "admin-tournament-detail";
  }

  private Integer parseMaxPlayers(String value, List<String> errors) {
    if (value.isBlank()) {
      errors.add("Max players is required.");
      return null;
    }
    try {
      int maxPlayers = Integer.parseInt(value);
      if (maxPlayers < MIN_PLAYERS || maxPlayers > MAX_PLAYERS) {
        errors.add("Max players must be between " + MIN_PLAYERS + " and " + MAX_PLAYERS + ".");
      }
      return maxPlayers;
    } catch (NumberFormatException ex) {
      errors.add("Max players must be a valid number.");
      return null;
    }
  }

  private LocalDate parseDate(String invalidMessage, String value, List<String> errors) {
    if (value.isBlank()) {
      errors.add(invalidMessage.replace(" is invalid.", " is required."));
      return null;
    }
    try {
      return LocalDate.parse(value);
    } catch (DateTimeParseException ex) {
      errors.add(invalidMessage);
      return null;
    }
  }

  private String safeTrim(String value) {
    return value == null ? "" : value.trim();
  }

  private void setCreateFormModel(
      Model model,
      String title,
      String maxPlayers,
      String registrationStart,
      String startDate,
      String format,
      String description,
      String prize) {
    model.addAttribute("adminTitle", title);
    model.addAttribute("adminMaxPlayers", maxPlayers);
    model.addAttribute("adminRegistrationStart", registrationStart);
    model.addAttribute("adminStartDate", startDate);
    model.addAttribute("adminDescription", description);
    model.addAttribute("adminPrize", prize);
    model.addAttribute("formatSingleSelected", "Single Elimination".equals(format));
    model.addAttribute("formatDoubleSelected", "Double Elimination".equals(format));
    model.addAttribute("formatRoundRobinSelected", "Round Robin".equals(format));
  }
}
