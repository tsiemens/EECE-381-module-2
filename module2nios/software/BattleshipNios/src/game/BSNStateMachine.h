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

#define HOST 1
#define P2 2
#define HOST_FORFEIT 3
#define P2_FORFEIT 4

#define GAME_BOARD_LENGTH 10
#define MISSED 1
#define HIT 2

typedef enum {WAITING_FOR_PLAYERS, PLAYING, GAME_OVER, FORFEIT } SystemState; // TODO add other states

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
	int hostBoardHitMiss [GAME_BOARD_LENGTH][GAME_BOARD_LENGTH] = {0};
	int p2BoardHitMiss [GAME_BOARD_LENGTH] [GAME_BOARD_LENGTH] = {0};

	int winner;

} BSNStateMachine;

BSNStateMachine* BSNStateMachine_alloc();
BSNStateMachine* BSNStateMachine_init(BSNStateMachine* this);
void BSNStateMachine_performFrameLogic(BSNStateMachine* this);
void BSNStateMachine_PerformLogic(BSNStateMachine* this);

void BSNStateMachine_StartPerformLogic(BSNStateMachine* this);

#endif /* BSNSTATEMACHINE_H_ */


