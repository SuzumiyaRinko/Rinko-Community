package suzumiya.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import suzumiya.model.vo.BaseResponse;
import suzumiya.model.vo.VerifyCodeVO;
import suzumiya.service.IVerifyService;
import suzumiya.util.ResponseGenerator;

import java.io.IOException;

@RestController
@RequestMapping("/verifyCode")
public class KaptchaController {

    @Autowired
    private IVerifyService verifyService;

    @GetMapping
    public BaseResponse<VerifyCodeVO> getVerifyCode() throws IOException {
        VerifyCodeVO verifyCode = verifyService.getVerifyCode();
        return ResponseGenerator.returnOK("成功生成验证码", verifyCode);
    }
}
