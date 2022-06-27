package com.umang345.Email.Quartz.Scheduler.job;

import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;

@Component
public class EmailJob extends QuartzJobBean {

    // Logger to log info
    private static final Logger logger = LoggerFactory.getLogger(EmailJob.class);

    /***
     * Injecting JavaMailSender Dependency
     */
    @Autowired
    private JavaMailSender mailSender;

    /***
     * Injecting MailProperties Dependency
     */
    @Autowired
    private MailProperties mailProperties;


    /***
     * This method gets invoked when a Job Schedule is triggered
     * @param jobExecutionContext : The JobExcecutionContext Object
     * @throws JobExecutionException : Throws Exception in case Job Trigger fails
     */
    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        logger.info("Executing Job with key {}", jobExecutionContext.getJobDetail().getKey());

        JobDataMap jobDataMap = jobExecutionContext.getMergedJobDataMap();
        String subject = jobDataMap.getString("subject");
        String body = jobDataMap.getString("body");
        String recipientEmail = jobDataMap.getString("email");

        sendMail(mailProperties.getUsername(), recipientEmail, subject, body);
    }

    /***
     * Method to send email
     * @param fromEmail : Sender
     * @param toEmail : Recpient
     * @param subject : Subject of mail
     * @param body : Body of mail
     */
    private void sendMail(String fromEmail, String toEmail, String subject, String body) {
        try {
            logger.info("Sending Email to {}", toEmail);
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper messageHelper = new MimeMessageHelper(message, StandardCharsets.UTF_8.toString());
            messageHelper.setSubject(subject);
            messageHelper.setText(body, true);
            messageHelper.setFrom(fromEmail);
            messageHelper.setTo(toEmail);

            mailSender.send(message);
        } catch (MessagingException ex) {
            logger.error("Failed to send email to {}", toEmail);
        }
    }
}
