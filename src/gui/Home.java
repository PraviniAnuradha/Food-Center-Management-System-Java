/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.table.DefaultTableModel;
import model.MYSQL;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.view.JasperViewer;

/**
 *
 * @author CHAMA COMPUTERS
 */
public class Home extends javax.swing.JFrame {

    public static String notification;

    DateTimeFormatter dtf2 = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public void loadresults(String content) {
        try {
            ResultSet rs = MYSQL.search("SELECT * FROM `invoice` INNER JOIN `customer` ON `invoice`.`customer_id`=`customer`.`id` INNER JOIN `address` ON `customer`.`id`=`address`.`customer_id` INNER JOIN `districts` ON `address`.`districts_id`=`districts`.`id` INNER JOIN `invoice_item` ON `invoice_item`.`invoice_id`=`invoice`.`id` INNER JOIN `stock` ON `invoice_item`.`stock_id`=`stock`.`id` INNER JOIN `product` ON `stock`.`product_id`=`product`.`id` INNER JOIN `order_through` ON `invoice`.`order_through_id`=`order_through`.`id` WHERE `invoice`.`code`LIKE '%" + content + "%' OR `customer`.`phone_no1` LIKE '%" + content + "%' OR `customer`.`name` LIKE '%" + content + "%' OR `product`.`name` LIKE '%" + content + "%'OR `invoice`.`uniq_id` LIKE '%" + content + "%' ORDER BY `invoice`.`code` DESC  LIMIT 20 ");
            DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
            dtm.setRowCount(0);

            while (rs.next()) {
                Vector v = new Vector();
                v.add(rs.getString("invoice.code"));
                v.add(rs.getString("invoice.uniq_id"));
                v.add(rs.getString("customer.name"));
                v.add(rs.getString("customer.phone_no1"));
                v.add(rs.getString("districts.name_en"));
                v.add(rs.getString("order_through.name"));
                v.add(rs.getString("product.name"));
                v.add(rs.getString("invoice.status"));
                dtm.addRow(v);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadresults() {
        try {
            ResultSet rs = MYSQL.search("SELECT * FROM `invoice` INNER JOIN `customer` ON `invoice`.`customer_id`=`customer`.`id` INNER JOIN `address` ON `customer`.`id`=`address`.`customer_id` INNER JOIN `districts` ON `address`.`districts_id`=`districts`.`id` INNER JOIN `invoice_item` ON `invoice_item`.`invoice_id`=`invoice`.`id` INNER JOIN `stock` ON `invoice_item`.`stock_id`=`stock`.`id` INNER JOIN `product` ON `stock`.`product_id`=`product`.`id` INNER JOIN `order_through` ON `invoice`.`order_through_id`=`order_through`.`id` WHERE `status`='Done' ORDER BY `invoice`.`code` DESC LIMIT 20");
            DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
            dtm.setRowCount(0);

            while (rs.next()) {
                Vector v = new Vector();
                v.add(rs.getString("invoice.code"));
                v.add(rs.getString("invoice.uniq_id"));
                v.add(rs.getString("customer.name"));
                v.add(rs.getString("customer.phone_no1"));
                v.add(rs.getString("districts.name_en"));
                v.add(rs.getString("order_through.name"));
                v.add(rs.getString("product.name"));
                v.add(rs.getString("invoice.status"));
                dtm.addRow(v);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadcounts() {

        LocalDateTime now = LocalDateTime.now();
        String sdate = dtf2.format(now);
        String sresult = sdate.replaceAll("[/]", "-");

        try {
            ResultSet customers_rs = MYSQL.search("SELECT COUNT(`id`) AS `customer_count` FROM `customer`");  //customer count
            ResultSet pending_rs = MYSQL.search("SELECT COUNT(`id`) AS `pending_count` FROM `invoice` WHERE  `status`!='Done' AND `status`!='Canseled'"); // product count
            ResultSet order_rs = MYSQL.search("SELECT COUNT(`id`) AS `order_count` FROM `invoice` WHERE  `status`='Done'"); // order count

            customers_rs.next();
            pending_rs.next();
            order_rs.next();

            jLabel8.setText(customers_rs.getString("customer_count"));
            jLabel7.setText(pending_rs.getString("pending_count"));
            jLabel5.setText(order_rs.getString("order_count"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void print(String id) {
        try {
            ResultSet rs = MYSQL.search("SELECT * FROM `invoice` WHERE `code`='" + id + "' ");
            while (rs.next()) {
//                System.out.println(rs.getString("id"));
//                customer data
                ResultSet customer_rs = MYSQL.search("SELECT * FROM `customer` INNER JOIN `address` ON `customer`.`id`=`address`.`customer_id` INNER JOIN `districts` ON `address`.`districts_id`=`districts`.`id` WHERE `customer`.`id`='" + rs.getString("customer_id") + "'");
//                item data
                ResultSet item_rs = MYSQL.search("SELECT * FROM `invoice_item` INNER JOIN `stock` ON `invoice_item`.`stock_id`=`stock`.`id` INNER JOIN `product` ON `stock`.`product_id`=`product`.`id` WHERE `invoice_item`.`invoice_id`='" + rs.getString("id") + "'");
//                payment data
                ResultSet payment_rs = MYSQL.search("SELECT * FROM `invoice_payment` WHERE `invoice_id`='" + rs.getString("id") + "'");

                customer_rs.next();
                payment_rs.next();
//                customer data
                String customer_name = customer_rs.getString("name");
                String customer_address = customer_rs.getString("address.content");
                String phone_no1 = customer_rs.getString("phone_no1");
                String phone_no2 = customer_rs.getString("phone_no2");
                String district = customer_rs.getString("districts.name_en");
                String ordernumber = rs.getString("code");
                String date = rs.getString("date_time");
                String waybill = rs.getString("uniq_id");

//                item data
                String[] item_data = new String[3];
                String[] item_code = new String[3];

                int i = -1;
                while (item_rs.next()) {
                    i = i + 1;
//                    System.out.println(i);
                    item_data[i] = item_rs.getString("name");
                    item_code[i] = item_rs.getString("code");
                }
//                item name
                String item_dataset = Arrays.deepToString(item_data);
                String result_itemdata = item_dataset.replaceAll("[\\[\\]\\,]", System.lineSeparator());
                String result_item_dataset = result_itemdata.replaceAll("\\s*\\bnull\\b\\s*", "");
//                item code
                String item_codeset = Arrays.deepToString(item_code);
                String result_itemcode = item_codeset.replaceAll("[\\[\\]\\,]", System.lineSeparator());
                String result_item_codeset = result_itemcode.replaceAll("\\s*\\bnull\\b\\s*", "");

//                payment data
                String amount = payment_rs.getString("payment") + "LKR";

                //adding to jasperreport
                InputStream filepath = getClass().getResourceAsStream("/reports/SaleSpot.jrxml");
                JasperReport jr = JasperCompileManager.compileReport(filepath);

                HashMap parameters = new HashMap();
                parameters.put("Parameter1", customer_name);
                parameters.put("Parameter2", customer_address);
                parameters.put("Parameter3", phone_no1);
                parameters.put("Parameter4", phone_no2);

                parameters.put("Parameter5", result_item_dataset);
                parameters.put("Parameter6", amount);
                parameters.put("Parameter7", ordernumber);
                parameters.put("Parameter8", district);
                parameters.put("Parameter9", waybill);

                JREmptyDataSource datasource = new JREmptyDataSource();
                JasperPrint jp = JasperFillManager.fillReport(jr, parameters, datasource);
//                JasperViewer.viewReport(jp, false);
                JasperPrintManager.printReport(jp, false);

            }
            JOptionPane.showMessageDialog(this, "Order Forward to print machine...", "Succcess", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void shownotifications() {
        jLabel3.setText(notification);
    }

    public void startnotoficationengine() {
        Thread t = new Thread(() -> {
            for (int i = 0; i <= 100; i++) {
                shownotifications();
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                }
            }
        });
        t.start();
    }

    public void resetstock(String code) {
        try {
            ResultSet code_rs = MYSQL.search("SELECT * FROM `invoice` WHERE `code`='" + code + "'");
            code_rs.next();

            int invo_id = code_rs.getInt("id");

            ResultSet item_rs = MYSQL.search("SELECT * FROM `invoice_item` WHERE `invoice_id`='" + invo_id + "'");

            while (item_rs.next()) {

                int stock_id = item_rs.getInt("stock_id");
                int quantity = item_rs.getInt("quantity");

                MYSQL.iud("UPDATE `stock` SET `quantity`=`quantity`+ " + quantity + " WHERE `id`='" + stock_id + "'");

            }

        } catch (Exception e) {
        }
    }

    public Home() {
        try {
            MYSQL.getConnection();
        } catch (Exception ex) {
        }
        initComponents();
        loadcounts();
        startnotoficationengine();
        loadresults();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel14 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton5 = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 32767));
        jPanel1 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jButton6 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel14.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/logo_new.png"))); // NOI18N
        jPanel14.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 20, 140, 120));

        jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/plus.png"))); // NOI18N
        jButton1.setText("Add customers");
        jButton1.setPreferredSize(new java.awt.Dimension(120, 33));
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel14.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 180, 173, -1));

        jButton2.setText("Manage Products");
        jButton2.setPreferredSize(new java.awt.Dimension(120, 33));
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel14.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 231, 173, 29));

        jButton3.setText("Manage GRN");
        jButton3.setPreferredSize(new java.awt.Dimension(120, 33));
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel14.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 280, 170, 29));

        jButton4.setText("Manage stock");
        jButton4.setPreferredSize(new java.awt.Dimension(120, 33));
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        jPanel14.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 325, 173, 29));

        jButton5.setText("Hand Over Orders");
        jButton5.setPreferredSize(new java.awt.Dimension(120, 33));
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });
        jPanel14.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 372, 173, 29));
        jPanel14.add(filler1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 278, -1, -1));

        getContentPane().add(jPanel14, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 200, 410));

        jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/exit.png"))); // NOI18N
        jButton6.setBorder(null);
        jButton6.setBorderPainted(false);
        jButton6.setContentAreaFilled(false);
        jButton6.setFocusPainted(false);
        jButton6.setFocusable(false);
        jButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton6ActionPerformed(evt);
            }
        });

        jLabel9.setText("Pravini Anuradha");

        jLabel10.setText("Welcome");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel9)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton6)
                .addGap(27, 27, 27))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel9)
                        .addComponent(jLabel10))
                    .addComponent(jButton6))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Orders", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Comic Sans MS", 0, 14))); // NOI18N

        jLabel7.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel7.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Pending", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Comic Sans MS", 0, 14))); // NOI18N

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel8.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Customers", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Comic Sans MS", 0, 14))); // NOI18N

        jPanel7.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Code", "Waybill id", "Customer ", "Phone", "District", "Order through", "Item", "Status"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setFocusable(false);
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jPanel7.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 48, 580, 201));

        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTextField1KeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });
        jPanel7.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 10, 320, 30));

        jLabel2.setText("Search");
        jPanel7.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 10, 60, 21));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 153, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(15, 15, 15))
            .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(200, 0, 580, 420));

        jLabel3.setFont(new java.awt.Font("Comic Sans MS", 0, 12)); // NOI18N
        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/alert.png"))); // NOI18N

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addGap(0, 199, Short.MAX_VALUE)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 585, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel9Layout.createSequentialGroup()
                .addGap(0, 33, Short.MAX_VALUE)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        getContentPane().add(jPanel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 430, -1, -1));

        jMenu1.setText("Home");
        jMenu1.addMenuKeyListener(new javax.swing.event.MenuKeyListener() {
            public void menuKeyPressed(javax.swing.event.MenuKeyEvent evt) {
                jMenu1MenuKeyPressed(evt);
            }
            public void menuKeyReleased(javax.swing.event.MenuKeyEvent evt) {
            }
            public void menuKeyTyped(javax.swing.event.MenuKeyEvent evt) {
            }
        });
        jMenu1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenu1MouseClicked(evt);
            }
        });
        jMenuBar1.add(jMenu1);

        jMenu2.setText("Process");
        jMenu2.addMenuKeyListener(new javax.swing.event.MenuKeyListener() {
            public void menuKeyPressed(javax.swing.event.MenuKeyEvent evt) {
                jMenu2MenuKeyPressed(evt);
            }
            public void menuKeyReleased(javax.swing.event.MenuKeyEvent evt) {
            }
            public void menuKeyTyped(javax.swing.event.MenuKeyEvent evt) {
            }
        });

        jMenuItem4.setText("Update Waybills");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem4);

        jMenuItem5.setText("Print Orders");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jMenu2.add(jMenuItem5);

        jMenuBar1.add(jMenu2);

        jMenu3.setText("Reports");

        jMenuItem1.setText("Pending Orders");
        jMenuItem1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenuItem1MouseClicked(evt);
            }
        });
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem1);

        jMenuItem2.setText("GRN Reports");
        jMenuItem2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenuItem2MouseClicked(evt);
            }
        });
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem2);

        jMenuItem3.setText("Stock Reports");
        jMenuItem3.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jMenuItem3MouseClicked(evt);
            }
        });
        jMenuItem3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem3ActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItem3);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        GRN grn = new GRN();
        grn.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        Addproducts addproducts = new Addproducts();
        addproducts.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
        Hand_over_to_courier hoc = new Hand_over_to_courier();
        hoc.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        Addcustomers addcus = new Addcustomers();
        addcus.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField1KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1KeyPressed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        // TODO add your handling code here:
        String content = jTextField1.getText();
        loadresults(content);
    }//GEN-LAST:event_jTextField1KeyReleased

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        Stock stock = new Stock();
        stock.setVisible(true);
        this.dispose();

    }//GEN-LAST:event_jButton4ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        // TODO add your handling code here:
        int r = jTable1.getSelectedRow();
        String type = jTable1.getValueAt(r, 7).toString();
        String id = jTable1.getValueAt(r, 0).toString();

        if (evt.getClickCount() == 2) {

            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Please select a grn item", "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                int option = JOptionPane.showConfirmDialog(this, "Do you want to print this order", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (option == JOptionPane.YES_OPTION) {
                    print(id);
                } else if (option == JOptionPane.NO_OPTION) {
                    int option2 = JOptionPane.showConfirmDialog(this, "Do you want to cansel this order", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                    if (option2 == JOptionPane.YES_OPTION) {
                        if (type.equals("Pending")) {
                            MYSQL.iud("UPDATE `invoice` SET `status`='Canseled' WHERE `code`='" + id + "' ");
                        } else if (type.equals("Canseled")) {
                            JOptionPane.showMessageDialog(this, "Order already canseled", "Warning", JOptionPane.WARNING_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(this, "You can't cansel dilivered orders", "Warning", JOptionPane.WARNING_MESSAGE);
                        }
                    }
                }
            }
        } else if (evt.getClickCount() == 1) {

            String name = jTable1.getValueAt(r, 2).toString();
            String code = jTable1.getValueAt(r, 0).toString();
            String waybill = jTable1.getValueAt(r, 1).toString();
            String item = jTable1.getValueAt(r, 6).toString();
            String district = jTable1.getValueAt(r, 4).toString();

            JButton button1 = new JButton("Print");
            button1.addActionListener((ActionEvent e) -> {
                print(id);
            });

            JButton button2 = new JButton("Cansel");
            button2.addActionListener((ActionEvent e) -> {
                if (type.equals("Pending")) {
                    MYSQL.iud("UPDATE `invoice` SET `status`='Canseled' WHERE `code`='" + id + "' ");
                    resetstock(id);
                    JOptionPane.showMessageDialog(this, "Order canseled.", "Succcess", JOptionPane.INFORMATION_MESSAGE);
                    loadresults();
                    loadcounts();
                    jTextField1.setText("");
                } else if (type.equals("Canseled")) {
                    JOptionPane.showMessageDialog(this, "Order already canseled", "Warning", JOptionPane.WARNING_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "You can't cansel dilivered orders", "Warning", JOptionPane.WARNING_MESSAGE);
                }
            });



// Create a panel with a horizontal BoxLayout
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

// Create the three buttons and add them to the panel
            buttonPanel.add(button1);
            buttonPanel.add(Box.createHorizontalStrut(5));
            buttonPanel.add(button2);

// Wrap the button panel in a scroll pane
            JScrollPane scrollPane = new JScrollPane(buttonPanel);

            JPopupMenu popupMenu = new JPopupMenu();
            JMenuItem menuItem1 = new JMenuItem("Name : " + name);
            JMenuItem menuItem2 = new JMenuItem("Code : " + code);
            JMenuItem menuItem3 = new JMenuItem("Waybill : " + waybill);
            JMenuItem menuItem4 = new JMenuItem("District : " + district);
            JMenuItem menuItem5 = new JMenuItem("Item : " + item);

            popupMenu.add(menuItem1);
            popupMenu.add(menuItem2);
            popupMenu.add(menuItem3);
            popupMenu.add(menuItem4);
            popupMenu.add(menuItem5);
            popupMenu.add(scrollPane); // Add the scroll pane to the popup menu

            popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());

        }
    }//GEN-LAST:event_jTable1MouseClicked

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO add your handling code here:
        try {
            Stock_history stock_history = new Stock_history();
            stock_history.setVisible(true);
            this.dispose();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jMenuItem3ActionPerformed

    private void jMenuItem3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItem3MouseClicked
        // TODO add your handling code here:
        Stock_history stock_history = new Stock_history();
        stock_history.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jMenuItem3MouseClicked

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        try {
            Grn_history grn_history = new Grn_history();
            grn_history.setVisible(true);
            this.dispose();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItem2MouseClicked
        // TODO add your handling code here:
        Grn_history grn_history = new Grn_history();
        grn_history.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jMenuItem2MouseClicked

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        try {
            Pending_orders pending = new Pending_orders();
            pending.setVisible(true);
            this.dispose();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItem1MouseClicked
        // TODO add your handling code here:
        Pending_orders pending = new Pending_orders();
        pending.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jMenuItem1MouseClicked

    private void jMenu2MenuKeyPressed(javax.swing.event.MenuKeyEvent evt) {//GEN-FIRST:event_jMenu2MenuKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenu2MenuKeyPressed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        // TODO add your handling code here:
        try {
            Print_invoice pi = new Print_invoice();
            pi.setVisible(true);
            this.dispose();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        // TODO add your handling code here:
        try {
            Update_waybills uw = new Update_waybills();
            uw.setVisible(true);
            this.dispose();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenu1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenu1MouseClicked
        // TODO add your handling code here:
        try {
            Home home = new Home();
            home.setVisible(true);
            this.dispose();
        } catch (Exception e) {

        }
    }//GEN-LAST:event_jMenu1MouseClicked

    private void jMenu1MenuKeyPressed(javax.swing.event.MenuKeyEvent evt) {//GEN-FIRST:event_jMenu1MenuKeyPressed
        // TODO add your handling code here:
        System.out.println("ok");
    }//GEN-LAST:event_jMenu1MenuKeyPressed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Home.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Home().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.Box.Filler filler1;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
