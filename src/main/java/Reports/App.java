package Reports;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class App {
    public static String startFolderId = "xxxxx";
    public static String driveOutputFolderID = "xxxxxx";
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
        switch (args[0]) {
            case "6":
                StaticReport.main(new String[]{""});
            case "7":
                DynamicReport.main(new String[]{""});
            default:
                System.exit(0);
        }
        System.exit(0);
    }

    public static void full_exceptions() {
        email_exceptions.put("user1", "service@xxx-216709.iam.gserviceaccount.com");
        email_exceptions.put("user2", "xxxx@gmail.com");
        email_exceptions.put("user3", "worklogs-updater@xxx-137012.iam.gserviceaccount.com");

        folder_exceptions.put("folder1", "asdasdasd");
        folder_exceptions.put("folder2", "asdasdasd");
    }
}
