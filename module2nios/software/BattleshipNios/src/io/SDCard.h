/*
 * SDCard.h
 */

#ifndef SDCARD_H_
#define SDCARD_H_

#include <altera_up_sd_card_avalon_interface.h>
#include "../util/ArrayPtr.h"

// Wrapper for the altera sdcard
typedef struct SDCard {
	// the sdcard device
	alt_up_sd_card_dev* sdcard_dev;
} SDCard;

SDCard * sdcard_init();
void sdcard_free(SDCard * this);

UnsignedCharPtr* sdcard_read_file(char* filename);
ShortIntPtr* sdcard_read_audio(char* filename);

#define ALLOC_SIZE 128
#define AUDIO_ALLOC_SIZE 128

#endif
