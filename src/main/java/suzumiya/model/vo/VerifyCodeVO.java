package suzumiya.model.vo;

import lombok.Data;

@Data
public class VerifyCodeVO {

    private String uuid;
    private String code;
    private String base64Img;
}
