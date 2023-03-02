package suzumiya.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.pool2.ObjectPool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.io.*;
import java.util.UUID;

@Component
@Slf4j
public class TestFTPUtils {
    /**
     * ftpClient连接池初始化标志
     */
    private static volatile boolean hasInit = false;
    /**
     * ftpClient连接池
     */
    private static ObjectPool<FTPClient> ftpClientPool;


    private static String encoding;

    @Value("${ftp.encoding}")
    public void setEncoding(String encoding) {
        TestFTPUtils.encoding = encoding;
    }

    public static void init(ObjectPool<FTPClient> ftpClientPool) {
        if (!hasInit) {
            synchronized (TestFTPUtils.class) {
                if (!hasInit) {
                    TestFTPUtils.ftpClientPool = ftpClientPool;
                    hasInit = true;
                }
            }
        }
    }

    private static FTPClient getFtpClient() {
        checkFtpClientPoolAvailable();
        FTPClient ftpClient = null;
        Exception ex = null;
        for (int i = 0; i < 3; i++) {
            try {
                ftpClient = ftpClientPool.borrowObject();
                ftpClient.changeWorkingDirectory("/");
                break;
            } catch (Exception e) {
                ex = e;
            }
        }
        if (ftpClient == null) {
            throw new RuntimeException("Could not get a ftpClient from the pool", ex);
        }
        return ftpClient;
    }

    // 上传
    public static String uploadFile(String fileName, InputStream inputStream) throws IOException {
        FTPClient ftp = getFtpClient();
        String finalName = null;
        try {
//            ftpClient.changeWorkingDirectory(directory);
//            ftpClient.enterLocalPassiveMode();
            // 生成最终文件名
            String suffix = fileName.substring(fileName.lastIndexOf("."));
            finalName = UUID.randomUUID() + suffix;
            ftp.storeFile(finalName, inputStream);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            inputStream.close();
            releaseFtpClient(ftp);
        }
//        return "/ftp/" + directory + "/" + finalName;
        return "/ftp/" + finalName;
    }

    // 删除
    public static void deleteFile(String remotePath) {
        FTPClient ftpClient = getFtpClient();
        try {
            String finalPath = remotePath.substring("/ftp".length());
            ftpClient.deleteFile(new String(finalPath.getBytes(encoding), "ISO-8859-1"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            releaseFtpClient(ftpClient);
        }
    }

    public static void deleteFile(String path, String remotePath) {
        FTPClient ftpClient = getFtpClient();
        try {
//            ftpClient.changeWorkingDirectory(path);
            ftpClient.deleteFile(new String(remotePath.getBytes(encoding), "ISO-8859-1"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            releaseFtpClient(ftpClient);
        }
    }

    public static String[] retrieveFTPFiles(String remotePath) throws IOException {
        FTPClient ftpClient = getFtpClient();
        try {
            ftpClient.setControlEncoding(encoding);
            ftpClient.changeWorkingDirectory(remotePath);
            return ftpClient.listNames();
        } finally {
            releaseFtpClient(ftpClient);
        }
    }

    public static FTPFile[] getFTPFiles() throws IOException {
        FTPClient ftpClient = getFtpClient();
        try {
            return ftpClient.listFiles();
        } finally {
            releaseFtpClient(ftpClient);
        }
    }

    public static void deleteFTPFiles(String remotePath, FTPClient ftpClient) {
        try {
            ftpClient.deleteFile(new String(remotePath.getBytes(encoding), "ISO-8859-1"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void releaseFtpClient(FTPClient ftpClient) {
        if (ftpClient == null) {
            return;
        }

        try {
            ftpClientPool.returnObject(ftpClient);
        } catch (Exception e) {
            log.error("Could not return the ftpClient to the pool", e);
            // destoryFtpClient
            if (ftpClient.isAvailable()) {
                try {
                    ftpClient.disconnect();
                } catch (IOException io) {
                }
            }
        }
    }


    public static byte[] getFileBytesByName(String ftpPath, String fileName) {
        // 登录
        FTPClient ftpClient = getFtpClient();
        //创建byte数组输出流
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try {
            ftpClient.changeWorkingDirectory(ftpPath);
            InputStream is = ftpClient.retrieveFileStream(new String(fileName.getBytes(encoding), "ISO-8859-1"));
            byte[] buffer = new byte[1024 * 1024 * 4];
            int len = -1;
            while ((len = is.read(buffer, 0, 1024 * 1024 * 4)) != -1) {
                byteStream.write(buffer, 0, len);
            }
            is.close();
            ftpClient.completePendingCommand();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            releaseFtpClient(ftpClient);
        }
        return byteStream.toByteArray();
    }

    public static void downloadFiles(String ftpPath, String savePath) {

        FTPClient ftpClient = getFtpClient();
        try {
            // 判断是否存在该目录
            if (!ftpClient.changeWorkingDirectory(ftpPath)) {
                return;
            }
            ftpClient.enterLocalPassiveMode();  // 设置被动模式，开通一个端口来传输数据
            String[] fs = ftpClient.listNames();
            // 判断该目录下是否有文件
            if (fs == null || fs.length == 0) {
                return;
            }
            File files = new File(savePath);
            if (!files.exists()) {
                files.mkdir();
            }
            for (String ff : fs) {
                //String ftpName = new String(ff.getBytes("UTF-8"),encoding);
                File file = new File(savePath + '/' + ff);
                try (OutputStream os = new FileOutputStream(file)) {
                    ftpClient.retrieveFile(ff, os);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            releaseFtpClient(ftpClient);
        }
    }


    private static void checkFtpClientPoolAvailable() {
        Assert.state(hasInit, "FTP未启用或连接失败！");
    }
}
