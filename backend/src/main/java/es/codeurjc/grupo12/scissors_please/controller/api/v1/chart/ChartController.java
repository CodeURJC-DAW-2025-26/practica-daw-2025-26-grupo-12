package es.codeurjc.grupo12.scissors_please.controller.api.v1.chart;

import es.codeurjc.grupo12.scissors_please.config.ResponseConstants;
import es.codeurjc.grupo12.scissors_please.dto.ResponseDto;
import es.codeurjc.grupo12.scissors_please.repository.UserRepository.MonthlyUserCount;
import es.codeurjc.grupo12.scissors_please.service.chart.ChartService;
import java.awt.Color;
import java.util.Base64;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("apiChartController")
@RequestMapping("/api/v1/charts")
public class ChartController {

  @Autowired private ChartService chartService;

  private String encodeToBase64(byte[] bytes) {
    return Base64.getEncoder().encodeToString(bytes);
  }

  @GetMapping("/results")
  public ResponseDto getResultsPieChart(
      @RequestParam int wins, @RequestParam int losses, @RequestParam int draws) {

    byte[] chartBytes = chartService.generateResultsPieChart(wins, losses, draws);
    String base64Chart = encodeToBase64(chartBytes);

    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, base64Chart);
  }

  @PostMapping("/elo")
  public ResponseDto getEloLineChart(@RequestBody List<Integer> eloHistory) {
    byte[] chartBytes = chartService.generateEloLineChart(eloHistory);
    String base64Chart = encodeToBase64(chartBytes);

    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, base64Chart);
  }

  @PostMapping("/users")
  public ResponseDto getUserHistoryChart(@RequestBody List<MonthlyUserCount> monthlyData) {
    byte[] chartBytes = chartService.generateUserHistory(monthlyData);
    String base64Chart = encodeToBase64(chartBytes);

    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, base64Chart);
  }

  @GetMapping("/progress")
  public ResponseDto getProgressBar(
      @RequestParam int current,
      @RequestParam int max,
      @RequestParam(required = false, defaultValue = "34,197,94") String color) {

    String[] rgb = color.split(",");
    Color barColor =
        new Color(
            Integer.parseInt(rgb[0].trim()),
            Integer.parseInt(rgb[1].trim()),
            Integer.parseInt(rgb[2].trim()));

    byte[] chartBytes = chartService.generateProgressBar(current, max, barColor);
    String base64Chart = encodeToBase64(chartBytes);

    return new ResponseDto(false, ResponseConstants.OK_CODE_INT, ResponseConstants.OK, base64Chart);
  }
}
