//
//  Renderer.h
//  Golgi Bird
//
//  Created by Brian Kelly on 19/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import <Foundation/Foundation.h>

@class Controller;

@interface Renderer : NSObject
{
    Controller *ctrlr;
    NSMutableArray *pipeViews;
    NSInteger firstHurdle;
    UIView *playerView;
}

- (void)reset;
- (void)draw;
- (Renderer *)initWithController:(Controller *)ctrlr;


@end
