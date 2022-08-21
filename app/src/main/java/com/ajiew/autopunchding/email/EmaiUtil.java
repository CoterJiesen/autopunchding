package com.ajiew.autopunchding.email;

import static com.ajiew.autopunchding.util.AppUtil.rmScreencap;
import static com.ajiew.autopunchding.util.AppUtil.screencap;

import android.os.Environment;

import com.sun.mail.util.MailSSLSocketFactory;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 * Created by zhangxiaoming on 2018/7/30.
 */

public class EmaiUtil {


    //qq
    private static final String HOST = "smtp.163.com";
    private static final String PORT = "465";
    private static final String FROM_ADD = "coterjiesen@163.com"; //发送方邮箱
    private static final String FROM_PSW = "GDSZJDDSGZXIPVKT";//发送方邮箱授权码


    public static void sendMsg(final String msg, final String toAdress) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendText(msg,toAdress);
            }
        }).start();
    }


    public static void sendMsgImage(final String msg, final String toAdress) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendTextImage(msg,toAdress);
                    rmScreencap();
                } catch (MessagingException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static Session getSession(){
        // 获取系统属性
        Properties properties = System.getProperties();

        // 设置邮件服务器
        properties.setProperty("mail.smtp.host", HOST); //设置邮件服务器
        properties.setProperty("mail.smtp.port", PORT);//设置服务器断开
        properties.put("mail.smtp.auth", "true"); //设置auth验证

        //QQ邮箱需要加入ssl加密
        MailSSLSocketFactory sf = null;
        try {
            sf = new MailSSLSocketFactory();
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        sf.setTrustAllHosts(true);
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.ssl.socketFactory", sf);


        Session session = Session.getDefaultInstance(properties,new Authenticator(){
            public PasswordAuthentication getPasswordAuthentication()
            {
                return new PasswordAuthentication(FROM_ADD, FROM_PSW); //发件人邮件用户名、密码
            }
        });
        return session;
    }

    private static void sendText(String msg,String toAdress){
        try {
            // 创建默认的 MimeMessage 对象
            MimeMessage message = new MimeMessage(getSession());

            // Set From: 头部头字段
            message.setFrom(new InternetAddress(FROM_ADD));

            // Set To: 头部头字段
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(toAdress));

            // Set Subject: 头部头字段
            message.setSubject("通知邮件");

            // 设置消息体
            message.setText(msg);

            // 发送消息
            Transport.send(message);
            System.out.println("邮件发送成功");
        } catch (Exception mex) {
            mex.printStackTrace();
        }
    }

    private static void sendTextImage(String message,String toAdress) throws MessagingException, UnsupportedEncodingException {
        //1.创建一封邮件的实例对象
        MimeMessage msg = new MimeMessage(getSession());
        //2.设置发件人地址
        msg.setFrom(new InternetAddress(FROM_ADD));
        /**
         * 3.设置收件人地址（可以增加多个收件人、抄送、密送），即下面这一行代码书写多行
         * MimeMessage.RecipientType.TO:发送
         * MimeMessage.RecipientType.CC：抄送
         * MimeMessage.RecipientType.BCC：密送
         */
        msg.setRecipient(MimeMessage.RecipientType.TO,new InternetAddress(toAdress));
        //4.设置邮件主题
        msg.setSubject(message,"UTF-8");

        //下面是设置邮件正文
        //msg.setContent("简单的纯文本邮件！", "text/html;charset=UTF-8");

        // 5. 创建图片"节点"
        MimeBodyPart image = new MimeBodyPart();
        // 读取本地文件
        DataHandler dh = new DataHandler(new FileDataSource(Environment.getExternalStorageDirectory().getPath()+"/screen.png"));
        // 将图片数据添加到"节点"
        image.setDataHandler(dh);
        // 为"节点"设置一个唯一编号（在文本"节点"将引用该ID）
        image.setContentID("mailTestPic");

        // 6. 创建文本"节点"
        MimeBodyPart text = new MimeBodyPart();
        // 这里添加图片的方式是将整个图片包含到邮件内容中, 实际上也可以以 http 链接的形式添加网络图片
        text.setContent("打卡截图：<br/><a href=''><img src='cid:mailTestPic'/></a>", "text/html;charset=UTF-8");

        // 7. （文本+图片）设置 文本 和 图片"节点"的关系（将 文本 和 图片"节点"合成一个混合"节点"）
        MimeMultipart mm_text_image = new MimeMultipart();
        mm_text_image.addBodyPart(text);
        mm_text_image.addBodyPart(image);
        mm_text_image.setSubType("related");    // 关联关系

        // 8. 将 文本+图片 的混合"节点"封装成一个普通"节点"
        // 最终添加到邮件的 Content 是由多个 BodyPart 组成的 Multipart, 所以我们需要的是 BodyPart,
        // 上面的 mailTestPic 并非 BodyPart, 所有要把 mm_text_image 封装成一个 BodyPart
        MimeBodyPart text_image = new MimeBodyPart();
        text_image.setContent(mm_text_image);

//        // 9. 创建附件"节点"
//        MimeBodyPart attachment = new MimeBodyPart();
//        // 读取本地文件
//        DataHandler dh2 = new DataHandler(new FileDataSource("src\\mailTestDoc.docx"));
//        // 将附件数据添加到"节点"
//        attachment.setDataHandler(dh2);
//        // 设置附件的文件名（需要编码）
//        attachment.setFileName(MimeUtility.encodeText(dh2.getName()));

        // 10. 设置（文本+图片）和 附件 的关系（合成一个大的混合"节点" / Multipart ）
        MimeMultipart mm = new MimeMultipart();
        mm.addBodyPart(text_image);
//        mm.addBodyPart(attachment);     // 如果有多个附件，可以创建多个多次添加
        mm.setSubType("mixed");         // 混合关系

        // 11. 设置整个邮件的关系（将最终的混合"节点"作为邮件的内容添加到邮件对象）
        msg.setContent(mm);
        //设置邮件的发送时间,默认立即发送
        msg.setSentDate(new Date());
        Transport.send(msg);
    }

}
