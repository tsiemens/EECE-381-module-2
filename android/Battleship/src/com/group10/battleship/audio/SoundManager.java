package com.group10.battleship.audio;

import com.group10.battleship.BattleshipApplication;
import com.group10.battleship.R;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.SparseIntArray;

public class SoundManager {

	private static int MAX_STREAMS = 10;

	private static SoundManager sSoundMangerInstance;

	private SoundPool mSoundPool;
	private SparseIntArray mSoundIDs;
	private SparseIntArray mStreamIDs;
	private float mVolume = 1f; 
	private boolean mIsMuted = false;

	public static SoundManager getInstance() {
		if (sSoundMangerInstance == null) {
			sSoundMangerInstance = new SoundManager();
		}
		return sSoundMangerInstance;
	}

	private SoundManager() {
		mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
		mSoundIDs = new SparseIntArray();
		mStreamIDs = new SparseIntArray();
		Context context = BattleshipApplication.getAppContext();

		// Load the sounds we will use
		// in game music
		int soundID = mSoundPool.load(context, R.raw.miss, 1);
		mSoundIDs.append(R.raw.miss, soundID);
		mStreamIDs.append(R.raw.miss, 0);
		// hit music
		soundID = mSoundPool.load(context, R.raw.hit, 1);
		mSoundIDs.append(R.raw.hit, soundID);
		mStreamIDs.append(R.raw.hit, 0);
		// exploding ship music
		soundID = mSoundPool.load(context, R.raw.ship_explode, 1);
		mSoundIDs.append(R.raw.ship_explode, soundID);
		mStreamIDs.append(R.raw.ship_explode, 0);
	}
	
	/**
	 * Plays the audio as an infinite loop. If already playing, stops it, and
	 * starts again.
	 * 
	 * @param resID
	 */
	public void playLoop(int resID, float vol) {
		int soundID = mSoundIDs.get(resID);
		if (soundID != 0) {
			if (mStreamIDs.get(resID) != 0) {
				// If loop is already playing, stop it
				mSoundPool.stop(mStreamIDs.get(resID));
			}
			int streamID = mSoundPool.play(soundID, vol, vol, 1, -1, mVolume);
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
	 * 
	 * @param resID
	 */
	public void playSFX(int resID) {
		Integer soundID = mSoundIDs.get(resID);
		if (soundID != null) {
			mSoundPool.play(soundID, mVolume, mVolume, 1, 0, 1f);
		}
	}
	
	public void mute()
	{
		mIsMuted = true;
		mVolume = 0f;
	}
	
	public void unmute()
	{
		mIsMuted = false;
		mVolume = 1f;
	}

	public boolean isMuted()
	{
		return mIsMuted;
	}
}
