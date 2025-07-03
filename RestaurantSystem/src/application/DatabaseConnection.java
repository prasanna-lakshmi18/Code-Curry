package application;
import java.sql.*;
public class DatabaseConnection {
 private static final String URL = "jdbc:oracle:thin:@localhost:1521:XE";
 private static final String USERNAME = "system";
 private static final String PASSWORD = "SYS";
 public static Connection getConnection() throws SQLException {
     try {
         Class.forName("oracle.jdbc.driver.OracleDriver");
         return DriverManager.getConnection(URL, USERNAME, PASSWORD);
     } catch (ClassNotFoundException e) {
         throw new SQLException("Oracle JDBC Driver not found", e);
     }
 }
 
 public static void initializeDatabase() {
	 
     try (Connection conn = getConnection()) {
         createTables(conn);
         insertInitialData(conn);
     } catch (SQLException e) {
         e.printStackTrace();
     }
 }
 private static void createTables(Connection conn) throws SQLException {
     String[] createTableQueries = {
         """
         CREATE TABLE  customers (
             id NUMBER PRIMARY KEY,
             name VARCHAR2(100) NOT NULL,
             table_no NUMBER,
             booking_time TIMESTAMP
         )
         """,
         """
         CREATE TABLE  menu_items (
             id NUMBER PRIMARY KEY,
             name VARCHAR2(100) NOT NULL,
             price NUMBER(10,2) NOT NULL
         )
         """,
         """
         CREATE TABLE  tables (
             table_number NUMBER PRIMARY KEY,
             capacity NUMBER NOT NULL
         )
         """,
         """
         CREATE TABLE  orders (
             order_id NUMBER PRIMARY KEY,
             customer_id NUMBER,
             order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
             is_paid NUMBER(1) DEFAULT 0,
             total_amount NUMBER(10,2),
             FOREIGN KEY (customer_id) REFERENCES customers(id)
         )
         """,
         """
         CREATE TABLE order_items (
             id NUMBER PRIMARY KEY,
             order_id NUMBER,
             menu_item_id NUMBER,
             quantity NUMBER DEFAULT 1,
             FOREIGN KEY (order_id) REFERENCES orders(order_id),
             FOREIGN KEY (menu_item_id) REFERENCES menu_items(id)
         )
         """,
         """
         CREATE TABLE  feedback (
             id NUMBER PRIMARY KEY,
             customer_id NUMBER,
             comment VARCHAR2(500),
             rating NUMBER(1),
             feedback_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
             FOREIGN KEY (customer_id) REFERENCES customers(id)
         )
         """,
         """
         CREATE TABLE  table_bookings (
             id NUMBER PRIMARY KEY,
             table_number NUMBER,
             customer_id NUMBER,
             booking_time TIMESTAMP,
             party_size NUMBER,
             FOREIGN KEY (table_number) REFERENCES tables(table_number),
             FOREIGN KEY (customer_id) REFERENCES customers(id)
         )
         """
     };
     
     for (String query : createTableQueries) {
         try (PreparedStatement stmt = conn.prepareStatement(query)) {
             stmt.execute();
         }
     }
     
     // Create sequences
     String[] sequences = {
         "CREATE SEQUENCE customer_seq START WITH 1 INCREMENT BY 1",
         "CREATE SEQUENCE order_seq START WITH 1 INCREMENT BY 1",
         "CREATE SEQUENCE order_item_seq START WITH 1 INCREMENT BY 1",
         "CREATE SEQUENCE feedback_seq START WITH 1 INCREMENT BY 1",
         "CREATE SEQUENCE booking_seq START WITH 1 INCREMENT BY 1"
     };
     
     for (String seq : sequences) {
         try (PreparedStatement stmt = conn.prepareStatement(seq)) {
             stmt.execute();
         } catch (SQLException e) {
             // Sequence might already exist
         }
     }
     
    

  // Create admin_users table
  String createAdminUsersTable = """
      CREATE TABLE IF NOT EXISTS admin_users (
          id NUMBER PRIMARY KEY,
          username VARCHAR2(50) UNIQUE NOT NULL,
          password_hash VARCHAR2(255) NOT NULL,
          is_active NUMBER(1) DEFAULT 1,
          created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
  """;

  // Create sequence for admin_users
  String createAdminSequence = """
      CREATE SEQUENCE admin_seq 
      START WITH 1 
      INCREMENT BY 1 
      NOCACHE
  """;

  // Insert default admin user (username: admin, password: admin123)
  String insertDefaultAdmin = """
      INSERT INTO admin_users (id, username, password_hash, is_active) 
      SELECT admin_seq.NEXTVAL, 'admin', 'admin123', 1 
      FROM dual 
      WHERE NOT EXISTS (SELECT 1 FROM admin_users WHERE username = 'admin')
  """;

  // Execute these statements in your initializeDatabase method
  try (Statement stmt = conn.createStatement()) {
      stmt.execute(createAdminUsersTable);
      
      // Check if sequence exists before creating
      try {
          stmt.execute(createAdminSequence);
      } catch (SQLException e) {
          if (!e.getMessage().contains("name is already used")) {
              throw e;
          }
      }
      
      stmt.execute(insertDefaultAdmin);
      System.out.println("Admin users table initialized successfully");
  }
 }
 
 private static void insertInitialData(Connection conn) throws SQLException {
     // Insert menu items
     String insertMenu = "INSERT INTO menu_items (id, name, price) VALUES (?, ?, ?)";
     Object[][] menuData = {
         {1, "Meals", 179.00},
         {2, "Veg Biryani", 199.00},
         {3, "Dum Biryani", 299.00},
         {4, "Gobi Manchuria", 79.00},
         {5, "Butterskotch Milkshake", 59.00},
         {6, "Rotis", 10.00},
         {7, "Bathua Raita", 169.00},
         {8, "Prawns Curry", 349.00},
         {9, "Water Bottle", 25.00},
         {10, "Egg Burj", 99.00}
     };
     
     try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM menu_items");
          ResultSet rs = stmt.executeQuery()) {
         rs.next();
         if (rs.getInt(1) == 0) {
             try (PreparedStatement insertStmt = conn.prepareStatement(insertMenu)) {
                 for (Object[] item : menuData) {
                     insertStmt.setInt(1, (Integer) item[0]);
                     insertStmt.setString(2, (String) item[1]);
                     insertStmt.setDouble(3, (Double) item[2]);
                     insertStmt.executeUpdate();
                 }
             }
         }
     }
     
     // Insert tables
     String insertTable = "INSERT INTO tables (table_number, capacity) VALUES (?, ?)";
     Object[][] tableData = {
         {1, 4}, {2, 4}, {3, 6}, {4, 2}, {5, 8}
     };
     
     try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM tables");
          ResultSet rs = stmt.executeQuery()) {
         rs.next();
         if (rs.getInt(1) == 0) {
             try (PreparedStatement insertStmt = conn.prepareStatement(insertTable)) {
                 for (Object[] table : tableData) {
                     insertStmt.setInt(1, (Integer) table[0]);
                     insertStmt.setInt(2, (Integer) table[1]);
                     insertStmt.executeUpdate();
                 }
             }
         }
     }
 }
 
 
}