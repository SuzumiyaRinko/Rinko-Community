package suzumiya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import suzumiya.model.dto.StatisticsQueryDTO;
import suzumiya.model.vo.BaseResponse;
import suzumiya.service.IStatisticsService;
import suzumiya.util.ResponseGenerator;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private IStatisticsService statisticsService;

    @GetMapping("/rangeUV")
    public BaseResponse<Long> getRangeUV(StatisticsQueryDTO statisticsQueryDTO) {
        Long rangeUV = statisticsService.getRangeUV(statisticsQueryDTO);
        return ResponseGenerator.returnOK("查询区间UV成功", rangeUV);
    }
}
