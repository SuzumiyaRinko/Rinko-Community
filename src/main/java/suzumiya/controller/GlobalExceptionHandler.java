package suzumiya.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import suzumiya.model.vo.BaseResponse;
import suzumiya.util.ResponseGenerator;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public BaseResponse<Object> exceptionHandler(Exception ex) {
        log.error("控制层抛出异常：{}", ex.getMessage());
        ex.printStackTrace();
        return ResponseGenerator.returnError(-1, ex.getMessage());
    }
}
