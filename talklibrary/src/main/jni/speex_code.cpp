#include <stdio.h>
#include "speex_code.h"

static void * encState = NULL;
static SpeexBits encBits;
static spx_int32_t encInFrameSize = 0;

/*******************************************
��ʼ������
����: frHz ����Ƶ��
����ֵ �� �ɹ�0��ʧ�� ������
*******************************************/
int InitSpeexEncode(int frHz)
{
	switch (frHz)
	{
		case 8000:
			encState = speex_encoder_init(speex_lib_get_mode(SPEEX_MODEID_NB));
			break;
		case 16000:
			encState = speex_encoder_init(speex_lib_get_mode(SPEEX_MODEID_WB));
			break;
		case 32000:
			encState = speex_encoder_init(speex_lib_get_mode(SPEEX_MODEID_UWB));
			break;
		default:
			return SPEEX_SAMPLING_FREQUENCY_ERR;
	}
	if (encState == NULL)
		return SPEEX_ENCODER_INIT_ERR;
	int tmp = 0;
	if (speex_encoder_ctl(encState, SPEEX_SET_VBR, &tmp) != 0)
		return SPEEX_UNKNOW_ERROR;
	tmp = 8;
	if (speex_encoder_ctl(encState, SPEEX_SET_QUALITY, &tmp) != 0)
		return SPEEX_UNKNOW_ERROR;
	tmp = 1;
	if (speex_encoder_ctl(encState, SPEEX_SET_COMPLEXITY, &tmp) != 0)
		return SPEEX_UNKNOW_ERROR;
	if (speex_encoder_ctl(encState, SPEEX_GET_FRAME_SIZE, &encInFrameSize) != 0)
		return SPEEX_GET_FRAME_SIZE_ERR;
	speex_bits_init(&encBits);
	return SPEEX_SUCCEED;
}

/************************************
�ͷű�����Դ
************************************/
void ReleaseSpeexEncode()
{
	if (encState != NULL)
	{
		speex_encoder_destroy(encState);
		encState = NULL;
	}
	speex_bits_destroy(&encBits);
}

/*************************************************
������Ƶ
������ pInFrame����ԭʼ��Ƶ
������ inFrameSize������Ƶ���ݴ�С
������ pOutFrame���������Ƶ
������ outFrameSize��������Ƶ�ռ�Ĵ�С
����ֵ���ɹ� �������Ƶ���ݴ�С��ʧ�� ������
*************************************************/
int SpeexEncode(short *pInFrame, int inFrameSize, char *pOutFrame, int outFrameSize)
{
	if (inFrameSize != encInFrameSize)
		return SPEEX_INFRAME_SIZE_ERR;
	speex_bits_reset(&encBits);
	speex_encode_int(encState, pInFrame, &encBits);
	int nbBytes = speex_bits_nbytes(&encBits);
	if (outFrameSize < nbBytes)
		return SPEEX_OUTFRAME_SIZE_ERR;
	return speex_bits_write(&encBits, pOutFrame, nbBytes);
}

static void * decState = NULL;
static SpeexBits decBits;
static spx_int32_t decOutFrameSize = 0;

/*******************************************
��ʼ������
����: frHz ����Ƶ��
����ֵ �� �ɹ�0��ʧ�� ������
*******************************************/
int InitSpeexDecode(int frHz)
{
	switch (frHz)
	{
		case 8000:
			decState = speex_decoder_init(speex_lib_get_mode(SPEEX_MODEID_NB));
			break;
		case 16000:
			decState = speex_decoder_init(speex_lib_get_mode(SPEEX_MODEID_WB));
			break;
		case 32000:
			decState = speex_decoder_init(speex_lib_get_mode(SPEEX_MODEID_UWB));
			break;
		default:
			return SPEEX_SAMPLING_FREQUENCY_ERR;
	}
	if (decState == NULL)
		return SPEEX_ENCODER_INIT_ERR;

	//����֪����ǿ
	int tmp = 1;
	if (speex_decoder_ctl(decState, SPEEX_SET_ENH, &tmp) != 0)
		return SPEEX_UNKNOW_ERROR;
	if (speex_decoder_ctl(decState, SPEEX_GET_FRAME_SIZE, &decOutFrameSize) != 0)
		return SPEEX_GET_FRAME_SIZE_ERR;
	speex_bits_init(&decBits);
	return SPEEX_SUCCEED;
}

/************************************
�ͷŽ�����Դ
************************************/
void ReleaseSpeexDecode()
{
	if (decState != NULL)
	{
		speex_decoder_destroy(decState);
		decState = NULL;
	}
	speex_bits_destroy(&decBits);
}

/*************************************************
������Ƶ
������ pInFrame����ѹ����Ƶ
������ inFrameSize������Ƶ���ݴ�С
������ pOutFrame���������Ƶ
������ outFrameSize��������Ƶ�ռ�Ĵ�С
����ֵ���ɹ�0��ʧ�� ������
*************************************************/
int SpeexDecode(char *pInFrame, int inFrameSize, short *pOutFrame, int outFrameSize)
{
	speex_bits_read_from(&decBits, pInFrame, inFrameSize);
	int nbBytes = speex_bits_nbytes(&decBits);
	if (outFrameSize < nbBytes )
		return SPEEX_OUTFRAME_SIZE_ERR;
	speex_decode_int(decState, &decBits, pOutFrame);
	return SPEEX_SUCCEED;
}
