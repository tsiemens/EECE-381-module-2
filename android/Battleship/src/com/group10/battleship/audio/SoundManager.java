package com.group10.battleship.audio;

import com.group10.battleship.BattleshipApplication;
import com.group10.battleship.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseArray;

public class SoundManager {

	private static int MAX_STREAMS = 10;
	
	private static SoundManager sSoundMangerInstance;
	
	private SoundPool mSoundPool;
	private SparseArray<Integer> mSoundIDs;
	private SparseArray<Integer> mStreamIDs;
	
	public static SoundManager getInstance() {
		if (sSoundMangerInstance == null) {
			sSoundMangerInstance = new SoundManager();
		}
		return sSoundMangerInstance;
	}
	
	private SoundManager() {
		mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
		mSoundIDs = new SparseArray<Integer>();
		mStreamIDs = new SparseArray<Integer>();
		Context context = BattleshipApplication.getAppContext();
		
		// Load the sounds we will use
		int soundID = mSoundPool.load(context, R.raw.game_music, 1);
		mSoundIDs.append(R.raw.game_music, Integer.valueOf(soundID));
		mStreamIDs.append(R.raw.game_music, null);
		soundID = mSoundPool.load(context, R.raw.menu_music, 1);
		mSoundIDs.append(R.raw.menu_music, Integer.valueOf(soundID));
		mStreamIDs.append(R.raw.menu_music, null);
	}
	
	/**
	 * Plays the audio as an infinite loop. If already playing,
	 * stops it, and starts again.
	 * @param resID
	 */
	public void playLoop(int resID) {
		Integer soundID = mSoundIDs.get(resID);
		if (soundID != null) {
			if (mStreamIDs.get(resID) != null) {
				// If loop is already playing, stop it
				mSoundPool.stop(mStreamIDs.get(resID));
			}
			int streamID = mSoundPool.play(soundID, 1f, 1f, 1, -1, 1f);
			mStreamIDs.put(resID, (streamID != 0) ? Integer.valueOf(streamID) : null);
		}
	}
	
	public void stopLoop(int resID) {
		Integer soundID = mSoundIDs.get(resID);
		if (soundID != null && mStreamIDs.get(resID) != null) {
			mSoundPool.stop(mStreamIDs.get(resID));
		}
	}
	
	/**
	 * Plays the audio once, possibly overlapping other sound effects
	 * @param resID
	 */
	public void playSFX(int resID) {
		Integer soundID = mSoundIDs.get(resID);
		if (soundID != null) {
			mSoundPool.play(soundID, 1f, 1f, 1, 0, 1f);
		}
	}
}
