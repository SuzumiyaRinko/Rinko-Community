package suzumiya.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IFileService {

    String uploadAvatar(MultipartFile multipartFile) throws IOException;
}
