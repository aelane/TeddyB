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



int split (const char *str, char c, char ***arr)
{
  int count = 1;
  int token_len = 1;
  int i = 0;
  char *p;
  char *t;

  p = str;
  while (*p != '\0')
    {
      if (*p == c)
        count++;
      p++;
    }

  *arr = (char**) malloc(sizeof(char*) * count);
  if (*arr == NULL)
    exit(1);

  p = str;
  while (*p != '\0')
    {
      if (*p == c)
      {
        (*arr)[i] = (char*) malloc( sizeof(char) * token_len );
        if ((*arr)[i] == NULL)
          exit(1);

        token_len = 0;
        i++;
      }
      p++;
      token_len++;
    }
  (*arr)[i] = (char*) malloc( sizeof(char) * token_len );
  if ((*arr)[i] == NULL)
    exit(1);

  i = 0;
  p = str;
  t = ((*arr)[i]);
  while (*p != '\0')
    {
      if (*p != c && *p != '\0')
      {
        *t = *p;
        t++;
      }
      else
      {
        *t = '\0';
        i++;
        t = ((*arr)[i]);
      }
      p++;
    }

  return count;
}

int str2uuid( const char *uuid_str, uuid_t *uuid ) 
{
    uint32_t uuid_int[4];
    char *endptr;

    if( strlen( uuid_str ) == 36 ) {
        // Parse uuid128 standard format: 12345678-9012-3456-7890-123456789012
        char buf[9] = { 0 };

        if( uuid_str[8] != '-' && uuid_str[13] != '-' &&
            uuid_str[18] != '-'  && uuid_str[23] != '-' ) {
            return 0;
        }
        // first 8-bytes
        strncpy(buf, uuid_str, 8);
        uuid_int[0] = htonl( strtoul( buf, &endptr, 16 ) );
        if( endptr != buf + 8 ) return 0;

        // second 8-bytes
        strncpy(buf, uuid_str+9, 4);
        strncpy(buf+4, uuid_str+14, 4);
        uuid_int[1] = htonl( strtoul( buf, &endptr, 16 ) );
        if( endptr != buf + 8 ) return 0;

        // third 8-bytes
        strncpy(buf, uuid_str+19, 4);
        strncpy(buf+4, uuid_str+24, 4);
        uuid_int[2] = htonl( strtoul( buf, &endptr, 16 ) );
        if( endptr != buf + 8 ) return 0;

        // fourth 8-bytes
        strncpy(buf, uuid_str+28, 8);
        uuid_int[3] = htonl( strtoul( buf, &endptr, 16 ) );
        if( endptr != buf + 8 ) return 0;

        if( uuid != NULL ) sdp_uuid128_create( uuid, uuid_int );
    } else if ( strlen( uuid_str ) == 8 ) {
        // 32-bit reserved UUID
        uint32_t i = strtoul( uuid_str, &endptr, 16 );
        if( endptr != uuid_str + 8 ) return 0;
        if( uuid != NULL ) sdp_uuid32_create( uuid, i );
    } else if( strlen( uuid_str ) == 4 ) {
        // 16-bit reserved UUID
        int i = strtol( uuid_str, &endptr, 16 );
        if( endptr != uuid_str + 4 ) return 0;
        if( uuid != NULL ) sdp_uuid16_create( uuid, i );
    } else {
        return 0;
    }

    return 1;
}

int main(void) {
	int i, j, err, sock, dev_id = -1;
	struct hci_dev_info dev_info;
	inquiry_info *info = NULL;
	int num_rsp, length, flags;
	bdaddr_t ba;
	char addr[19] = { 0 };
	char name[248] = { 0 };
	uuid_t uuid = { 0 };
	//Change this to your apps UUID
	char *uuid_str="4e5d48e0-75df-11e3-981f-0800200c9a66";
	uint32_t range = 0x0000ffff;
	sdp_list_t *response_list = NULL, *search_list, *attrid_list;
	int s, loco_channel = -1, status;
	struct sockaddr_rc loc_addr = { 0 };
	
	int continue_flag = 0,changeTopic_flag = 1, sphinx_count = 0, attempts;
	FILE *fp_Topic, *fp_Status, *fp_Sphinx;
	char message_Buffer[64];
	char line[16];
	char status_chk[16] = "";
	char sphinx_arr[64];
	size_t len = 16;
	ssize_t read;
	char **arr = NULL;
					
	(void) signal(SIGINT, SIG_DFL);

	dev_id = hci_get_route(NULL);
	if (dev_id < 0) {
		perror("No Bluetooth Adapter Available");
		exit(1);
	}

	if (hci_devinfo(dev_id, &dev_info) < 0) {
		perror("Can't get device info");
		exit(1);
	}



	sock = hci_open_dev( dev_id );
	if (sock < 0) {
		perror("HCI device open failed");
		free(info);
		exit(1);
	}

	
	if( !str2uuid( uuid_str, &uuid ) ) {
		perror("Invalid UUID");
		free(info);
		exit(1);
  }

	do {
		printf("Scanning ...\n");
		info = NULL;
		num_rsp = 0;
		flags = 0;
		length = 8; /* ~10 seconds */
		num_rsp = hci_inquiry(dev_id, length, num_rsp, NULL, &info, flags);
		if (num_rsp < 0) {
			perror("Inquiry failed");
			exit(1);
		}

		printf("No of resp %d\n",num_rsp);

		for (i = 0; i < num_rsp; i++) {
			//22:22:8E:FB:B2:85
			sdp_session_t *session;
			int retries;
			int foundit, responses;
			str2ba("22:22:8E:FB:B2:85",&(info+i)->bdaddr); 
			ba2str(&(info+i)->bdaddr, addr);
			memset(name, 0, sizeof(name));
			if (hci_read_remote_name(sock, &(info+i)->bdaddr, sizeof(name), 
					name, 0) < 0)
			strcpy(name, "[unknown]");
			printf("Found %s  %s, searching for the the desired service on it now\n", addr, name);
			// connect to the SDP server running on the remote machine
sdpconnect:
			session = 0; retries = 0;
			while(!session) {
				session = sdp_connect( BDADDR_ANY, &(info+i)->bdaddr, SDP_RETRY_IF_BUSY );
				if(session) break;
				if(errno == EALREADY && retries < 5) {
					perror("Retrying");
					retries++;
					sleep(1);
					continue;
				}
				break;
			}
			if ( session == NULL ) {
				perror("Can't open session with the device");
				free(info);
				continue;
			}
			search_list = sdp_list_append( 0, &uuid );
			attrid_list = sdp_list_append( 0, &range );
			err = 0;
			err = sdp_service_search_attr_req( session, search_list, SDP_ATTR_REQ_RANGE, attrid_list, &response_list);
			sdp_list_t *r = response_list;
			sdp_record_t *rec;
			// go through each of the service records
			foundit = 0;
			responses = 0;
			for (; r; r = r->next ) {
					responses++;
					rec = (sdp_record_t*) r->data;
					sdp_list_t *proto_list;
					
					// get a list of the protocol sequences
					if( sdp_get_access_protos( rec, &proto_list ) == 0 ) {
					sdp_list_t *p = proto_list;

						// go through each protocol sequence
						for( ; p ; p = p->next ) {
								sdp_list_t *pds = (sdp_list_t*)p->data;

								// go through each protocol list of the protocol sequence
								for( ; pds ; pds = pds->next ) {

										// check the protocol attributes
										sdp_data_t *d = (sdp_data_t*)pds->data;
										int proto = 0;
										for( ; d; d = d->next ) {
												switch( d->dtd ) { 
														case SDP_UUID16:
														case SDP_UUID32:
														case SDP_UUID128:
																proto = sdp_uuid_to_proto( &d->val.uuid );
																break;
														case SDP_UINT8:
																if( proto == RFCOMM_UUID ) {
																		printf("rfcomm channel: %d\n",d->val.int8);
																		loco_channel = d->val.int8;
																		foundit = 1;
																}
																break;
												}
										}
								}
								sdp_list_free( (sdp_list_t*)p->data, 0 );
						}
						sdp_list_free( proto_list, 0 );

					}
					if (loco_channel > 0)
						break;

			}
			printf("No of Responses %d\n", responses);
			if ( loco_channel > 0 && foundit == 1 ) {
				printf("Found service on this device, now gonna blast it with dummy data\n");
				s = socket(AF_BLUETOOTH, SOCK_STREAM, BTPROTO_RFCOMM);
				loc_addr.rc_family = AF_BLUETOOTH;
				loc_addr.rc_channel = loco_channel;
				loc_addr.rc_bdaddr = *(&(info+i)->bdaddr);
				status = connect(s, (struct sockaddr *)&loc_addr, sizeof(loc_addr));
				if( status < 0 ) {
					perror("uh oh");
				}
				
				//////////////////////////Start of teaching stuff//////////////////////////////////////////
				do {
					attempts = 0;
					
					if(changeTopic_flag){
						system("/AWS/SDK/sample_apps/subscribe_publish_sample/subscribe_publish_sample");
						changeTopic_flag = 0;
						sleep(2);
					}
					
					fp_Topic = fopen("/Curriculum/TopicData.txt", "r");
					printf("Opening Topic Data\n");
					if (fp_Topic == NULL){	
						printf("HMMMMMM");
						fprintf(stderr, "Unable to open Topic file\n");
						return -1;
					}
					//Format is: img, sound, sound based on mode, sound
					//status = write(s, "apple,repeat_after_me,apple", 64);
					//status = write(s, "bear,bear,in_english_is,bear",64);
					//status = write(s, "banana,banana,in_spanish_is,platano",64);
					
					while(fgets (line, 16, fp_Topic)!=NULL){
						line[strlen(line)-1] = 0;
						if(!strcmp(line, "Translation")){
							break;
						}
						//repeat_underscore_me
						else if(strcmp(line, "English")){
							printf ("Read %s\n", line);
							sprintf(message_Buffer,"%s,%s,in_english_is,%s,repeat_after_me,%s",line,line,line,line);
							printf("The Buffer is:%s\n",message_Buffer);
							status = write(s,message_Buffer,128);
							//status = write(s, "banana,banana,in_spanish_is,platano",64);
							printf ("Wrote %d bytes\n", status);
							sleep(7);
						}
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
								fprintf(fp_Status, "Blah");
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
							//printf("Attempting to split file\n");
							//sphinx_count = split(sphinx_arr, ' ', &arr);
							/*
							for (i = 0; i < sphinx_count; i++){
								 printf("string #%d: %s\n", i, arr[i]);
							}
							*/
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
					fclose(fp_Topic);
					changeTopic_flag = 1;
				} while (status > 0);
				close(s);
				sdp_record_free( rec );
			}

			sdp_close(session);
			if (loco_channel > 0) {
				goto sdpconnect;
				//break;
			}
		}
	} while (1);

	printf("Exiting...\n");
}