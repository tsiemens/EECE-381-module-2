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
import android.os.AsyncTask;
import android.os.Handler;
import android.text.format.Formatter;
import android.util.Log;

import com.group10.battleship.BattleshipApplication;

@SuppressWarnings("deprecation")
public class NetworkManager extends Object {
	private static final String TAG = NetworkManager.class.getSimpleName();
	private static NetworkManager NetworkManagerInstance;
	private static int PORT = 50002; 
	public boolean mIsHost = false;


	// IO Streams
	private PrintWriter mAndroidSocketOutput;
	private BufferedReader mAndroidSocketInput;
	private PrintWriter mNiosSocketOutput; // Android to Nios Output Stream
	private int mAndroidSocketVersion = 0;

	// Sockets
	private Socket mClientSocket;
	// Can be both the client socket for the server and the socket for the
	// client (depending on isHost)
	private Socket mNiosSocket;
	private ServerSocket mServerSocket;
	// IP & Ports
	private String mNiosHostIP = "";
	private int mNiosHostPort;
	private String mAndroidHostIP = "";
	private int mAndroidHostPort;

	// Listeners
	private OnAndroidSocketSetupListener onAndroidSocketSetupListener;
	private OnNiosSocketSetupListener onNiosSocketSetupListener;
	private OnAndroidDataReceivedListener onAndroidDataReceivedListener;

	private Handler mHandler;
	private boolean mIsConnected = false;
	// Private Constructor for Singleton
	private NetworkManager() {
		mHandler = new Handler(BattleshipApplication.getAppContext()
				.getMainLooper());
	}

	// Return the NetworkManager instance
	public static NetworkManager getInstance() {
		if (NetworkManagerInstance == null)
			NetworkManagerInstance = new NetworkManager();
		return NetworkManagerInstance;
	}

	public void endConnections()
	{
		mIsHost = false;
		mIsConnected = false;
		mHandler.post(
				new Runnable() {
					@Override
					public void run() {
						try {
							mServerSocket.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				});
	}

	public void close() {
		try {
			mAndroidSocketVersion++;
			if (mClientSocket != null) {
				mClientSocket.close();
				mClientSocket = null;
			}
			
			if (mAndroidSocketInput != null) {
				mAndroidSocketInput.close();
				mAndroidSocketInput = null;
			}
			
			if (mAndroidSocketOutput != null) {
				mAndroidSocketOutput.close();
				mAndroidSocketOutput = null;
			}
			
			if (mServerSocket != null) {
				mServerSocket.close();
				mServerSocket = null;
			}
			if (mNiosSocket != null) {
				mNiosSocketOutput.close();
				mNiosSocketOutput = null;
				mNiosSocket.close();
				mNiosSocket = null;
			}
		} catch (IOException e) {
			Log.d(TAG, "Error closing socket.");
			e.printStackTrace();
		}

	}

	public void setPort(int port) {
		this.PORT = port;
	}

	// SETUP LISTENERS
	public void setOnAndroidSocketSetupListener(
			OnAndroidSocketSetupListener listener) {
		onAndroidSocketSetupListener = listener;
	}

	public void setOnNiosSocketSetupListener(OnNiosSocketSetupListener listener) {
		onNiosSocketSetupListener = listener;
	}

	public void setOnAndroidDataReceivedListener(
			OnAndroidDataReceivedListener listener) {
		onAndroidDataReceivedListener = listener;
	}

	// SOCKET SETUP
	public void setupAndroidSocket(String ip, int port, boolean isHostBool)
			throws UnknownHostException, IOException {
		mIsHost = isHostBool;
		if (mIsHost) {
			Log.d(TAG, "Setting up host");
			// waits for a connection & sets the client socket when it finds one
			(new ServerSocketSetupTask()).execute();
		} else {
			Log.d(TAG, "Setting up client");
			mAndroidHostIP = ip;
			mAndroidHostPort = port;
			SetupSocketTask socketSetupThread = new SetupSocketTask();
			socketSetupThread.execute(ip, port, false);
		}
	}

	public void setupNiosSocket(String ip, int port)
			throws UnknownHostException, IOException {
		mNiosHostIP = ip;
		mNiosHostPort = port;
		SetupSocketTask socketThread = new SetupSocketTask();
		socketThread.execute(mNiosHostIP, mNiosHostPort, true);
	}

	public Socket setupSocket(String ip, int port) throws UnknownHostException,
	IOException {
		InetAddress inet = InetAddress.getByName(ip);
		return new Socket(inet, port);
	}

	// ACCESSORS
	public boolean isHost() {
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

	// getNiosSocketOutput: Returns the NIOS socket output stream
	// Initializes an output stream if null
	public PrintWriter getNiosSocketOutput() {
		if (mNiosSocketOutput == null) {
			try { 
				mNiosSocketOutput = new PrintWriter(new BufferedWriter(
						new OutputStreamWriter(mNiosSocket.getOutputStream())), true);
			} catch (Exception e) {
				Log.d(TAG, "Error with socket writer");
				e.printStackTrace();
			}
		}
		return mNiosSocketOutput;
	}

	// getAndroidSocketOutput: Returns an Android socket output stream
	// Initializes an output stream if null
	public PrintWriter getAndroidSocketOutput() {
		if (mAndroidSocketOutput == null) {
			try {
				mAndroidSocketOutput = new PrintWriter(
						new BufferedWriter(new OutputStreamWriter(
								mClientSocket.getOutputStream())), true);
			} catch (Exception e) {
				Log.d(TAG, "Error with socket writer");
				e.printStackTrace();
			}
		}
		return mAndroidSocketOutput;
	}

	// getAndroidSocketInput: Returns an Android input stream
	// Initializes an input stream if null
	public BufferedReader getAndroidSocketInput() {
		Log.d(TAG, "Attempting to create new Socket Input");
		if (mAndroidSocketInput == null)
			try {
				Log.d(TAG, "Creating new Socket Input");
				if(mClientSocket == null) return null; 
				mAndroidSocketInput = new BufferedReader(new InputStreamReader(
						mClientSocket.getInputStream()));
			} catch (IOException e) { 
				Log.d(TAG, "Error with socket reader");
				e.printStackTrace();
			}
		return mAndroidSocketInput;
	}

	// Send: Sends a message using a new send message runnable
	public void send(String message, boolean toAndroid) {
		SendMessageTask sendThread = new SendMessageTask();
		sendThread.execute(message, toAndroid);
	}

	// RUNNABLES
	// SocketSetupTask: Sets up a socket (Android client / NIOS socket
	// Notifies listeners that the setup was successful/is connected
	public class SetupSocketTask extends AsyncTask<Object, Void, Object> {

		String ipAddress;
		Integer portNum;
		Boolean isNios;

		@Override
		protected Object doInBackground(Object... params) {
			Log.d(TAG, "is setting up NIOS socket");
			ipAddress = (String) params[0];
			portNum = (Integer) params[1];
			isNios = (Boolean) params[2];

			try {
				if (isNios) {
					if (mNiosSocket != null) {
						mNiosSocketOutput.close();
						mNiosSocketOutput = null;
						mNiosSocket.close();
						mNiosSocket = null;
					}
					mNiosSocket = setupSocket(ipAddress, portNum);
					mNiosSocket.setKeepAlive(true);
					Log.d(TAG, "Set up NIOS Socket");
				} else {
					mAndroidSocketVersion++;
					
					if (mAndroidSocketInput != null) {
						mAndroidSocketInput.close();
						mAndroidSocketInput = null;
					}
					
					if (mAndroidSocketOutput != null) {
						mAndroidSocketOutput.close();
						mAndroidSocketOutput = null;
					}
					if (mClientSocket != null) {
						mClientSocket.close();
						mClientSocket = null;
					}
					mClientSocket = setupSocket(ipAddress, portNum);
					mClientSocket.setKeepAlive(true);
					// CLIENT WAS SUCCESSFULLY CONNECTED TO THE HOST!
					new Thread(new ReceiveMessageRunnable()).start();
				}
			} catch (UnknownHostException e) {
				Log.d(TAG, "Unknown Host");
				e.printStackTrace();
			} catch (IOException e) {
				Log.d(TAG, "IOException");
				e.printStackTrace();
				return e;
			} catch (Exception e)
			{
				Log.d(TAG, "exception");
				return e; 
			}
			return null;
		}

		protected void onPostExecute(Object result) {
			if (isNios) {
				if (onNiosSocketSetupListener != null )
				{
					if (mNiosSocket == null)
						onNiosSocketSetupListener.onNiosSocketSetupError();
					else 
						onNiosSocketSetupListener.onSuccessfulNiosSetup();
				}
			} else {
				if (mClientSocket != null) {
					mIsConnected = true;
					if (onAndroidSocketSetupListener != null)
						onAndroidSocketSetupListener.onGameFound();
				} else {
					if (onAndroidSocketSetupListener != null)
						onAndroidSocketSetupListener.onAndroidSocketSetupError();
				}
			}
		}

	}

	// ServerSocketSetupTask: Sets up the server socket & blocks until a
	// client connects,
	// then notifies listener that is has been connected to a client
	private class ServerSocketSetupTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// Wait for connections
			try {
				if (mServerSocket != null) {
					mServerSocket.close();
				}
				mServerSocket = new ServerSocket(PORT);

				if (mServerSocket.getLocalPort() == -1)
					mServerSocket = new ServerSocket(0);

				mAndroidHostIP = getLocalIpAddress();
				mAndroidHostPort = mServerSocket.getLocalPort();

				Runnable ipRunnable = new Runnable() {
					@Override
					public void run() {
						if (onAndroidSocketSetupListener != null) {
							onAndroidSocketSetupListener.onFoundIPAddress(
									getAndroidHostIP(), getAndroidHostPort());
						}
					}
				};
				mHandler.post(ipRunnable);

				mAndroidSocketVersion++;
				if (mClientSocket != null) {
					mClientSocket.close();
					mClientSocket = null;
				}
				if (mAndroidSocketInput != null) {
					mAndroidSocketInput.close();
					mAndroidSocketInput = null;
				}
				
				if (mAndroidSocketOutput != null) {
					mAndroidSocketOutput.close();
					mAndroidSocketOutput = null;
				}
				Log.d(TAG, "Waiting for guest on IP: " + getAndroidHostIP() + " : " + getAndroidHostPort());
				mClientSocket = mServerSocket.accept();
				mIsConnected = true;

				// HOST SUCCESSFULLY FOUND A CLIENT! (accept() blocks until it
				// finds a client)
				new Thread(new ReceiveMessageRunnable()).start();
				Log.d(TAG, "Connected!");

			}catch (IOException e) {
				Log.d(TAG, "Thread Error");
				e.printStackTrace();
			}
			return null;
		}

		protected void onPostExecute(Void params) {
			if (onAndroidSocketSetupListener != null) {
				if(mIsConnected)
					onAndroidSocketSetupListener.onGameFound();
			}
		}

	}


	// SendMessageTask: New task to send a message, either to an Android
	// device or the NIOS
	// Is created every time a message needs to be sent & is discarded after
	// every message
	class SendMessageTask extends AsyncTask<Object, Void, Void> {

		@Override
		synchronized protected Void doInBackground(Object... params) {
			String message = (String) params[0];
			Boolean sendToAndroid = (Boolean) params[1];
			if (sendToAndroid) {
				Log.d(TAG, "Sending message to Android: " + message);
				getAndroidSocketOutput().println(message);
			}
			else {
				Log.d(TAG, "Sending message to NIOS: " + message);
				if(getNiosSocketOutput() != null) {
					getNiosSocketOutput().println((char) (message.length() + 1) + message);
					getNiosSocketOutput().flush();
				}
			}
			return null;
		};
	}

	// ReceiveMessageRunnable: Receive message thread (always alive, one per
	// client/NIOS socket)
	// Is created when connection is made
	public class ReceiveMessageRunnable implements Runnable {
		private int currentSocketVersion;

		public ReceiveMessageRunnable() {
			currentSocketVersion = mAndroidSocketVersion;
		}

		@SuppressWarnings("resource")
		@Override
		public void run() {
			if(mIsConnected == false) return;
			Log.d(TAG, "Made Receiver thread");
			while (true) {
				String line = null;
				// If the socket has changed, then there is another duplicate
				// thread.
				// Kill this one
				if (mAndroidSocketVersion != currentSocketVersion) {
					return;
				}

				try {
					BufferedReader input = getAndroidSocketInput();
					while (input != null && (line = input.readLine()) != null) {
						Log.d(TAG, "Received: " + line);
						final String receivedString = line;
						Runnable dataReceivedRunnable = new Runnable() {
							@Override
							public void run() {
								if (onAndroidDataReceivedListener != null)
									onAndroidDataReceivedListener
									.ReceivedAndroidData(receivedString);
							}
						};
						mHandler.post(dataReceivedRunnable);
//						input = getAndroidSocketInput();
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

	public static String getLocalIpAddress() {
		WifiManager wifiManager = (WifiManager) BattleshipApplication
				.getAppContext().getSystemService(Context.WIFI_SERVICE);
		return Formatter.formatIpAddress(wifiManager.getConnectionInfo()
				.getIpAddress());
	}

	public Object getNiosSocket() {
		return mNiosSocket;
	}

}