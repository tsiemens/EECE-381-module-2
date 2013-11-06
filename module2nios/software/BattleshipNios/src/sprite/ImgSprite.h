/*
 * ImgSprite.h
 *
 *  Created on: 2013-10-05
 *  Author: Pui Yan (Denise) Kwok
 */

#ifndef IMGSPRITE_H_
#define IMGSPRITE_H_

#include "BaseSprite.h"
#define MAX_WIDTH 90
#define MAX_HEIGHT 90

typedef struct ImgSprite
{
	// The super class. This MUST be the first element in the struct
	// in order to permit error free casting between super and subclasses
	BaseSprite baseSprite;

	int (*colours)[];
} ImgSprite;

ImgSprite* ImgSprite_alloc();

ImgSprite* ImgSprite_init(ImgSprite* this);

void ImgSprite_free(ImgSprite* this);

#endif /* IMGSPRITE_H_ */
