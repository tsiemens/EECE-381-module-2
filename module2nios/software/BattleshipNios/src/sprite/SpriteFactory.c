#include "SpriteFactory.h"
#include <stdlib.h>


char intToChar(int val)
{
	switch(val) {
		case 0: return '0';
		case 1: return '1';
		case 2: return '2';
		case 3: return '3';
		case 4: return '4';
		case 5: return '5';
		case 6: return '6';
		case 7: return '7';
		case 8: return '8';
		case 9: return '9';
		default: return '\0';
	}
}

AlphaSprite* SpriteFactory_generateGameStatusSprite()
{
	AlphaSprite* this = AlphaSprite_init(AlphaSprite_alloc());
	this->baseSprite.yPos = GAMEBOARD_TOP_CHAR_PADDING - 2;
	this->baseSprite.xPos = 25;
	this->baseSprite.spriteId = ALPHA_STATUS_SPRITE_ID;
	this->string = "Status: Waiting for host";

	return this;
}

/**
 * Generates an array of SpriteArrayList for the board
 * The array should be size 10, and each element in the array is a new row.
 * Each element in the array lists is in a new column
 */
SpriteArrayList** SpriteFactory_generateNewBoard()
{
	// TODO
}
