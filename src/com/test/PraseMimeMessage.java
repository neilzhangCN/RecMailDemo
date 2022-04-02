package com.test;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SentDateTerm;
import java.io.*;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;


public class PraseMimeMessage {
    private MimeMessage mimeMessage = null;
    private String saveAttachPath = "";// 附件下载后的存放目录
    private StringBuffer bodytext = new StringBuffer();
    // 存放邮件内容的StringBuffer对象
    private String dateformat = "yy-MM-dd　HH:mm";// 默认的日前显示格式


    /**
     * 　*　PraseMimeMessage类测试 　
     *
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String downLoadPath = "e:/temp";
        String recMailAddr = "zhangxiaoning@dianmi365.com";
        String passwd = "WXrBJHXWadeKVvTP";//类似于token，不是邮箱登录密码

        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
        // Get a Properties object
        Properties props = System.getProperties();
        props.setProperty("mail.pop3.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.pop3.socketFactory.fallback", "false");
        props.setProperty("mail.pop3.port", "995");
        props.setProperty("mail.pop3.socketFactory.port", "995");
        //以下步骤跟一般的JavaMail操作相同
        Session session = Session.getDefaultInstance(props,null);
        //请将红色部分对应替换成你的邮箱帐号和密码
        URLName urln = new URLName("pop3","pop.exmail.qq.com",995,null,
                recMailAddr, passwd);
        Store store = session.getStore(urln);
        store.connect();
        Folder folder = store.getFolder("INBOX");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cd = Calendar.getInstance();//获取一个Calendar对象
        cd.add(Calendar.DATE, -14);//两周内的邮件
        SearchTerm st = new SentDateTerm(ComparisonTerm.GT, cd.getTime());
        folder.open(Folder.READ_ONLY);
        // 由于POP3协议无法获知邮件的状态,所以getUnreadMessageCount得到的是收件箱的邮件总数
        System.out.println("未读邮件数: " + folder.getUnreadMessageCount());
        // 由于POP3协议无法获知邮件的状态,所以下面得到的结果始终都是为0
        System.out.println("删除邮件数: " + folder.getDeletedMessageCount());
        System.out.println("邮件数: " + folder.getMessageCount());
//        Message message[] = folder.getMessages(1,folder.getMessageCount());也可默认取
        Message message[] = folder.search(st);
        PraseMimeMessage pmm = null;
        for (int i = 0; i < message.length; i++) {
            System.out.println("****************************************第"
                    + (i + 1) + "封邮件**********************************");
            pmm = new PraseMimeMessage((MimeMessage) message[i]);
            System.out.println("主题 :" + pmm.getSubject());
            pmm.setDateFormat("yy年MM月dd日 HH:mm");
            System.out.println("发送时间 :" + pmm.getSendDate());
            System.out.println("是否回执 :" + pmm.getReplySign());
            System.out.println("是否包含附件 :"
                    + pmm.isContainAttach((Part) message[i]));
            System.out.println("发件人 :" + pmm.getFrom1());
            System.out.println("收件人 :" + pmm.getMailAddress("TO"));
            System.out.println("抄送地址 :" + pmm.getMailAddress("CC"));
            System.out.println("密送地址 :" + pmm.getMailAddress("BCC"));
            System.out.println("邮件ID :" + i + ":" + pmm.getMessageId());
            pmm.getMailContent((Part) message[i]); // 根据内容的不同解析邮件
            pmm.setAttachPath(downLoadPath); // 设置邮件附件的保存路径
            pmm.saveAttachMent((Part) message[i]); // 保存附件
            System.out.println("邮件正文 :" + pmm.getBodyText());
            System.out.println("*********************************第" + (i + 1)
                    + "封邮件结束*************************************");
        }
    }

    /**
     * 　*　构造函数,初始化一个MimeMessage对象 　
     */
    public PraseMimeMessage() {
    }



    public PraseMimeMessage(MimeMessage mimeMessage) {
        this.mimeMessage = mimeMessage;
    }



    public void setMimeMessage(MimeMessage mimeMessage) {
        this.mimeMessage = mimeMessage;
    }



    /**
     * 　*　获得发件人的地址和姓名 　
     */
    public String getFrom1() throws Exception {
        InternetAddress address[] = (InternetAddress[]) mimeMessage.getFrom();
        String from = address[0].getAddress();
        if (from == null) {
            from = "";
        }
        String personal = address[0].getPersonal();
        if (personal == null) {
            personal = "";
        }
        String fromaddr = personal + "<" + from + ">";
        return fromaddr;
    }



    /**
     * 　*　获得邮件的收件人，抄送，和密送的地址和姓名，根据所传递的参数的不同
     * 　*　"to"----收件人　"cc"---抄送人地址　"bcc"---密送人地址 　
     *
     * @throws Exception
     */
    public String getMailAddress(String type) {
        String mailaddr = "";
        try {
            String addtype = type.toUpperCase();
            InternetAddress[] address = null;
            if (addtype.equals("TO") || addtype.equals("CC")
                    || addtype.equals("BBC")) {
                if (addtype.equals("TO")) {
                    address = (InternetAddress[]) mimeMessage
                            .getRecipients(Message.RecipientType.TO);
                } else if (addtype.equals("CC")) {
                    address = (InternetAddress[]) mimeMessage
                            .getRecipients(Message.RecipientType.CC);
                } else {
                    address = (InternetAddress[]) mimeMessage
                            .getRecipients(Message.RecipientType.BCC);
                }
                if (address != null) {
                    for (int i = 0; i < address.length; i++) {
                        String email = address[i].getAddress();
                        if (email == null)
                            email = "";
                        else {
                            email = MimeUtility.decodeText(email);
                        }
                        String personal = address[i].getPersonal();
                        if (personal == null)
                            personal = "";
                        else {
                            personal = MimeUtility.decodeText(personal);
                        }
                        String compositeto = personal + "<" + email + ">";
                        mailaddr += "," + compositeto;
                    }
                    mailaddr = mailaddr.substring(1);
                }
            } else {
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return mailaddr;
    }



    /**
     * 　　*　获得邮件主题 　　
     */
    public String getSubject() {
        String subject = "";
        try {
            subject = MimeUtility.decodeText(mimeMessage.getSubject());
            if (subject == null)
                subject = "";
        } catch (Exception e) {
            // TODO: handle exception
        }
        return subject;
    }



    /**
     * 　　*　获得邮件发送日期 　　
     */
    public String getSendDate() throws Exception {
        Date senddate = mimeMessage.getSentDate();
        SimpleDateFormat format = new SimpleDateFormat(dateformat);
        return format.format(senddate);
    }



    /**
     * 　　*　解析邮件，把得到的邮件内容保存到一个StringBuffer对象中，解析邮件
     * 　　*　主要是根据MimeType类型的不同执行不同的操作，一步一步的解析 　　
     */
    public void getMailContent(Part part) throws Exception {
        String contenttype = part.getContentType();
        int nameindex = contenttype.indexOf("name");
        boolean conname = false;
        if (nameindex != -1)
            conname = true;
        if (part.isMimeType("text/plain") && !conname) {
            bodytext.append((String) part.getContent());
        } else if (part.isMimeType("text/html") && !conname) {
            bodytext.append((String) part.getContent());
        } else if (part.isMimeType("multipart/*")) {
            Multipart multipart = (Multipart) part.getContent();
            int counts = multipart.getCount();
            for (int i = 0; i < counts; i++) {
                getMailContent(multipart.getBodyPart(i));
            }
        } else if (part.isMimeType("message/rfc822")) {
            getMailContent((Part) part.getContent());
        } else {
        }
    }



    /**
     * 　*　获得邮件正文内容 　　
     */
    public String getBodyText() {
        return bodytext.toString();
    }



    /**
     * 　　*　判断此邮件是否需要回执，如果需要回执返回"true",否则返回"false" 　　
     *
     * @throws MessagingException
     */
    public boolean getReplySign() throws MessagingException {
        boolean replysign = false;
        String needreply[] = mimeMessage
                .getHeader("Disposition-Notification-To");
        if (needreply != null) {
            replysign = true;
        }
        return replysign;
    }



    /**
     * 　*　获得此邮件的Message-ID 　　
     *
     * @throws MessagingException
     */
    public String getMessageId() throws MessagingException {
        return mimeMessage.getMessageID();
    }



    /**
     * 　*　【判断此邮件是否已读，如果未读返回返回false,反之返回true】 　　
     *
     * @throws MessagingException
     */
    public boolean isNew() throws MessagingException {
        boolean isnew = false;
        Flags flags = ((Message) mimeMessage).getFlags();
        Flags.Flag[] flag = flags.getSystemFlags();
        for (int i = 0; i < flag.length; i++) {
            if (flag[i] == Flags.Flag.SEEN) {
                isnew = true;
                break;
            }
        }
        return isnew;
    }



    /**
     * 　*　判断此邮件是否包含附件 　
     *
     * @throws MessagingException
     */
    public boolean isContainAttach(Part part) throws Exception {
        boolean attachflag = false;
        String contentType = part.getContentType();
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            // 获取附件名称可能包含多个附件
            for (int j = 0; j < mp.getCount(); j++) {
                BodyPart mpart = mp.getBodyPart(j);
                String disposition = mpart.getDescription();
                if ((disposition != null)
                        && ((disposition.equals(Part.ATTACHMENT)) || (disposition
                        .equals(Part.INLINE)))) {
                    attachflag = true;
                } else if (mpart.isMimeType("multipart/*")) {
                    attachflag = isContainAttach((Part) mpart);
                } else {
                    String contype = mpart.getContentType();
                    if (contype.toLowerCase().indexOf("application") != -1)
                        attachflag = true;
                    if (contype.toLowerCase().indexOf("name") != -1)
                        attachflag = true;
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            attachflag = isContainAttach((Part) part.getContent());
        }
        return attachflag;
    }



    /**
     * 　*　【保存附件】 　
     *
     * @throws Exception
     * @throws IOException
     * @throws MessagingException
     * @throws Exception
     */
    public void saveAttachMent(Part part) throws Exception {
        String fileName = "";
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart) part.getContent();
            for (int j = 0; j < mp.getCount(); j++) {
                BodyPart mpart = mp.getBodyPart(j);
                String disposition = mpart.getDescription();
                if ((disposition != null)
                        && ((disposition.equals(Part.ATTACHMENT)) || (disposition
                        .equals(Part.INLINE)))) {
                    fileName = mpart.getFileName();
                    if (fileName.toLowerCase().indexOf("GBK") != -1) {
                        fileName = MimeUtility.decodeText(fileName);
                    }
                    saveFile(fileName, mpart.getInputStream());
                } else if (mpart.isMimeType("multipart/*")) {
                    fileName = mpart.getFileName();
                } else {
                    fileName = mpart.getFileName();
                    if ((fileName != null)) {
                        fileName = MimeUtility.decodeText(fileName);
                        saveFile(fileName, mpart.getInputStream());
                    }
                }
            }
        } else if (part.isMimeType("message/rfc822")) {
            saveAttachMent((Part) part.getContent());
        }
    }



    /**
     * 　*　【设置附件存放路径】 　
     */
    public void setAttachPath(String attachpath) {
        this.saveAttachPath = attachpath;
    }



    /**
     * 　*　【设置日期显示格式】 　
     */
    public void setDateFormat(String format) {
        this.dateformat = format;
    }



    /**
     * 　*　【获得附件存放路径】 　
     */



    public String getAttachPath() {
        return saveAttachPath;
    }



    /**
     * 　*　【真正的保存附件到指定目录里】 　
     */
    private void saveFile(String fileName, InputStream in) throws Exception {
        String osName = System.getProperty("os.name");
        String storedir = getAttachPath();
        String separator = "";
        if (osName == null)
            osName = "";
        if (osName.toLowerCase().indexOf("win") != -1) {
            // 如果是window 操作系统
            separator = "/";
            if (storedir == null || storedir.equals(""))
                storedir = "c:\tmp";
        } else {
            // 如果是其他的系统
            separator = "/";
            storedir = "/tmp";
        }
        File strorefile = new File(storedir + separator + fileName);
        BufferedOutputStream bos = null;
        BufferedInputStream bis = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(strorefile));
            bis = new BufferedInputStream(in);
            int c;
            while ((c = bis.read()) != -1) {
                bos.write(c);
                bos.flush();
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            bos.close();
            bis.close();
        }
    }


}
