package dev.railroadide.githubplugin.http;

import com.google.gson.JsonObject;

public record DeviceCodeResponse(String deviceCode, String userCode, String verificationUri, int interval) {
    public static DeviceCodeResponse fromJson(JsonObject json) {
        String deviceCode = json.get("device_code").getAsString();
        String userCode = json.get("user_code").getAsString();
        String verificationUri = json.get("verification_uri").getAsString();
        int interval = json.get("interval").getAsInt();
        return new DeviceCodeResponse(deviceCode, userCode, verificationUri, interval);
    }
}
