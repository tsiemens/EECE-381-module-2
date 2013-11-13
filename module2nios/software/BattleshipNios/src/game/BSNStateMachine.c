/*
 * BSNStateMachine.c
 *
 *  Created on: 2013-10-08
 *      Author: Jill
 */

#include "BSNStateMachine.h"

#include <stdlib.h>
#include <stdio.h>

BSNStateMachine* BSNStateMachine_alloc()
{
	BSNStateMachine *this = (BSNStateMachine *)malloc(sizeof(BSNStateMachine));
	return this;
}

// Constructor for BSNStateMachine
BSNStateMachine* BSNStateMachine_init(BSNStateMachine* this)
{
	this->state = WAITING_FOR_PLAYERS;

	this->hostConfirmed = 0;
	this->hostPortIp = NULL;
	this->hostClientID = NO_PLAYER_CLIENT_ID;
	this->p2ClientID = NO_PLAYER_CLIENT_ID;

	this->gameBoard = GameBoard_init(GameBoard_alloc());
	this->boardSprites = SpriteArrayList_init(SpriteArrayList_alloc(), 2);

	//DELETE: testing GameBoard
	//testGameBoard(this);
	return this;
}

//DELETE: testing GameBoard
void testGameBoard(BSNStateMachine* this)
{
	int x;
	int y;
	for(x = 0; x < GAMEBOARD_LENGTH; x++)
	{

		for(y = 0; y < GAMEBOARD_LENGTH; y++)
		{
				GameBoard_p2Hit(this, x, y);
				GameBoard_hostMiss(this, x, y);
		}
	}
}

void BSNStateMachine_performFrameLogic(BSNStateMachine* this)
{
	BSNStateMachine_PerformLogic(this);

	//DELETE: testing
	while(1) {
		GameBoard_hostMiss(this, rand()%10, rand()%10);
		//GameBoard_hostHit(this, rand()%10, rand()%10);
		GameBoard_p2Miss(this, rand()%10, rand()%10);
		//GameBoard_p2Hit(this, rand()%10, rand()%10);
	}

	if(this->state == WAITING_FOR_PLAYERS || this->state == PLAYING)
	{
		ProtocolHandler_receive(this);
	}

	// TODO draw
}

void BSNStateMachine_PerformLogic(BSNStateMachine* this)
{
	switch( this->state )
	{
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

void BSNStateMachine_WaitingPerformLogic(BSNStateMachine* this)
{

}

void BSNStateMachine_PlayingPerformLogic(BSNStateMachine* this)
{

}

void BSNStateMachine_GameOverPerformLogic(BSNStateMachine* this)
{

}
