/*
 * BaseSprite.c
 *
 *  Created on: 2013-10-05
 *  Author: Trevor Siemens
 */

#include "BaseSprite.h"
#include <stdlib.h>

BaseSprite* BaseSprite_alloc()
{
	BaseSprite *this = (BaseSprite *)malloc(sizeof(BaseSprite));
	return this;
}

// Constructor for BaseSprite
BaseSprite* BaseSprite_init(BaseSprite* this)
{
	this->classType = 0;
	this->spriteId = 0;
	this->height = 0;
	this->width = 0;
	this->xPos = 0.0;
	this->yPos = 0.0;
	this->xVel = 0.0;
	this->yVel = 0.0;

	this->animTimer = NULL;

	// draw is pure virtual in the base
	this->draw = 0;

	return this;
}

void BaseSprite_setSize(BaseSprite* this, int width, int height)
{
	this->width = width;
	this->height = height;
}

void BaseSprite_setPosition(BaseSprite* this, float xPos, float yPos)
{
	this->xPos = xPos;
	this->yPos = yPos;
}

/**
 * Updates the position of the sprite, based on the current velocity.
 *
 * @param this
 * @param timeElapsed : the time which has passed, which the velocity should be applied on
 * 		to calculate the new location
 */
void BaseSprite_updatePos(BaseSprite* this, double timeElapsed)
{
	this->xPos += timeElapsed * this->xVel;
	this->yPos += timeElapsed * this->yVel;
}

void BaseSprite_free(BaseSprite* this)
{
	if (this->animTimer != NULL)
	{
		free(this->animTimer);
	}
}
