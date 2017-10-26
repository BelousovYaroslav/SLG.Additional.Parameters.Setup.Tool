/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flavt.slg.additional.parameters.setup.tool.main;

import flavt.slg.additional.parameters.setup.tool.communication.SLG_APST_CircleBuffer;
import flavt.slg.additional.parameters.setup.tool.communication.SLG_APST_StreamProcessingThread;
import flavt.slg.lib.constants.SLG_ConstantsCmd;
import flavt.slg.lib.constants.SLG_ConstantsParams;
import flavt.slg.lib.constants.SLG_Parameter;
import flavt.slg.lib.constants.SLG_Parameters;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model01);   model01.addElement( "");
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model02);   model02.addElement( "");
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model03);   model03.addElement( "");
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model04);   model04.addElement( "");
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model05);   model05.addElement( "");
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model06);   model06.addElement( "");
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model07);   model07.addElement( "");
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model08);   model08.addElement( "");
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model09);   model09.addElement( "");
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model10);   model10.addElement( "");
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model11);   model11.addElement( "");
        SLG_Parameters.getInstance().fillComboCanBeAddedToList( model12);   model12.addElement( "");
        
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
                
                JComboBox   cmbs[] = {      cmbParam02SetValue, cmbParam02SetValue, cmbParam03SetValue, cmbParam04SetValue,
                                            cmbParam05SetValue, cmbParam06SetValue, cmbParam07SetValue, cmbParam08SetValue,
                                            cmbParam09SetValue, cmbParam10SetValue, cmbParam11SetValue, cmbParam12SetValue };
                
                boolean bAllDefined = true;
                for( int i=0; i<12; bAllDefined = bAllDefined & theApp.m_bParamDefined[i++]);
                
                
                //комбобоксы значений
                for( int i = 0; i < theApp.LIST_PARAMS_LEN; i++) {
                    cmbs[i].setEnabled( theApp.m_bConnected && bAllDefined);
                    btnsS[i].setEnabled( theApp.m_bConnected && bAllDefined);
                }
                
                btnSave.setEnabled( theApp.m_bConnected && bAllDefined);
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
        edtQElement01 = new javax.swing.JTextField();
        btnParam01Set = new javax.swing.JButton();
        lblParam02Value = new javax.swing.JLabel();
        cmbParam02SetValue = new javax.swing.JComboBox();
        edtQElement02 = new javax.swing.JTextField();
        btnParam02Set = new javax.swing.JButton();
        lblParam03Value = new javax.swing.JLabel();
        cmbParam03SetValue = new javax.swing.JComboBox();
        edtQElement03 = new javax.swing.JTextField();
        btnParam03Set = new javax.swing.JButton();
        lblParam04Value = new javax.swing.JLabel();
        cmbParam04SetValue = new javax.swing.JComboBox();
        edtQElement04 = new javax.swing.JTextField();
        btnParam04Set = new javax.swing.JButton();
        lblParam05Value = new javax.swing.JLabel();
        cmbParam05SetValue = new javax.swing.JComboBox();
        edtQElement05 = new javax.swing.JTextField();
        btnParam05Set = new javax.swing.JButton();
        lblParam06Value = new javax.swing.JLabel();
        cmbParam06SetValue = new javax.swing.JComboBox();
        edtQElement06 = new javax.swing.JTextField();
        btnParam06Set = new javax.swing.JButton();
        lblParam07Value = new javax.swing.JLabel();
        cmbParam07SetValue = new javax.swing.JComboBox();
        edtQElement07 = new javax.swing.JTextField();
        btnParam07Set = new javax.swing.JButton();
        lblParam08Value = new javax.swing.JLabel();
        cmbParam08SetValue = new javax.swing.JComboBox();
        edtQElement08 = new javax.swing.JTextField();
        btnParam08Set = new javax.swing.JButton();
        lblParam09Value = new javax.swing.JLabel();
        cmbParam09SetValue = new javax.swing.JComboBox();
        edtQElement09 = new javax.swing.JTextField();
        btnParam09Set = new javax.swing.JButton();
        lblParam10Value = new javax.swing.JLabel();
        cmbParam10SetValue = new javax.swing.JComboBox();
        edtQElement10 = new javax.swing.JTextField();
        btnParam10Set = new javax.swing.JButton();
        lblParam11Value = new javax.swing.JLabel();
        cmbParam11SetValue = new javax.swing.JComboBox();
        edtQElement11 = new javax.swing.JTextField();
        btnParam11Set = new javax.swing.JButton();
        lblParam12Value = new javax.swing.JLabel();
        cmbParam12SetValue = new javax.swing.JComboBox();
        edtQElement12 = new javax.swing.JTextField();
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
        lblParam01Value.setBounds(10, 90, 450, 40);

        cmbParam01SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbParam01SetValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbParam01SetValueActionPerformed(evt);
            }
        });
        getContentPane().add(cmbParam01SetValue);
        cmbParam01SetValue.setBounds(470, 90, 280, 40);

        edtQElement01.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        edtQElement01.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                edtQElement01FocusLost(evt);
            }
        });
        getContentPane().add(edtQElement01);
        edtQElement01.setBounds(760, 90, 40, 40);

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
        lblParam02Value.setBounds(10, 140, 450, 40);

        cmbParam02SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbParam02SetValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbParam02SetValueActionPerformed(evt);
            }
        });
        getContentPane().add(cmbParam02SetValue);
        cmbParam02SetValue.setBounds(470, 140, 280, 40);

        edtQElement02.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        edtQElement02.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                edtQElement02FocusLost(evt);
            }
        });
        getContentPane().add(edtQElement02);
        edtQElement02.setBounds(760, 140, 40, 40);

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
        lblParam03Value.setBounds(10, 190, 450, 40);

        cmbParam03SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbParam03SetValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbParam03SetValueActionPerformed(evt);
            }
        });
        getContentPane().add(cmbParam03SetValue);
        cmbParam03SetValue.setBounds(470, 190, 280, 40);

        edtQElement03.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        edtQElement03.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                edtQElement03FocusLost(evt);
            }
        });
        getContentPane().add(edtQElement03);
        edtQElement03.setBounds(760, 190, 40, 40);

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
        lblParam04Value.setBounds(10, 240, 450, 40);

        cmbParam04SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbParam04SetValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbParam04SetValueActionPerformed(evt);
            }
        });
        getContentPane().add(cmbParam04SetValue);
        cmbParam04SetValue.setBounds(470, 240, 280, 40);

        edtQElement04.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        edtQElement04.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                edtQElement04FocusLost(evt);
            }
        });
        getContentPane().add(edtQElement04);
        edtQElement04.setBounds(760, 240, 40, 40);

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
        lblParam05Value.setBounds(10, 290, 450, 40);

        cmbParam05SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbParam05SetValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbParam05SetValueActionPerformed(evt);
            }
        });
        getContentPane().add(cmbParam05SetValue);
        cmbParam05SetValue.setBounds(470, 290, 280, 40);

        edtQElement05.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        edtQElement05.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                edtQElement05FocusLost(evt);
            }
        });
        getContentPane().add(edtQElement05);
        edtQElement05.setBounds(760, 290, 40, 40);

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
        lblParam06Value.setBounds(10, 340, 450, 40);

        cmbParam06SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbParam06SetValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbParam06SetValueActionPerformed(evt);
            }
        });
        getContentPane().add(cmbParam06SetValue);
        cmbParam06SetValue.setBounds(470, 340, 280, 40);

        edtQElement06.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        edtQElement06.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                edtQElement06FocusLost(evt);
            }
        });
        getContentPane().add(edtQElement06);
        edtQElement06.setBounds(760, 340, 40, 40);

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
        lblParam07Value.setBounds(10, 390, 450, 40);

        cmbParam07SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbParam07SetValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbParam07SetValueActionPerformed(evt);
            }
        });
        getContentPane().add(cmbParam07SetValue);
        cmbParam07SetValue.setBounds(470, 390, 280, 40);

        edtQElement07.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        edtQElement07.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                edtQElement07FocusLost(evt);
            }
        });
        getContentPane().add(edtQElement07);
        edtQElement07.setBounds(760, 390, 40, 40);

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
        lblParam08Value.setBounds(10, 440, 450, 40);

        cmbParam08SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbParam08SetValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbParam08SetValueActionPerformed(evt);
            }
        });
        getContentPane().add(cmbParam08SetValue);
        cmbParam08SetValue.setBounds(470, 440, 280, 40);

        edtQElement08.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        edtQElement08.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                edtQElement08FocusLost(evt);
            }
        });
        getContentPane().add(edtQElement08);
        edtQElement08.setBounds(760, 440, 40, 40);

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
        lblParam09Value.setBounds(10, 490, 450, 40);

        cmbParam09SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbParam09SetValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbParam09SetValueActionPerformed(evt);
            }
        });
        getContentPane().add(cmbParam09SetValue);
        cmbParam09SetValue.setBounds(470, 490, 280, 40);

        edtQElement09.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        edtQElement09.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                edtQElement09FocusLost(evt);
            }
        });
        getContentPane().add(edtQElement09);
        edtQElement09.setBounds(760, 490, 40, 40);

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
        lblParam10Value.setBounds(10, 540, 450, 40);

        cmbParam10SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbParam10SetValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbParam10SetValueActionPerformed(evt);
            }
        });
        getContentPane().add(cmbParam10SetValue);
        cmbParam10SetValue.setBounds(470, 540, 280, 40);

        edtQElement10.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        edtQElement10.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                edtQElement10FocusLost(evt);
            }
        });
        getContentPane().add(edtQElement10);
        edtQElement10.setBounds(760, 540, 40, 40);

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
        lblParam11Value.setBounds(10, 590, 450, 40);

        cmbParam11SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbParam11SetValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbParam11SetValueActionPerformed(evt);
            }
        });
        getContentPane().add(cmbParam11SetValue);
        cmbParam11SetValue.setBounds(470, 590, 280, 40);

        edtQElement11.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        edtQElement11.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                edtQElement11FocusLost(evt);
            }
        });
        getContentPane().add(edtQElement11);
        edtQElement11.setBounds(760, 590, 40, 40);

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
        lblParam12Value.setBounds(10, 640, 450, 40);

        cmbParam12SetValue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbParam12SetValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbParam12SetValueActionPerformed(evt);
            }
        });
        getContentPane().add(cmbParam12SetValue);
        cmbParam12SetValue.setBounds(470, 640, 280, 40);

        edtQElement12.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        edtQElement12.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                edtQElement12FocusLost(evt);
            }
        });
        getContentPane().add(edtQElement12);
        edtQElement12.setBounds(760, 640, 40, 40);

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

        for( int i=0; i<12; theApp.m_bParamDefined[i++] = false);
                        
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
            
            String strValue = edtQElement01.getText();
            if( strValue.isEmpty()) return;
            int nValue = Integer.parseInt( strValue, 16);
            
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
            
            String strValue = edtQElement02.getText();
            if( strValue.isEmpty()) return;
            int nValue = Integer.parseInt( strValue, 16);
            
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
            
            String strValue = edtQElement03.getText();
            if( strValue.isEmpty()) return;
            int nValue = Integer.parseInt( strValue, 16);
            
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
            
            String strValue = edtQElement04.getText();
            if( strValue.isEmpty()) return;
            int nValue = Integer.parseInt( strValue, 16);
            
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
            
            String strValue = edtQElement05.getText();
            if( strValue.isEmpty()) return;
            int nValue = Integer.parseInt( strValue, 16);
            
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
            
            String strValue = edtQElement06.getText();
            if( strValue.isEmpty()) return;
            int nValue = Integer.parseInt( strValue, 16);
            
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
            
            String strValue = edtQElement07.getText();
            if( strValue.isEmpty()) return;
            int nValue = Integer.parseInt( strValue, 16);
            
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
            
            String strValue = edtQElement08.getText();
            if( strValue.isEmpty()) return;
            int nValue = Integer.parseInt( strValue, 16);
            
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
            
            String strValue = edtQElement09.getText();
            if( strValue.isEmpty()) return;
            int nValue = Integer.parseInt( strValue, 16);
            
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
            
            String strValue = edtQElement10.getText();
            if( strValue.isEmpty()) return;
            int nValue = Integer.parseInt( strValue, 16);
            
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
            
            String strValue = edtQElement11.getText();
            if( strValue.isEmpty()) return;
            int nValue = Integer.parseInt( strValue, 16);
            
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
            
            String strValue = edtQElement12.getText();
            if( strValue.isEmpty()) return;
            int nValue = Integer.parseInt( strValue, 16);
            
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

    private void cmbParam01SetValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbParam01SetValueActionPerformed
        String strValue = ( String) cmbParam01SetValue.getSelectedItem();
        
        if( strValue.isEmpty()) return;
        
        try {
            int nCode = Integer.parseInt( strValue.substring( 0, 3));
            edtQElement01.setText( String.format( "%02X", nCode));
        } catch( NumberFormatException ex) {
            logger.warn( "После выбора значения комбо для эл1 произошёл Exception", ex);
        }       
        
    }//GEN-LAST:event_cmbParam01SetValueActionPerformed

    private void cmbParam02SetValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbParam02SetValueActionPerformed
        String strValue = ( String) cmbParam02SetValue.getSelectedItem();
        
        if( strValue.isEmpty()) return;
        
        try {
            int nCode = Integer.parseInt( strValue.substring( 0, 3));
            edtQElement02.setText( String.format( "%02X", nCode));
        } catch( NumberFormatException ex) {
            logger.warn( "После выбора значения комбо для эл2 произошёл Exception", ex);
        }
    }//GEN-LAST:event_cmbParam02SetValueActionPerformed

    private void cmbParam03SetValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbParam03SetValueActionPerformed
        String strValue = ( String) cmbParam03SetValue.getSelectedItem();
        
        if( strValue.isEmpty()) return;
        
        try {
            int nCode = Integer.parseInt( strValue.substring( 0, 3));
            edtQElement03.setText( String.format( "%02X", nCode));
        } catch( NumberFormatException ex) {
            logger.warn( "После выбора значения комбо для эл3 произошёл Exception", ex);
        }
    }//GEN-LAST:event_cmbParam03SetValueActionPerformed

    private void cmbParam04SetValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbParam04SetValueActionPerformed
        String strValue = ( String) cmbParam04SetValue.getSelectedItem();
        
        if( strValue.isEmpty()) return;
        
        try {
            int nCode = Integer.parseInt( strValue.substring( 0, 3));
            edtQElement04.setText( String.format( "%02X", nCode));
        } catch( NumberFormatException ex) {
            logger.warn( "После выбора значения комбо для эл4 произошёл Exception", ex);
        }
    }//GEN-LAST:event_cmbParam04SetValueActionPerformed

    private void cmbParam05SetValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbParam05SetValueActionPerformed
        String strValue = ( String) cmbParam05SetValue.getSelectedItem();
        
        if( strValue.isEmpty()) return;
        
        try {
            int nCode = Integer.parseInt( strValue.substring( 0, 3));
            edtQElement05.setText( String.format( "%02X", nCode));
        } catch( NumberFormatException ex) {
            logger.warn( "После выбора значения комбо для эл5 произошёл Exception", ex);
        }
    }//GEN-LAST:event_cmbParam05SetValueActionPerformed

    private void cmbParam06SetValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbParam06SetValueActionPerformed
        String strValue = ( String) cmbParam06SetValue.getSelectedItem();
        
        if( strValue.isEmpty()) return;
        
        try {
            int nCode = Integer.parseInt( strValue.substring( 0, 3));
            edtQElement06.setText( String.format( "%02X", nCode));
        } catch( NumberFormatException ex) {
            logger.warn( "После выбора значения комбо для эл6 произошёл Exception", ex);
        }
    }//GEN-LAST:event_cmbParam06SetValueActionPerformed

    private void cmbParam07SetValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbParam07SetValueActionPerformed
        String strValue = ( String) cmbParam07SetValue.getSelectedItem();
        
        if( strValue.isEmpty()) return;
        
        try {
            int nCode = Integer.parseInt( strValue.substring( 0, 3));
            edtQElement07.setText( String.format( "%02X", nCode));
        } catch( NumberFormatException ex) {
            logger.warn( "После выбора значения комбо для эл7 произошёл Exception", ex);
        }
    }//GEN-LAST:event_cmbParam07SetValueActionPerformed

    private void cmbParam08SetValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbParam08SetValueActionPerformed
        String strValue = ( String) cmbParam08SetValue.getSelectedItem();
        
        if( strValue.isEmpty()) return;
        
        try {
            int nCode = Integer.parseInt( strValue.substring( 0, 3));
            edtQElement08.setText( String.format( "%02X", nCode));
        } catch( NumberFormatException ex) {
            logger.warn( "После выбора значения комбо для эл8 произошёл Exception", ex);
        }
    }//GEN-LAST:event_cmbParam08SetValueActionPerformed

    private void cmbParam09SetValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbParam09SetValueActionPerformed
        String strValue = ( String) cmbParam09SetValue.getSelectedItem();
        
        if( strValue.isEmpty()) return;
        
        try {
            int nCode = Integer.parseInt( strValue.substring( 0, 3));
            edtQElement09.setText( String.format( "%02X", nCode));
        } catch( NumberFormatException ex) {
            logger.warn( "После выбора значения комбо для эл9 произошёл Exception", ex);
        }
    }//GEN-LAST:event_cmbParam09SetValueActionPerformed

    private void cmbParam10SetValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbParam10SetValueActionPerformed
        String strValue = ( String) cmbParam10SetValue.getSelectedItem();
        
        if( strValue.isEmpty()) return;
        
        try {
            int nCode = Integer.parseInt( strValue.substring( 0, 3));
            edtQElement10.setText( String.format( "%02X", nCode));
        } catch( NumberFormatException ex) {
            logger.warn( "После выбора значения комбо для эл10 произошёл Exception", ex);
        }
    }//GEN-LAST:event_cmbParam10SetValueActionPerformed

    private void cmbParam11SetValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbParam11SetValueActionPerformed
        String strValue = ( String) cmbParam11SetValue.getSelectedItem();
        
        if( strValue.isEmpty()) return;
        
        try {
            int nCode = Integer.parseInt( strValue.substring( 0, 3));
            edtQElement11.setText( String.format( "%02X", nCode));
        } catch( NumberFormatException ex) {
            logger.warn( "После выбора значения комбо для эл11 произошёл Exception", ex);
        }
    }//GEN-LAST:event_cmbParam11SetValueActionPerformed

    private void cmbParam12SetValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbParam12SetValueActionPerformed
        String strValue = ( String) cmbParam12SetValue.getSelectedItem();
        
        if( strValue.isEmpty()) return;
        
        try {
            int nCode = Integer.parseInt( strValue.substring( 0, 3));
            edtQElement12.setText( String.format( "%02X", nCode));
        } catch( NumberFormatException ex) {
            logger.warn( "После выбора значения комбо для эл12 произошёл Exception", ex);
        }
    }//GEN-LAST:event_cmbParam12SetValueActionPerformed

    private void edtQElement01FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edtQElement01FocusLost
        try {
            int nEdt = Integer.parseInt( edtQElement01.getText(), 16);

            DefaultComboBoxModel model = ( DefaultComboBoxModel) cmbParam01SetValue.getModel();
            int nResult = model.getSize() - 1;
            for( int i=0; i<model.getSize() - 1; i++) {
                String strCmb = ( String) model.getElementAt(i);
                strCmb = strCmb.substring( 0, 3);
                int nCmb = Integer.parseInt( strCmb);
                if( nCmb == nEdt) {
                    nResult = i;
                    break;
                }
            }
            
            cmbParam01SetValue.setSelectedIndex( nResult);
        }
        catch( NumberFormatException ex) {
            logger.warn( "При потере фокуса окна ввода парам1 произошёл NumberFormatException", ex);
        }
    }//GEN-LAST:event_edtQElement01FocusLost

    private void edtQElement02FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edtQElement02FocusLost
        try {
            int nEdt = Integer.parseInt( edtQElement02.getText(), 16);

            DefaultComboBoxModel model = ( DefaultComboBoxModel) cmbParam02SetValue.getModel();
            int nResult = model.getSize() - 1;
            for( int i=0; i<model.getSize() - 1; i++) {
                String strCmb = ( String) model.getElementAt(i);
                strCmb = strCmb.substring( 0, 3);
                int nCmb = Integer.parseInt( strCmb);
                if( nCmb == nEdt) {
                    nResult = i;
                    break;
                }
            }
            
            cmbParam02SetValue.setSelectedIndex( nResult);
        }
        catch( NumberFormatException ex) {
            logger.warn( "При потере фокуса окна ввода парам2 произошёл NumberFormatException", ex);
        }
    }//GEN-LAST:event_edtQElement02FocusLost

    private void edtQElement03FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edtQElement03FocusLost
        try {
            int nEdt = Integer.parseInt( edtQElement03.getText(), 16);

            DefaultComboBoxModel model = ( DefaultComboBoxModel) cmbParam03SetValue.getModel();
            int nResult = model.getSize() - 1;
            for( int i=0; i<model.getSize() - 1; i++) {
                String strCmb = ( String) model.getElementAt(i);
                strCmb = strCmb.substring( 0, 3);
                int nCmb = Integer.parseInt( strCmb);
                if( nCmb == nEdt) {
                    nResult = i;
                    break;
                }
            }
            
            cmbParam03SetValue.setSelectedIndex( nResult);
        }
        catch( NumberFormatException ex) {
            logger.warn( "При потере фокуса окна ввода парам3 произошёл NumberFormatException", ex);
        }
    }//GEN-LAST:event_edtQElement03FocusLost

    private void edtQElement04FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edtQElement04FocusLost
        try {
            int nEdt = Integer.parseInt( edtQElement04.getText(), 16);

            DefaultComboBoxModel model = ( DefaultComboBoxModel) cmbParam04SetValue.getModel();
            int nResult = model.getSize() - 1;
            for( int i=0; i<model.getSize() - 1; i++) {
                String strCmb = ( String) model.getElementAt(i);
                strCmb = strCmb.substring( 0, 3);
                int nCmb = Integer.parseInt( strCmb);
                if( nCmb == nEdt) {
                    nResult = i;
                    break;
                }
            }
            
            cmbParam04SetValue.setSelectedIndex( nResult);
        }
        catch( NumberFormatException ex) {
            logger.warn( "При потере фокуса окна ввода парам4 произошёл NumberFormatException", ex);
        }
    }//GEN-LAST:event_edtQElement04FocusLost

    private void edtQElement05FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edtQElement05FocusLost
        try {
            int nEdt = Integer.parseInt( edtQElement05.getText(), 16);

            DefaultComboBoxModel model = ( DefaultComboBoxModel) cmbParam05SetValue.getModel();
            int nResult = model.getSize() - 1;
            for( int i=0; i<model.getSize() - 1; i++) {
                String strCmb = ( String) model.getElementAt(i);
                strCmb = strCmb.substring( 0, 3);
                int nCmb = Integer.parseInt( strCmb);
                if( nCmb == nEdt) {
                    nResult = i;
                    break;
                }
            }
            
            cmbParam05SetValue.setSelectedIndex( nResult);
        }
        catch( NumberFormatException ex) {
            logger.warn( "При потере фокуса окна ввода парам5 произошёл NumberFormatException", ex);
        }
    }//GEN-LAST:event_edtQElement05FocusLost

    private void edtQElement06FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edtQElement06FocusLost
        try {
            int nEdt = Integer.parseInt( edtQElement06.getText(), 16);

            DefaultComboBoxModel model = ( DefaultComboBoxModel) cmbParam06SetValue.getModel();
            int nResult = model.getSize() - 1;
            for( int i=0; i<model.getSize() - 1; i++) {
                String strCmb = ( String) model.getElementAt(i);
                strCmb = strCmb.substring( 0, 3);
                int nCmb = Integer.parseInt( strCmb);
                if( nCmb == nEdt) {
                    nResult = i;
                    break;
                }
            }
            
            cmbParam06SetValue.setSelectedIndex( nResult);
        }
        catch( NumberFormatException ex) {
            logger.warn( "При потере фокуса окна ввода парам6 произошёл NumberFormatException", ex);
        }
    }//GEN-LAST:event_edtQElement06FocusLost

    private void edtQElement07FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edtQElement07FocusLost
        try {
            int nEdt = Integer.parseInt( edtQElement07.getText(), 16);

            DefaultComboBoxModel model = ( DefaultComboBoxModel) cmbParam07SetValue.getModel();
            int nResult = model.getSize() - 1;
            for( int i=0; i<model.getSize() - 1; i++) {
                String strCmb = ( String) model.getElementAt(i);
                strCmb = strCmb.substring( 0, 3);
                int nCmb = Integer.parseInt( strCmb);
                if( nCmb == nEdt) {
                    nResult = i;
                    break;
                }
            }
            
            cmbParam07SetValue.setSelectedIndex( nResult);
        }
        catch( NumberFormatException ex) {
            logger.warn( "При потере фокуса окна ввода парам7 произошёл NumberFormatException", ex);
        }
    }//GEN-LAST:event_edtQElement07FocusLost

    private void edtQElement08FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edtQElement08FocusLost
        try {
            int nEdt = Integer.parseInt( edtQElement08.getText(), 16);

            DefaultComboBoxModel model = ( DefaultComboBoxModel) cmbParam08SetValue.getModel();
            int nResult = model.getSize() - 1;
            for( int i=0; i<model.getSize() - 1; i++) {
                String strCmb = ( String) model.getElementAt(i);
                strCmb = strCmb.substring( 0, 3);
                int nCmb = Integer.parseInt( strCmb);
                if( nCmb == nEdt) {
                    nResult = i;
                    break;
                }
            }
            
            cmbParam08SetValue.setSelectedIndex( nResult);
        }
        catch( NumberFormatException ex) {
            logger.warn( "При потере фокуса окна ввода парам8 произошёл NumberFormatException", ex);
        }
    }//GEN-LAST:event_edtQElement08FocusLost

    private void edtQElement09FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edtQElement09FocusLost
        try {
            int nEdt = Integer.parseInt( edtQElement09.getText(), 16);

            DefaultComboBoxModel model = ( DefaultComboBoxModel) cmbParam09SetValue.getModel();
            int nResult = model.getSize() - 1;
            for( int i=0; i<model.getSize() - 1; i++) {
                String strCmb = ( String) model.getElementAt(i);
                strCmb = strCmb.substring( 0, 3);
                int nCmb = Integer.parseInt( strCmb);
                if( nCmb == nEdt) {
                    nResult = i;
                    break;
                }
            }
            
            cmbParam09SetValue.setSelectedIndex( nResult);
        }
        catch( NumberFormatException ex) {
            logger.warn( "При потере фокуса окна ввода парам9 произошёл NumberFormatException", ex);
        }
    }//GEN-LAST:event_edtQElement09FocusLost

    private void edtQElement10FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edtQElement10FocusLost
        try {
            int nEdt = Integer.parseInt( edtQElement10.getText(), 16);

            DefaultComboBoxModel model = ( DefaultComboBoxModel) cmbParam10SetValue.getModel();
            int nResult = model.getSize() - 1;
            for( int i=0; i<model.getSize() - 1; i++) {
                String strCmb = ( String) model.getElementAt(i);
                strCmb = strCmb.substring( 0, 3);
                int nCmb = Integer.parseInt( strCmb);
                if( nCmb == nEdt) {
                    nResult = i;
                    break;
                }
            }
            
            cmbParam10SetValue.setSelectedIndex( nResult);
        }
        catch( NumberFormatException ex) {
            logger.warn( "При потере фокуса окна ввода парам10 произошёл NumberFormatException", ex);
        }
    }//GEN-LAST:event_edtQElement10FocusLost

    private void edtQElement11FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edtQElement11FocusLost
        try {
            int nEdt = Integer.parseInt( edtQElement11.getText(), 16);

            DefaultComboBoxModel model = ( DefaultComboBoxModel) cmbParam11SetValue.getModel();
            int nResult = model.getSize() - 1;
            for( int i=0; i<model.getSize() - 1; i++) {
                String strCmb = ( String) model.getElementAt(i);
                strCmb = strCmb.substring( 0, 3);
                int nCmb = Integer.parseInt( strCmb);
                if( nCmb == nEdt) {
                    nResult = i;
                    break;
                }
            }
            
            cmbParam11SetValue.setSelectedIndex( nResult);
        }
        catch( NumberFormatException ex) {
            logger.warn( "При потере фокуса окна ввода парам11 произошёл NumberFormatException", ex);
        }
    }//GEN-LAST:event_edtQElement11FocusLost

    private void edtQElement12FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_edtQElement12FocusLost
        try {
            int nEdt = Integer.parseInt( edtQElement12.getText(), 16);

            DefaultComboBoxModel model = ( DefaultComboBoxModel) cmbParam12SetValue.getModel();
            int nResult = model.getSize() - 1;
            for( int i=0; i<model.getSize() - 1; i++) {
                String strCmb = ( String) model.getElementAt(i);
                strCmb = strCmb.substring( 0, 3);
                int nCmb = Integer.parseInt( strCmb);
                if( nCmb == nEdt) {
                    nResult = i;
                    break;
                }
            }
            
            cmbParam12SetValue.setSelectedIndex( nResult);
        }
        catch( NumberFormatException ex) {
            logger.warn( "При потере фокуса окна ввода парам12 произошёл NumberFormatException", ex);
        }
    }//GEN-LAST:event_edtQElement12FocusLost

    
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
    private javax.swing.JTextField edtQElement01;
    private javax.swing.JTextField edtQElement02;
    private javax.swing.JTextField edtQElement03;
    private javax.swing.JTextField edtQElement04;
    private javax.swing.JTextField edtQElement05;
    private javax.swing.JTextField edtQElement06;
    private javax.swing.JTextField edtQElement07;
    private javax.swing.JTextField edtQElement08;
    private javax.swing.JTextField edtQElement09;
    private javax.swing.JTextField edtQElement10;
    private javax.swing.JTextField edtQElement11;
    private javax.swing.JTextField edtQElement12;
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
