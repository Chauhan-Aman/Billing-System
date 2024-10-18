package com.mycompany.billing.system;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {

    public static Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find db.properties");
                return null;
            }
            // Load the properties file
            props.load(input);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return props;
    }
}
