package suzumiya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import suzumiya.model.dto.PostSearchDTO;
import suzumiya.model.pojo.Post;
import suzumiya.model.vo.BaseResponse;
import suzumiya.model.vo.PostSearchVO;
import suzumiya.service.IPostService;
import suzumiya.util.ResponseGenerator;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private IPostService postService;

    @PostMapping("/insert")
    public void insert(@RequestBody Post post) {
        postService.insert(post);
    }

    @GetMapping("/search")
    public BaseResponse<PostSearchVO> search(PostSearchDTO postSearchDTO) throws NoSuchFieldException, IllegalAccessException {
        PostSearchVO postSearchVO = postService.search(postSearchDTO);
        return ResponseGenerator.returnOK("查询post成功", postSearchVO);
    }
}
