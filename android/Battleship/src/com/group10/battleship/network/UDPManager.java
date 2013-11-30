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
	private static final int BROADCAST_PORT = 50002;
	private static final int RECIEVING_PORT = 50003;
	private InetAddress mTargetIP;
	private int mPort;
	private DatagramSocket mBroadcastSocket;
	private DatagramSocket mRecievingSocket;
	private OnBroadcastFoundListener onBroadcastFoundListener;
	private boolean mStopOperations = false;
	
	public UDPManager() {
		try {
			mBroadcastSocket = new DatagramSocket(BROADCAST_PORT);
			mBroadcastSocket.setSoTimeout(3000);
			mBroadcastSocket.setBroadcast(true);
			
			mRecievingSocket = new DatagramSocket(RECIEVING_PORT);
			mRecievingSocket.setSoTimeout(3000);
			mRecievingSocket.setBroadcast(true);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	
	private void resetSocket() {
		mBroadcastSocket.disconnect();
		mBroadcastSocket.close();
		
		mRecievingSocket.disconnect();
		mRecievingSocket.close();
		
		try {
			mBroadcastSocket = new DatagramSocket(BROADCAST_PORT);
			mBroadcastSocket.setSoTimeout(30000);
			mBroadcastSocket.setBroadcast(true);
			
			mRecievingSocket = new DatagramSocket(RECIEVING_PORT);
			mRecievingSocket.setSoTimeout(30000);
			mRecievingSocket.setBroadcast(true);

		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		Log.d(TAG, "UDP Socket Reset");
	}
	
	private void close() {
		if (mBroadcastSocket != null) {
			mBroadcastSocket.disconnect();
			mBroadcastSocket.close();
			mBroadcastSocket = null;
		}
		
		if (mRecievingSocket != null) {
			mRecievingSocket.disconnect();
			mRecievingSocket.close();
			mRecievingSocket = null;
		}
	}
	public void cancelOperations() {
		mStopOperations = true;
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
				mStopOperations = false;
				String data = new String("B");
				
				DatagramPacket sendPacket = new DatagramPacket(data.getBytes(), data.length(),
				    getBroadcastAddress(), RECIEVING_PORT);
				
	
				mBroadcastSocket.send(sendPacket);
				Log.d(TAG, "Sent Broadcast packet to " + getBroadcastAddress().toString());
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		protected void onPostExecute(Void n) {
		}
	}
	
	public String getIPString() {
		if (mTargetIP == null)
			return null;
		return mTargetIP.getHostAddress();
	}
	
	public String getPortString() {
		return Integer.toString(mPort);
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
				
				Log.d(TAG, "Starting to recieve UDP on " + mRecievingSocket.getLocalSocketAddress().toString());
				
				do {
					mRecievingSocket.receive(recPacket);
				} while (recPacket.getAddress().getHostAddress().contentEquals(NetworkManager.getLocalIpAddress()));
				Log.d(TAG, recPacket.getAddress().getHostAddress() + " local: " + NetworkManager.getLocalIpAddress());
				mTargetIP = recPacket.getAddress();
				mPort = recPacket.getPort();
				
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
