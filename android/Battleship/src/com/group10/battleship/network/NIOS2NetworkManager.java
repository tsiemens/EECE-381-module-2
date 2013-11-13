// NIOS2 Communication Static Functions
package com.group10.battleship.network;

public class NIOS2NetworkManager {

	// TODO: change send to NIOS
	
	// new_game = bytes: { ‘G’, [2 byte little endian short short for port], ip
	// string }
	public static void sendNewGame(int port, int ip) {
		byte[] conn_port = intToByteArray(port, 2);
		byte[] conn_ip = intToByteArray(ip, 0);
		byte[] conn = (byte[]) combineArray(conn_port, conn_port.length,
				conn_ip, conn_ip.length);
		NetworkManager.getInstance().send(new String(conn));
	}

	// bytes { ‘C‘ }
	public static void sendConfirmation() {
		byte[] data = { (byte) 'C' };
		NetworkManager.getInstance().send(new String(data));
	}

	// bytes: { 'M', ['1' or '2' for board this affects], [1 byte x coord], [1
	// byte y coord] }
	public static void sendMiss(int player, int x, int y) {
		byte[] data = { (byte) 'M', (byte) player, (byte) x, (byte) y };
		NetworkManager.getInstance().send(new String(data));
	}

	// bytes: { 'H', ['1' or '2' for board this affects], [1 byte x coord], [1
	// byte y coord] }
	public static void sendHit(int player, int x, int y) {
		byte[] data = { (byte) 'H', (byte) player, (byte) x, (byte) y };
		NetworkManager.getInstance().send(new String(data));
	}

	// bytes: { 'O', ['1' or '2' for winner], ['1’ for forfeit/quit midgame or
	// ‘0’ for not forfeit] }
	public static void sendGameOver(int winner, Boolean forfeit) {
		byte[] data = { (byte) winner, (byte) (forfeit ? 1 : 0) };
		NetworkManager.getInstance().send(new String(data));
	}

	// combines 2 arrays
	private static Object combineArray(Object l1, int l1Size, Object l2,
			int l2Size) {
		Object[] result = new Object[l1Size + l2Size];
		System.arraycopy(l1, 0, result, 0, l1Size);
		System.arraycopy(l2, 0, result, l1Size, l2Size);
		return result;
	}

	// convert int to array
	// if size is 0, convert entire int
	// otherwise size is number of bytes to convert to
	private static byte[] intToByteArray(int convert, int size) {
		int arraySize;
		if (size == 0)
			arraySize = 4;
		else
			arraySize = size;

		byte[] result = new byte[arraySize];

		for (int i = 0; i < arraySize; i++) {
			int offset = i * 8;
			result[i] = (byte) ((convert >>> offset) & 0xFF);
		}
		return result;
	}
}
