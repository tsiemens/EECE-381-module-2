/*
 * BSNStateMachine.h
 *
 *  Created on: 2013-10-08
 *      Author: Jill
 */

#ifndef BSNSTATEMACHINE_H_
#define BSNSTATEMACHINE_H_

#include "../util/Timer.h"
#include "../sprite/SpriteFactory.h"
#include "../video/VideoHandler.h"
#include "../sprite/BaseSprite.h"
#include "../sprite/SpriteArrayList.h"
#include "../sprite/ImgSprite.h"
#include "../sprite/RectSprite.h"
#include "../sprite/AlphaSprite.h"
#include "../sprite/SpriteParser.h"

#define NO_PLAYER_CLIENT_ID -1

typedef enum {WAITING_FOR_PLAYERS, PLAYING, GAME_OVER } SystemState; // TODO add other states

typedef struct BSNStateMachine
{
	// The game state
	SystemState state;

	unsigned char* hostPortIp;
	int hostPortIpLength;
	short hostConfirmed;
	int hostClientID;
	int p2ClientID;

	SpriteArrayList* boardSprites;

} BSNStateMachine;

BSNStateMachine* BSNStateMachine_alloc();
BSNStateMachine* BSNStateMachine_init(BSNStateMachine* this);
void BSNStateMachine_performFrameLogic(BSNStateMachine* this);
void BSNStateMachine_PerformLogic(BSNStateMachine* this);

void BSNStateMachine_StartPerformLogic(BSNStateMachine* this);

#endif /* BSNSTATEMACHINE_H_ */

