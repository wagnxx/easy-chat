package mob.godutch.easychat.utils;

import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;

public class NetworkTimeTask extends AsyncTask<Void, Void, Long> {

    private static final String TAG = "NetworkTimeTask";

    @Override
    protected Long doInBackground(Void... voids) {
        long networkTime = NTPClient.getNetworkTime();
        return networkTime;
    }

    @Override
    protected void onPostExecute(Long networkTime) {
        if (networkTime != null && networkTime > 0) {
            SystemClock.setCurrentTimeMillis(networkTime);
            Log.i(TAG, "Network time set to: " + networkTime);
        } else {
            Log.e(TAG, "Failed to get network time");
        }
    }
}