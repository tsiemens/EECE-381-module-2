package com.group10.battleship.network;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.EditText;

import com.group10.battleship.BattleshipApplication;
import com.group10.battleship.MainActivity;
import com.group10.battleship.network.NetworkManager.OnAndroidSocketSetupListener;

public class UDPManager {
	private static final String TAG = UDPManager.class.getSimpleName();
	private static final int PORT = 50002;
	private InetAddress mTargetIP;
	private DatagramSocket mSocket;
	private OnBroadcastFoundListener onBroadcastFoundListener;
	
	public UDPManager() {
		try {
			mSocket = new DatagramSocket(PORT);
			mSocket.setBroadcast(true);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public InetAddress getBroadcastAddress() throws IOException {
		WifiManager wifi = (WifiManager) BattleshipApplication
				.getAppContext().getSystemService(Context.WIFI_SERVICE);
	    DhcpInfo dhcp = wifi.getDhcpInfo();

	    int Broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
	    byte[] quads = new byte[4];
	    for (int k = 0; k < 4; k++)
	      quads[k] = (byte) ((Broadcast >> k * 8) & 0xFF);
	    return InetAddress.getByAddress(quads);
	}
	
	/**
	 * Used by host to Broadcast its own ip over LAN
	 */
	public class SendBroadcast extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... n) {
			try {
				String data = new String("B");
				
				DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), data.length(),
				    getBroadcastAddress(), PORT);
				Log.d(TAG, "Sent Broadcast packet to " + getBroadcastAddress().toString());
				
				long timerStart = System.currentTimeMillis();
				while(System.currentTimeMillis() - timerStart < 30000) {
				mSocket.send(sendPacket);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public String getIPString() {
		return mTargetIP.getHostAddress();
	}
	
	public void setOnBroadcastFoundListener(
			OnBroadcastFoundListener listener) {
		onBroadcastFoundListener = listener;
	}
	
	public static interface OnBroadcastFoundListener {
		public void onBroadcastFound();
	}

	/**
	 * Used by client (P2) to recieve Host's IP
	 */
	public class RecieveBroadcast extends AsyncTask<Void, Void, Void> {
		protected Void doInBackground(Void... n) {
			try {			
				byte[] buf = new byte[1024];
				DatagramPacket recPacket = new DatagramPacket(buf, buf.length);
				
				Log.d(TAG, "Starting to recieve UDP on " + mSocket.getLocalSocketAddress().toString());
				mSocket.receive(recPacket);
				
				mTargetIP = recPacket.getAddress();
				
				Log.d(TAG, "Received UDP Packet: " + buf + " from " + mTargetIP.toString());
				
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(TAG, "Possible timeout in UDPManager");
				e.printStackTrace();
			}
			return null;
		}
		
		protected void onPostExecute(Void n) {
			if (onBroadcastFoundListener != null) {
				onBroadcastFoundListener.onBroadcastFound();
			}
		}
	}
}
