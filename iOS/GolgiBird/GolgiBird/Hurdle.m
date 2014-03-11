//
//  Hurdle.m
//  Golgi Bird
//
//  Created by Brian Kelly on 19/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import "Hurdle.h"
#import "Controller.h"
#import "Random.h"


@implementation Hurdle
@synthesize x;
@synthesize gapY;
@synthesize cleared;
@synthesize pipeView;

- (Hurdle *)initWithController:(Controller *)_ctrlr andXPos:(NSInteger)xPos
{
    self = [self init];
    ctrlr = _ctrlr;
    x = xPos;
    gapY = PIPE_YMIN + [Random nextWithMax:(PIPE_YMAX - PIPE_YMIN)];
    cleared = FALSE;
    
    return self;
}

@end
