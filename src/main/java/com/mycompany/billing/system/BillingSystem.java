/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.billing.system;
import java.util.Properties;
import javax.swing.JOptionPane;

/**
 *
 * @author ab665
 */
public class BillingSystem {

    public static void main(String[] args) {
        AuthDialog authDialog = new AuthDialog(null);
        authDialog.setVisible(true);
        
        String dbPassword = authDialog.getDbPassword();
        
        Properties props = DatabaseConfig.loadProperties();
        String Password = props.getProperty("db.password");
        
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
}
