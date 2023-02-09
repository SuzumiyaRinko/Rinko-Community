package suzumiya;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.annotation.PostConstruct;

@SpringBootApplication
@MapperScan(basePackages = "suzumiya.mapper")
//@EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
@EnableAspectJAutoProxy(exposeProxy = true)
public class Application {

    @PostConstruct
    public void init() {
        // 解决Redis和ES的netty启动冲突问题
        // see Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
