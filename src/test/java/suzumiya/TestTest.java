//package suzumiya;
//
//import cn.hutool.core.bean.BeanUtil;
//import cn.hutool.core.codec.Base64Encoder;
//import cn.hutool.crypto.digest.MD5;
//import cn.hutool.dfa.WordTree;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.github.benmanes.caffeine.cache.Cache;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.mybatis.spring.annotation.MapperScan;
//import org.quartz.SchedulerException;
//import org.springframework.amqp.core.Message;
//import org.springframework.amqp.core.MessageBuilder;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.annotation.EnableAspectJAutoProxy;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.dao.DataAccessException;
//import org.springframework.data.redis.connection.RedisConnection;
//import org.springframework.data.redis.connection.RedisStringCommands;
//import org.springframework.data.redis.core.BoundListOperations;
//import org.springframework.data.redis.core.RedisCallback;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.core.script.DefaultRedisScript;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import suzumiya.mapper.AuthorityMapper;
//import suzumiya.mapper.HotelMapper;
//import suzumiya.mapper.UserMapper;
//import suzumiya.model.dto.HotelSearchDTO;
//import suzumiya.model.pojo.Hotel;
//import suzumiya.model.pojo.User;
//import suzumiya.util.QuartzUtil;
//
//import javax.annotation.Resource;
//import javax.imageio.ImageIO;
//import java.awt.image.BufferedImage;
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDate;
//import java.time.LocalDateTime;
//import java.time.LocalTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@MapperScan(basePackages = "suzumiya.mapper")
//// @EnableElasticsearchRepositories(basePackages = "suzumiya.repository")
//@Slf4j
//@EnableAspectJAutoProxy(exposeProxy = true)
//public class TestTest {
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    @Autowired
//    private HotelDocRepository hotelDocRepository;
//
//    @Resource
//    private ElasticsearchRestTemplate esTemplate;
//
//    @Resource
//    private JavaMailSender mailSender;
//
//    @Autowired
//    private IHotelDocService hotelDocService;
//
//    @Resource
//    private ObjectMapper objectMapper;
//
//    @Resource
//    private UserMapper userMapper;
//
//    @Autowired
//    private HotelMapper hotelMapper;
//
//    @Autowired
//    private AuthorityMapper authorityMapper;
//
//    @Autowired
//    private PasswordEncoder passwordEncoder;
//
//    @Resource
//    private RabbitTemplate rabbitTemplate;
//
//    @Autowired
//    private Cache<String, Object> cache; // Caffeine
//
//    @Test
//    void testObjectMapper() throws JsonProcessingException {
//        User user = new User();
//        user.setCreateTime(LocalDateTime.now());
//
//        Map<String, Object> value = new HashMap<>();
//        BeanUtil.beanToMap(user, value, true, null); // null不会被序列化
//
//        redisTemplate.opsForHash().putAll("test:json", value);
//        redisTemplate.opsForValue().set("test:jsonValue", LocalDateTime.now());
//    }
//
//    @Test
//    void test1() {
//        User user = new User();
//        user.setId(1L);
//        user.setGender(255);
//        userMapper.updateById(user);
//    }
//
//    String htmlStr = "<html>\n" +
//            "<body>\n" +
//            "\t<h2 style=\"font-size: 48px; color: red;\">这里是邮件正文</h2>\n" +
//            "</body>\n" +
//            "</html>";
//
//    String currentDir = new File("").getAbsolutePath();
//
//    @Test
//    void testSpringMail() throws MessagingException {
//        /* 准备Message对象 */
//        MimeMessage message = mailSender.createMimeMessage();
//        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
//        messageHelper.setFrom("3233219183@qq.com");
//        messageHelper.addTo("3233219183@qq.com");
//        messageHelper.setSubject("SpringMail主题");
//        Date date = new Date();
//        date.setTime(System.currentTimeMillis() - 24 * 60 * 60 * 1000); // 发送时间显示为一天前
//        messageHelper.setSentDate(date);
//
//        /* 添加邮件正文 */
//        // setText(String text, boolean isHTML)
//        messageHelper.setText("你好", true);
//
//        /* 添加邮件附件 */
//        String filePath = currentDir + "\\src\\main\\resources\\static\\1.jpg";
//        File file = new File(filePath);
//        messageHelper.addAttachment(file.getName(), file);
//
//        /* 发送邮件 */
//        mailSender.send(message);
//    }
//
//    @Autowired
//    private DefaultKaptcha kaptcha;
//
//    @Test
//    void testKaptcha() throws IOException {
//        // 生成文字验证码
//        String content = kaptcha.createText();
//
//        // 生成图片验证码
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        ;
//        BufferedImage image = kaptcha.createImage(content);
//        ImageIO.write(image, "jpg", outputStream); // 把图片写到outputStream中
//
//        // 对字节数组Base64编码
//        String str = "data:image/jpeg;base64,";
//        String base64Img = str + Base64Encoder.encode(outputStream.toByteArray()).replace("\n", "").replace("\r", "");
//
//        System.out.println("content: " + content);
//        System.out.println("base64Img: " + base64Img);
//    }
//
//    @Test
//    void testMD5() {
//        User user = new User();
//        user.setPassword("password");
//        user.setSalt("salt114514");
//        String encryptedPassword = MD5.create().digestHex16(user.getPassword() + user.getSalt());
//        System.out.println(encryptedPassword); // 51b364c36f7a33b8
//    }
//
//    ThreadLocal<User> threadLocal = new ThreadLocal<>();
//
//    @Test
//    void testThreadLocal() throws InterruptedException {
//        User user = new User();
//        user.setUsername("juejue");
//        threadLocal.set(user);
//        User user1 = threadLocal.get();
//        System.out.println("user1: " + user1);
//
//        Thread.sleep(1000L);
//
//        new Thread(() -> {
//            User user2 = threadLocal.get();
//            System.out.println("user2: " + user2);
//        }).start();
//    }
//
//    @Test
//    void testSensitiveWordFilter() throws IOException {
//        // 获取文件
//        InputStream is = new ClassPathResource("data/sensitiveWord.txt").getInputStream();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//        // 把单词添加到List中
//        List<String> sensitiveWords = new ArrayList<>();
//        String t;
//        while ((t = reader.readLine()) != null) {
//            sensitiveWords.add(t);
//        }
//
//        WordTree wordTree = new WordTree();
//        wordTree.addWords(sensitiveWords);
//
//        //密集匹配原则：假如关键词有 ab,b，文本是abab，将匹配 [ab,b,ab]
//        //贪婪匹配（最长匹配）原则：假如关键字a,ab，最长匹配将匹配[a, ab]
//        String str = "飞机打飞机傻飞逼机傻逼";
//        List<String> matchAll = wordTree.matchAll(str, -1, false, false);
//        for (String sensitiveWord : matchAll) {
//            str = str.replaceAll(sensitiveWord, "**");
//        }
//        System.out.println(str);
//    }
//
//    @Test
//    void testRedis() throws JsonProcessingException {
//        User user1 = objectMapper.readValue((String) redisTemplate.opsForValue().get("login:user1:1"), User.class);
//        System.out.println(user1);
//        System.out.println("================");
//        User user2 = new User();
//        Map<Object, Object> entries = redisTemplate.opsForHash().entries("login:user2:1");
//        BeanUtil.fillBeanWithMap(entries, user2, null);
//        System.out.println(user2);
//    }
//
//    @Test
//    void importHotels2ES() {
//        // 到MySQL查询数据
//        List<Hotel> hotels = hotelMapper.selectList(null);
//        List<HotelDoc> hotelDocs = hotels.stream().map(HotelDoc::new).collect(Collectors.toList());
//        // 导入ES
//        esTemplate.save(hotelDocs);
//    }
//
//    @Test
//    void testES() throws NoSuchFieldException, IllegalAccessException {
//        HotelSearchDTO hotelSearchDTO = new HotelSearchDTO();
//        hotelSearchDTO.setPageNum(1);
//        hotelSearchDTO.setPageSize(10);
//
//        HotelSearchVO result = hotelDocService.search(hotelSearchDTO);
//        System.out.println(result);
//    }
//
//    @Test
//    void testPasswordEncoder() {
//        System.out.println(passwordEncoder.encode("1234"));
//    }
//
//    @Test
//    void testAuthorityMapper() {
//        List<String> authorities = authorityMapper.getAuthorityByUserId(1);
//        System.out.println(authorities);
//    }
//
//    @Test
//    void testBeanUtil() {
//
//    }
//
//    @Test
//    public void testHyperLogLog() {
//        String key1 = "test:hll:1";
//        redisTemplate.opsForHyperLogLog().add(key1, 1, 2, 3);
//        Long size1 = redisTemplate.opsForHyperLogLog().size(key1);
//        System.out.println(size1); // 3
//
//        String key2 = "test:hll:2";
//        redisTemplate.opsForHyperLogLog().add(key2, 1, 2);
//        String key3 = "test:hll:3";
//        redisTemplate.opsForHyperLogLog().add(key3, 2, 3, 4);
//        String keyUnion = "test:hll:union";
//        redisTemplate.opsForHyperLogLog().union(keyUnion, key2, key3);
//        Long size2 = redisTemplate.opsForHyperLogLog().size(keyUnion);
//        System.out.println(size2); // 4
//    }
//
//    @Test
//    void testBitMap1() {
//        String redisKey = "test:bm:01";
//
//        // 记录
//        redisTemplate.opsForValue().setBit(redisKey, 1, true);
//        redisTemplate.opsForValue().setBit(redisKey, 4, true);
//        redisTemplate.opsForValue().setBit(redisKey, 7, true);
//        redisTemplate.opsForValue().setBit(redisKey, 8, true);
//
//        // 查询
//        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0)); // false
//        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1)); // true
//        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2)); // false
//
//        // 统计
//        Object obj1 = redisTemplate.execute(new RedisCallback() {
//            @Override
//            public Object doInRedis(RedisConnection connection) throws DataAccessException {
//                // bitCount(byte[] key)
//                return connection.bitCount(redisKey.getBytes()); // 4（算的是全部）
//            }
//        });
//        Object obj2 = redisTemplate.execute(new RedisCallback() {
//            @Override
//            public Object doInRedis(RedisConnection connection) throws DataAccessException {
//                // bitCount(byte[] key, long start, long end)
//                return connection.bitCount(redisKey.getBytes(), 0, 0); // 3
//            }
//        });
//    }
//
//    @Test
//    void testBitMap2() {
//        String redisKey2 = "test:bm:02";
//        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
//        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
//        redisTemplate.opsForValue().setBit(redisKey2, 2, true);
//
//        String redisKey3 = "test:bm:03";
//        redisTemplate.opsForValue().setBit(redisKey3, 2, true);
//        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
//        redisTemplate.opsForValue().setBit(redisKey3, 4, true);
//
//        String redisKey4 = "test:bm:04";
//        redisTemplate.opsForValue().setBit(redisKey4, 4, true);
//        redisTemplate.opsForValue().setBit(redisKey4, 5, true);
//        redisTemplate.opsForValue().setBit(redisKey4, 6, true);
//
//        String redisKey = "test:bm:or";
//        Object obj = redisTemplate.execute(new RedisCallback() {
//            @Override
//            public Object doInRedis(RedisConnection connection) throws DataAccessException {
//                connection.bitOp(RedisStringCommands.BitOperation.OR,
//                        redisKey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes()); // 做或运算，并把结果存到redisKey中
//                return connection.bitCount(redisKey.getBytes()); // 7
//            }
//        });
//
//        System.out.println(obj);
//
//        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0)); // true
//        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1)); // true
//        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2)); // true
//        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3)); // true
//        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4)); // true
//        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 5)); // true
//        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 6)); // true
//    }
//
//    @Test
//    void testBoundSetOperations() {
////        redisTemplate.opsForList().rightPushAll("test:bso", 1, 2, 3, 4);
//        BoundListOperations<String, Object> listOps = redisTemplate.boundListOps("test:bso");
//        if(listOps.size() == 0) {
//            System.out.println("没有元素");
//            return;
//        }
//
//        while(listOps.size() > 0) {
//            Object obj = listOps.leftPop();
//            System.out.println(obj);
//        }
//    }
//
//    @Test
//    void testByteArr() {
//        byte[] arr1 = "1".getBytes(StandardCharsets.UTF_8);
//        byte[] arr2 = "2".getBytes(StandardCharsets.UTF_8);
//        List<byte[]> bytes = List.of(arr1, arr2);
//        byte[][] array = bytes.toArray(new byte[0][0]);
//        for(int i=0; i<=array.length-1; i++) {
//            System.out.println(new String(array[i]));
//        }
//    }
//
//    @Test
//    void testFormatter() {
//        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
//        LocalDate result1 = LocalDate.parse("2023-02-01", formatter1);
//        System.out.println(result1);
//
//        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("HH:mm:ss");
//        LocalTime result2 = LocalTime.parse("11:45:14", formatter2);
//        System.out.println(result2);
//
//        DateTimeFormatter formatter3 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime result3 = LocalDateTime.parse("2023-02-01 11:45:14", formatter3);
//        System.out.println(result3);
//    }
//
//    @Test
//    void testQuartz() throws SchedulerException {
////        JobDataMap jobDataMap = new JobDataMap();
////        jobDataMap.put("data", "fuckyou");
////        QuartzUtil.addjob("jName", "jGroup", jobDataMap, "tName", "tGroup", "0/5 * * * * ?", HelloJob.class);
////        while(true){}
//
//        QuartzUtil.deletejob("jName", "jGroup","tName", "tGroup");
//    }
//
//    @Test
//    void testCaffeine() {
//        Object check = cache.getIfPresent("key");
//        System.out.println("check = " + check);
//
//        Object value = cache.get("key", (key) -> { return "juejue"; });
//        System.out.println("value = " + value);
//
//        check = cache.getIfPresent("key");
//        System.out.println("check = " + check);
//    }
//
//    @Test
//    void testMQ() throws InterruptedException {
//        // 交换机
//        String exchange = "delay.direct";
//        // convertAndSend(String exchange, String routingKey, Object message)
//        String content1 = "msg1";
//        Message message1 = MessageBuilder.withBody(content1.getBytes(StandardCharsets.UTF_8)).setHeader("x-delay", 20000).build(); // 20s
//        rabbitTemplate.convertAndSend(exchange, "delay", message1);
//
//        Thread.sleep(1000L);
//
//        String content2 = "msg2";
//        Message message2 = MessageBuilder.withBody(content2.getBytes(StandardCharsets.UTF_8)).setHeader("x-delay", 1000).build(); // 1s
//        rabbitTemplate.convertAndSend(exchange, "delay", message2);
//
//        while(true) {}
//    }
//
//    @Test
//    void testLua() {
//        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//        redisScript.setLocation(new ClassPathResource("lua/test.lua"));
//        redisScript.setResultType(Long.class);
//        // execute(RedisScript<T> script, List<K> keys, Object... args)
//        Long result = redisTemplate.execute(redisScript, Collections.emptyList());
//        System.out.println("result = " + result);
//    }
//}
