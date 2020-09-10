package sample;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Controller {
    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8187;
    @FXML
    TextArea ChatArea;
    @FXML
    TextField LoginField;
    @FXML
    TextField PasswordField;
    @FXML
    HBox LoginBox;
    @FXML
    HBox TextEnterBox;
    @FXML
    TextField TextEnterField;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean authenticated;
    private String nickname;
    private final String TITLE = "Simple chat";

    private void setAuthorized(boolean authenticated) {
        this.authenticated = authenticated;
        LoginBox.setVisible(!authenticated);
        LoginBox.setManaged(!authenticated);
        TextEnterBox.setVisible(authenticated);
        TextEnterBox.setManaged(authenticated);

        if (!authenticated) {
            nickname = "";
        }
        setTitle(nickname);
    }

    private void setTitle(String nickname) {
        Platform.runLater(() -> {
            ((Stage) LoginField.getScene().getWindow()).setTitle(TITLE + ": " + nickname);
        });
    }

    public void onConnect(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }

        try {
            out.writeUTF(String.format("/auth %s %s", LoginField.getText().trim().toLowerCase(),
                    PasswordField.getText().trim()));
            PasswordField.clear();

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void connect() {
        try {
            Socket socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            setAuthorized(false);
            Thread t = new Thread(() -> {
                try {
                    while (true) {
                        String strFromServer = in.readUTF();
                        if (strFromServer.startsWith("/authok")) {
                            nickname = strFromServer.split(" ", 2)[1];
                            ChatArea.appendText("Добро пожаловать, " + nickname + "\n");
                            setAuthorized(true);
                            break;
                        }
                        ChatArea.appendText(strFromServer + "\n");
                    }
                    while (true) {
                        String strFromServer = in.readUTF();

                        if (strFromServer.equalsIgnoreCase("/end")) {
                            break;
                        }
                            ChatArea.appendText(strFromServer);
                            ChatArea.appendText("\n");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Мы отключились от сервера");
                    setAuthorized(false);
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            t.setDaemon(true);
            t.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(ActionEvent actionEvent) {

        try {
            out.writeUTF(TextEnterField.getText());
            TextEnterField.clear();
            TextEnterField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
