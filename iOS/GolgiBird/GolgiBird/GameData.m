//
//  GameData.m
//  Golgi Bird
//
//  Created by Brian Kelly on 20/02/2014.
//  Copyright (c) 2014 Openmind Networks. All rights reserved.
//

#import "GameData.h"

static GameData *instance = nil;

@implementation GameData

+ (GameData *)getInstance
{
    if(instance == nil){
        instance = [[GameData alloc] init];
    }
    
    return instance;
}


+ (NSInteger)getHiScore
{
    return [[GameData getInstance] _getHiScore];
}

+ (void)setHiScore:(NSInteger)_hiScore
{
    [[GameData getInstance] _setHiScore:_hiScore];
}

+ (BOOL)getWarningShown
{
    return [[GameData getInstance] _getWarningShown];
}

+ (void)setWarningShown:(BOOL)_warningShown
{
    [[GameData getInstance] _setWarningShown:_warningShown];
}

+ (NSString *)getInstanceId
{
    return [[GameData getInstance] _getInstanceId];
}

+ (void)setInstanceId:(NSString *)_instanceId
{
    [[GameData getInstance] _setInstanceId:_instanceId];
}


/*********************************************************************/

- (NSString *)_getInstanceId
{
    return instanceId;
}

- (void)_setInstanceId:(NSString *)_instanceId
{
    instanceId = _instanceId;
    [self save];
}


- (NSInteger)_getHiScore
{
    return hiScore;
}

- (void)_setHiScore:(NSInteger)_hiScore
{
    hiScore = _hiScore;
    [self save];
    
}

- (BOOL)_getWarningShown
{
    return warningShown;
}

- (void)_setWarningShown:(BOOL)_warningShown
{
    warningShown = _warningShown;
    [self save];
}

- (void)save
{
    NSString *error = nil;
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *plistPath = [rootPath stringByAppendingPathComponent:@"GameData.plist"];
    
    NSDictionary *plistDict = [NSDictionary dictionaryWithObjects:
                               [NSArray arrayWithObjects:
                                [NSString stringWithString:instanceId],
                                [NSNumber numberWithInteger:hiScore],
                                [NSNumber numberWithBool:warningShown],
                                nil]
                               
                                forKeys:[NSArray arrayWithObjects:
                                         @"instanceId",
                                         @"hiScore",
                                         @"warningShown",
                                         nil]
                               ];
    
    NSData *plistData = [NSPropertyListSerialization dataFromPropertyList:plistDict
                                                                   format:NSPropertyListXMLFormat_v1_0
                                                         errorDescription:&error];
    if(plistData) {
        [plistData writeToFile:plistPath atomically:YES];
    }
    else {
        NSLog(@"Error Writing GameData: %@", error);
    }
}

- (GameData *)init
{
    self = [super init];
    
    warningShown = FALSE;
    hiScore = 0;
    instanceId = @"";
    
    NSNumber *num;
    NSString *str;
    
    NSString *errorDesc = nil;
    NSPropertyListFormat format;
    NSString *plistPath;
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory,
                                                              NSUserDomainMask, YES) objectAtIndex:0];
    plistPath = [rootPath stringByAppendingPathComponent:@"GameData.plist"];
    if (![[NSFileManager defaultManager] fileExistsAtPath:plistPath]) {
        plistPath = [[NSBundle mainBundle] pathForResource:@"GameData" ofType:@"plist"];
    }
    NSData *plistXML = [[NSFileManager defaultManager] contentsAtPath:plistPath];
    NSDictionary *temp = (NSDictionary *)[NSPropertyListSerialization
                                          propertyListFromData:plistXML
                                          mutabilityOption:NSPropertyListMutableContainersAndLeaves
                                          format:&format
                                          errorDescription:&errorDesc];
    if (!temp) {
        NSLog(@"Error reading plist: %@, format: %d", errorDesc, (int)format);
    }
    else {
        if((num = [temp objectForKey:@"hiScore"]) != nil){
            hiScore = [num integerValue];
        }
        if((str = [temp objectForKey:@"instanceId"]) != nil){
            instanceId = str;
        }
        if((num = [temp objectForKey:@"warningShown"]) != nil){
            if([num integerValue] != 0){
                warningShown = TRUE;
            }
            else{
                warningShown = FALSE;
            }
        }
    }
    
    NSLog(@"     HI SCORE: %ld", (long)hiScore);
    NSLog(@"  Instance Id: '%@'", instanceId);
    NSLog(@"Warning Shown: %d", warningShown);
    
    return self;
}




@end
