package com.google.drive.controller;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.drive.App;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Controller
public class UploadController {
    //public static String[] array_F_userArray;
    private static String UPLOADED_FOLDER = "/tmp/";
    public static Path path;

    public Multimap<String, String> email_exceptions = HashMultimap.create();
    public Multimap<String, String> folder_exceptions = HashMultimap.create();

    @GetMapping("/")
    public String index() {
        return "permission_delete";
    }

    public void loadJsonFile(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes);
        } catch (IOException io) {
            System.out.println(io.getMessage());
        }
    }

    public void addServiceAccountToExceptionMap(Multimap<String, String> exception_map, String jsonFile) {
        System.out.println("addServiceAccountToExceptionMap...");
        try (FileReader reader = new FileReader(jsonFile)) {
            JsonElement rootElement = new JsonParser().parse(reader);
            exception_map.put("serviceUser", rootElement.getAsJsonObject().get("client_email").toString().replaceAll("\"", ""));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void fill_exceptions(Multimap<String, String> exception_map, String jsonFile, String jsonRoot, String exceptions_root, String key, String value) {
        System.out.println("fill_exceptions...");
        InputStream json = Thread.currentThread().getContextClassLoader().getResourceAsStream(jsonFile);
        Reader reader = new InputStreamReader(json);
        JsonElement rootElement = new JsonParser().parse(reader);
        JsonObject rootObject = rootElement.getAsJsonObject();
        JsonObject pages = rootObject.getAsJsonObject(jsonRoot).getAsJsonObject(exceptions_root);
        for (Map.Entry<String, JsonElement> entry : pages.entrySet()) {
            JsonObject entryObject = entry.getValue().getAsJsonObject();
            exception_map.put(entryObject.get(key).toString().replaceAll("\"", ""),
                    entryObject.get(value).toString().replaceAll("\"", ""));
        }
    }

    @PostMapping("/permission_static")
    public String permission_static(@RequestParam("file") MultipartFile file,
                                    @RequestParam("F_scanFolderId") String F_scanFolderId,
                                    @RequestParam("F_outputFolderId") String F_outputFolderId,
                                    RedirectAttributes redirectAttributes) throws Exception {
        try {
            loadJsonFile(file);
            addServiceAccountToExceptionMap(email_exceptions, "/tmp/service_account.json");
            fill_exceptions(email_exceptions, "email_exceptions.json", "email_exceptions", "exceptions", "user", "email");
            fill_exceptions(folder_exceptions, "folder_exceptions.json", "folder_exceptions", "exceptions", "folder", "folderId");
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("task", "static");
            modelAndView.addObject("F_scanFolderId", F_scanFolderId);
            modelAndView.addObject("F_outputFolderId", F_outputFolderId);
            App.main(modelAndView, email_exceptions, folder_exceptions);

            redirectAttributes.addFlashAttribute("message", "Done '" + file.getOriginalFilename() + "'");
            redirectAttributes.addFlashAttribute("F_scanFolderId", F_scanFolderId);
            redirectAttributes.addFlashAttribute("F_outputFolderId", F_outputFolderId);
        } catch (Exception x) {
            System.out.println(x);
        }
        return "redirect:/uploadStatus";
    }

    @PostMapping("/permission_dynamic")
    public String permission_dynamic(@RequestParam("file") MultipartFile file,
                                     @RequestParam("F_scanFolderId") String F_scanFolderId,
                                     @RequestParam("F_outputFolderId") String F_outputFolderId,
                                     RedirectAttributes redirectAttributes) throws Exception {
        try {
            loadJsonFile(file);
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("task", "static");
            modelAndView.addObject("F_scanFolderId", F_scanFolderId);
            modelAndView.addObject("F_outputFolderId", F_outputFolderId);
            App.main(modelAndView, email_exceptions, folder_exceptions);

            redirectAttributes.addFlashAttribute("message", "Done '" + file.getOriginalFilename() + "'");
            redirectAttributes.addFlashAttribute("F_scanFolderId", F_scanFolderId);
            redirectAttributes.addFlashAttribute("F_outputFolderId", F_outputFolderId);
        } catch (Exception x) {
            System.out.println(x);
        }
        return "redirect:/uploadStatus";
    }


    @PostMapping("/permission_update")
    public String permission_update(@RequestParam("file") MultipartFile file,
                                    @RequestParam("F_scanFolderId") String F_scanFolderId,
                                    @RequestParam("F_outputFolderId") String F_outputFolderId,
                                    @RequestParam("F_userArray") String F_userArray,
                                    @RequestParam("F_userNewPermission") String F_userNewPermission,
                                    RedirectAttributes redirectAttributes) throws Exception {
        try {
            loadJsonFile(file);
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("task", "update");
            modelAndView.addObject("F_scanFolderId", F_scanFolderId);
            modelAndView.addObject("F_outputFolderId", F_outputFolderId);
            modelAndView.addObject("F_userArray", F_userArray);
            modelAndView.addObject("F_userNewPermission", F_userNewPermission);
            App.main(modelAndView, email_exceptions, folder_exceptions);

            redirectAttributes.addFlashAttribute("message", "Done '" + file.getOriginalFilename() + "'");
            redirectAttributes.addFlashAttribute("F_scanFolderId", F_scanFolderId);
            redirectAttributes.addFlashAttribute("F_outputFolderId", F_outputFolderId);
            redirectAttributes.addFlashAttribute("F_userArray", F_userArray.split("\r\n"));
            redirectAttributes.addFlashAttribute("F_userNewPermission", F_userNewPermission);
        } catch (Exception x) {
            System.out.println(x);
        }
        return "redirect:/uploadStatus";
    }


    @PostMapping("/permission_delete")
    public String permission_delete(@RequestParam("file") MultipartFile file,
                                    @RequestParam("F_scanFolderId") String F_scanFolderId,
                                    @RequestParam("F_outputFolderId") String F_outputFolderId,
                                    @RequestParam("F_userArray") String F_userArray,
                                    RedirectAttributes redirectAttributes) throws Exception {

        /*if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Please select a file to upload");
            return "redirect:uploadStatus";
        }*/
        try {
            loadJsonFile(file);
            ModelAndView modelAndView = new ModelAndView();
            modelAndView.addObject("task", "delete");
            modelAndView.addObject("F_scanFolderId", F_scanFolderId);
            modelAndView.addObject("F_outputFolderId", F_outputFolderId);
            modelAndView.addObject("F_userArray", F_userArray);
            //array_F_userArray = F_userArray.split("\r\n");
            //for (int i = 0; i < array_F_userArray.length; i++) {
            //System.out.println(array_F_userArray[i]);
            //}
            App.main(modelAndView, email_exceptions, folder_exceptions);

            redirectAttributes.addFlashAttribute("message", "Done '" + file.getOriginalFilename() + "'");
            redirectAttributes.addFlashAttribute("F_scanFolderId", F_scanFolderId);
            redirectAttributes.addFlashAttribute("F_outputFolderId", F_outputFolderId);
            redirectAttributes.addFlashAttribute("F_userArray", F_userArray.split("\r\n"));
            System.out.println(F_userArray);
        } catch (Exception x) {
            System.out.println(x);
        }
        return "redirect:/uploadStatus";
    }

    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }
}