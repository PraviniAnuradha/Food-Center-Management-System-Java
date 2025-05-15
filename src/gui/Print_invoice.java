/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.io.File;
import java.io.FileOutputStream;
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
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import model.MYSQL;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperPrintManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRTableModelDataSource;
import net.sf.jasperreports.view.JasperViewer;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 *
 * @author CHAMA COMPUTERS
 */
public class Print_invoice extends javax.swing.JFrame {

    public void loadorders() {
        try {
            DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
            dtm.setRowCount(0);
            ResultSet rs = MYSQL.search("SELECT * FROM `invoice` INNER JOIN `customer` ON `invoice`.`customer_id`=`customer`.`id` INNER JOIN `address` ON `customer`.`id`=`address`.`customer_id` INNER JOIN `cities` ON `address`.`cities_id`=`cities`.`id` INNER JOIN `districts` ON `cities`.`district_id`=`districts`.`id` INNER JOIN `provinces` ON `districts`.`province_id`=`provinces`.`id`   INNER JOIN `order_through` ON `invoice`.`order_through_id`=`order_through`.`id` WHERE `invoice`.`status`='Ready to print' ");
            while (rs.next()) {
                Vector v = new Vector();
                v.add(rs.getString("invoice.code"));
                v.add(rs.getString("invoice.date_time"));
                v.add(rs.getString("customer.name"));
                v.add(rs.getString("cities.name_en"));
                v.add(rs.getString("districts.name_en"));
                v.add(rs.getString("customer.phone_no1"));
                v.add(rs.getString("order_through.name"));
                v.add(rs.getString("invoice.status"));

                dtm.addRow(v);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ordercount() {

        try {
            ResultSet pending_rs = MYSQL.search("SELECT COUNT(`id`) AS `pending_count` FROM `invoice` WHERE `status`='Ready to print'");
            pending_rs.next();
            jLabel3.setText(pending_rs.getString("pending_count"));
        } catch (Exception e) {
        }

    }

    public void print(String id) {
        try {
            ResultSet rs = MYSQL.search("SELECT * FROM `invoice` WHERE `invoice`.`status`='Ready to print' AND `code`='" + id + "' ");
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
                String district = customer_rs.getString("districts.name_en");
                String phone_no1 = customer_rs.getString("phone_no1");
                String phone_no2 = customer_rs.getString("phone_no2");
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
                JasperPrintManager.printReport(jp, false);

                //update status
                MYSQL.iud("UPDATE `invoice` SET `status`='Done' WHERE `id`='" + rs.getString("id") + "'");
            }
            Thread t = new Thread(() -> {
                for (int i = 0; i <= 100; i++) {
                    jProgressBar1.setValue(i);
                    try {
                        Thread.sleep(5);
                    } catch (Exception e) {
                    }
                }
                //clear fileds
                loadorders();
                ordercount();
                JOptionPane.showMessageDialog(this, "Orders Forward to print machine...", "Succcess", JOptionPane.INFORMATION_MESSAGE);
//                Addcustomers addcus = new Addcustomers();
//                addcus.setVisible(true);
//                this.dispose();
            });
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void print_selected(String id) {
        try {
            ResultSet rs = MYSQL.search("SELECT * FROM `invoice` WHERE `invoice`.`status`='Ready to print' AND `code`='" + id + "' ");
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
                String district = customer_rs.getString("districts.name_en");
                String phone_no1 = customer_rs.getString("phone_no1");
                String phone_no2 = customer_rs.getString("phone_no2");
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
                JasperPrintManager.printReport(jp, false);

                //update status
                MYSQL.iud("UPDATE `invoice` SET `status`='Done' WHERE `id`='" + rs.getString("id") + "'");
                Home.notification = "Orders are print by admin " + Login.admin_username + " to the system ";

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadorders() {

        try {
            MYSQL.getConnection();
            // Retrieve orders data
            ResultSet rs = MYSQL.search("SELECT * FROM `invoice` INNER JOIN `customer` ON `invoice`.`customer_id`=`customer`.`id` INNER JOIN `address` ON `customer`.`id`=`address`.`customer_id` INNER JOIN `districts` ON `address`.`districts_id`=`districts`.`id` INNER JOIN `invoice_payment` ON `invoice`.`id`=`invoice_payment`.`invoice_id` INNER JOIN `invoice_item` ON `invoice`.`id`=`invoice_item`.`invoice_id` INNER JOIN `stock` ON `stock`.`id`=`invoice_item`.`stock_id` INNER JOIN `product` ON `product`.`id`=`stock`.`product_id` WHERE `invoice`.`status`='Ready to print' ");

            // Create Excel workbook and worksheet objects
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Orders");

            // Create column headers row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Waybill Id", "Order Number", "Receiver Name", "Delivery Address", "District Name", "Receiver Phone", "COD", "Description", "Actual Value"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Populate orders data rows
            int rowNum = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rs.getString("uniq_id"));
                row.createCell(1).setCellValue(rs.getString("code"));
                row.createCell(2).setCellValue(rs.getString("customer.name"));
                row.createCell(3).setCellValue(rs.getString("address.content"));
                row.createCell(4).setCellValue(rs.getString("districts.name_en"));
                row.createCell(5).setCellValue(rs.getString("customer.phone_no1"));
                row.createCell(6).setCellValue(rs.getDouble("invoice_payment.payment"));
                row.createCell(7).setCellValue(rs.getString("product.name"));
                row.createCell(8).setCellValue(rs.getDouble("invoice_payment.payment"));

            }

            // Save workbook to file
            JFileChooser fileChooser = new JFileChooser();
            // Get the current date and time
            LocalDateTime currentDateTime = LocalDateTime.now();

            // Create a formatter to format the date and time as a string
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            // Format the date and time as a string using the formatter
            String formattedDateTime = currentDateTime.format(formatter);
            fileChooser.setSelectedFile(new File(formattedDateTime + ".xlsx"));

            // Show the save dialog and get the result
            int result = fileChooser.showSaveDialog(null);

            // If the user clicked the "Save" button
            if (result == JFileChooser.APPROVE_OPTION) {
                // Get the selected file
                File selectedFile = fileChooser.getSelectedFile();
                String filePath = selectedFile.getAbsolutePath();

                try {
                    FileOutputStream outputStream = new FileOutputStream(selectedFile);
                    workbook.write(outputStream);
                    workbook.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void processSelectedRows() throws SQLException, ClassNotFoundException {
        MYSQL.getConnection();
        DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
        int numRows = dtm.getRowCount();
        if (jTable1 == null) {
            System.err.println("jTable1 is null");
            return;
        }
        for (int i = 0; i < numRows; i++) {
            Boolean isSelected = (Boolean) dtm.getValueAt(i, dtm.getColumnCount() - 1);
            if (isSelected != null && isSelected) {
                String invoiceCode = (String) dtm.getValueAt(i, 0);
                try {
                    print_selected(invoiceCode);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Print_invoice() {
        try {
            MYSQL.getConnection();
        } catch (Exception ex) {
        }
        initComponents();
        loadorders();
        ordercount();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel6 = new javax.swing.JPanel();
        jButton8 = new javax.swing.JButton();
        jLabel11 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jProgressBar1 = new javax.swing.JProgressBar();
        jButton3 = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jMenu3 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setResizable(false);

        jPanel6.setBackground(new java.awt.Color(255, 255, 255));
        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Print invoice here", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Comic Sans MS", 0, 11))); // NOI18N

        jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/exit.png"))); // NOI18N
        jButton8.setBorder(null);
        jButton8.setBorderPainted(false);
        jButton8.setContentAreaFilled(false);
        jButton8.setFocusPainted(false);
        jButton8.setFocusable(false);
        jButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton8ActionPerformed(evt);
            }
        });

        jLabel11.setFont(new java.awt.Font("Comic Sans MS", 0, 20)); // NOI18N
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Print invoice");

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(261, 261, 261)
                .addComponent(jButton8)
                .addContainerGap())
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(jLabel11)
                .addGap(0, 12, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jButton8))
        );

        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Order no", "Order date", "Customer name", "City", "District", "Phone", "Order_through", "Status", "Action"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTable1MouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 0, 880, 240));

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/edit.png"))); // NOI18N
        jLabel2.setText("Order count :");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 290, -1, 22));

        jLabel3.setText("None");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 290, 77, 22));

        jButton1.setText("Print Orders ");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(620, 270, 257, 50));

        jProgressBar1.setStringPainted(true);
        jPanel1.add(jProgressBar1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 350, 898, 14));

        jButton3.setText("Print Orders (Selected)");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton3, new org.netbeans.lib.awtextra.AbsoluteConstraints(340, 270, 257, 50));

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
        System.exit(0);
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
        downloadorders();
        try {
            MYSQL.getConnection();
            ResultSet rs = MYSQL.search("SELECT * FROM `invoice` WHERE `invoice`.`status`='Ready to print' ");
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
                String district = customer_rs.getString("districts.name_en");
                String phone_no1 = customer_rs.getString("phone_no1");
                String phone_no2 = customer_rs.getString("phone_no2");
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
//                String filepath = "src//reports//invo.jasper";
//                JasperReport jr = JasperCompileManager.compileReport(filepath);
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
//                            JasperViewer.viewReport(jp, false);
                JasperPrintManager.printReport(jp, false);

                //update status
                MYSQL.iud("UPDATE `invoice` SET `status`='Done' WHERE `id`='" + rs.getString("id") + "'");
            }
            Thread t = new Thread(() -> {
                for (int i = 0; i <= 100; i++) {
                    jProgressBar1.setValue(i);
                    try {
                        Thread.sleep(5);
                    } catch (Exception e) {
                    }
                }
                //clear fileds
                loadorders();
                ordercount();
                JOptionPane.showMessageDialog(this, "Orders Forward to print machine...", "Succcess", JOptionPane.INFORMATION_MESSAGE);
//                Addcustomers addcus = new Addcustomers();
//                addcus.setVisible(true);
//                this.dispose();
            });
            t.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Home.notification = "Orders are print by admin " + Login.admin_username + " to the system ";

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTable1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTable1MouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() == 2) {
            int r = jTable1.getSelectedRow();

            if (r == -1) {
                JOptionPane.showMessageDialog(this, "Please select a order", "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                String order_number = jTable1.getValueAt(r, 0).toString();

                DefaultTableModel dtm = (DefaultTableModel) jTable1.getModel();
                int option = JOptionPane.showConfirmDialog(this, "Do you want to print this order only", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
                if (option == JOptionPane.YES_OPTION) {
                    print(order_number);
                }

            }
        }
    }//GEN-LAST:event_jTable1MouseClicked

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        try {
            // TODO add your handling code here:
            processSelectedRows();
            JOptionPane.showMessageDialog(this, "Orders Forward to print machine...", "Succcess", JOptionPane.INFORMATION_MESSAGE);
            loadorders();
            ordercount();
        } catch (Exception ex) {
            Logger.getLogger(Print_invoice.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jMenu1MenuKeyPressed(javax.swing.event.MenuKeyEvent evt) {//GEN-FIRST:event_jMenu1MenuKeyPressed
        // TODO add your handling code here:
        System.out.println("ok");
    }//GEN-LAST:event_jMenu1MenuKeyPressed

    private void jMenu1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenu1MouseClicked
        // TODO add your handling code here:
        try {
            Home home = new Home();
            home.setVisible(true);
            this.dispose();
        } catch (Exception e) {

        }
    }//GEN-LAST:event_jMenu1MouseClicked

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        // TODO add your handling code here:
        try {
            Update_waybills uw = new Update_waybills();
            uw.setVisible(true);
            this.dispose();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        // TODO add your handling code here:
        try {
            Print_invoice pi = new Print_invoice();
            pi.setVisible(true);
            this.dispose();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenu2MenuKeyPressed(javax.swing.event.MenuKeyEvent evt) {//GEN-FIRST:event_jMenu2MenuKeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_jMenu2MenuKeyPressed

    private void jMenuItem1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItem1MouseClicked
        // TODO add your handling code here:
        Pending_orders pending = new Pending_orders();
        pending.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jMenuItem1MouseClicked

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        // TODO add your handling code here:
        try {
            Pending_orders pending = new Pending_orders();
            pending.setVisible(true);
            this.dispose();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItem2MouseClicked
        // TODO add your handling code here:
        Grn_history grn_history = new Grn_history();
        grn_history.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jMenuItem2MouseClicked

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        // TODO add your handling code here:
        try {
            Grn_history grn_history = new Grn_history();
            grn_history.setVisible(true);
            this.dispose();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jMenuItem3MouseClicked
        // TODO add your handling code here:
        Stock_history stock_history = new Stock_history();
        stock_history.setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jMenuItem3MouseClicked

    private void jMenuItem3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem3ActionPerformed
        // TODO add your handling code here:
        try {
            Stock_history stock_history = new Stock_history();
            stock_history.setVisible(true);
            this.dispose();
        } catch (Exception e) {
        }
    }//GEN-LAST:event_jMenuItem3ActionPerformed

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
            java.util.logging.Logger.getLogger(Print_invoice.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Print_invoice.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Print_invoice.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Print_invoice.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Print_invoice().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton8;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
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
    private javax.swing.JPanel jPanel6;
    private javax.swing.JProgressBar jProgressBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
