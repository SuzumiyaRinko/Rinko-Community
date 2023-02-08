package suzumiya.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import suzumiya.model.pojo.Hotel;

@Mapper
public interface HotelMapper extends BaseMapper<Hotel> {
}
