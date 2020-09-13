package sample.Server;

public interface AuthService {
    void start();
    String getNickByLoginPass(String login, String pass);
    boolean registration(String login, String password, String nickname);
    void stop();
}

