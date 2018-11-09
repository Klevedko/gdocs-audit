package Reports;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class App {
    public static String startFolderId =
            "0B3jemUSF0v3dVFN6Wk8taXdLcms";
    //"1eHmvAPbDDGubIj3VvlmFcm7W65A1deAh";
    public static String driveOutputFolderID = "19U2YXvyA0A4M7M6idNq3O_ChzAYwNIk0";
    //"1eHmvAPbDDGubIj3VvlmFcm7W65A1deAh";
    public static Multimap<String, String> email_exceptions = HashMultimap.create();
    public static Multimap<String, String> folder_exceptions = HashMultimap.create();

    static {
        email_json();
        folder_json();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Running with params:");
        System.out.println(startFolderId);
        System.out.println(driveOutputFolderID);
        System.out.println();
        //StaticReport.main(new String[]{""});
        //DynamicReport.main(new String[]{""});

        switch (args[0].toString()) {
            case "6":
                System.out.println("6");
                StaticReport.main(new String[]{""});
                break;
            case "7":
                System.out.println("7");
                DynamicReport.main(new String[]{""});
                break;
            default:
                System.exit(0);
        }
        System.exit(0);
    }

    public static void email_json() {
        InputStream json = Thread.currentThread().getContextClassLoader().getResourceAsStream("email_exceptions.json");
        JsonObject rootObject = getJsonRoot(json);
        JsonObject pages = rootObject.getAsJsonObject("email_exceptions").getAsJsonObject("exceptions");
        for (Map.Entry<String, JsonElement> entry : pages.entrySet()) {
            JsonObject entryObject = entry.getValue().getAsJsonObject();
            email_exceptions.put(entryObject.get("user").toString().replaceAll("\"", ""),
                    entryObject.get("email").toString().replaceAll("\"", ""));
        }
    }

    public static void folder_json() {
        InputStream json = Thread.currentThread().getContextClassLoader().getResourceAsStream("folder_exceptions.json");
        JsonObject rootObject = getJsonRoot(json);
        JsonObject pages = rootObject.getAsJsonObject("folder_exceptions").getAsJsonObject("exceptions");
        for (Map.Entry<String, JsonElement> entry : pages.entrySet()) {
            JsonObject entryObject = entry.getValue().getAsJsonObject();
            folder_exceptions.put(entryObject.get("folder").toString().replaceAll("\"", ""),
                    entryObject.get("folderId").toString().replaceAll("\"", ""));
        }
        System.out.println(folder_exceptions.values());
    }

    public static JsonObject getJsonRoot(InputStream json) {
        JsonParser parser = new JsonParser();
        Reader reader = new InputStreamReader(json);
        JsonElement rootElement = parser.parse(reader);
        JsonObject rootObject = rootElement.getAsJsonObject();
        return rootObject;
    }
}
