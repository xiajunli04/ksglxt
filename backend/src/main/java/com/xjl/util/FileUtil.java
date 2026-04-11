package com.xjl.util;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class FileUtil {

    public static final List<String> ALLOWED_TYPES = Arrays.asList(
            "pdf", "doc", "docx", "jpg", "jpeg", "png"
    );

    public static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB

    public static String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    public static boolean isAllowed(String filename) {
        String ext = getExtension(filename);
        return ALLOWED_TYPES.contains(ext);
    }

    public static String generateFileName(String filename) {
        String ext = getExtension(filename);
        return UUID.randomUUID().toString().replace("-", "") + "." + ext;
    }
}
