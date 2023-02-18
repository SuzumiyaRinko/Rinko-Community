package suzumiya.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

//配置类
@Configuration
public class WebSocketConfig {

    //注入ServerEndpointExporter bean对象，自动注册使用注解@ServerEndpoint的bean
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

//    /**
//     * 因 SpringBoot WebSocket 对每个客户端连接都会创建一个 WebSocketServer（@ServerEndpoint 注解对应的） 对象，Bean 注入操作会被直接略过，因而手动注入一个全局变量
//     *
//     */
//    @Autowired
//    public void setMessageService(MessageService messageService) {
//        ChatEndPoint.messageService = messageService;
//
//    }
}