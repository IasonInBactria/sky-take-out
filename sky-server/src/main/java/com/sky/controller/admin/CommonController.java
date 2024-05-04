package com.sky.controller.admin;
import com.sky.constant.MessageConstant;
import com.sky.properties.AliOssProperties;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;


@Api(tags = "通用接口")
@Slf4j
@RestController
@RequestMapping("/admin/common")
@RequiredArgsConstructor
public class CommonController {

    private final AliOssUtil aliOssUtil;

    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        // 文件上传
        log.info("文件上传:{}", file.getOriginalFilename());
        try {
            //为防止文件名重复，文件名加上uuid
            String filePath = aliOssUtil.upload(file.getBytes(), UUID.randomUUID() +
                    Objects.requireNonNull(file.getOriginalFilename()).
                            substring(file.getOriginalFilename().lastIndexOf(".")));
            return Result.success(filePath);
        } catch (IOException e) {
            log.error(MessageConstant.UPLOAD_FAILED, e);
            throw new RuntimeException(e);
        }
    }
}
