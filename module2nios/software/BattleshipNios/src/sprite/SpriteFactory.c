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

AlphaSprite* SpriteFactory_generateP1WinStatusSprite()
{
	AlphaSprite* this = AlphaSprite_init(AlphaSprite_alloc());
	this->baseSprite.yPos = GAMEBOARD_TOP_CHAR_PADDING;
	this->baseSprite.xPos = 18;
	this->baseSprite.spriteId = ALPHA_P1_WIN_STATUS_SPRITE_ID;
	this->string = "      ";

	return this;
}

AlphaSprite* SpriteFactory_generateP2WinStatusSprite()
{
	AlphaSprite* this = AlphaSprite_init(AlphaSprite_alloc());
	this->baseSprite.yPos = GAMEBOARD_TOP_CHAR_PADDING + 20;
	this->baseSprite.xPos = 50;
	this->baseSprite.spriteId = ALPHA_P1_NAME_SPRITE_ID;
	this->string = "      ";

	return this;
}

AlphaSprite* SpriteFactory_generateP1NameSprite()
{
	AlphaSprite* this = AlphaSprite_init(AlphaSprite_alloc());
	this->baseSprite.yPos = GAMEBOARD_TOP_CHAR_PADDING + 40;
	this->baseSprite.xPos = 18;
	this->baseSprite.spriteId = ALPHA_P1_NAME_SPRITE_ID;
	this->string = "Player 1";

	return this;
}

AlphaSprite* SpriteFactory_generateP2NameSprite()
{
	AlphaSprite* this = AlphaSprite_init(AlphaSprite_alloc());
	this->baseSprite.yPos = GAMEBOARD_TOP_CHAR_PADDING + 40;
	this->baseSprite.xPos = 50;
	this->baseSprite.spriteId = ALPHA_P2_NAME_SPRITE_ID;
	this->string = "Player 2";

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
