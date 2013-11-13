/* Network Manager Singleton */
package com.group10.battleship.network;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;

import com.group10.battleship.BattleshipApplication;


@SuppressWarnings("deprecation")
public class NetworkManager extends Object
{
	private static final String TAG = NetworkManager.class.getSimpleName();
	// SHARED: 
	private static NetworkManager NetworkManagerInstance;
	public boolean isHost = false;
	
	// IO Streams
	private static PrintWriter socketOutput; 
	private static BufferedReader socketInput;
	
	// Threads
	private ReceiverThread receiverThread;
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
		} catch (IOException e) {
			Log.d(TAG, "Error closing socket.");
			e.printStackTrace();
		}
		
	}
	
	/* Mutators */
	
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
	
	public PrintWriter getSocketOutput()
	{
		if(socketOutput == null)
		{
			try {
				socketOutput =  new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())), true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return socketOutput; 
	}
	
	public BufferedReader getSocketInput()
	{
		return socketInput; 
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
						niosSocket = setupSocket(ipAddress, portNum);
					else 
						clientSocket = setupSocket(ipAddress, portNum);
				} catch (UnknownHostException e) {
					Log.d(TAG, "Unknown Host");
					e.printStackTrace();
				} catch (IOException e) {
					Log.d(TAG, "IOException");
					Runnable socketErrorRunnable = new Runnable() {
						@Override
						public void run() {
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
	
	public void send(String message)
	{
		SenderThread sendThread = new SenderThread(message);
		new Thread(sendThread).start();
	}
	
	public class SenderThread implements Runnable
	{
		String message; 
		public SenderThread(String msg)
		{
			message = msg;
		}

		@Override
		public void run() {
			getSocketOutput().println(message);
		};	
	}
	
	public class ReceiverThread implements Runnable
	{

		@Override
		public void run() {
			
		};	
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
	
	public static String getLocalIpAddress()
	{
		WifiManager wifiManager = (WifiManager) BattleshipApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);
		return Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
	}

	
}





