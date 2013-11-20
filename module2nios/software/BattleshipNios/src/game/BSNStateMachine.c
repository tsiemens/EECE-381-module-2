/*
 * BSNStateMachine.c
 *
 *  Created on: 2013-10-08
 *      Author: Jill
 */

#include "BSNStateMachine.h"

#include <stdlib.h>
#include <stdio.h>

BSNStateMachine* BSNStateMachine_alloc() {
	BSNStateMachine *this = (BSNStateMachine *) malloc(sizeof(BSNStateMachine));
	return this;
}

// Constructor for BSNStateMachine
BSNStateMachine* BSNStateMachine_init(BSNStateMachine* this) {
	this->state = WAITING_FOR_PLAYERS;

	this->gameBoard = GameBoard_init(GameBoard_alloc());
	this->boardSprites = SpriteArrayList_init(SpriteArrayList_alloc(), 2);

	AlphaSprite* statusSprite = SpriteFactory_generateGameStatusSprite();
	SpriteArrayList_insert(this->boardSprites, statusSprite, 0);

	return this;
}

void BSNStateMachine_performFrameLogic(BSNStateMachine* this)
{
	VideoHandler_drawSprites(this->boardSprites);
	BSNStateMachine_PerformLogic(this);

	if(this->state == WAITING_FOR_PLAYERS || this->state == PLAYING)
	{
		ProtocolHandler_receive(this);
	}
}

void BSNStateMachine_PerformLogic(BSNStateMachine* this) {
	switch (this->state) {
	case WAITING_FOR_PLAYERS:
		BSNStateMachine_WaitingPerformLogic(this);
		break;
	case PLAYING:
		BSNStateMachine_PlayingPerformLogic(this);
		break;
	case GAME_OVER:
		BSNStateMachine_GameOverPerformLogic(this);
		break;
	}
}

void BSNStateMachine_WaitingPerformLogic(BSNStateMachine* this) {
	((AlphaSprite*)SpriteArrayList_getWithId(this->boardSprites, ALPHA_STATUS_SPRITE_ID))->string = "Status: Waiting for host";
}

void BSNStateMachine_PlayingPerformLogic(BSNStateMachine* this) {
	((AlphaSprite*)SpriteArrayList_getWithId(this->boardSprites, ALPHA_STATUS_SPRITE_ID))->string = "Status: Game In Progress";
}

void BSNStateMachine_GameOverPerformLogic(BSNStateMachine* this) {
	((AlphaSprite*)SpriteArrayList_getWithId(this->boardSprites, ALPHA_STATUS_SPRITE_ID))->string = "Status: Game Over       ";
}
