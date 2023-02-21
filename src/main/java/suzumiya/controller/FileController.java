package suzumiya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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

    @PostMapping("/upload")
    public BaseResponse<String> uploadAvatar(MultipartFile file) throws IOException {
        String path = fileService.uploadAvatar(file);
        return ResponseGenerator.returnOK("成功上传文件", path);
    }
}
