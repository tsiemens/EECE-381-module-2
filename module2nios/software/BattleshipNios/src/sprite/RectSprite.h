/*
 * RectSprite.h
 *
 *  Created on: 2013-10-05
 *  Author: Trevor Siemens
 */

#ifndef RECTSPRITE_H_
#define RECTSPRITE_H_

#include "BaseSprite.h"

typedef struct RectSprite
{
	// The super class. This MUST be the first element in the struct
	// in order to permit error free casting between super and subclasses
	BaseSprite baseSprite;
	int colour;
} RectSprite;

RectSprite* RectSprite_alloc();

RectSprite* RectSprite_init(RectSprite* this);

void RectSprite_free(RectSprite* this);

#endif /* RECTSPRITE_H_ */
