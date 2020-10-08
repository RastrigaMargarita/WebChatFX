package Server;

import java.sql.*;

public class SQLHandler {
    private static Connection connection;
    private static PreparedStatement psInsert;
    private static PreparedStatement psSelectUse;
    private static PreparedStatement psSelectNick;
    private static PreparedStatement psUpdateNick;
    private static PreparedStatement psInsertLog;

    public SQLHandler() {
        try {
            connectSQL();
            System.out.println("SQL база подключена!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            disconnectSQL();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            disconnectSQL();
        } finally {

        }
    }

    private void connectSQL() throws ClassNotFoundException, SQLException {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:chatusers.db");

        psInsert = connection.prepareStatement("INSERT INTO members (login, password, nickname) VALUES (?, ?, ?)");
        psSelectUse = connection.prepareStatement("SELECT * FROM members WHERE login = ? or nickname = ?");
        psSelectNick = connection.prepareStatement("SELECT * FROM members WHERE login=? and password  =?");
        psUpdateNick = connection.prepareStatement("UPDATE members SET nickname = ? WHERE ID = ?");
        psInsertLog = connection.prepareStatement("INSERT INTO log (user, event, datetime, text) VALUES (?, ?, ?, ?)");
    }

    public void disconnectSQL() {
        System.out.println("SQL база данных отключается...");
        try {
            psInsert.close();
            psSelectUse.close();
            psSelectNick.close();
            psUpdateNick.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean registration(String s, String s1, String s2) {
        try {

            psSelectUse.setString(1, s);
            psSelectUse.setString(2, s2);
            ResultSet rs = psSelectUse.executeQuery();
            if (rs.next()) {
                return false;
            } else {

                psInsert.setString(1, s);
                psInsert.setString(2, s1);
                psInsert.setString(3, s2);
                psInsert.executeUpdate();

                return true;
            }
        } catch (SQLException throwables) {
            System.out.println("Ошибка при попытке регистрации" + throwables.getMessage());
            return false;
        }

    }

    public boolean replaceNick(String s, String s1, String s2) {
        try {
            psSelectNick.setString(1, s);
            psSelectNick.setString(2, s1);

            ResultSet rs = psSelectNick.executeQuery();
            if (rs.next()) {
                Integer idrec = rs.getInt("ID");
                psUpdateNick.setString(1, s2);
                psUpdateNick.setInt(2, idrec);
                psInsert.executeUpdate();

                return true;
            } else {
                return registration(s, s1, s2);
            }
        } catch (SQLException throwables) {
            System.out.println("Ошибка при попытке смены ника " + throwables.getMessage());
            return false;
        }
    }

    public void writelog(String name, String event, String date, String s) {
        try {
            psInsertLog.setString(1, name);
            psInsertLog.setString(2, event);
            psInsertLog.setString(3, date);
            psInsertLog.setString(4, s);
            psInsertLog.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public String getNickByLoginPass(String part, String part1) {
        try {
            psSelectNick.setString(1, part);
            psSelectNick.setString(2, part1);

            ResultSet rs = psSelectNick.executeQuery();
            if (rs.next()) {
                return rs.getString("nickname");
            } else {
                return null;
            }
        } catch (SQLException throwables) {
            return null;
        }
    }

}