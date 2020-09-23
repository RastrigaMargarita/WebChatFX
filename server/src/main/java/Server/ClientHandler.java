package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;

import static com.sun.org.apache.xalan.internal.lib.ExsltDatetime.date;

public class ClientHandler {
    private MyServer myServer;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private String name;

    public String getName() {
        return name;
    }

    public ClientHandler(MyServer myServer, Socket socket) {

        try {

            this.myServer = myServer;
            this.socket = socket;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());

            this.name = "";
            new Thread(() -> {
                try {
                    authentication();

                    System.out.println("Прошли аутентификацию");
                    readMessages();

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    System.out.println("Закрытие коннекта");
                    closeConnection();
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException("Проблемы при создании обработчика клиента");


        }
    }

    public void authentication() throws IOException {
        while (true) {
            socket.setSoTimeout(120000);
            String str = in.readUTF();
            socket.setSoTimeout(0);
            if (str.startsWith("/auth")) {
                String[] parts = str.split("\\s");
                if (parts.length < 3) {
                    continue;
                }
                String nick = myServer.getSqlHandler().getNickByLoginPass(parts[1], parts[2]);
                if (nick != null) {
                    if (!myServer.isNickBusy(nick)) {
                        sendMsg("/authok " + nick);
                        name = nick;
                        myServer.broadcastMsg(name + " зашел в чат");
                        myServer.getSqlHandler().writelog(name, "enter", date(), "");
                        myServer.subscribe(this);
                        return;
                    } else {
                        sendMsg("Учетная запись уже используется");
                    }
                } else {
                    sendMsg("Неверные логин/пароль");
                }
            }
            if (str.startsWith("/reg ")) {
                String[] token = str.split("\\s");
                if (token.length < 4) {
                    continue;
                }

                boolean b = myServer.getSqlHandler()
                        .registration(token[1], token[2], token[3]);
                if (b) {
                    sendMsg("/regok");
                } else {
                    sendMsg("/regno");
                }
            }
            if (str.startsWith("/ren ")) {
                String[] token = str.split("\\s");
                if (token.length < 4) {
                    continue;
                }

                boolean b = myServer.getSqlHandler()
                        .replaceNick(token[1], token[2], token[3]);
                if (b) {
                    sendMsg("/regok");
                } else {
                    sendMsg("/regno");
                }
            }
        }
    }

    public void readMessages() throws IOException {

        while (true) {
            String strFromClient = in.readUTF();

            if (strFromClient.equals("/end")) {
                out.writeUTF("/end");
                return;
            }
            myServer.broadcastMsg(name + ": " + strFromClient);
            myServer.getSqlHandler().writelog(name, "post", date(), strFromClient);
        }
    }

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        myServer.unsubscribe(this);
        myServer.broadcastMsg(name + " вышел из чата");
        myServer.getSqlHandler().writelog(name, "out", date(), "");
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Коннект закрыт");
    }


}
