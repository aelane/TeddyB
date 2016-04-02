#ifndef _LESSONS_H_INCLUDED 
#define _LESSONS_H_INCLUDED 

#include "aws_iot_mqtt_interface.h"


int english_to_other(int s, char* language, MQTTPublishParams MetricsParams);
int repeat_after_me_english(int s, MQTTPublishParams MetricsParams);

#endif