package ru.andcool;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpRequest.BodyPublishers;
import java.util.Objects;

import org.json.JSONObject;

import static java.lang.String.format;

public class CloudFlare {
    String BEARER;
    String ZONE;

    public JSONObject getDNSRecord(String name, String type) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(format("https://api.cloudflare.com/client/v4/zones/%s/dns_records", this.ZONE)))
                .header("Authorization", format("Bearer %s", this.BEARER))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }
        JSONObject json_response = new JSONObject(response.body());
        for (Object record : json_response.getJSONArray("result")) {
            JSONObject record_json = new JSONObject(record.toString());
            if (Objects.equals(record_json.getString("name"), name) &&
                    Objects.equals(record_json.getString("type"), type)) {
                return record_json;
            }
        }
        return null;
    }

    public JSONObject createDNSRecord(String name, String type, boolean proxied, String content) throws Exception {
        JSONObject POSTObject = new JSONObject();
        POSTObject.put("content", content);
        POSTObject.put("name", name);
        POSTObject.put("proxied", proxied);
        POSTObject.put("type", type);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(format("https://api.cloudflare.com/client/v4/zones/%s/dns_records", this.ZONE)))
                .POST(BodyPublishers.ofString(POSTObject.toString()))
                .header("Authorization", format("Bearer %s", this.BEARER))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }
        return new JSONObject(response.body()).getJSONObject("result");
    }

    public JSONObject updateDNSRecord(String name, String type, boolean proxied, String content, JSONObject record) throws Exception {
        JSONObject POSTObject = new JSONObject();
        POSTObject.put("content", content);
        POSTObject.put("name", name);
        POSTObject.put("proxied", proxied);
        POSTObject.put("type", type);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(format("https://api.cloudflare.com/client/v4/zones/%s/dns_records/%s",
                        this.ZONE,
                        record.getString("id"))))
                .PUT(BodyPublishers.ofString(POSTObject.toString()))
                .header("Authorization", format("Bearer %s", this.BEARER))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }
        return new JSONObject(response.body()).getJSONObject("result");
    }
}
