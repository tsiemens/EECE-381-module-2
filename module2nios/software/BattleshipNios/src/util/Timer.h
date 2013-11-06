/*
 * Timer.h
 *
 *  Created on: 2013-10-02
 *  Author: Trevor Siemens
 */

#ifndef TIMER_H_
#define TIMER_H_

#include "sys/alt_timestamp.h"

typedef struct Timer
{
	// When the timer was started (32 or 64bit)
	alt_timestamp_type startTime;

	// How long the timer should run
	double waitTimeMs;
} Timer;

Timer* Timer_alloc();

Timer* Timer_init(Timer* this, double waitTimeMs);

void Timer_start(Timer* this);

int Timer_isDone(Timer* this);

double Timer_timeElapsed(Timer* this);

#endif /* TIMER_H_ */
