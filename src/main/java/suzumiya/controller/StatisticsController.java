package suzumiya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import suzumiya.model.dto.StatisticsDTO;
import suzumiya.model.vo.BaseResponse;
import suzumiya.util.ResponseGenerator;
import suzumiya.service.IStatisticsService;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private IStatisticsService statisticsService;

    @GetMapping("/rangeUV")
    public BaseResponse<Long> getRangeUV(StatisticsDTO statisticsDTO) {
        Long rangeUV = statisticsService.getRangeUV(statisticsDTO);
        return ResponseGenerator.returnOK("查询区间UV成功", rangeUV);
    }
}
