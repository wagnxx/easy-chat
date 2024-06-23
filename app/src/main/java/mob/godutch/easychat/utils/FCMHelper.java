package mob.godutch.easychat.utils;


import java.io.IOException;

import okhttp3.*;

public class FCMHelper {
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/fb-dev-fde3a/messages:send";

    public static Boolean sendNotification(String accessToken, String deviceToken, String title, String message) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String jsonBody = "{"
                + "\"message\": {"
                + "\"token\": \"" + deviceToken + "\","
                + "\"notification\": {"
                + "\"title\": \"" + title + "\","
                + "\"body\": \"" + message + "\""
                + "}"
                + "}"
                + "}";

        RequestBody requestBody = RequestBody.create(
                jsonBody, MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(FCM_URL)
                .post(requestBody)
                .addHeader("Authorization", "Bearer " + accessToken)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Unexpected code " + response);
                return false;
            }
            System.out.println(response.body().string());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }



}
