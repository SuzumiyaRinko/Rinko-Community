package suzumiya.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import suzumiya.mapper.UserMapper;
import suzumiya.model.pojo.User;
import suzumiya.service.IFileService;
import suzumiya.util.TestFTPUtils;

import java.io.IOException;

@Service
public class FileServiceImpl implements IFileService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public String uploadAvatar(MultipartFile multipartFile) throws IOException {
        // 获取上传的文件的文件名
        String originalFilename = multipartFile.getOriginalFilename();
        String path = TestFTPUtils.uploadFile(originalFilename, multipartFile.getInputStream());

        // 判空
        if (path == null) {
            throw new RuntimeException("文件上传失败");
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long myUserId = user.getId();
        User t = new User();
        t.setId(myUserId);
        t.setAvatar(path);
        userMapper.updateById(t);

        return path;
    }
}
