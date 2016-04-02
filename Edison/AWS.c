#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <unistd.h>

#include <signal.h>
#include <memory.h>
#include <sys/time.h>
#include <limits.h>

#include "jsmn.h"
#include "AWS.h"

int changeTopic_flag = 1;

int MQTTcallbackHandler(MQTTCallbackParams params) {

	INFO("Subscribe callback");
	INFO("%.*s\t%.*s",
			(int)params.TopicNameLen, params.pTopicName,
			(int)params.MessageParams.PayloadLen, (char*)params.MessageParams.pPayload);
			
	jsmn_parser parser;		
	jsmn_init(&parser);
	jsmntok_t tokens[32];
	
	FILE *fp, *fp_trans, *fp_settings;
	const char *js;
	int r, i, trans_flag = 0;
	
	fp = fopen("/Curriculum/TopicData.txt","w");
	fp_trans = fopen("/Curriculum/TopicDataTrans.txt","w");
	fp_settings = fopen("/Curriculum/BearSettings.txt","w");
	
	
	js = params.MessageParams.pPayload;
	r = jsmn_parse(&parser, js, strlen(js), tokens, 256);
	
	for(i = 1; i < 5; i++){
		jsmntok_t key = tokens[i];
		unsigned int length = key.end - key.start;
		char keyString[length + 1];    
		memcpy(keyString, &params.MessageParams.pPayload[key.start], length);
		keyString[length] = '\0';
		printf("Key: %s\n", keyString);
		if(strcmp(keyString, "Language") && strcmp(keyString, "Setting")){
			fprintf(fp_settings, "%s\n", keyString);
		}
		
		
	}
	fclose(fp_settings);
	
	for(i; i < 32; i++){
		jsmntok_t key = tokens[i];
		unsigned int length = key.end - key.start;
		char keyString[length + 1];    
		memcpy(keyString, &params.MessageParams.pPayload[key.start], length);
		keyString[length] = '\0';
		printf("Key: %s\n", keyString);
		if(!strcmp(keyString, "English") || !strcmp(keyString, "Translation")){
			if(!strcmp(keyString, "Translation")){
				trans_flag = 1;
			}
			i++;
		}	
		if(trans_flag){
			fprintf(fp_trans, "%s\n", keyString);
		}
		else{
			fprintf(fp, "%s\n", keyString);		
		}
		
		if(tokens[i].type == JSMN_PRIMITIVE){
			fclose(fp);
			fclose(fp_trans);
			changeTopic_flag = 0;
			printf("Closing Topic File\n");
			return 0;
		}
		
	}
	fclose(fp);
	fclose(fp_trans);
	printf("Closing Topic File\n");
	return 0;
}

void disconnectCallbackHandler(void) {
	WARN("MQTT Disconnect");
}

//Work in progress?
MQTTConnectParams ConnectParamsInit(){
	
	char HostAddress[255] = AWS_IOT_MQTT_HOST;
	char certDirectory[PATH_MAX + 1] = "/AWS/SDK/certs/";
	uint32_t port = AWS_IOT_MQTT_PORT;
	
	char rootCA[PATH_MAX + 1];
	char clientCRT[PATH_MAX + 1];
	char clientKey[PATH_MAX + 1];
	
	char cafileName[] = AWS_IOT_ROOT_CA_FILENAME;
	char clientCRTName[] = AWS_IOT_CERTIFICATE_FILENAME;
	char clientKeyName[] = AWS_IOT_PRIVATE_KEY_FILENAME;
	
	sprintf(rootCA, "/%s/%s", certDirectory, cafileName);
	sprintf(clientCRT, "/%s/%s", certDirectory, clientCRTName);
	sprintf(clientKey, "/%s/%s", certDirectory, clientKeyName);
	
	MQTTConnectParams connectParams = MQTTConnectParamsDefault;

	connectParams.KeepAliveInterval_sec = 10;
	connectParams.isCleansession = true;
	connectParams.MQTTVersion = MQTT_3_1_1;
	connectParams.pClientID = "CSDK-test-device";
	connectParams.pHostURL = HostAddress;
	connectParams.port = port;
	connectParams.isWillMsgPresent = false;
	connectParams.pRootCALocation = rootCA;
	connectParams.pDeviceCertLocation = clientCRT;
	connectParams.pDevicePrivateKeyLocation = clientKey;
	connectParams.mqttCommandTimeout_ms = 2000;
	connectParams.tlsHandshakeTimeout_ms = 5000;
	connectParams.isSSLHostnameVerify = true;// ensure this is set to true for production
	connectParams.disconnectHandler = disconnectCallbackHandler;
	
	return connectParams;
}