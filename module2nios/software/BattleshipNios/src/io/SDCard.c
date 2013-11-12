/*
 * SDCard.c
 *
 */

#include <stdio.h>
#include <stdlib.h>
#include "SDCard.h"

/*
 * Initialises the SD Card reader
 *
 * @return SDCard struct
 */
SDCard * sdcard_init() {
	SDCard* this = (SDCard *) malloc(sizeof(SDCard));
	alt_up_sd_card_dev * device = alt_up_sd_card_open_dev("/dev/sd_card");
	if (device != NULL) {
		if (alt_up_sd_card_is_Present()) {
			printf("SD Card: connected.\n");
			if (alt_up_sd_card_is_FAT16()) {
				printf("SD Card: FAT16 file system detected.\n");
				this->sdcard_dev = device;
			} else {
				printf("Error: Unknown file system.\n");
			}
		}
	} else {
		printf("Error: cannot initiate sd card.\n");
	}
	return this;
}

/*
 * free the memory allocated by SDCard struct
 *
 * @param this: the SDCard struct
 */
void sdcard_free(SDCard* this) {
	free(this->sdcard_dev);
	free(this);
	return;
}

/*
 * Reads the file in the SDCard, parse the data into 2 bytes
 *
 * @param filename: the name of the file to read on the SD Card
 *
 * @return UnsignedCharPtr: contains the data and the number of data
 * 							read from the file
 */
UnsignedCharPtr* sdcard_read_file(char* filename) {
	short int file_id;
	int count = 0;
	unsigned char* data_temp;

	UnsignedCharPtr* output;
	output = UnsignedCharPtr_alloc(ALLOC_SIZE);

	file_id = alt_up_sd_card_fopen(filename, 0);
	short temp = alt_up_sd_card_read(file_id);

	while (temp != -1) {
		output->data[count] = temp;
		count++;
		if ((count % ALLOC_SIZE) == 0) {
			data_temp = (unsigned char*) realloc(output->data, (count
					+ ALLOC_SIZE) * sizeof(unsigned char));
			if (data_temp == NULL) {
				printf("Error: SD Card Memory Realloc\n");
				return output;
			}
			output->data = data_temp;
		}
		temp = alt_up_sd_card_read(file_id);
	}
	alt_up_sd_card_fclose(file_id);
	output->size = count;
	return output;
}

/*
 * Reads the file in the SDCard, assume first 4 bytes gives width and height of data matrix
 *
 * @param filename: the name of the file to read on the SD Card
 *
 * @return UnsignedCharPtr: contains the data and the number of data
 * 							read from the file
 */
UnsignedCharPtr* sdcard_read_file_w_size(char* filename) {
	short int file_id;
	int count = 0;
	unsigned char* data_temp;
	unsigned char width_bytes[4] = { 0, 0, 0, 0 };
	unsigned char height_bytes[4] = { 0, 0, 0, 0 };

	UnsignedCharPtr* output;
	output = UnsignedCharPtr_alloc(ALLOC_SIZE);

	file_id = alt_up_sd_card_fopen(filename, 0);
	short int temp = alt_up_sd_card_read(file_id);

	while (count < 8) {
		output->data[count] = temp;
		if (count < 4) {
			width_bytes[count] = (unsigned char) temp;
		} else {
			height_bytes[count - 4] = (unsigned char) temp;
		}
		temp = alt_up_sd_card_read(file_id);
		count++;
	}

	int width = 0;
	int height = 0;
	int p = 0;
	for (p = 0; p < 4; p++) {
		int r = 0;
		int offset = 1;
		for (r = 0; r < (3 - p); r++) {
			offset = offset * 16;
		}
		width += ((int) width_bytes[p]) * offset;
		height += ((int) height_bytes[p]) * offset;
	}

	int count_total = 2 * (width * height) + 8;

	while (count < count_total) {
		output->data[count] = temp;
		count++;
		if ((count % ALLOC_SIZE) == 0) {
			data_temp = (unsigned char*) realloc(output->data, (count
					+ ALLOC_SIZE) * sizeof(unsigned char));
			if (data_temp == NULL) {
				printf("Error: SD Card Memory Realloc\n");
				return output;
			}
			output->data = data_temp;
		}
		temp = alt_up_sd_card_read(file_id);
	}
	alt_up_sd_card_fclose(file_id);
	output->size = count;
	return output;
}

/* Reads the file from SD Card, parse the data into 4 bytes
 * wave files are little endian
 *
 * @param filename: the name of the file to read from
 *
 * @return ShortIntPtr: the data in 4 bytes and the number of data
 * 						read from file.
 */

ShortIntPtr* sdcard_read_audio(char* filename) {
	short int file_id;
	int count = 0;
	short int temp;
	short int* data_temp;

	ShortIntPtr* output = ShortIntPtr_alloc(AUDIO_ALLOC_SIZE);
	output->size = 0;

	file_id = alt_up_sd_card_fopen(filename, 0);

	char byte_before = alt_up_sd_card_read(file_id);
	char byte_after = alt_up_sd_card_read(file_id);

	temp = ((unsigned char) byte_after << 8) | (unsigned char) byte_before;

	while (temp != -1) {
		output->data[count] = temp;
		count++;
		if ((count % AUDIO_ALLOC_SIZE) == 0) {
			data_temp = (short int*) realloc(output->data, (count
					+ AUDIO_ALLOC_SIZE) * sizeof(short int));
			if (data_temp == NULL) {
				printf("Error: SD Card Memory Re-alloc\n");
				return output;
			}
			output->data = data_temp;
		}
		byte_before = alt_up_sd_card_read(file_id);
		byte_after = alt_up_sd_card_read(file_id);

		temp = ((unsigned char) byte_after << 8) | (unsigned char) byte_before;
	}
	alt_up_sd_card_fclose(file_id);
	output->size = count;
	return output;
}
