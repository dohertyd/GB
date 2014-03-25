//
//  ViewController.h
//  Golgi Bird
//
//  Created by Brian Kelly on 17/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Playfield.h"
#import "Controller.h"

@class GolgiStuff;

@interface ViewController : UIViewController
{
    GolgiStuff *golgiStuff;
}
@property Controller *controller;
@property IBOutlet Playfield *playfield;
@property IBOutlet UIImageView *bgImageView;
@property IBOutlet UIView *collisionView;
@property IBOutlet UILabel *hiScoreLabel;
@property IBOutlet UILabel *scoreLabel;
@property IBOutlet UILabel *gameOverLabel;
@property IBOutlet UILabel *tapToStartLabel;
@property IBOutlet UIButton *playButton;
@property IBOutlet UIButton *watchButton;
@property IBOutlet UILabel *nameLabel;
@property IBOutlet UILabel *netHiScoreLabel;
@property IBOutlet UIView *forkMeTouchView;
@property IBOutlet UIImageView *forkMeImageView;


- (IBAction)touchDown:(id)sender;
- (IBAction)playPressed:(id)sender;
- (IBAction)watchPressed:(id)sender;


@end
