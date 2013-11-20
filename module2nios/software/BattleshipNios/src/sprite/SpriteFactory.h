#ifndef SPRITEFACTORY_H_
#define SPRITEFACTORY_H_

#include "../sprite/ImgSprite.h"
#include "../sprite/RectSprite.h"
#include "../sprite/AlphaSprite.h"
#include "../sprite/SpriteParser.h"
#include "../sprite/BaseSprite.h"
#include "../sprite/SpriteArrayList.h"

// Should return an array of sprite arrays
SpriteArrayList** SpriteFactory_generateNewBoard();
char intToChar(int val);

#define ALPHA_STATUS_SPRITE_ID 1

/****************CHAR AND PIXEL CONVERSIONS****************/
#define CHAR_TO_PIXEL_HEIGHT ((float)SCREEN_HEIGHT/CHARBUFFER_HEIGHT)
#define PIXEL_TO_CHAR_HEIGHT ((float)CHARBUFFER_HEIGHT/SCREEN_HEIGHT)
#define CHAR_TO_PIXEL_WIDTH ((float)SCREEN_WIDTH/CHARBUFFER_WIDTH)
#define PIXEL_TO_CHAR_WIDTH ((float)CHARBUFFER_WIDTH/SCREEN_WIDTH)

/**************** GENERAL DEFINITIONS****************/
#define GAMEBOARD_TOP_CHAR_PADDING 10
#define SCREEN_WIDTH 320
#define SCREEN_HEIGHT 240

#define CHARBUFFER_WIDTH 80
#define CHARBUFFER_HEIGHT 60

#define CHAR_PADDING_Y 6
#define CHAR_PADDING_X 4
#define CHAR_NEWLINE 3

#endif /* SPRITEFACTORY_H_ */
