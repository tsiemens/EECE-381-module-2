package com.group10.battleship.audio;

import java.io.IOException;

import android.content.Context;
import android.media.MediaPlayer;

import com.group10.battleship.BattleshipApplication;
import com.group10.battleship.R;

public class MusicManager {

	private static MusicManager sMusicMangerInstance;

	private MediaPlayer mMenuMusic;
	private MediaPlayer mGameMusic;
	private MediaPlayer mThinkingMusic;
	private boolean mIsMuted = false;
	

	public enum Music {
		MENU, GAME, THINKING
	}

	private Music mNowPlaying;

	public static MusicManager getInstance() {
		if (sMusicMangerInstance == null) {
			sMusicMangerInstance = new MusicManager();
		}
		return sMusicMangerInstance;
	}

	private MusicManager() {
		Context context = BattleshipApplication.getAppContext();

		mMenuMusic = MediaPlayer.create(context, R.raw.menu_music);
		mMenuMusic.setLooping(true);
		mMenuMusic.setVolume(0.3f, 0.3f);
		mGameMusic = MediaPlayer.create(context, R.raw.game_music);
		mGameMusic.setLooping(true);
		mGameMusic.setVolume(0.1f, 0.1f);
		mThinkingMusic = MediaPlayer.create(context, R.raw.thinking);
		mThinkingMusic.setLooping(true);
		mThinkingMusic.setVolume(0.1f, 0.1f);
	}

	public void play(Music m) {
		if (m == Music.MENU) {
			mNowPlaying = Music.MENU;
			mMenuMusic.start();
		} else if (m == Music.GAME) {
			mNowPlaying = Music.GAME;
			mGameMusic.start();
		} else {
			mNowPlaying = Music.THINKING;
			mThinkingMusic.start();
		}
	}

	public void pause() {
		if(mNowPlaying == null) {
			return;
		} else if (mNowPlaying == Music.MENU) {
			mMenuMusic.pause();
		} else if (mNowPlaying == Music.GAME) {
			mGameMusic.pause();
		} else {
			mThinkingMusic.pause();
		}
	}

	public void stop(Music m) {
		MediaPlayer stopMusic = null;
		
		if (m == Music.MENU && mMenuMusic.isPlaying()) {
			stopMusic = mMenuMusic;
		} else if (m == Music.GAME && mGameMusic.isPlaying()) {
			stopMusic = mGameMusic;
		} else if (m == Music.THINKING && mThinkingMusic.isPlaying()) {
			stopMusic = mThinkingMusic;
		}
		if (stopMusic != null) {
			mNowPlaying = null;
			stopMusic.stop();
			try {
				stopMusic.prepare();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public boolean isMuted()
	{
		return mIsMuted;
	}
	
	public void resume() {
		if(mNowPlaying == null) {
			return;
		} else if (mNowPlaying == Music.MENU) {
			mMenuMusic.start();
		} else if (mNowPlaying == Music.GAME) {
			mGameMusic.start();
		} else {
			mThinkingMusic.start();
		}
	}
	
	public void mute() {
		mIsMuted = true;
		mMenuMusic.setVolume(0, 0);
		mGameMusic.setVolume(0, 0);
		mThinkingMusic.setVolume(0, 0);
	}
	
	public void unmute()
	{
		mIsMuted = false;
		mMenuMusic.setVolume(0.3f, 0.3f);
		mGameMusic.setVolume(0.1f, 0.1f);
		mThinkingMusic.setVolume(0.1f, 0.1f);
		
	}
}
