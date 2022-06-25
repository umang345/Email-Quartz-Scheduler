package com.umang345.Email.Quartz.Scheduler.payload;

public class EmailResponse
{
    private boolean success;

    private String jobId;

    private String jobGroup;

    private String message;

    public EmailResponse(boolean success, String message)
    {
         this.success = success;
         this.message = message;
    }

    public EmailResponse(boolean success, String jobId,String jobGroup, String message)
    {
         this.success = success;
         this.jobId = jobId;
         this.jobGroup = jobGroup;
         this.message = message;
    }

}
