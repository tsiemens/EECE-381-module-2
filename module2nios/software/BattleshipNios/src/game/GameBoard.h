/*
 * GameBoard.h
 *
 *  Created on: 2013-11-12
 *      Author: Wes
 */

#ifndef GAMEBOARD_H_
#define GAMEBOARD_H_

#include "../video/VideoHandler.h"

#define MISS 1
#define HIT 2

//ADJUSTABLE GAMEBOARD SETTINGS
#define GAMEBOARD_LEFT_PADDING ((SCREEN_WIDTH/2)-(GAMEBOARD_COL_WIDTH*GAMEBOARD_LENGTH/2)) //Set to exactly half the screen
#define GAMEBOARD_LENGTH 10

//PIECES TOGETHER THE GAMEBOARD DO NOT CHANGE
#define GAMEBOARD_COL_WIDTH (SCREEN_HEIGHT/(GAMEBOARD_LENGTH*2+2))
#define GAMEBOARD_COL_HEIGHT GAMEBOARD_COL_WIDTH
#define GAMEBOARD_RIGHTMOST_COL (GAMEBOARD_LEFT_PADDING+GAMEBOARD_COL_WIDTH*GAMEBOARD_LENGTH)

//END GAMEBOARD SETTINGS

typedef struct GameBoard
{
	int hostBoard[GAMEBOARD_LENGTH][GAMEBOARD_LENGTH];
	int p2Board[GAMEBOARD_LENGTH][GAMEBOARD_LENGTH];

	void (*hostHit)(struct GameBoard*, int x, int y);
	void (*hostMiss)(struct GameBoard*, int x, int y);
	void (*p2Hit)(struct GameBoard*, int x, int y);
	void (*p2Miss)(struct GameBoard*, int x, int y);
} GameBoard;

GameBoard* GameBoard_alloc();

GameBoard* GameBoard_init(GameBoard* this);

void GameBoard_reset(GameBoard* this);
void GameBoard_hostMiss(GameBoard* this, int x, int y);
void GameBoard_hostHit(GameBoard* this, int x, int y);
void GameBoard_p2Miss(GameBoard* this, int x, int y);
void GameBoard_p2Hit(GameBoard* this, int x, int y);

#endif /*GAMEBOARD_H_*/
