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
	// SHARED: 
	private static NetworkManager NetworkManagerInstance;
	public boolean isHost = false;
	
	// IO Streams
	private static PrintWriter AndroidSocketOutput; 
	private static BufferedReader AndroidSocketInput;
	private static PrintWriter NiosSocketOutput; 		// Android to Nios Output Stream
	private static BufferedReader NiosSocketInput;		// Nios to Android Input Stream
	
	// Threads
	private AndroidReceiverThread receiverThread;
	private ServerThread serverThread;
	
	// Sockets 
	private Socket clientSocket; 
		// Can be both the client socket for the server and the socket for the client (depending on isHost)
	private Socket niosSocket;
	// HOST:
	private ServerSocket serverSocket; 

	// Nios
	private String NiosHostIP = "";
	private int NiosHostPort;
	// Android 
	private String AndroidHostIP= ""; 
	private int AndroidHostPort;
	
	// Listeners
	private OnIPFoundListener onIPFoundListener; 
	private OnGameFoundListener onGameFoundListener; 
	private OnNetworkErrorListener onNetworkErrorListener;
	private OnAndroidDataReceivedListener onAndroidDataReceivedListener;
	private OnNiosDataReceivedListener onNiosDataReceivedListener;
	private OnNiosSuccessfulSetupListener onNiosSuccessfulSetupListener;
	
	private Handler handler;
	
	/* Return the NetworkManager instance*/ 
	public static NetworkManager getInstance()
	{
		if(NetworkManagerInstance == null)
			NetworkManagerInstance = new NetworkManager(); 
		return NetworkManagerInstance;
		
	}
	
	/* Private Constructor for Singleton */
	private NetworkManager()
	{
		handler = new Handler(BattleshipApplication.getAppContext().getMainLooper());
	}
	
	/* Set-up/Teardown */
	
	public void close()
	{
		try {
			clientSocket.close();
			serverSocket.close();
			niosSocket.close();
		} catch (IOException e) {
			Log.d(TAG, "Error closing socket.");
			e.printStackTrace();
		}
		
	}
	
	/* Mutators */
	public void setOnNiosSuccessfulSetupListener (OnNiosSuccessfulSetupListener niosSetupListener)
	{
		onNiosSuccessfulSetupListener = niosSetupListener;
	}
	public void setOnAndroidDataReceivedListener(OnAndroidDataReceivedListener dataListener)
	{
		onAndroidDataReceivedListener = dataListener;
	}
	
	public void setOnNiosDataReceivedListener(OnNiosDataReceivedListener niosListener)
	{
		onNiosDataReceivedListener = niosListener;
	}
	
	public void setOnNetworkErrorListener(OnNetworkErrorListener networkListener)
	{
		onNetworkErrorListener = networkListener;
	}
	
	public void setOnGameFoundListener(OnGameFoundListener gameListener)
	{
		onGameFoundListener = gameListener;
	}
	
	public void setOnIPFoundListener(OnIPFoundListener ipListener)
	{
		onIPFoundListener = ipListener;
	}
	
	
	private void setHost(boolean isHostBool)
	{
		isHost = isHostBool;
	}
	
	public void setupHost()
	{
			serverThread = new ServerThread();
			new Thread(serverThread).start(); // waits for a connection & sets the client socket when it finds one	
	}
	
	public void setupAndroidSocket(String ip, int port, boolean isHostBool) throws UnknownHostException, IOException
	{
		setHost(isHostBool);
		if(isHost)
		{
			Log.d(TAG, "Setting up host");
			setupHost();
		}
		else 
		{
			Log.d(TAG, "Setting up client");
			AndroidHostIP = ip;
			AndroidHostPort = port;
			SetupSocketThread socketSetupThread = new SetupSocketThread(ip, port, false);
			new Thread(socketSetupThread).start();
		}
		
	}
	
	public void setupNiosSocket(String ip, int port) throws UnknownHostException, IOException
	{
		NiosHostIP = ip; 
		NiosHostPort = port;
		SetupSocketThread socketThread = new SetupSocketThread(ip, port, true);
		new Thread(socketThread).start();
	}
	
	public Socket setupSocket(String ip, int port) throws UnknownHostException, IOException
	{
		InetAddress inet = InetAddress.getByName(ip);
		return new Socket(inet, port);
	}
	
	
	/* Accessors */
	
	public String getAndroidHostIP()
	{
		return AndroidHostIP;
	}
	
	public int getAndroidHostPort()
	{
		return AndroidHostPort;
	}
	
	public Socket getClientSocket()
	{
		return clientSocket;
	}
	
	public ServerSocket getServerSocket()
	{
		return serverSocket;
	}
	
	public PrintWriter getNiosSocketOutput()
	{
		if(NiosSocketOutput == null)
		{
			try {
				NiosSocketOutput =  new PrintWriter(new BufferedWriter(new OutputStreamWriter(niosSocket.getOutputStream())), true);
			} catch (IOException e) {
				Log.d(TAG, "Error with socket writer");
				e.printStackTrace();
			}
		}
		return NiosSocketOutput; 
	}
	
	public BufferedReader getNiosSocketInput()
	{
		if(NiosSocketInput == null)
			try {
				NiosSocketInput = new BufferedReader(new InputStreamReader(niosSocket.getInputStream()));
			} catch (IOException e) {
				Log.d(TAG, "Error with socket reader");
				e.printStackTrace();
			}
		return NiosSocketInput; 
	}
	
	public PrintWriter getAndroidSocketOutput()
	{
		if(AndroidSocketOutput == null)
		{
			try {
				AndroidSocketOutput =  new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
			} catch (IOException e) {
				Log.d(TAG, "Error with socket writer");
				e.printStackTrace();
			}
		}
		return AndroidSocketOutput; 
	}
	
	public BufferedReader getAndroidSocketInput()
	{
		if(AndroidSocketInput == null)
			try {
				AndroidSocketInput = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
			} catch (IOException e) {
				Log.d(TAG, "Error with socket reader");
				e.printStackTrace();
			}
		return AndroidSocketInput; 
	}
	
	public class SetupSocketThread implements Runnable
	{
		String ipAddress; 
		int portNum; 
		boolean isNios;
		
		SetupSocketThread(String ip, int port, boolean isNiosBool)
		{
			ipAddress = ip; 
			portNum = port;
			isNios = isNiosBool;
		}

		@Override
		public void run() {
				try {
					if(isNios)
					{
						niosSocket = setupSocket(ipAddress, portNum);
						Runnable successfulSetup = new Runnable() {
        					@Override
        					public void run() {
        						if(onNiosSuccessfulSetupListener != null)
        							onNiosSuccessfulSetupListener.SetupNiosSuccessfully();
        					}
        				};
        				handler.post(successfulSetup);
						Log.d(TAG, "Set up NIOS Socket");
						new Thread(new NiosReceiverThread()).start();
					}
					else 
					{
						clientSocket = setupSocket(ipAddress, portNum);
						
						// CLIENT WAS SUCCESSFULLY CONNECTED TO THE HOST! 
						new Thread(new AndroidReceiverThread()).start();
						Runnable gameFoundRunnable = new Runnable() {
							@Override
							public void run() {
								if(onGameFoundListener != null)
									onGameFoundListener.onGameFound();
								
							}
						};
						handler.post(gameFoundRunnable);
					}
				} catch (UnknownHostException e) {
					Log.d(TAG, "Unknown Host");
					e.printStackTrace();
				} catch (IOException e) {
					Log.d(TAG, "IOException");
					Runnable socketErrorRunnable = new Runnable() {
						@Override
						public void run() {
							if(onNetworkErrorListener != null)
								onNetworkErrorListener.onClientSocketError();
						}
					};
					handler.post(socketErrorRunnable);
					e.printStackTrace();
				}
		}
		
	}
	
	public class ServerThread implements Runnable
	{

		@Override
		public void run() {
			// Wait for connections
			try {
				serverSocket = new ServerSocket(0);
				AndroidHostIP = getLocalIpAddress();
				AndroidHostPort = serverSocket.getLocalPort(); 
				Runnable ipRunnable = new Runnable() {
					
					@Override
					public void run() {
						if(onIPFoundListener != null)
						{
							onIPFoundListener.onIPFound(getAndroidHostIP(), getAndroidHostPort());
						}
					}
				}; 
				handler.post(ipRunnable);
				clientSocket = serverSocket.accept();
				
				// HOST SUCCESSFULLY FOUND A CLIENT! (accept() blocks until it finds a client)
				new Thread(new AndroidReceiverThread()).start();
				Log.d(TAG, "Connected!");
				Runnable gameFoundRunnable = new Runnable() {
					@Override
					public void run() {
						if(onGameFoundListener != null)
							onGameFoundListener.onGameFound();
						
					}
				};
				handler.post(gameFoundRunnable);
				
			} catch (IOException e) {
				Log.d(TAG, "Thread Error");
				e.printStackTrace();
			}
			
			
		};	
	}
	
	public void send(String message, boolean toAndroid)
	{
		SenderThread sendThread = new SenderThread(message, toAndroid);
		new Thread(sendThread).start();
	}
	
	public class SenderThread implements Runnable
	{
		String message;
		boolean sendToAndroid;
		public SenderThread(String msg, boolean toAndroid)
		{
			message = msg;
			sendToAndroid = toAndroid;
		}

		@Override
		public void run() {
			Log.d(TAG, "Sending message: " + message);
			if(sendToAndroid)
				getAndroidSocketOutput().println(message);
			else 
				getNiosSocketOutput().println(message);
		};	
	}
	
	public class NiosReceiverThread implements Runnable
	{
		@Override
		public void run() {
			Log.d(TAG, "Made Nios Receiver thread");
			while(true)
			{
                try {
                	final String receivedString = readFromInput(getNiosSocketInput());
                	
                	Runnable niosDataReceived = new Runnable() {
    					@Override
    					public void run() {
    						if(onNiosDataReceivedListener != null)
    							onNiosDataReceivedListener.ReceivedNiosData(receivedString);
    					}
    				};
    				handler.post(niosDataReceived);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		};	
	}
	
	public class AndroidReceiverThread implements Runnable
	{

		@Override
		public void run() {
			Log.d(TAG, "Made Receiver thread");
			Log.d(TAG, "onAndroidDataReceivedListener: " + onAndroidDataReceivedListener);
			while(true)
			{
                try {
                		final String receivedString = readFromInput(getAndroidSocketInput());
                		Runnable gameFoundRunnable = new Runnable() {
        					@Override
        					public void run() {
        						if(onAndroidDataReceivedListener != null)
        							onAndroidDataReceivedListener.ReceivedAndroidData(receivedString);
        					}
        				};
        				handler.post(gameFoundRunnable);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		};	
	}
	
	public static String readFromInput(BufferedReader br) throws IOException {
		String line = null;
		StringBuilder sb = new StringBuilder("");
        	while ((line = br.readLine()) != null) {
        		Log.d(TAG, "Received: " + line);
        		sb.append(line);
			}
        	return sb.toString();
	}
	
	public static interface OnIPFoundListener {
		public void onIPFound(String IP, int port);
	}
	
	public static interface OnGameFoundListener {
		public void onGameFound();
	}
	
	public static interface OnNetworkErrorListener {
		public void onClientSocketError(); 
	}
	
	public static interface OnAndroidDataReceivedListener {
		public void ReceivedAndroidData(String message);
	}
	
	public static interface OnNiosDataReceivedListener {
		public void ReceivedNiosData(String message);
	}
	
	public static interface OnNiosSuccessfulSetupListener { 
		public void SetupNiosSuccessfully();
	}
	
	private static String getLocalIpAddress()
	{
		WifiManager wifiManager = (WifiManager) BattleshipApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);
		return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
	}	
}
