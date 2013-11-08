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

// Android to DE2

// new_game = bytes: { ‘G’, [2 byte little endian short short for port], ip string }
//confirm_host_server_started = bytes { ‘C‘ } // sent after you_are_host and before
//shot_missed = bytes: { 'M', ['1' or '2' for board this affects], [1 byte x coord], [1 byte y coord] }
//shot_hit = bytes: { 'H', ['1' or '2' for board this affects], [1 byte x coord], [1 byte y coord] }
//game_over = bytes: { 'O', ['1' or '2' for winner or 'Q' for forfeit/quit midgame] }
void ProtocolHandler_receive(BSNStateMachine* sm)
{
	int length;
	int clientID = 0;
	unsigned char* data = RS232Handler_receive(&clientID, &length);
	if (data[0] == 'G'){
		if (sm->state == WAITING_FOR_PLAYERS){
			if (sm->hostPortIp == NULL){
				// This is the first client to contact, so set as host
				sm->hostPortIp = data;
				sm->hostPortIpLength = length;
				sm->hostClientID = clientID;
				printf("Sending host confirmation\n");
				ProtocolHandler_sendSimpleNotification(clientID, 'H');
			} else if (sm->p2ClientID == NO_PLAYER_CLIENT_ID){
				// This is a second player
				sm->p2ClientID = clientID;
				if (sm->hostConfirmed != 0 && sm->hostPortIp != NULL){
					printf("Sending p2 confirmation\n");
					ProtocolHandler_sendYouAreP2Notification(clientID, sm->hostPortIp, sm->hostPortIpLength);
					sm->state = PLAYING;
				}
			} else {
				// A third device is trying to join
				printf("Rejecting game request\n");
				ProtocolHandler_sendSimpleNotification(clientID, 'X');
			}
		}
	} else if (data[0] == 'C'){
		// Host is confirming that is is ready
		sm->hostConfirmed = 1;
		if (sm->state == WAITING_FOR_PLAYERS && sm->p2ClientID != NO_PLAYER_CLIENT_ID) {
			printf("Sending p2 confirmation\n");
			ProtocolHandler_sendYouAreP2Notification(sm->p2ClientID, sm->hostPortIp, sm->hostPortIpLength);
			sm->state = PLAYING;
		}
	} else if (data[0] == 'M'){
		// Shot missed

	} else if (data[0] == 'H'){
		// Shot hit

	} else if (data[0] == 'O'){
		// Game over

	}
}

// DE2 to Android:

//you_are_host = bytes: { ‘H’ } // The host has a server socket, so does not need ip of p2
//game_in_progress = bytes: { 'X' } // if a third device sends new_game
void ProtocolHandler_sendSimpleNotification(int clientID, unsigned char c)
{
	unsigned char msg = c;
	RS232Handler_send((unsigned char)clientID, &msg, 1);
}

//you_are_player2 = bytes: { ‘2’ , [2 byte little endian short short for port of host], ip string of host}
void ProtocolHandler_sendYouAreP2Notification(int clientID, unsigned char* portip, int length)
{
	int newLength = length+1;
	unsigned char* msg = malloc(sizeof(unsigned char)*newLength);
	msg[0] = '2';

	int i;
	for(i = 1; i < newLength; i++){
		msg[i] = portip[i-1];
	}
	RS232Handler_send((unsigned char)clientID, msg, newLength);
}
