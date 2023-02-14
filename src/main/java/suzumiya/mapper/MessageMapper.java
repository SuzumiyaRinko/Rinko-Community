package suzumiya.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import suzumiya.model.pojo.Message;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
