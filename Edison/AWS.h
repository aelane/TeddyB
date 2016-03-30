
#ifndef _AWS_H_INCLUDED 
#define _AWS_H_INCLUDED 

#include "aws_iot_log.h"
#include "aws_iot_version.h"
#include "aws_iot_mqtt_interface.h"
#include "aws_iot_config.h"

int MQTTcallbackHandler(MQTTCallbackParams params);

void disconnectCallbackHandler(void);


#endif