//
//  Controller.h
//  Golgi Bird
//
//  Created by Brian Kelly on 19/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Pipe.h"
#import "PipeView.h"
#import "Hurdle.h"
#import "Playfield.h"
#import "Renderer.h"
#import "TapTelegraphSvcGen.h"

#define SCREEN_WIDTH_REF 1080.0
#define SCREEN_HEIGHT_REF  1557.0
#define PLAYER_WIDTH_REF  192.0
#define PLAYER_HEIGHT_REF  112.0
#define PIPE_GAP_HEIGHT  350.0
#define PIPE_YMIN  (PIPE_GAP_HEIGHT + 100.0)
#define PIPE_YMAX  (SCREEN_HEIGHT_REF - PIPE_YMIN)

#define PIPE_WIDTH_REF  200.0
#define PIPE_TOP_HEIGHT_REF 100.0
#define PIPE_SPACING 700.0

#define BOOST  1000.0
#define GRAVITY  4000.0
#define TERMINALV  2000.0
#define MAXBOOST (BOOST * 1.5)

typedef enum{
    PLAY_OR_WATCH=1,
    WATCHING,
    STARTING,
    RUNNING,
    DEAD,
    DEAD_REMOTE,
} GameStateEnum;


@class ViewController;
@interface Controller : NSObject
{
    BOOL dataEnabled;
    NSInteger gameSeed;
    Renderer *renderer;
    double lastTime;
    double timeOfDeath;
    NSInteger score;
    NSInteger hiScore;
    NSInteger personalBest;
    NSInteger tapIndex;
    NSMutableArray *tdQueue;
    GameOverData *gameOverData;
    GolgiTransportOptions *stdGto;
    GameStateEnum gameState;
    struct CGPath *forkPath;
}


@property NSTimer *refreshTimer;
@property ViewController *viewController;
@property Playfield *playfield;
@property double xScale;
@property double yScale;
@property double screenXglobal;
@property double screenXglobalMax;
@property double screenX;
@property double maxY;
@property double maxYglobal;
@property double playerX;
@property double playerXglobal;
@property double playerY;
@property double playerYglobal;
@property double dX;
@property double dY;
@property NSArray *hurdles;
@property NSString *gameId;
@property NSString *netHiScore;

- (void)streamGameArrived:(PlayerInfo *)playerInfo;
- (void)sendTapArrived:(TapData *)tapData;
- (void)gameOverArrived:(GameOverData *)gameOverData;

- (void)touchDown;
- (void)playPressed;
- (void)watchPressed;
- (void)layoutComplete;
- (void)layoutDiscarded;
- (void)incScore;
- (void)gameOver;
- (void)collision;
- (Controller *)initWithViewController:(ViewController *)viewController;


@end
