/*
 * RS232Handler.h
 *
 *  Created on: 2013-11-06
 *      Author: trevsiemens
 */

#ifndef RS232HANDLER_H_
#define RS232HANDLER_H_

void RS232Handler_init();
void RS232Handler_send(unsigned char data[], unsigned char length);
unsigned char* RS232Handler_receive(int* numReceived);

void uartTest();

#endif /* RS232HANDLER_H_ */
