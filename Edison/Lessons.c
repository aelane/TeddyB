#include <stdio.h>
#include <errno.h>
#include <ctype.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <getopt.h>
#include <signal.h>
#include <sys/param.h>
#include <sys/ioctl.h>
#include <sys/socket.h>

#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>
#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>
#include <bluetooth/rfcomm.h>

#include "Lessons.h"
#include "bear_info.h"

char threadMessage[16];

int english_to_other(int s, char* language, char* topic, MQTTPublishParams MetricsParams, mraa_gpio_context BearButton){

	int continue_flag = 0,changeTopic_flag = 1, sphinx_count = 0, attempts;
	int status = 1;
	int new_reading, old_reading;
	FILE *fp_Topic, *fp_Trans, *fp_Status, *fp_Sphinx;
	char message_Buffer[128], line[16], line_trans[16], status_chk[16] = "", sphinx_arr[64];
	
	MQTTMessageParams Msg = MQTTMessageParamsDefault;
	Msg.qos = QOS_0;
	char cPayload[200];
	Msg.pPayload = (void *) cPayload;

	fp_Topic = fopen("/Curriculum/TopicData.txt", "r");
	fp_Trans = fopen("/Curriculum/TopicDataTrans.txt", "r");
	
	printf("Opening Topic Data\n");
	if (fp_Topic == NULL){	
		fprintf(stderr, "Unable to open Topic file\n");
		return -1;
	}
	
SKIP:
	while(fgets(line, 16, fp_Topic)!=NULL && fgets(line_trans, 16, fp_Trans)!=NULL){
		attempts = 0;
		printf("Getting new word\n");
		line[strlen(line)-1] = 0;
		line_trans[strlen(line_trans)-1] = 0;
		printf("New Word: %s\n", line);
		printf("New Translation: %s\n", line_trans);
		if(0){
MENU:				
			while(strcmp(threadMessage, "learning")){
				printf("Waiting for learning, got: %s\n", threadMessage);
				sleep(1);
			}
		}
		//repeat_underscore_me
		printf("Language setting is: %s\n", language);
		if(strcmp(line, "English")){
			printf ("Read %s\n", line);
			
			if(!strcmp(language, "Spanish")){
				sprintf(message_Buffer,"%s,%s,in_spanish_is,%s,repeat_after_me,%s",line,line,line_trans,line_trans);
			}
			else if(!strcmp(language, "French")){
				sprintf(message_Buffer,"%s,%s,in_french_is,%s,repeat_after_me,%s",line,line,line_trans,line_trans);
			}
			else if(!strcmp(language, "Farsi")){
				sprintf(message_Buffer,"%s,%s,in_farsi_is,%s,repeat_after_me,%s",line,line,line_trans,line_trans);
			}
			else if(!strcmp(language, "Greek")){
				sprintf(message_Buffer,"%s,%s,in_greek_is,%s,repeat_after_me,%s",line,line,line_trans,line_trans);
			}
			
			
			printf("The Buffer is:%s\n",message_Buffer);
			status = write(s,message_Buffer,sizeof(message_Buffer));
			printf ("Wrote %d bytes\n", status);
			sleep(7);
		
			if(status > 0){
				/////////////////Wait for sphinx to do its thing////////////////////////
WRONG:
				do{
				continue_flag = 0;
				if(!strcmp(threadMessage, "skip")){
					memset(threadMessage, 0, sizeof(threadMessage));
					goto SKIP;
				}
				else if(!strcmp(threadMessage, "menu")){
					memset(threadMessage, 0, sizeof(threadMessage));
					goto MENU;
				}
				
				new_reading = mraa_gpio_read(BearButton);
				//Print the pin value
				printf("switch %d \n", new_reading);
				//Probably would want to check the current and previous button reading
				if (new_reading == 1 && old_reading == 0) {
					printf("start recording\n");
				}
				else if (new_reading == 1 && old_reading == 1) {
					printf("Continue recording\n");
				}
				if (new_reading == 0 && old_reading == 0) {
					printf("Keep waiting for a button press\n");
				}
				if (new_reading == 1 && old_reading == 0) {
					printf("start recording\n");
				}
				if (new_reading == 0 && old_reading == 1) {
					printf("Stop the recording\n");
					continue_flag = 1;
				}
				//Sleep for n microseconds, so then, we're reading each second
				usleep(1000 * 1000);
				old_reading = new_reading;
				
				
				/*
				//fp_Status
				fp_Status = fopen("/Curriculum/Bluetooth/Status.txt", "r");
				if (fp_Status == NULL){	
					fprintf(stderr, "Unable to open Status file\n");
					return -1;
				}
				fgets (status_chk, 16, fp_Status);
				//line[strlen(line)-1] = 0;
				printf("Waiting for Continue and got: %s\n",status_chk);
				if(!strcmp(status_chk, "Continue")){
					printf("Ready to Continue\n");
					//status_chk = NULL;
					continue_flag = 1;
					fclose(fp_Status);
					fp_Status = fopen("/Curriculum/Bluetooth/Status.txt", "w");
					fprintf(fp_Status, "Blank");
					fclose(fp_Status);
				}
				else{
					fclose(fp_Status);
					sleep(5);
				}
				*/
				}while(continue_flag != 1);
				////////////////////////////////////////////////////////////////////////
				
				/////////////////////////Read in what sphinx has done///////////////////
				
				sphinx_count = 0;
				
				fp_Sphinx = fopen("/sphinx/response.txt","r");
				if (fp_Sphinx == NULL){	
				
					fprintf(stderr, "Unable to open sphinx response file\n");
					return -1;
				}
				fgets (sphinx_arr, 64, fp_Sphinx);
				
				attempts++;
				printf("Checking against: %s\n", sphinx_arr);
				
				
				if(!strcmp(sphinx_arr, line_trans)){
					status = write(s,"check,good_job",128);
					sprintf(cPayload, "{\"BearID\":\"%s\",\"CorrectWord\":\"%s\",\"WordSaid\":\"%s\",\"TeachingMode\":\"English to Foreign Translations\", \"Language\":\"%s\", \"Topic\":\"%s\", \"Correct\":\"true\"}",BEARID, line_trans, sphinx_arr,language,topic);
					Msg.PayloadLen = strlen(cPayload) + 1;
					MetricsParams.MessageParams = Msg;
					aws_iot_mqtt_publish(&MetricsParams);
				}
				else if(attempts < 4){
					sprintf(message_Buffer,"xmark,try_again,%s",line_trans);
					status = write(s,message_Buffer,sizeof(message_Buffer));
					fclose(fp_Sphinx);
					
					sprintf(cPayload, "{\"BearID\":\"%s\",\"CorrectWord\":\"%s\",\"WordSaid\":\"%s\",\"TeachingMode\":\"English to Foreign Translations\", \"Language\":\"%s\", \"Topic\":\"%s\", \"Correct\":\"false\"}",BEARID, line_trans, sphinx_arr,language,topic);
					Msg.PayloadLen = strlen(cPayload) + 1;
					MetricsParams.MessageParams = Msg;
					aws_iot_mqtt_publish(&MetricsParams);
					goto WRONG;
				}
				else{
					attempts = 0;
				}
				
				fclose(fp_Sphinx);
				sleep(3);
				/////////////////////////////////////////////////////////////////////////
				
			}
		}
	}
	printf("About to exit teaching");
	fclose(fp_Topic);
	return status;
}

int repeat_after_me_english(int s,char* topic, MQTTPublishParams MetricsParams, mraa_gpio_context BearButton){

	int continue_flag = 0,changeTopic_flag = 1, sphinx_count = 0, attempts;
	int status = 1;
	int new_reading, old_reading;
	FILE *fp_Topic, *fp_Status, *fp_Sphinx;
	char message_Buffer[128], line[16], status_chk[16] = "", sphinx_arr[64];
	
	MQTTMessageParams Msg = MQTTMessageParamsDefault;
	Msg.qos = QOS_0;
	char cPayload[200];
	Msg.pPayload = (void *) cPayload;

	
	fp_Topic = fopen("/Curriculum/TopicData.txt", "r");
	printf("Opening Topic Data\n");
	if (fp_Topic == NULL){	
		fprintf(stderr, "Unable to open Topic file\n");
		return -1;
	}
	//Format is: img, sound, sound based on mode, sound
	
SKIP:
	while(fgets (line, 16, fp_Topic)!=NULL){
		attempts = 0;
		printf("Getting new word\n");
		line[strlen(line)-1] = 0;
		printf("New Word: %s\n", line);
		
		if(0){
MENU:				
			while(strcmp(threadMessage, "learning")){
				printf("Waiting for learning, got: %s\n", threadMessage);
				sleep(1);
			}
		}
		//repeat_underscore_me
		if(strcmp(line, "English")){
			sprintf(message_Buffer,"%s,%s,in_english_is,%s,repeat_after_me,%s",line,line,line,line);
			printf("Message sent was: %s\n",message_Buffer);
			status = write(s,message_Buffer,sizeof(message_Buffer));
			printf ("Wrote %d bytes\n", status);
			sleep(7);
		
			if(status > 0){
				/////////////////Wait for sphinx to do its thing////////////////////////
WRONG:
				do{
				continue_flag = 0;
				if(!strcmp(threadMessage, "skip")){
					memset(threadMessage, 0, sizeof(threadMessage));
					goto SKIP;
				}
				else if(!strcmp(threadMessage, "menu")){
					memset(threadMessage, 0, sizeof(threadMessage));
					goto MENU;
				}
	
				continue_flag = 0;
				
				//Read the pin
				new_reading = mraa_gpio_read(BearButton);
				//Print the pin value
				printf("switch %d \n", new_reading);
				//Probably would want to check the current and previous button reading
				if (new_reading == 1 && old_reading == 0) {
					printf("start recording\n");
				}
				else if (new_reading == 1 && old_reading == 1) {
					printf("Continue recording\n");
				}
				if (new_reading == 0 && old_reading == 0) {
					printf("Keep waiting for a button press\n");
				}
				if (new_reading == 1 && old_reading == 0) {
					printf("start recording\n");
				}
				if (new_reading == 0 && old_reading == 1) {
					printf("Stop the recording\n");
					continue_flag = 1;
				}
				//Sleep for n microseconds, so then, we're reading each second
				usleep(1000 * 1000);
				old_reading = new_reading;
				/*
				//fp_Status
				fp_Status = fopen("/Curriculum/Bluetooth/Status.txt", "r");
				if (fp_Status == NULL){	
					fprintf(stderr, "Unable to open Status file\n");
					return -1;
				}
				fgets (status_chk, 16, fp_Status);
				//line[strlen(line)-1] = 0;
				printf("Waiting for Continue and got: %s\n",status_chk);
				
				if(!strcmp(status_chk, "Continue")){
					printf("Ready to Continue\n");
					//status_chk = NULL;
					continue_flag = 1;
					fclose(fp_Status);
					fp_Status = fopen("/Curriculum/Bluetooth/Status.txt", "w");
					fprintf(fp_Status, "Blank");
					fclose(fp_Status);
				}
				else{
					fclose(fp_Status);
					sleep(5);
				}
				*/
				}while(continue_flag != 1);
				////////////////////////////////////////////////////////////////////////
				
				/////////////////////////Read in what sphinx has done///////////////////
				
				sphinx_count = 0;
				
				fp_Sphinx = fopen("/sphinx/response.txt","r");
				if (fp_Sphinx == NULL){	
				
					fprintf(stderr, "Unable to open sphinx response file\n");
					return -1;
				}
				fgets (sphinx_arr, 64, fp_Sphinx);
				
				attempts++;
				printf("Checking against: %s\n", sphinx_arr);
				
				
				if(!strcmp(sphinx_arr, line)){
					printf("WHY HAST THOU FORSAKEN ME");
					status = write(s,"check,good_job",128);
					printf("WHEN IT IS RIGHT");
					sprintf(cPayload, "{\"BearID\":\"%s\",\"CorrectWord\":\"%s\",\"WordSaid\":\"%s\",\"TeachingMode\":\"Repeat After Me\", \"Language\":\"%s\", \"Topic\":\"%s\", \"Correct\":\"true\"}",BEARID, line, sphinx_arr, topic);
					Msg.PayloadLen = strlen(cPayload) + 1;
					MetricsParams.MessageParams = Msg;
					aws_iot_mqtt_publish(&MetricsParams);
				}
				else if(attempts < 4){
					sprintf(message_Buffer,"xmark,try_again,%s",line);
					printf("WHY HAST THOU FORSAKEN ME");
					status = write(s,message_Buffer,sizeof(message_Buffer));
					fclose(fp_Sphinx);
					printf("MHMMMMMMM");
					sprintf(cPayload, "{\"BearID\":\"%s\",\"CorrectWord\":\"%s\",\"WordSaid\":\"%s\",\"TeachingMode\":\"Repeat After Me\", \"Language\":\"English\", \"Topic\":\"%s\", \"Correct\":\"false\"}",BEARID, line, sphinx_arr, topic);
					Msg.PayloadLen = strlen(cPayload) + 1;
					MetricsParams.MessageParams = Msg;
					aws_iot_mqtt_publish(&MetricsParams);
					printf("HMMMMMMM");
					goto WRONG;
				}
				else{
					attempts = 0;
				}
				
				fclose(fp_Sphinx);
				sleep(3);
				/////////////////////////////////////////////////////////////////////////
				
			}
		}
		
	}
	
	printf("About to exit teaching");
	fclose(fp_Topic);
	return status;
}