package suzumiya.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.io.Serializable;
import java.util.List;

@Mapper
public interface AuthorityMapper {

    List<String> getAuthorityByUserId(@Param("userId") Serializable userId);
}
