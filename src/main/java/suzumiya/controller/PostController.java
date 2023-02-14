package suzumiya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import suzumiya.model.dto.PostInsertDTO;
import suzumiya.model.dto.PostSearchDTO;
import suzumiya.model.dto.PostUpdateDTO;
import suzumiya.model.pojo.Post;
import suzumiya.model.vo.BaseResponse;
import suzumiya.model.vo.PostSearchVO;
import suzumiya.service.IPostService;
import suzumiya.util.ResponseGenerator;

import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {

    @Autowired
    private IPostService postService;

    @PostMapping("/insert")
    public BaseResponse<Object> insert(@RequestBody PostInsertDTO postInsertDTO) {
        postService.insert(postInsertDTO);
        return ResponseGenerator.returnOK("新增post成功", null);
    }

    @DeleteMapping("/delete/{postId}")
    public BaseResponse<Object> delete(@PathVariable("postId") Long postId) {
        postService.delete(postId);
        return ResponseGenerator.returnOK("删除post成功", null);
    }

    @PutMapping("/update")
    public BaseResponse<Object> update(@RequestBody PostUpdateDTO postUpdateDTO) {
        postService.update(postUpdateDTO);
        return ResponseGenerator.returnOK("更新post成功", null);
    }

    @GetMapping("/search")
    public BaseResponse<PostSearchVO> search(PostSearchDTO postSearchDTO) throws NoSuchFieldException, IllegalAccessException {
        PostSearchVO postSearchVO = postService.search(postSearchDTO);
        return ResponseGenerator.returnOK("查询post成功", postSearchVO);
    }

    @GetMapping("/search/suggestions")
    public BaseResponse<List<String>> suggest(String searchKey) throws NoSuchFieldException, IllegalAccessException {
        List<String> suggestions = postService.suggest(searchKey);
        return ResponseGenerator.returnOK("", suggestions);
    }

    @PostMapping("/like")
    public BaseResponse<Object> like(@RequestBody Long postId) {
        postService.like(postId);
        return ResponseGenerator.returnOK("点赞post成功", null);
    }

    @PostMapping("/collect")
    public BaseResponse<Object> collect(@RequestBody Long postId) {
        postService.collect(postId);
        return ResponseGenerator.returnOK("收藏post成功", null);
    }

    @GetMapping("/{postId}")
    public BaseResponse<Post> getPostByPostId(@PathVariable("postId") Long postId) {
        Post post = postService.getPostByPostId(postId);
        return ResponseGenerator.returnOK("查询post成功", post);
    }
}
