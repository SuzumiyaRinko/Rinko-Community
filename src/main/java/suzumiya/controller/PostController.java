package suzumiya.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import suzumiya.model.dto.PostInsertDTO;
import suzumiya.model.dto.PostSearchDTO;
import suzumiya.model.pojo.Post;
import suzumiya.model.vo.BaseResponse;
import suzumiya.model.vo.PostSearchVO;
import suzumiya.service.IPostService;
import suzumiya.util.ResponseGenerator;

import java.util.Set;

@RestController
@RequestMapping("/post")
@Slf4j
public class PostController {

    @Autowired
    private IPostService postService;

    @PostMapping("/insert")
    public BaseResponse<Long> insert(@RequestBody PostInsertDTO postInsertDTO) {
        Long postId = postService.insert(postInsertDTO);
        return ResponseGenerator.returnOK("新增post成功", postId);
    }

    @DeleteMapping("/delete/{postId}")
    public BaseResponse<Object> delete(@PathVariable("postId") Long postId) {
        postService.delete(postId);
        return ResponseGenerator.returnOK("删除post成功", null);
    }

//    @PutMapping("/update")
//    public BaseResponse<Object> update(@RequestBody PostUpdateDTO postUpdateDTO) {
//        postService.update(postUpdateDTO);
//        return ResponseGenerator.returnOK("更新post成功", null);
//    }

    @GetMapping("/search")
    public BaseResponse<PostSearchVO> search(PostSearchDTO postSearchDTO) throws NoSuchFieldException, IllegalAccessException {
        log.debug("PostController.search.postSearchDTO: {}", postSearchDTO);
        PostSearchVO postSearchVO = postService.search(postSearchDTO);
        return ResponseGenerator.returnOK("查询post成功", postSearchVO);
    }

    @GetMapping("/search/suggestions")
    public BaseResponse<Set<String>> suggest(String searchKey) throws NoSuchFieldException, IllegalAccessException {
        Set<String> suggestions = postService.suggest(searchKey);
        return ResponseGenerator.returnOK("联想查询成功", suggestions);
    }

    @PostMapping("/like/{postId}")
    public BaseResponse<Object> like(@PathVariable("postId") Long postId) {
        postService.like(postId);
        return ResponseGenerator.returnOK("点赞/取消点赞 成功", null);
    }

    @PostMapping("/collect/{postId}")
    public BaseResponse<Object> collect(@PathVariable("postId") Long postId) {
        postService.collect(postId);
        return ResponseGenerator.returnOK("收藏/取消收藏 成功", null);
    }

    @GetMapping("/hasLike/{postId}")
    public BaseResponse<Boolean> hasLike(@PathVariable("postId") Long postId) {
        Boolean hasLike = postService.hasLike(postId);
        return ResponseGenerator.returnOK("查询成功", hasLike);
    }

    @GetMapping("/hasCollect/{postId}")
    public BaseResponse<Boolean> hasCollect(@PathVariable("postId") Long postId) {
        Boolean hasCollect = postService.hasCollect(postId);
        return ResponseGenerator.returnOK("查询成功", hasCollect);
    }

    @GetMapping("/collections/{pageNum}")
    public BaseResponse<PostSearchVO> getCollections(@PathVariable("pageNum") Integer pageNum) {
        PostSearchVO postSearchVO = postService.getCollections(pageNum);
        return ResponseGenerator.returnOK("查询收藏列表成功", postSearchVO);
    }

    @GetMapping("/feeds/{pageNum}")
    public BaseResponse<PostSearchVO> getFeeds(@PathVariable("pageNum") Integer pageNum) {
        PostSearchVO postSearchVO = postService.getFeeds(pageNum);
        return ResponseGenerator.returnOK("查询收藏列表成功", postSearchVO);
    }

    @GetMapping("/{postId}")
    public BaseResponse<Post> getPostByPostId(@PathVariable("postId") Long postId) {
        Post post = postService.getPostByPostId(postId);
        return ResponseGenerator.returnOK("查询post成功", post);
    }

    @GetMapping("/getPostByCommentId/{commentId}")
    public BaseResponse<Post> getPostByCommentId(@PathVariable("commentId") Long commentId) {
        Post post = postService.getPostByCommentId(commentId);
        return ResponseGenerator.returnOK("post成功", post);
    }
}
