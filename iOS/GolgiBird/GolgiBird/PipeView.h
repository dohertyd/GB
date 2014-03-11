//
//  Pipe.h
//  Golgi Bird
//
//  Created by Brian Kelly on 18/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Hurdle.h"

@class Controller;

@interface PipeView : UIView
{
    Controller *ctrlr;
    Hurdle *hurdle;
    UIImage *topImg;
    UIImage *bodyImg;
}

- (void)setX:(CGFloat) x;
- (void)removeHurdle;
- (Hurdle *)getHurdle;
- (void)setHurdle:(Hurdle *)hurdle;

- (PipeView *)initWithController:(Controller *)controller;

@end
