package com.mmall.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Author xuqian
 * @Date 2019/6/24 19:42
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
