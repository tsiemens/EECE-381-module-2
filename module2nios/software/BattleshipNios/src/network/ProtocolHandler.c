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
 * game_over = bytes: { 'O', ['1' or '2' for winner], ['1' for forfeit/quit midgame or '0' for not forfeit] }
 */

void ProtocolHandler_receive(BSNStateMachine* sm) {
	int length;
	unsigned char* data = RS232Handler_receive(&length);
	if (data[0] == 'N') {
		if (sm->state == WAITING_FOR_PLAYERS) {
			printf("Host Connected. Starting Game.\n");
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
		if (data[2] == 1)
			sm->state = FORFEIT;
		else
			sm->state = GAME_OVER;
	}
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
