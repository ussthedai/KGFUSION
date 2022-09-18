package com.usst.kgfusion.util;
import javax.annotation.PostConstruct;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



@Component
public class Neo4jUtils {
    private static Driver driver;
    // static String NEO4J_CONFIG_PATH = "application.yml";
    @Value("${spring.neo4j.uri}")
    private String _uri;
    @Value("${spring.neo4j.username}")
    private String _user;
    @Value("${spring.neo4j.password}")
    private String _passwd;
    @Value("${spring.neo4j.maxConnectionPoolSize}")
    private int _maxConnectionPoolSize;

    private static String uri;
    private static String user;
    private static String passwd;
    private static int maxConnectionPoolSize;


//    static Map neo4jP = new LinkedHashMap();
//
    @PostConstruct
    public void setProp(){
        uri = this._uri;
        user = this._user;
        passwd = this._passwd;
        maxConnectionPoolSize = this._maxConnectionPoolSize;
        Config config = Config.build().withMaxConnectionPoolSize(maxConnectionPoolSize).toConfig();
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, passwd), config);
        // driver = GraphDatabase.driver(uri, AuthTokens.basic(user, passwd));

        System.out.println("neo4j参数接受成功");
    }

//
//    static{
//        Properties properties = new Properties();
//        Map neo4jP = new LinkedHashMap();
//        try {
//
//            properties.setProperty("uri",(String)neo4jP.get("uri"));
//            properties.setProperty("username",(String)neo4jP.get("username"));
//            properties.setProperty("password",(String)neo4jP.get("password"));
//
//            System.out.println("test1");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        driver = GraphDatabase.driver(properties.getProperty("uri"), AuthTokens.basic(properties.getProperty("username"), properties.getProperty("password")));
//
//        System.out.println("texst");
//    }
//
    /**
     * run create statements
     *
     * @param txt
     * @return
     */
    public static StatementResult exeStatement(String txt) {
        Session session = driver.session();
        Transaction transaction = session.beginTransaction();
        StatementResult result = transaction.run(txt);
        transaction.success();
        session.close();
        return result;
    }

    public static Driver getDriver(){
        return driver;
    }
 
    /**
     * explicitly call this method to stop the driver instance
     */
    public static void close() {
        driver.closeAsync();
    }
 
    public static void main(String[] args) {
        Driver driver = Neo4jUtils.getDriver();

        Neo4jUtils b = new Neo4jUtils();
        b.setProp();
        System.out.println("2");
        // StatementResult result = Neo4jUtils.exeStatement(String.format("match (n:misre_km_zsk_gnst {name: %s}) return id(n) as id ,n.itemId as itemId, n.name as name", "'" + "服务暴露" + "'"));
        // while (result.hasNext()) {
        //     Record record = (Record) result.next();
        //     System.out.println(Integer.toString(record.get("id").asInt()) + " "
        //             + record.get("name").asString());
        // }

        
        // StatementResult res = Neo4jUtils.exeStatement("match (n) where n.graphSymbol = 'SOA服务基础设施' return n as node");
        // while (res.hasNext()) {
        //     Record record = (Record) res.next();
        //     System.out.println(record.get("node").asNode().get("name").asString());
        // }
        // driver.close();

    }
}
