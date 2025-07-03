package application;
//RestaurantManagementApp.java
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;

public class RestaurantManagementApp extends Application {
 
 @Override
 public void start(Stage primaryStage) {
     DatabaseConnection.initializeDatabase();
     
     primaryStage.setTitle("Restaurant Management System");
     
     // Create main layout
     VBox mainLayout = new VBox(20);
     mainLayout.setPadding(new Insets(30));
     mainLayout.setAlignment(Pos.CENTER);
     mainLayout.setStyle("-fx-background-color: linear-gradient(to bottom, #667eea 0%, #764ba2 100%);");
     
     // Title
     Label titleLabel = new Label("Restaurant Management System");
     titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
     
     // Buttons
     Button customerBtn = createStyledButton("Customer Portal", "#4CAF50");
     Button adminBtn = createStyledButton("Admin Portal", "#2196F3");
     Button exitBtn = createStyledButton("Exit", "#f44336");
     
     customerBtn.setOnAction(e -> openCustomerPortal());
     adminBtn.setOnAction(e -> openAdminPortal());
     exitBtn.setOnAction(e -> primaryStage.close());
     
     mainLayout.getChildren().addAll(titleLabel, customerBtn, adminBtn, exitBtn);
     
     Scene scene = new Scene(mainLayout, 400, 300);
     primaryStage.setScene(scene);
     primaryStage.show();
 }
 
 private Button createStyledButton(String text, String color) {
     Button button = new Button(text);
     button.setPrefSize(200, 50);
     button.setStyle(String.format(
         "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 20px; " +
         "-fx-background-radius: 5; -fx-cursor: hand;", color));
     
     button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-opacity: 0.8;"));
     button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-opacity: 0.8;", "")));
     
     return button;
 }
 
 private void openCustomerPortal() {
     Stage customerStage = new Stage();
     customerStage.setTitle("Customer Portal");
     
     TabPane tabPane = new TabPane();
     
     // Registration Tab
     Tab registrationTab = new Tab("Registration");
     registrationTab.setContent(createRegistrationPane());
     registrationTab.setClosable(false);
     
     // Menu Tab
     Tab menuTab = new Tab("View Menu");
     menuTab.setContent(createMenuPane());
     menuTab.setClosable(false);
     
     // Order Tab
     Tab orderTab = new Tab("Place Order");
     orderTab.setContent(createOrderPane());
     orderTab.setClosable(false);
     
     // Booking Tab
     Tab bookingTab = new Tab("Book Table");
     bookingTab.setContent(createBookingPane());
     bookingTab.setClosable(false);
     
     // Payment Tab
     Tab paymentTab = new Tab("Make Payment");
     paymentTab.setContent(createPaymentPane());
     paymentTab.setClosable(false);
     
     // Feedback Tab
     Tab feedbackTab = new Tab("Feedback");
     feedbackTab.setContent(createFeedbackPane());
     feedbackTab.setClosable(false);
     
     tabPane.getTabs().addAll(registrationTab, menuTab, orderTab, bookingTab, paymentTab, feedbackTab);
     
     Scene scene = new Scene(tabPane, 800, 600);
     customerStage.setScene(scene);
     customerStage.show();
 }
//First, add this method to create the admin login dialog
 private String showAdminLogin() {
	    Dialog<String[]> dialog = new Dialog<>();
	    dialog.setTitle("Admin Login");
	    dialog.setHeaderText("Please enter admin credentials");
	    
	    // Set the button types
	    ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
	    dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);
	    
	    // Create the username and password labels and fields
	    GridPane grid = new GridPane();
	    grid.setHgap(10);
	    grid.setVgap(10);
	    grid.setPadding(new Insets(20, 150, 10, 10));
	    
	    TextField username = new TextField();
	    username.setPromptText("Username");
	    PasswordField password = new PasswordField();
	    password.setPromptText("Password");
	    
	    grid.add(new Label("Username:"), 0, 0);
	    grid.add(username, 1, 0);
	    grid.add(new Label("Password:"), 0, 1);
	    grid.add(password, 1, 1);
	    
	    // Enable/Disable login button depending on whether a username was entered
	    Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);
	    loginButton.setDisable(true);
	    
	    // Do some validation (using the Java 8 lambda syntax)
	    username.textProperty().addListener((observable, oldValue, newValue) -> {
	        loginButton.setDisable(newValue.trim().isEmpty());
	    });
	    
	    dialog.getDialogPane().setContent(grid);
	    
	    // Request focus on the username field by default
	    Platform.runLater(() -> username.requestFocus());
	    
	    // Convert the result to a username-password-pair when the login button is clicked
	    dialog.setResultConverter(dialogButton -> {
	        if (dialogButton == loginButtonType) {
	            return new String[]{username.getText(), password.getText()};
	        }
	        return null;
	    });
	    
	    Optional<String[]> result = dialog.showAndWait();
	    
	    if (result.isPresent()) {
	        String[] credentials = result.get();
	        if (authenticateAdmin(credentials[0], credentials[1])) {
	            return credentials[0]; // Return username if authentication successful
	        }
	    }
	    
	    return null; // Return null if authentication failed or cancelled
	}

//Add this method to authenticate admin credentials
private boolean authenticateAdmin(String username, String password) {
  try (Connection conn = DatabaseConnection.getConnection()) {
      String sql = "SELECT id, password_hash FROM admin_users WHERE username = ? AND is_active = 1";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
          stmt.setString(1, username);
          try (ResultSet rs = stmt.executeQuery()) {
              if (rs.next()) {
                  String storedHash = rs.getString("password_hash");
                  // For simplicity, we'll use basic comparison
                  // In production, use proper password hashing like BCrypt
                  return password.equals(storedHash) || checkPasswordHash(password, storedHash);
              }
          }
      }
  } catch (SQLException e) {
      showAlert("Error", "Authentication failed: " + e.getMessage());
      return false;
  }
  
  showAlert("Login Failed", "Invalid username or password!");
  return false;
}

//Simple password hashing check (replace with BCrypt in production)
private boolean checkPasswordHash(String password, String hash) {
  // For demo purposes, storing plain text passwords
  // In production, use: BCrypt.checkpw(password, hash)
  return password.equals(hash);
}

//Update the openAdminPortal method to include login check
private void openAdminPortal() {
  // Show login dialog first
	String adminUsername = showAdminLogin();
    if (adminUsername == null) {
        return; // Exit if login failed
    }
    showWelcomeMessage("Admin Login Successful", 
            "Welcome back, " + adminUsername + "!\n\nYou have successfully logged into the Admin Portal.\nYou now have access to all administrative functions.");
  
  Stage adminStage = new Stage();
  adminStage.setTitle("Admin Portal - Authenticated");
  
  TabPane tabPane = new TabPane();
  
  // Add a logout option in the admin portal
  Tab logoutTab = new Tab("Logout");
  logoutTab.setContent(createLogoutPane(adminStage, adminUsername));
  logoutTab.setClosable(false);
  
  // Customer Management Tab
  Tab customerTab = new Tab("Customer Management");
  customerTab.setContent(createCustomerManagementPane());
  customerTab.setClosable(false);
  
  // Table Management Tab
  Tab tableTab = new Tab("Table Management");
  tableTab.setContent(createTableManagementPane());
  tableTab.setClosable(false);
  
  // Payment Records Tab
  Tab paymentTab = new Tab("Payment Records");
  paymentTab.setContent(createPaymentRecordsPane());
  paymentTab.setClosable(false);
  
  // Analytics Tab
  Tab analyticsTab = new Tab("Analytics");
  analyticsTab.setContent(createAnalyticsPane());
  analyticsTab.setClosable(false);
  
  // Admin Settings Tab (new)
  Tab settingsTab = new Tab("Admin Settings");
  settingsTab.setContent(createAdminSettingsPane());
  settingsTab.setClosable(false);
  
  tabPane.getTabs().addAll(customerTab, tableTab, paymentTab, analyticsTab, settingsTab, logoutTab);
  
  Scene scene = new Scene(tabPane, 900, 700);
  adminStage.setScene(scene);
  adminStage.show();
}


//Create logout pane
private VBox createLogoutPane(Stage adminStage, String adminUsername) {
  VBox layout = new VBox(20);
  layout.setPadding(new Insets(50));
  layout.setAlignment(Pos.CENTER);
  
  Label titleLabel = new Label("Admin Session");
  titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
  
  Label welcomeLabel = new Label("Welcome, " + adminUsername + "!");
  welcomeLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");
  
  Label infoLabel = new Label("You are currently logged in as an administrator.");
  infoLabel.setStyle("-fx-font-size: 14px;");
  
  Button logoutBtn = new Button("Logout");
  logoutBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 16px;");
  logoutBtn.setPrefSize(150, 40);
  
  logoutBtn.setOnAction(e -> {
      Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
      confirmAlert.setTitle("Logout Confirmation");
      confirmAlert.setHeaderText("Are you sure you want to logout?");
      confirmAlert.setContentText("You will need to login again to access the admin portal.");
      
      Optional<ButtonType> result = confirmAlert.showAndWait();
      if (result.isPresent() && result.get() == ButtonType.OK) {
          adminStage.close();
      }
  });
  
  layout.getChildren().addAll(titleLabel, infoLabel, logoutBtn);
  return layout;
}

//Create admin settings pane for managing admin accounts
private VBox createAdminSettingsPane() {
  VBox layout = new VBox(15);
  layout.setPadding(new Insets(20));
  
  Label titleLabel = new Label("Admin Settings");
  titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
  
  // Change password section
  Label changePasswordLabel = new Label("Change Password");
  changePasswordLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
  
  TextField currentUsernameField = new TextField();
  currentUsernameField.setPromptText("Current Username");
  
  PasswordField currentPasswordField = new PasswordField();
  currentPasswordField.setPromptText("Current Password");
  
  PasswordField newPasswordField = new PasswordField();
  newPasswordField.setPromptText("New Password");
  
  PasswordField confirmPasswordField = new PasswordField();
  confirmPasswordField.setPromptText("Confirm New Password");
  
  Button changePasswordBtn = new Button("Change Password");
  changePasswordBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
  
  changePasswordBtn.setOnAction(e -> {
      String username = currentUsernameField.getText().trim();
      String currentPassword = currentPasswordField.getText();
      String newPassword = newPasswordField.getText();
      String confirmPassword = confirmPasswordField.getText();
      
      if (username.isEmpty() || currentPassword.isEmpty() || newPassword.isEmpty()) {
          showAlert("Error", "Please fill in all fields!");
          return;
      }
      
      if (!newPassword.equals(confirmPassword)) {
          showAlert("Error", "New passwords do not match!");
          return;
      }
      
      if (newPassword.length() < 6) {
          showAlert("Error", "New password must be at least 6 characters long!");
          return;
      }
      
      // Verify current credentials and update password
      if (authenticateAdmin(username, currentPassword)) {
          try (Connection conn = DatabaseConnection.getConnection()) {
              String sql = "UPDATE admin_users SET password_hash = ? WHERE username = ?";
              try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                  stmt.setString(1, newPassword); // In production, hash this password
                  stmt.setString(2, username);
                  int updated = stmt.executeUpdate();
                  
                  if (updated > 0) {
                      showAlert("Success", "Password changed successfully!");
                      currentUsernameField.clear();
                      currentPasswordField.clear();
                      newPasswordField.clear();
                      confirmPasswordField.clear();
                  } else {
                      showAlert("Error", "Failed to update password!");
                  }
              }
          } catch (SQLException ex) {
              showAlert("Error", "Database error: " + ex.getMessage());
          }
      } else {
          showAlert("Error", "Current credentials are invalid!");
      }
  });
  
  // Add separator
  Separator separator = new Separator();
  
  // Create new admin section
  Label createAdminLabel = new Label("Create New Admin Account");
  createAdminLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
  
  TextField newUsernameField = new TextField();
  newUsernameField.setPromptText("New Username");
  
  PasswordField newAdminPasswordField = new PasswordField();
  newAdminPasswordField.setPromptText("Password");
  
  Button createAdminBtn = new Button("Create Admin");
  createAdminBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
  
  createAdminBtn.setOnAction(e -> {
      String username = newUsernameField.getText().trim();
      String password = newAdminPasswordField.getText();
      
      if (username.isEmpty() || password.isEmpty()) {
          showAlert("Error", "Please fill in all fields!");
          return;
      }
      
      if (password.length() < 6) {
          showAlert("Error", "Password must be at least 6 characters long!");
          return;
      }
      
      try (Connection conn = DatabaseConnection.getConnection()) {
          String sql = "INSERT INTO admin_users (id, username, password_hash, is_active) VALUES (admin_seq.NEXTVAL, ?, ?, 1)";
          try (PreparedStatement stmt = conn.prepareStatement(sql)) {
              stmt.setString(1, username);
              stmt.setString(2, password); // In production, hash this password
              stmt.executeUpdate();
              
              showAlert("Success", "Admin account created successfully!");
              newUsernameField.clear();
              newAdminPasswordField.clear();
          }
      } catch (SQLException ex) {
          if (ex.getMessage().contains("unique constraint")) {
              showAlert("Error", "Username already exists!");
          } else {
              showAlert("Error", "Failed to create admin: " + ex.getMessage());
          }
      }
  });
  
  layout.getChildren().addAll(titleLabel,
      changePasswordLabel,
      new Label("Current Username:"), currentUsernameField,
      new Label("Current Password:"), currentPasswordField,
      new Label("New Password:"), newPasswordField,
      new Label("Confirm Password:"), confirmPasswordField,
      changePasswordBtn,
      separator,
      createAdminLabel,
      new Label("Username:"), newUsernameField,
      new Label("Password:"), newAdminPasswordField,
      createAdminBtn);
  
  return layout;
}
 
 private VBox createRegistrationPane() {
     VBox layout = new VBox(15);
     layout.setPadding(new Insets(20));
     
     Label titleLabel = new Label("Customer Registration");
     titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
     
     TextField nameField = new TextField();
     nameField.setPromptText("Enter your name");
     nameField.setPrefWidth(300);
     
     Button registerBtn = new Button("Register");
     registerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px;");
     
     TextArea resultArea = new TextArea();
     resultArea.setEditable(false);
     resultArea.setPrefRowCount(5);
     
     registerBtn.setOnAction(e -> {
         String name = nameField.getText().trim();
         if (name.isEmpty()) {
             resultArea.setText("Please enter a valid name!");
             return;
         }
         
         try (Connection conn = DatabaseConnection.getConnection()) {
             String sql = "INSERT INTO customers (id, name) VALUES (customer_seq.NEXTVAL, ?)";
             try (PreparedStatement stmt = conn.prepareStatement(sql, new String[]{"id"})) {
                 stmt.setString(1, name);
                 stmt.executeUpdate();
                 
                 try (ResultSet rs = stmt.getGeneratedKeys()) {
                     if (rs.next()) {
                         int customerId = rs.getInt(1);
                         showWelcomeMessage("Registration Successful", 
                                 "Welcome to our Restaurant, " + name + "!\n\n" +
                                 "Your registration has been completed successfully.\n" +
                                 "Your Customer ID is: " + customerId + "\n\n" +
                                 "Please remember your Customer ID as you'll need it to:\n" +
                                 "• Place orders\n" +
                                 "• Book tables\n" +
                                 "• Make payments\n" +
                                 "• Provide feedback\n\n" +
                                 "Thank you for choosing our restaurant!");
                             
                             resultArea.setText("Registration successful!\nYour Customer ID is: " + customerId);
                             nameField.clear();
                         
             }
                 }
             }
         } catch (SQLException ex) {
             resultArea.setText("Registration failed: " + ex.getMessage());
         }
     });
     
     layout.getChildren().addAll(titleLabel, 
         new Label("Name:"), nameField, registerBtn, resultArea);
     
     return layout;
 }
 private void showWelcomeMessage(String title, String message) {
	    Alert welcomeAlert = new Alert(Alert.AlertType.INFORMATION);
	    welcomeAlert.setTitle(title);
	    welcomeAlert.setHeaderText("🎉 " + title + " 🎉");
	    welcomeAlert.setContentText(message);
	    
	    // Make the dialog wider to accommodate longer text
	    welcomeAlert.getDialogPane().setPrefWidth(500);
	    welcomeAlert.getDialogPane().setPrefHeight(400);
	    
	    // Style the alert
	    welcomeAlert.getDialogPane().setStyle(
	        "-fx-background-color: linear-gradient(to bottom, #e3f2fd 0%, #f3e5f5 100%);"
	    );
	    
	    welcomeAlert.showAndWait();
	}
 
 
 private VBox createMenuPane() {
     VBox layout = new VBox(15);
     layout.setPadding(new Insets(20));
     
     Label titleLabel = new Label("Menu");
     titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
     
     ListView<String> menuListView = new ListView<>();
     menuListView.setPrefHeight(400);
     
     Button refreshBtn = new Button("Refresh Menu");
     refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
     
     refreshBtn.setOnAction(e -> loadMenu(menuListView));
     
     // Load menu initially
     loadMenu(menuListView);
     
     layout.getChildren().addAll(titleLabel, menuListView, refreshBtn);
     return layout;
 }
 
 private void loadMenu(ListView<String> menuListView) {
     ObservableList<String> menuItems = FXCollections.observableArrayList();
     
     try (Connection conn = DatabaseConnection.getConnection()) {
         String sql = "SELECT id, name, price FROM menu_items ORDER BY price";
         try (PreparedStatement stmt = conn.prepareStatement(sql);
              ResultSet rs = stmt.executeQuery()) {
             
             while (rs.next()) {
                 String item = String.format("ID: %d | %s | Price: ₹%.2f", 
                     rs.getInt("id"), rs.getString("name"), rs.getDouble("price"));
                 menuItems.add(item);
             }
         }
     } catch (SQLException e) {
         menuItems.add("Error loading menu: " + e.getMessage());
     }
     
     menuListView.setItems(menuItems);
 }
 
 private VBox createOrderPane() {
     VBox layout = new VBox(15);
     layout.setPadding(new Insets(20));
     
     Label titleLabel = new Label("Place Order");
     titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
     
     TextField customerIdField = new TextField();
     customerIdField.setPromptText("Enter Customer ID");
     
     ListView<String> menuListView = new ListView<>();
     menuListView.setPrefHeight(200);
     loadMenu(menuListView);
     
     TextField itemIdField = new TextField();
     itemIdField.setPromptText("Enter Item ID");
     
     TextField quantityField = new TextField();
     quantityField.setPromptText("Quantity");
     quantityField.setText("1");
     
     ListView<String> orderItemsListView = new ListView<>();
     orderItemsListView.setPrefHeight(150);
     
     Button addItemBtn = new Button("Add Item");
     Button placeOrderBtn = new Button("Place Order");
     
     addItemBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
     placeOrderBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
     
     ObservableList<String> orderItems = FXCollections.observableArrayList();
     orderItemsListView.setItems(orderItems);
     
     addItemBtn.setOnAction(e -> {
         try {
             int itemId = Integer.parseInt(itemIdField.getText().trim());
             int quantity = Integer.parseInt(quantityField.getText().trim());
             
             try (Connection conn = DatabaseConnection.getConnection()) {
                 String sql = "SELECT name, price FROM menu_items WHERE id = ?";
                 try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                     stmt.setInt(1, itemId);
                     try (ResultSet rs = stmt.executeQuery()) {
                         if (rs.next()) {
                             String itemText = String.format("%s (Qty: %d) - ₹%.2f", 
                                 rs.getString("name"), quantity, rs.getDouble("price") * quantity);
                             orderItems.add(itemId + ":" + quantity + ":" + itemText);
                             itemIdField.clear();
                             quantityField.setText("1");
                         } else {
                             showAlert("Error", "Item not found!");
                         }
                     }
                 }
             }
         } catch (Exception ex) {
             showAlert("Error", "Invalid input: " + ex.getMessage());
         }
     });
     
     placeOrderBtn.setOnAction(e -> {
         try {
             int customerId = Integer.parseInt(customerIdField.getText().trim());
             
             if (orderItems.isEmpty()) {
                 showAlert("Error", "Please add items to your order!");
                 return;
             }
             
             try (Connection conn = DatabaseConnection.getConnection()) {
                 conn.setAutoCommit(false);
                 
                 // Insert order
                 String orderSql = "INSERT INTO orders (order_id, customer_id, total_amount) VALUES (order_seq.NEXTVAL, ?, ?)";
                 double totalAmount = 0;
                 
                 // Calculate total
                 for (String item : orderItems) {
                     String[] parts = item.split(":");
                     int itemId = Integer.parseInt(parts[0]);
                     int quantity = Integer.parseInt(parts[1]);
                     
                     String priceSql = "SELECT price FROM menu_items WHERE id = ?";
                     try (PreparedStatement priceStmt = conn.prepareStatement(priceSql)) {
                         priceStmt.setInt(1, itemId);
                         try (ResultSet rs = priceStmt.executeQuery()) {
                             if (rs.next()) {
                                 totalAmount += rs.getDouble("price") * quantity;
                             }
                         }
                     }
                 }
                 
                 int orderId;
                 try (PreparedStatement orderStmt = conn.prepareStatement(orderSql, new String[]{"order_id"})) {
                     orderStmt.setInt(1, customerId);
                     orderStmt.setDouble(2, totalAmount);
                     orderStmt.executeUpdate();
                     
                     try (ResultSet rs = orderStmt.getGeneratedKeys()) {
                         rs.next();
                         orderId = rs.getInt(1);
                     }
                 }
                 
                 // Insert order items
                 String itemSql = "INSERT INTO order_items (id, order_id, menu_item_id, quantity) VALUES (order_item_seq.NEXTVAL, ?, ?, ?)";
                 try (PreparedStatement itemStmt = conn.prepareStatement(itemSql)) {
                     for (String item : orderItems) {
                         String[] parts = item.split(":");
                         int itemId = Integer.parseInt(parts[0]);
                         int quantity = Integer.parseInt(parts[1]);
                         
                         itemStmt.setInt(1, orderId);
                         itemStmt.setInt(2, itemId);
                         itemStmt.setInt(3, quantity);
                         itemStmt.executeUpdate();
                     }
                 }
                 
                 conn.commit();
                 showAlert("Success", "Order placed successfully!\nOrder ID: " + orderId + "\nTotal: ₹" + totalAmount);
                 
                 // Clear form
                 customerIdField.clear();
                 orderItems.clear();
                 
             } catch (SQLException ex) {
                 showAlert("Error", "Failed to place order: " + ex.getMessage());
             }
         } catch (NumberFormatException ex) {
             showAlert("Error", "Please enter a valid customer ID!");
         }
     });
     
     HBox buttonBox = new HBox(10, addItemBtn, placeOrderBtn);
     
     layout.getChildren().addAll(titleLabel,
         new Label("Customer ID:"), customerIdField,
         new Label("Menu:"), menuListView,
         new Label("Item ID:"), itemIdField,
         new Label("Quantity:"), quantityField,
         buttonBox,
         new Label("Order Items:"), orderItemsListView);
     
     return layout;
 }
 
 private VBox createBookingPane() {
     VBox layout = new VBox(15);
     layout.setPadding(new Insets(20));
     
     Label titleLabel = new Label("Book Table");
     titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
     
     TextField customerIdField = new TextField();
     customerIdField.setPromptText("Enter Customer ID");
     
     TextField partySizeField = new TextField();
     partySizeField.setPromptText("Number of people");
     
     DatePicker datePicker = new DatePicker();
     
     ComboBox<String> timeComboBox = new ComboBox<>();
     timeComboBox.getItems().addAll("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00");
     
     ListView<String> availableTablesListView = new ListView<>();
     availableTablesListView.setPrefHeight(150);
     
     Button checkAvailabilityBtn = new Button("Check Availability");
     Button bookTableBtn = new Button("Book Table");
     
     checkAvailabilityBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
     bookTableBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
     
     checkAvailabilityBtn.setOnAction(e -> {
         try {
             int partySize = Integer.parseInt(partySizeField.getText().trim());
             if (datePicker.getValue() == null || timeComboBox.getValue() == null) {
                 showAlert("Error", "Please select date and time!");
                 return;
             }
             
             LocalDateTime bookingTime = LocalDateTime.of(datePicker.getValue(), 
                 java.time.LocalTime.parse(timeComboBox.getValue()));
             
             ObservableList<String> availableTables = FXCollections.observableArrayList();
             
             try (Connection conn = DatabaseConnection.getConnection()) {
                 String sql = """
                     SELECT t.table_number, t.capacity 
                     FROM tables t 
                     WHERE t.capacity >= ? 
                     AND t.table_number NOT IN (
                         SELECT tb.table_number 
                         FROM table_bookings tb 
                         WHERE tb.booking_time BETWEEN ? AND ?
                     )
                     ORDER BY t.capacity
                     """;
                 
                 try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                     stmt.setInt(1, partySize);
                     stmt.setTimestamp(2, Timestamp.valueOf(bookingTime.minusHours(1)));
                     stmt.setTimestamp(3, Timestamp.valueOf(bookingTime.plusHours(1)));
                     
                     try (ResultSet rs = stmt.executeQuery()) {
                         while (rs.next()) {
                             availableTables.add("Table " + rs.getInt("table_number") + 
                                 " (Capacity: " + rs.getInt("capacity") + ")");
                         }
                     }
                 }
             }
             
             //if (availableTables.isEmpty()) {
               //  availableTables.add("No tables available for the selected time and party size");
             //}
             
             availableTablesListView.setItems(availableTables);
             
         } catch (Exception ex) {
             showAlert("Error", "Error checking availability: " + ex.getMessage());
         }
     });
     
     bookTableBtn.setOnAction(e -> {
         try {
             int customerId = Integer.parseInt(customerIdField.getText().trim());
             int partySize = Integer.parseInt(partySizeField.getText().trim());
             
             String selectedTable = availableTablesListView.getSelectionModel().getSelectedItem();
             if (selectedTable == null || selectedTable.contains("No tables available")) {
                 showAlert("Error", "Please select an available table!");
                 return;
             }
             
             int tableNumber = Integer.parseInt(selectedTable.split(" ")[1]);
             LocalDateTime bookingTime = LocalDateTime.of(datePicker.getValue(), 
                 java.time.LocalTime.parse(timeComboBox.getValue()));
             
             try (Connection conn = DatabaseConnection.getConnection()) {
                 String sql = "INSERT INTO table_bookings (id, table_number, customer_id, booking_time, party_size) VALUES (booking_seq.NEXTVAL, ?, ?, ?, ?)";
                 try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                     stmt.setInt(1, tableNumber);
                     stmt.setInt(2, customerId);
                     stmt.setTimestamp(3, Timestamp.valueOf(bookingTime));
                     stmt.setInt(4, partySize);
                     stmt.executeUpdate();
                     
                     showAlert("Success", "Table booked successfully!\nTable Number: " + tableNumber + 
                         "\nDate: " + datePicker.getValue() + "\nTime: " + timeComboBox.getValue());
                     
                     // Clear form
                     customerIdField.clear();
                     partySizeField.clear();
                     datePicker.setValue(null);
                     timeComboBox.setValue(null);
                     availableTablesListView.getItems().clear();
                 }
             }
         } catch (Exception ex) {
             showAlert("Error", "Booking failed: " + ex.getMessage());
         }
     });
     
     HBox buttonBox = new HBox(10, checkAvailabilityBtn, bookTableBtn);
     
     layout.getChildren().addAll(titleLabel,
         new Label("Customer ID:"), customerIdField,
         new Label("Party Size:"), partySizeField,
         new Label("Date:"), datePicker,
         new Label("Time:"), timeComboBox,
         buttonBox,
         new Label("Available Tables:"), availableTablesListView);
     
     return layout;
 }
 
 private VBox createPaymentPane() {
     VBox layout = new VBox(15);
     layout.setPadding(new Insets(20));
     
     Label titleLabel = new Label("Make Payment");
     titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
     
     TextField orderIdField = new TextField();
     orderIdField.setPromptText("Enter Order ID");
     
     TextArea orderDetailsArea = new TextArea();
     orderDetailsArea.setEditable(false);
     orderDetailsArea.setPrefRowCount(8);
     
     TextField paymentAmountField = new TextField();
     paymentAmountField.setPromptText("Enter payment amount");
     
     Button checkOrderBtn = new Button("Check Order");
     Button makePaymentBtn = new Button("Make Payment");
     
     checkOrderBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
     makePaymentBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
     
     checkOrderBtn.setOnAction(e -> {
         try {
             int orderId = Integer.parseInt(orderIdField.getText().trim());
             
             try (Connection conn = DatabaseConnection.getConnection()) {
                 String sql = """
                     SELECT o.order_id, o.customer_id, c.name, o.total_amount, o.is_paid, o.order_time
                     FROM orders o
                     JOIN customers c ON o.customer_id = c.id
                     WHERE o.order_id = ?
                     """;
                 
                 try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                     stmt.setInt(1, orderId);
                     try (ResultSet rs = stmt.executeQuery()) {
                         if (rs.next()) {
                             StringBuilder details = new StringBuilder();
                             details.append("Order ID: ").append(rs.getInt("order_id")).append("\n");
                             details.append("Customer: ").append(rs.getString("name")).append("\n");
                             details.append("Order Time: ").append(rs.getTimestamp("order_time")).append("\n");
                             details.append("Total Amount: ₹").append(rs.getDouble("total_amount")).append("\n");
                             details.append("Payment Status: ").append(rs.getInt("is_paid") == 1 ? "Paid" : "Pending").append("\n\n");
                             
                             // Get order items
                             String itemsSql = """
                                 SELECT mi.name, oi.quantity, mi.price
                                 FROM order_items oi
                                 JOIN menu_items mi ON oi.menu_item_id = mi.id
                                 WHERE oi.order_id = ?
                                 """;
                             
                             try (PreparedStatement itemsStmt = conn.prepareStatement(itemsSql)) {
                                 itemsStmt.setInt(1, orderId);
                                 try (ResultSet itemsRs = itemsStmt.executeQuery()) {
                                     details.append("Items:\n");
                                     while (itemsRs.next()) {
                                         details.append("- ").append(itemsRs.getString("name"))
                                             .append(" (Qty: ").append(itemsRs.getInt("quantity"))
                                             .append(") - ₹").append(itemsRs.getDouble("price")).append("\n");
                                     }
                                 }
                             }
                             
                             orderDetailsArea.setText(details.toString());
                             
                             if (rs.getInt("is_paid") == 1) {
                                 makePaymentBtn.setDisable(true);
                                 makePaymentBtn.setText("Already Paid");
                             } else {
                                 makePaymentBtn.setDisable(false);
                                 makePaymentBtn.setText("Make Payment");
                             }
                         } else {
                             orderDetailsArea.setText("Order not found!");
                             makePaymentBtn.setDisable(true);
                         }
                     }
                 }
             }
         } catch (Exception ex) {
             orderDetailsArea.setText("Error: " + ex.getMessage());
         }
     });
     
     makePaymentBtn.setOnAction(e -> {
         try {
             int orderId = Integer.parseInt(orderIdField.getText().trim());
             double paymentAmount = Double.parseDouble(paymentAmountField.getText().trim());
             
             try (Connection conn = DatabaseConnection.getConnection()) {
                 // Get order total
                 String checkSql = "SELECT total_amount, is_paid FROM orders WHERE order_id = ?";
                 try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                     checkStmt.setInt(1, orderId);
                     try (ResultSet rs = checkStmt.executeQuery()) {
                         if (rs.next()) {
                             double totalAmount = rs.getDouble("total_amount");
                             int isPaid = rs.getInt("is_paid");
                             
                             if (isPaid == 1) {
                                 showAlert("Info", "Order is already paid!");
                                 return;
                             }
                             
                             if (paymentAmount >= totalAmount) {
                                 // Update payment status
                                 String updateSql = "UPDATE orders SET is_paid = 1 WHERE order_id = ?";
                                 try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                                     updateStmt.setInt(1, orderId);
                                     updateStmt.executeUpdate();
                                     
                                     double change = paymentAmount - totalAmount;
                                     showAlert("Success", "Payment successful!\nChange: ₹" + String.format("%.2f", change));
                                     
                                     // Clear form
                                     orderIdField.clear();
                                     paymentAmountField.clear();
                                     orderDetailsArea.clear();
                                     makePaymentBtn.setDisable(true);
                                 }
                             } else {
                                 showAlert("Error", "Insufficient payment!\nRequired: ₹" + totalAmount + "\nProvided: ₹" + paymentAmount);
                             }
                         }
                     }
                 }
             }
         } catch (Exception ex) {
             showAlert("Error", "Payment failed: " + ex.getMessage());
         }
     });
     
     HBox buttonBox = new HBox(10, checkOrderBtn, makePaymentBtn);
     
     layout.getChildren().addAll(titleLabel,
         new Label("Order ID:"), orderIdField,
         buttonBox,
         new Label("Order Details:"), orderDetailsArea,
         new Label("Payment Amount:"), paymentAmountField);
     
     return layout;
 }
 
 private VBox createFeedbackPane() {
     VBox layout = new VBox(15);
     layout.setPadding(new Insets(20));
     
     Label titleLabel = new Label("Provide Feedback");
     titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
     
     TextField customerIdField = new TextField();
     customerIdField.setPromptText("Enter Customer ID");
     
     TextArea commentArea = new TextArea();
     commentArea.setPromptText("Enter your feedback...");
     commentArea.setPrefRowCount(5);
     
     ComboBox<Integer> ratingComboBox = new ComboBox<>();
     ratingComboBox.getItems().addAll(1, 2, 3, 4, 5);
     ratingComboBox.setPromptText("Select Rating (1-5)");
     
     Button submitBtn = new Button("Submit Feedback");
     submitBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
     
     submitBtn.setOnAction(e -> {
         try {
             int customerId = Integer.parseInt(customerIdField.getText().trim());
             String comments = commentArea.getText().trim();
             Integer rating = ratingComboBox.getValue();
             
             if (comments.isEmpty() || rating == null) {
                 showAlert("Error", "Please provide both comments and rating!");
                 return;
             }
             
             try (Connection conn = DatabaseConnection.getConnection()) {
                 String sql = "INSERT INTO feedback (id, customer_id, comments, rating) VALUES (feedback_seq.NEXTVAL, ?, ?, ?)";
                 try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                     stmt.setInt(1, customerId);
                     stmt.setString(2, comments);
                     stmt.setInt(3, rating);
                     stmt.executeUpdate();
                     
                     showAlert("Success", "Thank you for your feedback!");
                     
                     // Clear form
                     customerIdField.clear();
                     commentArea.clear();
                     ratingComboBox.setValue(null);
                 }
             }
         } catch (Exception ex) {
             showAlert("Error", "Failed to submit feedback: " + ex.getMessage());
         }
     });
     
     layout.getChildren().addAll(titleLabel,
         new Label("Customer ID:"), customerIdField,
         new Label("Comment:"), commentArea,
         new Label("Rating:"), ratingComboBox,
         submitBtn);
     
     return layout;
 }
 
 private VBox createCustomerManagementPane() {
     VBox layout = new VBox(15);
     layout.setPadding(new Insets(20));
     
     Label titleLabel = new Label("Customer Management");
     titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
     
     // Customer count section
     HBox countBox = new HBox(10);
     Label totalCustomersLabel = new Label("Total Customers: ");
     Label countLabel = new Label("0");
     countLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
     countBox.getChildren().addAll(totalCustomersLabel, countLabel);
     
     // Customer list
     ListView<String> customerListView = new ListView<>();
     customerListView.setPrefHeight(300);
     
     // Update customer section
     TextField customerIdField = new TextField();
     customerIdField.setPromptText("Customer ID to update");
     
     TextField newNameField = new TextField();
     newNameField.setPromptText("New name (optional)");
     
     Button refreshBtn = new Button("Refresh List");
     Button updateBtn = new Button("Update Customer");
     
     refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
     updateBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
     
     refreshBtn.setOnAction(e -> loadCustomers(customerListView, countLabel));
     
     updateBtn.setOnAction(e -> {
         try {
             int customerId = Integer.parseInt(customerIdField.getText().trim());
             String newName = newNameField.getText().trim();
             
             if (newName.isEmpty()) {
                 showAlert("Error", "Please enter a new name!");
                 return;
             }
             
             try (Connection conn = DatabaseConnection.getConnection()) {
                 String sql = "UPDATE customers SET name = ? WHERE id = ?";
                 try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                     stmt.setString(1, newName);
                     stmt.setInt(2, customerId);
                     int updated = stmt.executeUpdate();
                     
                     if (updated > 0) {
                         showAlert("Success", "Customer updated successfully!");
                         customerIdField.clear();
                         newNameField.clear();
                         loadCustomers(customerListView, countLabel);
                     } else {
                         showAlert("Error", "Customer not found!");
                     }
                 }
             }
         } catch (Exception ex) {
             showAlert("Error", "Update failed: " + ex.getMessage());
         }
     });
     
     HBox buttonBox = new HBox(10, refreshBtn, updateBtn);
     
     // Load customers initially
     loadCustomers(customerListView, countLabel);
     
     layout.getChildren().addAll(titleLabel, countBox,
         new Label("Customers:"), customerListView,
         new Label("Update Customer:"),
         new Label("Customer ID:"), customerIdField,
         new Label("New Name:"), newNameField,
         buttonBox);
     
     return layout;
 }
 
 private void loadCustomers(ListView<String> customerListView, Label countLabel) {
     ObservableList<String> customers = FXCollections.observableArrayList();
     
     try (Connection conn = DatabaseConnection.getConnection()) {
         String sql = "SELECT id, name, table_no, booking_time FROM customers ORDER BY id";
         try (PreparedStatement stmt = conn.prepareStatement(sql);
              ResultSet rs = stmt.executeQuery()) {
             
             int count = 0;
             while (rs.next()) {
                 count++;
                 String customer = String.format("ID: %d | Name: %s | Table: %s | Booking: %s",
                     rs.getInt("id"),
                     rs.getString("name"),
                     rs.getObject("table_no") != null ? rs.getString("table_no") : "None",
                     rs.getTimestamp("booking_time") != null ? rs.getTimestamp("booking_time").toString() : "None");
                 customers.add(customer);
             }
             countLabel.setText(String.valueOf(count));
         }
     } catch (SQLException e) {
         customers.add("Error loading customers: " + e.getMessage());
     }
     
     customerListView.setItems(customers);
 }
 
 private VBox createTableManagementPane() {
     VBox layout = new VBox(15);
     layout.setPadding(new Insets(20));
     
     Label titleLabel = new Label("Table Management");
     titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
     
     DatePicker datePicker = new DatePicker();
     ComboBox<String> timeComboBox = new ComboBox<>();
     timeComboBox.getItems().addAll("09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00");
     
     ListView<String> tableStatusListView = new ListView<>();
     tableStatusListView.setPrefHeight(400);
     
     Button checkStatusBtn = new Button("Check Table Status");
     checkStatusBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
     
     checkStatusBtn.setOnAction(e -> {
         if (datePicker.getValue() == null || timeComboBox.getValue() == null) {
             showAlert("Error", "Please select date and time!");
             return;
         }
         
         LocalDateTime checkTime = LocalDateTime.of(datePicker.getValue(), 
             java.time.LocalTime.parse(timeComboBox.getValue()));
         
         ObservableList<String> tableStatus = FXCollections.observableArrayList();
         
         try (Connection conn = DatabaseConnection.getConnection()) {
             String sql = """
                 SELECT t.table_number, t.capacity,
                        CASE WHEN tb.table_number IS NOT NULL THEN 'Booked' ELSE 'Available' END as status,
                        c.name as customer_name,
                        tb.party_size
                 FROM tables t
                 LEFT JOIN table_bookings tb ON t.table_number = tb.table_number 
                     AND tb.booking_time BETWEEN ? AND ?
                 LEFT JOIN customers c ON tb.customer_id = c.id
                 ORDER BY t.table_number
                 """;
             
             try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                 stmt.setTimestamp(1, Timestamp.valueOf(checkTime.minusMinutes(30)));
                 stmt.setTimestamp(2, Timestamp.valueOf(checkTime.plusMinutes(30)));
                 
                 try (ResultSet rs = stmt.executeQuery()) {
                     while (rs.next()) {
                         String status = String.format("Table %d (Capacity: %d) - %s",
                             rs.getInt("table_number"),
                             rs.getInt("capacity"),
                             rs.getString("status"));
                         
                         if ("Booked".equals(rs.getString("status"))) {
                             status += String.format(" by %s (Party: %d)",
                                 rs.getString("customer_name"),
                                 rs.getInt("party_size"));
                         }
                         
                         tableStatus.add(status);
                     }
                 }
             }
         } catch (SQLException ex) {
             tableStatus.add("Error loading table status: " + ex.getMessage());
         }
         
         tableStatusListView.setItems(tableStatus);
     });
     
     layout.getChildren().addAll(titleLabel,
         new Label("Select Date:"), datePicker,
         new Label("Select Time:"), timeComboBox,
         checkStatusBtn,
         new Label("Table Status:"), tableStatusListView);
     
     return layout;
 }
 
 private VBox createPaymentRecordsPane() {
     VBox layout = new VBox(15);
     layout.setPadding(new Insets(20));
     
     Label titleLabel = new Label("Payment Records");
     titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
     
     ListView<String> paymentListView = new ListView<>();
     paymentListView.setPrefHeight(400);
     
     Label totalRevenueLabel = new Label("Total Revenue: ₹0.00");
     totalRevenueLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: green;");
     
     Button refreshBtn = new Button("Refresh Records");
     refreshBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
     
     refreshBtn.setOnAction(e -> loadPaymentRecords(paymentListView, totalRevenueLabel));
     
     // Load payment records initially
     loadPaymentRecords(paymentListView, totalRevenueLabel);
     
     layout.getChildren().addAll(titleLabel, paymentListView, totalRevenueLabel, refreshBtn);
     
     return layout;
 }
 
 private void loadPaymentRecords(ListView<String> paymentListView, Label totalRevenueLabel) {
     ObservableList<String> payments = FXCollections.observableArrayList();
     double totalRevenue = 0;
     
     try (Connection conn = DatabaseConnection.getConnection()) {
         String sql = """
             SELECT o.order_id, c.name, o.total_amount, o.order_time
             FROM orders o
             JOIN customers c ON o.customer_id = c.id
             WHERE o.is_paid = 1
             ORDER BY o.order_time DESC
             """;
         
         try (PreparedStatement stmt = conn.prepareStatement(sql);
              ResultSet rs = stmt.executeQuery()) {
             
             while (rs.next()) {
                 double amount = rs.getDouble("total_amount");
                 totalRevenue += amount;
                 
                 String payment = String.format("Order ID: %d | Customer: %s | Amount: ₹%.2f | Date: %s",
                     rs.getInt("order_id"),
                     rs.getString("name"),
                     amount,
                     rs.getTimestamp("order_time").toString());
                 
                 payments.add(payment);
             }
         }
     } catch (SQLException e) {
         payments.add("Error loading payment records: " + e.getMessage());
     }
     
     paymentListView.setItems(payments);
     totalRevenueLabel.setText("Total Revenue: ₹" + String.format("%.2f", totalRevenue));
 }
 
 private VBox createAnalyticsPane() {
     VBox layout = new VBox(15);
     layout.setPadding(new Insets(20));
     
     Label titleLabel = new Label("Restaurant Analytics");
     titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
     
     // Stats grid
     GridPane statsGrid = new GridPane();
     statsGrid.setHgap(20);
     statsGrid.setVgap(10);
     
     Label totalCustomersLabel = new Label("Total Customers:");
     Label totalCustomersValue = new Label("0");
     
     Label totalOrdersLabel = new Label("Total Orders:");
     Label totalOrdersValue = new Label("0");
     
     Label avgRatingLabel = new Label("Average Rating:");
     Label avgRatingValue = new Label("0.0");
     
     Label mostPopularItemLabel = new Label("Most Popular Item:");
     Label mostPopularItemValue = new Label("None");
     
     statsGrid.add(totalCustomersLabel, 0, 0);
     statsGrid.add(totalCustomersValue, 1, 0);
     statsGrid.add(totalOrdersLabel, 0, 1);
     statsGrid.add(totalOrdersValue, 1, 1);
     statsGrid.add(avgRatingLabel, 0, 2);
     statsGrid.add(avgRatingValue, 1, 2);
     statsGrid.add(mostPopularItemLabel, 0, 3);
     statsGrid.add(mostPopularItemValue, 1, 3);
     
     // Style value labels
     totalCustomersValue.setStyle("-fx-font-weight: bold; -fx-text-fill: blue;");
     totalOrdersValue.setStyle("-fx-font-weight: bold; -fx-text-fill: green;");
     avgRatingValue.setStyle("-fx-font-weight: bold; -fx-text-fill: orange;");
     mostPopularItemValue.setStyle("-fx-font-weight: bold; -fx-text-fill: purple;");
     
     Button refreshStatsBtn = new Button("Refresh Statistics");
     refreshStatsBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
     
     refreshStatsBtn.setOnAction(e -> loadAnalytics(totalCustomersValue, totalOrdersValue, 
         avgRatingValue, mostPopularItemValue));
     
     // Load analytics initially
     loadAnalytics(totalCustomersValue, totalOrdersValue, avgRatingValue, mostPopularItemValue);
     
     layout.getChildren().addAll(titleLabel, statsGrid, refreshStatsBtn);
     
     return layout;
 }
 
 private void loadAnalytics(Label totalCustomersValue, Label totalOrdersValue, 
                           Label avgRatingValue, Label mostPopularItemValue) {
     try (Connection conn = DatabaseConnection.getConnection()) {
         // Total customers
         String customerSql = "SELECT COUNT(*) FROM customers";
         try (PreparedStatement stmt = conn.prepareStatement(customerSql);
              ResultSet rs = stmt.executeQuery()) {
             if (rs.next()) {
                 totalCustomersValue.setText(String.valueOf(rs.getInt(1)));
             }
         }
         
         // Total orders
         String orderSql = "SELECT COUNT(*) FROM orders";
         try (PreparedStatement stmt = conn.prepareStatement(orderSql);
              ResultSet rs = stmt.executeQuery()) {
             if (rs.next()) {
                 totalOrdersValue.setText(String.valueOf(rs.getInt(1)));
             }
         }
         
         // Average rating
         String ratingSql = "SELECT AVG(rating) FROM feedback";
         try (PreparedStatement stmt = conn.prepareStatement(ratingSql);
              ResultSet rs = stmt.executeQuery()) {
             if (rs.next()) {
                 double avgRating = rs.getDouble(1);
                 avgRatingValue.setText(String.format("%.1f", avgRating));
             }
         }
         
         // Most popular item
         String popularSql = """
             SELECT mi.name, SUM(oi.quantity) as total_qty
             FROM menu_items mi
             JOIN order_items oi ON mi.id = oi.menu_item_id
             GROUP BY mi.name
             ORDER BY total_qty DESC
             FETCH FIRST 1 ROWS ONLY
             """;
         try (PreparedStatement stmt = conn.prepareStatement(popularSql);
              ResultSet rs = stmt.executeQuery()) {
             if (rs.next()) {
                 mostPopularItemValue.setText(rs.getString("name") + " (" + rs.getInt("total_qty") + " orders)");
             }
         }
         
     } catch (SQLException e) {
         showAlert("Error", "Failed to load analytics: " + e.getMessage());
     }
 }
 
 private void showAlert(String title, String message) {
     Alert alert = new Alert(Alert.AlertType.INFORMATION);
     alert.setTitle(title);
     alert.setHeaderText(null);
     alert.setContentText(message);
     alert.showAndWait();
 }
 

 public static void main(String[] args) {
     launch(args);
 }
}
