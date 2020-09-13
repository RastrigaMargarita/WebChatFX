package sample.Client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.beans.Visibility;

public class RegController {
    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8187;
    private Controller controller;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField nickField;
    @FXML
    private TextArea textArea;

    public void tryToReg(ActionEvent actionEvent) {
        controller.tryToReg(loginField.getText().trim(),
                passwordField.getText().trim(),
                nickField.getText().trim());
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    public void addMsgToTextArea(String msg) {
        textArea.appendText(msg + "\n");
    }

    public void closeWindow() {
        try {
            Thread.sleep(1000);
            Platform.runLater(() -> {
                ((Stage) textArea.getScene().getWindow()).close();
            });

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
