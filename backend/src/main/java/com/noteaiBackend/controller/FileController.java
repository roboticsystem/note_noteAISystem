package com.noteaiBackend.controller;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.noteaiBackend.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {
    
    @Value("${file.upload-dir:./file}")
    private String uploadRoot;
    
    private String getUploadDir() {
        // 1. 如果配置了绝对路径（如 Docker 容器中的 /app/file），直接使用
        File dir = new File(uploadRoot + "/document/");
        if (dir.isAbsolute()) {
            dir.mkdirs();
            return dir.getAbsolutePath() + File.separator;
        }
        
        // 2. 如果是相对路径，尝试基于用户当前工作目录解析
        //    注意：Spring Boot JAR 运行时 user.dir 可能是 Tomcat 临时目录
        dir = new File(System.getProperty("user.dir"), uploadRoot + "/document/");
        if (dir.exists() || dir.mkdirs()) {
            return dir.getAbsolutePath() + File.separator;
        }
        
        // 3. 兜底：使用用户家目录下的 notefiles 文件夹
        //    确保在任何环境下都能正常创建和读取文件
        dir = new File(System.getProperty("user.home"), "notefiles/document/");
        dir.mkdirs();
        return dir.getAbsolutePath() + File.separator;
    }

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String uploadDir = getUploadDir();
            // 确保上传目录存在
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 获取原始文件名和后缀
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uuid = UUID.randomUUID().toString();
            String filename = uuid + extension;

            // 保存文件
            File dest = new File(uploadDir + filename);
            file.transferTo(dest);

            // 返回文件信息对象（前端期望的格式）
            String fileUrl = "/api/files/download/" + filename;
            Map<String, Object> fileInfo = new HashMap<>();
            fileInfo.put("fileName", originalFilename != null ? originalFilename : filename);
            fileInfo.put("fileUrl", fileUrl);
            fileInfo.put("fileSize", file.getSize());

            return ResponseEntity.ok(ApiResponse.success("上传成功", fileInfo));
        } catch (IOException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("上传失败: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String filename, HttpServletRequest request) {
        try {
            String uploadDir = getUploadDir();
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                String contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                // 使用 attachment 强制下载（确保能获取到文件内容）
                // 对于图片等浏览器可预览的类型，保留 inline 方便预览
                String contentDisposition;
                if (contentType.startsWith("image/")) {
                    contentDisposition = "inline";
                } else {
                    contentDisposition = "attachment";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, 
                                contentDisposition + "; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
