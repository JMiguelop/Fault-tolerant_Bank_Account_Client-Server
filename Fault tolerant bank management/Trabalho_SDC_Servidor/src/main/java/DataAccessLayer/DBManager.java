/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataAccessLayer;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Ricardo
 */
public class DBManager {
    private static DBManager instance = new DBManager();
    private String name = "bankserver";
    private Statement sqlStatement;
    private Connection con;

    
   
    //Get the only object available
    public static DBManager getInstance(){
       return instance;
    }
    
//    public DBManager(String serverId){
//        this.name = serverId;
//        createDB();
//        instance = this;
//    }
    
    public void createDB(String serverId) {
        this.name = serverId;
        try {
            System.out.println("A criar base de dados derby.");
            this.con = getConnection();
            this.con.setAutoCommit(true);
            this.sqlStatement = this.con.createStatement();
            boolean exists = ExistsDatabase(this.con);
            if(!exists){
                sqlStatement.executeUpdate("CREATE TABLE CONTAS (Id INT NOT NULL, Balance DOUBLE, PRIMARY KEY (ID))");
                sqlStatement.executeUpdate("CREATE TABLE MOVIMENTOS (Id INT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 0, INCREMENT BY 1), IdConta INT, Movimento VARCHAR(100), PRIMARY KEY (ID))");
            }
            
            System.out.println("Base de dados OK.");
        } catch (Exception e) {
            System.err.println("Error creating DB.");
            System.exit(1);
        }
    }
    
    private Connection getConnection() throws ClassNotFoundException, SQLException {
        //return DriverManager.getConnection("jdbc:derby:"+ name +";create=true" );
        return DriverManager.getConnection("jdbc:derby://localhost:1527/"+this.name+";create=true");
    }
    
    private boolean ExistsDatabase(Connection connection) throws SQLException {
        DatabaseMetaData dbm = connection.getMetaData();
        boolean existsDb = false;
        
        for(ResultSet rs = dbm.getTables(null, null, null, null); rs.next();){
            String tableName = rs.getString(3);
            if(tableName.equalsIgnoreCase("CONTAS")){
                existsDb = true;
                rs.close();
                break;
            }
        }
        
        return existsDb;
    }

    /**
     * @return the con
     */
    public Connection getCon() {
        return this.con;
    }
}
