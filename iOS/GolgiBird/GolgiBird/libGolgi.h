/* IS_AUTOGENERATED_SO_ALLOW_AUTODELETE=YES */
/* The previous line is to allow auto deletion */
#ifndef LIB_GOLGI_H
#define LIB_GOLGI_H
#import <Foundation/Foundation.h>
#import <Foundation/NSString.h>
#import <Foundation/NSDictionary.h>
//
//  CSL.h
//
//  Copyright 2010 Openmind Networks. All rights reserved.
//


@interface CSL : NSObject {
    
}
+ (char *)eatWhite:(const char *)c;
+ (char *)eatBlack:(const char *)c;
+ (char *)nextWord:(const char *)c;
+ (char *)strDup:(const char *)c;
+ (char *)substrDup:(const char *)bos endOfString:(const char *)eos;
+ (int) strToInt:(const char *)str;
+ (double) strToDouble:(const char *)str;
+ (NSString *)doubleToStr:(double)value;
+ (NSString *)NTLEscapeString:(NSString *)str;
+ (NSString *)NTLDeEscapeString:(NSString *)str;
+ (NSArray *)breakupLlistPayload:(NSString *)payload;
+ (NSArray *)breakupMultiPayload:(NSString *)payload;

@end
/* IS_AUTOGENERATED_SO_ALLOW_AUTODELETE=YES */
/* The previous line is to allow auto deletion */
#ifndef __GOLGI_GEN_H_DGEN__
#define __GOLGI_GEN_H_DGEN__
    enum golgi_device_type {
    GOLGI_ANDROID_DEVICE = 1,
    GOLGI_IOS_PRODUCTION_DEVICE = 2,
    GOLGI_IOS_DEVELOPMENT_DEVICE = 3,
    GOLGI_3RD_PARTY_SERVER = 0
    };
    enum golgi_message_type {
    GOLGI_MSGTYPE_REQ = 0,
    GOLGI_MSGTYPE_RSP = 1,
    GOLGI_MSGTYPE_ERR = 2
    };
#define GOLGI_SCHEDULED_OPTION "SCHEDULED"
#define GOLGI_EXPIRY_OPTION "EXPIRY"
#define GOLGI_PRIORITY_OPTION "PRIORITY"
#define GOLGI_ANDROID_PROJECT_ID_OPTION "ANDROID_PROJECT_ID"
#define GOLGI_PUBLIC_KEY_OPTION "PUBLIC_KEY"
#define GOLGI_PUBLIC_KEY_VERSION_OPTION "PUBLIC_KEY_VERSION"
#define GOLGI_HOME_CLUSTER_HOSTNAME_OPTION "HOME_CLUSTER"
#define GOLGI_PUSH_ORIG_REGISTER_OPTION "PUSH_ORIG_REGISTER"
#define GOLGI_ERROR_VALUE_OPTION "ERROR_VALUE"
    enum golgi_error_type {
    GOLGI_ERRTYPE_EXPIRED = 1001,
    GOLGI_ERRTYPE_PAYLOAD_MISMATCH = 1002,
    GOLGI_ERRTYPE_INVALID_GOLGI_KEY_PAIR = 2001,
    GOLGI_ERRTYPE_UNKNOWN_HOME_CLUSTER = 2002,
    GOLGI_ERRTYPE_UNKNOWN_DESTINATION_ADDRESS = 2003,
    GOLGI_ERRTYPE_NO_PUBLIC_KEY = 2004,
    GOLGI_ERRTYPE_PUBLIC_KEY_MISMATCH = 2005,
    GOLGI_ERRTPYE_SUBMISSION_FAILED = 2006
    };
#endif
//
//  B64.h
//  Portico
//
//  Created by Brian Kelly on 12/10/2011.
//  Copyright (c) 2011 Openmind Networks. All rights reserved.
//


@interface GolgiB64 : NSObject
+(NSData *)decodeData: (NSString *)b64Data;
+(NSString *)decodeDataToString: (NSString *)b64Data;
+(double)decodeDataToDouble:(NSString *)b64Data;
+(int)decodeDataToInt: (NSString *)b64Data;
+(NSString *)encodeData: (NSData *)data withNewLines:(BOOL)nl;
+(NSString *)encodeString: (NSString *)str withNewLines:(BOOL)nl;

@end
//
//  GolgiPayload.h
//
//  Copyright (c) 2013 Openmind Networks. All rights reserved.
//


@interface GolgiPayload : NSObject
{
    NSMutableDictionary *fieldHash;
    NSMutableDictionary *nestedHash;
    BOOL verbose;
}

- (GolgiPayload *)getNestedWithTag:(NSString *)tag;
- (NSString *)getStringWithTag:(NSString *)tag;
- (NSString *)getB64DataWithTag:(NSString *)tag;
- (NSNumber *)getIntWithTag:(NSString *)tag;
- (NSNumber *)getDoubleWithTag:(NSString *)tag;
+ (GolgiPayload *)payloadWithString:(NSString *)string;
@end
//
//  GolgiAPIMessage.h
//
//  Copyright (c) 2013 Openmind Networks. All rights reserved.
//


@class GolgiTransportOptions;

@interface GolgiAPIMessage : NSObject
{
    NSString *devKey;
    NSString *apiKey;
    NSString *oaAppUserId;
    NSString *daAppUserId;
    NSString *messageId;
    NSInteger msgType;
    NSString *method;
    NSString *errTxt;
    NSInteger errType;
    NSString *payload;
    GolgiTransportOptions *cto;
}

@property (retain) NSString *devKey;
@property (retain) NSString *apiKey;
@property (retain) NSString *oaAppUserId;
@property (retain) NSString *daAppUserId;
@property (retain) NSString *messageId;
@property          NSInteger msgType;
@property (retain) NSString *method;
@property (retain) NSString *errTxt;
@property          NSInteger errType;
@property (retain) NSString *payload;
@property (retain) GolgiTransportOptions *cto;

+ (GolgiAPIMessage *)createFromPayload:(NSString *)payload;

@end
/* IS_AUTOGENERATED_SO_ALLOW_AUTODELETE=YES */
/* The previous line is to allow auto deletion */




@interface GolgiException : NSObject
{
    NSString *errText;
    BOOL errTextIsSet;
    NSInteger errType;
    BOOL errTypeIsSet;
}

@property (readonly) BOOL errTextIsSet;
- (NSString *)getErrText;
- (void)setErrText:(NSString *)errText;
@property (readonly) BOOL errTypeIsSet;
- (NSInteger)getErrType;
- (void)setErrType:(NSInteger )errType;

+ (GolgiException *)deserialiseFromString: (NSString *)string;
+ (GolgiException *)deserialiseFromPayload: (GolgiPayload *)payload;
- (NSString *)serialiseWithPrefix:(NSString *)prefix;
- (NSString *)serialise;
- (id)initWithIsSet:(BOOL)defIsSet;
@end
//
//  Golgi.h
//
//  Copyright (c) 2013 Openmind Networks. All rights reserved.
//



@class GolgiException;

@protocol GolgiAPIImpl
- (void)useDevelopmentCluster:(BOOL)useIt;
- (void)enteringBackground;
- (void)enteringForeground;
#if !defined(SYS_ARCH_LINUX_X86)
- (void)pushReceived:(NSDictionary *)userInfo withCompletionHandler:(void (^)(UIBackgroundFetchResult result))completionHandler;
#endif
- (void)initFailure:(NSString *)errorText;
- (void)sendMsg:(GolgiAPIMessage *)msg;
- (void)useEphemeralConnection;
- (void)usePersistentConnection;
- (void)setPushToken:(NSString *)pushId andUseDevPush:(BOOL)useDevPush;
- (void)registerWithDevId:(NSString *)devId appId:(NSString *)appId instId:(NSString *)instId pushId:(NSString *)pushId andUseDevPush:(BOOL)useDevPush;
@end

@protocol GolgiAPIUser
- (void)golgiRegistrationSuccess;
- (void)golgiRegistrationFailure:(NSString *)errorText;
@end

@protocol GolgiAPIBaseImpl <GolgiAPIUser>
- (void)incomingMsg:(GolgiAPIMessage *)msg;
@end


@protocol GolgiInternalInboundResponseHandler
- (void)processResponsePayload:(NSString *)payload;
- (void)processGolgiException:(GolgiException *)golgiException;
@end

@protocol GolgiInternalInboundRequestHandler
- (void)incomingMsg:(NSString *)payload from:(NSString *)sender withMessageId:(NSString *)msgId;
@end


@interface Golgi : NSObject <GolgiAPIBaseImpl>
{
    NSMutableDictionary *obReqHash;
    NSMutableDictionary *ibReqHash;
    NSString *msgIdBase;
    NSInteger msgIdNext;
    NSString *devId;
    NSString *appId;
    NSString *instId;
    NSString *pushId;
    BOOL useDevPush;
    BOOL useDevCluster;
    BOOL persistentConn;
    id<GolgiAPIUser> apiUser;
    id<GolgiAPIImpl> apiImpl;
}

+ (void)setOption:(NSString *)name withValue:(NSString *)value;

+ (id<GolgiAPIBaseImpl>)setAPIImpl:(id<GolgiAPIImpl>)apiImpl;

+ (void)useEphemeralConnection;

+ (void)usePersistentConnection;

+ (void)setDevPushToken:(NSData *)token;

+ (void)setProdPushToken:(NSData *)token;

+ (void)registerWithDevId:(NSString *)_devId
                    appId:(NSString *)_appId
                   instId:(NSString *)_instId
               andAPIUser:(id<GolgiAPIUser>)_apiUser;

+ (void)sendRequestPayload:(NSString *)payload 
      withTransportOptions:(GolgiTransportOptions *)options
                        to:(NSString *)dst 
                withMethod:(NSString *)method
       andResponseHandler:(id<GolgiInternalInboundResponseHandler>)responseHandler;

+ (void)sendResponsePayload:(NSString *)payload 
                         to:(NSString *)dst 
                  forMethod:(NSString *)method
              withMessageId:(NSString *)messageId;

+ (void)sendRemoteError:(NSString *)errorText
                     to:(NSString *)dst 
              forMethod:(NSString *)method
          withMessageId:(NSString *)method;

+ (void)registerRequestHandler:(id<GolgiInternalInboundRequestHandler>)requestHandler
                     forMethod:(NSString *)method;

+ (void)enteringBackground;
+ (void)enteringForeground;
#if !defined(SYS_ARCH_LINUX_X86)
+ (BOOL)isGolgiPushData:(NSDictionary *)userInfo;
+ (void)pushReceived:(NSDictionary *)userInfo withCompletionHandler:(void (^)(UIBackgroundFetchResult result))completionHandler;
#endif
#if defined(GOLGI_M_SRC)

- (void)_setOption:(NSString *)name withValue:(NSString *)value;
- (void)_enteringBackground;
- (void)_enteringForeground;
#if !defined(SYS_ARCH_LINUX_X86)
- (void)_pushReceived:(NSDictionary *)userInfo withCompletionHandler:(void (^)(UIBackgroundFetchResult))completionHandle;
#endif

- (void)_sendRequestPayload:(NSString *)payload 
       withTransportOptions:(GolgiTransportOptions *)options
			 to:(NSString *)dst 
                 withMethod:(NSString *)method
         andResponseHandler:(id<GolgiInternalInboundResponseHandler>)responseHandler;

- (void)_sendResponsePayload:(NSString *)payload 
                          to:(NSString *)dst 
                   forMethod:(NSString *)method
               withMessageId:(NSString *)messageId;


- (void)_sendRemoteError:(NSString *)errorText
                      to:(NSString *)dst 
               forMethod:(NSString *)method
           withMessageId:(NSString *)msgId;



- (void)_registerRequestHandler:(id<GolgiInternalInboundRequestHandler>)requestHandler
                      forMethod:(NSString *)method;


- (void)_useEphemeralConnection;

- (void)_usePersistentConnection;

- (void)_setPushToken:(NSData *)token andUseDevPush:(BOOL)useDevPush;

- (void)_registerWithDevId:(NSString *)_devId
                     appId:(NSString *)_appId
		    instId:(NSString *)_instId
                andAPIUser:(id<GolgiAPIUser>)_apiUser;

- (id<GolgiAPIBaseImpl>)_setAPIImpl:(id<GolgiAPIImpl>)_apiImpl;

#endif

@end


@interface GolgiTransportOptions : NSObject
{
    int priority; 		// -1/0/1 low/normal/high
    int scheduledOffset;	// Relative in seconds
    int validityPeriod;         // Relative in seconds
}

- (int)_getPriority;
- (int)_getScheduledOffset;
- (int)_getValidityPeriod;

- (void)setLowPriority;
- (void)setNormalPriority;
- (void)setHighPriority;
- (void)setScheduledDeliveryWithOffset:(NSInteger)delayInSeconds;
- (void)clearScheduledDelivery;
- (void)setValidityPeriodInSeconds:(NSInteger)seconds;
- (void)clearValidityPeriod;



@end
#endif
