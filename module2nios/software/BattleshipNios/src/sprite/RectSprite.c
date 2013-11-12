/*
 * RectSprite.c
 *
 *  Created on: 2013-10-05
 *  Author: Trevor Siemens
 */

#include "RectSprite.h"
#include <stdlib.h>
#include "../video/VideoHandler.h"


void RectSprite_draw(BaseSprite* super/*, VideoBuffer* */);

RectSprite* RectSprite_alloc()
{
	RectSprite *this = (RectSprite *)malloc(sizeof(RectSprite));
	return this;
}

RectSprite* RectSprite_init(RectSprite* this)
{
	BaseSprite_init(&this->baseSprite);
	// Set this instance of RectSprite
	this->baseSprite.classType = RECTSPRITE_CLASS_TYPE;
	this->baseSprite.draw = &RectSprite_draw;
	this->colour = 0;
	return this;
}

void RectSprite_free(RectSprite* this)
{
	BaseSprite_free((BaseSprite*)this);
	free(this);
}

/**
 * Draws the rectangle sprite on the video buffer
 */
void RectSprite_draw(BaseSprite* super/*, VideoBuffer* */)
{
	RectSprite* this = (RectSprite* )super;
	drawRect(this->baseSprite.xPos, this->baseSprite.yPos, this->baseSprite.xPos+this->baseSprite.width, this->baseSprite.yPos+this->baseSprite.height, this->colour);
}
