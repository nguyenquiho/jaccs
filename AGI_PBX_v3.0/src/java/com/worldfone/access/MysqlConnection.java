package com.worldfone.access;

import com.mysql.cj.jdbc.MysqlDataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author dev MitsuyoRai
 */
public class MysqlConnection {

    static final Logger logger = LogManager.getLogger("CrmEvent");
    private static String dbHost = "127.0.0.1";
    private static String dbPort = "3306";
    private static String dbName = "pbx";
    private static String dbUsername = "root";
    private static String dbPassword = "QWQIWI";

    /**
     *
     */
    private MysqlDataSource ds = null;

    /**
     * Pattern Singleton
     */
    private static volatile MysqlConnection instance;

    public static MysqlConnection getInstance() {
        MysqlConnection _instance = MysqlConnection.instance;
        if (_instance == null) {
            synchronized (MysqlConnection.class) {
                _instance = MysqlConnection.instance;
                if (null == _instance) {
                    MysqlConnection.instance = _instance = new MysqlConnection();
                }
            }
        }
        return _instance;
    }

    private MysqlConnection() {
        ds = getMySQLDataSource();
    }

    /**
     * getMySQLDataSource
     *
     * @return MysqlDataSource
     */
    private static MysqlDataSource getMySQLDataSource() {
        Properties props = new Properties();
        try {
            FileInputStream in = new FileInputStream("/opt/tomcat9/webapps/AGI_PBX_Config.properties");
            props.load(in);
            if (props.containsKey("db.host")) {
                dbHost = props.getProperty("db.host");
//                System.out.print("DBHOST "+ dbHost);

            } else {
                logger.error("Missing db.host");
            }
            if (props.containsKey("db.port")) {
//                System.out.print("DBPORT "+ dbPort);
                dbPort = props.getProperty("db.port");
            } else {
                logger.error("Missing db.port");
            }
            if (props.containsKey("db.name")) {
                dbName = props.getProperty("db.name");
//                System.out.print("DBNAME "+ dbName);
            } else {
                logger.error("Missing db_name");
            }
            if (props.containsKey("db.user")) {
                dbUsername = props.getProperty("db.user");
//                System.out.print("DBUSER "+ dbUsername);
            } else {
                logger.error("Missing db.user");
                System.out.print("Missing db.user");
            }
            if (props.containsKey("db.pass")) {
                dbPassword = props.getProperty("db.pass");
//                System.out.print("DBPASS "+ dbPassword);
            } else {
                logger.error("Missing db.pass");
            }
        } catch (MalformedURLException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
        String url = "jdbc:mysql://" + dbHost + ":" + dbPort + "/" + dbName + "?user=" + dbUsername + "&password=" + dbPassword + "&useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&failOverReadOnly=false&maxReconnects=10";
        MysqlDataSource _ds = new MysqlDataSource();
        _ds.setURL(url);
        return _ds;
    }

    /**
     * openConnect()
     *
     * @return
     */
    public Connection openConnect() {
        try {
            return ds.getConnection();
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
        return null;
    }

    /**
     * bindParams
     *
     * @param pst
     * @param params
     * @throws SQLException
     */
    private void bindParams(PreparedStatement pst, Object... params) throws SQLException {
        int i = 0;
        for (Object item : params) {
            i++;
            if (item instanceof Integer) {
                pst.setInt(i, Integer.parseInt(item.toString()));
            }
            if (item instanceof String) {
                pst.setString(i, item.toString());
            }
        }
    }

    /**
     * getColumnByQuery
     *
     * @param sql
     * @param col
     * @return
     */
    public String getColumnByQuery(String sql, String col) {
//         System.out.println(sql);
        String context = null;
        try (Connection c = openConnect()) {
            try (PreparedStatement pst = c.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); ResultSet rs = pst.executeQuery()) {
                if (rs.first()) {
                    context = rs.getString(col);
                }
            }
        } catch (SQLException ex) {
            System.out.println("SQL Error query: " + ex);
        }
        return context;
    }

    /**
     * getColumnByQuery
     *
     * @param sql
     * @param col
     * @param params
     * @return
     */
    public String getColumnByQuery(String sql, String col, Object... params) {
        String context = null;
        try (Connection c = openConnect(); PreparedStatement pst = c.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
            bindParams(pst, params);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.first()) {
                    context = rs.getString(col);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error query: " + ex);
        }
        return context;
    }

    /**
     * getListByQuery
     *
     * @param sql
     * @return
     */
    public ArrayList<HashMap> getListByQuery(String sql) {
        // System.out.println(sql);
        ArrayList<HashMap> context = null;
        try {
            context = new ArrayList<>();
            try (Connection c = openConnect()) {
                try (PreparedStatement pst = c.prepareStatement(sql); ResultSet rs = pst.executeQuery()) {
                    ResultSetMetaData rsmt = rs.getMetaData();
                    while (rs.next()) {
                        HashMap<String, String> row = new HashMap<>();
                        int i = 1;
                        while (i <= rsmt.getColumnCount()) {
                            //System.out.println(rsmt.getColumnLabel(i));
                            //System.out.println(rsmt.getColumnName(i));
                            //row.put(rsmt.getColumnName(i), rs.getString(i));
                            row.put(rsmt.getColumnLabel(i), rs.getString(i));
                            i++;
                        }
                        context.add(row);
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error query : " + ex);
        }
        return context;
    }

    /**
     * getListByQuery
     *
     * @param sql
     * @param params
     * @return
     */
    public ArrayList<HashMap> getListByQuery(String sql, Object... params) {
        //System.out.println(sql);
        ArrayList<HashMap> context = null;
        try {
            context = new ArrayList<>();
            try (Connection c = openConnect(); PreparedStatement pst = c.prepareStatement(sql)) {
                bindParams(pst, params);
                try (ResultSet rs = pst.executeQuery()) {
                    ResultSetMetaData rsmt = rs.getMetaData();
                    while (rs.next()) {
                        HashMap<String, String> row = new HashMap<>();
                        int i = 1;
                        while (i <= rsmt.getColumnCount()) {
                            //System.out.println(rsmt.getColumnLabel(i));
                            //System.out.println(rsmt.getColumnName(i));
                            //row.put(rsmt.getColumnName(i), rs.getString(i));
                            row.put(rsmt.getColumnLabel(i), rs.getString(i));
                            i++;
                        }
                        context.add(row);
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error query : " + ex);
        }
        return context;
    }

    /**
     * getOneByQuery
     *
     * @param sql
     * @return
     */
    public HashMap<String, String> getOneByQuery(String sql) {
//        System.out.println(sql);
        HashMap<String, String> context = null;
        try {
            try (Connection c = openConnect()) {
                try (PreparedStatement pst = c.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY); ResultSet rs = pst.executeQuery()) {
                    ResultSetMetaData rsmt = rs.getMetaData();
                    if (rs.first()) {
                        HashMap<String, String> row = new HashMap<>();
                        int i = 1;
                        while (i <= rsmt.getColumnCount()) {
                            row.put(rsmt.getColumnLabel(i), rs.getString(i));
                            i++;
                        }
                        return row;
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("maSieu Error query : " + ex);
        }
        return context;
    }

    /**
     * getOneByQuery
     *
     * @param sql
     * @param params
     * @return
     */
    public HashMap<String, String> getOneByQuery(String sql, Object... params) {
//        System.out.println(sql);
        HashMap<String, String> context = null;
        try {
            try (Connection c = openConnect()) {
                try (PreparedStatement pst = c.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                    bindParams(pst, params);
                    try (ResultSet rs = pst.executeQuery()) {
                        ResultSetMetaData rsmt = rs.getMetaData();
                        if (rs.first()) {
                            HashMap<String, String> row = new HashMap<>();
                            int i = 1;
                            while (i <= rsmt.getColumnCount()) {
                                row.put(rsmt.getColumnLabel(i), rs.getString(i));
                                i++;
                            }
                            return row;
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            System.out.println("Error query : " + ex);
        }
        return context;
    }

    /**
     * executeQuery
     *
     * @param sql
     * @return
     */
    public boolean executeQuery(String sql) {
        try {
            try (Connection c = openConnect(); PreparedStatement pst = c.prepareStatement(sql)) {
                return pst.execute();
            }
        } catch (SQLException ex) {
            System.out.println("Error query : " + ex);
        }
        return false;
    }

    /**
     * executeQuery
     *
     * @param sql
     * @param params
     * @return
     */
    public boolean executeQuery(String sql, Object... params) {
        try {
            try (Connection c = openConnect(); PreparedStatement pst = c.prepareStatement(sql)) {
                bindParams(pst, params);
                return pst.execute();
            }
        } catch (SQLException ex) {
            System.out.println("insert Error query : " + ex);
        }
        return false;
    }

    /**
     * executeNonQuery
     *
     * @param sql
     */
    public void executeNonQuery(String sql) {
        try {
            try (Connection c = openConnect(); PreparedStatement pst = c.prepareStatement(sql)) {
                pst.execute();
            }
        } catch (SQLException ex) {
            System.out.println("Execute Error query: " + ex);
        }
    }

    /**
     * executeNonQuery
     *
     * @param sql
     * @param params
     */
    public void executeNonQuery(String sql, Object... params) {
        try {
            try (Connection c = openConnect(); PreparedStatement pst = c.prepareStatement(sql)) {
                bindParams(pst, params);
                pst.execute();
            }
        } catch (SQLException ex) {
            System.out.println("Execute Error query : " + ex);
        }
    }

    /**
     * executeUpdateQuery
     *
     * @param sql
     * @return
     */
    public boolean executeUpdateQuery(String sql) {
        //System.out.println(sql);
        try {
            try (Connection c = openConnect(); PreparedStatement pst = c.prepareStatement(sql)) {
                int exec = pst.executeUpdate();
                return (exec > 0);
            }
        } catch (SQLException ex) {
            System.out.println("Execute Update Error query : " + ex);
        }
        return false;
    }

    /**
     * executeUpdateQuery
     *
     * @param sql
     * @param params
     * @return
     */
    public boolean executeUpdateQuery(String sql, Object... params) {
        //System.out.println(sql);
        try {
            try (Connection c = openConnect(); PreparedStatement pst = c.prepareStatement(sql)) {
                bindParams(pst, params);
                int exec = pst.executeUpdate();
                return (exec > 0);
            }
        } catch (SQLException ex) {
            System.out.println("Execute Update Error query : " + ex);
        }
        return false;
    }

    /**
     * executeUpdateNonQuery
     *
     * @param sql
     */
    public void executeUpdateNonQuery(String sql) {
//        System.out.println(sql);
        try {
            try (Connection c = openConnect(); PreparedStatement pst = c.prepareStatement(sql)) {
                pst.executeUpdate();
            }
        } catch (SQLException ex) {
            System.out.println("Execute Update Error query : " + ex);
        }
    }

    /**
     * executeUpdateNonQuery
     *
     * @param sql
     * @param params
     */
    public void executeUpdateNonQuery(String sql, Object... params) {
        //System.out.println(sql);
        try {
            try (Connection c = openConnect(); PreparedStatement pst = c.prepareStatement(sql)) {
                bindParams(pst, params);
                pst.executeUpdate();
            }
        } catch (SQLException ex) {
            System.out.println("Execute Update Error query : " + ex);
        }
    }

    /**
     * executeUpdateNonQueryTransaction
     *
     * @param sqls String[] sqls = {"SQL1 ?,?,?...", "SQL2 ?,?,?...",...};
     */
    public void executeUpdateNonQueryTransaction(String[] sqls) {
        try {
            try (Connection c = openConnect()) {
                c.setAutoCommit(false);
                boolean flag = true;
                for (String sql : sqls) {
                    try (PreparedStatement pst = c.prepareStatement(sql)) {
                        if (pst.executeUpdate() < 0) {
                            flag = false;
                        }
                    }
                }
                if (flag) {
                    c.commit();
                } else {
                    c.rollback();
                }
            }
        } catch (SQLException ex) {
            System.out.println("Execute Update Error query : " + ex);
        }
    }

    /**
     * executeUpdateNonQueryTransaction
     *
     * @param sqls String[] sqls = {"SQL1 ?,?,?...", "SQL2 ?,?,?...",...};
     * @param params Object[] param1 = {Integer, "String", Object,...}, Object[]
     * param2 = {String, "Integer", Object,...}, ...
     */
    public void executeUpdateNonQueryTransaction(String[] sqls, Object[]... params) {
        try {
            try (Connection c = openConnect()) {
                c.setAutoCommit(false);
                boolean flag = true;
                int i = 0;
                for (String sql : sqls) {
                    try (PreparedStatement pst = c.prepareStatement(sql)) {
                        bindParams(pst, params[i]);
                        if (pst.executeUpdate() < 0) {
                            flag = false;
                        }
                    }
                    i++;
                }
                if (flag) {
                    c.commit();
                } else {
                    c.rollback();
                }
            }
        } catch (SQLException ex) {
            System.out.println("Execute Update Error query : " + ex);
        }
    }
}
