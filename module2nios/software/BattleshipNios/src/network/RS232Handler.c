/*
 * RS232Handler.c
 *
 *  Created on: 2013-11-06
 *      Author: trevsiemens
 */

#include <stdio.h>
#include <stdlib.h>
#include "altera_up_avalon_rs232.h"
#include <string.h>
#include "RS232Handler.h"
#include "io.h"
#include "../util/Timer.h"

static alt_up_rs232_dev* s_rs232Dev;
static int isDebug;

void RS232Handler_init()
{
	unsigned char data;
	unsigned char parity;
	isDebug = 0;

	printf("UART Initialization\n");
	s_rs232Dev = alt_up_rs232_open_dev(RS232_0_NAME);

	printf("Clearing uart read buffer\n");
	while (alt_up_rs232_get_used_space_in_read_FIFO(s_rs232Dev)) {
		alt_up_rs232_read_data(s_rs232Dev, &data, &parity);
	}
}

/**
 * Sends data to clientID (if not debug)
 */
void RS232Handler_send(unsigned char data[], unsigned char length)
{
	printf("Sending the message to the Middleman\n");

	if (isDebug == 0) {
		// Start with the client id
		//alt_up_rs232_write_data(s_rs232Dev, clientID);
	}

	// Second is the number of bytes in our message
	alt_up_rs232_write_data(s_rs232Dev, length);

	// Now send the actual message to the Middleman
	int i;
	for (i = 0; i < length; i++) {
		alt_up_rs232_write_data(s_rs232Dev, data[i]);
		printf("%2c\n", data[i]);
	}
}

/**
 * Waits for data, and returns a newly allocated array of bytes
 * Sets the value at numReceived to the number of bytes read
 * Sets the value at clientID to the client id from which it was received (if not debug)
 */
unsigned char* RS232Handler_receive(int* numReceived)
{
	unsigned char data;
	unsigned char parity;
	unsigned char* message;
	Timer* timer = Timer_init(Timer_alloc(), 3000);


	// Receive the message from the Middleman
	printf("Waiting for data to come back from the Middleman\n");
	while (alt_up_rs232_get_used_space_in_read_FIFO(s_rs232Dev) == 0);

	// Second byte is the clientID of characters in our message
	alt_up_rs232_read_data(s_rs232Dev, &data, &parity);
	int num_to_receive = (int)data;
	message = malloc(sizeof(unsigned char)*num_to_receive);

	printf("About to receive %d characters from (%d):\n", num_to_receive);

	int i;
	for (i = 0; i < num_to_receive; i++) {
		while (alt_up_rs232_get_used_space_in_read_FIFO(s_rs232Dev) == 0);
		alt_up_rs232_read_data(s_rs232Dev, &data, &parity);

		message[i] = data;
		printf("%2x", data);
	}
	printf("\n");

	*numReceived = num_to_receive;
	return message;
}
