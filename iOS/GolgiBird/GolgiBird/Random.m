//
//  Random.m
//  Golgi Bird
//
//  Created by Brian Kelly on 19/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import "Random.h"

static long long x = 123456789;
static long long mask = 0;
@implementation Random


+ (void)setSeed:(long long)seed
{
    x = seed;
}
+ (long long)next
{
    long long llt;
    if(mask == 0){
        mask = 0x7fffffffffffffffL;
    }
    x ^= (x << 21);
    llt = (x >> 1) & mask;
    x ^= (llt >> 34);
    x ^= (x << 4);
    return x;
}


+ (NSInteger)nextWithMax:(NSInteger) max
{
    int result = (int)([Random next] % max);
    return (result < 0) ? -result : result;
}

+ (NSString *)genRandomStringWithLength:(NSInteger)length
{
    [Random setSeed:(long long)time(NULL)];
    NSMutableString *str = [[NSMutableString alloc] init];
    for(int i = 0; i < length; i++){
        [str appendFormat:@"%c", (char)('A' + [Random nextWithMax:('z' - 'A')])];
    }
    return [NSString stringWithString:str];
}


@end
