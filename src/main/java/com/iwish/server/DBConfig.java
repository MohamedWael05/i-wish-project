package com.iwish.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DBConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = openConfigStream()) {
            if (in != null) {
                props.load(in);
            }
        } catch (IOException e) {
            System.out.println("[Config] Could not read config.properties, using defaults. " + e.getMessage());
        }
    }

    private static InputStream openConfigStream() throws IOException {
        java.io.File external = new java.io.File("config.properties");
        if (external.exists()) {
            return new FileInputStream(external);
        }
        return DBConfig.class.getClassLoader().getResourceAsStream("config.properties");
    }

    public static String dbHost() { return props.getProperty("db.host", "localhost"); }
    public static String dbPort() { return props.getProperty("db.port", "3306"); }
    public static String dbName() { return props.getProperty("db.name", "iwish_db"); }
    public static String dbUser() { return props.getProperty("db.user", "root"); }
    public static String dbPassword() { return props.getProperty("db.password", ""); }

    public static String jdbcUrl() {
        return "jdbc:mysql://" + dbHost() + ":" + dbPort() + "/" + dbName()
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    }

    public static int serverPort() {
        return Integer.parseInt(props.getProperty("server.port", "5000"));
    }
}
