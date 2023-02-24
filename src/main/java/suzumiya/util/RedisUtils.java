package suzumiya.util;

import suzumiya.constant.CommonConst;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class RedisUtils {

    public static double getZSetScoreBy2EpochSecond() {
        return (LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")) - CommonConst.COMMUNITY_EPOCH_SECOND) * 1.0 / 86400;
    }

    public static double getZSetScoreBy2EpochSecond(LocalDateTime base) {
        return (base.toEpochSecond(ZoneOffset.of("+8")) - CommonConst.COMMUNITY_EPOCH_SECOND) * 1.0 / 86400;
    }
}
