package com.mycompany.billing.system;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import java.util.ArrayList;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.border.EmptyBorder;
import java.util.Properties;

public class BillingInterface extends javax.swing.JFrame {
    
    class Product {
        private String itemName;
        private double price;
        private String imgPath;

        public Product(String itemName, double price, String imgPath) {
            this.itemName = itemName;
            this.price = price;
            this.imgPath = imgPath;
        }

        public String getItemName() {
            return itemName;
        }

        public double getPrice() {
            return price;
        }

        public String getImgPath() {
            return imgPath;
        }
    }
    
    public void addProductToPanel(String Name, Double Price,String imgpath){
        JButton triggerButton = new JButton("");
        ImageIcon icon = new ImageIcon(imgpath);
        
        Image image = icon.getImage();
        Image newImage = image.getScaledInstance(110, 180, java.awt.Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(newImage);
        triggerButton.setIcon(resizedIcon);
        
        ProductPanel.add(triggerButton);
        ProductPanel.revalidate();
        ProductPanel.repaint();
        
        triggerButton.addActionListener((ActionEvent e) -> {
            addProduct(Name, Price);
        });
        
    }
    
    private void fetchProductsAndDisplay() {
        String url = dburl;
        String user = dbuser;
        String password = dbPassword;

        String query = "SELECT item_name, price, img FROM groceryitems";

        ArrayList<Product> products = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(url, user, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                String itemName = rs.getString("item_name");
                Double price = rs.getDouble("price");
                String imgPath = rs.getString("img");

                products.add(new Product(itemName, price, imgPath));
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error Establishing Connection with Database");
            e.printStackTrace();
        }

        for (Product product : products) {
            addProductToPanel(product.getItemName(), product.getPrice(), product.getImgPath());
        }
    }
    
    public void clearProductPanel() {
        ProductPanel.removeAll();
        ProductPanel.revalidate();
        ProductPanel.repaint();
    }
    
    private String dbPassword;
    private String dburl;
    private String dbuser;
    public BillingInterface(String dbPassword) {
        initComponents();
        Properties props = DatabaseConfig.loadProperties();
        this.dbPassword = dbPassword;
        this.dburl = props.getProperty("db.url");
        this.dbuser = props.getProperty("db.username");
        
        fetchProductsAndDisplay();
        
        setLayout(new BorderLayout());

        ProductPanel.setLayout(new GridLayout(0, 4, 10, 10)); // 4 columns, auto-wrap, 10px gaps

        jTable1.setModel(new DefaultTableModel(
            new Object [][] {},
            new String [] {"Product Name", "Quantity", "Total Price", "Edit"}
        ));

        jTable1.getColumn("Edit").setCellRenderer(new ButtonRenderer("Edit"));
        jTable1.getColumn("Edit").setCellEditor(new ButtonEditor(new JCheckBox(), "Edit"));

        jTable1.getColumnModel().getColumn(0).setPreferredWidth(100); // Product Name column
        jTable1.getColumnModel().getColumn(1).setPreferredWidth(10); // Quantity column
        jTable1.getColumnModel().getColumn(2).setPreferredWidth(15); // Total Price column
        jTable1.getColumnModel().getColumn(3).setPreferredWidth(10);  // Edit button column

    }
    
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer(String buttonText) {
            setText(buttonText);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;

        public ButtonEditor(JCheckBox checkBox, String action) {
            super(checkBox);
            button = new JButton();
            button.setText(action);
            button.addActionListener(e -> {
                fireEditingStopped();
                int row = jTable1.getSelectedRow();
                if (row != -1) {
                    String productName = (String) jTable1.getValueAt(row, 0);
                    String quantityStr = JOptionPane.showInputDialog("Enter new quantity for " + productName + ":");
                    if (quantityStr != null && !quantityStr.trim().isEmpty()) {
                        try {
                            int newQuantity = Integer.parseInt(quantityStr);
                             if (newQuantity < 0) {
                            JOptionPane.showMessageDialog(BillingInterface.this, "Quantity cannot be negative.");
                        } else if (newQuantity == 0) {
                            ((DefaultTableModel) jTable1.getModel()).removeRow(row);
                        } else {
                            double pricePerUnit = (double) jTable1.getValueAt(row, 2) / (Integer) jTable1.getValueAt(row, 1);
                            jTable1.setValueAt(newQuantity, row, 1);
                            jTable1.setValueAt(pricePerUnit * newQuantity, row, 2);
                            cart_total();
                        }
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(BillingInterface.this, "Please enter a valid quantity.");
                        }
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            label = (value == null) ? "Edit" : value.toString();
            button.setText(label);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                clicked = false;
                return label;
            }
            clicked = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }
    
    public void cart_total(){
        int numofrow = jTable1.getRowCount();
        double total = 0;
        double Totaltax = 0;
        double Discount = 0;
        String disRateText = disrate.getText();
        String numericPart = disRateText.replaceAll("[^0-9]", ""); 

        double discountRate = Double.parseDouble(numericPart); 

        
        for(int i = 0; i < numofrow; i++){
            double value = Double.valueOf(jTable1.getValueAt(i,2).toString());
            total += value;
            Totaltax += (18.0/100.0)*value;
            Discount += (discountRate/100.0)*value;
        }
        
        DecimalFormat df = new DecimalFormat("000.00"); 
        String d1 = df.format(total);
        
        System.out.println("Total: "+d1);
        
        bill_tot.setText(d1);
        
//        double tx = Double.valueOf(tax.getText());
//        double ds = Double.valueOf(dis.getText());
        
        double allTot = total + Totaltax - Discount;
        
        DecimalFormat dff = new DecimalFormat("000.00"); 
        String d2 = dff.format(allTot);
        
        DecimalFormat dfff = new DecimalFormat("00.00"); 
        
        dis.setText(dfff.format(Discount));
        tax.setText(dff.format(Totaltax));
        
        if(allTot < 0){
            full_tot.setText("0");
        }else{
         full_tot.setText(d2);   
        }
    }
    
    public void addProduct(String Name, Double Price){
        for (int i = 0; i < jTable1.getRowCount(); i++) {
            if (jTable1.getValueAt(i, 0).equals(Name)) {
                JOptionPane.showMessageDialog(this, "Product already added!");
                return;
            }
        }
        
        String Qty = JOptionPane.showInputDialog(null,"Please Enter your Qty: ","1");
        System.out.println(Qty);
        
        Integer tqty = Integer.valueOf(Qty);
        Double Tot_prc = Price * tqty;
        System.out.println(Tot_prc);
        
        // Add product to cart
        DefaultTableModel dt = (DefaultTableModel) jTable1.getModel();
        
        Vector v = new Vector();
        v.add(Name);
        v.add(tqty);
        v.add(Tot_prc);
        
        dt.addRow(v);
        
        cart_total();
    }
    
    public void drwobill(){
        
            bill.setText("                  The xyz Super Market \n");
            bill.setText(bill.getText() + "\txyx Street, Delhi, India\n");
            bill.setText(bill.getText() + "\t  +91XXXXXXXXXX \n\n");
            
            bill.setText(bill.getText()+"\t      Retail Invoice \n\n");
            
            // Date time
            Date dd = new Date();
            
            SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timef = new SimpleDateFormat("HH:mm");
            
            String date = datef.format(dd);
            String time = timef.format(dd);
            
            bill.setText(bill.getText() + "Date : "+date+"\t\tTime : "+time+"\n");
            
            bill.setText(bill.getText() + "----------------------------------------------------\n");
            bill.setText(bill.getText() + " Item \t\tQty \tPrice \n");
            bill.setText(bill.getText() + "----------------------------------------------------\n");
            
            DefaultTableModel df = (DefaultTableModel) jTable1.getModel();
            for (int i = 0; i < jTable1.getRowCount(); i++) {
                
                String name = df.getValueAt(i, 0).toString();
                String qt = df.getValueAt(i, 1).toString();
                String prc = df.getValueAt(i, 2).toString();

                int qtp = Integer.parseInt(qt);
                double prcp = Double.parseDouble(prc);

                if (name.length() > 10) {
                       String formattedLine = String.format("%-15s\t%d\t%.2f\n", name, qtp, prcp);
                       bill.setText(bill.getText() + formattedLine);
                   } else {
                       String formattedLine = String.format("%-15s\t%d\t%.2f\n", name, qtp, prcp);
                       bill.setText(bill.getText() + formattedLine);
               }
                
            }
            bill.setText(bill.getText() + "----------------------------------------------------\n");
            bill.setText(bill.getText() + "SubTotal :\t\t\t"+bill_tot.getText()+"\n");
            bill.setText(bill.getText() + "Tax :\t\t\t"+tax.getText()+"\n");
            bill.setText(bill.getText() + "Discount :\t\t\t"+dis.getText()+"\n");
            bill.setText(bill.getText() + "==============================\n");
            bill.setText(bill.getText() + "Grand Total :\t\t"+full_tot.getText()+"\n");
            bill.setText(bill.getText() + "Paid :\t\t\t"+pay.getText()+"\n");
            bill.setText(bill.getText() + "Balance :\t\t\t"+balnce.getText()+"\n");
            
            bill.setText(bill.getText() + "*********************************************\n");
            bill.setText(bill.getText() +"                  Thanks For Visiting...!"+"\n");
            bill.setText(bill.getText() + "*********************************************\n");
            bill.setText(bill.getText() +"              Software by Aman Chauhan"+"\n");
            
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        newbillButton = new javax.swing.JButton();
        searchTextField = new javax.swing.JTextField();
        search = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        ProductPanel = new javax.swing.JPanel();
        AddNewProduct = new javax.swing.JButton();
        reset = new javax.swing.JButton();
        dbmanagement = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jPanel5 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        dis = new javax.swing.JTextField();
        paybutton = new javax.swing.JButton();
        jLabel7 = new javax.swing.JLabel();
        printButton = new javax.swing.JButton();
        balnce = new javax.swing.JLabel();
        tax = new javax.swing.JLabel();
        disrate = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        pay = new javax.swing.JTextField();
        bill_tot = new javax.swing.JLabel();
        full_tot = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        bill = new javax.swing.JTextPane();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(153, 153, 153));

        jLabel11.setIcon(new javax.swing.ImageIcon("C:\\Users\\ab665\\OneDrive\\Desktop\\Aman\\Java\\Billing--System\\src\\main\\resources\\img\\banner1.jpg")); // NOI18N

        newbillButton.setText("New Bill");
        newbillButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newbillButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGap(39, 39, 39)
                .addComponent(jLabel11)
                .addGap(18, 18, 18)
                .addComponent(newbillButton, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(14, 14, 14)
                        .addComponent(newbillButton, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(17, 17, 17))
        );

        searchTextField.setText("Enter Product to be Searched ...");
        searchTextField.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                searchTextFieldMouseClicked(evt);
            }
        });
        searchTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchTextFieldActionPerformed(evt);
            }
        });

        search.setText("Search");
        search.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchActionPerformed(evt);
            }
        });

        jScrollPane3.setPreferredSize(new java.awt.Dimension(100, 500));

        javax.swing.GroupLayout ProductPanelLayout = new javax.swing.GroupLayout(ProductPanel);
        ProductPanel.setLayout(ProductPanelLayout);
        ProductPanelLayout.setHorizontalGroup(
            ProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 660, Short.MAX_VALUE)
        );
        ProductPanelLayout.setVerticalGroup(
            ProductPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 564, Short.MAX_VALUE)
        );

        jScrollPane3.setViewportView(ProductPanel);

        AddNewProduct.setText("Add New Product");
        AddNewProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AddNewProductActionPerformed(evt);
            }
        });

        reset.setText("Reset");
        reset.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetActionPerformed(evt);
            }
        });

        dbmanagement.setText("Management");
        dbmanagement.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbmanagementActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(searchTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(search)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(reset, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(AddNewProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbmanagement)
                                .addGap(0, 20, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(searchTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 43, Short.MAX_VALUE)
                    .addComponent(search, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(reset, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(AddNewProduct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(dbmanagement, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 566, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jTable1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Item", "Quantity", "Price", ""
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTable1.setRowHeight(30);
        jScrollPane1.setViewportView(jTable1);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel5.setText("Tax $ :");

        jLabel6.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        jLabel6.setText("Dis $ :");

        dis.setText("0");
        dis.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disActionPerformed(evt);
            }
        });
        dis.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                disKeyReleased(evt);
            }
        });

        paybutton.setText("Pay");
        paybutton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paybuttonActionPerformed(evt);
            }
        });

        jLabel7.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel7.setText("Balance :");

        printButton.setText("Print");
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });

        balnce.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        balnce.setText("0");

        tax.setBackground(new java.awt.Color(153, 153, 153));
        tax.setFont(new java.awt.Font("Segoe UI", 0, 15)); // NOI18N
        tax.setText("18 % GST");
        tax.setAlignmentX(0.5F);
        tax.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(204, 204, 204), 1, true));

        disrate.setText("6 %");
        disrate.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                disrateMouseClicked(evt);
            }
        });

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(paybutton, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(printButton, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(balnce, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(tax, javax.swing.GroupLayout.PREFERRED_SIZE, 77, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dis, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(disrate, javax.swing.GroupLayout.PREFERRED_SIZE, 57, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(tax, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dis, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(disrate, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addComponent(paybutton, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(balnce, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(printButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())))
        );

        jLabel2.setFont(new java.awt.Font("Segoe UI", 0, 26)); // NOI18N
        jLabel2.setText("Sub Total :");

        jLabel3.setFont(new java.awt.Font("Segoe UI", 0, 28)); // NOI18N
        jLabel3.setText("Total :");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 0, 28)); // NOI18N
        jLabel4.setText("Paid :");

        pay.setText("0");
        pay.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                payMouseClicked(evt);
            }
        });
        pay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                payActionPerformed(evt);
            }
        });

        bill_tot.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        bill_tot.setText("0");

        full_tot.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        full_tot.setText("0");

        bill.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jScrollPane2.setViewportView(bill);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel3Layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 363, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 315, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 151, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(bill_tot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jLabel4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(pay, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel3Layout.createSequentialGroup()
                                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 105, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(full_tot, javax.swing.GroupLayout.PREFERRED_SIZE, 107, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 581, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(bill_tot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(full_tot, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pay, javax.swing.GroupLayout.PREFERRED_SIZE, 47, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(28, 28, 28))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void payActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_payActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_payActionPerformed

    private void disActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_disActionPerformed

    private void paybuttonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_paybuttonActionPerformed
        // pay bill
        double tot = Double.valueOf(full_tot.getText());
        double py = Double.valueOf(pay.getText());
        
        double bal = py - tot;
        
        DecimalFormat dff = new DecimalFormat("00.00"); 
        String d2 = dff.format(bal);
        
        balnce.setText(d2);
        
        drwobill();
    }//GEN-LAST:event_paybuttonActionPerformed

    private void newbillButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newbillButtonActionPerformed
        DefaultTableModel dt = (DefaultTableModel) jTable1.getModel();
        dt.setRowCount(0);
        
        bill.setText("");
        full_tot.setText("0");
        bill_tot.setText("0");
        pay.setText("0");
        balnce.setText("0");
        tax.setText("18% GST");
        dis.setText("0");
    }//GEN-LAST:event_newbillButtonActionPerformed

    private void searchTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchTextFieldActionPerformed
        search.doClick(); 
    }//GEN-LAST:event_searchTextFieldActionPerformed

    private void disKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_disKeyReleased
        cart_total();
    }//GEN-LAST:event_disKeyReleased

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
       try{
           bill.print();
       }catch(Exception e){
           JOptionPane.showMessageDialog(null, "Some Error Occurred!");
       }
    }//GEN-LAST:event_printButtonActionPerformed

    private void searchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchActionPerformed
        String searchQuery = searchTextField.getText();

        if (searchQuery.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please enter a product name to search.");
            return;
        }
        searchProductAndDisplay(searchQuery);
    }//GEN-LAST:event_searchActionPerformed

    private void searchProductAndDisplay(String productName) {
        String url = dburl;
        String user = dbuser;
        String password = dbPassword;

        String query = "SELECT item_name, price, img FROM groceryitems WHERE item_name LIKE ? LIMIT 1";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, "%" + productName + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String itemName = rs.getString("item_name");
                    Double price = rs.getDouble("price");
                    String imgPath = rs.getString("img");

                    clearProductPanel();
                    addProductToPanel(itemName, price, imgPath);
                } else {
                    JOptionPane.showMessageDialog(null, "No product found with that name.");
                }
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error Establishing Connection with Database");
//            e.printStackTrace();
        }
}
    
    private void resetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetActionPerformed
        clearProductPanel();
        fetchProductsAndDisplay();
    }//GEN-LAST:event_resetActionPerformed

    private void AddNewProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AddNewProductActionPerformed
        JFrame frame = new JFrame("Add New Product");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel nameLabel = new JLabel("Item Name:");
        JTextField nameField = new JTextField(20);

        JLabel priceLabel = new JLabel("Price:");
        JTextField priceField = new JTextField(10);

        JLabel imageLabel = new JLabel("Select Image:");
        JButton imageButton = new JButton("Browse");

        final String[] imagePath = {null};

        imageButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File("C:\\Users\\ab665\\OneDrive\\Desktop\\Aman\\Java\\Billing--System\\src\\main\\resources\\img\\"));
                fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "png", "jpeg"));
                int result = fileChooser.showOpenDialog(null);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    imagePath[0] = selectedFile.getAbsolutePath();
                }
            }
        });

        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(priceLabel);
        formPanel.add(priceField);
        formPanel.add(imageLabel);
        formPanel.add(imageButton);

        JButton submitButton = new JButton("Submit");
        submitButton.setPreferredSize(new Dimension(150, 50));

        submitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String itemName = nameField.getText();
                Double price = Double.parseDouble(priceField.getText());
                String imgPath = imagePath[0];

                if (itemName.isEmpty() || price <= 0 || imgPath == null) {
                    JOptionPane.showMessageDialog(null, "Please fill all the fields and select an image.");
                    return;
                }

                try {
                    saveProductToDatabase(itemName, price, imgPath);
                    JOptionPane.showMessageDialog(null, "Product added successfully!");
                    frame.dispose();
                    clearProductPanel();
                    fetchProductsAndDisplay();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Failed to add product.");
                }
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        buttonPanel.add(submitButton);

        frame.setLayout(new BorderLayout());
        frame.add(formPanel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }//GEN-LAST:event_AddNewProductActionPerformed

    private void searchTextFieldMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_searchTextFieldMouseClicked
        searchTextField.setText("");
    }//GEN-LAST:event_searchTextFieldMouseClicked

    private void payMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_payMouseClicked
        pay.setText("");
    }//GEN-LAST:event_payMouseClicked

    private void disrateMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_disrateMouseClicked
        disrate.setText("");
    }//GEN-LAST:event_disrateMouseClicked

    private void dbmanagementActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbmanagementActionPerformed
        AuthDialog authDialog = new AuthDialog(null);
        authDialog.setVisible(true);

        String dbPassword = authDialog.getDbPassword();

        if (dbPassword != null && !dbPassword.isEmpty()){
            if (dbPassword.equals("mysql!@#123")){
                ProductManagementPage productManagementPage = new ProductManagementPage(dbPassword);
                productManagementPage.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                        clearProductPanel();
                        fetchProductsAndDisplay();
                    }
                });
                productManagementPage.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "Wrong Password!");
            }
        } else {
            System.out.println("Authentication failed or canceled.");
        }
    }//GEN-LAST:event_dbmanagementActionPerformed

    
    private void saveProductToDatabase(String itemName, Double price, String imagePath) throws Exception {
        String url = dburl;
        String user = dbuser;
        String password = dbPassword;

        String query = "INSERT INTO groceryitems (item_name, price, img) VALUES (?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, itemName);
            pstmt.setDouble(2, price);
            pstmt.setString(3, imagePath);

            pstmt.executeUpdate();
        }
    }
    
    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(BillingInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BillingInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BillingInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BillingInterface.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        Properties props = DatabaseConfig.loadProperties();
        String Password = props.getProperty("db.password");
        
        AuthDialog authDialog = new AuthDialog(null);
        authDialog.setVisible(true);
        
        String dbPassword = authDialog.getDbPassword();
        
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                if (dbPassword != null && !dbPassword.isEmpty() && dbPassword.equals(Password)) {
                    new BillingInterface(dbPassword).setVisible(true);
                }else{
                    JOptionPane.showMessageDialog(null, "Wrong Password!");
                }
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddNewProduct;
    private javax.swing.JPanel ProductPanel;
    private javax.swing.JLabel balnce;
    private javax.swing.JTextPane bill;
    private javax.swing.JLabel bill_tot;
    private javax.swing.JButton dbmanagement;
    private javax.swing.JTextField dis;
    private javax.swing.JTextField disrate;
    private javax.swing.JLabel full_tot;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTable jTable1;
    private javax.swing.JButton newbillButton;
    private javax.swing.JTextField pay;
    private javax.swing.JButton paybutton;
    private javax.swing.JButton printButton;
    private javax.swing.JButton reset;
    private javax.swing.JButton search;
    private javax.swing.JTextField searchTextField;
    private javax.swing.JLabel tax;
    // End of variables declaration//GEN-END:variables
}
