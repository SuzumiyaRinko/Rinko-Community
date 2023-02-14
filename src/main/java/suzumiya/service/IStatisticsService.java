package suzumiya.service;

import suzumiya.model.dto.StatisticsQueryDTO;

public interface IStatisticsService {

    Long getRangeUV(StatisticsQueryDTO statisticsDTO);
}
