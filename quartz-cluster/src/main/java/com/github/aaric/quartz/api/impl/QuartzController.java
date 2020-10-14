package com.github.aaric.quartz.api.impl;

import com.github.aaric.quartz.api.QuartzApi;
import com.github.aaric.quartz.quartz.job.ClusterJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JobController
 *
 * @author Aaric, created on 2020-10-14T11:38.
 * @version 0.2.0-SNAPSHOT
 */
@Slf4j
@RestController
@RequestMapping("/api/quartz")
public class QuartzController implements QuartzApi {

    @Autowired
    private Scheduler scheduler;

    @Override
    @GetMapping("/create/{jobName}")
    public Map<String, Object> create(@PathVariable String jobName) throws Exception {
        JobKey jobKey = JobKey.jobKey(jobName, Scheduler.DEFAULT_GROUP);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, Scheduler.DEFAULT_GROUP);
        if (!(scheduler.checkExists(jobKey) || scheduler.checkExists(triggerKey))) {
            JobDetail jobDetail = JobBuilder.newJob(ClusterJob.class)
                    .withDescription("Cluster Job")
                    .withIdentity(jobName, Scheduler.DEFAULT_GROUP)
                    .usingJobData("id", 1L)
                    .usingJobData("name", "hello world")
                    .requestRecovery()
                    .build();
            Trigger trigger = TriggerBuilder.newTrigger()
                    .withDescription("Cluster Job Trigger")
                    .withIdentity(jobName, Scheduler.DEFAULT_GROUP)
                    .startAt(new Date())
                    .withSchedule(CronScheduleBuilder.cronSchedule("0/5 * * * * ?"))
                    .build();
            scheduler.scheduleJob(jobDetail, trigger);

            log.info("create job: {}", jobName);
        }
        return response(jobName);
    }

    @Override
    @PutMapping("/pause/{jobName}")
    public Map<String, Object> pause(@PathVariable String jobName) throws Exception {
        JobKey jobKey = JobKey.jobKey(jobName, Scheduler.DEFAULT_GROUP);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, Scheduler.DEFAULT_GROUP);
        if (scheduler.checkExists(jobKey) || scheduler.checkExists(triggerKey)) {
            scheduler.pauseTrigger(triggerKey);

            log.info("pause job: {}", jobName);
        }
        return response(jobName);
    }

    @Override
    @PutMapping("/resume/{jobName}")
    public Map<String, Object> resume(@PathVariable String jobName) throws Exception {
        JobKey jobKey = JobKey.jobKey(jobName, Scheduler.DEFAULT_GROUP);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, Scheduler.DEFAULT_GROUP);
        if (scheduler.checkExists(jobKey) || scheduler.checkExists(triggerKey)) {
            scheduler.resumeTrigger(triggerKey);

            log.info("resume job: {}", jobName);
        }
        return response(jobName);
    }

    @Override
    @DeleteMapping("/remove/{jobName}")
    public Map<String, Object> remove(@PathVariable String jobName) throws Exception {
        JobKey jobKey = JobKey.jobKey(jobName, Scheduler.DEFAULT_GROUP);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, Scheduler.DEFAULT_GROUP);
        if (scheduler.checkExists(jobKey) || scheduler.checkExists(triggerKey)) {
            scheduler.pauseTrigger(triggerKey);
            scheduler.unscheduleJob(triggerKey);

            log.info("delete job: {}", jobName);
        }
        return response(null);
    }

    @Override
    @GetMapping("/exists/{jobName}")
    public Map<String, Object> exists(@PathVariable String jobName) throws Exception {
        boolean flag = true;
        JobKey jobKey = JobKey.jobKey(jobName, Scheduler.DEFAULT_GROUP);
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, Scheduler.DEFAULT_GROUP);
        if (!(scheduler.checkExists(jobKey) || scheduler.checkExists(triggerKey))) {
            flag = false;

            log.info("exists job: {}", jobName);
        }
        return response(flag);
    }

    /**
     * 响应数据
     *
     * @param data 附加信息
     * @return
     */
    private Map<String, Object> response(Object data) {
        Map<String, Object> dataMap = new HashMap<>(3);
        dataMap.put("code", "0000");
        dataMap.put("msg", "SUCCESS");
        dataMap.put("data", data);
        return dataMap;
    }
}
