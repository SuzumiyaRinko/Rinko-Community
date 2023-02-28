package suzumiya.service;

import suzumiya.model.dto.HistorySearchDTO;
import suzumiya.model.pojo.History;

public interface IHistoryService {

    /* 查询当前用户某个post的历史 */
    History getHistory(HistorySearchDTO historySearchDTO);

    /* 存储某个PostHistory */
    void saveHistory(History history);
}
