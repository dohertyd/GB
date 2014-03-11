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
    NSInteger hiScore;
    NSString *instanceId;
}

+ (NSInteger)getHiScore;
+ (void)setHiScore:(NSInteger)hiScore;

+ (NSString *)getInstanceId;
+ (void)setInstanceId:(NSString *)instanceId;


@end
