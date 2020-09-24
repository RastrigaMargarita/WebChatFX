package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class MyServer {
    private final int PORT = 8184;
    private List<ClientHandler> clients;
    private String[] names;
    private SQLHandler sqlHandler;

    public SQLHandler getSqlHandler() {
        return sqlHandler;
    }

    public MyServer() {
        sqlHandler = new SQLHandler();
        try (ServerSocket server = new ServerSocket(PORT)) {
            clients = new ArrayList<>();
            while (true) {
                System.out.println("Сервер запущен и ожидает клиентов");
                Socket socket = server.accept();
                System.out.println("Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            System.out.println("Ошибка в работе сервера");
        } finally {
            sqlHandler.disconnectSQL();
        }
    }

    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler o : clients) {
            if (o.getName().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    public synchronized void broadcastMsg(String msg) {
        if (msg.contains("/w")) {
            names = msg.split(" ", 4);

            if (names[1].equals("/w")) {

                for (ClientHandler o : clients) {
                    if (names[0].replace(":", "").equals(o.getName()) ||
                            names[2].equals(o.getName())) {
                        o.sendMsg(msg.replace("/w", "ЛИЧНО"));
                    }
                }
                return;
            }

        }

        for (ClientHandler o : clients) {
            o.sendMsg(msg);
        }
    }

    private void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clientlist ");
        for (ClientHandler c : clients) {
            sb.append(c.getName()).append(" ");
        }

        String msg = sb.toString();
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    public synchronized void unsubscribe(ClientHandler o) {
        clients.remove(o);
        broadcastClientList();
    }

    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);
        broadcastClientList();
    }

}

