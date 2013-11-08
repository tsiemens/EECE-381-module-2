/*
 * Module1Main.c
 *
 *  Created on: 2013-09-27
 */

#include <stdio.h>
#include "system.h"
#include "io.h"
#include "util/Timer.h"
#include "video/VideoHandler.h"
#include "util/ArrayPtr.h"
#include "sprite/ImgSprite.h"
#include "sprite/SpriteParser.h"
#include "game/BSNStateMachine.h"
#include "network/RS232Handler.h"
#include "network/ProtocolHandler.h"

// Approx time per loop for 60 Hz
#define MAIN_LOOP_MIN_TIME_MS 16

int main()
{

	// INITIALISATION
	int hasQuit = 0;

	// Timestamp timer setup
	Timer* loopTimer = Timer_init(Timer_alloc(), MAIN_LOOP_MIN_TIME_MS);

	// Init LEDs
	unsigned char ledVals = 0;
	IOWR_8DIRECT(LEDS_BASE, 0, ledVals);

	RS232Handler_init();

	// Video and Character handler init
	VideoHandlerInit();

	BSNStateMachine* stateMachine = BSNStateMachine_init(BSNStateMachine_alloc());

	// TESTING
	int hostID = 1;
	int clientID = 2;
	int idTemp;
	int numTemp;
	RS232Handler_send(hostID, "GPP123.456.7.8", 14);
	ProtocolHandler_receive(stateMachine);
	RS232Handler_receive(&idTemp, &numTemp);
	RS232Handler_send(hostID, "C", 1);
	ProtocolHandler_receive(stateMachine);
	RS232Handler_send(clientID, "GPP777.777.7.7", 14);
	ProtocolHandler_receive(stateMachine);

	char debugFreqStr[10];

	printf("Done init");

	// MAIN PROGRAM LOOP
	while (hasQuit == 0) {
		Timer_start(loopTimer);

		// Debug lights (they increment each frame)
		ledVals++;
		IOWR_8DIRECT(LEDS_BASE, 0, ledVals);

		// TODO insert game logic here
		BSNStateMachine_performFrameLogic(stateMachine);


		// Swap buffers and clear background buffer
		//display();

		sprintf(debugFreqStr, "FPS:%2.1f", 1000/Timer_timeElapsed(loopTimer));
		//printString(debugFreqStr, 70, 0);

	}

	return 0;
}
