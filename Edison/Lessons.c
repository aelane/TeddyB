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

int english_to_other(int s, char* language, MQTTPublishParams MetricsParams){

	int continue_flag = 0,changeTopic_flag = 1, sphinx_count = 0, attempts;
	int status = 1;
	FILE *fp_Topic, *fp_Trans, *fp_Status, *fp_Sphinx;
	char message_Buffer[128], line[16], line_trans[16], status_chk[16] = "", sphinx_arr[64];
	
	MQTTMessageParams Msg = MQTTMessageParamsDefault;
	Msg.qos = QOS_0;
	char cPayload[100];
	Msg.pPayload = (void *) cPayload;

	fp_Topic = fopen("/Curriculum/TopicData.txt", "r");
	fp_Trans = fopen("/Curriculum/TopicDataTrans.txt", "r");
	
	printf("Opening Topic Data\n");
	if (fp_Topic == NULL){	
		fprintf(stderr, "Unable to open Topic file\n");
		return -1;
	}
	
	while(fgets(line, 16, fp_Topic)!=NULL && fgets(line_trans, 16, fp_Trans)!=NULL){
		attempts = 0;
		printf("Getting new word\n");
		line[strlen(line)-1] = 0;
		line_trans[strlen(line_trans)-1] = 0;
		printf("New Word: %s\n", line);
		printf("New Translation: %s\n", line_trans);

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
			status = write(s,message_Buffer,128);
			printf ("Wrote %d bytes\n", status);
			sleep(7);
		
			if(status > 0){
				/////////////////Wait for sphinx to do its thing////////////////////////
				WRONG:
				do{
				continue_flag = 0;
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
				
				sprintf(cPayload, "{\"BearID\":\"%s\",\"CorrectWord\":\"%s\",\"WordSaid\":\"%s\",\"TeachingMode\":\"1\"}",BEARID, line_trans, sphinx_arr);
				Msg.PayloadLen = strlen(cPayload) + 1;
				MetricsParams.MessageParams = Msg;
				aws_iot_mqtt_publish(&MetricsParams);
				
				if(!strcmp(sphinx_arr, line_trans)){
					status = write(s,"check,good_job",128);
				}
				else if(attempts < 4){
					sprintf(message_Buffer,"xmark,try_again,%s",line_trans);
					status = write(s,message_Buffer,128);
					fclose(fp_Sphinx);
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

int repeat_after_me_english(int s, MQTTPublishParams MetricsParams){

	int continue_flag = 0,changeTopic_flag = 1, sphinx_count = 0, attempts;
	int status = 1;
	FILE *fp_Topic, *fp_Status, *fp_Sphinx;
	char message_Buffer[128], line[16], status_chk[16] = "", sphinx_arr[64];
	
	MQTTMessageParams Msg = MQTTMessageParamsDefault;
	Msg.qos = QOS_0;
	char cPayload[100];
	Msg.pPayload = (void *) cPayload;

	
	fp_Topic = fopen("/Curriculum/TopicData.txt", "r");
	printf("Opening Topic Data\n");
	if (fp_Topic == NULL){	
		fprintf(stderr, "Unable to open Topic file\n");
		return -1;
	}
	//Format is: img, sound, sound based on mode, sound
	
	
	while(fgets (line, 16, fp_Topic)!=NULL){
		attempts = 0;
		printf("Getting new word\n");
		line[strlen(line)-1] = 0;
		printf("New Word: %s\n", line);
		
		//repeat_underscore_me
		if(strcmp(line, "English")){
			sprintf(message_Buffer,"%s,%s,in_english_is,%s,repeat_after_me,%s",line,line,line,line);
			printf("Message sent was: %s\n",message_Buffer);
			status = write(s,message_Buffer,128);
			printf ("Wrote %d bytes\n", status);
			sleep(7);
		
			if(status > 0){
				/////////////////Wait for sphinx to do its thing////////////////////////
				WRONG:
				do{
				continue_flag = 0;
				//fp_Status
				fp_Status = fopen("/Curriculum/Bluetooth/Status.txt", "r");
				if (fp_Status == NULL){	
					fprintf(stderr, "Unable to open Status file\n");
					return -1;
				}
				fgets (status_chk, 16, fp_Status);
				//line[strlen(line)-1] = 0;
				printf("Waiting for Continue and got: %s\n",status_chk);
				if(!strcmp(status_chk, "Blank")){
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
				
				sprintf(cPayload, "{\"BearID\":\"%s\",\"CorrectWord\":\"%s\",\"WordSaid\":\"%s\",\"TeachingMode\":\"1\"}",BEARID, line, sphinx_arr);
				Msg.PayloadLen = strlen(cPayload) + 1;
				MetricsParams.MessageParams = Msg;
				aws_iot_mqtt_publish(&MetricsParams);
				
				if(!strcmp(sphinx_arr, line)){
					status = write(s,"check,good_job",128);
				}
				else if(attempts < 4){
					sprintf(message_Buffer,"xmark,try_again,%s",line);
					status = write(s,message_Buffer,128);
					fclose(fp_Sphinx);
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