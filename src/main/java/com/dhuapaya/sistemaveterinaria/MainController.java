package com.dhuapaya.sistemaveterinaria;

import com.dhuapaya.sistemaveterinaria.dao.AppointmentDao;
import com.dhuapaya.sistemaveterinaria.dao.ClientDao;
import com.dhuapaya.sistemaveterinaria.dao.PetDao;
import com.dhuapaya.sistemaveterinaria.model.Appointment;
import com.dhuapaya.sistemaveterinaria.model.Client;
import com.dhuapaya.sistemaveterinaria.model.Pet;
import com.dhuapaya.sistemaveterinaria.util.ErrorHandler;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainController {

    // Status
    @FXML
    private Label lblStatus;

    // CLIENTE
    @FXML
    private TextField c_id, c_name, c_lastName, c_phone, c_email, c_dni;
    @FXML
    private TableView<Client> tblClients;
    @FXML
    private TableColumn<Client, Integer> colCId;
    @FXML
    private TableColumn<Client, String> colCName, colCLastName, colCPhone, colCEmail, colCDni;

    // MASCOTA
    @FXML
    private TextField p_id, p_name;
    @FXML
    private DatePicker p_birthDate;
    @FXML
    private ComboBox<Client> p_clientCombo;
    @FXML
    private ComboBox<String> p_speciesCombo, p_breedCombo;
    @FXML
    private TableView<Pet> tblPets;
    @FXML
    private TableColumn<Pet, Integer> colPId;
    @FXML
    private TableColumn<Pet, String> colPClient, colPName, colPSpecies, colPBreed, colPBirth;

    // CITA
    @FXML
    private TextField a_id, a_reason, a_notes, a_time;
    @FXML
    private DatePicker a_date;
    @FXML
    private ComboBox<String> a_status;
    @FXML
    private ComboBox<Pet> a_petCombo;
    @FXML
    private TableView<Appointment> tblAppts;
    @FXML
    private TableColumn<Appointment, Integer> colAId, colAPet;
    @FXML
    private TableColumn<Appointment, String> colADT, colAReason, colANotes, colAStatus;

    private final ClientDao clientDao = new ClientDao();
    private final PetDao petDao = new PetDao();
    private final AppointmentDao apptDao = new AppointmentDao();

    private final Map<Integer, Client> clientsIndex = new HashMap<>();
    private final Map<Integer, Pet> petsIndex = new HashMap<>();

    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private boolean updatingPetsUI = false;

    // --- Helper: recargar combo de clientes ---
    private void refreshClientCombo(Integer keepSelectedClientId) {
        try {
            var clientes = clientDao.listAll();
            clientsIndex.clear();
            for (Client c : clientes) {
                if (c.getId() != null) clientsIndex.put(c.getId(), c);
            }
            var items = FXCollections.observableArrayList(clientes);
            p_clientCombo.setItems(items);
            if (keepSelectedClientId != null) {
                for (Client c : items) {
                    if (Objects.equals(c.getId(), keepSelectedClientId)) {
                        p_clientCombo.getSelectionModel().select(c);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            ErrorHandler.showError(lblStatus, e, "No se pudieron cargar clientes");
        }
    }

    @FXML
    public void initialize() {
        // Bind columns CLIENTES
        colCId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colCLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colCPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colCEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colCDni.setCellValueFactory(new PropertyValueFactory<>("dni"));

        // Bind columns MASCOTAS
        colPId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colPClient.setCellValueFactory(cell -> {
            Pet p = cell.getValue();
            Client c = clientsIndex.get(p.getClientId());
            String full = (c == null) ? String.valueOf(p.getClientId())
                    : (c.getName() + (c.getLastName() == null || c.getLastName().isBlank()
                    ? "" : " " + c.getLastName()));
            return new SimpleStringProperty(full);
        });
        colPName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colPSpecies.setCellValueFactory(new PropertyValueFactory<>("species"));
        colPBreed.setCellValueFactory(new PropertyValueFactory<>("breed"));
        colPBirth.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getBirthdate() == null
                        ? "" : cell.getValue().getBirthdate().toString()));

        // Bind columns CITAS
        colAId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colAPet.setCellValueFactory(new PropertyValueFactory<>("petId"));
        colADT.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        colAReason.setCellValueFactory(new PropertyValueFactory<>("reason"));
        colANotes.setCellValueFactory(new PropertyValueFactory<>("notes"));
        colAStatus.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Selección CLIENTE → formulario
        tblClients.getSelectionModel().selectedItemProperty().addListener((obs, old, v) -> {
            if (v != null) {
                c_id.setText(String.valueOf(v.getId()));
                c_name.setText(v.getName());
                c_lastName.setText(v.getLastName());
                c_phone.setText(v.getPhone());
                c_email.setText(v.getEmail());
                c_dni.setText(v.getDni());
            }
        });

        // Selección MASCOTA → formulario
        tblPets.getSelectionModel().selectedItemProperty().addListener((obs, old, v) -> {
            if (updatingPetsUI || v == null) return;
            p_id.setText(v.getId() == null ? "" : String.valueOf(v.getId()));

            // Cliente: seleccionar en el combo por id
            p_clientCombo.getSelectionModel().clearSelection();
            for (Client c : p_clientCombo.getItems()) {
                if (c.getId() != null && c.getId().equals(v.getClientId())) {
                    p_clientCombo.getSelectionModel().select(c);
                    break;
                }
            }

            p_name.setText(v.getName() == null ? "" : v.getName());

            // Especie y razas
            if (v.getSpecies() != null) {
                p_speciesCombo.getSelectionModel().select(v.getSpecies());
            } else {
                p_speciesCombo.getSelectionModel().clearSelection();
                p_breedCombo.getItems().clear();
            }
            if (v.getBreed() != null) {
                p_breedCombo.getSelectionModel().select(v.getBreed());
            }

            // Fecha
            p_birthDate.setValue(v.getBirthdate() == null ? null : v.getBirthdate().toLocalDate());
        });

        // Selección CITA → formulario
        tblAppts.getSelectionModel().selectedItemProperty().addListener((obs, old, v) -> {
            if (v != null) {
                a_id.setText(String.valueOf(v.getId()));
                a_reason.setText(v.getReason());
                a_notes.setText(v.getNotes());

                // Fecha y hora
                if (v.getDateTime() != null) {
                    LocalDateTime dt = v.getDateTime();
                    a_date.setValue(dt.toLocalDate());
                    a_time.setText(dt.toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                } else {
                    a_date.setValue(null);
                    a_time.clear();
                }

                // Estados (si aún no están)
                if (a_status.getItems().isEmpty()) {
                    a_status.setItems(FXCollections.observableArrayList("PENDIENTE", "CONFIRMADO", "CANCELADO"));
                }
                a_status.setValue(v.getStatus());

                // Seleccionar mascota en combo
                Pet pet = petsIndex.get(v.getPetId());
                if (pet != null) {
                    a_petCombo.getSelectionModel().select(pet);
                } else {
                    a_petCombo.getSelectionModel().clearSelection();
                }
            }
        });

        // --- Mascotas: combos ---
        refreshClientCombo(null);

        // Mostrar "Nombre Apellido" en el combo de clientes
        p_clientCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Client c) {
                if (c == null) return "";
                var ln = c.getLastName() == null ? "" : (" " + c.getLastName());
                return c.getName() + ln;
            }

            @Override
            public Client fromString(String s) {
                return null;
            }
        });

        // Especies fijas
        p_speciesCombo.setItems(FXCollections.observableArrayList("Perro", "Gato", "Loro"));

        // Razas dependientes de especie
        p_speciesCombo.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> {
            if (b == null) {
                p_breedCombo.getItems().clear();
                return;
            }
            switch (b) {
                case "Perro" -> p_breedCombo.setItems(FXCollections.observableArrayList(
                        "Mestizo", "Labrador", "Poodle", "Bulldog", "Husky"));
                case "Gato" -> p_breedCombo.setItems(FXCollections.observableArrayList(
                        "Mestizo", "Siames", "Persa", "Bengalí"));
                case "Loro" -> p_breedCombo.setItems(FXCollections.observableArrayList(
                        "Mestizo", "Amazonas", "Cacatúa", "Guacamayo"));
                default -> p_breedCombo.getItems().clear();
            }
            if (!p_breedCombo.getItems().isEmpty()) {
                p_breedCombo.getSelectionModel().selectFirst();
            }
        });

        // Valores por defecto
        if (!p_speciesCombo.getItems().isEmpty()) {
            p_speciesCombo.getSelectionModel().selectFirst();
        }

        // Estados de citas
        a_status.setItems(FXCollections.observableArrayList("PENDIENTE", "CONFIRMADO", "CANCELADO"));

        // Converter del combo de mascotas para citas
        a_petCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pet p) {
                if (p == null) return "";
                Client c = clientsIndex.get(p.getClientId());
                String owner = (c == null)
                        ? "Dueño ID=" + p.getClientId()
                        : c.getName() + (c.getLastName() == null || c.getLastName().isBlank()
                        ? "" : " " + c.getLastName());
                return p.getName() + " (" + owner + ")";
            }

            @Override
            public Pet fromString(String s) {
                return null;
            }
        });

        // Cargar datos
        onClientReload();
        onPetReload();   // esto también llena el combo de mascotas
        onApptReload();
    }

    // CLIENTE
    @FXML
    public void onClientNew() {
        c_id.clear();
        c_name.clear();
        c_lastName.clear();
        c_phone.clear();
        c_email.clear();
        c_dni.clear();
    }

    @FXML
    public void onClientCreate() {
        try {
            // Validaciones
            if (!c_name.getText().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
                throw new IllegalArgumentException("El nombre solo debe contener letras y espacios.");
            }
            if (!c_lastName.getText().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
                throw new IllegalArgumentException("El apellido solo debe contener letras y espacios.");
            }
            if (!c_phone.getText().matches("\\d{9}")) {
                throw new IllegalArgumentException("El teléfono debe contener exactamente 9 dígitos. " +
                        "NO debe contener letras, símbolos ni espacios. Ejemplo: 987654321");
            }
            if (!c_email.getText().matches("^[\\w.-]+@[a-zA-Z\\d.-]+\\.[a-zA-Z]{2,}$")) {
                throw new IllegalArgumentException("El correo electrónico no es válido.");
            }
            if (!c_dni.getText().matches("\\d{8}")) {
                throw new IllegalArgumentException("El DNI debe contener exactamente 8 dígitos.");
            }
            if (!clientDao.isDniUnique(c_dni.getText())) {
                throw new IllegalArgumentException("El DNI ya está registrado.");
            }

            Client c = new Client(null, c_name.getText(), c_lastName.getText(),
                    c_phone.getText(), c_email.getText(), c_dni.getText());

            clientDao.create(c);

            Integer id = c.getId();
            if (id != null) {
                c_id.setText(String.valueOf(id));
                lblStatus.setText("Cliente creado (ID=" + id + ")");
            } else {
                lblStatus.setText("Cliente creado, pero no se pudo obtener el ID generado.");
            }

            onClientReload();
            if (id != null) {
                refreshClientCombo(id);
                tblClients.getItems().stream()
                        .filter(it -> it.getId() == id)
                        .findFirst()
                        .ifPresent(it -> {
                            tblClients.getSelectionModel().select(it);
                            tblClients.scrollTo(it);
                        });
            }

        } catch (IllegalArgumentException e) {
            ErrorHandler.showValidationError(lblStatus, e);
        } catch (Exception e) {
            ErrorHandler.showError(lblStatus, e, "Error creando cliente");
        }
    }

    @FXML
    public void onClientUpdate() {
        try {
            Client c = new Client(Integer.parseInt(c_id.getText()), c_name.getText(),
                    c_lastName.getText(), c_phone.getText(), c_email.getText(), c_dni.getText());
            clientDao.update(c);
            lblStatus.setText("Cliente actualizado");
            onClientReload();
            refreshClientCombo(Integer.parseInt(c_id.getText()));
        } catch (Exception e) {
            ErrorHandler.showError(lblStatus, e, "Error actualizando cliente");
        }
    }

    @FXML
    public void onClientDelete() {
        try {
            clientDao.delete(Integer.parseInt(c_id.getText()));
            lblStatus.setText("Cliente eliminado");
            onClientReload();
            refreshClientCombo(null);
            onClientNew();
        } catch (Exception e) {
            ErrorHandler.showError(lblStatus, e, "Error eliminando cliente");
        }
    }

    @FXML
    public void onClientReload() {
        try {
            var clients = clientDao.listAll();
            tblClients.getItems().setAll(clients);

            // reconstruir índice de clientes (por si cambió algo)
            clientsIndex.clear();
            for (Client c : clients) {
                if (c.getId() != null) clientsIndex.put(c.getId(), c);
            }

            refreshClientCombo(null);
        } catch (Exception e) {
            ErrorHandler.showError(lblStatus, e, "Error cargando clientes");
        }
    }


    public void onExportClientsCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar reporte de Clientes");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                new ClientDao().exportToCsv(file.getAbsolutePath());
                lblStatus.setText("Reporte de clientes exportado correctamente.");
            } catch (Exception e) {
                lblStatus.setText("Error al exportar el reporte de clientes.");
                e.printStackTrace();
            }
        }
    }

    // MASCOTA
    @FXML
    public void onPetNew() {
        p_id.clear();
        p_name.clear();
        p_birthDate.setValue(null);
        p_clientCombo.getSelectionModel().clearSelection();
        p_speciesCombo.getSelectionModel().clearSelection();
        p_breedCombo.getItems().clear();
    }

    @FXML
    public void onPetCreate() {
        try {
            // Validar nombre
            if (!p_name.getText().matches("[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+")) {
                throw new IllegalArgumentException("El nombre solo debe contener letras y espacios.");
            }

            Client cli = p_clientCombo.getSelectionModel().getSelectedItem();
            if (cli == null) throw new IllegalArgumentException("Selecciona un cliente.");

            String species = p_speciesCombo.getSelectionModel().getSelectedItem();
            String breed = p_breedCombo.getSelectionModel().getSelectedItem();

            LocalDate birthLocal = p_birthDate.getValue();
            if (birthLocal != null && birthLocal.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("La fecha de nacimiento no puede ser posterior a hoy.");
            }
            java.sql.Date birth = (birthLocal == null) ? null : java.sql.Date.valueOf(birthLocal);

            Pet p = new Pet(
                    null,
                    cli.getId(),
                    p_name.getText(),
                    species,
                    breed,
                    birth
            );

            petDao.create(p);

            Integer id = p.getId();
            if (id != null) {
                p_id.setText(String.valueOf(id));
                lblStatus.setText("Mascota creada (ID=" + id + ")");
            } else {
                lblStatus.setText("Mascota creada, pero no se pudo obtener el ID generado.");
            }

            onPetReload();
        } catch (IllegalArgumentException e) {
            ErrorHandler.showValidationError(lblStatus, e);
        } catch (Exception e) {
            ErrorHandler.showError(lblStatus, e, "Error creando mascota");
        }
    }

    @FXML
    public void onPetUpdate() {
        try {
            if (p_id.getText().isBlank()) throw new IllegalArgumentException("ID vacío.");
            Client cli = p_clientCombo.getSelectionModel().getSelectedItem();
            if (cli == null) throw new IllegalArgumentException("Selecciona un cliente.");
            String species = p_speciesCombo.getSelectionModel().getSelectedItem();
            String breed = p_breedCombo.getSelectionModel().getSelectedItem();

            LocalDate birthLocal = p_birthDate.getValue();
            if (birthLocal != null && birthLocal.isAfter(LocalDate.now())) {
                throw new IllegalArgumentException("La fecha de nacimiento no puede ser posterior a hoy.");
            }
            java.sql.Date birth = (birthLocal == null) ? null : java.sql.Date.valueOf(birthLocal);

            Pet p = new Pet(
                    Integer.parseInt(p_id.getText()),
                    cli.getId(),
                    p_name.getText(),
                    species,
                    breed,
                    birth
            );
            petDao.update(p);
            lblStatus.setText("Mascota actualizada");
            onPetReload();
        } catch (IllegalArgumentException e) {
            ErrorHandler.showValidationError(lblStatus, e);
        } catch (Exception e) {
            ErrorHandler.showError(lblStatus, e, "Error actualizando mascota");
        }
    }

    @FXML
    public void onPetDelete() {
        try {
            petDao.delete(Integer.parseInt(p_id.getText()));
            lblStatus.setText("Mascota eliminada");
            onPetReload();
            onPetNew();
        } catch (Exception e) {
            ErrorHandler.showError(lblStatus, e, "Error eliminando mascota");
        }
    }

    @FXML
    public void onPetReload() {
        try {
            updatingPetsUI = true;

            var pets = petDao.listAll();
            tblPets.getItems().setAll(pets);

            // reconstruir índice + llenar combo para citas
            petsIndex.clear();
            for (Pet p : pets) {
                if (p.getId() != null) petsIndex.put(p.getId(), p);
            }
            a_petCombo.setItems(FXCollections.observableArrayList(pets));

        } catch (Exception e) {
            ErrorHandler.showError(lblStatus, e, "Error cargando mascotas");
        } finally {
            updatingPetsUI = false;
        }
    }

    public void onExportPetsCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar reporte de Mascotas");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                new PetDao().exportToCsv(file.getAbsolutePath());
                lblStatus.setText("Reporte de mascotas exportado correctamente.");
            } catch (Exception e) {
                lblStatus.setText("Error al exportar el reporte de mascotas.");
                e.printStackTrace();
            }
        }
    }

    // CITA
    @FXML
    public void onApptNew() {
        a_id.clear();
        a_reason.clear();
        a_notes.clear();
        a_date.setValue(null);
        a_time.clear();
        a_status.getSelectionModel().clearSelection();
        a_petCombo.getSelectionModel().clearSelection();
    }

    @FXML
    public void onApptCreate() {
        try {
            // Mascota
            Pet pet = a_petCombo.getSelectionModel().getSelectedItem();
            if (pet == null || pet.getId() == null) {
                throw new IllegalArgumentException("Selecciona una mascota.");
            }

            // Fecha
            if (a_date.getValue() == null) {
                throw new IllegalArgumentException("Selecciona una fecha para la cita.");
            }

            // Hora
            LocalTime time = LocalTime.parse(a_time.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime dateTime = LocalDateTime.of(a_date.getValue(), time);

            // Validar futura
            if (!dateTime.isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("La fecha y hora deben ser iguales o posteriores a la actual.");
            }

            String status = a_status.getValue();
            if (status == null || status.isBlank()) {
                throw new IllegalArgumentException("Selecciona un estado para la cita.");
            }

            Appointment appointment = new Appointment(
                    null,
                    pet.getId(),
                    dateTime,
                    a_reason.getText(),
                    a_notes.getText(),
                    status
            );

            apptDao.create(appointment);

            Integer id = appointment.getId();
            if (id != null) {
                a_id.setText(String.valueOf(id));
                lblStatus.setText("Cita creada (ID=" + id + ")");
            } else {
                lblStatus.setText("Cita creada, pero no se pudo obtener el ID generado.");
            }

            onApptReload();
        } catch (IllegalArgumentException e) {
            ErrorHandler.showValidationError(lblStatus, e);
        } catch (Exception e) {
            ErrorHandler.showError(lblStatus, e, "Error creando cita");
        }
    }

    @FXML
    public void onApptUpdate() {
        try {
            if (a_id.getText().isBlank()) {
                throw new IllegalArgumentException("ID de cita vacío.");
            }

            Pet pet = a_petCombo.getSelectionModel().getSelectedItem();
            if (pet == null || pet.getId() == null) {
                throw new IllegalArgumentException("Selecciona una mascota.");
            }

            if (a_date.getValue() == null) {
                throw new IllegalArgumentException("Selecciona una fecha para la cita.");
            }

            LocalTime time = LocalTime.parse(a_time.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalDateTime dateTime = LocalDateTime.of(a_date.getValue(), time);

            if (!dateTime.isAfter(LocalDateTime.now())) {
                throw new IllegalArgumentException("La fecha y hora deben ser iguales o posteriores a la actual.");
            }

            String status = a_status.getValue();
            if (status == null || status.isBlank()) {
                throw new IllegalArgumentException("Selecciona un estado para la cita.");
            }

            Appointment appointment = new Appointment(
                    Integer.parseInt(a_id.getText()),
                    pet.getId(),
                    dateTime,
                    a_reason.getText(),
                    a_notes.getText(),
                    status
            );

            apptDao.update(appointment);
            lblStatus.setText("Cita actualizada exitosamente.");
            onApptReload();
        } catch (IllegalArgumentException e) {
            ErrorHandler.showValidationError(lblStatus, e);
        } catch (Exception e) {
            ErrorHandler.showError(lblStatus, e, "Error actualizando cita");
        }
    }

    @FXML
    public void onApptDelete() {
        try {
            apptDao.delete(Integer.parseInt(a_id.getText()));
            lblStatus.setText("Cita eliminada");
            onApptReload();
            onApptNew();
        } catch (Exception e) {
            ErrorHandler.showError(lblStatus, e, "Error eliminando cita");
        }
    }

    @FXML
    public void onApptReload() {
        try {
            tblAppts.getItems().setAll(apptDao.listAll());
        } catch (Exception e) {
            ErrorHandler.showError(lblStatus, e, "Error cargando citas");
        }
    }

    // 🔎 REPORTE: buscar citas por fecha y estado
    @FXML
    public void onApptSearch() {
        try {
            LocalDate date = a_date.getValue();
            LocalDateTime from = null;
            LocalDateTime to = null;

            if (date != null) {
                from = date.atStartOfDay();
                to = date.atTime(LocalTime.MAX);
            }

            String status = a_status.getValue();
            var results = apptDao.listByDateRangeAndStatus(from, to, status);

            tblAppts.getItems().setAll(results);

            if (results.isEmpty()) {
                lblStatus.setText("No se encontraron citas con los filtros seleccionados.");
            } else {
                lblStatus.setText("Se encontraron " + results.size() + " citas.");
            }

        } catch (Exception e) {
            ErrorHandler.showError(lblStatus, e, "Error consultando citas");
        }
    }

    @FXML
    public void onExportAppointmentsCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar reporte de Citas");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        File file = fileChooser.showSaveDialog(null);

        if (file != null) {
            try {
                apptDao.exportToCsv(file.getAbsolutePath());
                lblStatus.setText("Reporte de citas exportado correctamente.");
            } catch (Exception e) {
                lblStatus.setText("Error al exportar el reporte de citas.");
                e.printStackTrace();
            }
        }
    }
}