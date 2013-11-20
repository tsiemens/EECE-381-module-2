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
#define GAMEBOARD_LEFT_PADDING (23) //Set to exactly half the screen
#define GAMEBOARD_LENGTH 10

#define GAMEBOARD_TOP_PADDING GAMEBOARD_COL_HEIGHT*5.5
#define GAMEBOARD_TOP_CHAR_PADDING PIXEL_TO_CHAR_HEIGHT*GAMEBOARD_COL_HEIGHT*5
#define GAMEBOARD_LEFT_PADDING2 (20)
#define GAMEBOARD_LEFT_CHAR_PADDING ((PIXEL_TO_CHAR_WIDTH*(GAMEBOARD_LEFT_PADDING2+GAMEBOARD_COL_WIDTH))-2)

#define GAMEBOARD_HOST_COLOR 0xC986
#define GAMEBOARD_P2_COLOR 0x333F
#define GAMEBOARD_HIT_COLOR 0xFCA0
#define GAMEBOARD_MISS_COLOR 0x3186
#define GAMEBOARD_CENTER_COLOR 0xCE59
#define GAMEBOARD_WATER_COLOR 0x34B9

//PIECES TOGETHER THE GAMEBOARD DO NOT CHANGE
#define GAMEBOARD_COL_WIDTH (SCREEN_HEIGHT/(GAMEBOARD_LENGTH*2))
#define GAMEBOARD_COL_HEIGHT GAMEBOARD_COL_WIDTH
#define GAMEBOARD_RIGHTMOST_COL (GAMEBOARD_LEFT_PADDING+(GAMEBOARD_COL_WIDTH*(GAMEBOARD_LENGTH*2+1)))

//END GAMEBOARD SETTINGS

typedef struct GameBoard
{
	int hostBoard[GAMEBOARD_LENGTH][GAMEBOARD_LENGTH];
	int p2Board[GAMEBOARD_LENGTH][GAMEBOARD_LENGTH];
} GameBoard;

GameBoard* GameBoard_alloc();

GameBoard* GameBoard_init(GameBoard* this);

void GameBoard_reset(GameBoard* this);
void GameBoard_hostMiss(GameBoard* this, int x, int y);
void GameBoard_hostHit(GameBoard* this, int x, int y);
void GameBoard_p2Miss(GameBoard* this, int x, int y);
void GameBoard_p2Hit(GameBoard* this, int x, int y);

#endif /*GAMEBOARD_H_*/
