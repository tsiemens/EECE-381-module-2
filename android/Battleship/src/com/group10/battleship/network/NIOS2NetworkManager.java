// NIOS2 Communication Static Functions
package com.group10.battleship.network;

import com.group10.battleship.PrefsManager;

public class NIOS2NetworkManager {

	// TODO: change send to NIOS

	// new_game = bytes: { ‘G’, [2 byte little endian short short for port], ip
	// string }
	public static void sendNewGame() {
		if(!PrefsManager.getInstance().getBoolean(PrefsManager.KEY_USE_NIOS, false)) return;
		byte[] identifier = { (byte) 'N' };
		NetworkManager.getInstance().send(new String(identifier), false);
	}

	// bytes: { 'M', ['1' or '2' for board this affects], [1 byte x coord], [1
	// byte y coord] }
	public static void sendMiss(boolean isHost, int x, int y) {
		if(!PrefsManager.getInstance().getBoolean(PrefsManager.KEY_USE_NIOS, false)) return;
		int player = (byte) (isHost ? 1 : 2);
		byte[] data = { (byte) 'M', (byte) player, (byte) x, (byte) y };
		NetworkManager.getInstance().send(new String(data), false);
	}

	// bytes: { 'H', ['1' or '2' for board this affects], [1 byte x coord], [1
	// byte y coord] }
	public static void sendHit(boolean isHost, int x, int y) {
		if(!PrefsManager.getInstance().getBoolean(PrefsManager.KEY_USE_NIOS, false)) return;
		int player = (byte) (isHost ? 1 : 2);
		byte[] data = { (byte) 'H', (byte) player, (byte) x, (byte) y };
		NetworkManager.getInstance().send(new String(data), false);
	}

	// bytes: { 'O', ['1' or '2' for winner] }
	public static void sendGameOver(boolean isHost) {
		if(!PrefsManager.getInstance().getBoolean(PrefsManager.KEY_USE_NIOS, false)) return;
		int player = (byte) (isHost ? 1 : 2);
		byte[] data = { (byte) 'O', (byte) player };
		NetworkManager.getInstance().send(new String(data), false);
	}
	
	// bytes: { 'P', ['1' or '2' for player], "name" }
	public static void sendProfileName(boolean isHost, String name) {
		if(!PrefsManager.getInstance().getBoolean(PrefsManager.KEY_USE_NIOS, false)) return;
		int player = (byte) (isHost ? 1 : 2);
		byte[] data = new byte[name.length() + 2];
		data[0] = 'P';
		data[1] = (byte) player;
		for (int i = 0; i < name.length(); i++) {
			data[i+2] = (byte)name.charAt(i);
		}
		NetworkManager.getInstance().send(new String(data), false);
	}
}