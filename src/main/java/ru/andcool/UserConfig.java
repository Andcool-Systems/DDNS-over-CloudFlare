package ru.andcool;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class UserConfig {
    public static String HOSTNAME = "example.com";
    public static String BEARER = "<token>";
    public static String ZONE_ID = "<zone id>";
    public static String RECORD_TYPE = "A";
    public static boolean PROXIED = true;
    public static int PERIOD = 30;

    /*
    Save config to file
     */
    public static void save() {
        final File configFile = new File("./config.json");
        JSONObject jsonConfig = new JSONObject();
        jsonConfig.put("hostname", HOSTNAME);
        jsonConfig.put("token", BEARER);
        jsonConfig.put("zone_id", ZONE_ID);
        jsonConfig.put("record_type", RECORD_TYPE);
        jsonConfig.put("proxied", PROXIED);
        jsonConfig.put("period", PERIOD);
        try {
            Files.createDirectories(configFile.toPath().getParent());
            Files.writeString(configFile.toPath(), jsonConfig.toString(4));
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }

    /*
    Load config from file
     */
    public static void load() {
        final File configFile = new File("./config.json");
        try {
            JSONObject jsonConfig = new JSONObject(Files.readString(configFile.toPath()));
            for (String key : jsonConfig.keySet()) {
                switch (key) {
                    case "hostname" -> HOSTNAME = jsonConfig.getString(key);
                    case "token" -> BEARER = jsonConfig.getString(key);
                    case "zone_id" -> ZONE_ID = jsonConfig.getString(key);
                    case "record_type" -> RECORD_TYPE = jsonConfig.getString(key);
                    case "proxied" -> PROXIED = jsonConfig.getBoolean(key);
                    case "period" -> PERIOD = jsonConfig.getInt(key);
                }
            }
        } catch (Exception e) {
            System.out.println(e.toString());
            save();
        }
    }
}