package com.umang345.Email.Quartz.Scheduler.web;

import com.umang345.Email.Quartz.Scheduler.job.EmailJob;
import com.umang345.Email.Quartz.Scheduler.payload.EmailRequest;
import com.umang345.Email.Quartz.Scheduler.payload.EmailResponse;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Slf4j
@RestController
public class EmailSchedulerController
{
    private static final Logger logger = LoggerFactory.getLogger(EmailSchedulerController.class);

    /***
     * Injecting Scheduler dependency
     */
    @Autowired
    private Scheduler scheduler;

    /***
     * Rest API Endpoint to schedule Email
     * @param scheduleEmailRequest : Email Request Payload object
     * @return : Email Response Payload object; Exception otherwise
     */
    @PostMapping("/schedule/email")
    public ResponseEntity<EmailResponse> scheduleEmail(@Valid @RequestBody EmailRequest scheduleEmailRequest)
    {
        try {
            ZonedDateTime dateTime = ZonedDateTime.of(scheduleEmailRequest.getDateTime(), scheduleEmailRequest.getTimeZone());

            /***
             *  Adding validation for job to be of time later than the current time
             */
            if(dateTime.isBefore(ZonedDateTime.now())) {
                EmailResponse scheduleEmailResponse = new EmailResponse(false,
                        "dateTime must be after current time");
                return ResponseEntity.badRequest().body(scheduleEmailResponse);
            }

            /***
             * 1) Create JobDetail Object from Email Request Payload
             * 2) Create Trigger Object from JobDetailObject
             * 3) Schedule job using JobDetail and Trigger Object
             */
            JobDetail jobDetail = buildJobDetail(scheduleEmailRequest);
            Trigger trigger = buildJobTrigger(jobDetail, dateTime);
            scheduler.scheduleJob(jobDetail, trigger);

            /***
             *  Create and return Email Response Payload Object
             */
            EmailResponse scheduleEmailResponse = new EmailResponse(true,
                    jobDetail.getKey().getName(), jobDetail.getKey().getGroup(), "Email Scheduled Successfully!");
            return ResponseEntity.ok(scheduleEmailResponse);
        } catch (SchedulerException ex) {
            logger.error("Error scheduling email", ex);

            EmailResponse scheduleEmailResponse = new EmailResponse(false,
                    "Error scheduling email. Please try later!");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(scheduleEmailResponse);
        }
    }

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
