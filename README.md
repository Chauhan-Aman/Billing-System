# Billing System
  
This is a simple Billing System application developed in Java. The application allows users to manage billing, and products using a graphical user interface (GUI). The project is built using Java Swing for the front-end and MySQL for data storage.

#### Key features include:

- Product Management: Manage the inventory of products.
- Billing: Create and print customer bills.
- Database Storage: Data persistence using MySQL.
- User Authentication: Login system for authorized access.

## Technologies Used
- Java: The core programming language.
- Java Swing: For building the graphical user interface (GUI).
- MySQL: Database for storing product information.
- Maven: Build automation tool.

## Demo
https://github.com/user-attachments/assets/4af2f93a-d5b1-4a27-89f6-bc3585f2ee40

## How to Run This Project

1. Clone the repository:
```
git clone https://github.com/Chauhan-Aman/Billing-System.git
cd Billing-System
```
2. Install Dependencies
```
mvn install
```
3. Database Configuration
   - Step 1: Set Up MySQL Database
      - Install MySQL on your machine.
      - Create a database called billing_system.
      - Import the database schema and tables. You can create tables as per the application requirements:
```
CREATE DATABASE billing_system;

USE billing_system;

CREATE TABLE products (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100),
  price DECIMAL(10, 2),
  img VARCHAR(255)
);
```
  - Step 2: Configure Database Connection
      - Create a file named db.properties in the root of the project and add your MySQL credentials:
```
db.url=jdbc:mysql://localhost:3306/billing_system
db.username=your_mysql_username
db.password=your_mysql_password
```
4. Build the Project
  - After configuring the database, you can build the project using Maven:
```
mvn clean package
```
This will generate a JAR file in the target directory.

## How to Run
1. Run the Application: After building the project, you can run it from the command line or your IDE (NetBeans, Eclipse, IntelliJ).

  - To run the application using the JAR file:
```
java -jar target/Billing-System-1.0-SNAPSHOT.jar
```

2. Run from Apache NetBeans:
  - Open the project in NetBeans.
  - Set up the project with Maven and MySQL connection as described above.
  - Run the project from NetBeans using Run > Run Project.

### Final Notes
This setup will allow you to run the Billing System locally. You can modify the database settings, connection parameters, and various other configuration options to make the system compatible with your environment. Make sure to adjust the db.properties file according to your specific setup, such as changing the database name, username, password, or port as needed.
