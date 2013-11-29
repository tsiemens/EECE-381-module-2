/*
 * BSNStateMachine.c
 *
 *  Created on: 2013-10-08
 *      Author: Jill
 */

#include "BSNStateMachine.h"

#include <stdlib.h>
#include <stdio.h>
#include "GameBoard.h"
#include "../sprite/SpriteArrayList.h"
#include "../sprite/AlphaSprite.h"
#include "../sprite/SpriteFactory.h"
#include "../network/ProtocolHandler.h"

void BSNStateMachine_WaitingPerformLogic(BSNStateMachine* this);
void BSNStateMachine_PlayingPerformLogic(BSNStateMachine* this);
void BSNStateMachine_GameOverPerformLogic(BSNStateMachine* this);

BSNStateMachine* BSNStateMachine_alloc() {
	BSNStateMachine *this = (BSNStateMachine *) malloc(sizeof(BSNStateMachine));
	return this;
}

// Constructor for BSNStateMachine
BSNStateMachine* BSNStateMachine_init(BSNStateMachine* this) {
	this->state = WAITING_FOR_PLAYERS;

	this->gameBoard = GameBoard_init(GameBoard_alloc());
	this->boardSprites = SpriteArrayList_init(SpriteArrayList_alloc(), 5);

	AlphaSprite* statusSprite = SpriteFactory_generateGameStatusSprite();
	AlphaSprite* p1winSprite = SpriteFactory_generateP1WinStatusSprite();
	AlphaSprite* p2winSprite = SpriteFactory_generateP2WinStatusSprite();
	AlphaSprite* p1nameSprite = SpriteFactory_generateP1NameSprite();
	AlphaSprite* p2nameSprite = SpriteFactory_generateP2NameSprite();
	SpriteArrayList_insert(this->boardSprites, (BaseSprite*)statusSprite, 0);
	SpriteArrayList_insert(this->boardSprites, (BaseSprite*)p1winSprite, 1);
	SpriteArrayList_insert(this->boardSprites, (BaseSprite*)p2winSprite, 2);
	SpriteArrayList_insert(this->boardSprites, (BaseSprite*)p1nameSprite, 3);
	SpriteArrayList_insert(this->boardSprites, (BaseSprite*)p2nameSprite, 4);

	return this;
}

void BSNStateMachine_performFrameLogic(BSNStateMachine* this)
{
	BSNStateMachine_PerformLogic(this);
	VideoHandler_drawSprites(this->boardSprites);

	ProtocolHandler_receive(this);
}

void BSNStateMachine_PerformLogic(BSNStateMachine* this) {
	switch (this->state) {
	case WAITING_FOR_PLAYERS:
		printf("performing wait logic");
		BSNStateMachine_WaitingPerformLogic(this);
		break;
	case PLAYING:
		printf("performing play logic");
		BSNStateMachine_PlayingPerformLogic(this);
		break;
	case GAME_OVER:
		printf("performing game over logic");
		BSNStateMachine_GameOverPerformLogic(this);
		break;
	}
}

void BSNStateMachine_WaitingPerformLogic(BSNStateMachine* this) {
	((AlphaSprite*)SpriteArrayList_getWithId(this->boardSprites, ALPHA_STATUS_SPRITE_ID))->string = "Status: Waiting for host";
	((AlphaSprite*)SpriteArrayList_getWithId(this->boardSprites, ALPHA_P1_WIN_STATUS_SPRITE_ID))->string = "      ";
	((AlphaSprite*)SpriteArrayList_getWithId(this->boardSprites, ALPHA_P2_WIN_STATUS_SPRITE_ID))->string = "      ";
}

void BSNStateMachine_PlayingPerformLogic(BSNStateMachine* this) {
	((AlphaSprite*)SpriteArrayList_getWithId(this->boardSprites, ALPHA_STATUS_SPRITE_ID))->string = "Status: Game In Progress";
	((AlphaSprite*)SpriteArrayList_getWithId(this->boardSprites, ALPHA_P1_WIN_STATUS_SPRITE_ID))->string = "      ";
	((AlphaSprite*)SpriteArrayList_getWithId(this->boardSprites, ALPHA_P2_WIN_STATUS_SPRITE_ID))->string = "      ";
}

void BSNStateMachine_GameOverPerformLogic(BSNStateMachine* this) {
	((AlphaSprite*)SpriteArrayList_getWithId(this->boardSprites, ALPHA_STATUS_SPRITE_ID))->string = "Status: Game Over       ";
	if (this->winner == 1) {
		((AlphaSprite*)SpriteArrayList_getWithId(this->boardSprites, ALPHA_P1_WIN_STATUS_SPRITE_ID))->string = "WINS! ";
		((AlphaSprite*)SpriteArrayList_getWithId(this->boardSprites, ALPHA_P2_WIN_STATUS_SPRITE_ID))->string = "LOSES!";
	} else {
		((AlphaSprite*)SpriteArrayList_getWithId(this->boardSprites, ALPHA_P1_WIN_STATUS_SPRITE_ID))->string = "LOSES!";
		((AlphaSprite*)SpriteArrayList_getWithId(this->boardSprites, ALPHA_P2_WIN_STATUS_SPRITE_ID))->string = "WINS! ";
	}
}
