package com.group10.battleship.audio;

import com.group10.battleship.BattleshipApplication;
import com.group10.battleship.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

public class SoundManager {

	private static int MAX_STREAMS = 10;
	
	private static SoundManager sSoundMangerInstance;
	
	private SoundPool mSoundPool;
	private SparseIntArray mSoundIDs;
	private SparseIntArray mStreamIDs;
	
	public static SoundManager getInstance() {
		if (sSoundMangerInstance == null) {
			sSoundMangerInstance = new SoundManager();
		}
		return sSoundMangerInstance;
	}
	
	private SoundManager() {
		Log.d("test", "test1");
		mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
		mSoundIDs = new SparseIntArray();
		mStreamIDs = new SparseIntArray();
		Context context = BattleshipApplication.getAppContext();
		
		// Load the sounds we will use
		int soundID = mSoundPool.load(context, R.raw.game_music, 1);
		mSoundIDs.append(R.raw.game_music, soundID);
		mStreamIDs.append(R.raw.game_music, 0);
		soundID = mSoundPool.load(context, R.raw.menu_music, 1);
		mSoundIDs.append(R.raw.menu_music, soundID);
		mStreamIDs.append(R.raw.menu_music, 0);
		Log.d("test", "test2");
	}
	
	/**
	 * Plays the audio as an infinite loop. If already playing,
	 * stops it, and starts again.
	 * @param resID
	 */
	public void playLoop(int resID, float vol) {
		int soundID = mSoundIDs.get(resID);
		if (soundID != 0) {
			if (mStreamIDs.get(resID) != 0) {
				// If loop is already playing, stop it
				mSoundPool.stop(mStreamIDs.get(resID));
			}
			int streamID = mSoundPool.play(soundID, vol, vol, 1, -1, 1f);
			mStreamIDs.put(resID, (streamID != 0) ? streamID : 0);
		}
	}
	
	public void stopLoop(int resID) {
		int soundID = mSoundIDs.get(resID);
		if (soundID != 0 && mStreamIDs.get(resID) != 0) {
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
