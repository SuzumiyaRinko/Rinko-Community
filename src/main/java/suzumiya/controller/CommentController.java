package suzumiya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import suzumiya.model.dto.CommentInsertDTO;
import suzumiya.model.dto.CommentSelectDTO;
import suzumiya.model.vo.BaseResponse;
import suzumiya.service.ICommentService;
import suzumiya.util.ResponseGenerator;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    @PostMapping
    public BaseResponse<Object> comment(@RequestBody CommentInsertDTO commentInsertDTO) {
        commentService.comment(commentInsertDTO);
        return ResponseGenerator.returnOK("评论成功", null);
    }

    @DeleteMapping("/delete/{commentId}")
    public BaseResponse<Object> delete(@PathVariable("commentId") Long commentId) {
        commentService.delete(commentId);
        return ResponseGenerator.returnOK("删除评论成功", null);
    }

    @GetMapping
    public BaseResponse<Object> select(CommentSelectDTO commentSelectDTO) {
        commentService.select(commentSelectDTO);
        return ResponseGenerator.returnOK("查询评论成功", null);
    }
}
