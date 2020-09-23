package Client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8184;
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
    @FXML
    public ListView<String> clientList;

    private Socket socket;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private boolean authenticated;
    private String nickname;
    private final String TITLE = "Simple chat";
    private Stage stage;
    private Stage regStage;
    private RegController regController;

    private void setAuthorized(boolean authenticated) {
        this.authenticated = authenticated;
        LoginBox.setVisible(!authenticated);
        LoginBox.setManaged(!authenticated);
        TextEnterBox.setVisible(authenticated);
        TextEnterBox.setManaged(authenticated);
        clientList.setVisible(authenticated);
        clientList.setManaged(authenticated);

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

                        if (strFromServer.startsWith("/regok")) {
                            regController.addMsgToTextArea("Регистрация прошла успешно");
                            regController.closeWindow();

                        } else if (strFromServer.startsWith("/regno")) {
                            regController.addMsgToTextArea("Регистрация не получилась \n возможно логин или ник заняты");
                            regController.closeWindow();
                        } else {
                            ChatArea.appendText(strFromServer + "\n");
                        }
                    }
                    while (true) {
                        String strFromServer = in.readUTF();

                        if (strFromServer.equalsIgnoreCase("/end")) {
                            break;
                        } else if
                        (strFromServer.startsWith("/clientlist ")) {
                            String[] token = strFromServer.split("\\s+");
                            Platform.runLater(() -> {
                                clientList.getItems().clear();
                                for (int i = 1; i < token.length; i++) {
                                    clientList.getItems().add(token[i]);
                                }
                            });
                        } else {
                            ChatArea.appendText(strFromServer);
                            ChatArea.appendText("\n");
                        }
                    }
                } catch (SocketTimeoutException e) {
                    System.out.println("Время ожидания вышло");
                    regController.closeWindow();
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

    public void tryToReg(String login, String password, String nickname) {
        String msg = String.format("/reg %s %s %s", login, password, nickname);

        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(msg);
    }

    public void replaceNick(String login, String password, String nickname) {
        String msg = String.format("/ren %s %s %s", login, password, nickname);

        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println(msg);
    }

    public void registration(ActionEvent actionEvent) {
        createRegWindow();
        regStage.show();
        if (socket == null || socket.isClosed()) {
            connect();
        }
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/registr.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Регистрация нового пользователя");
            regStage.setScene(new Scene(root, 400, 250));

            regController = fxmlLoader.getController();
            regController.setController(this);

            regStage.initModality(Modality.APPLICATION_MODAL);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setAuthorized(false);
        //createRegWindow();
        Platform.runLater(() -> {
            stage = (Stage) TextEnterBox.getScene().getWindow();
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent event) {
                    System.out.println("bye");
                    if (socket != null && !socket.isClosed()) {
                        try {
                            out.writeUTF("/end");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            });
        });
    }

    public void clickClientList(javafx.scene.input.MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        TextEnterField.setText("/w " + receiver + " ");
    }
}
