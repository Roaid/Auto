/* Copyright (C) 2013 Interactive Brokers LLC. All rights reserved.  This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package auto.panel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.*;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.ib.client.TagValue;
import com.ib.client.DeltaNeutralContract;
import com.ib.contracts.StkContract;

public class DataDlg extends JDialog {
    final static String ALL_GENERIC_TICK_TAGS = "100,101,104,105,106,107,165,221,225,233,236,258,293,294,295,318";
    final static int OPERATION_INSERT = 0;
    final static int OPERATION_UPDATE = 1;
    final static int OPERATION_DELETE = 2;

    final static int SIDE_ASK = 0;
    final static int SIDE_BID = 1;

    private int m_type = 0;   //Apply data type 0 = stock , 1 = option
    public boolean m_rc;  //flag,if success = ture,unsuccess = false

    private JPanel top_JPanel;
    private JComboBox type_JComboBox;
    private JButton ok_JButton;
    private JButton cancel_JButton;
    private JPanel dlg_JPanel;
    private JPanel out_JPanel;
    private JPanel tab1_JPanel;
    private JPanel tab2_JPanel;
    private JTextField symbol_JTextField;
    private JTextField textField2;
    private JTextField textField3;
    private JTextField textField4;
    private JTextField textField5;
    private JTextField textField6;
    private JTabbedPane tabbedPane1;


    public int m_id;
    public String m_backfillEndTime;
    public String m_backfillDuration;
    public String m_barSizeSetting;
    public int m_useRTH;
    public int m_formatDate;
    public int m_marketDepthRows;
    public String m_whatToShow;
    public Contract m_contract;
    public Order m_order = new Order();
    public DeltaNeutralContract m_underComp = new DeltaNeutralContract();
    public int m_exerciseAction;
    public int m_exerciseQuantity;
    public int m_override;
    public int m_marketDataType;
    private String m_optionsDlgTitle;
    private Vector<TagValue> m_options = new Vector<TagValue>();


    private JButton m_btnOptions = new JButton("Options");

    private JButton m_ok = new JButton("OK");
    private JButton m_cancel = new JButton("Cancel");
    private AutoFrame m_parent;


    public String m_genericTicks;
    public boolean m_snapshotMktData;

    private static final int COL1_WIDTH = 30;
    private static final int COL2_WIDTH = 100 - COL1_WIDTH;
    private JButton button2;
    private JButton button3;
    private JPanel choice1_JPanel;


    public DataDlg(AutoFrame owner) {
        super(owner, true);
        m_parent = owner;

        setTitle("Connect");
        add(dlg_JPanel);
        pack();

        setResizable(false);
        setLocationRelativeTo(owner);
        type_JComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String item = (String) type_JComboBox.getSelectedItem();
                if ("Stock".equals(item)) {
                    m_parent.message("Chose stock\n");
                    m_type = 0;
                    //tabbedPane1.setSelectedIndex(0);
                    tab2_JPanel.setVisible(false);
                    tab1_JPanel.setVisible(true);

                    //TODO
                } else if ("Option".equals(item)) {
                    m_parent.message("Chose option\n");
                    m_type = 1;
                    tab1_JPanel.setVisible(false);
                    tab2_JPanel.setVisible(true);
                    //TODO
                } else {
                    m_parent.error("Invalid Type.\n");
                }
            }
        });
        ok_JButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    if (m_type == 0) {
                        m_contract = new StkContract(symbol_JTextField.getText());
                    } else if (m_type == 1) {
                        // TODO
                    }
                } catch (Exception es) {
                    error(es);
                    return;
                }
                m_rc = true;
                setVisible(false);
            }
        });
        cancel_JButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                m_rc = false;
                setVisible(false);
            }
        });
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
        m_id = current_id;
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

    public void error(Exception ex) {
        // do not report exceptions if we initiated disconnect

        Main.inform(this, "Error - " + ex.toString());

    }

}
