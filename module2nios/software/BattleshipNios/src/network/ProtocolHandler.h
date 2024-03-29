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
void ProtocolHandler_receive(BSNStateMachine* sm);

// DE2 to Android:
void ProtocolHandler_sendSimpleNotification(unsigned char c);

void ProtocolTest();

#endif /* PROTOCOLHANDLER_H_ */
