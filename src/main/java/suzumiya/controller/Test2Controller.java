package suzumiya.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import suzumiya.model.pojo.TestConvert;

@RestController
@RequestMapping("/test2")
@Slf4j
public class Test2Controller {

    @GetMapping
    public TestConvert testGet(TestConvert testConvert) {
        System.out.println(testConvert);
        return testConvert;
    }

    @PostMapping
    public TestConvert testPost(@RequestBody TestConvert testConvert) {
        System.out.println(testConvert);
        return testConvert;
    }
}
