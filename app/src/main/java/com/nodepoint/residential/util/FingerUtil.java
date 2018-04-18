package com.nodepoint.residential.util;

import android.content.Context;
import android.os.SystemClock;

import java.util.Arrays;

public class FingerUtil implements IUsbConnState{
    public static final byte[] COMMAND_FINGER_NOTDETECT={(byte)0xAA,(byte)0x55,(byte)0x13,(byte)0x01,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
    public static final byte[] COMMAND_FINGER_DETECT={(byte)0xAA,(byte)0x55,(byte)0x13,(byte)0x01,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x01};
    public static final byte[] COMMAND_FINGER_RETRIEVED={(byte)0x5A,(byte)0xA5,(byte)0x1A,(byte)0x01,(byte)0x04};

    // Packet Prefix
    public static final int    CMD_PREFIX_CODE						     = (0xAA55);
    public static final int    RCM_PREFIX_CODE						     = (0x55AA);
    public static final int    CMD_DATA_PREFIX_CODE					 = (0xA55A);
    public static final int    RCM_DATA_PREFIX_CODE					 = (0x5AA5);

    // Command
    public static final int    CMD_VERIFY_CODE						     = (0x0101);
    public static final int    CMD_IDENTIFY_CODE					     = (0x0102);
    public static final int    CMD_ENROLL_CODE						     = (0x0103);
    public static final int    CMD_ENROLL_ONETIME_CODE				 = (0x0104);
    public static final int    CMD_CLEAR_TEMPLATE_CODE				 = (0x0105);
    public static final int    CMD_CLEAR_ALLTEMPLATE_CODE			 = (0x0106);
    public static final int    CMD_GET_EMPTY_ID_CODE				     = (0x0107);
    public static final int    CMD_GET_BROKEN_TEMPLATE_CODE			 = (0x0109);
    public static final int    CMD_READ_TEMPLATE_CODE				 = (0x010A);
    public static final int    CMD_WRITE_TEMPLATE_CODE				 = (0x010B);
    public static final int    CMD_GET_FW_VERSION_CODE				 = (0x0112);
    public static final int    CMD_FINGER_DETECT_CODE				 = (0x0113);
    public static final int    CMD_FEATURE_OF_CAPTURED_FP_CODE		 = (0x011A);
    public static final int    CMD_IDENTIFY_TEMPLATE_WITH_FP_CODE	 = (0x011C);
    public static final int    CMD_SLED_CTRL_CODE					     = (0x0124);
    public static final int    CMD_IDENTIFY_FREE_CODE				 = (0x0125);
    public static final int    CMD_SET_DEVPASS_CODE					 = (0x0126);
    public static final int    CMD_VERIFY_DEVPASS_CODE				 = (0x0127);
    public static final int    CMD_GET_ENROLL_COUNT_CODE			     = (0x0128);
    public static final int    CMD_CHANGE_TEMPLATE_CODE				 = (0x0129);
    public static final int    CMD_UP_IMAGE_CODE				         = (0x012C);
    public static final int    CMD_VERIFY_WITH_DOWN_TMPL_CODE		 = (0x012D);
    public static final int    CMD_IDENTIFY_WITH_DOWN_TMPL_CODE		 = (0x012E);
    public static final int    CMD_FP_CANCEL_CODE					     = (0x0130);
    public static final int    CMD_ADJUST_SENSOR_CODE				 = (0x0137);
    public static final int    CMD_IDENTIFY_WITH_IMAGE_CODE			 = (0x0138);
    public static final int    CMD_VERIFY_WITH_IMAGE_CODE			 = (0x0139);
    public static final int    CMD_SET_PARAMETER_CODE				 = (0x013A);
    public static final int    CMD_EXIT_DEVPASS_CODE				     = (0x013B);
    public static final int    CMD_TEST_CONNECTION_CODE				 = (0x0150);
    public static final int	   CMD_ENTERSTANDBY_CODE				     = (0x0155);
    public static final int    RCM_INCORRECT_COMMAND_CODE			 = (0x0160);
    public static final int    CMD_ENTER_ISPMODE_CODE              	 = (0x0171);

    // Error Code
    public static final int  ERR_SUCCESS                              = (0);
    public static final int	ERR_FAIL					                 = (1);
    public static final int	ERR_CONTINUE				                 = (2);
    public static final int  ERR_COMM_FAIL						         = (3);
    public static final int	ERR_VERIFY					                 = (0x11);
    public static final int	ERR_IDENTIFY				                 = (0x12);
    public static final int	ERR_TMPL_EMPTY				             = (0x13);
    public static final int	ERR_TMPL_NOT_EMPTY			             = (0x14);
    public static final int	ERR_ALL_TMPL_EMPTY			             = (0x15);
    public static final int	ERR_EMPTY_ID_NOEXIST		                 = (0x16);
    public static final int	ERR_BROKEN_ID_NOEXIST		             = (0x17);
    public static final int	ERR_INVALID_TMPL_DATA		             = (0x18);
    public static final int	ERR_DUPLICATION_ID			             = (0x19);
    public static final int	ERR_TOO_FAST				                 = (0x20);
    public static final int	ERR_BAD_QUALITY				             = (0x21);
    public static final int	ERR_SMALL_LINES				             = (0x22);
    public static final int	ERR_TIME_OUT				                 = (0x23);
    public static final int	ERR_NOT_AUTHORIZED			             = (0x24);
    public static final int	ERR_GENERALIZE				             = (0x30);
    public static final int	ERR_COM_TIMEOUT				             = (0x40);
    public static final int	ERR_FP_CANCEL				                 = (0x41);
    public static final int	ERR_INTERNAL				                 = (0x50);
    public static final int	ERR_MEMORY					                 = (0x51);
    public static final int	ERR_EXCEPTION				                 = (0x52);
    public static final int	ERR_INVALID_TMPL_NO			             = (0x60);
    public static final int	ERR_INVALID_PARAM			                 = (0x70);
    public static final int	ERR_NO_RELEASE				             = (0x71);
    public static final int	ERR_INVALID_OPERATION_MODE	             = (0x72);
    public static final int    ERR_NOT_SET_PWD				             = (0x74);
    public static final int	ERR_FP_NOT_DETECTED			             = (0x75);
    public static final int	ERR_ADJUST_SENSOR			                 = (0x76);

    // Return Value
    public static final int	GD_DETECT_FINGER			                 = (0x01);
    public static final int	GD_NO_DETECT_FINGER			             = (0x00);

    // Packet
    public static final int    MAX_DATA_LEN                             = (600); /*512*/
    public static final int    CMD_PACKET_LEN                           = (22);
    public static final int    ST_COMMAND_LEN                           = (66);
    public static final int		IMAGE_RECEIVE_UINT				     = (498);
    public static final int      DATA_SPLIT_UNIT                       = (498);
    public static final int     ID_USER_TEMPLATE_SIZE			         = (498);

    // Template
    public static final int		GD_MAX_RECORD_COUNT				     = (5000);
    public static final int		GD_TEMPLATE_SIZE				         = (570);
    public static final int		GD_RECORD_SIZE					     = (GD_TEMPLATE_SIZE);// + 2)	// CkeckSum len = 2
    public static final int      GD_MAX_RECORD_SIZE                    = (900);

    //--------------- For Usb Communication ------------//
    public static final int		SCSI_TIMEOUT					         = (3000);
    public static final int		GD_MAX_FP_TIME_OUT				     = (60);
    public static final int		COMM_SLEEP_TIME					     = (100);
    public static final int		ONCE_UP_IMAGE_UINT				     = (60000);
    public static final int		COMM_TIMEOUT					         = (15000);

    public int messageBufferSize;
    public byte[] messageBuffer = new byte[64*1024];
    public byte[] tempMessageBuffer = new byte[64 * 1024];
    //--------------------------------------------------//

    private static final int VID = 0x2009;
    private static final int PID = 0x7638;

    public static boolean sendMessageStatus = false;
    public static boolean isCommandRunning=false;
    public static boolean sendCommandResult=false;

    // USB
    private UsbController usbController;

    public FingerUtil(Context context, IUsbConnState usbConnState){
        usbController = new UsbController(context,this,VID,PID);
    }

    public boolean isInit(){
        return usbController.IsInit();
    }

    public boolean connect(){
        if (!usbController.IsInit()) {
            usbController.init();
        }
        if (!usbController.IsInit()) {
            return false;
        }else{
            if (Run_TestConnection() ==  (short)ERR_SUCCESS){
                if (Run_GetDeviceInfo() == (short)ERR_SUCCESS){
                    return true;
                }else{
                    return false;
                }
            }else{
                return false;
            }
        }
    }

    public void close(){
        usbController.uninit();
    }

    /************************************************************************/
    int	Run_TestConnection()
    {
        boolean	w_bRet;

        InitPacket((short)CMD_TEST_CONNECTION_CODE, true);
        AddCheckSum(true);

        w_bRet = Send_Command((short)CMD_TEST_CONNECTION_CODE);

        if(!w_bRet)
        {
            return ERR_COMM_FAIL;
        }

        if (GetRetCode() != ERR_SUCCESS)
        {
            return ERR_FAIL;
        }

        return ERR_SUCCESS;
    }
    /************************************************************************/
    /************************************************************************/
    int Run_GetDeviceInfo()
    {
        return ERR_SUCCESS;
    }

    public boolean sendMessage(byte[] command, byte[] data){
        int dataLen;
        command[0] = (byte)0xFF;
        command[1] = 0x01;
        dataLen = (int)((((command[5] & 0xFF) << 8) & 0x0000FF00) | (command[4] & 0x000000FF));
        return usbController.UsbSCSIWrite(command, 6, data, dataLen, 5000);
    }

    private  boolean receiveMessage(byte[] pData, int[] pLevRen){
        int    w_nLen;
        byte[] w_abyPCCmd = new byte[6];
        byte[] w_abyRespond = new byte[4];
        boolean w_bRet;

        w_abyPCCmd[0] = (byte)0xEF;
        w_abyPCCmd[1] = 0x02;
        w_abyPCCmd[2] = 0;
        w_abyPCCmd[3] = 0;
        w_abyPCCmd[4] = 0;
        w_abyPCCmd[5] = 0;

        // receive status
        w_bRet = usbController.UsbSCSIRead(w_abyPCCmd, 6, w_abyRespond, 4, 5000);

        if (!w_bRet)
            return false;

        // receive data
        //w_nLen = (int)((w_abyRespond[3] << 8) | w_abyRespond[2]);
        w_nLen = (int)((int)((w_abyRespond[3] << 8) & 0x0000FF00) | (int)(w_abyRespond[2] & 0x000000FF));

        if (w_nLen > 0)
        {
            //w_nTime = SystemClock.elapsedRealtime();

            w_abyPCCmd[1] = 0x03;
            w_bRet = usbController.UsbSCSIRead(w_abyPCCmd, 6, pData, w_nLen, 5000);

            //w_nTime = SystemClock.elapsedRealtime() - w_nTime;

            if (!w_bRet)
                return false;

            pLevRen[0] = w_nLen;
        }

        return  true;
    }

    /***************************************************************************
     * Get Return Code
     ***************************************************************************/
    public short GetRetCode()
    {
        return (short)((int)((messageBuffer[7] << 8) & 0x0000FF00) | (int)(messageBuffer[6] & 0x000000FF));
    }

    /***************************************************************************
     * Get Data Length
     ***************************************************************************/
    public short GetDataLen()
    {
        return (short)(((messageBuffer[5] << 8) & 0x0000FF00) | (messageBuffer[4] & 0x000000FF));
    }

    /***************************************************************************
     * Set Data Length
     ***************************************************************************/
    public void SetDataLen(short p_wDataLen)
    {
        messageBuffer[4] = (byte)(p_wDataLen & 0xFF);
        messageBuffer[5] = (byte)(((p_wDataLen & 0xFF00) >> 8) & 0xFF);
    }


    /***************************************************************************
     * Set Command Data
     ***************************************************************************/
    public void SetCmdData(short p_wData, boolean p_bFirst)
    {
        if (p_bFirst)
        {
            messageBuffer[6] = (byte)(p_wData & 0xFF);
            messageBuffer[7] = (byte)(((p_wData & 0xFF00) >> 8) & 0xFF);
        }
        else
        {
            messageBuffer[8] = (byte)(p_wData & 0xFF);
            messageBuffer[9] = (byte)(((p_wData & 0xFF00) >> 8) & 0xFF);
        }
    }

    /***************************************************************************
     * Get Command Data
     ***************************************************************************/
    public short GetCmdData(boolean p_bFirst)
    {
        if (p_bFirst)
        {
            return (short)(((messageBuffer[7] << 8) & 0x0000FF00) | (messageBuffer[6] & 0x000000FF));
        }
        else
        {
            return (short)(((messageBuffer[9] << 8) & 0x0000FF00) | (messageBuffer[8] & 0x000000FF));
        }
    }

    /***************************************************************************
     * Get Data Packet Length
     ***************************************************************************/
    private short GetDataPacketLen()
    {
        return (short)(((messageBuffer[5] << 8) & 0x0000FF00) | (messageBuffer[4] & 0x000000FF) + 6);
    }

    /***************************************************************************
     * Make Packet
     ***************************************************************************/
    void InitPacket(short p_wCmd, boolean p_bCmdData)
    {
        memset(messageBuffer, (byte)0, CMD_PACKET_LEN);

        //g_pPacketBuffer->wPrefix = p_bCmdData?CMD_PREFIX_CODE:CMD_DATA_PREFIX_CODE;
        if (p_bCmdData)
        {
            messageBuffer[0] = (byte)(CMD_PREFIX_CODE & 0xFF);
            messageBuffer[1] = (byte)(((CMD_PREFIX_CODE & 0xFF00) >> 8) & 0xFF);
        }
        else
        {
            messageBuffer[0] = (byte)(CMD_DATA_PREFIX_CODE & 0xFF);
            messageBuffer[1] = (byte)(((CMD_DATA_PREFIX_CODE & 0xFF00) >> 8) & 0xFF);
        }

        //g_pPacketBuffer->wCMD_RCM = p_wCMD;
        messageBuffer[2] = (byte)(p_wCmd & 0xFF);
        messageBuffer[3] = (byte)(((p_wCmd & 0xFF00) >> 8) & 0xFF);
    }

    /***************************************************************************
     * Get CheckSum
     ***************************************************************************/
    short GetCheckSum(boolean p_bCmdData)
    {
        short w_wRet = 0;
        short w_nI = 0;

        w_wRet = 0;
        if (p_bCmdData)
        {
            for (w_nI = 0; w_nI < CMD_PACKET_LEN; w_nI ++)
                w_wRet += (messageBuffer[w_nI] & 0xFF);
        }
        else
        {
            for (w_nI = 0; w_nI < GetDataPacketLen(); w_nI ++)
                w_wRet += (messageBuffer[w_nI] & 0xFF);
        }
        return w_wRet;
    }

    /***************************************************************************
     * Set CheckSum
     ***************************************************************************/
    short AddCheckSum(boolean p_bCmdData)
    {
        short w_wRet = 0;
        short w_wLen = 0;
        int w_nI;

        if (p_bCmdData)
            w_wLen = CMD_PACKET_LEN;
        else
            w_wLen = GetDataPacketLen();

        w_wRet = 0;
        for (w_nI = 0; w_nI < w_wLen; w_nI ++)
            w_wRet += (messageBuffer[w_nI] & 0xFF);

        messageBuffer[w_wLen] = (byte)(w_wRet & 0xFF);
        messageBuffer[w_wLen + 1] = (byte)(((w_wRet & 0xFF00) >> 8) & 0xFF);

        return w_wRet;
    }

    /***************************************************************************
     * Check Packet
     ***************************************************************************/
    boolean CheckReceive(short p_wPrefix, short p_wCmd)
    {
        short	w_wCheckSum;
        short	w_wTmpPrefix;
        short	w_wTmpCmd;
        short	w_wLen;

        // Check Prefix Code
        w_wTmpPrefix = (short)(((messageBuffer[1] << 8) & 0x0000FF00) | (messageBuffer[0] & 0x000000FF));
        w_wTmpCmd = (short)(((messageBuffer[3] << 8) & 0x0000FF00) | (messageBuffer[2] & 0x000000FF));

//    	if ( g_pPacketBuffer->wCMD_RCM != CMD_FP_CANCEL_CODE )
        {
            if ((p_wPrefix != w_wTmpPrefix) || (p_wCmd != w_wTmpCmd))
            {
                return false;
            }
        }

        if (p_wPrefix == RCM_PREFIX_CODE)
            w_wLen = CMD_PACKET_LEN;
        else
            w_wLen = GetDataPacketLen();

        w_wCheckSum = (short)(((messageBuffer[w_wLen + 1] << 8) & 0x0000FF00) | (messageBuffer[w_wLen] & 0x000000FF));

        if (w_wCheckSum != GetCheckSum(p_wPrefix == RCM_PREFIX_CODE))
        {
            return false;
        }
        return true;
    }

    //--------------------------- Send, Receive Communication Packet Functions ---------------------//
    public boolean Send_Command(short p_wCmd)
    {
        return USB_SendPacket(p_wCmd);
    }
    /***************************************************************************/
    /***************************************************************************/
    public boolean Send_DataPacket(short p_wCmd)
    {
        return USB_SendDataPacket(p_wCmd);
    }
    /***************************************************************************/
    /***************************************************************************/
    public boolean Receive_DataPacket(short p_wCmd)
    {
        return USB_ReceiveDataPacket(p_wCmd);

    }
    //------------------------------------------ USB Functions -------------------------------------//
    public  boolean USB_SendPacket(short wCMD)
    {
        byte[]	btCDB = new byte[8];
        boolean	w_bRet;

        Arrays.fill(btCDB, (byte)0);

        btCDB[0] = (byte)0xEF; btCDB[1] = 0x11; btCDB[4] = CMD_PACKET_LEN + 2;

        while (sendMessageStatus)
        {
            SystemClock.sleep(1);
        }
        sendMessageStatus = true;
        w_bRet = usbController.UsbSCSIWrite(btCDB, 8, messageBuffer, (int)(CMD_PACKET_LEN + 2), SCSI_TIMEOUT);
        sendMessageStatus = false;

        if (!w_bRet)
            return false;

        return USB_ReceiveAck( wCMD );
    }

    /***************************************************************************/
    /***************************************************************************/
    public boolean USB_ReceiveAck(short p_wCmd)
    {
        int		w_nLen;
        byte[]	btCDB = new byte[8];
        byte[]	w_abyWaitPacket = new byte[CMD_PACKET_LEN + 2];
        int	w_dwTimeOut = SCSI_TIMEOUT;

        if (p_wCmd == CMD_VERIFY_CODE ||
                p_wCmd == CMD_IDENTIFY_CODE ||
                p_wCmd == CMD_IDENTIFY_FREE_CODE ||
                p_wCmd == CMD_ENROLL_CODE ||
                p_wCmd == CMD_ENROLL_ONETIME_CODE)
            w_dwTimeOut = (GD_MAX_FP_TIME_OUT + 1)*(1000);

        Arrays.fill(btCDB, (byte)0);

        //w_nReadCount = GetReadWaitTime(p_byCMD);

        Arrays.fill(w_abyWaitPacket, (byte)0xAF);

        do
        {
            Arrays.fill(messageBuffer, (byte)0);

            btCDB[0] = (byte)0xEF; btCDB[1] = (byte)0x12;

            w_nLen = CMD_PACKET_LEN + 2;

            if (!usbController.UsbSCSIRead(btCDB, 8, messageBuffer, w_nLen, w_dwTimeOut))
                return false;

            SystemClock.sleep(COMM_SLEEP_TIME);
        } while ( memcmp(messageBuffer, w_abyWaitPacket, CMD_PACKET_LEN + 2) == true );

        messageBufferSize = w_nLen;

        if (!CheckReceive((short)RCM_PREFIX_CODE, p_wCmd))
            return false;

        return true;
    }

    /***************************************************************************/
    /***************************************************************************/
    boolean USB_ReceiveDataAck(short p_wCmd)
    {
        byte[]	btCDB = new byte[8];
        byte[]	w_WaitPacket = new byte[8];
        int		w_nLen;
        int	w_dwTimeOut = COMM_TIMEOUT;

        if (p_wCmd == CMD_VERIFY_CODE ||
                p_wCmd == CMD_IDENTIFY_CODE ||
                p_wCmd == CMD_IDENTIFY_FREE_CODE ||
                p_wCmd == CMD_ENROLL_CODE ||
                p_wCmd == CMD_ENROLL_ONETIME_CODE)
            w_dwTimeOut = (GD_MAX_FP_TIME_OUT + 1)*(1000);

        memset(btCDB, (byte)0, 8);
        memset(w_WaitPacket, (byte)0xAF, 8);
        Arrays.fill(tempMessageBuffer, (byte)0);

        do
        {
            btCDB[0] = (byte)0xEF; btCDB[1] = 0x15;
            w_nLen = 6;

            if (!usbController.UsbSCSIRead(btCDB, 8, messageBuffer, w_nLen, w_dwTimeOut))
            {
                return false;
            }

            SystemClock.sleep(COMM_SLEEP_TIME);
        }while(memcmp(messageBuffer, w_WaitPacket, 6) == true);

        do
        {
            w_nLen = GetDataLen() + 2;
            if (USB_ReceiveRawData(tempMessageBuffer, w_nLen) == false) {
                return false;
            }
            System.arraycopy(tempMessageBuffer, 0, messageBuffer, 6, w_nLen);
            SystemClock.sleep(COMM_SLEEP_TIME);
        } while(memcmp(messageBuffer, w_WaitPacket, 4) == true);

        if (!CheckReceive((short)RCM_DATA_PREFIX_CODE, p_wCmd)) {
            return false;
        }

        return true;
    }
    /***************************************************************************/
    /***************************************************************************/
    boolean USB_SendDataPacket(short wCMD)
    {
        byte[]	btCDB = new byte[8];
        short	w_wLen = (short)(GetDataPacketLen() + 2);

        memset(btCDB, (byte)0, 8);

        btCDB[0] = (byte)0xEF; btCDB[1] = 0x13;

        btCDB[4] = (byte)(w_wLen & 0xFF);
        btCDB[5] = (byte)(((w_wLen & 0xFF00) >> 8) & 0xFF);

        if (!usbController.UsbSCSIWrite(btCDB, 8, messageBuffer, GetDataPacketLen() + 2, SCSI_TIMEOUT ) )
            return false;

        return USB_ReceiveDataAck(wCMD);
    }
    /***************************************************************************/
    /***************************************************************************/
    public boolean USB_ReceiveDataPacket(short wCMD)
    {
        return USB_ReceiveDataAck(wCMD);
    }
    /***************************************************************************/
    /***************************************************************************/
    boolean USB_ReceiveRawData(byte[] pBuffer, int nDataLen)
    {
        int     w_nDataCnt = nDataLen;
        byte[]  btCDB = new byte[8];

        memset(btCDB, (byte)0, 8);
        btCDB[0] = (byte)0xEF; btCDB[1] = (byte)0x14;
        if (!usbController.UsbSCSIRead(btCDB, 8, pBuffer, w_nDataCnt, SCSI_TIMEOUT)) {
            return false;
        }

        return true;
    }
    /***************************************************************************/
    /***************************************************************************/
    public boolean USB_ReceiveImage(byte[] p_pBuffer, int p_dwDataLen )
    {
        byte[]	btCDB = new byte[8];
        byte[]	w_WaitPacket = new byte[8];
        int w_nI;
        int w_nIndex;
        int w_nRemainCount;
        byte[]	w_pTmpImgBuf = new byte[ONCE_UP_IMAGE_UINT];

        memset( btCDB, (byte)0, 8 );
        memset( w_WaitPacket, (byte)0xAF, 8 );

        if (p_dwDataLen == 208*288 || p_dwDataLen == 242*266 || p_dwDataLen == 202*258 || p_dwDataLen == 256*288)
        {
            w_nIndex = 0;
            w_nRemainCount = p_dwDataLen;
            w_nI = 0;
            while (w_nRemainCount > ONCE_UP_IMAGE_UINT)
            {
                btCDB[0] = (byte)0xEF; btCDB[1] = 0x16; btCDB[2] = (byte)(w_nI & 0xFF);
                if (!usbController.UsbSCSIRead(btCDB, 8, w_pTmpImgBuf, ONCE_UP_IMAGE_UINT, SCSI_TIMEOUT))
                    return false;
                System.arraycopy(w_pTmpImgBuf, 0, p_pBuffer, w_nIndex, ONCE_UP_IMAGE_UINT);
                w_nRemainCount -= ONCE_UP_IMAGE_UINT;
                w_nIndex += ONCE_UP_IMAGE_UINT;
                w_nI ++;
            }
            btCDB[0] = (byte)0xEF; btCDB[1] = 0x16; btCDB[2] = (byte)(w_nI & 0xFF);
            if (!usbController.UsbSCSIRead(btCDB, 8, w_pTmpImgBuf, w_nRemainCount, SCSI_TIMEOUT))
                return false;
            System.arraycopy(w_pTmpImgBuf, 0, p_pBuffer, w_nIndex, w_nRemainCount);
        }

        return true;
    }
    /***************************************************************************/
    /***************************************************************************/
    public boolean USB_DownImage(byte[] pBuf, int nBufLen)
    {
        byte[]	w_pImgBuf = new byte[ONCE_UP_IMAGE_UINT];
        int		w_nI;
        int		w_nIndex = 0;
        int		w_nRemainCount;
        byte[]	btCDB = new byte[8];

        w_nIndex = 0;
        w_nRemainCount = nBufLen;
        w_nI = 0;
        memset(btCDB, (byte)0, 8);

        while (w_nRemainCount > ONCE_UP_IMAGE_UINT)
        {
            btCDB[0] = (byte)0xEF; btCDB[1] = 0x17; btCDB[2] = (byte)(w_nI & 0xFF);
            btCDB[3] = LOBYTE((short)(ONCE_UP_IMAGE_UINT & 0x00FF));
            btCDB[4] = HIBYTE((short)(ONCE_UP_IMAGE_UINT & 0xFF00));

            System.arraycopy(pBuf, w_nIndex, w_pImgBuf, 0, ONCE_UP_IMAGE_UINT);
            if (!usbController.UsbSCSIRead(btCDB, 6, w_pImgBuf,ONCE_UP_IMAGE_UINT, SCSI_TIMEOUT))
                return false;

            w_nRemainCount -= ONCE_UP_IMAGE_UINT;
            w_nIndex += ONCE_UP_IMAGE_UINT;
            w_nI ++;
        }

        btCDB[0] = (byte)0xEF; btCDB[1] = 0x17; btCDB[2] = (byte)(w_nI & 0xFF);
        btCDB[3] = LOBYTE((short)(w_nRemainCount & 0x00FF));
        btCDB[4] = HIBYTE((short)(w_nRemainCount & 0xFF00));

        if (!usbController.UsbSCSIWrite(btCDB, 6, w_pImgBuf, w_nRemainCount, SCSI_TIMEOUT))
            return false;

        return true;
    }

    /***************************************************************************/
    /***************************************************************************/
    public boolean memcmp(byte[] p1, byte[] p2, int nLen)
    {
        int		i;

        for (i=0; i<nLen; i++)
        {
            if (p1[i] != p2[i])
                return false;
        }

        return true;
    }

    public void memset(byte[] p1, byte nValue, int nLen)
    {
        Arrays.fill(p1, 0, nLen, nValue);
    }

    public void memcpy(byte[] p1, byte nValue, int nLen)
    {
        Arrays.fill(p1, 0, nLen, nValue);
    }

    public short MAKEWORD(byte low, byte high)
    {
        short s;
        s = (short)((((high & 0x00FF) << 8) & 0x0000FF00) | (low & 0x000000FF));
        return s;
    }

    public byte LOBYTE(short s)
    {
        return (byte)(s & 0xFF);
    }

    public byte HIBYTE(short s)
    {
        return (byte)(((s & 0xFF00) >> 8) & 0xFF);
    }

    public void startScan(){
        while (isCommandRunning)
        {
            SystemClock.sleep(1);
        }
        new Thread(new Runnable() {
            public void run() {
                while(true){
                    sendCommandMessage((short)CMD_FINGER_DETECT_CODE,(short)0x00);
                    if(isFingerDetected()){
                        sendCommandMessage((short)CMD_FEATURE_OF_CAPTURED_FP_CODE,(short)0x00);
                        if(isFingerRetrieved()){
                            retrieveFingerData();
                        }
                    }
                }
            }
        }).start();
    }

    public void retrieveFingerData(){
        USB_ReceiveDataPacket((short)CMD_FEATURE_OF_CAPTURED_FP_CODE);
        if(isHeadSame(messageBuffer,COMMAND_FINGER_RETRIEVED)){
            byte[] fingerData=new byte[570];
            for(int i=0;i<570;i++){
                fingerData[i]=messageBuffer[i+8];
            }
            onFinger(fingerData);
        }
    }

    public void onFinger(byte[] fingerData){
        for(int i=0;i<570;i++){
            System.out.print(fingerData[i]+" ");
        }
    }

    protected boolean isFingerRetrieved(){
        return isHeadSame(messageBuffer,COMMAND_FINGER_RETRIEVED);
    }

    protected boolean isFingerDetected(){
        return isHeadSame(messageBuffer,COMMAND_FINGER_DETECT);
    }


    public boolean isHeadSame(byte[] from,byte[] to){
        boolean result=true;
        for(int i=0;i<to.length;i++){
            if(from[i]!=to[i]){
                result=false;
                break;
            }
        }
        return result;
    }

    public boolean isSame(byte[] from,byte[] to){
        boolean result=true;
        if(from.length==to.length){
            for(int i=0;i<from.length;i++){
                if(from[i]!=to[i]){
                    result=false;
                    break;
                }
            }
        }else{
            result=false;
        }
        return result;
    }

    public boolean sendCommandMessage(short command, short data){
        InitPacket(command, true);
        SetDataLen((short)0x0002);
        SetCmdData(data, true);
        AddCheckSum(true);
        return USB_SendPacket(command);
    }

//    public void startSendMessage(final short command)
//    {
//                boolean commandResult=false;
//                short commandPrefix = 0;
//                isCommandRunning = true;
//                commandPrefix = (short)(((messageBuffer[1] << 8) & 0x0000FF00) | (messageBuffer[0] & 0x000000FF));
//                if (commandPrefix == (short)(CMD_PREFIX_CODE)){
//                    commandResult = USB_SendPacket(command);
//                }else if (commandPrefix == (short)(CMD_DATA_PREFIX_CODE)){
//                    commandResult = USB_SendDataPacket(command);
//                }else{
//                    if (command != (short)(CMD_FEATURE_OF_CAPTURED_FP_CODE)){
//                        commandResult = USB_ReceiveAck(command);
//                    }else{
//                        commandResult = USB_ReceiveDataPacket((short)CMD_FEATURE_OF_CAPTURED_FP_CODE);
//                    }
//                }
//                sendCommandResult = commandResult;
//                isCommandRunning = false;
//
//    }

    @Override
    public void onUsbConnected() {
        System.out.println("connected");
        if (Run_TestConnection() == (short)ERR_SUCCESS)
        {
            if (Run_GetDeviceInfo() == (short)ERR_SUCCESS)
            {
                System.out.println("test connected OK");
            }
        }
        else
        {
            System.out.println("test connected ERROR");
        }
    }

    @Override
    public void onUsbPermissionDenied() {
        System.out.println("Permission Denied");
    }

    @Override
    public void onDeviceNotFound() {
        System.out.println("not found");
    }
}
