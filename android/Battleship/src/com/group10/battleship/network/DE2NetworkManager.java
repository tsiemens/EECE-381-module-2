package com.group10.battleship.network;

public class DE2NetworkManager {

	// network variables
	// middleman IP

	public DE2NetworkManager() {

	}

	// new_game = bytes: { ‘G’, [2 byte little endian short short for port], ip
	// string }
	public void sendNewGame(int port, int ip) {
		// TODO format data and send
		byte[] conn_port = intToByteArray(port, 2);
		byte[] conn_ip = intToByteArray(ip, 0);
		byte[] conn = (byte[]) combineArray(conn_port, conn_port.length,
				conn_ip, conn_ip.length);
	}

	// bytes { ‘C‘ }
	public void sendConfirmation() {
		// TODO format data to fir send function and send
		byte[] data = { (byte) 'C' };
	}

	// bytes: { 'M', ['1' or '2' for board this affects], [1 byte x coord], [1
	// byte y coord] }
	public void sendMiss(int player, int x, int y) {
		// TODO format data to fit send function and send
		byte[] data = { (byte) 'M', (byte) player, (byte) x, (byte) y };
	}

	// bytes: { 'H', ['1' or '2' for board this affects], [1 byte x coord], [1
	// byte y coord] }
	public void sendHit(int player, int x, int y) {
		// TODO format data to fit send function and send
		byte[] data = { (byte) 'H', (byte) player, (byte) x, (byte) y };
	}

	// bytes: { 'O', ['1' or '2' for winner], ['1’ for forfeit/quit midgame or
	// ‘0’ for not forfeit] }
	public void sendGameOver(int winner, Boolean forfeit) {
		// TODO format data to fit send function and send
		byte[] data = { (byte) winner, (byte) (forfeit ? 1 : 0) };
	}

	// combines 2 arrays
	private Object combineArray(Object l1, int l1Size, Object l2, int l2Size) {
		Object[] conn = new Object[l1Size + l2Size];
		System.arraycopy(l1, 0, conn, 0, l1Size);
		System.arraycopy(l2, 0, conn, l1Size, l2Size);
		return conn;
	}

	// convert int to array
	// if size is 0, convert entire int 
	// otherwise size is number of bytes to convert to
	private byte[] intToByteArray(int convert, int size) {
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
