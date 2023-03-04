package suzumiya.controller;

import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import suzumiya.model.dto.CommentInsertDTO;
import suzumiya.model.dto.CommentSelectDTO;
import suzumiya.model.pojo.Comment;
import suzumiya.model.vo.BaseResponse;
import suzumiya.service.ICommentService;
import suzumiya.util.ResponseGenerator;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    @PostMapping
    public BaseResponse<Long> comment(@RequestBody CommentInsertDTO commentInsertDTO) {
        Long commentId = commentService.comment(commentInsertDTO);
        return ResponseGenerator.returnOK("评论成功", commentId);
    }

    @DeleteMapping("/delete/{commentId}")
    public BaseResponse<Object> delete(@PathVariable("commentId") Long commentId) {
        commentService.delete(commentId);
        return ResponseGenerator.returnOK("删除评论成功", null);
    }

    @GetMapping("/{commentId}")
    public BaseResponse<Comment> getCommentByCommentId(@PathVariable("commentId") Long commentId) {
        Comment comment = commentService.getCommentByCommentId(commentId);
        return ResponseGenerator.returnOK("查询comment成功", comment);
    }

    @GetMapping
    public BaseResponse<PageInfo<Comment>> select(CommentSelectDTO commentSelectDTO) {
        PageInfo<Comment> pageInfo = commentService.select(commentSelectDTO);
        return ResponseGenerator.returnOK("查询评论成功", pageInfo);
    }

    @PostMapping("/like/{commentId}")
    public BaseResponse<Object> like(@PathVariable("commentId") Long commentId) {
        commentService.like(commentId);
        return ResponseGenerator.returnOK("点赞/取消点赞 成功", null);
    }

    @GetMapping("/hasLike/{commentId}")
    public BaseResponse<Boolean> hasLike(@PathVariable("commentId") Long commentId) {
        Boolean hasLike = commentService.hasLike(commentId);
        return ResponseGenerator.returnOK("查询成功", hasLike);
    }
}
