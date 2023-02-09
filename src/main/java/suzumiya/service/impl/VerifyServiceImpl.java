package suzumiya.service.impl;

import cn.hutool.core.codec.Base64Encoder;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import org.springframework.stereotype.Service;
import suzumiya.constant.CommonConst;
import suzumiya.model.vo.VerifyCodeVO;
import suzumiya.service.IVerifyService;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class VerifyServiceImpl implements IVerifyService {

    @Resource
    private DefaultKaptcha kaptcha;

    @Override
    public VerifyCodeVO getVerifyCode() throws IOException {
        VerifyCodeVO verifyCodeVO = new VerifyCodeVO();
        /* 生成文字验证码 */
        String content = kaptcha.createText();
        verifyCodeVO.setCode(kaptcha.createText());
        /* 生成图片验证码 */
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BufferedImage image = kaptcha.createImage(content);
        ImageIO.write(image, "jpg", outputStream);
        // 对字节数组Base64编码
        String base64Img = CommonConst.PREFIX_BASE64IMG + Base64Encoder.encode(outputStream.toByteArray()).replace("\n", "").replace("\r", "");
        verifyCodeVO.setBase64Img(base64Img);

        return verifyCodeVO;
    }
}
