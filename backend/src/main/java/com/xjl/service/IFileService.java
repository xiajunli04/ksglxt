package com.xjl.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface IFileService {

    String upload(MultipartFile file);

    ResponseEntity<Resource> download(String filename);
}
