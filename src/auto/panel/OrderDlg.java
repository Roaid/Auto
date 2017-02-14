/* Copyright (C) 2013 Interactive Brokers LLC. All rights reserved.  This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package auto.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.ib.client.*;
//import com.ib.client.UnderComp;

public class OrderDlg extends JDialog {
    final static String ALL_GENERIC_TICK_TAGS = "100,101,104,105,106,107,165,221,225,233,236,258,293,294,295,318";
    final static int OPERATION_INSERT = 0;
    final static int OPERATION_UPDATE = 1;
    final static int OPERATION_DELETE = 2;

    final static int SIDE_ASK = 0;
    final static int SIDE_BID = 1;

    public boolean m_rc;
    public int m_id;
    public String m_backfillEndTime;
    public String m_backfillDuration;
    public String m_barSizeSetting;
    public int m_useRTH;
    public int m_formatDate;
    public int m_marketDepthRows;
    public String m_whatToShow;
    public Contract m_contract = new Contract();
    public Order m_order = new Order();
    public DeltaNeutralContract m_underComp = new DeltaNeutralContract();
    public int m_exerciseAction;
    public int m_exerciseQuantity;
    public int m_override;
    public int m_marketDataType;
    private String m_optionsDlgTitle;
    private Vector<TagValue> m_options = new Vector<TagValue>();

    private JTextField m_Id = new JTextField();  //Next order id
    private JTextField m_BackfillEndTime = new JTextField(22);
    private JTextField m_BackfillDuration = new JTextField("1 M");
    private JTextField m_BarSizeSetting = new JTextField("1 day");
    private JTextField m_UseRTH = new JTextField("1");
    private JTextField m_FormatDate = new JTextField("1");
    private JTextField m_WhatToShow = new JTextField("TRADES");
    private JTextField m_conId = new JTextField();
    private JTextField m_symbol = new JTextField("QQQQ");
    private JTextField m_secType = new JTextField("STK");
    private JTextField m_expiry = new JTextField();
    private JTextField m_strike = new JTextField("0");
    private JTextField m_right = new JTextField();
    private JTextField m_multiplier = new JTextField("");
    private JTextField m_exchange = new JTextField("SMART");
    private JTextField m_primaryExch = new JTextField("ISLAND");
    private JTextField m_currency = new JTextField("USD");
    private JTextField m_localSymbol = new JTextField();
    private JTextField m_tradingClass = new JTextField();
    private JTextField m_includeExpired = new JTextField("0");
    private JTextField m_secIdType = new JTextField();
    private JTextField m_secId = new JTextField();
    private JTextField m_action = new JTextField("BUY");
    private JTextField m_totalQuantity = new JTextField("10");
    private JTextField m_orderType = new JTextField("LMT");
    private JTextField m_lmtPrice = new JTextField("40");
    private JTextField m_auxPrice = new JTextField("0");
    private JTextField m_goodAfterTime = new JTextField();
    private JTextField m_goodTillDate = new JTextField();
    private JTextField m_marketDepthRowTextField = new JTextField("20");
    private JTextField m_genericTicksTextField = new JTextField(ALL_GENERIC_TICK_TAGS);
    private JCheckBox m_snapshotMktDataTextField = new JCheckBox("Snapshot", false);
    private JTextField m_exerciseActionTextField = new JTextField("1");
    private JTextField m_exerciseQuantityTextField = new JTextField("1");
    private JTextField m_overrideTextField = new JTextField("0");
    private JComboBox m_marketDataTypeCombo = new JComboBox(MarketDataType.getFields());

    private JButton m_sharesAlloc = new JButton("FA Allocation Info...");
    private JButton m_comboLegs = new JButton("Combo Legs");
    private JButton m_btnUnderComp = new JButton("Delta Neutral");
    private JButton m_btnAlgoParams = new JButton("Algo Params");
    private JButton m_btnSmartComboRoutingParams = new JButton("Smart Combo Routing Params");
    private JButton m_btnOptions = new JButton("Options");

    private JButton m_ok = new JButton("OK");
    private JButton m_cancel = new JButton("Cancel");
    private AutoFrame m_parent;

    private String m_faGroup;
    private String m_faProfile;
    private String m_faMethod;
    private String m_faPercentage;
    public String m_genericTicks;
    public boolean m_snapshotMktData;

    private static final int COL1_WIDTH = 30;
    private static final int COL2_WIDTH = 100 - COL1_WIDTH;

    public void faGroup(String s) {
        m_faGroup = s;
    }

    public void faProfile(String s) {
        m_faProfile = s;
    }

    public void faMethod(String s) {
        m_faMethod = s;
    }

    public void faPercentage(String s) {
        m_faPercentage = s;
    }

    private static void addGBComponent(IBGridBagPanel panel, Component comp,
                                       GridBagConstraints gbc, int weightx, int gridwidth) {
        gbc.weightx = weightx;
        gbc.gridwidth = gridwidth;
        panel.setConstraints(comp, gbc);
        panel.add(comp, gbc);
    }

    public OrderDlg(AutoFrame owner) {
        super(owner, true);

        m_parent = owner;
        setTitle("Sample");

        java.awt.GridBagConstraints gbc = new java.awt.GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.weighty = 100;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.gridheight = 1;
        // create id panel
        IBGridBagPanel pId = new IBGridBagPanel();
        pId.setBorder(BorderFactory.createTitledBorder("Message Id"));

        addGBComponent(pId, new JLabel("Id"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pId, m_Id, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);

        // create contract panel
        IBGridBagPanel pContractDetails = new IBGridBagPanel();
        pContractDetails.setBorder(BorderFactory.createTitledBorder("Contract Info"));
        addGBComponent(pContractDetails, new JLabel("Contract Id"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_conId, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);
        addGBComponent(pContractDetails, new JLabel("Symbol"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_symbol, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);
        addGBComponent(pContractDetails, new JLabel("Security Type"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_secType, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);
        addGBComponent(pContractDetails, new JLabel("Expiry"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_expiry, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);
        addGBComponent(pContractDetails, new JLabel("Strike"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_strike, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);
        addGBComponent(pContractDetails, new JLabel("Put/Call"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_right, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);
        addGBComponent(pContractDetails, new JLabel("Option Multiplier"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_multiplier, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);
        addGBComponent(pContractDetails, new JLabel("Exchange"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_exchange, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);
        addGBComponent(pContractDetails, new JLabel("Primary Exchange"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_primaryExch, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);
        addGBComponent(pContractDetails, new JLabel("Currency"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_currency, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);
        addGBComponent(pContractDetails, new JLabel("Local Symbol"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_localSymbol, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);
        addGBComponent(pContractDetails, new JLabel("Trading Class"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_tradingClass, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);
        addGBComponent(pContractDetails, new JLabel("Include Expired"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_includeExpired, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);
        addGBComponent(pContractDetails, new JLabel("Sec Id Type"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_secIdType, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);
        addGBComponent(pContractDetails, new JLabel("Sec Id"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pContractDetails, m_secId, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);


        // create marketDataType panel
        IBGridBagPanel pMarketDataType = new IBGridBagPanel();
        pMarketDataType.setBorder(BorderFactory.createTitledBorder("Market Data Type"));
        addGBComponent(pMarketDataType, new JLabel("Market Data Type"), gbc, COL1_WIDTH, GridBagConstraints.RELATIVE);
        addGBComponent(pMarketDataType, m_marketDataTypeCombo, gbc, COL2_WIDTH, GridBagConstraints.REMAINDER);

        // create mid Panel
        JPanel pMidPanel = new JPanel();
        pMidPanel.setLayout(new BoxLayout(pMidPanel, BoxLayout.Y_AXIS));
        pMidPanel.add(pContractDetails, BorderLayout.CENTER);
        pMidPanel.add(pMarketDataType, BorderLayout.CENTER);

        // create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(m_ok);
        buttonPanel.add(m_cancel);

        // create action listeners

        m_ok.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOk();
            }
        });
        m_cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        // create top panel
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.add(pId);
        topPanel.add(pMidPanel);

        // create dlg box
        getContentPane().add(topPanel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        JScrollPane scroller = new JScrollPane(topPanel);
        this.add(scroller, BorderLayout.CENTER);
        pack();
    }

    private static String pad(int val) {
        return val < 10 ? "0" + val : "" + val;
    }


    void onOk() {
        m_rc = false;

        try {
            // set id
            m_id = Integer.parseInt(m_Id.getText());
            // m_parent.setCurrent_id(m_id+1);
            // set contract fields
            m_contract.conid(ParseInt(m_conId.getText(), 0));
            m_contract.symbol(m_symbol.getText());
            m_contract.secType(m_secType.getText());
            m_contract.lastTradeDateOrContractMonth(m_expiry.getText());
            m_contract.strike(ParseDouble(m_strike.getText(), 0.0));
            m_contract.right(m_right.getText());
            m_contract.multiplier(m_multiplier.getText());
            m_contract.exchange(m_exchange.getText());
            m_contract.primaryExch(m_primaryExch.getText());
            m_contract.currency(m_currency.getText());
            m_contract.localSymbol(m_localSymbol.getText());
            m_contract.tradingClass(m_tradingClass.getText());
            try {
                int includeExpired = Integer.parseInt(m_includeExpired.getText());
                m_contract.includeExpired(includeExpired == 1);
            } catch (NumberFormatException ex) {
                m_contract.includeExpired(false);
            }
            m_contract.secIdType(m_secIdType.getText());
            m_contract.secId(m_secId.getText());

            m_genericTicks = m_genericTicksTextField.getText();
            m_snapshotMktData = m_snapshotMktDataTextField.isSelected();
        } catch (Exception e) {
            Main.inform(this, "Error - " + e);
            return;
        }

        m_rc = true;
        setVisible(false);
    }

    void onCancel() {
        m_rc = false;
        setVisible(false);
    }

    public void show() {
        m_rc = false;
        super.show();
    }

    void setIdAtLeast(int id) {
        try {
            // set id field to at least id
            int curId = Integer.parseInt(m_Id.getText());
            if (curId < id) {
                m_Id.setText(String.valueOf(id));
            }
        } catch (Exception e) {
            Main.inform(this, "Error - " + e);
        }
    }

    private static int ParseInt(String text, int defValue) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    private static double ParseDouble(String text, double defValue) {
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    private static double parseStringToMaxDouble(String value) {
        if (value.trim().length() == 0) {
            return Double.MAX_VALUE;
        }
        return Double.parseDouble(value);
    }

//    void setOptionsDlgTitle(String title){
//    	m_optionsDlgTitle = title;
//    }

    void init(String btnText, boolean btnEnabled, String dlgTitle, Vector<TagValue> options, int current_id) {
        init(btnText, btnEnabled);
        m_Id.setText(String.valueOf(current_id));
        m_options = options;
        m_optionsDlgTitle = dlgTitle;
    }

    void init(String btnText, boolean btnEnabled) {
        m_btnOptions.setText(btnText);
        m_btnOptions.setEnabled(btnEnabled);
    }
//    void setOptions(Vector<TagValue> options) {
//    	m_options = options;
//    }

//    void setOptionsBtnName(String name){
//    	m_btnOptions.setText(name);
//    }

    Vector<TagValue> getOptions() {
        return m_options;
    }
//    void disableBtnOptions(){
    //      m_btnOptions.setText("Options");
//        m_btnOptions.setEnabled(false);
    //  }
//    void enableBtnOptions(){
    //  	m_btnOptions.setEnabled(true);
//    }

}
