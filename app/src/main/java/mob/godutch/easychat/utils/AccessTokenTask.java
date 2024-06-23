package mob.godutch.easychat.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;


public class AccessTokenTask extends AsyncTask<Void, Void, Boolean> {

    private Context context;

    private String deviceToken;
    private String title;
    private String body;


    private OnTaskCompleted listener;
    public interface OnTaskCompleted {
        void onTaskCompleted(boolean success);
    }

    public AccessTokenTask(Context context,  String deviceToken, String  title,String body, OnTaskCompleted listener) {
        this.context = context.getApplicationContext();
        this.deviceToken = deviceToken;
        this.title = title;
        this.body = body;
        this.listener = listener;

    }

    @Override
    public Boolean doInBackground(Void... voids) {
        try {
            InputStream serviceAccountStream = context.getAssets().open("serviceAccountKey2.json");
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(serviceAccountStream)
                    .createScoped(Collections.singleton("https://www.googleapis.com/auth/firebase.messaging"));

            credentials.refreshIfExpired();
            AccessToken accessToken = credentials.refreshAccessToken();
            String tokenValue = accessToken.getTokenValue();
            Log.i("AccessToken",String.valueOf(accessToken));
            return  FCMHelper.sendNotification(tokenValue, deviceToken, title, body);
        } catch (IOException e) {
            Log.e("AccessTokenTask", "IOException while fetching access token", e);
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (listener != null) {
            listener.onTaskCompleted(success);
        }
    }



}
