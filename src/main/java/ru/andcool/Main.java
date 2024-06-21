package ru.andcool;

import org.json.JSONObject;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class Main {
    private static String last_ip;
    private static JSONObject DNSRecord = null;
    private static final CloudFlare cf = new CloudFlare();

    public static void main(String[] args) throws Exception {
        UserConfig.load();
        cf.BEARER = UserConfig.BEARER;
        cf.ZONE = UserConfig.ZONE_ID;

        JSONObject record = cf.getDNSRecord(UserConfig.HOSTNAME, UserConfig.RECORD_TYPE);
        if (record != null) {
            System.out.println("Found existing DNS with id " + record.getString("id"));
            last_ip = record.getString("content");
            DNSRecord = record;
        }else {
            System.out.println("DNS record not found! Creating...");
            String ip = Ip.getCurrentIp();
            JSONObject new_record = cf.createDNSRecord(UserConfig.HOSTNAME, UserConfig.RECORD_TYPE, UserConfig.PROXIED, ip);
            DNSRecord = new_record;
            last_ip = ip;
            System.out.println("DNS record with id " + new_record.getString("id") + " created!");
        }

        Timer timer = new Timer();

        TimerTask repeatedTask = new TimerTask() {
            public void run() {
                try {
                    String ip = Ip.getCurrentIp();
                    if (!Objects.equals(ip, last_ip)) {
                        JSONObject new_record = cf.updateDNSRecord(UserConfig.HOSTNAME, UserConfig.RECORD_TYPE, UserConfig.PROXIED, ip, DNSRecord);
                        if (new_record != null) {
                            System.out.println("Record updated! New ip: " + ip);
                            last_ip = ip;
                            DNSRecord = new_record;
                        } else {
                            throw new RuntimeException("Can't update cf record!");
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.toString());
                }
            }
        };

        timer.scheduleAtFixedRate(repeatedTask, 0, UserConfig.PERIOD * 60000L);
    }
}