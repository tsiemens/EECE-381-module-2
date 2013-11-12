/*
 * Audio.h
 */

#ifndef AUDIO_H_
#define AUDIO_H_

#include "altera_up_avalon_audio_and_video_config.h"
#include "altera_up_avalon_audio.h"
#include <stdio.h>
#include <stdlib.h>
#include "../util/ArrayPtr.h"

// Wrapper for the altera audio
typedef struct Audio {
	// the audio device
	alt_up_audio_dev* audio_dev;
} Audio;

void av_config_setup();
Audio* audio_init();
void Audio_free(Audio * this);

void audio_reset(Audio * this);

int audio_play(Audio* this, unsigned int *buf, int len);
int audio_play_r(Audio* this, unsigned int *buf, int len);
int audio_play_l(Audio* this, unsigned int *buf, int len);

int audio_check_buffer(Audio* this);

#endif
