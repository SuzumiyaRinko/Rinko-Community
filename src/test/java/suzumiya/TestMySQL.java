package suzumiya;

import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import suzumiya.mapper.CommentMapper;
import suzumiya.mapper.PostMapper;
import suzumiya.mapper.TagMapper;
import suzumiya.mapper.UserMapper;
import suzumiya.model.dto.CommentSelectDTO;
import suzumiya.model.dto.MessageInsertDTO;
import suzumiya.model.pojo.Comment;
import suzumiya.model.pojo.Post;
import suzumiya.model.pojo.User;
import suzumiya.service.ICommentService;
import suzumiya.service.IMessageService;
import suzumiya.service.IPostService;
import suzumiya.service.IUserService;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@MapperScan(basePackages = "suzumiya.mapper")
// @EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@Slf4j
@EnableAspectJAutoProxy(exposeProxy = true)
public class TestMySQL {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private IUserService userService;

    @Autowired
    private IPostService postService;

    @Autowired
    private ICommentService commentService;

    @Autowired
    private IMessageService messageService;

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private TagMapper tagMapper;

    @Test
    void testMySQL() {
        User user = userMapper.getUserById(1L);
        System.out.println(user);
    }

    @Test
    void testInsert() {
        User user = new User();
        user.setUsername("123");
        user.setPassword("456");
        user.setNickname("juejue");
        userService.save(user);
        System.out.println(user);
    }

    // 增量更新
    @Test
    void testUpdate() {
        User user = new User();
        user.setId(1L);
        user.setIsFamous(true);
        userService.updateById(user);
    }

    // 测试逻辑删除
    @Test
    void testTableLogic() {
//        System.out.println(.getById(5));
    }

    @Test
    void testTagMapper() {
        List<String> names = tagMapper.getAllNameByTagIDs(List.of(1, 2, 4));
        System.out.println(names);
    }

    @Test
    void testUserMapper() {
        System.out.println(Boolean.TRUE.equals(userMapper.getIsFamousByUserId(114514L)));
    }

    @Test
    void testPostMapper() {
        List<Long> ids = List.of(2L, 3L, 4L, 5L);
        List<Post> posts = ids.stream().map((postId) -> {
            Post t = new Post();
            t.setId(postId);
            t.setIsWonderful(true);
            return t;
        }).collect(Collectors.toList());
        postService.updateBatchById(posts);
    }

    @Test
    void testCommentService() {
        CommentSelectDTO commentSelectDTO = new CommentSelectDTO();
        commentSelectDTO.setTargetType(CommentSelectDTO.TARGET_TYPE_POST);
        commentSelectDTO.setTargetId(1L);
        commentSelectDTO.setSelectType(CommentSelectDTO.SELECT_TYPE_DEFAULT);
        commentSelectDTO.setSortType(CommentSelectDTO.SORT_TYPE_REVERSE);
        commentSelectDTO.setPageNum(1);

        PageInfo<Comment> select = commentService.select(commentSelectDTO);
        int pageNum = select.getPageNum();
        System.out.println("pageNum = " + pageNum);
        int pageSize = select.getPageSize();
        System.out.println("pageSize = " + pageSize);
        long total = select.getTotal();
        System.out.println("total = " + total);

        List<Comment> list = select.getList();
        for (Comment comment : list) {
            System.out.println(comment);
        }
    }

    @Test
    void testgetFirst3Comments() {
        List<String> first3comments = commentMapper.getFirst3CommentsByTargetId(1L);
        System.out.println(first3comments);

        first3comments = commentMapper.getFirst3CommentsByTargetId(2L);
        System.out.println(first3comments);
    }

    @Test
    void testSendMessage() {
        MessageInsertDTO messageInsertDTO = new MessageInsertDTO();
        messageInsertDTO.setContent("juejue");
        messageInsertDTO.setIsSystem(true);
        messageService.sendMessage(messageInsertDTO);
    }

    @Test
    void testTop10() {
        List<Post> top10PostWithIdAndIsWonderful = postMapper.getTop10PostWithIdAndIsWonderful();
        for (Post post : top10PostWithIdAndIsWonderful) {
            System.out.println(post);
        }
    }

    @Test
    void testCommentMapper() {
        commentMapper.deleteCommentByTargetTypeAndTargetId(2, 1L);
    }
}
