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
	jsmntok_t tokens[40];
	
	FILE *fp, *fp_trans, *fp_settings;
	const char *js;
	int r, i, trans_flag = 0;
	
	fp = fopen("/Curriculum/TopicData.txt","w");
	fp_trans = fopen("/Curriculum/TopicDataTrans.txt","w");
	fp_settings = fopen("/Curriculum/BearSettings.txt","w");
	
	
	js = params.MessageParams.pPayload;
	r = jsmn_parse(&parser, js, strlen(js), tokens, 256);
	
	for(i = 1; i < 9; i++){
		jsmntok_t key = tokens[i];
		unsigned int length = key.end - key.start;
		char keyString[length + 1];    
		memcpy(keyString, &params.MessageParams.pPayload[key.start], length);
		keyString[length] = '\0';
		printf("SettingsKey: %s\n", keyString);
		if(strcmp(keyString, "Language") && strcmp(keyString, "Setting") && strcmp(keyString, "Topic") && strcmp(keyString, "TopicLen")){
			fprintf(fp_settings, "%s\n", keyString);
		}
		
		
	}
	fclose(fp_settings);
	printf("Closing Settings File\n");
	for(i; i < 40; i++){
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

