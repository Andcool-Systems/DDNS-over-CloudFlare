package ru.andcool;

import org.json.JSONObject;
import ru.andcool.Logger.Level;
import ru.andcool.Logger.SillyLogger;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.String.format;

public class Main {
    private static String last_ip;
    private static JSONObject DNSRecord = null;
    private static final CloudFlare cf = new CloudFlare();
    public static final SillyLogger Logger = new SillyLogger("DDNS", true, Level.INFO);

    public static void main(String[] args) {
        UserConfig.load();
        cf.BEARER = UserConfig.BEARER;
        cf.ZONE = UserConfig.ZONE_ID;

        try {
            JSONObject record = cf.getDNSRecord(UserConfig.HOSTNAME);
            if (record != null) {
                Logger.log(Level.INFO, "Found existing DNS with id " + record.getString("id"));
                last_ip = record.getString("content");
                DNSRecord = record;
            } else {
                Logger.log(Level.INFO, "DNS record not found! Creating...");
                String ip = Ip.getCurrentIp();
                String record_type = Ip.validateIp(ip);

                JSONObject new_record = cf.createDNSRecord(UserConfig.HOSTNAME, record_type, UserConfig.PROXIED, ip);
                DNSRecord = new_record;
                last_ip = ip;
                Logger.log(Level.INFO, "DNS record with id " + new_record.getString("id") + " created!");
            }

            Timer timer = new Timer();

            TimerTask repeatedTask = new TimerTask() {
                public void run() {
                    try {
                        String ip = Ip.getCurrentIp();
                        Logger.log(Level.DEBUG, ip);
                        if (!Objects.equals(ip, last_ip)) {
                            Logger.log(Level.INFO, format("Detected ip change! %s -> %s", last_ip, ip));
                            String record_type = Ip.validateIp(ip);
                            JSONObject new_record = cf.updateDNSRecord(UserConfig.HOSTNAME, record_type, UserConfig.PROXIED, ip, DNSRecord);
                            if (new_record == null) {
                                Logger.log(Level.ERROR, "Can't update CloudFlare record!");
                                System.exit(-1);
                            }
                            Logger.log(Level.INFO, "Record updated! New ip: " + ip);
                            last_ip = ip;
                            DNSRecord = new_record;
                        }
                    } catch (Exception e) {
                        Logger.log(Level.ERROR, e.toString());
                    }
                }
            };

            timer.scheduleAtFixedRate(repeatedTask, 0, UserConfig.PERIOD * 1000L);
        } catch (Exception e) {
            Logger.log(Level.ERROR, e.toString());
        }
    }
}