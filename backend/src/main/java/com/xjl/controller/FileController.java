package com.xjl.controller;

import com.xjl.domain.R;
import com.xjl.service.IFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    private final IFileService fileService;

    @PostMapping("/upload")
    public R<String> upload(@RequestParam("file") MultipartFile file) {
        String url = fileService.upload(file);
        return R.ok(url);
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> download(@PathVariable String filename) {
        return fileService.download(filename);
    }
}
