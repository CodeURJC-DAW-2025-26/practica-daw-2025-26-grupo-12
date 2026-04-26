package es.codeurjc.grupo12.scissors_please.controller.api.v1.chart;

import es.codeurjc.grupo12.scissors_please.repository.UserRepository.MonthlyUserCount;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("apiChartController")
@RequestMapping("/api/v1/charts")
@Tag(name = "Charts", description = "Endpoints that return chart data in JSON format")
public class ChartController {

  public record ResultsChartDto(int wins, int losses, int draws) {}

  public record ProgressDto(int current, int max) {}

  @GetMapping("/results")
  @Operation(
      summary = "Get results data",
      description = "Returns wins, losses and draws to build a pie chart in frontend.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Data returned successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ResultsChartDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid query parameters")
      })
  public ResponseEntity<ResultsChartDto> getResults(
      @Parameter(description = "Number of wins", example = "12") @RequestParam int wins,
      @Parameter(description = "Number of losses", example = "4") @RequestParam int losses,
      @Parameter(description = "Number of draws", example = "2") @RequestParam int draws) {

    return ResponseEntity.ok(new ResultsChartDto(wins, losses, draws));
  }

  @GetMapping("/elo")
  @Operation(
      summary = "Get ELO history data",
      description = "Returns a list of ELO values to build a line chart.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Data returned successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(schema = @Schema(type = "integer")))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters")
      })
  public ResponseEntity<List<Integer>> getEloData(@RequestParam List<Integer> eloHistory) {

    return ResponseEntity.ok(eloHistory);
  }

  @GetMapping("/users")
  @Operation(
      summary = "Get user history data",
      description = "Returns monthly user counts to build a bar chart.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Data returned successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array =
                        @ArraySchema(schema = @Schema(implementation = MonthlyUserCount.class)))),
        @ApiResponse(responseCode = "400", description = "Invalid query parameters")
      })
  public ResponseEntity<List<MonthlyUserCount>> getUserHistory(
      @RequestParam(required = false) List<String> monthlyDataPlaceholder) {

    return ResponseEntity.ok(java.util.Collections.emptyList());
  }

  @GetMapping("/progress")
  @Operation(
      summary = "Get progress data",
      description = "Returns current and max values to build a progress bar.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Data returned successfully",
            content =
                @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProgressDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid query parameters")
      })
  public ResponseEntity<ProgressDto> getProgress(
      @Parameter(description = "Current progress value", example = "75") @RequestParam int current,
      @Parameter(description = "Maximum progress value", example = "100") @RequestParam int max) {

    return ResponseEntity.ok(new ProgressDto(current, max));
  }
}
