package com.group10.battleship.model;

import java.io.IOException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import com.group10.battleship.graphics.BitmapUtils;
import com.group10.battleship.model.Ship.ShipType;


public class ModelParser { 
	
	private static final String TAG = ModelParser.class.getSimpleName();
	
	public static final String TYPE_KEY  = "type";
	
	public static final String MOVE_TYPE_VAL = "move";
	public static final String MOVE_XPOS_KEY = "xpos";
	public static final String MOVE_YPOS_KEY  = "ypos";
	
	public static final String MOVE_RESPONSE_KEY  = "response";
	public static final String MOVE_RESPONSE_TYPE_VAL = "moveresponse";
	public static final String MOVE_RESPONSE_HIT_KEY = "hit";
	public static final String MOVE_RESPONSE_SUNK_KEY = "sunk";
	public static final String GAME_OVER_TYPE_VAL = "gameover";
	public static final String GAME_OVER_WIN_KEY = "youwin";
	
	public static final String BOARD_TYPE_VAL = "gameboard"; 
	public static final String BOARD_TYPE_SHIPS_KEY = "ships";
	public static final String SHIP_TYPE_VAL = "ship"; 
	public static final String SHIP_TYPE_TYPE_KEY = "shiptype";
	public static final String SHIP_XPOS_KEY = "xpos";
	public static final String SHIP_YPOS_KEY = "ypos";
	public static final String SHIP_HORIZ_KEY = "horiz";
	
	public static final String SHIP_BATTLESHIP_VAL = "battleship";
	public static final String SHIP_PATROL_VAL = "patrol";
	public static final String SHIP_SUB_VAL = "sub";
	public static final String SHIP_CARRIER_VAL = "carrier";
	public static final String SHIP_DESTROYER_VAL = "destroyer";
	
	public static final String YIELD_TURN_TYPE_VAL = "yield";
	
	public static final String PROFILE_TYPE_VAL = "profile";
	public static final String PROFILE_NAME_KEY = "name";
	public static final String PROFILE_IMAGE_KEY = "image";
	public static final String PROFILE_TAUNT_KEY = "taunt";
	
	public static String getJsonForBoard(List<Ship> ships) throws JSONException
	{
		JSONArray shipArr = new JSONArray();
		JSONObject board = new JSONObject();
		
		for(int i=0; i < ships.size(); i++)
		{
			JSONObject ship = new JSONObject();
			ship.put(TYPE_KEY, SHIP_TYPE_VAL);
			ship.put(SHIP_TYPE_TYPE_KEY, getShipTypeFromEnum(ships.get(i).getType()));
			ship.put(SHIP_XPOS_KEY, ships.get(i).getPosIndex().x);
			ship.put(SHIP_YPOS_KEY, ships.get(i).getPosIndex().y);
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
	
	public static String getJsonForMoveResponse(boolean wasHit, boolean wasSunk) throws JSONException
	{
		JSONObject obj = new JSONObject(); 
		obj.put(TYPE_KEY , MOVE_RESPONSE_TYPE_VAL);
		obj.put(MOVE_RESPONSE_HIT_KEY, wasHit);
		obj.put(MOVE_RESPONSE_SUNK_KEY, wasSunk);
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
	
	public static String getJsonForProfile(String name, Uri imageUri, String taunt) throws JSONException
	{
		String imageString = null;
		if (imageUri != null) {
			try {
				Bitmap bm = BitmapUtils.decodeSampledBitmapFromUri(imageUri, 200, 200);
				imageString = BitmapUtils.encodeToBase64(bm);
			} catch (IOException e) {
				Log.e(TAG, "Could not decode profile image");
				e.printStackTrace();
			}
		}
		
		JSONObject obj = new JSONObject(); 
		obj.put(TYPE_KEY , PROFILE_TYPE_VAL); 
		obj.put(PROFILE_NAME_KEY, name);
		obj.put(PROFILE_IMAGE_KEY, imageString);
		obj.put(PROFILE_TAUNT_KEY, taunt);
		String json = obj.toString(); 
		return json;
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