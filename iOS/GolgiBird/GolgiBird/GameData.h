//
//  GameData.h
//  Golgi Bird
//
//  Created by Brian Kelly on 20/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GameData : NSObject
{
    BOOL warningShown;
    NSInteger hiScore;
    NSString *instanceId;
}


+ (BOOL)getWarningShown;
+ (void)setWarningShown:(BOOL)value;

+ (NSInteger)getHiScore;
+ (void)setHiScore:(NSInteger)hiScore;

+ (NSString *)getInstanceId;
+ (void)setInstanceId:(NSString *)instanceId;


@end
