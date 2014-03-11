//
//  GolgiStuff.m
//  Golgi Bird
//
//  Created by Brian Kelly on 19/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import "GolgiStuff.h"
#import "TapTelegraphSvcWrapper.h"
#import "GameData.h"
#import "Random.h"

@implementation GolgiStuff

// GOLGI
//********************************* Registration ***************************
//
// Setup handling of inbound SendMessage methods and then Register with Golgi
//
- (void)doGolgiRegistration
{
    //
    // Do this before registration because on registering, there may be messages queued
    // up for us that would arrive and be rejected because there is no handler in place
    //
    
    // [TapTelegraphSvc registerSendMessageRequestReceiver:self];
    
    //
    // and now do the main registration with the service
    //
    NSLog(@"Registering with golgiId: '%@'", ourId);
    // [Golgi setOption:@"USE_DEV_CLUSTER" withValue:@"0"];
    
    [Golgi registerWithDevId:@"2T3R2OYDQPW43WNWAQV7KA2D6WFEK7CU" //--DEVELOPER-KEY-HERE--
                       appId:@"R5E7VCT4E5YIBIGEDX4CE624VODF342Z" //--APPLICATION-KEY-HERE--
                      instId:ourId
                  andAPIUser:self];
}

//
// Registration worked
//

- (void)golgiRegistrationSuccess
{
    NSLog(@"Golgi Registration: PASS");
    
}

//
// Registration failed
//

- (void)golgiRegistrationFailure:(NSString *)errorText
{
    NSLog(@"Golgi Registration: FAIL => '%@'", errorText);
}

- (void)setPushId:(NSString *)_pushId
{
    if([pushId  compare:_pushId] != NSOrderedSame){
        pushId = _pushId;
        [self doGolgiRegistration];
    }
}

- (NSString *)pushTokenToString:(NSData *)token
{
    NSMutableString *hexStr = [[NSMutableString alloc]init];
        
    for(int i = 0; i < token.length; i++){
        [hexStr appendFormat:@"%02x", ((unsigned char *)[token bytes])[i]];
    }
    
    return [NSString stringWithString:hexStr];
}

- (GolgiStuff *)initWithViewController:(ViewController *)_viewController
{
    self = [self init];
    viewController = _viewController;
    
    ourId = [GameData getInstanceId];
    if(ourId.length == 0){
        
        ourId = [Random genRandomStringWithLength:20];
        [GameData setInstanceId:ourId];
    }
    
    NSLog(@"Instance Id: '%@'", ourId);
    
    pushId = @"";
    
    [self doGolgiRegistration];
    
    return self;
}

@end
