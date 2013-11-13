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
	int xPosToDraw = GAMEBOARD_LEFT_PADDING;

	//Draw Vertical Lines
	for( i = 0; i <= GAMEBOARD_LENGTH; i++)
	{
		//Red on top
		drawLineForeground(xPosToDraw, GAMEBOARD_COL_HEIGHT, 
					xPosToDraw, GAMEBOARD_COL_HEIGHT*(GAMEBOARD_LENGTH+1),	0xF800);

		//blue on bottom
		drawLineForeground(xPosToDraw, GAMEBOARD_COL_HEIGHT*(GAMEBOARD_LENGTH+1),
					xPosToDraw, GAMEBOARD_COL_HEIGHT*(GAMEBOARD_LENGTH*2+1), 0x333F);
						
		xPosToDraw += GAMEBOARD_COL_WIDTH;
	}
	
	int yPosToDraw = GAMEBOARD_COL_HEIGHT;
	//Draw Horizontal Lines
	for( i = 0; i <= GAMEBOARD_LENGTH*2; i++)
	{
		if(i < GAMEBOARD_LENGTH)
			drawLineForeground(GAMEBOARD_LEFT_PADDING, yPosToDraw,
					GAMEBOARD_RIGHTMOST_COL, yPosToDraw, 0xF800); //Red
		else if(i == GAMEBOARD_LENGTH)
			drawLineForeground(GAMEBOARD_LEFT_PADDING, yPosToDraw,
					GAMEBOARD_RIGHTMOST_COL, yPosToDraw, 0x04A0); //Green
		else
			drawLineForeground(GAMEBOARD_LEFT_PADDING, yPosToDraw,
					GAMEBOARD_RIGHTMOST_COL, yPosToDraw, 0x333F); //Blue
						
		yPosToDraw += GAMEBOARD_COL_HEIGHT;
	}
}

void GameBoard_hostMiss(GameBoard* this, int x, int y)
{
	//Translate x and y boar positions to the screen
	int xToScreen = GAMEBOARD_LEFT_PADDING+(GAMEBOARD_COL_WIDTH*x);
	int yToScreen = GAMEBOARD_COL_HEIGHT+(GAMEBOARD_COL_HEIGHT*y);

	//White
	GameBoard_draw(xToScreen, yToScreen, MISS);
	this->hostBoard[x][y] = MISS;

}

void GameBoard_hostHit(GameBoard* this, int x, int y)
{
	int xToScreen = GAMEBOARD_LEFT_PADDING+(GAMEBOARD_COL_WIDTH*x);
	int yToScreen = GAMEBOARD_COL_HEIGHT+(GAMEBOARD_COL_HEIGHT*y);

	//Orange
	GameBoard_draw(xToScreen, yToScreen, HIT);
	this->hostBoard[x][y] = HIT;
}

void GameBoard_p2Miss(GameBoard* this, int x, int y)
{
	//Translate x and y boar positions to the screen
	int xToScreen = GAMEBOARD_LEFT_PADDING+(GAMEBOARD_COL_WIDTH*x);
	int yToScreen = GAMEBOARD_COL_HEIGHT+(GAMEBOARD_COL_HEIGHT*GAMEBOARD_LENGTH)+(GAMEBOARD_COL_HEIGHT*y);

	GameBoard_draw(xToScreen, yToScreen, MISS);
	this->p2Board[x][y] = MISS;
}

void GameBoard_p2Hit(GameBoard* this, int x, int y)
{
	//Translate x and y boar positions to the screen
	int xToScreen = GAMEBOARD_LEFT_PADDING+(GAMEBOARD_COL_WIDTH*x);
	int yToScreen = GAMEBOARD_COL_HEIGHT+(GAMEBOARD_COL_HEIGHT*GAMEBOARD_LENGTH)+(GAMEBOARD_COL_HEIGHT*y);

	GameBoard_draw(xToScreen, yToScreen, HIT);
	this->p2Board[x][y] = HIT;
}

void GameBoard_draw(int x, int y, int status)
{
	int color;

	if (status == HIT)
		color = 0xFCA0;
	else if (status == MISS)
		color == 0xFFFF;
	else
		color == 0x4516;

	drawBoxForeground(x+1, y+1, x+GAMEBOARD_COL_WIDTH-1, y+GAMEBOARD_COL_HEIGHT-1, color);
}
