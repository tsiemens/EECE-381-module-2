/*
 * AlphaSprite.h
 *
 *  Created on: 2013-10-08
 *  Author: Wesley Tsai
 */

#ifndef ALPHASPRITE_H_
#define ALPHASPRITE_H_

#include "BaseSprite.h"

typedef struct AlphaSprite
{
	// The super class. This MUST be the first element in the struct
	// in order to permit error free casting between super and subclasses
	BaseSprite baseSprite;
	char* string;
	void (*setString)(struct AlphaSprite*, char* string);
	int prev_x;
	int prev_y;
	char* prev_str;
} AlphaSprite;

AlphaSprite* AlphaSprite_alloc();

AlphaSprite* AlphaSprite_init(AlphaSprite* this);

void AlphaSprite_draw(BaseSprite* super);

void AlphaSprite_Clear(AlphaSprite* this);

void AlphaSprite_setString(AlphaSprite* super, char* string);

char* AlphaSprite_EmptyString(char* string);

#endif /* ALPHASPRITE_H_ */
