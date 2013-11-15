package com.group10.battleship.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.group10.battleship.model.Ship.ShipType;


public class ModelParser { 
	
	public static String TYPE_KEY  = "type";
	
	public static String MOVE_TYPE_VAL = "move";
	public static String MOVE_XPOS_KEY = "xpos";
	public static String MOVE_YPOS_KEY  = "ypos";
	
	public static String MOVE_RESPONSE_KEY  = "response";
	public static String MOVE_RESPONSE_TYPE_VAL = "moveresponse";
	public static String MOVE_RESPONSE_HIT_KEY = "hit";
	public static String GAME_OVER_TYPE_VAL = "gameover";
	public static String GAME_OVER_WIN_KEY = "youwin";
	
	public static String BOARD_TYPE_VAL = "gameboard"; 
	public static String BOARD_TYPE_SHIPS_KEY = "ships";
	public static String SHIP_TYPE_VAL = "ship"; 
	public static String SHIP_TYPE_TYPE_KEY = "shiptype";
	public static String SHIP_XPOS_KEY = "xpos";
	public static String SHIP_YPOS_KEY = "ypos";
	public static String SHIP_HORIZ_KEY = "horiz";
	
	public static String SHIP_BATTLESHIP_VAL = "battleship";
	public static String SHIP_PATROL_VAL = "patrol";
	public static String SHIP_SUB_VAL = "sub";
	public static String SHIP_CARRIER_VAL = "carrier";
	public static String SHIP_DESTROYER_VAL = "destroyer";
	
	public final static String YIELD_TURN_TYPE_VAL = "yield";
	
	public static String getJsonForBoard(List<Ship> ships) throws JSONException
	{
		JSONArray shipArr = new JSONArray();
		JSONObject board = new JSONObject();
		
		for(int i=0; i < ships.size(); i++)
		{
			JSONObject ship = new JSONObject();
			ship.put(TYPE_KEY, SHIP_TYPE_VAL);
			ship.put(SHIP_TYPE_TYPE_KEY, getShipTypeFromEnum(ships.get(i).getType()));
			ship.put(SHIP_XPOS_KEY, ships.get(i).getPosIndex()[0]);
			ship.put(SHIP_YPOS_KEY, ships.get(i).getPosIndex()[1]);
			ship.put(SHIP_HORIZ_KEY, ships.get(i).isHorizontal());
			shipArr.put(ship);
		}
		
		board.put(TYPE_KEY, BOARD_TYPE_VAL);
		board.put(BOARD_TYPE_SHIPS_KEY, shipArr);
		return board.toString();
	}
	
	public static String getJsonForMove(int xPos, int yPos, String response) throws JSONException
	{
		JSONObject obj = new JSONObject(); 
		obj.put(TYPE_KEY , MOVE_TYPE_VAL);
		obj.put(MOVE_XPOS_KEY, xPos);
		obj.put(MOVE_YPOS_KEY, yPos);
		obj.put(MOVE_RESPONSE_KEY, response);
		String json = obj.toString(); 
		return json; 
	}
	
	public static String getJsonForMoveResponse(boolean wasHit) throws JSONException
	{
		JSONObject obj = new JSONObject(); 
		obj.put(TYPE_KEY , MOVE_RESPONSE_TYPE_VAL);
		obj.put(MOVE_RESPONSE_HIT_KEY, wasHit);
		String json = obj.toString(); 
		return json; 
	}
	
	public static String getJsonForGameOver(boolean hasWon) throws JSONException
	{
		JSONObject obj = new JSONObject(); 
		obj.put(TYPE_KEY , GAME_OVER_TYPE_VAL); 
		obj.put(GAME_OVER_WIN_KEY, hasWon);
		String json = obj.toString(); 
		return json; 
	}
	
	public static String getJsonForYield() throws JSONException
	{
		JSONObject yield = new JSONObject(); 
		yield.put(TYPE_KEY, YIELD_TURN_TYPE_VAL);
		return yield.toString();
	}

	
	private static String getShipTypeFromEnum(ShipType st)
	{
		switch(st) {
		case CARRIER:
			return SHIP_CARRIER_VAL; 
		case BATTLESHIP:
			return SHIP_BATTLESHIP_VAL;
		case DESTROYER:
			return SHIP_DESTROYER_VAL; 
		case SUB:
			return SHIP_SUB_VAL;
		case PATROL:
			return SHIP_PATROL_VAL;
		default:
			return "";
		}			
	}
	
	public static ShipType getShipTypeFromString(String st)
	{
		if(st.equals(SHIP_CARRIER_VAL))
		{
			return ShipType.CARRIER;
		}
		else if(st.equals(SHIP_BATTLESHIP_VAL))
		{
			return ShipType.BATTLESHIP; 
		}
		else if(st.equals(SHIP_DESTROYER_VAL))
		{
			return ShipType.DESTROYER;
		}
		else if(st.equals(SHIP_SUB_VAL))
		{
			return ShipType.SUB;
		}
		else if(st.equals(SHIP_PATROL_VAL))
		{
			return ShipType.PATROL;
		}
		else 
		{
			return ShipType.PATROL; 
		}		
	}
		
}