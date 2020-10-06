package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.*;

public class MyServer {
    private final int PORT = 8187;
    private List<ClientHandler> clients;
    private String[] names;
    private SQLHandler sqlHandler;

    private static final Logger logger = Logger.getLogger(MyServer.class.getName());
    private static FileHandler fileHandler;

    {
        try {
            fileHandler = new FileHandler("log_%g.log",10 * 1024, 20, true);
            fileHandler.setLevel(Level.ALL);
            fileHandler.setFormatter(new SimpleFormatter());
            logger.addHandler(fileHandler);
            logger.setUseParentHandlers(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public SQLHandler getSqlHandler() {
        return sqlHandler;
    }

    public MyServer() {
        sqlHandler = new SQLHandler();
        try (ServerSocket server = new ServerSocket(PORT)) {
            clients = new ArrayList<>();
            while (true) {
                //System.out.println("Сервер запущен и ожидает клиентов");
                logger.log(Level.SEVERE,"Сервер запущен и ожидает клиентов");

                Socket socket = server.accept();
                //System.out.println("Клиент подключился");
                logger.log(Level.INFO,"Клиент подключился");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            //System.out.println("Ошибка в работе сервера");
            e.printStackTrace();
            logger.log(Level.WARNING,"Ошибка в работе сервера");
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
        logger.log(Level.INFO,"Клиент вышел из чата");
        broadcastClientList();
    }

    public synchronized void subscribe(ClientHandler o) {
        clients.add(o);
        logger.log(Level.INFO,"Клиент вошел в чат");
        broadcastClientList();
    }

}

