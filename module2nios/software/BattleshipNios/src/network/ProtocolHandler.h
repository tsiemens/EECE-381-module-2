/*
 * ProtocolHandler.h
 *
 *  Created on: 2013-11-08
 *      Author: trevsiemens
 */

#ifndef PROTOCOLHANDLER_H_
#define PROTOCOLHANDLER_H_

#include "../game/BSNStateMachine.h"

// Android to DE2

// new_game = bytes: { ‘G’, [2 byte little endian short short for port], ip string }
//confirm_host_server_started = bytes { ‘C‘ } // sent after you_are_host and before
//shot_missed = bytes: { 'M', ['1' or '2' for board this affects], [1 byte x coord], [1 byte y coord] }
//shot_hit = bytes: { 'H', ['1' or '2' for board this affects], [1 byte x coord], [1 byte y coord] }
//game_over = bytes: { 'O', ['1' or '2' for winner or 'Q' for forfeit/quit midgame] }
void ProtocolHandler_receive(BSNStateMachine* sm);

// DE2 to Android:

//wait_for_game (handshake for new game) = bytes: { ‘W’ }
//you_are_host = bytes: { ‘H’ } // The host has a server socket, so does not need ip of p2
//game_in_progress = bytes: { 'X' } // if a third device sends new_game
void ProtocolHandler_sendSimpleNotification(int clientID, unsigned char c);
//you_are_player2 = bytes: { ‘2’ , [2 byte little endian short short for port of host], ip string of host}
void ProtocolHandler_sendYouAreP2Notification(int clientID, unsigned char* portip, int length);

#endif /* PROTOCOLHANDLER_H_ */
