#include <pocketsphinx.h>


int run_sphinx(char* LanguageMode, char* File_target){

	ps_decoder_t *ps;		//pocket sphinx decoder
	cmd_ln_t *config;		//configuration object
	FILE *fh, *recognition,*continue_file;				//file to open
	char const *hyp;//string of recoginzed speech
	char const *fileToOpen,*languageMode;	
	int16 buf[512];		//buffer fed to decoder
	int rv,langNum;					
	int32 score;

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
				"-lm", "/sphinx/pocketsphinx-5prealpha/model/en-us/en-us.lm.bin",
				"-dict", "/sphinx/cmudict-custom.dict",
				NULL);
		break;
	case 2:
		// load dir
		printf("\n\nSPANISH MODE\n\n");
		config = cmd_ln_init(NULL, ps_args(), TRUE, 
				"-hmm", "/sphinx/voxforge-es-0.2/model_parameters/voxforge_es_sphinx.cd_ptm_3000/",
				"-lm", "/sphinx/voxforge-es-0.2/etc/voxforge_es_sphinx.transcription.test.lm",
				"-dict", "/sphinx/voxforge-es-0.2/etc/spanish_dictionary.dic",
				NULL);
		break;
	case 3:
		// load dir
		printf("\n\nFRENCH MODE\n\n");
                config = cmd_ln_init(NULL, ps_args(), TRUE,
                                "-hmm", "/sphinx/pocketsphinx-5prealpha/model/en-us/en-us/",
                                "-lm", "/sphinx/pocketsphinx-5prealpha/model/en-us/en-us.lm.bin",
                                "-dict", "/sphinx/cmudict-custom.dict",
                                NULL);
		break;
	case 4:
		// load dir
		printf("\n\nGREEK MODE Currently unavailable\nLoading English\n\n");
                config = cmd_ln_init(NULL, ps_args(), TRUE,
                                "-hmm", "/sphinx/pocketsphinx-5prealpha/model/en-us/en-us/",
                                "-lm", "/sphinx/pocketsphinx-5prealpha/model/en-us/en-us.lm.bin",
                                "-dict", "/sphinx/cmudict-custom.dict",
                                NULL);
		break;
	case 5:
		// load dir
		printf("\n\nPERSIAN MODE Currently unavailable\nLoading English\n\n");
                config = cmd_ln_init(NULL, ps_args(), TRUE,
                                "-hmm", "/sphinx/pocketsphinx-5prealpha/model/en-us/en-us/",
                                "-lm", "/sphinx/pocketsphinx-5prealpha/model/en-us/en-us.lm.bin",
                                "-dict", "/sphinx/cmudict-custom.dict",
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
	fileToOpen = File_target;
	//open file to recognize
	fh = fopen( fileToOpen, "rb");
	if (fh == NULL)
	{
		fprintf(stderr, "Unable to open input file\n");
		return -1;
	}
	
	recognition = fopen( "response.txt", "w+" );
	continue_file = fopen( "/Curriculum/Bluetooth/Status.txt", "w+" );
	if (recognition == NULL || continue_file == NULL)
	{	
		fprintf(stderr, "Unable to open recognition file\n");
		return -1;
	}
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
	printf("Recognized: %s\n", hyp);
	fprintf(continue_file, "Continue");
	//close audio file
	fclose(fh);
	fclose(recognition);
	//close pocket sphinx instance
	ps_free(ps);
	//clean up config
	cmd_ln_free_r(config);

	return 0;

}
