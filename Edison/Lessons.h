#ifndef _LESSONS_H_INCLUDED 
#define _LESSONS_H_INCLUDED 

#include "aws_iot_mqtt_interface.h"
#include "mraa.h"

extern char threadMessage[16];

int english_to_other(int s, char* language,char* topic, MQTTPublishParams MetricsParams, mraa_gpio_context BearButton);
int repeat_after_me_english(int s,char* topic, MQTTPublishParams MetricsParams, mraa_gpio_context BearButton);

#endif