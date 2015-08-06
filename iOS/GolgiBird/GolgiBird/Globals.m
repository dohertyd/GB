//
//  Globals.m
//  GolgiBird
//
//  Created by Derek Doherty on 05/08/2015.
//  Copyright (c) 2015 Openmind Networks. All rights reserved.
//

#import "Globals.h"

@implementation Globals

+ (Globals *)sharedInstance
{
    static Globals *myInstance = nil;
    // Self in a static function refers to the the Class rather than any instance
    if (nil == myInstance)
    {
        myInstance  = [[[self class] alloc] init];
    }
    return myInstance;
}


-(NSString *)_getPT
{
    return pushToken;
}
+(NSString *)getPT
{
    return [[Globals sharedInstance] _getPT];
}



-(void)_setPT:(NSString *)_pushToken
{
    pushToken = _pushToken;
}
+ (void)setPT:(NSString *)_pushToken
{
    return [[Globals sharedInstance] _setPT:_pushToken];
}

@end
