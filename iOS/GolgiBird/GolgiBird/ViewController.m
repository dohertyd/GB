//
//  ViewController.m
//  Golgi Bird
//
//  Created by Brian Kelly on 17/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import "ViewController.h"
#import "GolgiStuff.h"

@interface ViewController ()

@end

@implementation ViewController
@synthesize controller;
@synthesize playfield;
@synthesize bgImageView;
@synthesize forkMeTouchView;
@synthesize forkMeImageView;

- (IBAction)touchDown:(id)sender
{
    [controller touchDown];
}

- (IBAction)playPressed:(id)sender
{
    [controller playPressed];
}

- (IBAction)watchPressed:(id)sender
{
    [controller watchPressed];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    if(golgiStuff == nil){
        golgiStuff = [[GolgiStuff alloc] initWithViewController:self];
    }
    NSLog(@"       My Size: %fx%f", self.view.frame.size.width, self.view.frame.size.height);
    NSLog(@"Playfield Size: %fx%f", playfield.frame.size.width, playfield.frame.size.height);
    if(playfield.frame.size.height != self.view.frame.size.height){
        CGRect r = CGRectMake(playfield.frame.origin.x, playfield.frame.origin.y, playfield.frame.size.width, self.view.frame.size.height);
        playfield.frame = r;
        bgImageView.frame = r;
    }
    controller = [[Controller alloc] initWithViewController:self];
    [controller layoutComplete];
}

- (void)viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
    [controller layoutDiscarded];
}


- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
