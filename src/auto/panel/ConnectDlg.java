/* Copyright (C) 2013 Interactive Brokers LLC. All rights reserved.  This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package auto.panel;

import java.awt.event.*;

import javax.swing.*;

public class ConnectDlg extends JDialog {
    public static int LAST_CLIENT_ID = 0;
    private AutoFrame m_parent;
    boolean m_rc; //flag,if success = ture,unsuccess = false
    String m_retIpAddress; //ip address
    int m_retPort; //port
    int m_retClientId; //client id

    private JTextField ip_JTextField;
    private JTextField port_JTextField;
    private JTextField client_JTextField;
    private JPanel dlg_JPanel; //main dialog
    private JLabel ip_JLabel;
    private JLabel port_JLabel;
    private JLabel client_JLabel;
    private JButton ok_JButton;
    private JButton cancel_JButton;

    public ConnectDlg(AutoFrame owner) {
        super(owner, true);
        m_parent = owner;

        setTitle("Connect");
        add(dlg_JPanel);
        pack();

        setResizable(false);
        setLocationRelativeTo(owner);
        ok_JButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOk();
            }
        });
        cancel_JButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });
    }

    void onOk() {
        m_rc = false;

        try {
            // set id
            m_retIpAddress = ip_JTextField.getText();
            m_retPort = Integer.parseInt(port_JTextField.getText());
            m_retClientId = Integer.parseInt(client_JTextField.getText());
            LAST_CLIENT_ID = m_retClientId;
        } catch (Exception e) {
            Main.inform(this, "Error - " + e);
            return;
        }

        m_rc = true;
        setVisible(false);
    }

    void onCancel() {
        LAST_CLIENT_ID = Integer.parseInt(client_JTextField.getText());

        m_rc = false;
        setVisible(false);
    }

}
