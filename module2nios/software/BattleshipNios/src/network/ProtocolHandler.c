/*
 * ProtocolHandler.c
 *
 *  Created on: 2013-11-08
 *      Author: trevsiemens
 */

#include <stdio.h>
#include <stdlib.h>
#include "ProtocolHandler.h"
#include "RS232Handler.h"
#include "../game/BSNStateMachine.h"

/**
 * Receives a message from client, and takes action, changing values and state of sm
 *
 * Compatible messages are the following. Action taken depends on state of sm.
 *
 * new_game = bytes: { ‘N’}
 * confirm_host_server_started = bytes { ‘C‘ [2 byte little endian short short for port], ip string} // sent after you_are_host
 * shot_missed = bytes: { 'M', ['1' or '2' for board this affects], [1 byte x coord], [1 byte y coord] }
 * shot_hit = bytes: { 'H', ['1' or '2' for board this affects], [1 byte x coord], [1 byte y coord] }
 * game_over = bytes: { 'O', ['1' or '2' for winner] }
 */

void ProtocolHandler_receive(BSNStateMachine* sm) {
	int length;
	unsigned char* data = RS232Handler_receive(&length);
	if (data[0] == 'N') {
		if (sm->state == WAITING_FOR_PLAYERS || sm->state == GAME_OVER) {
			printf("Host Connected. Starting Game.\n");
			GameBoard_reset(sm->gameBoard);
			AlphaSprite* p1name = ((AlphaSprite*)SpriteArrayList_getWithId(sm->boardSprites, ALPHA_P1_NAME_SPRITE_ID));
			if (p1name->string != PLAYER_1_STRING) {
				free(p1name->string);
			}
			AlphaSprite* p2name = ((AlphaSprite*)SpriteArrayList_getWithId(sm->boardSprites, ALPHA_P2_NAME_SPRITE_ID));
			if (p2name->string != PLAYER_2_STRING) {
				free(p2name->string);
			}
			sm->state = PLAYING;
		}
	} else if (data[0] == 'M' && sm->state == PLAYING) {
		// Shot missed
		if (data[1] == HOST) {
			GameBoard_hostMiss(sm->gameBoard, data[2], data[3]);;
		} else {
			GameBoard_p2Miss(sm->gameBoard, data[2], data[3]);
		}
	} else if (data[0] == 'H' && sm->state == PLAYING) {
		// Shot hit
		if (data[1] == HOST) {
			GameBoard_hostHit(sm->gameBoard, data[2], data[3]);
		} else {
			GameBoard_p2Hit(sm->gameBoard, data[2], data[3]);
		}
	} else if (data[0] == 'O' && sm->state == PLAYING) {
		// Game over
		sm->winner = data[1];
		sm->state = GAME_OVER;
	} else if (data[0] == 'P') {
		// Update player data
		int player = data[1];
		int id = (player == 1)? ALPHA_P1_NAME_SPRITE_ID : ALPHA_P2_NAME_SPRITE_ID;
		int i = 2;
		char* name;
		name = malloc(sizeof(unsigned char) * (length - 2));
		for(i = 2; i < length - 1 && i <= 15; i++) {
			name[i-2] = data[i];
		}
		name[i] = 0;
		AlphaSprite* namespr = ((AlphaSprite*)SpriteArrayList_getWithId(sm->boardSprites, id));
		namespr->string = name;
		printf("player %d's name is %s (%s)\n", player, name, namespr->string);
	}

	free(data);
}

/**
 * Sends message to client over RS232, with the message
 * bytes: { c }
 *
 * @param c -- character to send. Valid characters are the following:
 * 	'H' : Sent to host to tell it that it is host.
 */
void ProtocolHandler_sendSimpleNotification(unsigned char c) {
	unsigned char msg = c;
	RS232Handler_send(&msg, 1);
}

/*
void ProtocolTest(BSNStateMachine* stateMachine) {
	int hostID = 1;
	int clientID = 2;
	int idTemp;
	int numTemp;
	RS232Handler_send(hostID, "N", 1);
	ProtocolHandler_receive(stateMachine);
	RS232Handler_receive(&idTemp, &numTemp);
	RS232Handler_send(hostID, "CPP123.456.7.8", 14);
	ProtocolHandler_receive(stateMachine);
	RS232Handler_send(clientID, "N", 1);
	ProtocolHandler_receive(stateMachine);
}*/
