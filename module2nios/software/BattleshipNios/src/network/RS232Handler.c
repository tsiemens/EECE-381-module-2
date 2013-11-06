/*
 * RS232Handler.c
 *
 *  Created on: 2013-11-06
 *      Author: trevsiemens
 */

#include <stdio.h>
#include "altera_up_avalon_rs232.h"
#include <string.h>
#include "RS232Handler.h"

static alt_up_rs232_dev* s_rs232Dev;

void RS232Handler_init()
{
	unsigned char data;
	unsigned char parity;

	printf("UART Initialization\n");
	s_rs232Dev = alt_up_rs232_open_dev(RS232_0_NAME);

	printf("Clearing uart read buffer\n");
	while (alt_up_rs232_get_used_space_in_read_FIFO(s_rs232Dev)) {
		alt_up_rs232_read_data(s_rs232Dev, &data, &parity);
	}
}

/**
 * Sends data
 */
void RS232Handler_send(unsigned char data[], unsigned char length)
{
	unsigned char data;
	unsigned char parity;

	printf("Sending the message to the Middleman\n");

	// Start with the number of bytes in our message
	alt_up_rs232_write_data(s_rs232Dev, length);

	// Now send the actual message to the Middleman

	for (i = 0; i < length; i++) {
		alt_up_rs232_write_data(uart, message[i]);
	}
}

/**
 * Waits for data, and returns a newly allocated array of bytes
 * Sets the value at numReceived to the number of bytes read
 */
unsigned char* RS232Handler_receive(int* numReceived)
{
	unsigned char data;
	unsigned char parity;
	unsigned char message[];

	// Now receive the message from the Middleman

	printf("Waiting for data to come back from the Middleman\n");
	while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0);

	// First byte is the number of characters in our message

	alt_up_rs232_read_data(uart, &data, &parity);
	int num_to_receive = (int)data;
	message = malloc(sizeof(unsigned char)*num_to_receive);

	printf("About to receive %d characters:\n", num_to_receive);

	for (i = 0; i < num_to_receive; i++) {
		while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0);
		alt_up_rs232_read_data(uart, &data, &parity);

		message[i] = data;
		printf("%c", data);
	}
	printf("\n");

	*numReceived = num_to_receive;
	return message;
}

void uartTest()
{
	int i;
	unsigned char data;
	unsigned char parity;
	unsigned char message[] = "EECE381 is so much fun";

	printf("Sending the message to the Middleman\n");

	// Start with the number of bytes in our message
	alt_up_rs232_write_data(uart, (unsigned char) strlen(message));

	// Now send the actual message to the Middleman

	for (i = 0; i < strlen(message); i++) {
		alt_up_rs232_write_data(uart, message[i]);
	}

	// Now receive the message from the Middleman

	printf("Waiting for data to come back from the Middleman\n");
	while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0)
		;

	// First byte is the number of characters in our message

	alt_up_rs232_read_data(uart, &data, &parity);
	int num_to_receive = (int)data;

	printf("About to receive %d characters:\n", num_to_receive);

	for (i = 0; i < num_to_receive; i++) {
		while (alt_up_rs232_get_used_space_in_read_FIFO(uart) == 0)
			;
		alt_up_rs232_read_data(uart, &data, &parity);

		printf("%c", data);
	}
	printf("\n");
	printf("Message Echo Complete\n");
}
