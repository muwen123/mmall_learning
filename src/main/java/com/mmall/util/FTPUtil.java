package com.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * @Author xuqian
 * @Date 2019/6/24 21:04
 */
public class FTPUtil {

    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    //构造器
    public FTPUtil(String ip, int port, String user, String pwd){
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    //写一些静态方法，开放出去的（即外部可以通过类名.方法名调用）
    //返回上传成功或者失败
    public static boolean uploadFile(List<File> fileList) throws IOException { //可以批量上传
        FTPUtil ftpUtil = new FTPUtil(ftpIp,21,ftpUser,ftpPass);
        logger.info("开始连接FTP服务器");
        //上传文件（封装）
        boolean result = ftpUtil.uploadFile("img",fileList);
        logger.info("开始连接FTP服务器,结束上传，上传结果:{}");
        return result;
    }

    //上传的具体逻辑（封装！）
    private boolean uploadFile(String remotePath, List<File> fileList) throws IOException { //参数1为远程路径
        boolean uploaded = true;
        FileInputStream fis = null;
        //连接服务器（封装）
        if(connectServer(this.ip,this.port,this.user,this.pwd)){ //连接服务器成功
            try {
                ftpClient.changeWorkingDirectory(remotePath); //更改工作目录
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE); //文件类型为二进制，防止乱码
                ftpClient.enterLocalPassiveMode(); //打开本地的被动模式
                for(File fileItem : fileList){ //挨个对fileList中文件进行上传
                    fis = new FileInputStream(fileItem); //采用FileInputStream上传文件
                    ftpClient.storeFile(fileItem.getName(),fis); //将上传的文件保存在ftp服务器上
                }
            } catch (IOException e) {
                logger.error("上传文件异常",e);
                uploaded = false;
                e.printStackTrace();
            } finally {
                fis.close();
                ftpClient.disconnect();
            }
        }
        return uploaded;
    }

    //连接服务器（封装）
    private boolean connectServer(String ip, int port, String user, String pwd){
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip);
            isSuccess = ftpClient.login(user,pwd);
        } catch (IOException e) {
            logger.error("连接FTP服务器异常",e);
//            e.printStackTrace();
        }
        return isSuccess;
    }


    private String ip;
    private int port;
    private String user;
    private String pwd;
    private FTPClient ftpClient;

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPass() {
        return pwd;
    }

    public void setPass(String pass) {
        this.pwd = pwd;
    }

    public FTPClient getFtpClient() {
        return ftpClient;
    }

    public void setFtpClient(FTPClient ftpClient) {
        this.ftpClient = ftpClient;
    }
}
