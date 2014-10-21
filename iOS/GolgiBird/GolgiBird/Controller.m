//
//  Controller.m
//  Golgi Bird
//
//  Created by Brian Kelly on 19/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import "Controller.h"
#import "Random.h"
#import "ViewController.h"
#import "TapTelegraphSvcWrapper.h"
#import "GameData.h"

@implementation Controller
@synthesize refreshTimer;
@synthesize viewController;
@synthesize playfield;
@synthesize xScale;
@synthesize yScale;
@synthesize screenX;
@synthesize screenXglobal;
@synthesize screenXglobalMax;
@synthesize maxY;
@synthesize maxYglobal;
@synthesize playerX;
@synthesize playerXglobal;
@synthesize playerY;
@synthesize playerYglobal;
@synthesize dX;
@synthesize dY;
@synthesize hurdles;
@synthesize gameId;
@synthesize netHiScore;

- (void)streamGameArrived:(PlayerInfo *)playerInfo
{
    if(gameState == WATCHING){
        gameSeed = [playerInfo getGameSeed];
        [self setupGameStartWithSeed:gameSeed];
        viewController.nameLabel.text = [playerInfo getName];

    }
}

- (void)sendTapArrived:(TapData *)tapData
{
    if(gameState == WATCHING){
        [tdQueue addObject:tapData];
    }
}

- (void)gameOverArrived:(GameOverData *)_gameOverData
{
    if(gameState == WATCHING){
        gameOverData = _gameOverData;
    }
}

- (NSString *)getPlayerName
{
    NSString *name = [[NSUserDefaults standardUserDefaults] stringForKey:@"screenName"];
    return (name == nil) ? @"Anonymous" : name;
}

- (BOOL)getDataEnabled
{
    return [[NSUserDefaults standardUserDefaults] boolForKey:@"broadcastGames"];
}

- (void)refreshTimer:(id)userInfo
{
    double dT;
    double now = CACurrentMediaTime();
    BOOL dead = FALSE;
    
    dT = now - lastTime;
    lastTime = now;
    
    if(gameState == RUNNING || gameState == WATCHING){
        if(gameState == WATCHING){
            if(tdQueue.count >= 2 || (tdQueue.count == 1 && gameOverData != nil)){
                if(screenXglobal >= screenXglobalMax){
                    TapData *td0 = [tdQueue objectAtIndex:0];
                    if(tdQueue.count >= 2){
                        TapData *td1 = [tdQueue objectAtIndex:1];
                        screenXglobalMax = [td1 getScreenOffset];
                    }
                    else{
                        screenXglobalMax = [gameOverData getScreenOffset];
                    }
                    [tdQueue removeObjectAtIndex:0];
                    dY = (double)[td0 getDeltaY] / 1000.0;
                    screenXglobal = [td0 getScreenOffset];
                    playerYglobal = (double)[td0 getPlayerY];
                }
            }
            else if(gameOverData != nil){
                screenXglobalMax = [gameOverData getScreenOffset];
                if(screenXglobal >= screenXglobalMax){
                    [self gameOver];
                    gameOverData = nil;
                }
            }
        }
        if(gameState == RUNNING || screenXglobal < screenXglobalMax){
            screenXglobal += dT * dX;
            playerYglobal += dT * dY;
    
            if((playerYglobal + PLAYER_HEIGHT_REF) > SCREEN_HEIGHT_REF){
                playerYglobal = (SCREEN_HEIGHT_REF - PLAYER_HEIGHT_REF);
                dead = TRUE;
            
            }
            playerY = playerYglobal * yScale;
        
            dY += (GRAVITY * dT);
            if(dY > TERMINALV){
                dY = TERMINALV;
            }
            if(dead && gameState == RUNNING){
                [self gameOver];
            }
        }
    }
    
    [renderer draw];
    
}

- (void)setupGameStartWithSeed:(NSInteger)seed
{
    gameSeed = seed;

    [self generateHurdlesUsingSeed:gameSeed];
    score = 0;
    dX = 300.0;
    dY = 0.0;
    
    if(gameState == STARTING || gameState == WATCHING){
        playerXglobal = ((SCREEN_WIDTH_REF / 3) - (PLAYER_WIDTH_REF / 2));
        playerYglobal = (SCREEN_HEIGHT_REF - PLAYER_HEIGHT_REF)/2;
    }
    else{
        playerXglobal = -PLAYER_WIDTH_REF;
        playerYglobal = 0;
    }
    playerX = (playerXglobal * xScale);
    playerY = (playerYglobal * yScale);
    screenXglobal = 0.0;
    screenX = 0.0;
    
    [self showScore];
    
    

}

- (void)layoutComplete
{
    lastTime = CACurrentMediaTime();
    
    
    refreshTimer = [NSTimer scheduledTimerWithTimeInterval:0.02 target:self selector:@selector(refreshTimer:) userInfo:NULL repeats:YES];
    
}
- (void)layoutDiscarded
{
    [refreshTimer invalidate];
    
}

- (void)generateHurdlesUsingSeed:(long long)seed
{
    NSMutableArray *ha = [[NSMutableArray alloc] init];
    NSInteger i, x;
    

    [renderer reset];
    
    [Random setSeed:seed];
    
    
    x = (SCREEN_WIDTH_REF * 3)/2;
    for(i = 0; i < 500; i++){
        [ha addObject:[[Hurdle alloc] initWithController:self andXPos:x]];
        x += PIPE_SPACING;
    }
    
    hurdles = [NSArray arrayWithArray:ha];
}

- (void)showScore
{
    viewController.scoreLabel.text = [NSString stringWithFormat:@"%ld", (long)score];
    viewController.hiScoreLabel.text = [NSString stringWithFormat:@"%ld", (long)hiScore];
}

- (void)incScore
{
    score++;
    
    if(gameState == RUNNING && score > hiScore){
        hiScore = score;
    }

    [self showScore];
}

- (void)gameOver
{
    if(gameState == RUNNING){
        gameState = DEAD;
        [self showStateStuff];
        timeOfDeath = CACurrentMediaTime();
        [self sendGameOver];
        if(score == hiScore){
            [GameData setHiScore:hiScore];
        }
        if(score > personalBest && score >= 10){
            personalBest = score;
            [self sendNewPB];
        }
    }
    else if(gameState == WATCHING){
        gameState = DEAD_REMOTE;
        [self showStateStuff];
        timeOfDeath = CACurrentMediaTime();
    }
}

- (void)collision
{
    if(gameState == RUNNING){
        [self gameOver];
    }
}

- (void)sendGameStart
{
    PlayerInfo *playerInfo = [[PlayerInfo alloc] init];
    
    [playerInfo setName:[self getPlayerName]];
    [playerInfo setGolgiId:[GameData getInstanceId]];
    [playerInfo setGameId:gameId];
    [playerInfo setHiScore:hiScore];
    [playerInfo setGameSeed:gameSeed];
    [playerInfo setAppVer:1];
    [playerInfo setOs:@"ios"];
    
    NSLog(@"Player Name: %@", [self getPlayerName]);
    
    if(dataEnabled){
        [TapTelegraphSvc sendStartGameUsingResultHandler:^(TapTelegraphStartGameExceptionBundle *exBundle)
                                                        {
                                                            if(exBundle == nil){
                                                                NSLog(@"Start Game: SUCCESS");
                                                            }
                                                            else{
                                                                NSLog(@"Start Game: FAILED '%@'", [exBundle.golgiException getErrText]);
                                                            }
                                                        }
                                    withTransportOptions:stdGto
                                          andDestination:@"SERVER"
                                          withPlayerInfo:playerInfo];

    }
}

- (void)sendTap
{
    TapData *tapData = [[TapData alloc]init];
    
    [tapData setGameId:gameId];
    [tapData setScreenOffset:screenXglobal];
    [tapData setPlayerY:playerYglobal];
    [tapData setDeltaY:(dY * 1000)];
    [tapData setIndex:tapIndex++];
    [tapData setScore:score];
    
    if(dataEnabled){
        [TapTelegraphSvc sendSendTapUsingResultHandler:^(TapTelegraphSendTapExceptionBundle *exBundle)
                                                      {
                                                          if(exBundle == nil){
                                                              NSLog(@"Send Tap: SUCCESS");
                                                          }
                                                          else{
                                                              NSLog(@"Send Tap: FAILED '%@'", [exBundle.golgiException getErrText]);
                                                          }
                                                      }
                                  withTransportOptions:stdGto
                                        andDestination:@"SERVER"
                                           withTapData:tapData];
    }

}

- (void)sendGameOver
{
    GameOverData *god = [[GameOverData alloc] init];
    
    [god setGameId:gameId];
    [god setScreenOffset:screenXglobal];
    [god setPlayerY:playerYglobal];
    [god setScore:score];
    
    if(dataEnabled){
        [TapTelegraphSvc sendGameOverUsingResultHandler:^(TapTelegraphGameOverExceptionBundle *exBundle)
                                                        {
                                                            if(exBundle == nil){
                                                                NSLog(@"Game Over: SUCCESS");
                                                            }
                                                            else{
                                                                NSLog(@"Game Over: FAILED '%@'", [exBundle.golgiException getErrText]);
                                                            }
                                                        }
                                   withTransportOptions:stdGto
                                         andDestination:@"SERVER"
                                       withGameOverData:god];
    }
}

- (void)sendNewPB
{
    HiScoreData *hsd = [[HiScoreData alloc] init];
    
    [hsd setName:[self getPlayerName]];
    [hsd setScore:personalBest];
    
    if(dataEnabled){
        [TapTelegraphSvc sendNewPBUsingResultHandler:^(TapTelegraphNewPBExceptionBundle *exBundle)
                                                    {
                                                        if(exBundle == nil){
                                                            NSLog(@"New PB: SUCCESS");
                                                        }
                                                        else{
                                                            NSLog(@"New PB: FAILED '%@'", [exBundle.golgiException getErrText]);
                                                        }
                                                    }
                                withTransportOptions:stdGto
                                      andDestination:@"SERVER"
                                     withHiScoreData:hsd];
    }
    
    
}

- (void)sendStreamGame
{
    [TapTelegraphSvc sendStreamGameUsingResultHandler:^(TapTelegraphStreamGameExceptionBundle *exBundle)
                                                     {
                                                         if(exBundle == nil){
                                                             NSLog(@"Stream Game: SUCCESS");
                                                         }
                                                         else{
                                                             NSLog(@"Stream Game: FAILED '%@'", [exBundle.golgiException getErrText]);
                                                         }
                                                     }
                                 withTransportOptions:stdGto
                                       andDestination:@"SERVER"
                                          withGolgiId:[GameData getInstanceId]];
}


- (void)touchDown
{
    switch(gameState){
        case PLAY_OR_WATCH:
            break;
        case STARTING:
            gameId = [Random genRandomStringWithLength:10];
            [self sendGameStart];
            gameState = RUNNING;
            [self showStateStuff];
            // the break is commented out on purpose so
            // we start the game with the player travelling
            // "up" the screen.
            // break;
        case RUNNING:
            if(dY > 0.0){
                dY = 0.0;
            }
            dY -= BOOST;
            if(dY < -MAXBOOST){
                dY = -MAXBOOST;
            }
            [self sendTap];
            break;
        case DEAD:
        case DEAD_REMOTE:
            if((CACurrentMediaTime() - timeOfDeath) > 1.0){
                gameState = PLAY_OR_WATCH;
                [self showStateStuff];
                [self setupGameStartWithSeed:12345];
            }
            break;
        case WATCHING:
            break;
    }
    
}

- (void)getHiScore
{
    [TapTelegraphSvc sendGetHiScoreUsingResultHandler:^(HiScoreData *hsd, TapTelegraphGetHiScoreExceptionBundle *exBundle)
                                                     {
                                                         if(exBundle == nil){
                                                             NSLog(@"Hi Score is %ld held by '%@'", (long)[hsd getScore], [hsd getName]);
                                                             viewController.netHiScoreLabel.text = [NSString stringWithFormat:@"%@: %ld", [hsd getName], (long)[hsd getScore]];
                                                         }
                                                         else{
                                                             NSLog(@"Get Hi Score: FAILED '%@'", [exBundle.golgiException getErrText]);
                                                         }
                                                     }
                                 withTransportOptions:stdGto
                                       andDestination:@"SERVER"
                                            withPooky:1];
}

- (void)showStateStuff
{
    BOOL hiScoreHidden = TRUE;
    BOOL scoreHidden = TRUE;
    BOOL playHidden = TRUE;
    BOOL tapToStartHidden = TRUE;
    BOOL gameOverHidden = TRUE;
    BOOL watchHidden = TRUE;
    BOOL nameHidden = TRUE;
    BOOL netHiScoreHidden = TRUE;
    BOOL forkMeHidden = TRUE;
    
    
    switch(gameState){
        case PLAY_OR_WATCH:
            forkMeHidden = FALSE;
            playHidden = FALSE;
            watchHidden = FALSE;
            netHiScoreHidden = FALSE;
            NSLog(@"Sending getHiScore");
            [self getHiScore];
            break;
        case STARTING:
            hiScoreHidden = FALSE;
            scoreHidden = FALSE;
            tapToStartHidden = FALSE;
            break;
        case RUNNING:
            hiScoreHidden = FALSE;
            scoreHidden = FALSE;
            break;
        case WATCHING:
            hiScoreHidden = FALSE;
            scoreHidden = FALSE;
            nameHidden = FALSE;
            break;
        case DEAD_REMOTE:
            nameHidden = FALSE;
            //
            // Intentional comment out of break
            //
            // break;
        case DEAD:
            hiScoreHidden = FALSE;
            scoreHidden = FALSE;
            gameOverHidden = FALSE;
            break;
    }
    
    
    viewController.hiScoreLabel.hidden = hiScoreHidden;
    viewController.scoreLabel.hidden = scoreHidden;
    viewController.playButton.hidden = playHidden;
    viewController.tapToStartLabel.hidden = tapToStartHidden;
    viewController.gameOverLabel.hidden = gameOverHidden;
    viewController.watchButton.hidden = watchHidden;
    viewController.nameLabel.hidden = nameHidden;
    viewController.netHiScoreLabel.hidden = netHiScoreHidden;
    viewController.forkMeImageView.hidden = forkMeHidden;
    viewController.forkMeTouchView.hidden = forkMeHidden;
}


- (void)playPressed
{
    NSLog(@"Play Pressed");
    dataEnabled = [self getDataEnabled];
    gameState = STARTING;
    [self setupGameStartWithSeed:12345];
    [self showStateStuff];
}

- (void)watchPressed
{
    NSLog(@"Watch Pressed");
    
    dataEnabled = [self getDataEnabled];
    
    gameState = WATCHING;
    viewController.nameLabel.text = @"Waiting...";
    
    tdQueue = [[NSMutableArray alloc] init];
    gameOverData = nil;
    screenXglobalMax = 0;
    screenXglobal = 0;

    
    [self showStateStuff];
    [self sendStreamGame];
}
- (void)handleGesture:(UIGestureRecognizer *) recognizer {
    
    CGPoint touchPoint = [recognizer locationOfTouch:0 inView:viewController.forkMeTouchView];
    bool processTouch = CGPathContainsPoint(forkPath, NULL, touchPoint, true);
    
    if(processTouch) {
        // call your method to process the touch
        NSLog(@"FORK: YES");
        [[UIApplication sharedApplication] openURL:[NSURL URLWithString:@"https://github.com/GolgiDevs/GolgiBird"]];
    }
    else{
        NSLog(@"FORK: NO");
    }
}

- (void)addStartGameHandler
{
    [TapTelegraphSvc registerStartGameRequestHandler:^(id<TapTelegraphStartGameResultSender> resultSender, PlayerInfo *playerInfo) {
        NSLog(@"Start game arrived for game played by: '%@' with ID: '%@'", [playerInfo getName], [playerInfo getGameId]);
        
        [self streamGameArrived:playerInfo];
        [resultSender success];
    }];
}

- (void)addSendTapHandler
{
    [TapTelegraphSvc registerSendTapRequestHandler:^(id<TapTelegraphSendTapResultSender> resultSender, TapData *tapData) {
        NSLog(@"Send Tap arrived for game: '%@'", [tapData getGameId]);
        [self sendTapArrived:tapData];
        [resultSender success];
    }];
}

- (void)addGameOverHandler
{
    [TapTelegraphSvc registerGameOverRequestHandler:^(id<TapTelegraphGameOverResultSender> resultSender, GameOverData *god) {
        NSLog(@"Game Over arrived for game: '%@'", [GameData getInstanceId]);
        [self gameOverArrived:god];
        [resultSender success];
    }];
    
}

-(void)addNewHiScoreHandler
{
    [TapTelegraphSvc registerNewHiScoreRequestHandler:^(id<TapTelegraphNewHiScoreResultSender> resultSender, HiScoreData *hiScoreData) {
        NSLog(@"Zoikes, a new hi score by '%@' %ld", [hiScoreData getName], (long)[hiScoreData getScore]);
        UILocalNotification* localNotification = [[UILocalNotification alloc] init];
        localNotification.alertBody = [NSString stringWithFormat:@"'%@' new Hi-Score: %ld",[hiScoreData getName], (long)[hiScoreData getScore]];
        [[UIApplication sharedApplication] cancelAllLocalNotifications];
        [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
        
        [resultSender success];
    }];
    
}

- (void)addNewPBHandler
{
    [TapTelegraphSvc registerNewPBRequestHandler:^(id<TapTelegraphNewPBResultSender> resultSender, HiScoreData *hiScoreData) {
        NSLog(@"Zoikes, a new pb by '%@' %ld", [hiScoreData getName], (long)[hiScoreData getScore]);
        
        UILocalNotification* localNotification = [[UILocalNotification alloc] init];
        localNotification.alertBody = [NSString stringWithFormat:@"'%@' new Personal Best: %ld",[hiScoreData getName], (long)[hiScoreData getScore]];
        [[UIApplication sharedApplication] cancelAllLocalNotifications];
        [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
        
        [resultSender success];
    }];
    
}


- (Controller *)initWithViewController:(ViewController *)_viewController
{
    self = [self init];
    viewController = _viewController;
    playfield = viewController.playfield;
    
    viewController.netHiScoreLabel.text = @"";
    
    [self addStartGameHandler];
    [self addSendTapHandler];
    [self addGameOverHandler];
    [self addNewHiScoreHandler];
    [self addNewPBHandler];
    
    stdGto = [[GolgiTransportOptions alloc] init];
    [stdGto setValidityPeriodInSeconds:60];
    
    xScale = playfield.frame.size.width / SCREEN_WIDTH_REF;
    yScale = playfield.frame.size.height / SCREEN_HEIGHT_REF;
    gameState = PLAY_OR_WATCH;
    [self showStateStuff];
    playerXglobal = -PLAYER_WIDTH_REF;
    playerYglobal = 0;
    playerX = (playerXglobal * xScale);
    playerY = (playerYglobal * yScale);
    score = 0;
    hiScore = [GameData getHiScore];
    personalBest = hiScore;
    tapIndex = 1;
    
    forkPath = CGPathCreateMutable();
    CGPathMoveToPoint(forkPath, NULL, 0.0, 0.0);
    CGPathAddLineToPoint(forkPath, NULL, viewController.forkMeTouchView.frame.size.width, 0.0);
    CGPathAddLineToPoint(forkPath, NULL, 0.0, viewController.forkMeTouchView.frame.size.height);
    CGPathCloseSubpath(forkPath);

    UITapGestureRecognizer *tapRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleGesture:)];
    // [tapRecognizer setNumberOfTapsRequired:2]; // default is 1
    
    [viewController.forkMeTouchView addGestureRecognizer:tapRecognizer];

    renderer = [[Renderer alloc] initWithController:self];
    
    

    return self;
}



@end




