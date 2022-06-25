package com.umang345.Email.Quartz.Scheduler.web;

import com.umang345.Email.Quartz.Scheduler.job.EmailJob;
import com.umang345.Email.Quartz.Scheduler.payload.EmailRequest;
import org.quartz.*;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

public class EmailSchedulerController
{
    /***
     * Method to build job detail from Email Request Payload
     * @param emailRequest : Email Request Payload Object
     * @return : Job Detail Object
     */
    private JobDetail buildJobDetail(EmailRequest emailRequest)
    {
        JobDataMap jobDataMap = new JobDataMap();

        jobDataMap.put("email", emailRequest.getEmail());
        jobDataMap.put("subject", emailRequest.getSubject());
        jobDataMap.put("body", emailRequest.getBody());

        return JobBuilder.newJob(EmailJob.class)
                .withIdentity(UUID.randomUUID().toString(), "email-jobs")
                .withDescription("Send Email Job")
                .usingJobData(jobDataMap)
                .storeDurably()
                .build();
    }

    /***
     * Method to implement Job Trigger
     * @param jobDetail : JobDetail Object containing details about the job
     * @param startAt : Time Zone
     * @return : The built Trigger Object
     */
    private Trigger buildJobTrigger(JobDetail jobDetail, ZonedDateTime startAt) {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.getKey().getName(), "email-triggers")
                .withDescription("Send Email Trigger")
                .startAt(Date.from(startAt.toInstant()))
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build();
    }
}
