/* Yi Fang personal test use. */

package auto.panel;

import auto.model.Proportion;
import auto.model.Stock;
import auto.model.Strategy;
import auto.service.AutoService;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
//import java.awt.event.WindowAdapter;
//import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.*;

import auto.service.AutoServiceImpl;
import com.ib.client.*;


class AutoFrame extends JFrame implements EWrapper {

    AutoService autoService;

    private int current_id; //Current id.ID represents a unique sign for each connection.
    private int strategy_id;
    public boolean m_bIsFAAccount = false; //The status of FA(Financial advisor) account.
    public String m_FAAcctCodes;
    private boolean m_disconnectInProgress = false;  //The status of disconnect progress


    private EJavaSignal m_signal = new EJavaSignal();
    private EClientSocket m_client = new EClientSocket(this, m_signal);
    private EReader m_reader;  //EReader class is the class in charge of reading and parsing the raw messages from the TWS

    //private DataDlg m_dataDlg = new DataDlg(this);  //Create a new data dialog
    private OrderDlg m_orderDlg = new OrderDlg(this);  //Create a new order dialog
    private Vector<TagValue> m_mktDataOptions = new Vector<TagValue>();
    private HashMap<Integer, Stock> m_mapStock = new HashMap<Integer, Stock>();
    private HashMap<Integer, Strategy> m_mapStrategy = new HashMap<Integer, Strategy>();


    private static final int NOT_AN_FA_ACCOUNT_ERROR = 321;
    private int faErrorCodes[] = {503, 504, 505, 522, 1100, NOT_AN_FA_ACCOUNT_ERROR};
    private boolean faError;
    private HashMap<Integer, MktDepthDlg> m_mapRequestToMktDepthDlg = new HashMap<Integer, MktDepthDlg>();
    private DefaultTableModel model, model2;
    private JTable table, table2;
    private AccountDlg m_acctDlg = new AccountDlg(this);
    private MainPanel mainPanel;  //
    private JMenuItem itemConnect1, itemConnect2;
    private FileOutputStream fs; //save dispensable information
    PrintStream p;
    int test_flag = 0;

    AutoFrame() throws FileNotFoundException {
        autoService = new AutoServiceImpl();
        //MenuBar
        JMenuBar menuBar = new JMenuBar();
        //First menu****************************************************************
        JMenu menu = new JMenu("Initialize");

        itemConnect1 = new JMenuItem("Connect");
        itemConnect1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onConnect();
            }
        });
        menu.add(itemConnect1);

        itemConnect2 = new JMenuItem("Disconnect");
        itemConnect2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onDisconnect();
            }
        });
        itemConnect2.setEnabled(false);
        menu.add(itemConnect2);

        JMenuItem itemConnect = new JMenuItem("Req account");
        itemConnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onReqAcctData();
            }
        });
        menu.add(itemConnect);

        menuBar.add(menu);

        //Second menu****************************************************************
        menu = new JMenu("Contract");

        itemConnect = new JMenuItem("Apply data");
        itemConnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onReqMktData();
            }
        });
        menu.add(itemConnect);

        itemConnect = new JMenuItem("Save stock");
        itemConnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveStocks();
            }
        });
        menu.add(itemConnect);
        menuBar.add(menu);

        //Third menu****************************************************************
        menu = new JMenu("Strategy");

        itemConnect = new JMenuItem("Add a stragegy");
        itemConnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addAStrategy();
            }
        });
        menu.add(itemConnect);

        itemConnect = new JMenuItem("Start stragegies");
        itemConnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startAStrategy();
            }
        });
        menu.add(itemConnect);

        itemConnect = new JMenuItem("Stop stragegies");
        itemConnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopAStrategy();
            }
        });
        menu.add(itemConnect);

        itemConnect = new JMenuItem("Save stragegies");
        itemConnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveStrategies();
            }
        });
        menu.add(itemConnect);

        itemConnect = new JMenuItem("Load stragegies");
        itemConnect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadStrategies();
            }
        });
        menu.add(itemConnect);
        menuBar.add(menu);
        //Third menu****************************************************************
        setJMenuBar(menuBar);

        mainPanel = new MainPanel(this);
        model = mainPanel.getModel_DefaultTableModel();
        model2 = mainPanel.getModel_DefaultTableModel2();
        table = mainPanel.getStock_JTable();
        table2 = mainPanel.getStrategy_JTable();
        setContentPane(mainPanel.main_Panel);

        setSize(800, 700);
        setTitle("Auto Trader");
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        //initialize
        strategy_id = 1;
        fs = new FileOutputStream(new File("src\\log.txt"));
        p = new PrintStream(fs);
    }


    //Send processor
    private void onConnect() {
        if (m_client.isConnected()) {
            error("Error: Already have a connection.\n");
            return;
        }
        m_bIsFAAccount = false;
        // get connection parameters
        ConnectDlg dlg = new ConnectDlg(this);
        dlg.setVisible(true);
        if (!dlg.m_rc) {
            return;
        }

        // connect to TWS
        m_disconnectInProgress = false;

        m_client.eConnect(dlg.m_retIpAddress, dlg.m_retPort, dlg.m_retClientId);
        if (m_client.isConnected()) {
            message("Connected to Tws server version " + m_client.serverVersion() + " at " + m_client
                    .TwsConnectionTime() + "\n");
            itemConnect1.setEnabled(false);
            itemConnect2.setEnabled(true);
        } else {
            error("Error: Connect unsuccessful!\n");
        }

        m_reader = new EReader(m_client, m_signal);

        m_reader.start();

        new Thread() {
            public void run() {
                processMessages();

                int i = 0;
                System.out.println(i);
            }
        }.start();
    }

    private void processMessages() {

        while (m_client.isConnected()) {
            m_signal.waitForSignal();
            try {
                m_reader.processMsgs();
            } catch (Exception e) {
                //error("Testing!");
                error(e);
            }
        }
    }

    private void onDisconnect() {
        // disconnect from TWS
        if (m_client.isConnected()) {
            m_disconnectInProgress = true;
            m_client.eDisconnect();
            if (!m_client.isConnected()) {
                message("Message: Disconnect successful.\n");
                itemConnect1.setEnabled(true);
                itemConnect2.setEnabled(false);
            } else {
                error("Error: Disconnect unsuccessful!\n");
            }
        } else {
            error("Error: No available connect to shut down!\n");
        }
    }

    private void onReqMktData() {   //Request market data method
        DataDlg m_dataDlg = new DataDlg(this);      //Create a new DataDlg dialogue
        m_dataDlg.init("Mkt Data Options", true, "Market Data Options", m_mktDataOptions, current_id);
        m_dataDlg.setVisible(true);

        if (!m_dataDlg.m_rc) {  //Check the activity of dialogue
            return;
        }

        m_mktDataOptions = m_dataDlg.getOptions(); //Option section

        //Iterator it = m_mapStock.entrySet().iterator();
        boolean hadone = false;
        for (Stock value : m_mapStock.values()) {
            if (value.compareTo(m_dataDlg.m_contract.symbol()) == 0) {
                hadone = true;
            }
        }

        if (!hadone) {
            m_client.reqContractDetails(m_dataDlg.m_id, m_dataDlg.m_contract);
            current_id++;
        } else {
            error("Already have this ID.Try a new one.\n");
        }
    }

    private void onReqAcctData() {
        AcctUpdatesDlg dlg = new AcctUpdatesDlg(this);

        dlg.setVisible(true);

        if (dlg.m_subscribe) {
            m_acctDlg.accountDownloadBegin(dlg.m_acctCode);
        }

        m_client.reqAccountUpdates(dlg.m_subscribe, dlg.m_acctCode);

        if (m_client.isConnected() && dlg.m_subscribe) {
            m_acctDlg.reset();
            m_acctDlg.setVisible(true);
        }
    }

    private void saveStocks() {
        if (m_mapStock.size() < 1) {
            error("No stocks can be saved.");
        } else {
            autoService = new AutoServiceImpl();
            autoService.saveStocks(m_mapStock);
        }
    }

    private void addAStrategy() {
        Proportion prop1 = new Proportion(); //Create a new proportion strategy
        int rows = table.getSelectedRowCount(); //Get the number of the selected rows in main table
        if (rows < 2) {
            error("Unselected enough stocks\n"); //Print out error message.
            return;
        } else if (rows > 2) {
            error("Selected too many stocks\n"); //Print out error message.
            return;
        } else {
            int[] row_index = table.getSelectedRows();
            int id0 = Integer.parseInt(table.getValueAt(row_index[0], 0).toString());
            int id1 = Integer.parseInt(table.getValueAt(row_index[1], 0).toString());
            int conid0 = m_mapStock.get(id0).getContract().conid();
            int conid1 = m_mapStock.get(id1).getContract().conid();
            if (conid0 > conid1) {
                prop1.setStock1(m_mapStock.get(id1));
                prop1.setStock2(m_mapStock.get(id0));
            } else {
                prop1.setStock1(m_mapStock.get(id0));
                prop1.setStock2(m_mapStock.get(id1));
            }
            String prop_code;
            prop_code = prop1.getStock1().getCode() + "," + prop1.getStock2().getCode();
            prop1.setCode(prop_code);
        }


        //test set volumes.
        prop1.setVolume1(350);
        prop1.setVolume2(574);
        m_mapStrategy.put(strategy_id, prop1);
        String type = m_mapStrategy.get(strategy_id).getClass().toString().replaceFirst("class auto.model.", "");
        model2.addRow(new Object[]{strategy_id, m_mapStrategy.get(strategy_id).getCode(), type,
                getStatusText(m_mapStrategy.get(strategy_id).getStatus())});
        strategy_id++;
        mainPanel.getMaintab_JTabbedPanel().setSelectedIndex(1);
    }

    private void startAStrategy() {

        if (m_client.isConnected()) {   //Check connection firstly.

            int rows = table2.getSelectedRowCount(); //Get the number of the selected rows in main table
            if (rows < 1) {
                error("At least select one strategy\n"); //Print out error message.
                return;
            } else {
                Strategy strategy;
                int[] row_index = table2.getSelectedRows();
                for (int i = 0; i < row_index.length; i++) {
                    strategy = m_mapStrategy.get(table2.getValueAt(row_index[i], 0));
                    strategy.setStatus(1);    //Status 1: waiting
                    table2.setValueAt(getStatusText(strategy.getStatus()), row_index[i], 3);
                    String type = table2.getValueAt(row_index[i], 2).toString().toLowerCase(); //Get strategy type
                    switch (type) {
                        case "proportion":
                            Proportion prop1 = (Proportion) strategy;
                            int finalI = i;
                            prop1.setTimer(new java.util.Timer());
                            prop1.getTimer().schedule(
                                    new java.util.TimerTask() {
                                        @Override
                                        public void run() {
                                            if (prop1.getStock1().getBpirce() <= 0 || prop1.getStock1().getApirce() <= 0 || prop1.getStock2()
                                                    .getBpirce() <= 0 || prop1.getStock2().getApirce() <= 0) {
                                                message("Strategy " + table2.getValueAt(row_index[finalI], 0)
                                                        .toString() + " : Do  not have a valid price.\n");
                                            } else if (prop1.getStatus() == 2) { //proportion submitted an
                                                // order and was waiting for an confirmation.
                                                message("Strategy " + table2.getValueAt(row_index[finalI], 0)
                                                        .toString() + " : Waiting an confirmation for submitted " +
                                                        "order.\n");
                                            } else if (prop1.getStatus() == 3) {  //proportion submitted an
                                                // order and has been confirmed.
                                                message("Strategy " + table2.getValueAt(row_index[finalI], 0)
                                                        .toString() + " : Has had an open order.\n");
                                            } else if (prop1.compare()) {
                                                message("Strategy " + table2.getValueAt(row_index[finalI], 0)
                                                        .toString() + " : Try send an order.\n");
                                                m_client.placeOrder(current_id, prop1.getC_contract(), prop1.getOrder());
                                                prop1.setOrder_id(current_id);
                                                current_id++;
                                                prop1.setStatus(2);    //Status 2: PreSubmitted or Submitted -
                                                // proportion submitted an order and was waiting for an confirmation.
                                            } else {
                                                /*message("Strategy " + table2.getValueAt(row_index[finalI], 0)
                                                        .toString() + " : Nothing to do.\n");*/
                                            }

                                        }
                                    }, 0, 5000
                            );
                            break;

                    }


                }
            }
        } else {
            message("No connection now.\n");
        }

    }

    private void stopAStrategy() {

    }


    private void saveStrategies() {
        autoService = new AutoServiceImpl();
        autoService.saveStrategies(m_mapStrategy);
    }

    private void loadStrategies() {
        autoService = new AutoServiceImpl();
        ArrayList<Strategy> tmp = autoService.loadStrategies();

        // m_mapStrategy

    }

    private String getStatusText(int a) {
        switch (a) {
            case 0:
                return "Stop";
            case 1:
                return "Waiting";
            case 2:
                return "PendingSubmit";
            case 3:
                return "PreSubmitted or Submitted";
            case 4:
                return "Cancelled";
            case 5:
                return "Filled";
            case 6:
                return "Inactive";
            default:
                return "Unknown";
        }
    }

    //Receive processor
    @Override
    public void managedAccounts(String accountsList) {
        System.out.println("*************************");
        System.out.println("AutoFrame-managedAccounts");
        m_bIsFAAccount = true;
        m_FAAcctCodes = accountsList;
        String msg = EWrapperMsgGenerator.managedAccounts(accountsList);
        System.out.println(accountsList);
        System.out.println("*************************");
    }

    @Override
    public void nextValidId(int orderId) {
        System.out.println("*************************");
        System.out.println("AutoFrame-nextValidId");
        // received next valid order id
        String msg = EWrapperMsgGenerator.nextValidId(orderId);

        //m_orderDlg.setIdAtLeast( orderId);
        System.out.println(orderId);
        current_id = orderId;
        System.out.println("*************************");
    }

    @Override
    public void error(Exception ex) {
        // do not report exceptions if we initiated disconnect
        if (!m_disconnectInProgress) {
            String msg = EWrapperMsgGenerator.error(ex);
            Main.inform(this, msg);
        }
    }

    @Override
    public void error(String str) {
        String msg = EWrapperMsgGenerator.error(str);
        Main.inform(this, msg);
        //mainPanel.getInfo_JTextArea().append(msg);
    }

    public void message(String str) {
        mainPanel.getInfo_JTextArea().append(str);
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {
        String msg = EWrapperMsgGenerator.error(id, errorCode, errorMsg) + System.lineSeparator();
        mainPanel.getInfo_JTextArea().append(msg);
        for (int ctr = 0; ctr < faErrorCodes.length; ctr++) {
            faError |= (errorCode == faErrorCodes[ctr]);
        }
        if (errorCode == MktDepthDlg.MKT_DEPTH_DATA_RESET) {

            MktDepthDlg depthDialog = m_mapRequestToMktDepthDlg.get(id);
            if (depthDialog != null) {
                depthDialog.reset();
            } else {
                System.err.println("cannot find dialog that corresponds to request id [" + id + "]");
            }
        }
    }

    @Override
    public void connectionClosed() {
        String msg = EWrapperMsgGenerator.connectionClosed();
        itemConnect1.setEnabled(true);
        itemConnect2.setEnabled(false);
        Main.inform(this, msg);
    }

    @Override
    public void connectAck() {

    }

    @Override
    public void positionMulti(int reqId, String account, String modelCode, Contract contract, double pos, double avgCost) {

    }

    @Override
    public void positionMultiEnd(int reqId) {

    }

    @Override
    public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value, String currency) {

    }

    @Override
    public void accountUpdateMultiEnd(int reqId) {

    }

    @Override
    public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId, String tradingClass, String multiplier, Set<String> expirations, Set<Double> strikes) {

    }

    @Override
    public void securityDefinitionOptionalParameterEnd(int reqId) {

    }

    @Override
    public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {

    }

    @Override
    public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
        //message(tickerId + " -> " + field + " -> " + price + " -> " + canAutoExecute + "\n");

        //p.println(tickerId + " -> " + field + " -> " + price + " -> " + canAutoExecute);

        if (field == 1 || field == 2) {
            Stock value = m_mapStock.get(tickerId);
            switch (field) {
                case 1:
                    value.setBpirce(price);
                    break;
                case 2:
                    value.setApirce(price);
                    break;
            }
            m_mapStock.put(tickerId, value);
            int rows = table.getRowCount();
            for (int i = 0; i < rows; i++) {
                int id = Integer.parseInt(table.getValueAt(i, 0).toString());
                if (id == tickerId) {
                    switch (field) {
                        case 1:
                            table.setValueAt(value.getBpirce(), i, 2);
                            break;
                        case 2:
                            table.setValueAt(value.getApirce(), i, 3);
                            break;
                    }
                }
            }
        }


    }

    @Override
    public void tickSize(int tickerId, int field, int size) {
    }

    @Override
    public void tickOptionComputation(int tickerId, int field, double impliedVol,
                                      double delta, double optPrice, double pvDividend,
                                      double gamma, double vega, double theta, double undPrice) {
    }

    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {
    }

    @Override
    public void tickString(int tickerId, int tickType, String value) {
    }

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints,
                        String formattedBasisPoints, double impliedFuture, int holdDays,
                        String futureExpiry, double dividendImpact, double dividendsToExpiry) {
    }

    @Override
    public void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
        p.print("This is an status report of order: \n");
        String msg = EWrapperMsgGenerator.orderStatus(orderId, status, filled, remaining, avgFillPrice, permId, parentId,
                lastFillPrice, clientId, whyHeld);
        p.print(msg);

        if (m_mapStrategy.size() == 0) {
            return;
        } else {
            int position = -1;  //store row number.
            int index = -1;  //store hashmap key from certain row.
            int rows = table2.getRowCount();
            for (int i = 0; i < rows; i++) {
                int index_tmp = Integer.parseInt(table2.getValueAt(i, 0).toString());
                if (m_mapStrategy.get(index_tmp).getOrder_id() == orderId) {
                    position = i;
                    index = index_tmp;
                }
            }
            if (position >= 0) {
                switch (status) {
                    case "PreSubmitted":
                    case "Submitted":
                        m_mapStrategy.get(index).setStatus(3); //PreSubmitted or Submitted
                        table2.setValueAt(getStatusText(3), position, 3);
                        break;
                    case "Filled":
                        if (remaining == 0) {
                            m_mapStrategy.get(index).setStatus(1); //1 waiting
                            m_mapStrategy.get(index).fillorder();  //modify the strategy based on filled information.
                            table2.setValueAt(getStatusText(1), position, 3);
                            test_flag = 1;  //test code
                        } else {
                            m_mapStrategy.get(index).setStatus(5); //5 Filled
                            table2.setValueAt(getStatusText(5), position, 3);
                        }
                        break;
                }
            } else {
                message("No such order in recordings.\n");
            }
        }
    }


    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
        message("This is an openOrder: " + orderId + "\n");

        /*if (m_mapStrategy.size() == 0 ) {
            return;
        } /*else if (m_mapStrategy.get(orderId).getStatus() != 2) {
            m_mapStrategy.get(orderId).setStatus(2);
        }*/
    }

    @Override
    public void openOrderEnd() {
        String msg = "!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\nFinish open order.\n";
        message(msg);
    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        m_acctDlg.updateAccountValue(key, value, currency, accountName);
    }

    @Override
    public void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
    }

    @Override
    public void updateAccountTime(String timeStamp) {
        m_acctDlg.updateAccountTime(timeStamp);
    }

    @Override
    public void accountDownloadEnd(String accountName) {
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {
        Stock tmp = new Stock(contractDetails.contract().symbol());
        tmp.setContract(contractDetails.contract());
        m_mapStock.put(reqId, tmp);
        model.addRow(new Object[]{reqId, m_mapStock.get(reqId).getCode(), m_mapStock.get(reqId).getBpirce(), m_mapStock.get(reqId).getApirce()});
        m_client.reqMktData(reqId, contractDetails.contract(), "", false, m_mktDataOptions);

    }

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {
    }

    @Override
    public void contractDetailsEnd(int reqId) {

        p.println("finish: " + reqId + "\n");
    }

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {
        //message("executed: " + reqId + "\n");
        String msg = EWrapperMsgGenerator.execDetails(reqId, contract, execution);
        message(msg);
    }

    @Override
    public void execDetailsEnd(int reqId) {
        message("finish executed: " + reqId + "\n");
    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
    }

    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation,
                                 int side, double price, int size) {
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
    }

    @Override
    public void receiveFA(int faDataType, String xml) {
    }

    @Override
    public void historicalData(int reqId, String date, double open, double high, double low,
                               double close, int volume, int count, double WAP, boolean hasGaps) {
    }

    @Override
    public void scannerParameters(String xml) {
    }

    @Override
    public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance,
                            String benchmark, String projection, String legsStr) {
    }

    @Override
    public void scannerDataEnd(int reqId) {
    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
    }

    @Override
    public void currentTime(long time) {
    }

    @Override
    public void fundamentalData(int reqId, String data) {
    }

    @Override
    public void deltaNeutralValidation(int reqId, DeltaNeutralContract underComp) {
    }

    @Override
    public void tickSnapshotEnd(int reqId) {
    }

    @Override
    public void marketDataType(int reqId, int marketDataType) {
    }

    @Override
    public void commissionReport(CommissionReport commissionReport) {
    }

    @Override
    public void position(String account, Contract contract, double pos, double avgCost) {

    }

    @Override
    public void positionEnd() {
    }

    @Override
    public void accountSummary(int reqId, String account, String tag, String value, String currency) {
    }

    @Override
    public void accountSummaryEnd(int reqId) {
    }

    @Override
    public void verifyMessageAPI(String apiData) {
    }

    @Override
    public void verifyCompleted(boolean isSuccessful, String errorText) {
    }

    @Override
    public void verifyAndAuthMessageAPI(String apiData, String xyzChallange) {

    }

    @Override
    public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {

    }

    @Override
    public void displayGroupList(int reqId, String groups) {
    }

    @Override
    public void displayGroupUpdated(int reqId, String contractInfo) {
    }
}
