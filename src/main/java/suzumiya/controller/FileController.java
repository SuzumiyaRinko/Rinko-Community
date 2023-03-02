package suzumiya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import suzumiya.model.vo.BaseResponse;
import suzumiya.service.IFileService;
import suzumiya.util.ResponseGenerator;

import java.io.IOException;

@RestController
@RequestMapping("/file")
public class FileController {

    @Autowired
    private IFileService fileService;

    @PostMapping
    public BaseResponse<String> uploadFile(MultipartFile file) throws IOException {
        String filePath = fileService.uploadFile(file);
        return ResponseGenerator.returnOK("文件存储成功", filePath);
    }

    @DeleteMapping
    public BaseResponse<Object> deleteFile(@RequestBody String filePath) {
        fileService.deleteFile(filePath);
        return ResponseGenerator.returnOK("文件删除成功", null);
    }
}
