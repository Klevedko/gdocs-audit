package Quartz;

import Reports.DynamicReport;
import Reports.StaticReport;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

public class CronBuild {
    public static String startFolderId = "GdriveFolderId";
    public static String driveOutputFolderID = "GdriveFolderId";
    public static Multimap<String, String> email_exceptions = HashMultimap.create();
    public static Multimap<String, String> folder_exceptions = HashMultimap.create();

    static {
        full_exceptions();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Running with params:");
        System.out.println(startFolderId);
        System.out.println(driveOutputFolderID);
        System.out.println();
        JobDetail job1 = JobBuilder.newJob(StaticReport.class)
                .withIdentity("Static", "group1").build();
        JobDetail job2 = JobBuilder.newJob(DynamicReport.class)
                .withIdentity("Dynamic", "group1").build();

        Trigger trigger1 = TriggerBuilder
                .newTrigger()
                .withIdentity("Static", "group1")
                //.withSchedule(CronScheduleBuilder.cronSchedule("0 00 20 ? * SAT "))
                .withSchedule(CronScheduleBuilder.cronSchedule("00,20,40 * *  ? * * "))
                .build();
        Trigger trigger2 = TriggerBuilder
                .newTrigger()
                .withIdentity("Dynamic", "group1")
                //.withSchedule(CronScheduleBuilder.cronSchedule("0 00 20 ? * SUN "))
                .withSchedule(CronScheduleBuilder.cronSchedule("10,30,50 * * ? * * "))
                .build();

        Scheduler scheduler = new StdSchedulerFactory().getScheduler();
        scheduler.start();
        scheduler.scheduleJob(job1, trigger1);
        scheduler.scheduleJob(job2, trigger2);
    }

    public static void full_exceptions() {
        email_exceptions.put("user1", "service@xxx-12345.iam.gserviceaccount.com");
        email_exceptions.put("user2", "xxx@gmail.com");
        email_exceptions.put("user3", "xxxx@xxx-12345.iam.gserviceaccount.com");

        folder_exceptions.put("folder1", "folderId");
        folder_exceptions.put("folder2", "folderId");
        folder_exceptions.put("folder3", "folderId");
        folder_exceptions.put("folder4", "folderId");
        folder_exceptions.put("folder5", "folderId");
        folder_exceptions.put("folder6", "folderId");
        folder_exceptions.put("folder7", "folderId");
    }
}
