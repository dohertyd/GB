//
//  Pipe.m
//  Golgi Bird
//
//  Created by Brian Kelly on 18/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import "PipeView.h"
#import "Playfield.h"
#import "Controller.h"

@implementation PipeView

- (void)setX:(CGFloat) x
{
    if(x >= (-PIPE_WIDTH_REF) && x < SCREEN_WIDTH_REF){
        self.frame = CGRectMake(x * ctrlr.xScale, 0.0f, PIPE_WIDTH_REF * ctrlr.xScale, SCREEN_HEIGHT_REF * ctrlr.yScale);
        self.hidden = FALSE;
    }
    else{
        self.hidden = TRUE;
    }
}

- (Hurdle *)getHurdle
{
    return hurdle;
}

- (void)removeHurdle
{
    if(hurdle != nil){
        hurdle.pipeView = nil;
    }
    hurdle = nil;
}

- (void)setHurdle:(Hurdle *)_hurdle
{
    UIImageView *iv;
    NSArray *ar;
    hurdle = _hurdle;
    hurdle.pipeView = self;
    
    
    
    [self removeFromSuperview];
    
    while((ar = [self subviews]).count > 0){
        [((UIView *)[ar objectAtIndex:0]) removeFromSuperview];
    }
    
    if(bodyImg == nil){
        bodyImg = [UIImage imageWithContentsOfFile:[[NSBundle mainBundle ] pathForResource:@"pipe"
                                                                                    ofType:@"png"
                                                                               inDirectory: @"/"]];
        topImg = [UIImage imageWithContentsOfFile:[[NSBundle mainBundle ] pathForResource:@"pipe_top"
                                                                                   ofType:@"png"
                                                                              inDirectory: @"/"]];
        
    }
    
    
    iv = [[UIImageView alloc]initWithImage:bodyImg];
    [self addSubview:iv];
    iv.frame = CGRectMake(0.0, 0.0, PIPE_WIDTH_REF * ctrlr.xScale, (hurdle.gapY - PIPE_GAP_HEIGHT) * ctrlr.yScale);
    
    iv = [[UIImageView alloc]initWithImage:bodyImg];
    [self addSubview:iv];
    iv.frame = CGRectMake(0.0, (hurdle.gapY + PIPE_TOP_HEIGHT_REF) * ctrlr.yScale, PIPE_WIDTH_REF * ctrlr.xScale, (SCREEN_HEIGHT_REF - (hurdle.gapY + PIPE_TOP_HEIGHT_REF)) * ctrlr.yScale);
    
    iv = [[UIImageView alloc]initWithImage:topImg];
    [self addSubview:iv];
    iv.frame = CGRectMake(0.0, hurdle.gapY * ctrlr.yScale, PIPE_WIDTH_REF * ctrlr.xScale, PIPE_TOP_HEIGHT_REF* ctrlr.yScale);
    
    iv = [[UIImageView alloc]initWithImage:topImg];
    iv.transform = CGAffineTransformMakeScale(1, -1);
    [self addSubview:iv];
    iv.frame = CGRectMake(0.0, (hurdle.gapY - (PIPE_GAP_HEIGHT + PIPE_TOP_HEIGHT_REF))* ctrlr.yScale, PIPE_WIDTH_REF * ctrlr.xScale, PIPE_TOP_HEIGHT_REF* ctrlr.yScale);

    [self setX:hurdle.x];
}

- (PipeView *)initWithController:(Controller *)controller
{
    self = [self init];
    ctrlr = controller;
    bodyImg = nil;
    topImg = nil;
    
    return self;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect
{
    // Drawing code
}
*/

@end
