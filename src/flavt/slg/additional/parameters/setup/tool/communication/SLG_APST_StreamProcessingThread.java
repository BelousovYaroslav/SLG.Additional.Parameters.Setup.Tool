/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flavt.slg.additional.parameters.setup.tool.communication;

import flavt.slg.additional.parameters.setup.tool.main.SLG_APST_App;
import flavt.slg.lib.constants.SLG_ConstantsParams;

/**
 *
 * @author yaroslav
 */
public class SLG_APST_StreamProcessingThread implements Runnable {
    SLG_APST_App theApp;
    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger( SLG_APST_StreamProcessingThread.class);

    public boolean m_bRunning;
    public boolean m_bStopThread;
    
    public SLG_APST_StreamProcessingThread( SLG_APST_App app) {
        theApp = app;
        
        m_bRunning = false;
        m_bStopThread = false;
    }
    
    @Override
    public void run() {
        m_bRunning = true;
        m_bStopThread = false;
        
        theApp.m_nMarkerFails = 0;
        theApp.m_nCounterFails = 0;
        theApp.m_nCheckSummFails = 0;
        theApp.m_nPacksCounter = 0;
        
    
        
        boolean bMarkerFailOnce = false;
        do {
            
            int nMarkerCounter = 0;
            do {
                if( theApp.m_bfCircleBuffer.getReadyIncomingDataLen() > 20) {
                    byte [] bts = new byte[1];
                    theApp.m_bfCircleBuffer.getAnswer( 1, bts);

                    String tmps = String.format( "BT: 0x%02X", bts[0]);
                    logger.trace( tmps);
                    
                    tmps = "BEFORE: " + nMarkerCounter;
                    switch( nMarkerCounter) {
                        case 0:
                            if( ( bts[0] & 0xFF) == 0x55)
                                nMarkerCounter++;
                            else
                                theApp.m_nMarkerFails++;
                        break;

                        case 1:
                            if( ( bts[0] & 0xFF) == 0xAA)
                                nMarkerCounter++;  //2! (условие выхода)
                            else {
                                nMarkerCounter = 0;
                                theApp.m_nMarkerFails++;
                            }
                        break;
                    }
                    tmps += " AFTER: " + nMarkerCounter;
                    logger.trace( tmps);
                }
                else {
                    if( m_bStopThread == true) {
                        return;
                    }
                }
            } while( nMarkerCounter != 2);
        
            if( theApp.m_bfCircleBuffer.getReadyIncomingDataLen() < 12) {
                logger.error( "После отмотки маркера в кольцевом буфере недостаточно байт пачки");
                continue;
            }
            
            byte [] bts = new byte[12];
            if( theApp.m_bfCircleBuffer.getAnswer( 12, bts) != 0) {
                logger.error( "После отмотки маркера кольцевой буфер не дал 12 байт!");
                continue;
            }
            
            //TODO: CHECKSUMM CHECK
            
            //ANALYZE ADD.PARAM DESCRIPTOR
            switch( bts[4]) {
                case SLG_ConstantsParams.SLG_PARAM_VERSION:
                    theApp.m_strVersion = String.format( "%02d.%02d.%02d", bts[5] & 0xFF, bts[6] & 0xFF, bts[7] & 0xFF);
                break;
            }
            
            theApp.m_nPacksCounter = bts[9] & 0xFF;
            
        } while( m_bStopThread == false);
    }
}
