package suzumiya.service.impl;

import cn.hutool.core.codec.Base64Encoder;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import suzumiya.constant.CommonConst;
import suzumiya.constant.RedisConst;
import suzumiya.model.vo.VerifyCodeVO;
import suzumiya.service.IVerifyService;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class VerifyServiceImpl implements IVerifyService {

    @Resource
    private DefaultKaptcha kaptcha;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public VerifyCodeVO getVerifyCode() throws IOException {
        VerifyCodeVO verifyCodeVO = new VerifyCodeVO();
        /* 生成文字验证码 */
        String content = kaptcha.createText();
        /* 生成图片验证码 */
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedImage image = kaptcha.createImage(content);
        ImageIO.write(image, "jpg", outputStream);
        // 对字节数组Base64编码
        String base64Img = CommonConst.PREFIX_BASE64IMG + Base64Encoder.encode(outputStream.toByteArray()).replace("\n", "").replace("\r", "");
        /* 生成UUID */
        String uuid = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(RedisConst.USER_VERIFY_KEY + uuid, content);

        verifyCodeVO.setCode(content);
        verifyCodeVO.setBase64Img(base64Img);
        verifyCodeVO.setUuid(uuid);
        return verifyCodeVO;
    }
}
