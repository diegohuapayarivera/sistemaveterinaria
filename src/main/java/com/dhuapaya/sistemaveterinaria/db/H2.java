package com.dhuapaya.sistemaveterinaria.db;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class H2 {
    private static final String PROPS = "/db.properties";

    public static Connection get() throws Exception {
        Properties p = new Properties();
        try (InputStream in = H2.class.getResourceAsStream(PROPS)) {
            p.load(in);
        }
        Class.forName("org.h2.Driver");
        // Class.forName("com.ibm.db2.jcc.DB2Driver"); // opcional
        return DriverManager.getConnection(p.getProperty("db.url"), p.getProperty("db.user"), p.getProperty("db.password"));
    }
}
