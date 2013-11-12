/*
 * ArrayPtr.c
 *
 */

#include <stdlib.h>
#include <stdio.h>
#include "ArrayPtr.h"

/*
 * allocate memory for the ShortIntPtr struct
 *
 * @param data_size: number of short int wanted
 *
 * @return struct with all memory allocated
 */
ShortIntPtr* ShortIntPtr_alloc(int data_size) {
	ShortIntPtr* this = (ShortIntPtr *) malloc(sizeof(ShortIntPtr));
	this->data = malloc(data_size * sizeof(short int));
	this->size = 0;
	if (this == NULL || this->data == NULL) {
		printf("Error: ShortIntPtr alloc\n");
	}
	return this;
}

/*
 * Free the memory of ShortIntPtr struct
 *
 * @param this: pointer to free the memory of
 */
void ShortIntPtr_free(ShortIntPtr* this) {
	free(this->data);
	free(this);
}

/*
 * allocate memory for the UnsignedCharPtr struct
 *
 * @param data_size: number of unsigned char wanted
 *
 * @return struct with all memory allocated
 */
UnsignedCharPtr* UnsignedCharPtr_alloc(int data_size) {
	UnsignedCharPtr* this = (UnsignedCharPtr *) malloc(sizeof(UnsignedCharPtr));
	this->data = malloc(data_size * sizeof(unsigned char));
	this->size = 0;
	if (this == NULL || this->data == NULL) {
		printf("Error: UnsignedCharPtr alloc\n");
	}
	return this;
}

/*
 * Free the memory of UnsignedCharPtr struct
 *
 * @param this: pointer to free the memory of
 */
void UnsignedCharPtr_free(UnsignedCharPtr* this) {
	free(this->data);
	free(this);
}

/*
 * Copy all elements of a short int array to an unsigned int array
 *
 * @param sibuf: short int array
 * @param uibuf: unsigned int array
 * @param size: number of elements to copy
 */
void ShortToIntCPY(short int* sibuf, unsigned int* uibuf, int size) {
	int i = 0;
	for (i = 0; i < size; i++) {
		uibuf[i] = sibuf[i];
	}
}
