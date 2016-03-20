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
		
			//22:22:8E:FB:B2:85 <- Address of the Tablet(Add to header file later?
			sdp_session_t *session;
			int retries;
			int foundit, responses;
			str2ba("22:22:8E:FB:B2:85",&target); 
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
				
				//////////////////////////Start of teaching stuff//////////////////////////////////////////
				do {
					
					if(changeTopic_flag){
						system("/AWS/SDK/sample_apps/subscribe_publish_sample/subscribe_publish_sample");
						changeTopic_flag = 0;
						sleep(2);
					}
					
					status = repeat_after_me_english(s);
					
					changeTopic_flag = 1;
					printf("Changing Topic");
					sleep(10);
				} while (status > 0);
				printf("CHAOS REIGNS");
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