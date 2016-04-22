#include <stdio.h>
#include <errno.h>
#include <ctype.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <getopt.h>
#include <signal.h>
#include <pthread.h>
#include <sys/param.h>
#include <sys/ioctl.h>
#include <sys/socket.h>


#include <bluetooth/bluetooth.h>
#include <bluetooth/hci.h>
#include <bluetooth/hci_lib.h>
#include <bluetooth/sdp.h>
#include <bluetooth/sdp_lib.h>
#include <bluetooth/rfcomm.h>

#include "mraa.h"

#include "Lessons.h"
#include "AWS.h"
#include "Sphinx.h"
#include "aws_iot_config.h"
#include "bear_info.h"

//Thread so that we are constantly listening for messages from the tablet
void *threadListen(void *s){
	
	char message_Buffer[16];
	char network_Name[50], network_Pass[50], command[100];
	memset(message_Buffer, 0, sizeof(message_Buffer));
	memset(network_Name, 0, sizeof(network_Name));
	memset(network_Pass, 0, sizeof(network_Pass));
	printf("Stay a while and listen!\n");
	while(1){
		read((int) s, message_Buffer, sizeof(message_Buffer));
		printf("Done reading got: %s\n", message_Buffer);
		if(!strcmp(message_Buffer, "network")){
			//memset(message_Buffer, 0, sizeof(message_Buffer));

			read((int) s, network_Name, sizeof(network_Name));
			printf("NAME: %s\n", network_Name);				
			read((int) s, network_Pass, sizeof(network_Pass));
			printf("PASSWORD: %s\n", network_Pass);

			sprintf(command,"wpa_passphrase %s %s >> /etc/wpa_supplicant/wpa_supplicant.conf", network_Name, network_Pass);
			system(command);
			system("service networking restart");
			
		}
		else{
			sprintf(threadMessage, "%s", message_Buffer);
		}
		memset(message_Buffer, 0, sizeof(message_Buffer));
		sleep(5);
	}

}
int str2uuid( const char *uuid_str, uuid_t *uuid ) {
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
	bdaddr_t target;
	char addr[19] = { 0 };
	char name[248] = { 0 };
	uuid_t uuid = { 0 };
	//Change this to your apps UUID
	char *uuid_str="4e5d48e0-75df-11e3-981f-0800200c9a66";
	uint32_t range = 0x0000ffff;
	sdp_list_t *response_list = NULL, *search_list, *attrid_list;
	int s, loco_channel = -1, status;
	struct sockaddr_rc loc_addr = { 0 };
	pthread_t blueThread;
	
	FILE *fp_Setting;
	char message_Buffer[64];
	char mode[30], language[16], topic[16], topicLen[16];
	size_t len = 16;
	(void) signal(SIGINT, SIG_DFL);
	
	changeTopic_flag = 1;
///////////////////////////MRAA/////////////////////////////	
	//Initialize MRAA
        mraa_init();
        
        //Initialize MRAA Pin 8 == IO8 == GP49
        mraa_gpio_context BearButton = mraa_gpio_init(8);

        //Check for successful initialization or else return
        if (BearButton == NULL){
                printf("Error initializing Push to Talk Pin, IO2\n");
                return -1;
        }
        
		//Set pin to an input
		mraa_gpio_dir(BearButton, MRAA_GPIO_IN);
		printf("Set Pin to an input\n");
 
        
        int curr_pin = mraa_gpio_get_pin(BearButton);
        printf("The current pin number is %d\n", curr_pin);
        int curr_raw = mraa_gpio_get_pin_raw(BearButton);
        printf("The raw pin number is %d\n", curr_raw);

        printf("Going to start reading the button via polling\n");
	
//////////////////////AWS///////////////////////////////////	
	IoT_Error_t rc = NONE_ERROR;
	char HostAddress[255] = AWS_IOT_MQTT_HOST;
	char certDirectory[PATH_MAX + 1] = "/AWS/SDK/certs/";
	uint32_t port = AWS_IOT_MQTT_PORT;
	
	MQTTMessageParams Msg = MQTTMessageParamsDefault;
	Msg.qos = QOS_0;
	char cPayload[100];
	Msg.pPayload = (void *) cPayload;
	
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

	connectParams.KeepAliveInterval_sec = 1000;
	connectParams.isCleansession = true;
	connectParams.MQTTVersion = MQTT_3_1_1;
	connectParams.pClientID = "TED";
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
	
	MQTTPublishParams CurriculumParams = MQTTPublishParamsDefault;
	CurriculumParams.pTopic = "Bear/Curriculum/Request";
	
	
	MQTTPublishParams MetricsParams = MQTTPublishParamsDefault;
	MetricsParams.pTopic = "Bear/Curriculum/Metrics";

///////////////////////////////////////////////////////////////////////////////////////
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
		
			sdp_session_t *session;
			int retries;
			int foundit, responses;
			str2ba(TABLET_ADDRESS,&target); 
			memset(name, 0, sizeof(name));
			if (hci_read_remote_name(sock, &target, sizeof(name), name, 0) < 0){
				strcpy(name, "[unknown]");
			}
			
			printf("Found %s  %s, searching for the the desired service on it now\n", addr, name);
			// connect to the SDP server running on the remote machine
sdpconnect:
			session = 0; retries = 0;
			while(!session) {
				session = sdp_connect( BDADDR_ANY, &target, SDP_RETRY_IF_BUSY );
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
				loc_addr.rc_bdaddr = *(&target);
				status = connect(s, (struct sockaddr *)&loc_addr, sizeof(loc_addr));
				if( status < 0 ) {
					perror("uh oh");
				}

				
				rc = pthread_create(&blueThread, NULL, threadListen, (void *)s);
					if (rc){
						printf("ERROR: %d\n", rc);
						exit(1);
					}
				MQTTSubscribeParams subParams = MQTTSubscribeParamsDefault;
				//Loop through trying to subscribe to AWS incase the first attempt fails, so will keep trying until the app sends the edison proper network info.
				do{
					INFO("Connecting...");
					rc = aws_iot_mqtt_connect(&connectParams);
					
					if (NONE_ERROR != rc) {
						ERROR("Error(%d) connecting to %s:%d", rc, connectParams.pHostURL, connectParams.port);
					}
					
					subParams.mHandler = MQTTcallbackHandler;
					subParams.pTopic = "Bear/Curriculum/Response";
					subParams.qos = QOS_0;

					if (NONE_ERROR == rc) {
						INFO("Subscribing...");
						rc = aws_iot_mqtt_subscribe(&subParams);
						if (NONE_ERROR != rc) {
							ERROR("Error subscribing");
						}
					}
					sleep(1);
				}while(NONE_ERROR != rc);
//////////////////////////Start of teaching stuff//////////////////////////////////////////
				do {
					
				
					if(changeTopic_flag){
					
						sprintf(cPayload, "{\"BearID\":\"%s\"}",BEARID);
						Msg.PayloadLen = strlen(cPayload) + 1;
						CurriculumParams.MessageParams = Msg;
						rc = aws_iot_mqtt_publish(&CurriculumParams);
						while(rc == NONE_ERROR && changeTopic_flag){
							rc = aws_iot_mqtt_yield(100);
							INFO("-->sleep");
							sleep(1);
						}								
						changeTopic_flag = 0;
					}
					
					
					fp_Setting = fopen("/Curriculum/BearSettings.txt", "r");
					fgets(language, sizeof(language), fp_Setting);
					fgets(mode, sizeof(mode), fp_Setting);
					fgets(topic, sizeof(topic), fp_Setting);
					fgets(topicLen, sizeof(topicLen), fp_Setting);
					language[strlen(language)-1] = 0;
					mode[strlen(mode)-1] = 0;
					topic[strlen(topic)-1] = 0;
					topicLen[strlen(topicLen)-1] = 0;
					fclose(fp_Setting);

					//Wait for the tablet to tell the edison to start the lesson
					while(strcmp(threadMessage, "learning")){
						printf("Waiting for learning, got: %s\n", threadMessage);
						sleep(1);
					}

					
					printf("Current Mode: %s\n", mode);
					printf("Current Language: %s\n", language);
					printf("Current topic: %s\n", topic);

					//Call a different function depending on what teaching mode it is, also write to the tablet based on the topic info and lesson
					if(!strcmp(mode, "Repeat After Me")){
						sprintf(message_Buffer,"%s,1,%s",language, topicLen);
						status = write(s,message_Buffer,sizeof(message_Buffer));
						if(!strcmp(language, "English")){
							status = repeat_after_me_english(s, topic, MetricsParams, BearButton);
						}
						else{
							status = repeat_after_me_foreign(s, language, topic, MetricsParams, BearButton);
						}
					}
					else if(!strcmp(mode, "English to Foreign")){
						sprintf(message_Buffer,"%s,3,%s",language, topicLen);
						status = write(s,message_Buffer,sizeof(message_Buffer));
						status = english_to_foreign(s, language, topic, MetricsParams, BearButton);
					}
					else if(!strcmp(mode, "Foreign to English")){
						sprintf(message_Buffer,"%s,2,%s",language, topicLen);
						status = write(s,message_Buffer,sizeof(message_Buffer));
						status = foreign_to_english(s, language, topic, MetricsParams, BearButton);
					}
					
					
					changeTopic_flag = 1;
					printf("Changing Topic");
					sleep(5);
				} while (status > 0);
				printf("\nBluetooth Disconnected\n");
				close(s);
				sdp_record_free( rec );
			}

			sdp_close(session);
			if (loco_channel > 0) {
				goto sdpconnect;
				//break;
			}
		sleep(1);
	} while (1);

	printf("Exiting...\n");
}