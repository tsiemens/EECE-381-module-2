/*
 * SpriteArrayList.h
 *
 *  Created on: 2013-10-08
 *      Author: Jill Barnett
 */

#ifndef SPRITEARRAYLIST_H_
#define SPRITEARRAYLIST_H_
#include "BaseSprite.h"

typedef struct SpriteArrayList
{
	BaseSprite **sprites;
	int size;
	int last;
} SpriteArrayList;

SpriteArrayList* SpriteArrayList_alloc();
SpriteArrayList* SpriteArrayList_init(SpriteArrayList* this, int size);

BaseSprite* SpriteArrayList_getAt(SpriteArrayList* this, int index);
BaseSprite* SpriteArrayList_getWithId(SpriteArrayList* this, int id);
void SpriteArrayList_removeAtIndex(SpriteArrayList* this, int index);
void SpriteArrayList_removeObject(SpriteArrayList* this, BaseSprite* sprite);
void SpriteArrayList_insert(SpriteArrayList* this, BaseSprite* sprite, int index);

void SpriteArrayList_reallocate(SpriteArrayList* this, int size);


#endif /* SPRITEARRAYLIST_H_ */
