package suzumiya.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import suzumiya.model.pojo.Message;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    List<Message> getSystemMessagesLtId(@Param("toUserId") Long toUserId, @Param("lastId") Long lastId);

    @Select("SELECT content,is_read FROM tb_message WHERE (from_user_id = #{fromUserId} AND to_user_id = #{toUserId})" +
            "OR (from_user_id = #{toUserId} AND to_user_id = #{fromUserId}) ORDER BY create_time DESC LIMIT 0,1")
    Message getFirstMessageBy2Id(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId);

    List<Message> getChatMessagesLtId(@Param("fromUserId") Long fromUserId, @Param("toUserId") Long toUserId, @Param("lastId") Long lastId);
}
