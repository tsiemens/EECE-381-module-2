/*
 * ImgSprite.h
 *
 *  Created on: 2013-10-05
 *  Author: Pui Yan (Denise) Kwok
 */

#include "ImgSprite.h"
#include "../video/VideoHandler.h"
#include <stdlib.h>
#include <stdio.h>


void ImgSprite_draw(BaseSprite* super/*, VideoBuffer* */);

ImgSprite* ImgSprite_alloc()
{
	ImgSprite *this = (ImgSprite *)malloc(sizeof(ImgSprite));
	return this;
}

ImgSprite* ImgSprite_init(ImgSprite* this)
{
	BaseSprite_init(&this->baseSprite);
	// Set this instance of ImgSprite
	this->baseSprite.classType = IMGSPRITE_CLASS_TYPE;
	this->baseSprite.draw = &ImgSprite_draw;
	return this;
}

/**
 * Draws the rectangle sprite on the video buffer
 */
void ImgSprite_draw(BaseSprite* super/*, VideoBuffer* */)
{
	ImgSprite* this = (ImgSprite* )super;
	int i, j;
		for (i = 0; i < this->baseSprite.height; i++) {
			for (j = 0; j < this->baseSprite.width; j++) {
				if((*this->colours)[(i * this->baseSprite.width) + j] != 0)
					drawPixel((*this->colours)[(i * this->baseSprite.width) + j], (unsigned int)this->baseSprite.xPos + j, (unsigned int)this->baseSprite.yPos +i);
			}
		}
}

void ImgSprite_free(ImgSprite* this)
{
	BaseSprite_free((BaseSprite*)this);
	free(this->colours);
	free(this);
}
