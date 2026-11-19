package com.hoteluq.view;

import com.hoteluq.controller.HotelController;
import com.hoteluq.model.Reservation;
import com.hoteluq.model.User;
import com.hoteluq.model.Wallet;
import com.hoteluq.model.enums.Role;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.time.LocalDate;

public class HotelView extends Application {

    private final HotelController controller = new HotelController();
    private Stage ventanaPrincipal;

    @Override
    public void start(Stage stage) {
        this.ventanaPrincipal = stage;
        mostrarLogin();
    }

    private void mostrarLogin() {

        Label titulo = new Label("HotelUQ - Inicio de sesión");
        titulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        TextField correo = new TextField();
        correo.setPromptText("Correo electrónico");

        PasswordField contrasena = new PasswordField();
        contrasena.setPromptText("Contraseña");

        Button ingresar = new Button("Iniciar sesión");

        ingresar.setOnAction(e -> {
            try {
                controller.login(
                        correo.getText(),
                        contrasena.getText().toCharArray()
                );

                mostrarSistema();

            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        });

        VBox root = new VBox(
                15,
                titulo,
                new Label("Correo"),
                correo,
                new Label("Contraseña"),
                contrasena,
                ingresar
        );

        root.setPadding(new Insets(30));

        ventanaPrincipal.setTitle("HotelUQ - Inicio de sesión");
        ventanaPrincipal.setScene(new Scene(root, 450, 350));
        ventanaPrincipal.show();
    }

    private void mostrarSistema() {

        User usuario = controller.getUsuarioActual();

        if (usuario == null) {
            mostrarLogin();
            return;
        }

        boolean esAdmin = usuario.getRole() == Role.ADMIN;
        Wallet wallet = usuario.getWallet();

        Label titulo = new Label("HotelUQ | Sistema Integral de Gestión Hotelera");
        titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");

        Label bienvenida = new Label(
                "Bienvenido: " + usuario.getName()
                        + " | Rol: " + usuario.getRole()
        );

        Button cerrarSesion = new Button("Cerrar sesión");

        cerrarSesion.setOnAction(e -> {
            controller.cerrarSesion();
            mostrarLogin();
        });

        ComboBox<String> habitaciones = new ComboBox<>();

        controller.rooms().forEach(r ->
                habitaciones.getItems().add(
                        r.getNumber() + " - "
                                + r.getType() + " - $"
                                + r.getPricePerNight() + " / noche"
                )
        );

        habitaciones.getSelectionModel().selectFirst();

        DatePicker entrada = new DatePicker(LocalDate.now().plusDays(1));
        DatePicker salida = new DatePicker(LocalDate.now().plusDays(3));

        TextArea output = new TextArea();
        output.setEditable(false);
        output.setPrefHeight(230);

        // RESERVAS
        Button reservar = new Button("Reservar y pagar");

        reservar.setOnAction(e -> {
            try {
                if (esAdmin) {
                    throw new SecurityException(
                            "Esta operación está disponible para clientes"
                    );
                }

                if (usuario != controller.client()) {
                    throw new SecurityException(
                            "Las reservas de demostración solo están "
                                    + "habilitadas para el cliente demo"
                    );
                }

                if (habitaciones.getValue() == null) {
                    throw new IllegalArgumentException(
                            "Selecciona una habitación"
                    );
                }

                String numero = habitaciones.getValue().split(" - ")[0];

                Reservation reserva = controller.createDemoReservation(
                        numero,
                        entrada.getValue(),
                        salida.getValue()
                );

                output.appendText(
                        "Reserva confirmada: " + reserva.getId()
                                + "\nHabitación: "
                                + reserva.getRoom().getNumber()
                                + "\nTotal: $" + reserva.getTotal()
                                + "\nEstado: " + reserva.getStatus()
                                + "\nSaldo restante: $"
                                + wallet.getBalance() + "\n\n"
                );

            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        });

        reservar.setDisable(esAdmin);

        Button consultarReservas = new Button("Consultar reservas");

        consultarReservas.setOnAction(e -> {
            controller.reservations().stream()
                    .filter(r -> esAdmin
                            || r.getCustomer() == usuario)
                    .forEach(r ->
                            output.appendText(
                                    r.getId() + " | "
                                            + r.getCustomer().getName()
                                            + " | "
                                            + r.getStatus() + "\n"
                            )
                    );
        });

        // TARJETA VIRTUAL
        Label tituloTarjeta = new Label("Tarjeta virtual");
        tituloTarjeta.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold;"
        );

        Button consultarSaldo = new Button("Consultar saldo");

        consultarSaldo.setOnAction(e ->
                output.appendText(
                        "Saldo disponible: $"
                                + wallet.getBalance()
                                + " COP\n\n"
                )
        );

        Button movimientos = new Button("Ver movimientos");

        movimientos.setOnAction(e -> {
            output.appendText("MOVIMIENTOS DE TARJETA\n");

            if (wallet.getMovements().isEmpty()) {
                output.appendText(
                        "No hay movimientos registrados.\n"
                );
            }

            wallet.getMovements().forEach(m ->
                    output.appendText(
                            m.date() + " | "
                                    + m.type() + " | $"
                                    + m.amount() + " | "
                                    + m.detail() + "\n"
                    )
            );

            output.appendText("\n");
        });

        // OPERACIONES ADMINISTRATIVAS
        Label tituloAdmin = new Label("Administración de tarjetas");
        tituloAdmin.setStyle(
                "-fx-font-size: 16px; -fx-font-weight: bold;"
        );

        TextField monto = new TextField();
        monto.setPromptText("Monto de recarga");
        monto.setPrefWidth(160);

        Button recargar = new Button("Recargar tarjeta del cliente");

        recargar.setOnAction(e -> {
            try {
                double valor = Double.parseDouble(
                        monto.getText().trim()
                );

                controller.recargarTarjeta(
                        usuario,
                        controller.client(),
                        valor
                );

                output.appendText(
                        "Recarga realizada: $" + valor
                                + "\nCliente: "
                                + controller.client().getName()
                                + "\nNuevo saldo: $"
                                + controller.client()
                                .getWallet().getBalance()
                                + "\n\n"
                );

                monto.clear();

            } catch (NumberFormatException ex) {
                mostrarError("Ingresa un monto numérico válido");
            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        });

        Button bloquear = new Button("Bloquear tarjeta");
        Button activar = new Button("Activar tarjeta");

        bloquear.setOnAction(e -> {
            try {
                if (controller.getUsuarioActual() != usuario
                        || !esAdmin) {
                    throw new SecurityException(
                            "Operación exclusiva del administrador"
                    );
                }

                controller.bloquearTarjeta(controller.client());
                output.appendText(
                        "Tarjeta del cliente bloqueada.\n\n"
                );

            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        });

        activar.setOnAction(e -> {
            try {
                if (controller.getUsuarioActual() != usuario
                        || !esAdmin) {
                    throw new SecurityException(
                            "Operación exclusiva del administrador"
                    );
                }

                controller.activarTarjeta(controller.client());
                output.appendText(
                        "Tarjeta del cliente activada.\n\n"
                );

            } catch (Exception ex) {
                mostrarError(ex.getMessage());
            }
        });

        VBox root = new VBox(
                12,
                titulo,
                bienvenida,
                cerrarSesion,
                new Separator(),
                new Label("Habitación"),
                habitaciones,
                new Label("Fecha de entrada"),
                entrada,
                new Label("Fecha de salida"),
                salida,
                new HBox(10, reservar, consultarReservas),
                new Separator(),
                tituloTarjeta,
                new HBox(10, consultarSaldo, movimientos),
                new Separator()
        );

        if (esAdmin) {
            root.getChildren().addAll(
                    tituloAdmin,
                    new HBox(10, monto, recargar),
                    new HBox(10, bloquear, activar),
                    new Separator()
            );
        }

        root.getChildren().addAll(
                new Label("Actividad del sistema"),
                output
        );

        root.setPadding(new Insets(20));

        ScrollPane scroll = new ScrollPane(root);
        scroll.setFitToWidth(true);

        ventanaPrincipal.setTitle(
                "HotelUQ - " + usuario.getName()
        );

        ventanaPrincipal.setScene(
                new Scene(scroll, 850, 700)
        );

        ventanaPrincipal.show();
    }

    private void mostrarError(String mensaje) {
        new Alert(
                Alert.AlertType.ERROR,
                mensaje == null ? "Error desconocido" : mensaje
        ).showAndWait();
    }
}