//
//  Random.h
//  Golgi Bird
//
//  Created by Brian Kelly on 19/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Random : NSObject

+ (void)setSeed:(long long)seed;
+ (long long)next;
+ (NSInteger)nextWithMax:(NSInteger) max;
+ (NSString *)genRandomStringWithLength:(NSInteger)length;

@end
