/*
 * AudioHandler.h
 */

#ifndef AUDIO_HANDLER_H_
#define AUDIO_HANDLER_H_

#include <stdio.h>
#include "../io/SDCard.h"
#include "../io/Audio.h"
#include "../util/ArrayPtr.h"
#include "../util/Timer.h"

void AudioHandlerInit();

void AudioHandler_play();

void AudioHandler_playShoot();
void AudioHandler_playHit();
void AudioHandler_playMenu();

#endif
