package suzumiya.util;

import cn.hutool.core.util.StrUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.File;
import java.util.Date;
import java.util.List;

@Configuration
public class MailUtils {

    private static JavaMailSender mailSender;

    @Resource
    public void setMailSender(JavaMailSender mailSender) {
        MailUtils.mailSender = mailSender;
    }

    public static void sendMail(String from, List<String> to, String subject, Date sentDate, String text, List<File> files) throws MessagingException {
        /* 准备Message对象 */
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper messageHelper = new MimeMessageHelper(message, true, "UTF-8");
        messageHelper.setFrom(from);
        for (String t : to) {
            messageHelper.addTo(t);
        }
        messageHelper.setSubject(subject);
        if (sentDate == null) {
            messageHelper.setSentDate(new Date());
        }

        /* 添加邮件正文 */
        if (StrUtil.isNotBlank(text)) {
            messageHelper.setText(text, true);
        }

        /* 添加邮件附件 */
        if (files != null) {
            for (File file : files) {
                messageHelper.addAttachment(file.getName(), file);
            }
        }

        /* 发送邮件 */
        mailSender.send(message);
    }
}
