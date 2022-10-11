package com.usst.kgfusion.util;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import org.neo4j.driver.v1.Driver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class KingBaseUtils {

    private static DataSource ds;
    //private static String CONFIG_PATH = "application.yml";
    //1.定义成员变量 DataSource
    @Value("${spring.mysql.driver-class-name}")
    private  String _driver;
    @Value("${spring.mysql.url}")
    private  String _url;
    @Value("${spring.mysql.username}")
    private  String _username;
    @Value("${spring.mysql.password}")
    private  String _password;
    @Value("${spring.mysql.initialSize}")
    private  Integer _initialSize;
    @Value("${spring.mysql.minIdle}")
    private  Integer _minIdle;
    @Value("${spring.mysql.maxActive}")
    private  Integer _maxActive;
    @Value("${spring.mysql.maxWait}")
    private  Integer _maxWait;
    @Value("${spring.mysql.timeBetweenEvictionRunsMillis}")
    private  Integer _timeBetweenEvictionRunsMillis;
    @Value("${spring.mysql.minEvictableIdleTimeMillis}")
    private  Integer _minEvictableIdleTimeMillis;

    private  static String driver;
    private  static String url;
    private  static String username;
    private  static String password;
    private  static String initialSize;
    private  static String minIdle;
    private  static String maxActive;
    private  static String maxWait;
    private  static String timeBetweenEvictionRunsMillis;
    private  static String minEvictableIdleTimeMillis;


    private static Map prop;

    @PostConstruct
    public void setProp() throws Exception{
        prop = new HashMap();
        driver = this._driver;
        url = this._url;
        username = this._username;
        password = this._password;
        initialSize = this._initialSize.toString();
        minIdle = this._minIdle.toString();
        maxActive = this._maxActive.toString();
        maxWait = this._maxWait.toString();
        timeBetweenEvictionRunsMillis = this._timeBetweenEvictionRunsMillis.toString();
        minEvictableIdleTimeMillis = this._minEvictableIdleTimeMillis.toString();


        prop.put("driverClassName", driver);
        prop.put("url", url);
        prop.put("username", username);
        prop.put("password", password);
        prop.put("initialSize", initialSize);
        prop.put("minIdle", minIdle);
        prop.put("maxActive", maxActive);
        prop.put("maxWait", maxWait);
        prop.put("timeBetweenEvictionRunsMillis", timeBetweenEvictionRunsMillis);
        prop.put("minEvictableIdleTimeMillis", minEvictableIdleTimeMillis);




        ds = DruidDataSourceFactory.createDataSource(prop);
        System.out.println("mysql参数接受成功");
    }

//    static {
//        //1.加载配置文件
//        Properties pro1 = new Properties();
//
//        Map neo4jP = new LinkedHashMap();
//        try {
//            //pro1.load(KingBaseUtils.class.getClassLoader().getResourceAsStream(CONFIG_PATH));
//            Yaml yaml = new Yaml();
//            InputStream in = KingBaseUtils.class.getClassLoader().getResourceAsStream(CONFIG_PATH);
//            LinkedHashMap<String, Map<String,String>> properties1 = yaml.loadAs(in, LinkedHashMap.class);
//            neo4jP = (LinkedHashMap) properties1.get("spring");
//            neo4jP = (LinkedHashMap) neo4jP.get("mysql");
//            pro1.setProperty("url",(String)neo4jP.get("url"));
//            pro1.setProperty("username",(String)neo4jP.get("username"));
//            pro1.setProperty("password",(String)neo4jP.get("password"));
//            pro1.setProperty("driverClassName",(String)neo4jP.get("driver-class-name"));
//            System.out.println("test1");
//            ds = DruidDataSourceFactory.createDataSource(pro1);
////        try {
////            pro1.load(KingBaseUtils.class.getClassLoader().getResourceAsStream("KBdruid.properties"));
////            //获取DataSource
////            ds = DruidDataSourceFactory.createDataSource(pro1);
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//
//
//    }
    //获取连接
    public static Connection getConnection1() throws SQLException {
        Connection conn = null;
        try{
            conn = ds.getConnection();
        }catch(SQLException e){
            throw e;
        }
        
        return conn;

    }


    //释放资源
    public static void close(Statement stmt, Connection conn) throws SQLException{
        if (stmt!=null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        if (conn!=null){
            try {
                conn.close();//归还连接
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }


    public static void close(ResultSet rs, Statement stmt, Connection conn) throws SQLException{
        if (rs!=null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        if (stmt!=null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
        if (conn!=null){
            try {
                conn.close();//归还连接
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    //获取连接池
    public static DataSource getDataSource1(){

        return ds;
    }


    public static void main(String[] args) throws SQLException {
        Connection driver = KingBaseUtils.getConnection1();
    }
}
