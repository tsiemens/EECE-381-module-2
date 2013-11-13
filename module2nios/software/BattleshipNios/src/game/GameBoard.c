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
	GameBoard_drawGrid();
	GameBoard_drawAlpha();
	GameBoard_drawWater();
}

void GameBoard_drawGrid()
{
	int i;
	int xPosToDraw = GAMEBOARD_LEFT_PADDING;
	int yPosToDraw = GAMEBOARD_TOP_PADDING;

	//Draw Vertical Lines
	for( i = 0; i <= GAMEBOARD_LENGTH; i++)
	{
		//Red on top
		drawLineForeground(xPosToDraw, GAMEBOARD_TOP_PADDING,
					xPosToDraw, GAMEBOARD_TOP_PADDING+(GAMEBOARD_COL_HEIGHT*(GAMEBOARD_LENGTH)), GAMEBOARD_HOST_COLOR);

		//blue on bottom
		drawLineForeground(xPosToDraw, GAMEBOARD_TOP_PADDING+(GAMEBOARD_COL_HEIGHT*(GAMEBOARD_LENGTH)),
					xPosToDraw, GAMEBOARD_TOP_PADDING+(GAMEBOARD_COL_HEIGHT*(GAMEBOARD_LENGTH*2+1)), GAMEBOARD_P2_COLOR);
						
		xPosToDraw += GAMEBOARD_COL_WIDTH;
	}
	


	//Draw Horizontal Lines
	for( i = 0; i <= GAMEBOARD_LENGTH*2; i++)
	{
		if(i < GAMEBOARD_LENGTH)
			drawLineForeground(GAMEBOARD_LEFT_PADDING, yPosToDraw,
					GAMEBOARD_RIGHTMOST_COL, yPosToDraw, GAMEBOARD_HOST_COLOR); //Red
		else if(i == GAMEBOARD_LENGTH)
			drawLineForeground(GAMEBOARD_LEFT_PADDING, yPosToDraw,
					GAMEBOARD_RIGHTMOST_COL, yPosToDraw, GAMEBOARD_MISS_COLOR); //Grey
		else
			drawLineForeground(GAMEBOARD_LEFT_PADDING, yPosToDraw,
					GAMEBOARD_RIGHTMOST_COL, yPosToDraw, GAMEBOARD_P2_COLOR); //Blue
						
		yPosToDraw += GAMEBOARD_COL_HEIGHT;
	}
}

GameBoard_drawAlpha()
{
	char letter[] = "A";
	char number[] = "1";

	int i = 0;
	for (i = 0; i < GAMEBOARD_LENGTH*2; i++)
	{
		//Print the letters on the left side
		printString(letter, PIXEL_TO_CHAR_WIDTH*(GAMEBOARD_LEFT_PADDING-1), (PIXEL_TO_CHAR_HEIGHT*GAMEBOARD_COL_HEIGHT)+(3*i));

		//Print the numbers on the top and bottom
		if(i < GAMEBOARD_LENGTH)
			printString(number, (GAMEBOARD_LEFT_CHAR_PADDING+3+3*i) ,0);
		else if(i == GAMEBOARD_LENGTH)
			printString("10", (GAMEBOARD_LEFT_CHAR_PADDING+3*i) ,0);

		letter[0]++;
		number[0]++;

		if(i == 9)
			letter[0] = 'A';
	}
}

GameBoard_drawWater()
{
	int xToScreen;
	int p2YToScreen;
	int hostYToScreen;


	int x;
	int y;
	for(x = 0; x < GAMEBOARD_LENGTH; x++)
	{
		for(y = 0; y < GAMEBOARD_LENGTH; y++)
		{
			xToScreen = GAMEBOARD_LEFT_PADDING+(GAMEBOARD_COL_WIDTH*x);
			p2YToScreen = GAMEBOARD_TOP_PADDING+(GAMEBOARD_COL_HEIGHT*GAMEBOARD_LENGTH)+(GAMEBOARD_COL_HEIGHT*y);
			hostYToScreen = GAMEBOARD_TOP_PADDING+(GAMEBOARD_COL_HEIGHT*y);

			GameBoard_draw(xToScreen, p2YToScreen);
			GameBoard_draw(xToScreen, hostYToScreen);
		}
	}
}

void GameBoard_hostMiss(GameBoard* this, int x, int y)
{
	//Translate x and y boar positions to the screen
	int xToScreen = GAMEBOARD_LEFT_PADDING+(GAMEBOARD_COL_WIDTH*x);
	int yToScreen = GAMEBOARD_TOP_PADDING+(GAMEBOARD_COL_HEIGHT*y);

	//White
	GameBoard_draw(xToScreen, yToScreen, MISS);
	this->hostBoard[x][y] = MISS;

}

void GameBoard_hostHit(GameBoard* this, int x, int y)
{
	int xToScreen = GAMEBOARD_LEFT_PADDING+(GAMEBOARD_COL_WIDTH*x);
	int yToScreen = GAMEBOARD_TOP_PADDING+(GAMEBOARD_COL_HEIGHT*y);

	//Orange
	GameBoard_draw(xToScreen, yToScreen, HIT);
	this->hostBoard[x][y] = HIT;
}

void GameBoard_p2Miss(GameBoard* this, int x, int y)
{
	//Translate x and y boar positions to the screen
	int xToScreen = GAMEBOARD_LEFT_PADDING+(GAMEBOARD_COL_WIDTH*x);
	int yToScreen = GAMEBOARD_TOP_PADDING+(GAMEBOARD_COL_HEIGHT*GAMEBOARD_LENGTH)+(GAMEBOARD_COL_HEIGHT*y);

	GameBoard_draw(xToScreen, yToScreen, MISS);
	this->p2Board[x][y] = MISS;
}

void GameBoard_p2Hit(GameBoard* this, int x, int y)
{
	//Translate x and y boar positions to the screen
	int xToScreen = GAMEBOARD_LEFT_PADDING+(GAMEBOARD_COL_WIDTH*x);
	int yToScreen = GAMEBOARD_TOP_PADDING+(GAMEBOARD_COL_HEIGHT*GAMEBOARD_LENGTH)+(GAMEBOARD_COL_HEIGHT*y);

	GameBoard_draw(xToScreen, yToScreen, HIT);
	this->p2Board[x][y] = HIT;
}

void GameBoard_draw(int x, int y, int status)
{
	int color;

	if (status == HIT)
		color = GAMEBOARD_HIT_COLOR;
	else if (status == MISS)
		color = GAMEBOARD_MISS_COLOR;
	else
		color = GAMEBOARD_WATER_COLOR;

	drawBoxForeground(x+1, y+1, x+GAMEBOARD_COL_WIDTH-1, y+GAMEBOARD_COL_HEIGHT-1, color);
}
