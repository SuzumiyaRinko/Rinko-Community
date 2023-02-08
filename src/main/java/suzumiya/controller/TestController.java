package suzumiya.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import suzumiya.model.pojo.User;
import suzumiya.model.vo.BaseResponse;
import suzumiya.util.ResponseGenerator;

@RestController
@RequestMapping("/test")
@Slf4j
public class TestController {

    @GetMapping("/1")
    @PreAuthorize("hasAuthority('sys:book')")
    public BaseResponse<User> test1() {
        return ResponseGenerator.returnOK("ok", null);
    }

    @GetMapping("/2")
    @PreAuthorize("hasAuthority('sys:book:juejue')")
    public BaseResponse<User> test2() {
        return ResponseGenerator.returnOK("ok", null);
    }
}
