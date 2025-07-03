
module application {

    // Requires JavaFX modules for UI components and FXML loading
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    // Requires java.sql for JDBC database connectivity
    requires java.sql;

    // Opens packages containing FXML controllers to javafx.fxml module
    // This is crucial for FXML to inject UI elements and call controller methods.
    // Ensure this matches your actual controller package!
    opens application to javafx.fxml;
    exports application;

    
}

