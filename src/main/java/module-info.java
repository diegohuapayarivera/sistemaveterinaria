module com.dhuapaya.sistemaveterinaria {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.dhuapaya.sistemaveterinaria to javafx.fxml;
    exports com.dhuapaya.sistemaveterinaria;
    // 👇 Necesario para PropertyValueFactory (reflexión sobre getters de tus POJOs)
    opens com.dhuapaya.sistemaveterinaria.model to javafx.base;
}