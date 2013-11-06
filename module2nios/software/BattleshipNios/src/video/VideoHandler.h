#ifndef VIDEOHANDLER_H_
#define VIDEOHANDLER_H_

#include "altera_up_avalon_video_pixel_buffer_dma.h"
#include "altera_up_avalon_video_character_buffer_with_dma.h"
#include "../sprite/BaseSprite.h"
#include "../sprite/SpriteArrayList.h"
#include "VideoLibrary.h"

void VideoDemo();
void VideoHandlerInit();
void drawPixel(unsigned int color, unsigned int x, unsigned int y);
void drawLine(int x0, int y0, int x1, int y1, int color);
void printString(const char *ptr, unsigned int x,unsigned int y);
void VideoHandler_drawSprites(SpriteArrayList* spriteArray);
void clearChar();
void drawRect(int x0, int y0, int x1, int y1, int color);
void display();


#endif /* VIDEOHANDLER_H_ */
