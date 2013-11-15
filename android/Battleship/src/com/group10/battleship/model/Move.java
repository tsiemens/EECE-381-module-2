package com.group10.battleship.model;

public class Move extends Object
{
	public int xpos; 
	public int ypos;
	public MoveResponse response;
	
	public Move(int xCoord, int yCoord, MoveResponse moveResponse)
	{
		xpos = xCoord; 
		ypos = yCoord; 
		response = moveResponse;
	}
	
	public MoveResponse getMoveResponse()
	{
		return response;
	}
	
	public int getX()
	{
		return xpos; 
	}

	public int getY()
	{
		return ypos; 
	}
}