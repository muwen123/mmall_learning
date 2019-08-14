package com.mmall.service.Impl;

import com.google.common.collect.Lists;
import com.mmall.service.IFileService;
import com.mmall.util.FTPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @Author xuqian
 * @Date 2019/6/24 19:43
 */
@Service("iFileService")
public class FileServiceImpl implements IFileService {
    //声明日志
    private Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    //返回上传的文件名
    public String upload(MultipartFile file, String path){
        String fileName = file.getOriginalFilename(); //拿到上传的文件的原始文件名
        //获取文件的扩展名
        //abd.jpg //abc.abc.abc.jpg   结果是fileName.lastIndexOf(".")获取.的位置，然后加1，所以最后返回的是jpg（就是我们想要的扩展名）
        //substring获取子串，.开始直到最后，其中.是从最后往前走遇到的第一个，这样做的目的是为了正确获取像abc.abc.abc.jpj这样的有好几个.的文件的扩展名
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        logger.info("开始上传文件,上传文件的文件名:{},上传的路径:{},新文件名:{}",fileName,path,uploadFileName);

        //创建目录
        File fileDir = new File(path);
        if(!fileDir.exists()){
            fileDir.setWritable(true);
            fileDir.mkdirs();
        }
        File targetFile = new File(path,uploadFileName);  //创建文件

        //用SpringMVC(file)的transferTo来上传该文件
        try {
            file.transferTo(targetFile);
            //文件已经上传成功了，上传的路径为path:也就是上传到upload文件夹下了

            FTPUtil.uploadFile(Lists.newArrayList(targetFile)); //用guava的Lists下的newArrayList(E... elements)方法将targetFile填充到list中
            //将targetFile 上传到FTP服务器上

            targetFile.delete();
            //上传完之后删除upload文件夹下面的文件
        } catch (IOException e) {
            logger.error("文件上传异常",e);
            return null; //异常时，返回null，即没有文件传给upload文件夹
        }
        return targetFile.getName(); //返回要上传的目标文件名
    }


}
