/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flavt.slg.additional.parameters.setup.tool.main;

import flavt.slg.additional.parameters.setup.tool.communication.SLG_APST_CircleBuffer;
import flavt.slg.additional.parameters.setup.tool.communication.SLG_APST_StreamProcessingThread;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Timer;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import org.apache.log4j.Logger;

/**
 *
 * @author yaroslav
 */
public class SLG_APST_MainFrame extends javax.swing.JFrame {

    static Logger logger = Logger.getLogger( SLG_APST_MainFrame.class);
    private final SLG_APST_App theApp;
    
    Timer tRefreshStates;
    Timer tRefreshValues;
    Timer tPolling;
    
    public String m_strPort;
    public static SerialPort serialPort;
    PortReader m_evListener;
    
    SLG_APST_StreamProcessingThread thrProcessorRunnable;
    Thread thrProcessorThread;
    /**
     * Creates new form SLG_SAP_MainFrame
     */
    public SLG_APST_MainFrame( SLG_APST_App app) {
        initComponents();
        theApp = app;
        
        edtComPortValue.setText( theApp.GetSettings().GetComPort());
        
        thrProcessorRunnable = new SLG_APST_StreamProcessingThread( theApp);
        thrProcessorThread = new Thread( thrProcessorRunnable);
        
        tRefreshStates = new Timer( 200, new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e) {
                
                edtComPortValue.setEnabled( !theApp.m_bConnected);
                btnConnect.setEnabled(      !theApp.m_bConnected);
                btnDisconnect.setEnabled(   theApp.m_bConnected);
                
                //btnParam01Remove           always disabled
                JButton      btnsR[] =  {   btnParam01Remove, btnParam02Remove, btnParam03Remove, btnParam04Remove,
                                            btnParam05Remove, btnParam06Remove, btnParam07Remove, btnParam08Remove,
                                            btnParam09Remove, btnParam10Remove, btnParam11Remove, btnParam12Remove };
                
                JButton      btnsS[] =  {   btnParam01Set, btnParam02Set, btnParam03Set, btnParam04Set,
                                            btnParam05Set, btnParam06Set, btnParam07Set, btnParam08Set,
                                            btnParam09Set, btnParam10Set, btnParam11Set, btnParam12Set };
                
                JButton      btnsP[] =  {   btnParam01Plus, btnParam02Plus, btnParam03Plus, btnParam04Plus,
                                            btnParam05Plus, btnParam06Plus, btnParam07Plus, btnParam08Plus,
                                            btnParam09Plus, btnParam10Plus, btnParam11Plus, btnParam12Plus };
                
                JComboBox   cmbs[] = {      cmbParam01SetValue, cmbParam02SetValue, cmbParam03SetValue, cmbParam04SetValue,
                                            cmbParam05SetValue, cmbParam06SetValue, cmbParam07SetValue, cmbParam08SetValue,
                                            cmbParam09SetValue, cmbParam10SetValue, cmbParam11SetValue, cmbParam12SetValue };
                
                //кнопки удаления
                boolean bPrevValid = true;
                for( int i=0; i<theApp.LIST_PARAMS_LEN; i++) {
                    
                    if( i > 0) {
                        if( theApp.m_bParamDefined[i] == true) {
                            if( bPrevValid) {
                                btnsR[i].setEnabled( true);
                            }
                        }
                        else {
                            btnsR[i].setEnabled( false);
                            bPrevValid = false;
                        }
                    }
                    else
                        btnsR[i].setEnabled( false);
                }
                
                
                //комбобоксы значений
                bPrevValid = true;
                for( int i = 0; i < theApp.LIST_PARAMS_LEN; i++) {
                    
                    if( bPrevValid) {
                        
                        if( theApp.m_bParamDefined[ i] == true) {
                            
                            if( theApp.m_nParamIndex[ i] != 0xFF) {
                                cmbs[i].setEnabled( true);
                            }
                            else {
                                cmbs[i].setEnabled( false);
                                bPrevValid = false;
                            }
                            
                        }
                        else {
                            cmbs[i].setEnabled( false);
                            bPrevValid = false;
                        }
                    }
                    else {
                        cmbs[i].setEnabled( false);
                    }
                }
                
                
                //кнопки задания значений
                bPrevValid = true;
                for( int i=0; i<theApp.LIST_PARAMS_LEN; i++) {
                    
                    if( bPrevValid) {
                        if( theApp.m_bParamDefined[i] == true) {
                            if( theApp.m_nParamIndex[i] != 0xFF) {
                                btnsS[i].setEnabled( true);
                            }
                        }
                        else {
                            btnsS[i].setEnabled( false);
                            bPrevValid = false;
                        }
                    }
                    else {
                        btnsS[i].setEnabled( false);
                    }

                }
                
                
                
                //кнопки добавления
                bPrevValid = true;
                for( int i=0; i<theApp.LIST_PARAMS_LEN; i++) {
                    
                    if( bPrevValid) {
                        if( i == theApp.LIST_PARAMS_LEN - 1) {
                            btnsP[i].setEnabled( false);
                            break;
                        }

                        if( theApp.m_bParamDefined[i] == true) {
                            if( theApp.m_nParamIndex[i] != 0xFF) {
                                btnsP[i].setEnabled( true);
                            }
                        }
                        else {
                            btnsP[i].setEnabled( false);
                            bPrevValid = false;
                        }
                    }
                    else {
                        btnsP[i].setEnabled( false);
                    }

                }
                
                
            }
        });
        tRefreshStates.start();
        
        tRefreshValues = new Timer( 200, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                if( theApp.m_bConnected) {
                    String strStatus;
                    strStatus =
                            String.format( "MF:%d CF:%d CSF:%d PC:%d",
                                    theApp.m_nMarkerFails,
                                    theApp.m_nCounterFails,
                                    theApp.m_nCheckSummFails,
                                    theApp.m_nPacksCounter);
                    
                    if( !theApp.m_strVersion.isEmpty())
                        strStatus += "   Версия = " + theApp.m_strVersion;
                    
                    lblConnectionStateValue.setText( strStatus);
                }
                else {
                    lblConnectionStateValue.setText( "Нет соединения");
                }
                
                JLabel      vals[] =  {  lblParam01Value, lblParam02Value, lblParam03Value, lblParam04Value,
                                        lblParam05Value, lblParam06Value, lblParam07Value, lblParam08Value,
                                        lblParam09Value, lblParam10Value, lblParam11Value, lblParam12Value };
                
                /*
                JComboBox   edts[] = {  cmbParam01SetValue, cmbParam02SetValue, cmbParam03SetValue, cmbParam04SetValue,
                                        cmbParam05SetValue, cmbParam06SetValue, cmbParam07SetValue, cmbParam08SetValue,
                                        cmbParam09SetValue, cmbParam10SetValue, cmbParam11SetValue, cmbParam12SetValue,};
                */
                
                for( int i = 0; i < theApp.LIST_PARAMS_LEN; i++) {
                    if( theApp.m_bParamDefined[i] == true) {
                        if( theApp.m_nParamIndex[i] == 0xFF)
                            vals[i].setText( "Не задано");
                        else
                            vals[i].setText( String.format( "0x%02X", theApp.m_nParamIndex[i]));
                    }
                    else
                        vals[i].setText( "Значение не определено");
                }
                
            }
            
            
        });
        tRefreshValues.start();
        
        tPolling = new Timer( 200, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
            }
            
        });
        tPolling.start();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lblCOMPortTitle = new javax.swing.JLabel();
        edtComPortValue = new javax.swing.JTextField();
        btnConnect = new javax.swing.JButton();
        btnDisconnect = new javax.swing.JButton();
        lblConnectionStateTitle = new javax.swing.JLabel();
        lblConnectionStateValue = new javax.swing.JLabel();
        btnParam01Remove = new javax.swing.JButton();
        lblParam01Value = new javax.swing.JLabel();
        cmbParam01SetValue = new javax.swing.JComboBox();
        btnParam01Set = new javax.swing.JButton();
        btnParam01Plus = new javax.swing.JButton();
        btnParam02Remove = new javax.swing.JButton();
        lblParam02Value = new javax.swing.JLabel();
        cmbParam02SetValue = new javax.swing.JComboBox();
        btnParam02Set = new javax.swing.JButton();
        btnParam02Plus = new javax.swing.JButton();
        btnParam03Remove = new javax.swing.JButton();
        lblParam03Value = new javax.swing.JLabel();
        cmbParam03SetValue = new javax.swing.JComboBox();
        btnParam03Set = new javax.swing.JButton();
        btnParam03Plus = new javax.swing.JButton();
        btnParam04Remove = new javax.swing.JButton();
        lblParam04Value = new javax.swing.JLabel();
        cmbParam04SetValue = new javax.swing.JComboBox();
        btnParam04Set = new javax.swing.JButton();
        btnParam04Plus = new javax.swing.JButton();
        btnParam05Remove = new javax.swing.JButton();
        lblParam05Value = new javax.swing.JLabel();
        cmbParam05SetValue = new javax.swing.JComboBox();
        btnParam05Set = new javax.swing.JButton();
        btnParam05Plus = new javax.swing.JButton();
        btnParam06Remove = new javax.swing.JButton();
        lblParam06Value = new javax.swing.JLabel();
        cmbParam06SetValue = new javax.swing.JComboBox();
        btnParam06Set = new javax.swing.JButton();
        btnParam06Plus = new javax.swing.JButton();
        btnParam07Remove = new javax.swing.JButton();
        lblParam07Value = new javax.swing.JLabel();
        cmbParam07SetValue = new javax.swing.JComboBox();
        btnParam07Set = new javax.swing.JButton();
        btnParam07Plus = new javax.swing.JButton();
        btnParam08Remove = new javax.swing.JButton();
        lblParam08Value = new javax.swing.JLabel();
        cmbParam08SetValue = new javax.swing.JComboBox();
        btnParam08Set = new javax.swing.JButton();
        btnParam08Plus = new javax.swing.JButton();
        btnParam09Remove = new javax.swing.JButton();
        lblParam09Value = new javax.swing.JLabel();
        cmbParam09SetValue = new javax.swing.JComboBox();
        btnParam09Set = new javax.swing.JButton();
        btnParam09Plus = new javax.swing.JButton();
        btnParam10Remove = new javax.swing.JButton();
        lblParam10Value = new javax.swing.JLabel();
        cmbParam10SetValue = new javax.swing.JComboBox();
        btnParam10Set = new javax.swing.JButton();
        btnParam10Plus = new javax.swing.JButton();
        btnParam11Remove = new javax.swing.JButton();
        lblParam11Value = new javax.swing.JLabel();
        cmbParam11SetValue = new javax.swing.JComboBox();
        btnParam11Set = new javax.swing.JButton();
        btnParam11Plus = new javax.swing.JButton();
        btnParam12Remove = new javax.swing.JButton();
        lblParam12Value = new javax.swing.JLabel();
        cmbParam12SetValue = new javax.swing.JComboBox();
        btnParam12Set = new javax.swing.JButton();
        btnParam12Plus = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("МЛГ3Б. Утилита для редактирования списка доп. параметров  (С) ФЛАВТ   2017.08.03 13:20");
        setPreferredSize(new java.awt.Dimension(800, 720));
        setResizable(false);
        getContentPane().setLayout(null);

        lblCOMPortTitle.setText("COM порт:");
        getContentPane().add(lblCOMPortTitle);
        lblCOMPortTitle.setBounds(10, 10, 90, 30);
        getContentPane().add(edtComPortValue);
        edtComPortValue.setBounds(100, 10, 250, 30);

        btnConnect.setText("Соединить");
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });
        getContentPane().add(btnConnect);
        btnConnect.setBounds(360, 10, 210, 30);

        btnDisconnect.setText("Разъединить");
        btnDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisconnectActionPerformed(evt);
            }
        });
        getContentPane().add(btnDisconnect);
        btnDisconnect.setBounds(580, 10, 210, 30);

        lblConnectionStateTitle.setText("Состояние связи:");
        getContentPane().add(lblConnectionStateTitle);
        lblConnectionStateTitle.setBounds(10, 50, 130, 30);

        lblConnectionStateValue.setText("jLabel2");
        lblConnectionStateValue.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblConnectionStateValue);
        lblConnectionStateValue.setBounds(150, 50, 640, 30);

        btnParam01Remove.setText("-");
        btnParam01Remove.setEnabled(false);
        getContentPane().add(btnParam01Remove);
        btnParam01Remove.setBounds(10, 90, 50, 40);

        lblParam01Value.setText("1");
        lblParam01Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam01Value);
        lblParam01Value.setBounds(80, 90, 260, 40);

        cmbParam01SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam01SetValue);
        cmbParam01SetValue.setBounds(360, 90, 280, 40);

        btnParam01Set.setLabel("Set");
        getContentPane().add(btnParam01Set);
        btnParam01Set.setBounds(660, 90, 60, 40);

        btnParam01Plus.setText("+");
        getContentPane().add(btnParam01Plus);
        btnParam01Plus.setBounds(740, 90, 50, 40);

        btnParam02Remove.setText("-");
        getContentPane().add(btnParam02Remove);
        btnParam02Remove.setBounds(10, 140, 50, 40);

        lblParam02Value.setText("2");
        lblParam02Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam02Value);
        lblParam02Value.setBounds(80, 140, 260, 40);

        cmbParam02SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam02SetValue);
        cmbParam02SetValue.setBounds(360, 140, 280, 40);

        btnParam02Set.setLabel("Set");
        getContentPane().add(btnParam02Set);
        btnParam02Set.setBounds(660, 140, 60, 40);

        btnParam02Plus.setText("+");
        getContentPane().add(btnParam02Plus);
        btnParam02Plus.setBounds(740, 140, 50, 40);

        btnParam03Remove.setText("-");
        getContentPane().add(btnParam03Remove);
        btnParam03Remove.setBounds(10, 190, 50, 40);

        lblParam03Value.setText("3");
        lblParam03Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam03Value);
        lblParam03Value.setBounds(80, 190, 260, 40);

        cmbParam03SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam03SetValue);
        cmbParam03SetValue.setBounds(360, 190, 280, 40);

        btnParam03Set.setLabel("Set");
        getContentPane().add(btnParam03Set);
        btnParam03Set.setBounds(660, 190, 60, 40);

        btnParam03Plus.setText("+");
        getContentPane().add(btnParam03Plus);
        btnParam03Plus.setBounds(740, 190, 50, 40);

        btnParam04Remove.setText("-");
        getContentPane().add(btnParam04Remove);
        btnParam04Remove.setBounds(10, 240, 50, 40);

        lblParam04Value.setText("4");
        lblParam04Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam04Value);
        lblParam04Value.setBounds(80, 240, 260, 40);

        cmbParam04SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam04SetValue);
        cmbParam04SetValue.setBounds(360, 240, 280, 40);

        btnParam04Set.setLabel("Set");
        getContentPane().add(btnParam04Set);
        btnParam04Set.setBounds(660, 240, 60, 40);

        btnParam04Plus.setText("+");
        getContentPane().add(btnParam04Plus);
        btnParam04Plus.setBounds(740, 240, 50, 40);

        btnParam05Remove.setText("-");
        getContentPane().add(btnParam05Remove);
        btnParam05Remove.setBounds(10, 290, 50, 40);

        lblParam05Value.setText("5");
        lblParam05Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam05Value);
        lblParam05Value.setBounds(80, 290, 260, 40);

        cmbParam05SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam05SetValue);
        cmbParam05SetValue.setBounds(360, 290, 280, 40);

        btnParam05Set.setLabel("Set");
        getContentPane().add(btnParam05Set);
        btnParam05Set.setBounds(660, 290, 60, 40);

        btnParam05Plus.setText("+");
        getContentPane().add(btnParam05Plus);
        btnParam05Plus.setBounds(740, 290, 50, 40);

        btnParam06Remove.setText("-");
        getContentPane().add(btnParam06Remove);
        btnParam06Remove.setBounds(10, 340, 50, 40);

        lblParam06Value.setText("6");
        lblParam06Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam06Value);
        lblParam06Value.setBounds(80, 340, 260, 40);

        cmbParam06SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam06SetValue);
        cmbParam06SetValue.setBounds(360, 340, 280, 40);

        btnParam06Set.setLabel("Set");
        getContentPane().add(btnParam06Set);
        btnParam06Set.setBounds(660, 340, 60, 40);

        btnParam06Plus.setText("+");
        getContentPane().add(btnParam06Plus);
        btnParam06Plus.setBounds(740, 340, 50, 40);

        btnParam07Remove.setText("-");
        getContentPane().add(btnParam07Remove);
        btnParam07Remove.setBounds(10, 390, 50, 40);

        lblParam07Value.setText("7");
        lblParam07Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam07Value);
        lblParam07Value.setBounds(80, 390, 260, 40);

        cmbParam07SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam07SetValue);
        cmbParam07SetValue.setBounds(360, 390, 280, 40);

        btnParam07Set.setLabel("Set");
        getContentPane().add(btnParam07Set);
        btnParam07Set.setBounds(660, 390, 60, 40);

        btnParam07Plus.setText("+");
        getContentPane().add(btnParam07Plus);
        btnParam07Plus.setBounds(740, 390, 50, 40);

        btnParam08Remove.setText("-");
        getContentPane().add(btnParam08Remove);
        btnParam08Remove.setBounds(10, 440, 50, 40);

        lblParam08Value.setText("8");
        lblParam08Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam08Value);
        lblParam08Value.setBounds(80, 440, 260, 40);

        cmbParam08SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam08SetValue);
        cmbParam08SetValue.setBounds(360, 440, 280, 40);

        btnParam08Set.setLabel("Set");
        getContentPane().add(btnParam08Set);
        btnParam08Set.setBounds(660, 440, 60, 40);

        btnParam08Plus.setText("+");
        getContentPane().add(btnParam08Plus);
        btnParam08Plus.setBounds(740, 440, 50, 40);

        btnParam09Remove.setText("-");
        getContentPane().add(btnParam09Remove);
        btnParam09Remove.setBounds(10, 490, 50, 40);

        lblParam09Value.setText("9");
        lblParam09Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam09Value);
        lblParam09Value.setBounds(80, 490, 260, 40);

        cmbParam09SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam09SetValue);
        cmbParam09SetValue.setBounds(360, 490, 280, 40);

        btnParam09Set.setLabel("Set");
        getContentPane().add(btnParam09Set);
        btnParam09Set.setBounds(660, 490, 60, 40);

        btnParam09Plus.setText("+");
        getContentPane().add(btnParam09Plus);
        btnParam09Plus.setBounds(740, 490, 50, 40);

        btnParam10Remove.setText("-");
        getContentPane().add(btnParam10Remove);
        btnParam10Remove.setBounds(10, 540, 50, 40);

        lblParam10Value.setText("10");
        lblParam10Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam10Value);
        lblParam10Value.setBounds(80, 540, 260, 40);

        cmbParam10SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam10SetValue);
        cmbParam10SetValue.setBounds(360, 540, 280, 40);

        btnParam10Set.setLabel("Set");
        getContentPane().add(btnParam10Set);
        btnParam10Set.setBounds(660, 540, 60, 40);

        btnParam10Plus.setText("+");
        getContentPane().add(btnParam10Plus);
        btnParam10Plus.setBounds(740, 540, 50, 40);

        btnParam11Remove.setText("-");
        getContentPane().add(btnParam11Remove);
        btnParam11Remove.setBounds(10, 590, 50, 40);

        lblParam11Value.setText("11");
        lblParam11Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam11Value);
        lblParam11Value.setBounds(80, 590, 260, 40);

        cmbParam11SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam11SetValue);
        cmbParam11SetValue.setBounds(360, 590, 280, 40);

        btnParam11Set.setLabel("Set");
        getContentPane().add(btnParam11Set);
        btnParam11Set.setBounds(660, 590, 60, 40);

        btnParam11Plus.setText("+");
        getContentPane().add(btnParam11Plus);
        btnParam11Plus.setBounds(740, 590, 50, 40);

        btnParam12Remove.setText("-");
        getContentPane().add(btnParam12Remove);
        btnParam12Remove.setBounds(10, 640, 50, 40);

        lblParam12Value.setText("12");
        lblParam12Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam12Value);
        lblParam12Value.setBounds(80, 640, 260, 40);

        cmbParam12SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam12SetValue);
        cmbParam12SetValue.setBounds(360, 640, 280, 40);

        btnParam12Set.setLabel("Set");
        getContentPane().add(btnParam12Set);
        btnParam12Set.setBounds(660, 640, 60, 40);

        btnParam12Plus.setText("+");
        getContentPane().add(btnParam12Plus);
        btnParam12Plus.setBounds(740, 640, 50, 40);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        m_strPort = edtComPortValue.getText();
        if( m_strPort.isEmpty()) {
            logger.info( "Connect to no-port? Ha (3 times)");
            return;
        }
        
        theApp.m_bfCircleBuffer= new SLG_APST_CircleBuffer();
        
        serialPort = new SerialPort( m_strPort);
        try {
            //Открываем порт
            serialPort.openPort();

            //Выставляем параметры
            serialPort.setParams( 921600,
                                 SerialPort.DATABITS_8,
                                 SerialPort.STOPBITS_1,
                                 SerialPort.PARITY_NONE);

            //Включаем аппаратное управление потоком
            //serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN | 
            //                              SerialPort.FLOWCONTROL_RTSCTS_OUT);

            //Устанавливаем ивент лисенер и маску
            m_evListener = new PortReader();
            serialPort.addEventListener( m_evListener, SerialPort.MASK_RXCHAR);
        }
        catch( SerialPortException ex) {
            logger.error( "COM-Communication exception", ex);
            theApp.m_bConnected = false;
            SLG_APST_App.MessageBoxError( "При попытке соединения получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
            return;
        }
        
        theApp.m_strVersion = "";
        theApp.m_bConnected = true;
        
        thrProcessorThread.start();
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnDisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisconnectActionPerformed
        theApp.m_bConnected = false;
        try {
            serialPort.removeEventListener();
            serialPort.closePort();
            
            thrProcessorRunnable.m_bStopThread = true;
            thrProcessorThread.join( 1000);
            if( thrProcessorThread.isAlive()) {
                logger.error( "Thread stopped, but alive!");
            }
        }
        catch( SerialPortException ex) {
            logger.error( "COM-Communication exception", ex);
        } catch (InterruptedException ex) {
            logger.error( "Processing thread join fails", ex);
        }
    }//GEN-LAST:event_btnDisconnectActionPerformed

    private void disconnectMePlease() {                                              
        new Timer( 500, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                theApp.m_bConnected = false;
                try {
                    serialPort.removeEventListener();
                    serialPort.closePort();
                    thrProcessorRunnable.m_bStopThread = true;
                    thrProcessorThread.join( 1000);
                    if( thrProcessorThread.isAlive()) {
                        logger.error( "Thread stopped, but alive!");
                    }
                }
                catch( SerialPortException ex) {
                    logger.error( "COM-Communication exception", ex);
                }
                catch (InterruptedException ex) {
                    logger.error( "Processing thread join fails", ex);
                }
            }
        });
    }
    
    private class PortReader implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {            
            if( event.isRXCHAR() && event.getEventValue() > 0){
                try {
                    //Получаем ответ от устройства, обрабатываем данные и т.д.
                    int nReadyBytes = event.getEventValue();
                    byte bts[] = new byte[ nReadyBytes];
                    bts = serialPort.readBytes( nReadyBytes);
                    
                    /*
                    String strLogMessage;
                    strLogMessage = String.format( "READ %d BYTE. FIRST ONE=0x%02X", nReadyBytes, bts[0]);
                    logger.debug( strLogMessage);
                    */
                    
                    theApp.m_bfCircleBuffer.AddBytes( bts, nReadyBytes);
                }
                catch (SerialPortException ex) {
                    logger.error( "SerialPortException caught", ex);
                }
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnConnect;
    private javax.swing.JButton btnDisconnect;
    private javax.swing.JButton btnParam01Plus;
    private javax.swing.JButton btnParam01Remove;
    private javax.swing.JButton btnParam01Set;
    private javax.swing.JButton btnParam02Plus;
    private javax.swing.JButton btnParam02Remove;
    private javax.swing.JButton btnParam02Set;
    private javax.swing.JButton btnParam03Plus;
    private javax.swing.JButton btnParam03Remove;
    private javax.swing.JButton btnParam03Set;
    private javax.swing.JButton btnParam04Plus;
    private javax.swing.JButton btnParam04Remove;
    private javax.swing.JButton btnParam04Set;
    private javax.swing.JButton btnParam05Plus;
    private javax.swing.JButton btnParam05Remove;
    private javax.swing.JButton btnParam05Set;
    private javax.swing.JButton btnParam06Plus;
    private javax.swing.JButton btnParam06Remove;
    private javax.swing.JButton btnParam06Set;
    private javax.swing.JButton btnParam07Plus;
    private javax.swing.JButton btnParam07Remove;
    private javax.swing.JButton btnParam07Set;
    private javax.swing.JButton btnParam08Plus;
    private javax.swing.JButton btnParam08Remove;
    private javax.swing.JButton btnParam08Set;
    private javax.swing.JButton btnParam09Plus;
    private javax.swing.JButton btnParam09Remove;
    private javax.swing.JButton btnParam09Set;
    private javax.swing.JButton btnParam10Plus;
    private javax.swing.JButton btnParam10Remove;
    private javax.swing.JButton btnParam10Set;
    private javax.swing.JButton btnParam11Plus;
    private javax.swing.JButton btnParam11Remove;
    private javax.swing.JButton btnParam11Set;
    private javax.swing.JButton btnParam12Plus;
    private javax.swing.JButton btnParam12Remove;
    private javax.swing.JButton btnParam12Set;
    private javax.swing.JComboBox cmbParam01SetValue;
    private javax.swing.JComboBox cmbParam02SetValue;
    private javax.swing.JComboBox cmbParam03SetValue;
    private javax.swing.JComboBox cmbParam04SetValue;
    private javax.swing.JComboBox cmbParam05SetValue;
    private javax.swing.JComboBox cmbParam06SetValue;
    private javax.swing.JComboBox cmbParam07SetValue;
    private javax.swing.JComboBox cmbParam08SetValue;
    private javax.swing.JComboBox cmbParam09SetValue;
    private javax.swing.JComboBox cmbParam10SetValue;
    private javax.swing.JComboBox cmbParam11SetValue;
    private javax.swing.JComboBox cmbParam12SetValue;
    private javax.swing.JTextField edtComPortValue;
    private javax.swing.JLabel lblCOMPortTitle;
    private javax.swing.JLabel lblConnectionStateTitle;
    private javax.swing.JLabel lblConnectionStateValue;
    private javax.swing.JLabel lblParam01Value;
    private javax.swing.JLabel lblParam02Value;
    private javax.swing.JLabel lblParam03Value;
    private javax.swing.JLabel lblParam04Value;
    private javax.swing.JLabel lblParam05Value;
    private javax.swing.JLabel lblParam06Value;
    private javax.swing.JLabel lblParam07Value;
    private javax.swing.JLabel lblParam08Value;
    private javax.swing.JLabel lblParam09Value;
    private javax.swing.JLabel lblParam10Value;
    private javax.swing.JLabel lblParam11Value;
    private javax.swing.JLabel lblParam12Value;
    // End of variables declaration//GEN-END:variables
}
