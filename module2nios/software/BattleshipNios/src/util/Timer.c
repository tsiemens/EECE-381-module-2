/*
 * Timer.c
 *
 *  Created on: 2013-10-02
 *  Author: Trevor Siemens
 */

#include "Timer.h"

#include <stdlib.h>
#include <stdio.h>
#include "sys/alt_timestamp.h"

static int s_timestampTimerStatus = -1;
static double s_timestampTimerFreq = 0;

/**
 * Initialises the system timestamp timer, and
 * sets the static Timer vars.
 */
int Timer_initTimestampTimer()
{
	/*
	 * NOTE: If this receives an error where is is not recognized,
	 * the issue is that you must manually go into the bsp editor
	 * and set sys_clk_timer to use none, and timestamp_timer to timer_0.
	 * The bsp must then be re-generated.
	 */

	int tsStatus = alt_timestamp_start();
	if (tsStatus != 0) {
		printf("Timestamp timer failed with status %d.\n", tsStatus);
	} else {
		printf("Timestamp timer initialised.\n");
		s_timestampTimerFreq = (double)alt_timestamp_freq();
		printf("Timer freq: %0.1f Hz\n", s_timestampTimerFreq);
	}

	s_timestampTimerStatus = tsStatus;
	return tsStatus;
}

/**
 * Initialises a new timer
 *
 * @param waitTimeMs : The time the timer should run for, in miliseconds
 */
Timer* Timer_alloc()
{
	Timer *this = (Timer *)malloc(sizeof(Timer));
	return this;
}

/**
 * Constructor for Timer
 *
 * @param waitTimeMs : The time the timer should run for, in miliseconds
 */
Timer* Timer_init(Timer* this, double waitTimeMs)
{
	this->startTime = 0;
	this->waitTimeMs = waitTimeMs;

	// If the global timer has not been initialised, do so.
	if(s_timestampTimerStatus != 0) {
		if (Timer_initTimestampTimer() != 0) {
			return NULL;
		}
	}

	return this;
}

/**
 * Starts the timer
 *
 * @param this
 */
void Timer_start(Timer* this)
{
	this->startTime = alt_timestamp();
}

/**
 * Returns if the timer is done
 *
 * @param this
 *
 * @return 1 if the timer has finished (or was never started), 0 otherwise or if (Timer* this) is null.
 */
int Timer_isDone(Timer* this)
{
	if (this == NULL)
		return 0;

	if (this->startTime == 0) {
		return 1;
	}

	double timeTaken = Timer_timeElapsed(this);

	if (timeTaken < this->waitTimeMs) {
		return 0;
	} else {
		return 1;
	}
}

/**
 * Returns the time elapsed since the timer was started.
 *
 * @param this
 *
 * @returns the time in milliseconds since the timer was started,
 * 			or 0 if the timer has not been started
 */
double Timer_timeElapsed(Timer* this)
{
	if (this->startTime == 0) {
		return 0.0;
	}

	alt_timestamp_type time = alt_timestamp();
	return ((time - this->startTime)/s_timestampTimerFreq)*1000.0;
}
