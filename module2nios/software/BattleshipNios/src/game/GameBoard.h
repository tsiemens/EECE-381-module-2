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

#define GAMEBOARD_LENGTH 10
#define GAMEBOARD_COL_WIDTH (SCREEN_HEIGHT/(GAMEBOARD_LENGTH*2+2))
#define GAMEBOARD_COL_HEIGHT GAMEBOARD_COL_WIDTH

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
void GameBoard_hostHis(GameBoard* this, int x, int y);
void GameBoard_p2Miss(GameBoard* this, int x, int y);
void p2Hit(GameBoard* this, int x, int y);

#endif /*GAMEBOARD_H_*/
