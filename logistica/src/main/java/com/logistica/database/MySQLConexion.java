package com.logistica.database;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;

public class MySQLConexion {

    private static final String MYSQL_Url = "jdbc:mysql://localhost:3306/logistica";
    private static final String MYSQL_Usuario = "logistica";
    private static final String MYSQL_Password = "logistica";

    // abro conexion jdbc
    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(MYSQL_Url, MYSQL_Usuario, MYSQL_Password);
    }
}
