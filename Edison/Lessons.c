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

int foreign_to_english(int s, char* language, char* topic, MQTTPublishParams MetricsParams, mraa_gpio_context BearButton){

	int continue_flag = 0,changeTopic_flag = 1, sphinx_count = 0, attempts;
	int status = 1, i;
	int new_reading, old_reading;
	FILE *fp_Topic, *fp_Trans, *fp_Status, *fp_Sphinx;
	char message_Buffer[128], line[16], line_trans[16], status_chk[16] = "", sphinx_arr[64];
	int correct = 0;
	char* token;

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
	while(fgets(line, sizeof(line), fp_Topic)!=NULL && fgets(line_trans, sizeof(line_trans), fp_Trans)!=NULL){
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
		
		printf("Language setting is: %s\n", language);
		if(strcmp(line, "English")){
			printf ("Read %s\n", line);
			
			sprintf(message_Buffer,"thinking,how_do_you_say,%s,in_english_question_mark",line_trans);
			
			printf("The Buffer is:%s\n",message_Buffer);
			status = write(s,message_Buffer,sizeof(message_Buffer));
			if(status < 0){
				printf("Diconnected");
				return status;
			}
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
				//Check the old button value against the new
				if (new_reading == 1 && old_reading == 1) {
					printf("Keep waiting for a button press\n");
				}
				if (new_reading == 0 && old_reading == 0) {
					printf("Keep waiting for a button press\n");
				}
				if (new_reading == 1 && old_reading == 0) {
					printf("start recording\n");
				}
				if (new_reading == 0 && old_reading == 1) {
					printf("Stop the recording\n");
					system("arecord -f S16_LE -r 16000 -d 3 /Curriculum/Bluetooth/hello.wav");
					run_sphinx("e");
					continue_flag = 1;
					printf("\nSphinx exit success\n");
				}
				//Sleep for n microseconds, so then, we're reading each second
				usleep(500000);
				old_reading = new_reading;
				
				}while(continue_flag != 1);
				////////////////////////////////////////////////////////////////////////
				
				/////////////////////////Read in what sphinx has done///////////////////
				
				
				fp_Sphinx = fopen("/sphinx/response.txt","r");
				if (fp_Sphinx == NULL){				
					fprintf(stderr, "Unable to open sphinx response file\n");
					return -1;
				}
				fgets (sphinx_arr, sizeof(sphinx_arr), fp_Sphinx);
				
				attempts++;
				//Output of sphinx is uppercase for persian and greek so make them lowercase
				if(!strcmp(language, "Persian") || !strcmp(language, "Greek")){
					for(i = 0; i < strlen(sphinx_arr); i++){
						if(sphinx_arr[i] != ' '){
							sphinx_arr[i] = tolower(sphinx_arr[i]);
						}
					}
				}
				printf("Target word: %s\n", line);
				printf("Sphinx result: %s\n", sphinx_arr);
				
				
				correct = 0;
				token = strtok(sphinx_arr, " ");
				//Look through each word sphinx outputs to see if the correct word is there
				while (token) {
					if(!strcmp(token, line)){
						correct = 1;
						break;
					}
					
				    printf("token: %s\n", token);
				    token = strtok(NULL, " ");
				}
				if(correct){
					sprintf(message_Buffer,"check,good_job");
					status = write(s,message_Buffer,sizeof(message_Buffer));
					if(status < 0){
						printf("Diconnected");
						return status;
					}
					sprintf(cPayload, "{\"BearID\":\"%s\",\"CorrectWord\":\"%s\",\"WordSaid\":\"%s\",\"TeachingMode\":\"Foreign to English\", \"Language\":\"%s\", \"Topic\":\"%s\", \"Correct\":\"true\", \"Attempt\":\"%d\"}",BEARID, line_trans, sphinx_arr,language,topic,attempts);
					Msg.PayloadLen = strlen(cPayload) + 1;
					MetricsParams.MessageParams = Msg;
					aws_iot_mqtt_publish(&MetricsParams);
				}
				else if(attempts < 3){
					sprintf(message_Buffer,"xmark,try_again,%s", line_trans);
					status = write(s,message_Buffer,sizeof(message_Buffer));
					fclose(fp_Sphinx);
					if(status < 0){
						printf("Diconnected");
						return status;
					}
					sprintf(cPayload, "{\"BearID\":\"%s\",\"CorrectWord\":\"%s\",\"WordSaid\":\"%s\",\"TeachingMode\":\"Foreign to English\", \"Language\":\"%s\", \"Topic\":\"%s\", \"Correct\":\"false\", \"Attempt\":\"%d\"}",BEARID, line_trans, sphinx_arr,language,topic,attempts);
					Msg.PayloadLen = strlen(cPayload) + 1;
					MetricsParams.MessageParams = Msg;
					aws_iot_mqtt_publish(&MetricsParams);
					goto WRONG;
				}
				else{
					sprintf(message_Buffer,"wrong");
					printf("Message sent was: %s\n",message_Buffer);
					status = write(s,message_Buffer,sizeof(message_Buffer));
					if(status < 0){
						printf("Diconnected");
						return status;
					}
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

int english_to_foreign(int s, char* language, char* topic, MQTTPublishParams MetricsParams, mraa_gpio_context BearButton){

	int continue_flag = 0,changeTopic_flag = 1, sphinx_count = 0, attempts;
	int status = 1, i;
	int new_reading, old_reading;
	FILE *fp_Topic, *fp_Trans, *fp_Status, *fp_Sphinx;
	char message_Buffer[128], line[16], line_trans[16], status_chk[16] = "", sphinx_arr[64];
	int correct = 0;
	char* token;

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
	while(fgets(line, sizeof(line), fp_Topic)!=NULL && fgets(line_trans, sizeof(line_trans), fp_Trans)!=NULL){
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

		printf("Language setting is: %s\n", language);
		if(strcmp(line, "English")){
			printf ("Read %s\n", line);
			
			if(!strcmp(language, "Spanish")){
				sprintf(message_Buffer,"%s,how_do_you_say,%s,in_spanish_question_mark",line,line);
			}
			else if(!strcmp(language, "French")){
				sprintf(message_Buffer,"%s,how_do_you_say,%s,in_french_question_mark",line,line);
			}
			else if(!strcmp(language, "Persian")){
				sprintf(message_Buffer,"%s,how_do_you_say,%s,in_persian_question_mark",line,line);
			}
			else if(!strcmp(language, "Greek")){
				sprintf(message_Buffer,"%s,how_do_you_say,%s,in_greek_question_mark",line,line);
			}
			
			
			printf("The Buffer is:%s\n",message_Buffer);
			status = write(s,message_Buffer,sizeof(message_Buffer));
			if(status < 0){
				printf("Diconnected");
				return status;
			}
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
				//Check the old button value against the new
				if (new_reading == 1 && old_reading == 1) {
					printf("Keep waiting for a button press\n");
				}
				if (new_reading == 0 && old_reading == 0) {
					printf("Keep waiting for a button press\n");
				}
				if (new_reading == 1 && old_reading == 0) {
					printf("start recording\n");
				}
				if (new_reading == 0 && old_reading == 1) {
					printf("Stop the recording\n");
					system("arecord -f S16_LE -r 16000 -d 3 /Curriculum/Bluetooth/hello.wav");
					if(!strcmp(language, "Spanish")){
						run_sphinx("s");
					}
					else if(!strcmp(language, "French")){
						run_sphinx("f");
					}
					else if(!strcmp(language, "Persian")){
						run_sphinx("p");
					}
					else if(!strcmp(language, "Greek")){
						run_sphinx("g");
					}
					continue_flag = 1;
				}
				//Sleep for n microseconds, so then, we're reading each second
				usleep(500000);
				old_reading = new_reading;
				
				}while(continue_flag != 1);
				////////////////////////////////////////////////////////////////////////
				
				/////////////////////////Read in what sphinx has done///////////////////
				
				fp_Sphinx = fopen("/sphinx/response.txt","r");
				if (fp_Sphinx == NULL){					
					fprintf(stderr, "Unable to open sphinx response file\n");
					return -1;
				}
				fgets (sphinx_arr, sizeof(sphinx_arr), fp_Sphinx);
				
				attempts++;
				//Output of sphinx is uppercase for persian and greek so make them lowercase
				if(!strcmp(language, "Persian") || !strcmp(language, "Greek")){
					for(i = 0; i < strlen(sphinx_arr); i++){
						if(sphinx_arr[i] != ' '){
							sphinx_arr[i] = tolower(sphinx_arr[i]);
						}
					}
				}
				printf("Target word: %s\n", line_trans);
				printf("Sphinx result: %s\n", sphinx_arr);
				
				
				correct = 0;
				token = strtok(sphinx_arr, " ");
				//Look through each word sphinx outputs to see if the correct word is there
				while (token) {
					if(!strcmp(token, line_trans)){
						correct = 1;
						break;
					}
					
				    printf("token: %s\n", token);
				    token = strtok(NULL, " ");
				}
				if(correct){
					sprintf(message_Buffer,"check,good_job");
					status = write(s,message_Buffer,sizeof(message_Buffer));
					if(status < 0){
						printf("Diconnected");
						return status;
					}
					sprintf(cPayload, "{\"BearID\":\"%s\",\"CorrectWord\":\"%s\",\"WordSaid\":\"%s\",\"TeachingMode\":\"English to Foreign\", \"Language\":\"%s\", \"Topic\":\"%s\", \"Correct\":\"true\", \"Attempt\":\"%d\"}",BEARID, line, sphinx_arr,language,topic,attempts);
					Msg.PayloadLen = strlen(cPayload) + 1;
					MetricsParams.MessageParams = Msg;
					aws_iot_mqtt_publish(&MetricsParams);
				}
				else if(attempts < 3){					
					sprintf(message_Buffer,"xmark,try_again,%s", line);
					status = write(s,message_Buffer,sizeof(message_Buffer));
					fclose(fp_Sphinx);
					if(status < 0){
						printf("Diconnected");
						return status;
					}
					sprintf(cPayload, "{\"BearID\":\"%s\",\"CorrectWord\":\"%s\",\"WordSaid\":\"%s\",\"TeachingMode\":\"English to Foreign\", \"Language\":\"%s\", \"Topic\":\"%s\", \"Correct\":\"false\", \"Attempt\":\"%d\"}",BEARID, line, sphinx_arr,language,topic,attempts);
					Msg.PayloadLen = strlen(cPayload) + 1;
					MetricsParams.MessageParams = Msg;
					aws_iot_mqtt_publish(&MetricsParams);
					goto WRONG;
				}
				else{
					sprintf(message_Buffer,"wrong");
					printf("Message sent was: %s\n",message_Buffer);
					status = write(s,message_Buffer,sizeof(message_Buffer));
					if(status < 0){
						printf("Diconnected");
						return status;
					}
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

int repeat_after_me_foreign(int s, char* language,char* topic, MQTTPublishParams MetricsParams, mraa_gpio_context BearButton){

	int continue_flag = 0,changeTopic_flag = 1, sphinx_count = 0, attempts;
	int status = 1, i;
	int new_reading, old_reading;
	FILE *fp_Topic, *fp_Trans, *fp_Status, *fp_Sphinx;
	char message_Buffer[128], line[16], line_trans[16], status_chk[16] = "", sphinx_arr[64];
	int correct = 0;
	char* token;

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
	while(fgets(line, sizeof(line), fp_Topic)!=NULL && fgets(line_trans, sizeof(line_trans), fp_Trans)!=NULL){
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

		printf("Language setting is: %s\n", language);
		if(strcmp(line, "English")){
			printf ("Read %s\n", line);
			
			if(!strcmp(language, "Spanish")){
				sprintf(message_Buffer,"%s,%s,in_spanish_is,%s,repeat_after_me,%s",line,line,line_trans,line_trans);
			}
			else if(!strcmp(language, "French")){
				sprintf(message_Buffer,"%s,%s,in_french_is,%s,repeat_after_me,%s",line,line,line_trans,line_trans);
			}
			else if(!strcmp(language, "Persian")){
				sprintf(message_Buffer,"%s,%s,in_Persian_is,%s,repeat_after_me,%s",line,line,line_trans,line_trans);
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
				//Check the old button value against the new

				if (new_reading == 1 && old_reading == 1) {
					printf("Keep waiting for a button press\n");
				}
				if (new_reading == 0 && old_reading == 0) {
					printf("Keep waiting for a button press\n");
				}
				if (new_reading == 1 && old_reading == 0) {
					printf("start recording\n");
				}
				if (new_reading == 0 && old_reading == 1) {
					printf("Stop the recording\n");
					system("arecord -f S16_LE -r 16000 -d 3 /Curriculum/Bluetooth/hello.wav");
					if(!strcmp(language, "Spanish")){
						run_sphinx("s");
					}
					else if(!strcmp(language, "French")){
						run_sphinx("f");
					}
					else if(!strcmp(language, "Persian")){
						run_sphinx("p");
					}
					else if(!strcmp(language, "Greek")){
						run_sphinx("g");
					}
					continue_flag = 1;
				}
				//Sleep for n microseconds, so then, we're reading each second
				usleep(500000);
				old_reading = new_reading;
				
		
				}while(continue_flag != 1);
				////////////////////////////////////////////////////////////////////////
				
				/////////////////////////Read in what sphinx has done///////////////////
				
				
				fp_Sphinx = fopen("/sphinx/response.txt","r");
				if (fp_Sphinx == NULL){	
				
					fprintf(stderr, "Unable to open sphinx response file\n");
					return -1;
				}
				fgets (sphinx_arr, sizeof(sphinx_arr), fp_Sphinx);
				fclose(fp_Sphinx);
				attempts++;

				//Output of sphinx is uppercase for persian and greek so make them lowercase
				if(!strcmp(language, "Persian") || !strcmp(language, "Greek")){
					for(i = 0; i < strlen(sphinx_arr); i++){
						if(sphinx_arr[i] != ' '){
							sphinx_arr[i] = tolower(sphinx_arr[i]);
						}
					}
				}
				printf("Target word: %s\n", line_trans);
				printf("Sphinx result: %s\n", sphinx_arr);
				
				correct = 0;
				token = strtok(sphinx_arr, " ");
				//Look through each word sphinx outputs to see if the correct word is there
				while (token) {
					if(!strcmp(token, line_trans)){
						correct = 1;
						break;
					}
					
				    printf("token: %s\n", token);
				    token = strtok(NULL, " ");
				}
				if(correct){
					sprintf(message_Buffer,"check,good_job");
					printf("Message sent was: %s\n",message_Buffer);
					status = write(s,message_Buffer,sizeof(message_Buffer));
					if(status < 0){
						printf("Diconnected");
						return status;
					}
					sprintf(cPayload, "{\"BearID\":\"%s\",\"CorrectWord\":\"%s\",\"WordSaid\":\"%s\",\"TeachingMode\":\"Repeat After Me\", \"Language\":\"%s\", \"Topic\":\"%s\", \"Correct\":\"true\", \"Attempt\":\"%d\"}",BEARID, line_trans, sphinx_arr,language,topic,attempts);
					Msg.PayloadLen = strlen(cPayload) + 1;
					MetricsParams.MessageParams = Msg;
					aws_iot_mqtt_publish(&MetricsParams);
				}
				else if(attempts < 3){
					sprintf(message_Buffer,"xmark,try_again,%s", line_trans);
					printf("Message sent was: %s\n",message_Buffer);
					status = write(s,message_Buffer,sizeof(message_Buffer));
					if(status < 0){
						printf("Diconnected");
						return status;
					}
					sprintf(cPayload, "{\"BearID\":\"%s\",\"CorrectWord\":\"%s\",\"WordSaid\":\"%s\",\"TeachingMode\":\"Repeat After Me\", \"Language\":\"%s\", \"Topic\":\"%s\", \"Correct\":\"false\", \"Attempt\":\"%d\"}",BEARID, line_trans, sphinx_arr,language,topic,attempts);
					Msg.PayloadLen = strlen(cPayload) + 1;
					MetricsParams.MessageParams = Msg;
					aws_iot_mqtt_publish(&MetricsParams);
					goto WRONG;
				}
				else{
					sprintf(message_Buffer,"wrong");
					printf("Message sent was: %s\n",message_Buffer);
					status = write(s,message_Buffer,sizeof(message_Buffer));
					if(status < 0){
						printf("Diconnected");
						return status;
					}
					attempts = 0;
				}
				
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
	int correct = 0;
	char* token;

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

	
SKIP:
	while(fgets (line, sizeof(line), fp_Topic)!=NULL){
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
				//Check the old button value against the new
				if (new_reading == 1 && old_reading == 1) {
					printf("Keep waiting for a button press\n");
				}
				if (new_reading == 0 && old_reading == 0) {
					printf("Keep waiting for a button press\n");
				}
				if (new_reading == 1 && old_reading == 0) {
					printf("start recording\n");
				}
				if (new_reading == 0 && old_reading == 1) {
					printf("Stop the recording\n");
					system("arecord -f S16_LE -r 16000 -d 3 /Curriculum/Bluetooth/hello.wav");
					run_sphinx("e");
					continue_flag = 1;
				}
				//Sleep for n microseconds, so then, we're reading each second
				usleep(500000);
				old_reading = new_reading;
				
				}while(continue_flag != 1);
				////////////////////////////////////////////////////////////////////////
				
				/////////////////////////Read in what sphinx has done///////////////////
				
				sphinx_count = 0;
				
				fp_Sphinx = fopen("/sphinx/response.txt","r");
				if (fp_Sphinx == NULL){	
				
					fprintf(stderr, "Unable to open sphinx response file\n");
					return -1;
				}
				fgets (sphinx_arr, sizeof(sphinx_arr), fp_Sphinx);
				fclose(fp_Sphinx);
				attempts++;
				printf("Target word: %s\n", line);
				printf("Sphinx result: %s\n", sphinx_arr);
				
				correct = 0;
				token = strtok(sphinx_arr, " ");
				//Look through each word sphinx outputs to see if the correct word is there
				while (token) {
					if(!strcmp(token, line)){
						correct = 1;
						break;
					}
					
				    printf("token: %s\n", token);
				    token = strtok(NULL, " ");
				}
				if(correct){					
					sprintf(message_Buffer,"check,good_job");
					printf("Message sent was: %s\n",message_Buffer);
					status = write(s,message_Buffer,sizeof(message_Buffer));
					if(status < 0){
						printf("Diconnected");
						return status;
					}
					
					sprintf(cPayload, "{\"BearID\":\"%s\",\"CorrectWord\":\"%s\",\"WordSaid\":\"%s\",\"TeachingMode\":\"Repeat After Me\", \"Language\":\"English\", \"Topic\":\"%s\", \"Correct\":\"true\", \"Attempt\":\"%d\"}",BEARID, line, sphinx_arr, topic, attempts);
					Msg.PayloadLen = strlen(cPayload) + 1;
					MetricsParams.MessageParams = Msg;
					aws_iot_mqtt_publish(&MetricsParams);
				}
				else if(attempts < 3){
					sprintf(message_Buffer,"xmark,try_again,%s", line);
					printf("Message sent was: %s\n",message_Buffer);
					status = write(s,message_Buffer,sizeof(message_Buffer));
					if(status < 0){
						printf("Diconnected");
						return status;
					}
					sprintf(cPayload, "{\"BearID\":\"%s\",\"CorrectWord\":\"%s\",\"WordSaid\":\"%s\",\"TeachingMode\":\"Repeat After Me\", \"Language\":\"English\", \"Topic\":\"%s\", \"Correct\":\"false\", \"Attempt\":\"%d\"}",BEARID, line, sphinx_arr, topic, attempts);
					Msg.PayloadLen = strlen(cPayload) + 1;
					MetricsParams.MessageParams = Msg;
					aws_iot_mqtt_publish(&MetricsParams);
					goto WRONG;
				}
				else{
					sprintf(message_Buffer,"wrong");
					printf("Message sent was: %s\n",message_Buffer);
					status = write(s,message_Buffer,sizeof(message_Buffer));
					if(status < 0){
						printf("Diconnected");
						return status;
					}
					attempts = 0;
				}
				
				sleep(3);
				/////////////////////////////////////////////////////////////////////////
				
			}
		}
		
	}
	
	printf("About to exit teaching");
	fclose(fp_Topic);
	return status;
}