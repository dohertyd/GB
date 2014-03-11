//
//  Hurdle.h
//  Golgi Bird
//
//  Created by Brian Kelly on 19/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import <Foundation/Foundation.h>

@class Controller;
@class PipeView;

@interface Hurdle : NSObject
{
    Controller *ctrlr;
}
@property NSInteger x;
@property NSInteger gapY;
@property BOOL cleared;
@property PipeView *pipeView;


- (Hurdle *)initWithController:(Controller *)ctrlr andXPos:(NSInteger)xPos;

@end
