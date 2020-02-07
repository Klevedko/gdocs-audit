package com.google.drive;

import com.google.common.collect.Multimap;
import com.google.drive.Reports.DeleteReport;
import com.google.drive.Reports.DynamicReport;
import com.google.drive.Reports.StaticReport;
import com.google.drive.Reports.UpdateReport;
import org.springframework.web.servlet.ModelAndView;



public class App {
    public static void main(ModelAndView modelAndView, Multimap<String, String> email_exceptions, Multimap<String, String> folder_exceptions) {
        switch (modelAndView.getModel().get("task").toString()) {
            case "static":
                StaticReport.main(modelAndView, email_exceptions, folder_exceptions);
                break;
            case "dynamic":
                DynamicReport.main(modelAndView, email_exceptions, folder_exceptions);
                break;
            case "update":
                UpdateReport.main(modelAndView, email_exceptions, folder_exceptions);
                break;
            case "delete":
                DeleteReport.main(modelAndView, email_exceptions, folder_exceptions);
                break;
            default:
                throw new IllegalArgumentException("Invalid model's task! ");
        }
    }


}
