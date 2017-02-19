/* Copyright (C) 2013 Interactive Brokers LLC. All rights reserved.  This code is subject to the terms
 * and conditions of the IB API Non-Commercial License or the IB API Commercial License, as applicable. */

package auto.panel;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.awt.Component;
import java.io.FileNotFoundException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class Main {

    // This method is called to start the application
    public static void main(String args[]) throws FileNotFoundException {
        ApplicationContext context = new ClassPathXmlApplicationContext("Beans.xml");
        AutoFrame autoframe = (AutoFrame) context.getBean("autoframe");
        autoframe.setVisible(true);

        /*
        AutoFrame AutoFrame = new AutoFrame();
        AutoFrame.setVisible(true);*/
    }

    static public void inform(final Component parent, final String str) {
        if (SwingUtilities.isEventDispatchThread()) {
            showMsg(parent, str, JOptionPane.INFORMATION_MESSAGE);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    showMsg(parent, str, JOptionPane.INFORMATION_MESSAGE);
                }
            });
        }
    }

    static private void showMsg(Component parent, String str, int type) {
        // this function pops up a dlg box displaying a message
        JOptionPane.showMessageDialog(parent, str, "IB Java Test Client", type);
    }
}
