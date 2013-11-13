/*
 * GameBoard.c
 *
 *  Created on: 2013-11-12
 *      Author: Wes
 */

#include "GameBoard.h"

extern GameBoard* GameBoard_alloc()
{
	GameBoard *this = (GameBoard*)malloc(sizeof(GameBoard));
	return this;
}


extern GameBoard* GameBoard_init(GameBoard* this)
{
	this->hostMiss = &GameBoard_hostMiss;
	this->hostHit = &GameBoard_hostHit;
	this->p2Miss = &GameBoard_p2Miss;
	this->p2Hit = &GameBoard_p2Hit;

	GameBoard_reset(this);
}

void GameBoard_reset(GameBoard* this)
{
	int i;
	int xPosToDraw = GAMEBOARD_COL_WIDTH;  

	//Draw Vertical Lines
	for( i = 0; i <= GAMEBOARD_LENGTH; i++)
	{
		drawLineForeground(xPosToDraw, GAMEBOARD_COL_HEIGHT, 
						xPosToDraw, SCREEN_HEIGHT-GAMEBOARD_COL_HEIGHT);
						
		xPosToDraw += GAMEBOARD_COL_WIDTH;
	}
	
	int yPosToDraw = GAMEBOARD_COL_HEIGHT;
	//Draw Horizontal Lines
	for( i = 0; i <= GAMEBOARD_LENGTH*2; i++)
	{
		drawLineForeground(GAMEBOARD_COL_WIDTH, yPosToDraw, 
						SCREEN_WIDTH-GAMEBOARD_COL_WIDTH, yPosToDraw);
						
		yPosToDraw += GAMEBOARD_COL_HEIGHT;
	}
}

void GameBoard_hostMiss(GameBoard* this, int x, int y)
{
	this->hostBoard[x][y] = MISS;
}

void GameBoard_hostHit(GameBoard* this, int x, int y)
{
	this->hostBoard[x][y] = HIT;
}

void GameBoard_p2Miss(GameBoard* this, int x, int y)
{
	this->p2Board[x][y] = MISS;
}

void p2Hit(GameBoard* this, int x, int y)
{
	this->p2Board[x][y] = HIT;
}
