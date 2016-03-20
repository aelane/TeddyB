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

int repeat_after_me_english(int s){

	int continue_flag = 0,changeTopic_flag = 1, sphinx_count = 0, attempts;
	int status = 1;
	FILE *fp_Topic, *fp_Status, *fp_Sphinx;
	char message_Buffer[64];
	char line[16];
	char status_chk[16] = "";
	char sphinx_arr[64];

	
	fp_Topic = fopen("/Curriculum/TopicData.txt", "r");
	printf("Opening Topic Data\n");
	if (fp_Topic == NULL){	
		fprintf(stderr, "Unable to open Topic file\n");
		return -1;
	}
	//Format is: img, sound, sound based on mode, sound
	//status = write(s, "apple,repeat_after_me,apple", 64);
	//status = write(s, "bear,bear,in_english_is,bear",64);
	//status = write(s, "banana,banana,in_spanish_is,platano",64);
	
	while(fgets (line, 16, fp_Topic)!=NULL){
		attempts = 0;
		printf("Getting new word");
		line[strlen(line)-1] = 0;

		//repeat_underscore_me
		if(strcmp(line, "English")){
			printf ("Read %s\n", line);
			sprintf(message_Buffer,"%s,%s,in_english_is,%s,repeat_after_me,%s",line,line,line,line);
			printf("The Buffer is:%s\n",message_Buffer);
			status = write(s,message_Buffer,128);
			//status = write(s, "banana,banana,in_spanish_is,platano",64);
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
				if(!strcmp(sphinx_arr, line)){
					status = write(s,"check,good_job",64);
				}
				else if(attempts < 4){
					sprintf(message_Buffer,"xmark,try_again,%s",line);
					status = write(s,message_Buffer,64);
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