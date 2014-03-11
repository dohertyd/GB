//
//  Pipe.h
//  Golgi Bird
//
//  Created by Brian Kelly on 19/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Pipe : NSObject

@property double xPos;
@property double gapY;

- (Pipe *)initWithXPos:(double)xPos andGapY:(double)gapY;

@end
