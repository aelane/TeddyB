#include <pocketsphinx.h>


int run_sphinx(char* LanguageMode){
	
	ps_decoder_t *ps;		//pocket sphinx decoder
	cmd_ln_t *config;		//configuration object
	FILE *fh, *recognition,*continue_file,*save_file;				//file to open
	char const *hyp;//string of recoginzed speech
	char *hyp_lower;
	char const *fileToOpen;	
	int16 buf[512];		//buffer fed to decoder
	int rv,langNum;					
	int32 score;
	int i;
	
	if (*LanguageMode == 'e')
	{
		langNum = 1;
	}
	else if (*LanguageMode == 's')
	{
		langNum = 2;
	}
	else if (*LanguageMode == 'f')
	{
		langNum = 3;
	}
	else if (*LanguageMode == 'g')
	{
		langNum = 4;
	}
	else if (*LanguageMode == 'p')
	{
		langNum = 5;
	}
	else{
		langNum = -1;
	}
	//initialize configurations
	switch (langNum){ // languages modes: eng, spa, fre, grk, per
	case 1:
		// set up directory of hmm,lm,dict
		printf("\n\nENGLISH MODE\n\n");
		config = cmd_ln_init(NULL, ps_args(), TRUE, 
				"-hmm", "/sphinx/pocketsphinx-5prealpha/model/en-us/en-us/",
				"-lm", "/sphinx/pocketsphinx-5prealpha/model/en-us/english.lm",
				"-dict", "/sphinx/cmudict-custom.dict",
				NULL);
		break;
	case 2:
		// load dir
		printf("\n\nSPANISH MODE\n\n");
		config = cmd_ln_init(NULL, ps_args(), TRUE, 
				"-hmm", "/sphinx/languages/spanish_language/spanish_adapted/",
				"-lm", "/sphinx/languages/spanish_language/spanish.lm",
				"-dict", "/sphinx/languages/spanish_language/spanish_dictionary.dic",
				NULL);
		break;
	case 3:
		// load dir
		printf("\n\nFRENCH MODE\n\n");
                config = cmd_ln_init(NULL, ps_args(), TRUE,
                                "-hmm", "/sphinx/languages/french_language/french-corpus/french-corpus/en-us/",
                                "-lm", "/sphinx/languages/french_language/french-corpus/french-corpus/french.lm",
                                "-dict", "/sphinx/languages/french_language/french-corpus/french-corpus/french.dic",
                                NULL);
		break;
	case 4:
		// load dir
		printf("\n\nGREEK MODE\n\n");
                config = cmd_ln_init(NULL, ps_args(), TRUE,
                                "-hmm", "/sphinx/languages/greek_language/greek_adaptation/",
                                "-lm", "/sphinx/languages/greek_language/greek_language_model.lm",
                                "-dict", "/sphinx/languages/greek_language/greek_dict.dict",
                                NULL);
		break;
	case 5:
		// load dir
		printf("\n\nPERSIAN MODE\n\n");
                config = cmd_ln_init(NULL, ps_args(), TRUE,
                                "-hmm", "/sphinx/languages/persian_language/persian_adaptation/",
                                "-lm", "/sphinx/languages/persian_language/persian_language_model.lm",
                                "-dict", "/sphinx/languages/persian_language/persian_dict.dict",
                                NULL);
		break;
	default:
		//load english
		printf("\n\nUNKNOWN MODE. Language not recognized defaulting to English\n\n");
                config = cmd_ln_init(NULL, ps_args(), TRUE,
                                "-hmm", "/sphinx/pocketsphinx-5prealpha/model/en-us/en-us/",
                                "-lm", "/sphinx/pocketsphinx-5prealpha/model/en-us/en-us.lm.bin",
                                "-dict", "/sphinx/cmudict-custom.dict",
                                NULL);
		break;

	}
	//check if configuration is updated properly
	if (config == NULL)
	{
		fprintf(stderr, "Failed to create config object, see log for details\n");
		return -1;
	}

	//initialize pocket sphinx with configs
	ps = ps_init(config);
	if (ps == NULL)
	{
		fprintf(stderr, "Failed to create recognizer, see log for details\n");
		return -1;
	}
	//open file to recognize
	fh = fopen( "/Curriculum/Bluetooth/hello.wav", "rb");
	if (fh == NULL)
	{
		fprintf(stderr, "Unable to open input file\n");
		return -1;
	}



	recognition = fopen( "/sphinx/response.txt", "w+" );
	save_file = fopen( "/Curriculum/Sounds/Bucket/bucket.txt", "w+" );

	//start decoding file
	rv = ps_start_utt(ps);

	//feed data 512 samples at a time until end of flie
	while (!feof(fh))
	{
		size_t nsamp;
		nsamp = fread(buf, 2, 512 , fh);
		rv = ps_process_raw(ps, buf, nsamp, FALSE, FALSE);
	}
	//mark end of utterance
	rv = ps_end_utt(ps);
	//get hypothesis of recognition and print it
	hyp = ps_get_hyp(ps, &score);

	fprintf(recognition, "%s",hyp);
	fprintf(save_file, "%s" ,hyp);
	printf("Recognized: %s\n", hyp);
	
	//close audio file

	fclose(fh);

	fclose(recognition);
	fclose(save_file);
	//close pocket sphinx instance
	ps_free(ps);
	//clean up config
	cmd_ln_free_r(config);

	return 0;

}
