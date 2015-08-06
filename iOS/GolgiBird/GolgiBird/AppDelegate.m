//
//  AppDelegate.m
//  Golgi Bird
//
//  Created by Brian Kelly on 17/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import "AppDelegate.h"
#import "TapTelegraphSvcWrapper.h"
#import "GameData.h"
#import "GOLGIBIRD_KEYS.h"

@implementation AppDelegate

- (void)application:(UIApplication*)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)deviceToken
{
    NSLog(@"My token is: %@", deviceToken);
#ifdef DEBUG
    //[_git setDevPushToken:deviceToken];
    [_git setPushToken:deviceToken andUseDevPush:YES];

#else
    //[Golgi setProdPushToken:deviceToken];
    [_git setPushToken:deviceToken andUseDevPush:NO];
#endif
}

- (void)application:(UIApplication*)application didReceiveRemoteNotification:(NSDictionary*)userInfo  fetchCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandler
{
    
    if([_git isGolgiPushData:userInfo])
    {
	// NSLog(@"Golgi Received notification(1): %@", userInfo);
	[_git pushReceived:userInfo withCompletionHandler:completionHandler];
    }
    else{
        //
        // Not a PUSH for Golgi, handle as normal in the application
        //
        NSLog(@"Application received a Remote Notification not for Golgi");
        completionHandler(UIBackgroundFetchResultNoData);
    }
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo
{
    [self application:application didReceiveRemoteNotification:userInfo fetchCompletionHandler:nil];
}

- (void)alertView:(UIAlertView *)alertView clickedButtonAtIndex:(NSInteger)buttonIndex
{
    NSLog(@"Button Pressed: %ld", (long)buttonIndex);
    if(alertView == dataSharingAlert){
        NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
        if(buttonIndex == 0){
            //
            // The user pressed OK
            //
            [defaults setObject:@"YES" forKey:@"broadcastGames"];
        }
        else{
            //
            // The user opted out of sharing
            //
            [defaults setObject:@"NO" forKey:@"broadcastGames"];
        }
        [defaults synchronize];
        [GameData setWarningShown:TRUE];
        dataSharingAlert = nil;
    }
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
    BOOL launchInBg = false;
    
    // Override point for customization after application launch.
    if(golgiStuff == nil){
        golgiStuff = [[GolgiStuff alloc] initWithViewController:nil];
    }
    
    
    
    _git = [[GolgiIosTransport alloc] initWithDevId:GOLGIBIRD_DEV_KEY andAppId:GOLGIBIRD_APP_KEY];
    
    [Golgi setSBI:[_git getSBI]];
    [_git setNBI:[Golgi getNBI]];
    
    [_git start];
    
    if(launchOptions != nil) {
        // Launched from push notification
        NSDictionary *d = [launchOptions objectForKey:UIApplicationLaunchOptionsRemoteNotificationKey];
        if(d != nil){
            //
            // Ok, launched into the background, setup Golgi
            //
            
            /*
             UILocalNotification* localNotification = [[UILocalNotification alloc] init];
             localNotification.alertBody = @"Launching into BG";
             [[UIApplication sharedApplication] cancelAllLocalNotifications];
             [[UIApplication sharedApplication] scheduleLocalNotification:localNotification];
             */
            
            //[Golgi enteringBackground];
            [_git enteringBackground];
            //[Golgi useEphemeralConnection];
            [_git useEphemeralConnection];
            launchInBg = true;
        }
    }
    
    
    //
    // Lifted from StackOverflow, how to register for push
    // in a backwards compatible way
    //
    if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 8.0){
        [[UIApplication sharedApplication] registerUserNotificationSettings:[UIUserNotificationSettings settingsForTypes:(UIUserNotificationTypeSound | UIUserNotificationTypeAlert | UIUserNotificationTypeBadge) categories:nil]];
        [[UIApplication sharedApplication] registerForRemoteNotifications];
    }
    else{
        [[UIApplication sharedApplication] registerForRemoteNotificationTypes:
         (UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound | UIRemoteNotificationTypeAlert)];
    }

    
    if(!launchInBg && ![GameData getWarningShown]){
        dataSharingAlert = [[UIAlertView alloc] initWithTitle:@"GolgiBird Data Sharing"
                                                      message:@"GolgiBird sends your Screen Name (default is 'Anonymous'), High-Score and Gameplay to our servers for sharing with other players.\n\nThis can be enabled/disabled in the\nSettings App"
                                                     delegate:self
                                            cancelButtonTitle:@"OK"
                                            otherButtonTitles:@"Don't Send", nil];
        
        [dataSharingAlert show];
    }

    return YES;
}
							
- (void)applicationWillResignActive:(UIApplication *)application
{
    // Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
    // Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
    // Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
    // If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.

    //
    // GOLGI: Tell the framework that we are going into the background
    //
    NSLog(@"applicationDidEnterBackground()");
    //[Golgi enteringBackground];
    [_git enteringBackground];
    //[Golgi useEphemeralConnection];
    [_git useEphemeralConnection];
    

}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
    // Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
    // Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
    
    //
    // GOLGI: Tell the framework that we are active again
    //
    
    NSLog(@"applicationDidBecomeActive()");
    //[Golgi enteringForeground];
    [_git enteringForeground];
    //[Golgi usePersistentConnection];
    [_git usePersistentConnection];
}

- (void)applicationWillTerminate:(UIApplication *)application
{
    // Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

@end
