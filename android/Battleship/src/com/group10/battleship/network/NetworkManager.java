/* Network Manager Singleton */
package com.group10.battleship.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;

import com.group10.battleship.BattleshipApplication;
import com.group10.battleship.PrefsManager;


@SuppressWarnings("deprecation")
public class NetworkManager extends Object
{
	private static final String TAG = NetworkManager.class.getSimpleName();
	private static NetworkManager NetworkManagerInstance;
	public boolean mIsHost = false;
	
//	IO Streams
	private PrintWriter mAndroidSocketOutput; 
	private BufferedReader mAndroidSocketInput;
	private PrintWriter mNiosSocketOutput; 		// Android to Nios Output Stream
	private int mAndroidSocketVersion = 0; 
	
//	Sockets 
	private Socket mClientSocket; 
		// Can be both the client socket for the server and the socket for the client (depending on isHost)
	private Socket mNiosSocket;
	private ServerSocket mServerSocket; 
//	IP & Ports 
	private String mNiosHostIP = "";
	private int mNiosHostPort;
	private String mAndroidHostIP= ""; 
	private int mAndroidHostPort;
	
	// Listeners
		private OnAndroidSocketSetupListener onAndroidSocketSetupListener; 
		private OnNiosSocketSetupListener onNiosSocketSetupListener;
		private OnAndroidDataReceivedListener onAndroidDataReceivedListener;
	
	private Handler mHandler;
	
//	Private Constructor for Singleton
	private NetworkManager() {
		mHandler = new Handler(BattleshipApplication.getAppContext().getMainLooper());
	}
	
//	Return the NetworkManager instance
	public static NetworkManager getInstance() {
		if(NetworkManagerInstance == null)
			NetworkManagerInstance = new NetworkManager(); 
		return NetworkManagerInstance;
	}
		
	public void close() {
		try {
			mAndroidSocketVersion++;
			if (mClientSocket != null)
				mClientSocket.close();
			if (mServerSocket != null)
				mServerSocket.close();
			if (mNiosSocket != null)
				mNiosSocket.close();
		} catch (IOException e) {
			Log.d(TAG, "Error closing socket.");
			e.printStackTrace();
		}
		
	}
	
//	SETUP LISTENERS
	public void setOnAndroidSocketSetupListener (OnAndroidSocketSetupListener listener) {
		onAndroidSocketSetupListener = listener;
	}
	
	public void setOnNiosSocketSetupListener (OnNiosSocketSetupListener listener) {
		onNiosSocketSetupListener = listener;
	}
	
	public void setOnAndroidDataReceivedListener (OnAndroidDataReceivedListener listener) {
		onAndroidDataReceivedListener = listener;
	}
	
//	SOCKET SETUP
	private void setHost(boolean isHostBool) {
		mIsHost = isHostBool;
	}
	
	public void setupHost() {
			new Thread(new ServerSocketSetupRunnable()).start(); // waits for a connection & sets the client socket when it finds one	
	}
	
	public void setupAndroidSocket(String ip, int port, boolean isHostBool) throws UnknownHostException, IOException
	{
		setHost(isHostBool);
		if(mIsHost)
		{
			Log.d(TAG, "Setting up host");
			setupHost();
		}
		else 
		{
			Log.d(TAG, "Setting up client");
			mAndroidHostIP = ip;
			mAndroidHostPort = port;
			SetupSocketRunnable socketSetupThread = new SetupSocketRunnable(ip, port, false);
			new Thread(socketSetupThread).start();
		}
		
	}
	
	public void setupNiosSocket(String ip, int port) throws UnknownHostException, IOException
	{
		mNiosHostIP = ip; 
		mNiosHostPort = port;
		SetupSocketRunnable socketThread = new SetupSocketRunnable(mNiosHostIP, mNiosHostPort, true);
		new Thread(socketThread).start();
	}
	
	public Socket setupSocket(String ip, int port) throws UnknownHostException, IOException
	{
		InetAddress inet = InetAddress.getByName(ip);
		return new Socket(inet, port);
	}
	
//	ACCESSORS	
	public boolean getIsHost() {
		return mIsHost;
	}
	
	public String getAndroidHostIP() {
		return mAndroidHostIP;
	}
	
	public int getAndroidHostPort() {
		return mAndroidHostPort;
	}
	
	public Socket getClientSocket() {
		return mClientSocket;
	}
	
	public ServerSocket getServerSocket() {
		return mServerSocket;
	}
	
//	getNiosSocketOutput: Returns the NIOS socket output stream
//	 					 Initializes an output stream if null
	public PrintWriter getNiosSocketOutput() {
		if(mNiosSocketOutput == null)
		{
			try {
				mNiosSocketOutput =  new PrintWriter(new BufferedWriter(new OutputStreamWriter(mNiosSocket.getOutputStream())), true);
			} catch (IOException e) {
				Log.d(TAG, "Error with socket writer");
				e.printStackTrace();
			}
		}
		return mNiosSocketOutput; 
	}
	
//	getAndroidSocketOutput: Returns an Android socket output stream
//							Initializes an output stream if null
	public PrintWriter getAndroidSocketOutput() {
		if(mAndroidSocketOutput == null)
		{
			try {
				mAndroidSocketOutput =  new PrintWriter(new BufferedWriter(new OutputStreamWriter(mClientSocket.getOutputStream())), true);
			} catch (IOException e) {
				Log.d(TAG, "Error with socket writer");
				e.printStackTrace();
			}
		}
		return mAndroidSocketOutput; 
	}
	
//	getAndroidSocketInput: Returns an Android input stream
//						   Initializes an input stream if null
	public BufferedReader getAndroidSocketInput() {
		if(mAndroidSocketInput == null)
			try {
				mAndroidSocketInput = new BufferedReader(new InputStreamReader(mClientSocket.getInputStream()));
			} catch (IOException e) {
				Log.d(TAG, "Error with socket reader");
				e.printStackTrace();
			}
		return mAndroidSocketInput; 
	}
	
//	Send: Sends a message using a new send message runnable
	public void send(String message, boolean toAndroid) {
		if(!toAndroid && !PrefsManager.getInstance().getBoolean(PrefsManager.PREF_KEY_USE_NIOS, false)) return;
		SendMessageRunnable sendThread = new SendMessageRunnable(message, toAndroid);
		new Thread(sendThread).start();
	}
	
//	RUNNABLES
//	SocketSetupRunnable: Sets up a socket (Android client / NIOS socket
//	 					 Notifies listeners that the setup was successful/is connected
	public class SetupSocketRunnable implements Runnable {
		String ipAddress; 
		int portNum; 
		boolean isNios;
		
		SetupSocketRunnable(String ip, int port, boolean isNiosBool) {
			ipAddress = ip; 
			portNum = port;
			isNios = isNiosBool;
		}

		@Override
		public void run() {
				try {
					if(isNios)
					{
						if (mNiosSocket != null) {
							mNiosSocket.close();
						}
						mNiosSocket = setupSocket(ipAddress, portNum);
						Runnable successfulSetup = new Runnable() {
        					@Override
        					public void run() {
        						if(onNiosSocketSetupListener != null)
        							onNiosSocketSetupListener.onSuccessfulNiosSetup();
        					}
        				};
        				mHandler.post(successfulSetup);
						Log.d(TAG, "Set up NIOS Socket");
					}
					else 
					{
						mAndroidSocketVersion++;
						mAndroidSocketInput = null;
						mAndroidSocketOutput = null;
						if (mClientSocket != null) {
							mClientSocket.close();
						}
						mClientSocket = setupSocket(ipAddress, portNum);
						
						// CLIENT WAS SUCCESSFULLY CONNECTED TO THE HOST! 
						new Thread(new ReceiveMessageRunnable()).start();
						Runnable gameFoundRunnable = new Runnable() {
							@Override
							public void run() {
								if(onAndroidSocketSetupListener != null)
									onAndroidSocketSetupListener.onGameFound();
								
							}
						};
						mHandler.post(gameFoundRunnable);
					}
				} catch (UnknownHostException e) {
					Log.d(TAG, "Unknown Host");
					e.printStackTrace();
				} catch (IOException e) {
					Log.d(TAG, "IOException");
					Runnable socketErrorRunnable = new Runnable() {
						@Override
						public void run() {
							if(onAndroidSocketSetupListener != null)
								onAndroidSocketSetupListener.onAndroidSocketSetupError();
						}
					};
					mHandler.post(socketErrorRunnable);
					e.printStackTrace();
				}
		}
		
	}
//	ServerSocketSetupRunnable: Sets up the server socket & blocks until a client connects, 
//	   						   then notifies listener that is has been connected to a client
	public class ServerSocketSetupRunnable implements Runnable {
		@Override
		public void run() {
			// Wait for connections
			try {
				if (mServerSocket != null) {
					mServerSocket.close();
				}
				mServerSocket = new ServerSocket(0);
				mAndroidHostIP = getLocalIpAddress();
				mAndroidHostPort = mServerSocket.getLocalPort(); 
				Runnable ipRunnable = new Runnable() {
					
					@Override
					public void run() {
						if(onAndroidSocketSetupListener != null)
						{
							onAndroidSocketSetupListener.onFoundIPAddress(getAndroidHostIP(), getAndroidHostPort());
						}
					}
				}; 
				mHandler.post(ipRunnable);
				mAndroidSocketVersion++;
				mAndroidSocketInput = null;
				mAndroidSocketOutput = null;
				if (mClientSocket != null) {
					mClientSocket.close();
				}
				mClientSocket = mServerSocket.accept();
				
				// HOST SUCCESSFULLY FOUND A CLIENT! (accept() blocks until it finds a client)
				new Thread(new ReceiveMessageRunnable()).start();
				Log.d(TAG, "Connected!");
				Runnable gameFoundRunnable = new Runnable() {
					@Override
					public void run() {
						if(onAndroidSocketSetupListener != null)
							onAndroidSocketSetupListener.onGameFound();
						
					}
				};
				mHandler.post(gameFoundRunnable);
				
			} catch (IOException e) {
				Log.d(TAG, "Thread Error");
				e.printStackTrace();
			}
		};	
	}
	
//	SendMessageRunnable: New Runnable to send a message, either to an Android device or the NIOS
//						 Is created every time a message needs to be sent & is discarded after every message
	public class SendMessageRunnable implements Runnable {
		String message;
		boolean sendToAndroid;
		public SendMessageRunnable(String msg, boolean toAndroid) {
			message = msg;
			sendToAndroid = toAndroid;
		}

		@Override
		public void run() {
			Log.d(TAG, "Sending message: " + message);
			if(sendToAndroid)
				getAndroidSocketOutput().println(message);
			else 
				getNiosSocketOutput().println((char)(message.length()+1) + message);
		};	
	}
//	ReceiveMessageRunnable: Receive message thread (always alive, one per client/NIOS socket)
//							Is created when connection is made
	public class ReceiveMessageRunnable implements Runnable {
		private int currentSocketVersion;
		
		public ReceiveMessageRunnable() {
			currentSocketVersion = mAndroidSocketVersion;
		}

		@Override
		public void run() {
			Log.d(TAG, "Made Receiver thread");
			while(true)
			{
				String line = null;
				// If the socket has changed, then there is another duplicate thread.
				// Kill this one
				if (mAndroidSocketVersion != currentSocketVersion) {
					return;
				}
				
                try {
                	while ((line = getAndroidSocketInput().readLine()) != null) {
                		Log.d(TAG, "Received: " + line);
                		final String receivedString = line;
                		Runnable gameFoundRunnable = new Runnable() {
        					@Override
        					public void run() {
        						if(onAndroidDataReceivedListener != null)
        							onAndroidDataReceivedListener.ReceivedAndroidData(receivedString);
        					}
        				};
        				mHandler.post(gameFoundRunnable);
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		};	
	}
	
	public static interface OnAndroidSocketSetupListener {
		public void onFoundIPAddress(String ip, int port); 
		public void onGameFound();
		public void onAndroidSocketSetupError();
	}
	
	public static interface OnNiosSocketSetupListener {
		public void onSuccessfulNiosSetup();
		public void onNiosSocketSetupError(); 
	}
	
	public static interface OnAndroidDataReceivedListener {
		public void ReceivedAndroidData(String message);
	}
	
	private static String getLocalIpAddress() {
		WifiManager wifiManager = (WifiManager) BattleshipApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);
		return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
	}	
}
