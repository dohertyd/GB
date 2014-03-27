//
//  Renderer.m
//  Golgi Bird
//
//  Created by Brian Kelly on 19/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import "Renderer.h"
#import "PipeView.h"
#import "Controller.h"
#import "ViewController.h"

static int rawSamples[] = {
    193, 111,  // Dimensions of the image
    88,0,      // Point samples
    142,5,
    192,50,
    139, 81,
    88,108,
    48,95,
    24,71,
    0,38,
    -1, -1};

static int *cSamples = NULL;

@implementation Renderer

- (PipeView *)allocPipeView
{
    PipeView *pv;
    
    if(pipeViews.count > 0){
        pv = [pipeViews firstObject];
        [pipeViews removeObjectAtIndex:0];
    }
    else{
        pv = [[PipeView alloc] initWithController:ctrlr];
    }
    
    return pv;
}

- (void)releasePipeView:(PipeView *)pipeView
{
    [pipeView removeHurdle];
    [pipeViews addObject:pipeView];
    pipeView.hidden = TRUE;
}

- (void)reset
{
    
    firstHurdle = 0;
    if(ctrlr.hurdles != nil){
        for(int i = 0; i < ctrlr.hurdles.count; i++){
            Hurdle *h = [ctrlr.hurdles objectAtIndex:i];
            if(h.pipeView){
                [self releasePipeView:h.pipeView];
            }
        }
    }
}

- (void)draw
{
    BOOL dead;
    NSInteger i;
    
    playerView.frame = CGRectMake(ctrlr.playerX, ctrlr.playerY, PLAYER_WIDTH_REF * ctrlr.xScale, PLAYER_HEIGHT_REF * ctrlr.yScale);

    i = firstHurdle;
    firstHurdle = -1;
    dead = false;
    for(; i < ctrlr.hurdles.count; i++){
        double x;
        Hurdle *h = [ctrlr.hurdles objectAtIndex:i];
        x = h.x - ctrlr.screenXglobal;
        
        if(x < -PIPE_WIDTH_REF){
            if(h.pipeView != nil){
                [self releasePipeView:h.pipeView];
                // NSLog(@"Remove pipeView from %d", i);
            }
        }
        else if(x >= SCREEN_WIDTH_REF){
            break;
        }
        else{
            if(firstHurdle < 0){
                firstHurdle = i;
            }
            if(h.pipeView == nil){
                // NSLog(@"Add pipeView to %d", i);
                h.pipeView = [[self allocPipeView] init];
                [h.pipeView setHurdle:h];
                [ctrlr.playfield addSubview:h.pipeView];
            }
            if((ctrlr.playerXglobal + PLAYER_WIDTH_REF) >= (h.x - ctrlr.screenXglobal)){
                if(ctrlr.playerXglobal < ((h.x - ctrlr.screenXglobal) + PIPE_WIDTH_REF)){
                    if((ctrlr.playerYglobal + PLAYER_HEIGHT_REF) >= h.gapY){
                        dead = TRUE;
                    }
                    else if(ctrlr.playerYglobal < (h.gapY - PIPE_GAP_HEIGHT)){
                        dead = TRUE;
                    }
                    if(dead){
                        //
                        // Coarse collision
                        //
                        dead = false;
                        int hxl = h.x - ctrlr.screenXglobal;
                        int hxr = hxl + PIPE_WIDTH_REF;
                        int hyb = (int)h.gapY;
                        int hyt = hyb - PIPE_GAP_HEIGHT;
                        
                        for(int i = 0; !dead && cSamples[i] >= 0; i+= 2){
                            int px = cSamples[i] + ctrlr.playerXglobal;
                            int py = cSamples[i+1] + ctrlr.playerYglobal;
                            
                            if(px >= hxl && px < hxr && (py >= hyb || py <= hyt)){
                                dead = TRUE;
                            }
                        }
                    }
                }
            }
            if(!h.cleared){
                if((ctrlr.playerXglobal + PLAYER_WIDTH_REF/2) >= ((h.x + PIPE_WIDTH_REF/2) - ctrlr.screenXglobal)){
                    h.cleared = TRUE;
                    [ctrlr incScore];
                }
            }
               
            [h.pipeView setX:x];
            
        }
    }
    if(firstHurdle < 0){
        firstHurdle = 0;
    }
    if(dead){
        // ctrlr.viewController.collisionView.hidden = FALSE;
        [ctrlr collision];
    }
    else{
        // ctrlr.viewController.collisionView.hidden = TRUE;
    }

}
- (Renderer *)initWithController:(Controller *)_ctrlr
{
    int i, j;
    self = [self init];
    ctrlr = _ctrlr;
    pipeViews = [[NSMutableArray alloc]init];
    [self reset];

    
    UIImage *img = [UIImage imageWithContentsOfFile:[[NSBundle mainBundle ] pathForResource:@"player"
                                                                                     ofType:@"png"
                                                                                inDirectory: @"/"]];
    playerView = [[UIImageView alloc]initWithImage:img];
    playerView.frame = CGRectMake(0.0, 0.0, PLAYER_WIDTH_REF * ctrlr.xScale, PLAYER_HEIGHT_REF * ctrlr.yScale);
    [ctrlr.playfield addSubview:playerView];
    ctrlr.viewController.collisionView.hidden = TRUE;

    if(cSamples == NULL){
        for(i = 0; rawSamples[i] != -1; i += 2){
        }
        cSamples = malloc(sizeof(int) * (i + 2));
        j = 0;
        i = 0;
        double maxX = rawSamples[0];
        double maxY = rawSamples[1];
        
        for(i = 2; rawSamples[i] != -1; i += 2){
            double x = rawSamples[i];
            double y = rawSamples[i+1];
            x = ((x / maxX) * (double)PLAYER_WIDTH_REF);
            y = ((y / maxY) * (double)PLAYER_HEIGHT_REF);
            
            cSamples[j++] = (int)x;
            cSamples[j++] = (int)y;
        }
        cSamples[j++] = -1;
        cSamples[j++] = -1;
    }
    
    return self;
}


@end
