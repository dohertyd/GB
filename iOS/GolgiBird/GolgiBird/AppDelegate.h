//
//  AppDelegate.h
//  Golgi Bird
//
//  Created by Brian Kelly on 17/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TapTelegraphSvcWrapper.h"
#import "GolgiStuff.h"

@interface AppDelegate : UIResponder <UIApplicationDelegate,UIAlertViewDelegate>
{
    UIAlertView *dataSharingAlert;
    GolgiStuff *golgiStuff;
}

@property (strong, nonatomic) UIWindow *window;
@property (strong, nonatomic) GolgiIosTransport * git;
@end
