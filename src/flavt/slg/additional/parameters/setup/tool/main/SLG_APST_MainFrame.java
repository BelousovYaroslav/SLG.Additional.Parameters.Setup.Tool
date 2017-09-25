/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flavt.slg.additional.parameters.setup.tool.main;

import flavt.slg.additional.parameters.setup.tool.communication.SLG_APST_CircleBuffer;
import flavt.slg.additional.parameters.setup.tool.communication.SLG_APST_StreamProcessingThread;
import flavt.slg.lib.constants.SLG_Constants;
import flavt.slg.lib.constants.SLG_ConstantsCmd;
import flavt.slg.lib.constants.SLG_ConstantsParams;
import flavt.slg.lib.constants.SLG_Parameter;
import flavt.slg.lib.constants.SLG_Parameters;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import java.util.logging.Level;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
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
    
    int m_nRequestedParam;
    
    SLG_APST_StreamProcessingThread thrProcessorRunnable;
    Thread thrProcessorThread;
    /**
     * Creates new form SLG_SAP_MainFrame
     */
    public SLG_APST_MainFrame( SLG_APST_App app) {
        initComponents();
        theApp = app;
        
        edtComPortValue.setText( theApp.GetSettings().GetComPort());
        
        DefaultComboBoxModel model01 = new DefaultComboBoxModel();
        DefaultComboBoxModel model02 = new DefaultComboBoxModel();
        DefaultComboBoxModel model03 = new DefaultComboBoxModel();
        DefaultComboBoxModel model04 = new DefaultComboBoxModel();
        DefaultComboBoxModel model05 = new DefaultComboBoxModel();
        DefaultComboBoxModel model06 = new DefaultComboBoxModel();
        DefaultComboBoxModel model07 = new DefaultComboBoxModel();
        DefaultComboBoxModel model08 = new DefaultComboBoxModel();
        DefaultComboBoxModel model09 = new DefaultComboBoxModel();
        DefaultComboBoxModel model10 = new DefaultComboBoxModel();
        DefaultComboBoxModel model11 = new DefaultComboBoxModel();
        DefaultComboBoxModel model12 = new DefaultComboBoxModel();
        
        //fillComboWithAddParam( model, true);
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model01);
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model02);
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model03);
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model04);
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model05);
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model06);
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model07);
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model08);
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model09);
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model10);
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model11);
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model12);
        
        cmbParam01SetValue.setModel( model01);
        cmbParam02SetValue.setModel( model02);
        cmbParam03SetValue.setModel( model03);
        cmbParam04SetValue.setModel( model04);
        cmbParam05SetValue.setModel( model05);
        cmbParam06SetValue.setModel( model06);
        cmbParam07SetValue.setModel( model07);
        cmbParam08SetValue.setModel( model08);
        cmbParam09SetValue.setModel( model09);
        cmbParam10SetValue.setModel( model10);
        cmbParam11SetValue.setModel( model11);
        cmbParam12SetValue.setModel( model12);
        
        theApp.m_bfCircleBuffer= new SLG_APST_CircleBuffer();
        
        thrProcessorRunnable = new SLG_APST_StreamProcessingThread( theApp);
        thrProcessorThread = new Thread( thrProcessorRunnable);
        thrProcessorThread.start();
        
        tRefreshStates = new Timer( 200, new ActionListener() {

            @Override
            public void actionPerformed( ActionEvent e) {
                
                edtComPortValue.setEnabled( !theApp.m_bConnected);
                btnConnect.setEnabled(      !theApp.m_bConnected);
                btnDisconnect.setEnabled(   theApp.m_bConnected);
                
                JButton      btnsS[] =  {   btnParam01Set, btnParam02Set, btnParam03Set, btnParam04Set,
                                            btnParam05Set, btnParam06Set, btnParam07Set, btnParam08Set,
                                            btnParam09Set, btnParam10Set, btnParam11Set, btnParam12Set };
                
                JComboBox   cmbs[] = {      cmbParam01SetValue, cmbParam02SetValue, cmbParam03SetValue, cmbParam04SetValue,
                                            cmbParam05SetValue, cmbParam06SetValue, cmbParam07SetValue, cmbParam08SetValue,
                                            cmbParam09SetValue, cmbParam10SetValue, cmbParam11SetValue, cmbParam12SetValue };
                
                boolean bAllDefined = true;
                for( int i=0; i<12; bAllDefined = bAllDefined & theApp.m_bParamDefined[i++]);
                
                
                //комбобоксы значений
                for( int i = 0; i < theApp.LIST_PARAMS_LEN; i++) {
                    cmbs[i].setEnabled( theApp.m_bConnected && bAllDefined);
                    btnsS[i].setEnabled( theApp.m_bConnected && bAllDefined);
                }
                
            }
        });
        tRefreshStates.start();
        
        tRefreshValues = new Timer( 200, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                
                if( theApp.m_bConnected) {
                    String strStatus = "";
                    
                    if( !theApp.m_strVersion.isEmpty())
                        strStatus = "Версия ПО прибора = " + theApp.m_strVersion + "   ";
                    
                    strStatus +=
                            String.format( "MF:%d CF:%d CSF:%d PC:%d",
                                    theApp.m_nMarkerFails,
                                    theApp.m_nCounterFails,
                                    theApp.m_nCheckSummFails,
                                    theApp.m_nPacksCounter);
                    
                    
                    
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
                        else {
                            SLG_Parameter param = ( SLG_Parameter) SLG_Parameters.getInstance().m_devices.get( theApp.m_nParamIndex[i]);
                            if( param != null) {
                                vals[i].setText( param.GetFullName());
                            }
                            else
                                vals[i].setText( String.format( "0x%02X", theApp.m_nParamIndex[i]));
                        }
                    }
                    else
                        vals[i].setText( "Значение не получено от прибора");
                }
                
            }
            
            
        });
        tRefreshValues.start();
        
        tPolling = new Timer( 200, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if( theApp.m_bConnected && serialPort != null && serialPort.isOpened()) {
                    
                    if( theApp.m_strVersion.isEmpty()) {
                        byte aBytes[] = new byte[4];

                        aBytes[0] = SLG_ConstantsCmd.SLG_CMD_REQ;
                        aBytes[1] = SLG_ConstantsParams.SLG_PARAM_VERSION;
                        aBytes[2] = 0;
                        aBytes[3] = 0;

                        try {
                            serialPort.writeBytes( aBytes);
                            logger.trace( ">> VERSION");
                        } catch (SerialPortException ex) {
                            logger.error( "COM-Communication exception", ex);
                            theApp.m_bConnected = false;
                            SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                            return;
                        }
                    }
                    else {
                        byte aBytes[] = new byte[4];

                        aBytes[0] = SLG_ConstantsCmd.SLG_CMD_REQ;
                        aBytes[1] = SLG_ConstantsParams.SLG_PARAM_ADD_PARAM_LIST_ELEMENT;
                        aBytes[2] = ( byte) m_nRequestedParam;
                        aBytes[3] = 0;

                        try {
                            serialPort.writeBytes( aBytes);
                            logger.trace( ">> PARAM_" + m_nRequestedParam);
                        } catch (SerialPortException ex) {
                            logger.error( "COM-Communication exception", ex);
                            theApp.m_bConnected = false;
                            SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                            return;
                        }

                        m_nRequestedParam = (++m_nRequestedParam) % theApp.LIST_PARAMS_LEN;
                    }
                }
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
        lblParam01Value = new javax.swing.JLabel();
        cmbParam01SetValue = new javax.swing.JComboBox();
        btnParam01Set = new javax.swing.JButton();
        lblParam02Value = new javax.swing.JLabel();
        cmbParam02SetValue = new javax.swing.JComboBox();
        btnParam02Set = new javax.swing.JButton();
        lblParam03Value = new javax.swing.JLabel();
        cmbParam03SetValue = new javax.swing.JComboBox();
        btnParam03Set = new javax.swing.JButton();
        lblParam04Value = new javax.swing.JLabel();
        cmbParam04SetValue = new javax.swing.JComboBox();
        btnParam04Set = new javax.swing.JButton();
        lblParam05Value = new javax.swing.JLabel();
        cmbParam05SetValue = new javax.swing.JComboBox();
        btnParam05Set = new javax.swing.JButton();
        lblParam06Value = new javax.swing.JLabel();
        cmbParam06SetValue = new javax.swing.JComboBox();
        btnParam06Set = new javax.swing.JButton();
        lblParam07Value = new javax.swing.JLabel();
        cmbParam07SetValue = new javax.swing.JComboBox();
        btnParam07Set = new javax.swing.JButton();
        lblParam08Value = new javax.swing.JLabel();
        cmbParam08SetValue = new javax.swing.JComboBox();
        btnParam08Set = new javax.swing.JButton();
        lblParam09Value = new javax.swing.JLabel();
        cmbParam09SetValue = new javax.swing.JComboBox();
        btnParam09Set = new javax.swing.JButton();
        lblParam10Value = new javax.swing.JLabel();
        cmbParam10SetValue = new javax.swing.JComboBox();
        btnParam10Set = new javax.swing.JButton();
        lblParam11Value = new javax.swing.JLabel();
        cmbParam11SetValue = new javax.swing.JComboBox();
        btnParam11Set = new javax.swing.JButton();
        lblParam12Value = new javax.swing.JLabel();
        cmbParam12SetValue = new javax.swing.JComboBox();
        btnParam12Set = new javax.swing.JButton();
        btnSave = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("МЛГ3Б. Утилита для редактирования списка доп. параметров  (С) ФЛАВТ   2017.08.03 13:20");
        setPreferredSize(new java.awt.Dimension(880, 740));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(null);

        lblCOMPortTitle.setText("COM порт:");
        getContentPane().add(lblCOMPortTitle);
        lblCOMPortTitle.setBounds(10, 10, 90, 30);
        getContentPane().add(edtComPortValue);
        edtComPortValue.setBounds(100, 10, 470, 30);

        btnConnect.setText("Соединить");
        btnConnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConnectActionPerformed(evt);
            }
        });
        getContentPane().add(btnConnect);
        btnConnect.setBounds(580, 10, 140, 30);

        btnDisconnect.setText("Разъединить");
        btnDisconnect.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDisconnectActionPerformed(evt);
            }
        });
        getContentPane().add(btnDisconnect);
        btnDisconnect.setBounds(730, 10, 140, 30);

        lblConnectionStateTitle.setText("Состояние связи:");
        getContentPane().add(lblConnectionStateTitle);
        lblConnectionStateTitle.setBounds(10, 50, 130, 30);

        lblConnectionStateValue.setText("jLabel2");
        lblConnectionStateValue.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblConnectionStateValue);
        lblConnectionStateValue.setBounds(150, 50, 720, 30);

        lblParam01Value.setText("1");
        lblParam01Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam01Value);
        lblParam01Value.setBounds(10, 90, 490, 40);

        cmbParam01SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam01SetValue);
        cmbParam01SetValue.setBounds(510, 90, 280, 40);

        btnParam01Set.setLabel("Set");
        btnParam01Set.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnParam01SetActionPerformed(evt);
            }
        });
        getContentPane().add(btnParam01Set);
        btnParam01Set.setBounds(810, 90, 60, 40);

        lblParam02Value.setText("2");
        lblParam02Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam02Value);
        lblParam02Value.setBounds(10, 140, 490, 40);

        cmbParam02SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam02SetValue);
        cmbParam02SetValue.setBounds(510, 140, 280, 40);

        btnParam02Set.setLabel("Set");
        btnParam02Set.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnParam02SetActionPerformed(evt);
            }
        });
        getContentPane().add(btnParam02Set);
        btnParam02Set.setBounds(810, 140, 60, 40);

        lblParam03Value.setText("3");
        lblParam03Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam03Value);
        lblParam03Value.setBounds(10, 190, 490, 40);

        cmbParam03SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam03SetValue);
        cmbParam03SetValue.setBounds(510, 190, 280, 40);

        btnParam03Set.setLabel("Set");
        btnParam03Set.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnParam03SetActionPerformed(evt);
            }
        });
        getContentPane().add(btnParam03Set);
        btnParam03Set.setBounds(810, 190, 60, 40);

        lblParam04Value.setText("4");
        lblParam04Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam04Value);
        lblParam04Value.setBounds(10, 240, 490, 40);

        cmbParam04SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam04SetValue);
        cmbParam04SetValue.setBounds(510, 240, 280, 40);

        btnParam04Set.setLabel("Set");
        btnParam04Set.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnParam04SetActionPerformed(evt);
            }
        });
        getContentPane().add(btnParam04Set);
        btnParam04Set.setBounds(810, 240, 60, 40);

        lblParam05Value.setText("5");
        lblParam05Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam05Value);
        lblParam05Value.setBounds(10, 290, 490, 40);

        cmbParam05SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam05SetValue);
        cmbParam05SetValue.setBounds(510, 290, 280, 40);

        btnParam05Set.setLabel("Set");
        btnParam05Set.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnParam05SetActionPerformed(evt);
            }
        });
        getContentPane().add(btnParam05Set);
        btnParam05Set.setBounds(810, 290, 60, 40);

        lblParam06Value.setText("6");
        lblParam06Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam06Value);
        lblParam06Value.setBounds(10, 340, 490, 40);

        cmbParam06SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam06SetValue);
        cmbParam06SetValue.setBounds(510, 340, 280, 40);

        btnParam06Set.setLabel("Set");
        btnParam06Set.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnParam06SetActionPerformed(evt);
            }
        });
        getContentPane().add(btnParam06Set);
        btnParam06Set.setBounds(810, 340, 60, 40);

        lblParam07Value.setText("7");
        lblParam07Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam07Value);
        lblParam07Value.setBounds(10, 390, 490, 40);

        cmbParam07SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam07SetValue);
        cmbParam07SetValue.setBounds(510, 390, 280, 40);

        btnParam07Set.setLabel("Set");
        btnParam07Set.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnParam07SetActionPerformed(evt);
            }
        });
        getContentPane().add(btnParam07Set);
        btnParam07Set.setBounds(810, 390, 60, 40);

        lblParam08Value.setText("8");
        lblParam08Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam08Value);
        lblParam08Value.setBounds(10, 440, 490, 40);

        cmbParam08SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam08SetValue);
        cmbParam08SetValue.setBounds(510, 440, 280, 40);

        btnParam08Set.setLabel("Set");
        btnParam08Set.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnParam08SetActionPerformed(evt);
            }
        });
        getContentPane().add(btnParam08Set);
        btnParam08Set.setBounds(810, 440, 60, 40);

        lblParam09Value.setText("9");
        lblParam09Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam09Value);
        lblParam09Value.setBounds(10, 490, 490, 40);

        cmbParam09SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam09SetValue);
        cmbParam09SetValue.setBounds(510, 490, 280, 40);

        btnParam09Set.setLabel("Set");
        btnParam09Set.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnParam09SetActionPerformed(evt);
            }
        });
        getContentPane().add(btnParam09Set);
        btnParam09Set.setBounds(810, 490, 60, 40);

        lblParam10Value.setText("10");
        lblParam10Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam10Value);
        lblParam10Value.setBounds(10, 540, 490, 40);

        cmbParam10SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam10SetValue);
        cmbParam10SetValue.setBounds(510, 540, 280, 40);

        btnParam10Set.setLabel("Set");
        btnParam10Set.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnParam10SetActionPerformed(evt);
            }
        });
        getContentPane().add(btnParam10Set);
        btnParam10Set.setBounds(810, 540, 60, 40);

        lblParam11Value.setText("11");
        lblParam11Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam11Value);
        lblParam11Value.setBounds(10, 590, 490, 40);

        cmbParam11SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam11SetValue);
        cmbParam11SetValue.setBounds(510, 590, 280, 40);

        btnParam11Set.setLabel("Set");
        btnParam11Set.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnParam11SetActionPerformed(evt);
            }
        });
        getContentPane().add(btnParam11Set);
        btnParam11Set.setBounds(810, 590, 60, 40);

        lblParam12Value.setText("12");
        lblParam12Value.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        getContentPane().add(lblParam12Value);
        lblParam12Value.setBounds(10, 640, 490, 40);

        cmbParam12SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        getContentPane().add(cmbParam12SetValue);
        cmbParam12SetValue.setBounds(510, 640, 280, 40);

        btnParam12Set.setLabel("Set");
        btnParam12Set.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnParam12SetActionPerformed(evt);
            }
        });
        getContentPane().add(btnParam12Set);
        btnParam12Set.setBounds(810, 640, 60, 40);

        btnSave.setText("Сохранить");
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });
        getContentPane().add(btnSave);
        btnSave.setBounds(620, 690, 250, 40);

        getAccessibleContext().setAccessibleName("МЛГ3Б. Утилита для редактирования списка доп. параметров  (С) ФЛАВТ   2017.08.08 16:40");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnConnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnConnectActionPerformed
        m_strPort = edtComPortValue.getText();
        if( m_strPort.isEmpty()) {
            logger.info( "Connect to no-port? Ha (3 times)");
            return;
        }
        
        theApp.m_bfCircleBuffer= new SLG_APST_CircleBuffer();
        
        m_nRequestedParam = 0;
        
        for( int i=0; i<12; theApp.m_bParamDefined[i++] = false);
        
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
    }//GEN-LAST:event_btnConnectActionPerformed

    private void btnDisconnectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDisconnectActionPerformed
        theApp.m_bConnected = false;
        try {
            serialPort.removeEventListener();
            serialPort.closePort();
            
            /*
            thrProcessorRunnable.m_bStopThread = true;
            thrProcessorThread.join( 1000);
            if( thrProcessorThread.isAlive()) {
                logger.error( "Thread stopped, but alive!");
            }*/
        }
        catch( SerialPortException ex) {
            logger.error( "COM-Communication exception", ex);
        }
        /*
        catch (InterruptedException ex) {
            logger.error( "Processing thread join fails", ex);
        }
        */
    }//GEN-LAST:event_btnDisconnectActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        tRefreshStates.stop();
        tRefreshValues.stop();
        tPolling.stop();
        
        theApp.m_bConnected = false;
        try {
            if( serialPort != null && serialPort.isOpened()) {
                serialPort.removeEventListener();
                serialPort.closePort();
            }
            
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
        
        String strComPort = edtComPortValue.getText();
        if( !strComPort.isEmpty()) {
            theApp.GetSettings().SetComPort( strComPort);
            theApp.GetSettings().SaveSettings();
        }
    }//GEN-LAST:event_formWindowClosing

    /*
    public void fillComboWithAddParam( DefaultComboBoxModel model, boolean bClear) {
        if( bClear)
            model.removeAllElements();

        model.addElement( "00.Термодатчик 1");
        model.addElement( "01.Термодатчик 2");
        model.addElement( "02.Термодатчик 3");
        model.addElement( "03.Разрядный ток I1");
        model.addElement( "04.Разрядный ток I2");
        model.addElement( "05.Напряжение на пьезокорректорах");
        model.addElement( "06.Амплитуда колебаний (ALTERA)");
        model.addElement( "07.Амплитуда колебаний (ДУП)");
        model.addElement( "08.Сигнал RULA");
        model.addElement( "2B.Версия ПО прибора");
        model.addElement( "35.Алг. стаб. ампл.: среднее");
        model.addElement( "36.Алг. стаб. ампл.: круг");
        model.addElement( "36.Алг. стаб. ампл.: флаг активной регулировки");
        model.addElement( "38.Секундный таймер");
    }
    */
    
    private void btnParam01SetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParam01SetActionPerformed
        if( theApp.m_bConnected && serialPort != null && serialPort.isOpened()) {
            
            String strValue = ( String) cmbParam01SetValue.getSelectedItem();
            int nValue = Integer.parseInt( strValue.substring( 0, 3), 16);
            
            byte aBytes[] = new byte[4];
                    
            aBytes[0] = SLG_ConstantsCmd.SLG_CMD_SET;
            aBytes[1] = SLG_ConstantsParams.SLG_PARAM_ADD_PARAM_LIST_ELEMENT;
            aBytes[2] = 0;
            aBytes[3] = ( byte) nValue;

            logger.info(    String.format( "%02X %02X %02X %02X",
                                        aBytes[0],  aBytes[1],  aBytes[2],  aBytes[3]));
            
            try {
                serialPort.writeBytes( aBytes);
            } catch (SerialPortException ex) {
                logger.error( "COM-Communication exception", ex);
                theApp.m_bConnected = false;
                SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                return;
            }
        }
    }//GEN-LAST:event_btnParam01SetActionPerformed

    private void btnParam02SetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParam02SetActionPerformed
        if( theApp.m_bConnected && serialPort != null && serialPort.isOpened()) {
            
            String strValue = ( String) cmbParam02SetValue.getSelectedItem();
            int nValue = Integer.parseInt( strValue.substring( 0, 3), 16);
            
            byte aBytes[] = new byte[4];
                    
            aBytes[0] = SLG_ConstantsCmd.SLG_CMD_SET;
            aBytes[1] = SLG_ConstantsParams.SLG_PARAM_ADD_PARAM_LIST_ELEMENT;
            aBytes[2] = 1;
            aBytes[3] = ( byte) nValue;

            logger.info(    String.format( "%02X %02X %02X %02X",
                                        aBytes[0],  aBytes[1],  aBytes[2],  aBytes[3]));
            
            try {
                serialPort.writeBytes( aBytes);
            } catch (SerialPortException ex) {
                logger.error( "COM-Communication exception", ex);
                theApp.m_bConnected = false;
                SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                return;
            }
        }
    }//GEN-LAST:event_btnParam02SetActionPerformed

    private void btnParam03SetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParam03SetActionPerformed
        if( theApp.m_bConnected && serialPort != null && serialPort.isOpened()) {
            
            String strValue = ( String) cmbParam03SetValue.getSelectedItem();
            int nValue = Integer.parseInt( strValue.substring( 0, 3), 16);
            
            byte aBytes[] = new byte[4];
                    
            aBytes[0] = SLG_ConstantsCmd.SLG_CMD_SET;
            aBytes[1] = SLG_ConstantsParams.SLG_PARAM_ADD_PARAM_LIST_ELEMENT;
            aBytes[2] = 2;
            aBytes[3] = ( byte) nValue;

            logger.info(    String.format( "%02X %02X %02X %02X",
                                        aBytes[0],  aBytes[1],  aBytes[2],  aBytes[3]));
            
            try {
                serialPort.writeBytes( aBytes);
            } catch (SerialPortException ex) {
                logger.error( "COM-Communication exception", ex);
                theApp.m_bConnected = false;
                SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                return;
            }
        }
    }//GEN-LAST:event_btnParam03SetActionPerformed

    private void btnParam04SetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParam04SetActionPerformed
        if( theApp.m_bConnected && serialPort != null && serialPort.isOpened()) {
            
            String strValue = ( String) cmbParam04SetValue.getSelectedItem();
            int nValue = Integer.parseInt( strValue.substring( 0, 3), 16);
            
            byte aBytes[] = new byte[4];
                    
            aBytes[0] = SLG_ConstantsCmd.SLG_CMD_SET;
            aBytes[1] = SLG_ConstantsParams.SLG_PARAM_ADD_PARAM_LIST_ELEMENT;
            aBytes[2] = 3;
            aBytes[3] = ( byte) nValue;

            logger.info(    String.format( "%02X %02X %02X %02X",
                                        aBytes[0],  aBytes[1],  aBytes[2],  aBytes[3]));
            
            try {
                serialPort.writeBytes( aBytes);
            } catch (SerialPortException ex) {
                logger.error( "COM-Communication exception", ex);
                theApp.m_bConnected = false;
                SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                return;
            }
        }
    }//GEN-LAST:event_btnParam04SetActionPerformed

    private void btnParam05SetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParam05SetActionPerformed
        if( theApp.m_bConnected && serialPort != null && serialPort.isOpened()) {
            
            String strValue = ( String) cmbParam05SetValue.getSelectedItem();
            int nValue = Integer.parseInt( strValue.substring( 0, 3), 16);
            
            byte aBytes[] = new byte[4];
                    
            aBytes[0] = SLG_ConstantsCmd.SLG_CMD_SET;
            aBytes[1] = SLG_ConstantsParams.SLG_PARAM_ADD_PARAM_LIST_ELEMENT;
            aBytes[2] = 4;
            aBytes[3] = ( byte) nValue;

            logger.info(    String.format( "%02X %02X %02X %02X",
                                        aBytes[0],  aBytes[1],  aBytes[2],  aBytes[3]));
            
            try {
                serialPort.writeBytes( aBytes);
            } catch (SerialPortException ex) {
                logger.error( "COM-Communication exception", ex);
                theApp.m_bConnected = false;
                SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                return;
            }
        }
    }//GEN-LAST:event_btnParam05SetActionPerformed

    private void btnParam06SetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParam06SetActionPerformed
        if( theApp.m_bConnected && serialPort != null && serialPort.isOpened()) {
            
            String strValue = ( String) cmbParam06SetValue.getSelectedItem();
            int nValue = Integer.parseInt( strValue.substring( 0, 3), 16);
            
            byte aBytes[] = new byte[4];
                    
            aBytes[0] = SLG_ConstantsCmd.SLG_CMD_SET;
            aBytes[1] = SLG_ConstantsParams.SLG_PARAM_ADD_PARAM_LIST_ELEMENT;
            aBytes[2] = 5;
            aBytes[3] = ( byte) nValue;

            logger.info(    String.format( "%02X %02X %02X %02X",
                                        aBytes[0],  aBytes[1],  aBytes[2],  aBytes[3]));
            
            try {
                serialPort.writeBytes( aBytes);
            } catch (SerialPortException ex) {
                logger.error( "COM-Communication exception", ex);
                theApp.m_bConnected = false;
                SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                return;
            }
        }
    }//GEN-LAST:event_btnParam06SetActionPerformed

    private void btnParam07SetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParam07SetActionPerformed
        if( theApp.m_bConnected && serialPort != null && serialPort.isOpened()) {
            
            String strValue = ( String) cmbParam07SetValue.getSelectedItem();
            int nValue = Integer.parseInt( strValue.substring( 0, 3), 16);
            
            byte aBytes[] = new byte[4];
                    
            aBytes[0] = SLG_ConstantsCmd.SLG_CMD_SET;
            aBytes[1] = SLG_ConstantsParams.SLG_PARAM_ADD_PARAM_LIST_ELEMENT;
            aBytes[2] = 6;
            aBytes[3] = ( byte) nValue;

            logger.info(    String.format( "%02X %02X %02X %02X",
                                        aBytes[0],  aBytes[1],  aBytes[2],  aBytes[3]));
            
            try {
                serialPort.writeBytes( aBytes);
            } catch (SerialPortException ex) {
                logger.error( "COM-Communication exception", ex);
                theApp.m_bConnected = false;
                SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                return;
            }
        }
    }//GEN-LAST:event_btnParam07SetActionPerformed

    private void btnParam08SetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParam08SetActionPerformed
        if( theApp.m_bConnected && serialPort != null && serialPort.isOpened()) {
            
            String strValue = ( String) cmbParam08SetValue.getSelectedItem();
            int nValue = Integer.parseInt( strValue.substring( 0, 3), 16);
            
            byte aBytes[] = new byte[4];
                    
            aBytes[0] = SLG_ConstantsCmd.SLG_CMD_SET;
            aBytes[1] = SLG_ConstantsParams.SLG_PARAM_ADD_PARAM_LIST_ELEMENT;
            aBytes[2] = 7;
            aBytes[3] = ( byte) nValue;

            logger.info(    String.format( "%02X %02X %02X %02X",
                                        aBytes[0],  aBytes[1],  aBytes[2],  aBytes[3]));
            
            try {
                serialPort.writeBytes( aBytes);
            } catch (SerialPortException ex) {
                logger.error( "COM-Communication exception", ex);
                theApp.m_bConnected = false;
                SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                return;
            }
        }
    }//GEN-LAST:event_btnParam08SetActionPerformed

    private void btnParam09SetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParam09SetActionPerformed
        if( theApp.m_bConnected && serialPort != null && serialPort.isOpened()) {
            
            String strValue = ( String) cmbParam09SetValue.getSelectedItem();
            int nValue = Integer.parseInt( strValue.substring( 0, 3), 16);
            
            byte aBytes[] = new byte[4];
                    
            aBytes[0] = SLG_ConstantsCmd.SLG_CMD_SET;
            aBytes[1] = SLG_ConstantsParams.SLG_PARAM_ADD_PARAM_LIST_ELEMENT;
            aBytes[2] = 8;
            aBytes[3] = ( byte) nValue;

            logger.info(    String.format( "%02X %02X %02X %02X",
                                        aBytes[0],  aBytes[1],  aBytes[2],  aBytes[3]));
            
            try {
                serialPort.writeBytes( aBytes);
            } catch (SerialPortException ex) {
                logger.error( "COM-Communication exception", ex);
                theApp.m_bConnected = false;
                SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                return;
            }
        }
    }//GEN-LAST:event_btnParam09SetActionPerformed

    private void btnParam10SetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParam10SetActionPerformed
        if( theApp.m_bConnected && serialPort != null && serialPort.isOpened()) {
            
            String strValue = ( String) cmbParam10SetValue.getSelectedItem();
            int nValue = Integer.parseInt( strValue.substring( 0, 3), 16);
            
            byte aBytes[] = new byte[4];
                    
            aBytes[0] = SLG_ConstantsCmd.SLG_CMD_SET;
            aBytes[1] = SLG_ConstantsParams.SLG_PARAM_ADD_PARAM_LIST_ELEMENT;
            aBytes[2] = 9;
            aBytes[3] = ( byte) nValue;

            logger.info(    String.format( "%02X %02X %02X %02X",
                                        aBytes[0],  aBytes[1],  aBytes[2],  aBytes[3]));
            
            try {
                serialPort.writeBytes( aBytes);
            } catch (SerialPortException ex) {
                logger.error( "COM-Communication exception", ex);
                theApp.m_bConnected = false;
                SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                return;
            }
        }
    }//GEN-LAST:event_btnParam10SetActionPerformed

    private void btnParam11SetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParam11SetActionPerformed
        if( theApp.m_bConnected && serialPort != null && serialPort.isOpened()) {
            
            String strValue = ( String) cmbParam11SetValue.getSelectedItem();
            int nValue = Integer.parseInt( strValue.substring( 0, 3), 16);
            
            byte aBytes[] = new byte[4];
                    
            aBytes[0] = SLG_ConstantsCmd.SLG_CMD_SET;
            aBytes[1] = SLG_ConstantsParams.SLG_PARAM_ADD_PARAM_LIST_ELEMENT;
            aBytes[2] = 10;
            aBytes[3] = ( byte) nValue;

            logger.info(    String.format( "%02X %02X %02X %02X",
                                        aBytes[0],  aBytes[1],  aBytes[2],  aBytes[3]));
            
            try {
                serialPort.writeBytes( aBytes);
            } catch (SerialPortException ex) {
                logger.error( "COM-Communication exception", ex);
                theApp.m_bConnected = false;
                SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                return;
            }
        }
    }//GEN-LAST:event_btnParam11SetActionPerformed

    private void btnParam12SetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnParam12SetActionPerformed
        if( theApp.m_bConnected && serialPort != null && serialPort.isOpened()) {
            
            String strValue = ( String) cmbParam12SetValue.getSelectedItem();
            int nValue = Integer.parseInt( strValue.substring( 0, 3), 16);
            
            byte aBytes[] = new byte[4];
                    
            aBytes[0] = SLG_ConstantsCmd.SLG_CMD_SET;
            aBytes[1] = SLG_ConstantsParams.SLG_PARAM_ADD_PARAM_LIST_ELEMENT;
            aBytes[2] = 11;
            aBytes[3] = ( byte) nValue;

            logger.info(    String.format( "%02X %02X %02X %02X",
                                        aBytes[0],  aBytes[1],  aBytes[2],  aBytes[3]));
            
            try {
                serialPort.writeBytes( aBytes);
            } catch (SerialPortException ex) {
                logger.error( "COM-Communication exception", ex);
                theApp.m_bConnected = false;
                SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                return;
            }
        }
    }//GEN-LAST:event_btnParam12SetActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        byte aBytes[] = new byte[4];
                    
            aBytes[0] = SLG_ConstantsCmd.SLG_CMD_ACT_SAVE_FLASH_PARAM;
            aBytes[1] = 3;
            aBytes[2] = 0;
            aBytes[3] = 0;

            logger.info(    String.format( "%02X %02X %02X %02X",
                                        aBytes[0],  aBytes[1],  aBytes[2],  aBytes[3]));
            
            try {
                serialPort.writeBytes( aBytes);
            } catch (SerialPortException ex) {
                logger.error( "COM-Communication exception", ex);
                theApp.m_bConnected = false;
                SLG_APST_App.MessageBoxError( "При попытке записи в порт получили исключительную ситуацию:\n\n" + ex.toString(), "SLG_APST");
                return;
            }
    }//GEN-LAST:event_btnSaveActionPerformed

    
    private void disconnectMePlease() {                                              
        new Timer( 500, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                theApp.m_bConnected = false;
                try {
                    serialPort.removeEventListener();
                    serialPort.closePort();
                    
                    /*
                    thrProcessorRunnable.m_bStopThread = true;
                    thrProcessorThread.join( 1000);
                    if( thrProcessorThread.isAlive()) {
                        logger.error( "Thread stopped, but alive!");
                    }
                    */
                }
                catch( SerialPortException ex) {
                    logger.error( "COM-Communication exception", ex);
                }
                /*
                catch (InterruptedException ex) {
                    logger.error( "Processing thread join fails", ex);
                }
                */
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
    private javax.swing.JButton btnParam01Set;
    private javax.swing.JButton btnParam02Set;
    private javax.swing.JButton btnParam03Set;
    private javax.swing.JButton btnParam04Set;
    private javax.swing.JButton btnParam05Set;
    private javax.swing.JButton btnParam06Set;
    private javax.swing.JButton btnParam07Set;
    private javax.swing.JButton btnParam08Set;
    private javax.swing.JButton btnParam09Set;
    private javax.swing.JButton btnParam10Set;
    private javax.swing.JButton btnParam11Set;
    private javax.swing.JButton btnParam12Set;
    private javax.swing.JButton btnSave;
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
