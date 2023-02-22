package suzumiya.util;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Configuration
public class FTPUtils {

    // IP
    @Value("${ftp.ftp_address}")
    private String FTP_ADDRESS;
    // 端口号
    @Value("${ftp.ftp_port}")
    private int FTP_PORT;
    // 用户名
    @Value("${ftp.ftp_username}")
    private String FTP_USERNAME;
    // 密码
    @Value("${ftp.ftp_password}")
    private String FTP_PASSWORD;
    // 客户端
    public static final FTPClient ftpClient = new FTPClient();

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @PostConstruct
    public void init() {
        System.out.println("FTP_ADDRESS: " + FTP_ADDRESS);
        try {
            ftpClient.connect(FTP_ADDRESS, FTP_PORT);
            ftpClient.login(FTP_USERNAME, FTP_PASSWORD);
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                ftpClient.disconnect();
            }
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.setBufferSize(1024);
            ftpClient.setControlEncoding("UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 返回文件路径，比如 /ftp/20230221/xxxxxx.png
    public static String uploadFile(String fileName, InputStream inputStream) throws IOException {
        if (!ftpClient.isConnected()) {
            return null;
        }
        String directory = LocalDate.now().format(formatter);
        String finalName = null;
        try {
//            ftpClient.changeWorkingDirectory(directory);
//            ftpClient.enterLocalPassiveMode(); // 开启了就会很慢
            // 生成最终文件名
            String suffix = fileName.substring(fileName.lastIndexOf("."));
            finalName = UUID.randomUUID() + suffix;
            ftpClient.storeFile(finalName, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            inputStream.close();
        }
//        return "/ftp/" + directory + "/" + finalName;
        return "/ftp/" + finalName;
    }
}
