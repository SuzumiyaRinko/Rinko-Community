package suzumiya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import suzumiya.model.pojo.Page;
import suzumiya.model.pojo.Post;
import suzumiya.model.vo.BaseResponse;
import suzumiya.service.IPostService;

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
    public BaseResponse<Page<Post>> search() {

        return null;
    }

}
