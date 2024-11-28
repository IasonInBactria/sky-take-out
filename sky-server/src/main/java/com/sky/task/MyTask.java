package com.sky.task;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Slf4j
public class MyTask {

    /**
     * 定时任务执行方法
     */
    @Scheduled(cron = "0/5 * * * * ? ")
    public void executeTask() {
        //log.info("定时任务执行, 时间: {}", LocalDateTime.now());
    }
}
