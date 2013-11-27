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

import com.group10.battleship.BattleshipApplication;
import com.group10.battleship.network.NetworkManager.OnAndroidSocketSetupListener;

public class UDPManager {
	private static final String TAG = UDPManager.class.getSimpleName();
	private static final int PORT = 50002;
	private InetAddress targetIP;
	private DatagramSocket socket;
	private OnUDPRecieveListener onUDPRecieveListener;
	
	public UDPManager() {
		try {
			socket = new DatagramSocket(PORT);
			socket.setBroadcast(true);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	public InetAddress getBroadcastAddress() throws IOException {
		WifiManager wifi = (WifiManager) BattleshipApplication
				.getAppContext().getSystemService(Context.WIFI_SERVICE);
	    DhcpInfo dhcp = wifi.getDhcpInfo();

	    int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
	    byte[] quads = new byte[4];
	    for (int k = 0; k < 4; k++)
	      quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
	    return InetAddress.getByAddress(quads);
	}
	
	/**
	 * Used by host to broadcast its own ip over LAN
	 */
	public class SendBroadcast extends AsyncTask<Void, Void, Void> {

		protected Void doInBackground(Void... n) {
			try {
				String data = new String("B");
				DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), data.length(),
				    getBroadcastAddress(), PORT);
				
				socket.send(sendPacket);
				
				Log.d(TAG, "Sent broadcast packet to " + getBroadcastAddress().toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	public String getIPString() {
		return targetIP.getHostAddress();
	}

	/**
	 * Used by client (P2) to recieve Host's IP
	 */
	public class RecieveBroadcast extends AsyncTask<Void, Void, Void> {
		
		protected Void doInBackground(Void... n) {
			try {			
				byte[] buf = new byte[1024];
				DatagramPacket recPacket = new DatagramPacket(buf, buf.length);
				
				Log.d(TAG, "Starting to recieve UDP on " + socket.getLocalSocketAddress().toString());
				socket.receive(recPacket);
				
				targetIP = recPacket.getAddress();
				
				Log.d(TAG, "Received UDP Packet: " + buf + " from " + targetIP.toString());
				
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(TAG, "Possible timeout in UDPManager");
				e.printStackTrace();
			}
			return null;
		}
		
		protected void onPostExecute(Void n) {
			if (onUDPRecieveListener != null) {
				onUDPRecieveListener.onUDPRecieved();
			}
		}
	}
	
	public void setOnUDPRecieveListener(OnUDPRecieveListener listener) {
		onUDPRecieveListener = listener;
	}
	
	public interface OnUDPRecieveListener {
		public void onUDPRecieved();
	}
}
