#include "AudioHandler.h"

static const int BUFFER_SIZE = 128;
static const int SILENCE = 0;
static const int SHOOT = 1;
static const int HIT = 2;
static const int MENU = 3;
static const double delay = 1.35;

static Audio* audioDev;
static ShortIntPtr* shoot_audio;
static ShortIntPtr* hit_audio;
static ShortIntPtr* menu_audio;
static int begin;
static int end;
static int playing;

static Timer* audioTimer;

/*
 * Initializes audio device and audiohandler variables
 */
void AudioHandlerInit() {
	// initialize audio devices
	audioDev = audio_init();
	// get shoot sound effect
	shoot_audio = sdcard_read_audio("shoot.wav");
	printf("SD Card: Read shoot.wav %d bytes\n", shoot_audio->size);
	// get hit sound effect
	hit_audio = sdcard_read_audio("hit.wav");
	printf("SD Card: Read hit.wav %d bytes\n", hit_audio->size);
	// get menu sound
	menu_audio = sdcard_read_audio("menu.wav");
	printf("SD Card: Read menu.wav %d bytes\n", menu_audio->size);
	// initialize to beginning of audio array
	begin = 0;
	end = 0;
	// no audio
	playing = SILENCE;
	// initialize timer
	// used to limit the number of times
	// to check the buffer of the audio device
	audioTimer = Timer_init(Timer_alloc(), delay);
}

/* Retreives the next segment of the wanted audio and play it
*
*/
void AudioHandler_play() {
	// check if there is audio to play and timer
	if (playing != SILENCE && Timer_isDone(audioTimer) == 1) {
		//if (audio_check_buffer(audioDev) < BUFFER_SIZE) {
			// audio buffer is not empty, don't play
		//	return;
		//}
		ShortIntPtr* audio_all;

		// get audio to output
		if (playing == MENU) {
			audio_all = menu_audio;
		} else if (playing == HIT) {
			audio_all = hit_audio;
		} else {
			audio_all = shoot_audio;
		}

		unsigned int audio_output[BUFFER_SIZE];

		// get the index of the segment of the audio to output
		begin = end;
		if (audio_all->size < BUFFER_SIZE) {
			end = audio_all->size;
		} else if (((audio_all->size) - end) < BUFFER_SIZE) {
			end = audio_all->size;
			playing = SILENCE;
		} else {
			end += BUFFER_SIZE;
		}

		// copy segment to play
		ShortToIntCPY(&(audio_all->data[begin]), audio_output, end - begin);
		// play segment
		audio_play(audioDev, audio_output, end - begin);
		// restart timer
		Timer_start(audioTimer);
	}
}

/*
 * Set audio to play effect, set the segment index to beginning of file
 */

void AudioHandler_playShoot() {
	playing = SHOOT;
	begin = 0;
	end = 0;
}

void AudioHandler_playHit() {
	playing = HIT;
	begin = 0;
	end = 0;
}

void AudioHandler_playMenu() {
	playing = MENU;
	begin = 0;
	end = 0;
}
