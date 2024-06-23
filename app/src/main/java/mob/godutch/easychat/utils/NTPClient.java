package mob.godutch.easychat.utils;

import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class NTPClient {
    private static final String TAG = "NTPClient";

    public static long getNetworkTime() {
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(30000);
            InetAddress address = InetAddress.getByName("time.nist.gov");
            byte[] buffer = new byte[48];
            buffer[0] = 27;

            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 123);
            socket.send(packet);
            Log.i(TAG, "Packet sent.");

            socket.receive(packet);
            Log.i(TAG, "Packet received.");

            long transmitTime = ((buffer[40] & 0xffL) << 24) | ((buffer[41] & 0xffL) << 16) | ((buffer[42] & 0xffL) << 8) | (buffer[43] & 0xffL);
            transmitTime -= 2208988800L;

            Log.i(TAG, "Transmit time: " + transmitTime);
            return transmitTime * 1000;
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
        return System.currentTimeMillis();
    }
}
