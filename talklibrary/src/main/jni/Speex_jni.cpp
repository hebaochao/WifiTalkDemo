#include <jni.h>

#include <string.h>
#include <unistd.h>
#include "speex_code.h"
#include <speex/speex.h>

#define LOG_TAG "Native"
#define LOGI(...) ((void)__android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))

static int codec_open = 0;

static int dec_frame_size;
static int enc_frame_size;

static SpeexBits ebits, dbits;
void *enc_state;
void *dec_state;

static JavaVM *gJavaVM;

extern "C"
JNIEXPORT jint JNICALL Java_com_example_alex_talklibrary_Utils_Speex_open
  (JNIEnv *env, jobject obj, jint compression) {
	int tmp;

	if (codec_open++ != 0)
		return (jint)0;

	speex_bits_init(&ebits);
	speex_bits_init(&dbits);

	enc_state = speex_encoder_init(&speex_nb_mode);
	dec_state = speex_decoder_init(&speex_nb_mode);
	tmp = compression;
	speex_encoder_ctl(enc_state, SPEEX_SET_QUALITY, &tmp);
	speex_encoder_ctl(enc_state, SPEEX_GET_FRAME_SIZE, &enc_frame_size);
	speex_decoder_ctl(dec_state, SPEEX_GET_FRAME_SIZE, &dec_frame_size);

	return (jint)0;
}

extern "C"
JNIEXPORT jint Java_com_example_alex_talklibrary_Utils_Speex_encode
    (JNIEnv *env, jobject obj, jshortArray lin, jint offset, jbyteArray encoded, jint size) {

        jshort buffer[enc_frame_size];
        jbyte output_buffer[enc_frame_size];
	int nsamples = (size-1)/enc_frame_size + 1;
	int i, tot_bytes = 0;

	if (!codec_open)
		return 0;

	speex_bits_reset(&ebits);

	for (i = 0; i < nsamples; i++) {
		env->GetShortArrayRegion(lin, offset + i*enc_frame_size, enc_frame_size, buffer);
		speex_encode_int(enc_state, buffer, &ebits);
	}
	//env->GetShortArrayRegion(lin, offset, enc_frame_size, buffer);
	//speex_encode_int(enc_state, buffer, &ebits);

	tot_bytes = speex_bits_write(&ebits, (char *)output_buffer,
				     enc_frame_size);
	env->SetByteArrayRegion(encoded, 0, tot_bytes,
				output_buffer);

        return (jint)tot_bytes;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_example_alex_talklibrary_Utils_Speex_decode
    (JNIEnv *env, jobject obj, jbyteArray encoded, jshortArray lin, jint size) {

        jbyte buffer[dec_frame_size];
        jshort output_buffer[dec_frame_size];
        jsize encoded_length = size;

	if (!codec_open)
		return 0;

	env->GetByteArrayRegion(encoded, 0, encoded_length, buffer);
	speex_bits_read_from(&dbits, (char *)buffer, encoded_length);
	speex_decode_int(dec_state, &dbits, output_buffer);
	env->SetShortArrayRegion(lin, 0, dec_frame_size,
				 output_buffer);

	return (jint)dec_frame_size;
}

extern "C"
JNIEXPORT jint JNICALL Java_com_example_alex_talklibrary_Utils_Speex_getFrameSize
    (JNIEnv *env, jobject obj) {

	if (!codec_open)
		return 0;
	return (jint)enc_frame_size;

}

extern "C"
JNIEXPORT void JNICALL Java_com_example_alex_talklibrary_Utils_Speex_close
    (JNIEnv *env, jobject obj) {

	if (--codec_open != 0)
		return;

	speex_bits_destroy(&ebits);
	speex_bits_destroy(&dbits);
	speex_decoder_destroy(dec_state);
	speex_encoder_destroy(enc_state);
}


extern "C"
JNIEXPORT void JNICALL Java_com_example_alex_talklibrary_Utils_SpeexCoder_InitSpeexEncode
		(JNIEnv *, jobject, jint frameHZ) {

	// TODO
	InitSpeexEncode(frameHZ);

}



extern "C"
JNIEXPORT void JNICALL Java_com_example_alex_talklibrary_Utils_SpeexCoder_ReleaseSpeexEncode
		(JNIEnv *env, jobject obj){

	// TODO
	ReleaseSpeexEncode();
}

extern "C"
JNIEXPORT jint JNICALL Java_com_example_alex_talklibrary_Utils_SpeexCoder_SpeexEncodeAudioData
		(JNIEnv *env, jobject onj, jshortArray pInBuf_, jint len, jbyteArray outFrame_, jint outFrameSize){
	jshort *pInBuf = env->GetShortArrayElements(pInBuf_, NULL);
	jbyte *outFrame = env->GetByteArrayElements(outFrame_, NULL);

	int  result = SpeexEncode(pInBuf,len,(char *)outFrame,outFrameSize);

	env->ReleaseShortArrayElements(pInBuf_, pInBuf, 0);
	env->ReleaseByteArrayElements(outFrame_, outFrame, 0);
	return   result;
}

extern "C"
JNIEXPORT void JNICALL Java_com_example_alex_talklibrary_Utils_SpeexCoder_InitSpeexDecode
		(JNIEnv * env, jobject obj, jint frameHZ) {

	// TODO
	InitSpeexDecode(frameHZ);

}

extern "C"
JNIEXPORT void JNICALL Java_com_example_alex_talklibrary_Utils_SpeexCoder_ReleaseSpeexDecode
		(JNIEnv * onj, jobject obj){

	// TODO
	ReleaseSpeexDecode();
}



extern "C"
JNIEXPORT jint JNICALL Java_com_example_alex_talklibrary_Utils_SpeexCoder_SpeexDecodeAudioData
		(JNIEnv * env, jobject obj, jbyteArray pInBuf_, jint len, jshortArray outFrame_, jint outFrameSize){
	jbyte *pInBuf = env->GetByteArrayElements(pInBuf_, NULL);
	jshort *outFrame = env->GetShortArrayElements(outFrame_, NULL);

	// TODO
	int result = SpeexDecode((char *)pInBuf,len,outFrame,outFrameSize);

	env->ReleaseByteArrayElements(pInBuf_, pInBuf, 0);
	env->ReleaseShortArrayElements(outFrame_, outFrame, 0);
	return   result;
}









