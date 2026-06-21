package com.yourname.ticketflow.modules.upload.controller;

import cn.hutool.core.util.IdUtil;
import com.yourname.ticketflow.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * 文件上传控制器
 */
@Slf4j
@Tag(name = "文件上传", description = "图片等文件上传")
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Value("${upload.url-prefix:/uploads}")
    private String urlPrefix;

    @Operation(summary = "上传图片")
    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.fail("文件为空");
        }

        // 校验文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.fail("只允许上传图片文件");
        }

        // 校验文件大小 (5MB)
        if (file.getSize() > 5 * 1024 * 1024) {
            return Result.fail("图片大小不能超过 5MB");
        }

        try {
            // 生成文件名：events/yyyyMM/dd/uuid.ext
            String dateDir = java.time.LocalDate.now().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyyMM/dd"));
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String newFileName = IdUtil.fastSimpleUUID() + ext;

            // 创建目录
            Path dir = Paths.get(uploadPath, "events", dateDir);
            Files.createDirectories(dir);

            // 保存文件
            Path filePath = dir.resolve(newFileName);
            file.transferTo(filePath.toFile());

            // 返回访问 URL
            String fileUrl = urlPrefix + "/events/" + dateDir + "/" + newFileName;
            log.info("图片上传成功: {} -> {}", originalName, fileUrl);

            return Result.ok(Map.of(
                    "url", fileUrl,
                    "name", originalName != null ? originalName : newFileName
            ));
        } catch (IOException e) {
            log.error("图片上传失败", e);
            return Result.fail("图片上传失败: " + e.getMessage());
        }
    }
}
