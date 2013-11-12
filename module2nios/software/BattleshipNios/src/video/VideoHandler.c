/*
 * VideoHandler.c
 *
 * 	Created on: 2013-10-4
 * 	Author: Wesley Tsai
 *
 * 	This file abstracts dealing with writing to the foreground/background video buffers.
 * 	It allows drawing and printing to the screen without any knowledge of buffers and addresses.
 *
 * 	How to: First call VideoHandlerInit(), then proceed to use the provided functions to draw to the background buffer.
 * 			Finally, call display() to swap background buffer to the foreground.
 *	Note: Clearing the character buffer requires manually calling clearChar().
 */

#include <stdlib.h>
#include <stdio.h>
#include "VideoHandler.h"
#include "../audio/AudioHandler.h"

static VideoBuffer* Video;
static CharBuffer* Character;

void lzrPrty(unsigned char randomVal)
{
	drawLine(randomVal%320, 0, 320, randomVal%240, 0xF22F-randomVal*10);
	drawLine(randomVal*5%320, 240, 160, -randomVal%240, 0x15F-randomVal*20);
	drawLine(-randomVal*2%320, 0, randomVal*5%320, randomVal%240, 0x78F-randomVal*10);
	drawLine(320, 120, randomVal*2%320, randomVal*2%240, 0xA22F+randomVal*50);
	Video_swapBuffers(Video);
	while(Video_bufferIsSwapping(Video));
}

/*
 * Intializes the static Video and Character buffers contained in this file
 */
void VideoHandlerInit()
{
	Video = Video_New();
	Character = Char_New();
}

/*
 * Draws a pixel with a specific color to the background
 *
 * @param unsigned int color: The color in 16-bit RGB 565
 * @param unsigned int x: The x coordinate to draw at
 * @param unsigned int y: the y coordinate to draw at
 */
void drawPixel(unsigned int color, unsigned int x, unsigned int y)
{
	Video_drawPixel(Video, color, x, y);
}

/*
 * Draws a line with a specific color to the background
 *
 * @param int x0: starting x coordinate (0..320)
 * @param int y0: starting y coordinate (0..240)
 * @param int x1: ending x coordinate (0..320)
 * @param int y1: ending y coordinate (0..240)
 * @param int color: The color in 16-bit RGB
 */
void drawLine(int x0, int y0, int x1, int y1, int color)
{
	Video_drawLine(Video, x0, y0, x1, y1, color, BACKGROUND);
}

/*
 * Prints a string to the foreground
 * WARNING: The character buffer is separate from the video buffer.
 * 			Meaning, clearing and switching the video buffer will not affect the character buffer.
 *
 * @param const char *ptr: The string to print
 * @param unsigned int x: The x coordinate to print at
 * @param unsigned int y: the y coordinate to print at
 *
 */
void printString(const char *ptr, unsigned int x,unsigned int y)
{
	Char_printString(Character, ptr, x, y);
}

void drawRect(int x0, int y0, int x1, int y1, int color)
{
	Video_drawRect(Video, x0, y0, x1, y1, color, BACKGROUND);
}

/*
 * Clears the character buffer.
 */
void clearChar()
{
	Char_clearScreen(Character);
}

/*
 * Called at the end of the loop, after all the drawing to the background has finished.
 * Swaps buffer from background to foreground, and clears background in preparation for further drawing.
 */
void display()
{
	Video_swapBuffers(Video);
	while(Video_bufferIsSwapping(Video)) {
		// output audio
		// game loop loops too slow for audio to sound smoothly
		AudioHandler_play();
	}
	Video_clearScreen(Video, BACKGROUND);
}

void VideoHandler_drawSprites(SpriteArrayList* spriteArray)
{
//	printf("drawing sprite of size: %i x %i", ((*sprites[0]).width), ((*sprites[0]).height));
	int i;
	BaseSprite* sprite;
	for(i = 0; i <= spriteArray->last; i++)
	{
		sprite = SpriteArrayList_getAt(spriteArray, i);
		sprite->draw(sprite);
	}
}
