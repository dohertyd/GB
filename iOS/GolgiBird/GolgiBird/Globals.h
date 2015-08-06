//
//  Globals.h
//  GolgiBird
//
//  Created by Derek Doherty on 05/08/2015.
//  Copyright (c) 2015 Openmind Networks. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Globals : NSObject

{
    NSString * pushToken;
}


+ (NSString *)getPT;
+ (void)setPT:(NSString *)pushToken;

@end
