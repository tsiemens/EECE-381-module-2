#ifndef VIDEOLIBRARY_H_
#define VIDEOLIBRARY_H_

#include "altera_up_avalon_video_pixel_buffer_dma.h"
#include "altera_up_avalon_video_character_buffer_with_dma.h"
#include "system.h"

#define CHAR_BUFFER_NAME "/dev/char_drawer"
#define SCREEN_WIDTH 320
#define SCREEN_HEIGHT 240
#define FOREGROUND 0
#define BACKGROUND 1

typedef struct VideoBuffer {
	alt_up_char_buffer_dev* pixel_buffer;
} VideoBuffer;

typedef struct CharBuffer {
	alt_up_char_buffer_dev* char_buffer;
} CharBuffer;

VideoBuffer* Video_New();

CharBuffer* Char_New();

void Video_drawPixel(VideoBuffer *video_buffer, unsigned int color, unsigned int x, unsigned int y);

alt_up_char_buffer_dev* Char_openDevice(const char* name);

alt_up_pixel_buffer_dma_dev* Video_openDevice(const char* name);

int Char_printString(CharBuffer *char_buffer, const char *ptr, unsigned int x, unsigned int y);

void Char_init(CharBuffer *char_buffer);

int Video_swapBuffers(VideoBuffer *video_buffer);

int Video_backBufferAddress(VideoBuffer *video_buffer, unsigned int new_address);

int Video_bufferIsSwapping(VideoBuffer *video_buffer);

void Video_clearScreen(VideoBuffer *video_buffer, int backbuffer);

void Video_drawLine(VideoBuffer* video_buffer, int x0, int y0, int x1, int y1, int color, int backbuffer);

void Char_clearScreen(CharBuffer* char_buffer);

#endif /* VIDEOLIBRARY_H_ */
