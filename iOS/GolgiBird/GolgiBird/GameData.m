//
//  GameData.m
//  Golgi Bird
//
//  Created by Brian Kelly on 20/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import "GameData.h"
#import "libGolgi.h"

#define HI_SCORE_KEY @"HI-SCORE"
#define WARNING_SHOWN_KEY @"WARNING-SHOWN"
#define INSTANCE_ID_KEY @"INSTANCE-ID"

static GameData *instance = nil;

@implementation GameData

+ (NSInteger)getHiScore
{
    return [GolgiStore getIntegerForKey:HI_SCORE_KEY withDefault:0];
}

+ (void)setHiScore:(NSInteger)_hiScore
{
    [GolgiStore deleteIntegerForKey:HI_SCORE_KEY];
    [GolgiStore putInteger:_hiScore forKey:HI_SCORE_KEY];
}

+ (BOOL)getWarningShown
{
    return ([GolgiStore getIntegerForKey:WARNING_SHOWN_KEY withDefault:0] != 0) ? TRUE : FALSE;
}

+ (void)setWarningShown:(BOOL)_warningShown
{
    [GolgiStore deleteIntegerForKey:WARNING_SHOWN_KEY];
    [GolgiStore putInteger:(_warningShown ? 1 : 0) forKey:WARNING_SHOWN_KEY];
}

+ (NSString *)getInstanceId
{
    return [GolgiStore getStringForKey:INSTANCE_ID_KEY withDefault:@""];
}

+ (void)setInstanceId:(NSString *)_instanceId
{
    [GolgiStore deleteStringForKey:INSTANCE_ID_KEY];
    [GolgiStore putString:_instanceId forKey:INSTANCE_ID_KEY];
}

@end
