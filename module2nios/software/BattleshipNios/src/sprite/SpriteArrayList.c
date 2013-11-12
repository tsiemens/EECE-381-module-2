/*
 * SpriteArrayList.c
 *
 *  Created on: 2013-10-08
 *      Author: Jill Barnett
 */

#include "BaseSprite.h"
#include "SpriteArrayList.h"
#include <stdlib.h>

SpriteArrayList* SpriteArrayList_alloc()
{
	SpriteArrayList *this = (SpriteArrayList *)malloc(sizeof(SpriteArrayList));
	return this;
}

// Constructor for SpriteArrayList
SpriteArrayList* SpriteArrayList_init(SpriteArrayList* this, int size)
{
	this->size = size;
	this->last = -1;
	this->sprites = malloc(size*sizeof(BaseSprite*));
	return this;
}

BaseSprite* SpriteArrayList_getAt(SpriteArrayList* this, int index)
{
	if (index < this->size)
	{
		return this->sprites[index];
	}
	else
		return NULL;
}

BaseSprite* SpriteArrayList_getWithId(SpriteArrayList* this, int id)
{
	int i;
	for(i=0; i <= this->last; i++)
	{
		if((this->sprites[i])->spriteId == id)
			return this->sprites[i];
	}
	return NULL;
}

void SpriteArrayList_removeAtIndex(SpriteArrayList* this, int index)
{
	int i;
	if (index < this->size){
		for(i=index; i<this->last; i++){
			this->sprites[i] = this->sprites[i+1];
		}
		this->sprites[this->last] = NULL;
		this->last--;
	}
	if (this->last < (this->size)/2){
		SpriteArrayList_reallocate(this, (this->size)/2);
	}
}

void SpriteArrayList_removeObject(SpriteArrayList* this, BaseSprite* sprite)
// removes FIRST instance of sprite
{
	int i;
	for(i=0; i<=this->last; i++){
		if(this->sprites[i] == sprite){
			SpriteArrayList_removeAtIndex(this, i);
			break;
		}
	}
}

void SpriteArrayList_insert(SpriteArrayList* this, BaseSprite* sprite, int index)
{
	int i;

	if(this->last >= this->size-1){
		SpriteArrayList_reallocate(this, 2*this->size);
	}

	if(index > this->last+1 || index < 0){
		index = this->last+1;
	}

	for(i = this->last; i >= index; i--){
		this->sprites[i+1] = this->sprites[i];
	}
	this->sprites[index] = sprite;
	this->last++;
}

void SpriteArrayList_reallocate(SpriteArrayList* this, int size){
	int i;

	BaseSprite **temp = (BaseSprite**) malloc(size*sizeof(BaseSprite*));
	for (i=0; i <= this->last; i++){
		temp[i] = this->sprites[i];
	}
	free(this->sprites);
	this->sprites = temp;
	this->size = size;
}
