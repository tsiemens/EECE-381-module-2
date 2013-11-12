/*
 * AlphaSprite.h
 *
 *  Created on: 2013-10-08
 *  Author: Wesley Tsai
 *
 *  Reminder that the character buffer is an 80x60 Array
 */

#include "AlphaSprite.h"
#include "../video/VideoHandler.h"
#include <stdlib.h>

void AlphaSprite_Clear(AlphaSprite* this);

AlphaSprite* AlphaSprite_alloc()
{
	AlphaSprite *this = (AlphaSprite *)malloc(sizeof(AlphaSprite));
	return this;
}

AlphaSprite* AlphaSprite_init(AlphaSprite* this)
{
	BaseSprite_init(&this->baseSprite);

	char* defaultString = "Please Set A String";
	this->prev_x = 0;
	this->prev_y = 0;
	this->baseSprite.classType = ALPHASPRITE_CLASS_TYPE;
	this->baseSprite.draw = &AlphaSprite_draw;
	this->setString = &AlphaSprite_setString;
	this->string = defaultString;
	this->prev_str = NULL;

	return this;
}

void AlphaSprite_setString(AlphaSprite* this, char* string)
{
	this->string = string;
}

void AlphaSprite_draw(BaseSprite* super)
{
	AlphaSprite* this = (AlphaSprite*) super;

	//Erase previous string
		AlphaSprite_Clear(this);
		//Assign previous values
		this->prev_x = this->baseSprite.xPos;
		this->prev_y = this->baseSprite.yPos;
		this->prev_str = AlphaSprite_EmptyString(this->string);

	//Print current string
	printString(this->string, (unsigned int)this->baseSprite.xPos, (unsigned int)this->baseSprite.yPos);
}

void AlphaSprite_Clear(AlphaSprite* this)
{
	printString(this->prev_str, (unsigned int)this->prev_x, (unsigned int)this->prev_y);
}

char* AlphaSprite_EmptyString(char* string)
{
	int i,j;
	for(i = 0; string[i] != '\0'; i++);

	char* prev_str = (char*)malloc(sizeof(char)*(i+1));

	prev_str[i] = '\0';

	for(j = 0; j < i; j++)
		prev_str[j] = ' ';

	return prev_str;
}
