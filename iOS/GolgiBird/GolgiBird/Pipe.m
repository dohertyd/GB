//
//  Pipe.m
//  Golgi Bird
//
//  Created by Brian Kelly on 19/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import "Pipe.h"

@implementation Pipe
@synthesize gapY;
@synthesize xPos;

- (Pipe *)initWithXPos:(double)_xPos andGapY:(double)_gapY
{
    self = [self init];
    xPos = _xPos;
    gapY = _gapY;
    
    return self;
}


@end
