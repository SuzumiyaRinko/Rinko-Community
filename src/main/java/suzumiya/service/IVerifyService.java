package suzumiya.service;

import suzumiya.model.vo.VerifyCodeVO;

import java.io.IOException;

public interface IVerifyService {

    VerifyCodeVO getVerifyCode() throws IOException;
}
