package com.mycompany.billing.system;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.Properties;

public class ProductManagementPage extends javax.swing.JFrame {

    private JTextField searchTextField;
    private JTextField nameTextField;
    private JTextField priceTextField;
    private JButton searchButton, updateButton, deleteButton;
    private String dbPassword;
    private String dburl;
    private String dbuser;

    public ProductManagementPage(String dbPassword) {
        initComponents();
        this.dbPassword = dbPassword;
        this.dburl = "jdbc:mysql://localhost:3306/groceryshop";
        this.dbuser = "root";

        searchTextField = new JTextField(20);
        nameTextField = new JTextField(20);
        priceTextField = new JTextField(20);
        searchButton = new JButton("Search Product");
        updateButton = new JButton("Update Product");
        deleteButton = new JButton("Delete Product");

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Add components to the frame with organized layout
        // Row 1 - Search Label and Search TextField
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(new JLabel("Search Product by Name:"), gbc);

        gbc.gridx = 1;
        add(searchTextField, gbc);

        gbc.gridx = 2;
        add(searchButton, gbc);

        // Row 2 - Product Name Label and Name TextField
        gbc.gridx = 0;
        gbc.gridy = 1;
        add(new JLabel("Product Name:"), gbc);

        gbc.gridx = 1;
        add(nameTextField, gbc);

        // Row 3 - Price Label and Price TextField
        gbc.gridx = 0;
        gbc.gridy = 2;
        add(new JLabel("Price:"), gbc);

        gbc.gridx = 1;
        add(priceTextField, gbc);

        // Row 4 - Update and Delete Buttons
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 4; 
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, gbc);
        
        searchTextField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchButton.doClick();
            }
        });

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchProduct(searchTextField.getText());
            }
        });

        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateProduct(searchTextField.getText(), nameTextField.getText(), Double.parseDouble(priceTextField.getText()));
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteProduct(searchTextField.getText());
            }
        });

        setTitle("Product Management");
        pack();
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);
    }
    
    // Search product in database
    private void searchProduct(String productName) {
        String query = "SELECT * FROM groceryitems WHERE item_name = ?";
        try (Connection conn = DriverManager.getConnection(dburl, dbuser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, productName);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                nameTextField.setText(rs.getString("item_name"));
                priceTextField.setText(String.valueOf(rs.getDouble("price")));
            } else {
                JOptionPane.showMessageDialog(this, "Product not found.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error Establishing Connection with Database");
//            ex.printStackTrace();
        }
    }

    // Update product in database
    private void updateProduct(String oldProductName, String newProductName, Double newPrice) {
        String query = "UPDATE groceryitems SET item_name = ?, price = ? WHERE item_name = ?";
        try (Connection conn = DriverManager.getConnection(dburl, dbuser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, newProductName);
            pstmt.setDouble(2, newPrice);
            pstmt.setString(3, oldProductName);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Product updated successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Product update failed.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error Establishing Connection with Database");
//            ex.printStackTrace();
        }
    }

    // Delete product from database
    private void deleteProduct(String productName) {
        String query = "DELETE FROM groceryitems WHERE item_name = ?";
        try (Connection conn = DriverManager.getConnection(dburl, dbuser, dbPassword);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, productName);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully.");
            } else {
                JOptionPane.showMessageDialog(this, "Product deletion failed.");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error Establishing Connection with Database");
//            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
            java.util.logging.Logger.getLogger(ProductManagementPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ProductManagementPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ProductManagementPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ProductManagementPage.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        Properties props = DatabaseConfig.loadProperties();
        String Password = props.getProperty("db.password");
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ProductManagementPage(Password).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
