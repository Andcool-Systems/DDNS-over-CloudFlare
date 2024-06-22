package ru.andcool;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;


public class Ip {
    public static String getCurrentIp() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://www.cloudflare.com/cdn-cgi/trace"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }
        String body = response.body();
        String[] lines = body.split("\n");
        for (String line : lines) {
            String[] line_data = line.split("=");
            if (Objects.equals(line_data[0], "ip")) {
                return line_data[1];
            }
        }
        return null;
    }


    public static String validateIp(String ip) throws UnknownHostException {
        InetAddress address = InetAddress.getByName(ip);
        if (address instanceof Inet4Address) {
            return "A";
        } else if (address instanceof Inet6Address) {
            return "AAAA";
        }
        throw new RuntimeException("Cannot validate ip!");
    }

}
