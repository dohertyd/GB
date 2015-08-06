//
//  GolgiStuff.h
//  Golgi Bird
//
//  Created by Brian Kelly on 19/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "TapTelegraphSvcWrapper.h"
#import "ViewController.h"


@interface GolgiStuff : NSObject
{
    ViewController *viewController;
    NSString *ourId;
    NSString *pushId;
}
- (NSString *)pushTokenToString:(NSData *)token;
- (void)setPushId:(NSString *)_pushId;
- (GolgiStuff *)initWithViewController:(ViewController *)viewController;

@property (strong, nonatomic) GolgiIosTransport * git;
@end
