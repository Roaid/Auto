package auto.panel;

import org.springframework.stereotype.*;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

import static com.sun.javafx.fxml.expression.Expression.add;

/**
 * Created by Fang on 2016/9/13.
 */
public class MainPanel {
    protected JPanel main_Panel;

    public JTabbedPane getMaintab_JTabbedPanel() {
        return maintab_JTabbedPanel;
    }

    public void setMaintab_JTabbedPanel(JTabbedPane maintab_JTabbedPanel) {
        this.maintab_JTabbedPanel = maintab_JTabbedPanel;
    }

    private JTabbedPane maintab_JTabbedPanel;
    private JPanel stock_JPanel;
    private JPanel strategy_JPanel;
    private JTable stock_JTable;
    private JTable strategy_JTable;
    private JScrollPane stock_JScrollPane;
    private JScrollPane strategy_JScrollPane;

    public JTextArea getInfo_JTextArea() {
        return info_JTextArea;
    }

    private JTextArea info_JTextArea;


    private DefaultTableModel model_DefaultTableModel1;
    private DefaultTableModel model_DefaultTableModel2;
    private AutoFrame autoframe;

    public JTable getStock_JTable() {
        return stock_JTable;
    }

    public JTable getStrategy_JTable() {
        return strategy_JTable;
    }

    public DefaultTableModel getModel_DefaultTableModel1() {
        return model_DefaultTableModel1;
    }

    public DefaultTableModel getModel_DefaultTableModel2() {
        return model_DefaultTableModel2;
    }

    public MainPanel(AutoFrame autoframe) {
        this.autoframe = autoframe;

        Object[][] data = null;

        String[] columnNames = {"ID", "Stock", "bid price", "ask price"};

        model_DefaultTableModel1 = new DefaultTableModel(data, columnNames);
        stock_JTable.setModel(model_DefaultTableModel1);
        float[] columnWidthPercentage = {0.1f, 0.4f, 0.3f, 0.3f};
        Dimension a = stock_JTable.getPreferredSize();
        double tW = a.getWidth();
        for (int i = 0; i < 4; i++) {
            double pWidth = Math.round(columnWidthPercentage[i] * tW);
            stock_JTable.getColumnModel().getColumn(i).setPreferredWidth((int) pWidth);
        }


        Object[][] data2 = null;
        String[] columnNames2 = {"ID", "Code", "Type", "Status"};
        model_DefaultTableModel2 = new DefaultTableModel(data2, columnNames2);
        strategy_JTable.setModel(model_DefaultTableModel2);
        Dimension b = stock_JTable.getPreferredSize();
        double tW2 = b.getWidth();
        for (int i = 0; i < 4; i++) {
            double pWidth = Math.round(columnWidthPercentage[i] * tW2);
            strategy_JTable.getColumnModel().getColumn(i).setPreferredWidth((int) pWidth);
        }

        autoframe.setContentPane(main_Panel);
    }

}
