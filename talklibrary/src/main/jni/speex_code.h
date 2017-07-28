#ifndef __SPEEX_CODE_H__
#define __SPEEX_CODE_H__

#include "include/speex/speex.h"

#define	LIBEXPORT_API extern "C" __declspec(dllexport)

#define SPEEX_SUCCEED					(0)		//�ɹ�
#define SPEEX_UNKNOW_ERROR				(-1)	//δ֪����
#define SPEEX_SAMPLING_FREQUENCY_ERR	(-2)	//����Ƶ�ʴ���
#define SPEEX_ENCODER_INIT_ERR			(-3)	//��ʼ��ʧ��
#define SPEEX_GET_FRAME_SIZE_ERR		(-4)	//��ȡ�����뻺������С����
#define SPEEX_INFRAME_SIZE_ERR			(-5)	//��Ƶ�����С����
#define SPEEX_OUTFRAME_SIZE_ERR			(-6)

/*******************************************
��ʼ������
����: frHz ����Ƶ��
����ֵ �� �ɹ�0��ʧ�� ������
*******************************************/
 int InitSpeexEncode(int frHz);

/************************************
�ͷű�����Դ
************************************/
 void ReleaseSpeexEncode();

/*************************************************
������Ƶ
������ pInFrame ����ԭʼ��Ƶ
������ inFrameSize ������Ƶ���ݴ�С
������ pOutFrame ���������Ƶ
������ outFrameSize ��������Ƶ�ռ�Ĵ�С
����ֵ���ɹ� �������Ƶ���ݴ�С��ʧ�� ������
*************************************************/
 int SpeexEncode(short *pInFrame, int inFrameSize, char *pOutFrame, int outFrameSize);

/*******************************************
��ʼ������
����: frHz ����Ƶ��
����ֵ �� �ɹ�0��ʧ�� ������
*******************************************/
 int InitSpeexDecode(int frHz);

/************************************
�ͷŽ�����Դ
************************************/
 void ReleaseSpeexDecode();

/*************************************************
������Ƶ
������ pInFrame ����ѹ����Ƶ
������ inFrameSize ������Ƶ���ݴ�С
������ pOutFrame ���������Ƶ
������ outFrameSize ��������Ƶ�ռ�Ĵ�С
����ֵ���ɹ�0��ʧ�� ������
*************************************************/
 int SpeexDecode(char *pInFrame, int inFrameSize, short *pOutFrame, int outFrameSize);

#endif