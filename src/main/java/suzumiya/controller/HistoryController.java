package suzumiya.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import suzumiya.model.dto.HistorySearchDTO;
import suzumiya.model.pojo.History;
import suzumiya.model.vo.BaseResponse;
import suzumiya.service.IHistoryService;
import suzumiya.util.ResponseGenerator;

@RestController
@RequestMapping("/history")
@Slf4j
public class HistoryController {

    @Autowired
    private IHistoryService historyService;

    @PostMapping
    public BaseResponse<Object> saveHistory(@RequestBody History history) {
        historyService.saveHistory(history);
        return ResponseGenerator.returnOK("保存历史成功", null);
    }

    @GetMapping
    public BaseResponse<History> getHistory(HistorySearchDTO historySearchDTO) {
        History history = historyService.getHistory(historySearchDTO);
        return ResponseGenerator.returnOK("查询历史成功", history);
    }
}
