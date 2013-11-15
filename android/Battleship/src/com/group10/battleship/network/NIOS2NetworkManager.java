// NIOS2 Communication Static Functions
package com.group10.battleship.network;

public class NIOS2NetworkManager {

	// TODO: change send to NIOS

	// new_game = bytes: { ‘G’, [2 byte little endian short short for port], ip
	// string }
	public static void sendNewGame() {
		byte[] identifier = { (byte) 'N' };
		NetworkManager.getInstance().send(new String(identifier), false);
	}

	// bytes { ‘C‘, [2 byte short for port], ip string } // sent after you_are_host
	public static void sendConfirmationHostStarted(String ip, int port) {
		byte[] data = { (byte) 'C' };
		byte[] socketByteArray = (byte[])combineArray(intToByteArray(port, 2), 2, ip.getBytes(), ip.getBytes().length);
		NetworkManager.getInstance().send(new String(data).concat(new String(socketByteArray)), false);
	}

	// bytes: { 'M', ['1' or '2' for board this affects], [1 byte x coord], [1
	// byte y coord] }
	public static void sendMiss(boolean isHost, int x, int y) {
		int player = (byte) (isHost ? 1 : 2);
		byte[] data = { (byte) 'M', (byte) player, (byte) x, (byte) y };
		NetworkManager.getInstance().send(new String(data), false);
	}

	// bytes: { 'H', ['1' or '2' for board this affects], [1 byte x coord], [1
	// byte y coord] }
	public static void sendHit(boolean isHost, int x, int y) {
		int player = (byte) (isHost ? 1 : 2);
		byte[] data = { (byte) 'H', (byte) player, (byte) x, (byte) y };
		NetworkManager.getInstance().send(new String(data), false);
	}

	// bytes: { 'O', ['1' or '2' for winner], ['1’ for forfeit/quit midgame or
	// ‘0’ for not forfeit] }
	public static void sendGameOver(boolean isHost, Boolean forfeit) {
		int player = (byte) (isHost ? 1 : 2);
		byte[] data = { (byte) 'O', (byte) player, (byte) (forfeit ? 1 : 0) };
		NetworkManager.getInstance().send(new String(data), false);
	}

	// combines 2 byte arrays
	private static byte[] combineArray(byte[] array1, int size1, byte[] array2,
			int size2) {
		byte[] result = new byte[size1 + size2];
		System.arraycopy(array1, 0, result, 0, size1);
		System.arraycopy(array2, 0, result, size1, size2);
		return result;
	}

	// convert int to array in little endian
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