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

	this->boardSprites = SpriteArrayList_init(SpriteArrayList_alloc(), 2);

	return this;
}

void BSNStateMachine_performFrameLogic(BSNStateMachine* this)
{
	BSNStateMachine_PerformLogic(this);

	if(this->state == WAITING_FOR_PLAYERS)
	{

	}
}

void BSNStateMachine_PerformLogic(BSNStateMachine* this)
{
	switch( this->state )
	{
	    case WAITING_FOR_PLAYERS:
	        BSNStateMachine_StartPerformLogic(this);
	        break;
	}
}

void BSNStateMachine_StartPerformLogic(BSNStateMachine* this)
{

}
