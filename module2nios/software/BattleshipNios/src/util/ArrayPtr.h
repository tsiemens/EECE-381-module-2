/*
 * array_ptr.h
 *
 */

#ifndef ARRAYPTR_H_
#define ARRAYPTR_H_

typedef struct ShortIntPtr {
	short int* data;
	int size;
} ShortIntPtr;

typedef struct UnsignedCharPtr {
	unsigned char* data;
	int size;
} UnsignedCharPtr;

ShortIntPtr* ShortIntPtr_alloc(int data_size);
void ShortIntPtr_free(ShortIntPtr * this);

UnsignedCharPtr* UnsignedCharPtr_alloc(int data_size);
void UnsignedCharPtr_free(UnsignedCharPtr * this);

void ShortToIntCPY(short int* sibuf, unsigned int* uibuf, int size);

#endif
