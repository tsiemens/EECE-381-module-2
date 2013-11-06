/*
 * Audio.c
 *
 */

#include "Audio.h"

/*
 * Initialise the audio and video device
 */
void av_config_setup() {
	alt_up_av_config_dev * av_config = alt_up_av_config_open_dev(
			"/dev/audio_and_video");
	if (av_config != NULL) {
		printf("Initializing audio video device\n");
		while (!alt_up_av_config_read_ready(av_config)) {
		}
		printf("Opened audio video device\n");
	} else {
		printf("Error: could not open audio video device\n");
	}
}

/*
 * Initialise the audio device
 *
 * @return Audio: the wrapper struct for altera audio device
 */
Audio* audio_init() {
	av_config_setup();
	Audio* this = (Audio *) malloc(sizeof(Audio));
	alt_up_audio_dev * device = alt_up_audio_open_dev("/dev/audio_0");
	if (device != NULL) {
		printf("Opened audio device\n");
		this->audio_dev = device;
	} else {
		printf("Error: could not open audio\n");
	}

	return this;
}

/*
 * Free the memory of an Audio pointer
 *
 * @param this: the Audio object to free the memory of
 */
void Audio_free(Audio* this) {
	free(this->audio_dev);
	free(this);
}

/*
 * Reset the Audio Core by clearing buffers for both channels
 *
 * @param this: the Audio object to control
 */
void audio_reset(Audio* this) {
	alt_up_audio_reset_audio_core(this->audio_dev);
}

/*
 * Write data to audio output
 *
 * @param this: the Audio device to control
 * @param buf: the data to be written
 * @param len: number of data to write to the output
 *
 * @return int: the total number of data written
 */
int audio_play(Audio* this, unsigned int *buf, int len) {
	alt_up_audio_write_fifo(this->audio_dev, buf, len, ALT_UP_AUDIO_LEFT);
	return alt_up_audio_write_fifo(this->audio_dev, buf, len,
			ALT_UP_AUDIO_RIGHT);
}

int audio_play_r(Audio* this, unsigned int *buf, int len) {
	return alt_up_audio_write_fifo(this->audio_dev, buf, len,
			ALT_UP_AUDIO_RIGHT);
}

int audio_play_l(Audio* this, unsigned int *buf, int len) {
	return alt_up_audio_write_fifo(this->audio_dev, buf, len, ALT_UP_AUDIO_LEFT);
}

int audio_check_buffer(Audio* this) {
	return alt_up_audio_write_fifo_space(this->audio_dev, ALT_UP_AUDIO_LEFT);
}
