package es.codeurjc.grupo12.scissors_please.controller.api.v1.chart;

import es.codeurjc.grupo12.scissors_please.repository.UserRepository.MonthlyUserCount;
import es.codeurjc.grupo12.scissors_please.service.chart.ChartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.awt.Color;
import java.util.Base64;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("apiChartController")
@RequestMapping("/api/v1/charts")
@Tag(name = "Charts", description = "Endpoints that generate charts encoded as Base64 strings")
public class ChartController {

  private final ChartService chartService;

  public ChartController(ChartService chartService) {
    this.chartService = chartService;
  }

  private String encodeToBase64(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }

  @GetMapping("/results")
  @Operation(
      summary = "Generate a results pie chart",
      description =
          "Returns a pie chart encoded as Base64 using the provided wins, losses and draws.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Chart generated successfully",
            content =
                @Content(
                    mediaType = "text/plain",
                    schema = @Schema(type = "string", example = "iVBORw0KGgoAAAANSUhEUgAA..."))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid query parameters",
            content = @Content)
      })
  public ResponseEntity<String> getResultsPieChart(
      @Parameter(description = "Number of wins", example = "12") @RequestParam int wins,
      @Parameter(description = "Number of losses", example = "4") @RequestParam int losses,
      @Parameter(description = "Number of draws", example = "2") @RequestParam int draws) {

    byte[] chartBytes = chartService.generateResultsPieChart(wins, losses, draws);
    String base64Chart = encodeToBase64(chartBytes);
    return ResponseEntity.ok(base64Chart);
  }

  @GetMapping("/elo")
  @Operation(
      summary = "Generate an ELO line chart",
      description = "Returns a line chart encoded as Base64 from a list of ELO values.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Chart generated successfully",
            content =
                @Content(
                    mediaType = "text/plain",
                    schema = @Schema(type = "string", example = "iVBORw0KGgoAAAANSUhEUgAA..."))),
        @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
      })
  public ResponseEntity<String> getEloLineChart(
      @RequestBody
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      array = @ArraySchema(schema = @Schema(type = "integer", example = "1500"))))
          List<Integer> eloHistory) {
    byte[] chartBytes = chartService.generateEloLineChart(eloHistory);
    String base64Chart = encodeToBase64(chartBytes);
    return ResponseEntity.ok(base64Chart);
  }

  @GetMapping("/users")
  @Operation(
      summary = "Generate a user history chart",
      description =
          "Returns a bar chart encoded as Base64 from monthly user count data grouped by year and month.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Chart generated successfully",
            content =
                @Content(
                    mediaType = "text/plain",
                    schema = @Schema(type = "string", example = "iVBORw0KGgoAAAANSUhEUgAA..."))),
        @ApiResponse(responseCode = "400", description = "Invalid request body", content = @Content)
      })
  public ResponseEntity<String> getUserHistoryChart(
      @RequestBody
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              required = true,
              content =
                  @Content(
                      mediaType = "application/json",
                      array =
                          @ArraySchema(schema = @Schema(implementation = MonthlyUserCount.class))))
          List<MonthlyUserCount> monthlyData) {
    byte[] chartBytes = chartService.generateUserHistory(monthlyData);
    String base64Chart = encodeToBase64(chartBytes);
    return ResponseEntity.ok(base64Chart);
  }

  @GetMapping("/progress")
  @Operation(
      summary = "Generate a progress bar",
      description =
          "Returns a progress bar encoded as Base64 using a current value, a maximum value and an optional RGB color.")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Chart generated successfully",
            content =
                @Content(
                    mediaType = "text/plain",
                    schema = @Schema(type = "string", example = "iVBORw0KGgoAAAANSUhEUgAA..."))),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid query parameters",
            content = @Content)
      })
  public ResponseEntity<String> getProgressBar(
      @Parameter(description = "Current progress value", example = "75") @RequestParam int current,
      @Parameter(description = "Maximum progress value", example = "100") @RequestParam int max,
      @Parameter(description = "Optional RGB color in the format R,G,B", example = "34,197,94")
          @RequestParam(required = false, defaultValue = "34,197,94")
          String color) {

    String[] rgb = color.split(",");
    Color barColor =
        new Color(
            Integer.parseInt(rgb[0].trim()),
            Integer.parseInt(rgb[1].trim()),
            Integer.parseInt(rgb[2].trim()));

    byte[] chartBytes = chartService.generateProgressBar(current, max, barColor);
    String base64Chart = encodeToBase64(chartBytes);
    return ResponseEntity.ok(base64Chart);
  }
}
