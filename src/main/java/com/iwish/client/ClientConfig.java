package com.iwish.client;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ClientConfig {

    private static final Properties props = new Properties();

    static {
        try (InputStream in = openConfigStream()) {
            if (in != null) props.load(in);
        } catch (IOException e) {
            System.out.println("[Config] Could not read client.properties, using defaults.");
        }
    }

    private static InputStream openConfigStream() throws IOException {
        java.io.File external = new java.io.File("client.properties");
        if (external.exists()) {
            return new FileInputStream(external);
        }
        return ClientConfig.class.getClassLoader().getResourceAsStream("client.properties");
    }

    public static String serverHost() { return props.getProperty("server.host", "localhost"); }
    public static int serverPort() { return Integer.parseInt(props.getProperty("server.port", "5000")); }
}
