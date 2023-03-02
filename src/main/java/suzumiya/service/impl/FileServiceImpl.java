package suzumiya.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import suzumiya.service.IFileService;
import suzumiya.util.TestFTPUtils;

import java.io.IOException;

@Service
public class FileServiceImpl implements IFileService {

    @Override
    public String uploadFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        return TestFTPUtils.uploadFile(file.getOriginalFilename(), file.getInputStream());
    }

    @Override
    public void deleteFile(String filePath) {
        TestFTPUtils.deleteFile(filePath);
    }
}
