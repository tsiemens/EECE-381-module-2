/*
 * VideoLibrary.c
 *
 * 	Created on: 2013-10-4
 * 	Author: Wesley Tsai
 *
 * 	This file abstracts dealings with HAL
 * 	Contains the lower level functions to be used by VideoHandler.c to draw to the screen
 */

#include "altera_up_avalon_video_pixel_buffer_dma.h"
#include "altera_up_avalon_video_character_buffer_with_dma.h"
#include "VideoLibrary.h"
#include <stdlib.h>
#include "io.h"

int draw_pixel_fast(alt_up_pixel_buffer_dma_dev *pixel_buffer,
		unsigned int color, unsigned int x, unsigned int y, int backbuffer);

/*
 * Instantiates a new pointer to the VideoBuffer.
 * Clears the screen and sets the foreground address
 *
 * NOTE: Background and foreground addresses are stored in the VideoBuffer struct.
 * 		 So, this is the only function where we need to specify addresses
 */
VideoBuffer* Video_New()
{
	VideoBuffer* this = NULL;
	this = (VideoBuffer *)malloc(sizeof(VideoBuffer));

	// Use the name of your pixel buffer DMA core
	this->pixel_buffer = Video_openDevice(PIXEL_BUFFER_DMA_NAME);

	// Set the background buffer address – Although we don’t use the background,
	// they only provide a function to change the background buffer address, so
	// we must set that, and then swap it to the foreground.
	Video_backBufferAddress(this, PIXEL_BASE);

	// Swap background and foreground buffers
	Video_swapBuffers(this);

	// Wait for the swap to complete
	while (Video_bufferIsSwapping(this));

	// Now set the actual background buffer address (Foreground buffer is being displayed)
	Video_backBufferAddress(this, PIXEL_BASE + (SCREEN_WIDTH*SCREEN_HEIGHT*2));

	// Clear the foreground and background buffers
	Video_clearScreen(this, 0);
	Video_clearScreen(this, 1);

	return this;
}

/*
 * Instantiates a new pointer to the CharBuffer.
 */
CharBuffer* Char_New()
{
	CharBuffer* this = NULL;
	this = (CharBuffer *)malloc(sizeof(CharBuffer));

	// Initialize Char Buffer
	this->char_buffer = Char_openDevice(CHAR_BUFFER_NAME); //**Couldn't find the constant in system.h
	Char_init(this->char_buffer);

	return this;
}

/*
 * Draws pixel to Video Buffer. Abstracts messy HAL naming.
 */
void Video_drawPixel(VideoBuffer *video_buffer, unsigned int color,  unsigned int x, unsigned int y)
{
	draw_pixel_fast(video_buffer -> pixel_buffer, color, x, y, BACKGROUND);
}

/*
 * Returns the VideoBuffer settings struct using the Device Name from QSYS
 */
alt_up_pixel_buffer_dma_dev* Video_openDevice(const char* name)
{
	return alt_up_pixel_buffer_dma_open_dev(name);
}

/*
 * Returns the CharBuffer settings struct using the Device Name from QSYS
 */
alt_up_char_buffer_dev* Char_openDevice(const char* name)
{
	return alt_up_char_buffer_open_dev(name);
}

/*
 * @brief Initialize the name of the alt_up_char_buffer_dev structure
 *
 * @param char_buffer -- struct for the character buffer device
 *
 */
void Char_init(CharBuffer *char_buffer)
{
	alt_up_char_buffer_init(char_buffer -> char_buffer);
}

/**
 * @brief Draw a NULL-terminated text string at the location specified by <em>(x, y)</em>
 *
 * @param ch -- the character to draw
 * @param x	-- the \em x coordinate
 * @param y	-- the \em y coordinate
 *
 * @return 0 for success, -1 for error (such as out of bounds)
 **/
int Char_printString(CharBuffer *char_buffer, const char *ptr, unsigned int x, unsigned int y)
{
	return alt_up_char_buffer_string(char_buffer->char_buffer, ptr, x, y);
}

/**
 * @brief Swaps which buffer is being sent to the VGA Controller
 *
 * @param pixel_buffer -- the pointer to the VGA structure
 *
 * @return 0 for success
 **/
int Video_swapBuffers(VideoBuffer *video_buffer)
{
	return alt_up_pixel_buffer_dma_swap_buffers(video_buffer -> pixel_buffer);
}

/**
 * @brief Changes the back buffer's start address
 *
 * @param pixel_buffer -- the pointer to the VGA structure
 * @param new_address  -- the new start address of the back buffer
 *
 * @return 0 for success
 **/
int Video_backBufferAddress(VideoBuffer *video_buffer, unsigned int new_address)
{
	return alt_up_pixel_buffer_dma_change_back_buffer_address(video_buffer -> pixel_buffer, new_address);
}

/**
 * @brief Check if swapping buffers has completed
 *
 * @param pixel_buffer -- the pointer to the VGA structure
 *
 * @return 0 if complete, 1 if still processing
 **/
int Video_bufferIsSwapping(VideoBuffer *video_buffer)
{
	return alt_up_pixel_buffer_dma_check_swap_buffers_status(video_buffer -> pixel_buffer);
}

/**
 * @brief This function clears the screen or the back buffer.
 *
 * @param pixel_buffer -- the pointer to the VGA structure
 * @param backbuffer -- set to 1 to clear the back buffer, otherwise set to 0 to clear the current screen.
 *
 * @return 0 if complete, 1 if still processing
 **/
void Video_clearScreen(VideoBuffer *video_buffer, int backbuffer)
{
	alt_up_pixel_buffer_dma_clear_screen(video_buffer -> pixel_buffer, backbuffer);
}

/**
 * @brief This function draws a line of a given color between points (x0,y0) and (x1,y1).
 *
 * @param pixel_buffer -- the pointer to the VGA structure
 * @param x0,x1,y0,y1 -- coordinates (x0,y0) and (x1,y1) correspond to end points of the line
 * @param color -- color of the line to be drawn
 * @param backbuffer -- set to 1 to select the back buffer, otherwise set to 0 to select the current screen.
 *
 * @return 0 if complete, 1 if still processing
 **/
void Video_drawLine(VideoBuffer* video_buffer, int x0, int y0, int x1, int y1, int color, int backbuffer)
{
	alt_up_pixel_buffer_dma_draw_line(video_buffer -> pixel_buffer, x0, y0, x1, y1, color, backbuffer);
}

void Video_drawRect(VideoBuffer *video_buffer, int x0, int y0, int x1, int y1, int color, int backbuffer)
{
	alt_up_pixel_buffer_dma_draw_rectangle(video_buffer->pixel_buffer, x0, y0, x1, y1, color, backbuffer);
}

void Char_clearScreen(CharBuffer* char_buffer)
{
	alt_up_char_buffer_clear(char_buffer->char_buffer);
}

/* This funcion draws a pixel to the background buffer, and assumes:
 * 1. Your pixel buffer DMA is set to CONSECUTIVE
 * 2. The resolution is 320x240
 * 3. x and y are within the screen (0,0)->(319, 239)
 * 4. You are using 16-bit color
 *
 * DO NOT USE THIS FUNCTION IF ANY OF THE ABOVE ARE NOT GUARANATEED, OR YOU
 * MAY WRITE TO INVALID MEMORY LOCATIONS, CRASHING YOUR PROGRAM, OR
 * CAUSING UNEXPECTED BEHAVIOR.
 */
int draw_pixel_fast(alt_up_pixel_buffer_dma_dev *pixel_buffer,
		unsigned int color, unsigned int x, unsigned int y, int backbuffer) {
	unsigned int bufferAddr;
	if (backbuffer == 1)
		bufferAddr = pixel_buffer->back_buffer_start_address;
	else
		bufferAddr = pixel_buffer->buffer_start_address;

	unsigned int addr;

	addr = ((x & pixel_buffer->x_coord_mask) << 1);
	addr += (((y & pixel_buffer->y_coord_mask) * 320) << 1);

	IOWR_16DIRECT(bufferAddr, addr, color);

	return 0;
}
