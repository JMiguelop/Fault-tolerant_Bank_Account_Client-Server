/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataAccessLayer;

import dados.Conta;
import dados.Movimento;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ricardo
 */
public class MovimentoDAO implements InterfaceDAL<Movimento> {
    private static final String TableName = "Movimentos";
    private Statement sqlStatement;

    
    
    /* Construtor */
    public MovimentoDAO() {
        try {
            Connection con = DBManager.getInstance().getCon();
            sqlStatement = con.createStatement();
        } catch (Exception e) {
            System.err.println("Error instanciating sqlStatement DB.");
            Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    
    /* Métodos */
    @Override
    public ArrayList<Movimento> getAll() {
        ArrayList<Movimento> values = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM " + TableName;
            ResultSet result = sqlStatement.executeQuery(sql);

            while (result.next()) {
                values.add(new Movimento(result.getString("Movimento"),result.getInt("IdConta")));
            }
            result.close();
        } catch (Exception e) {
            Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
        
        return values;
    }
    
    @Override
    public ArrayList<Movimento> getAll(String whereClause) {
        ArrayList<Movimento> values = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM " + TableName + " " + whereClause + "";
            ResultSet result = sqlStatement.executeQuery(sql);

            while (result.next()) {
                values.add(new Movimento(result.getString("Movimento"),result.getInt("IdConta")));
            }
            result.close();
        } catch (Exception e) {
            Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
        
        return values;
    }

    @Override
    public Movimento get(int key) {
        Movimento tempAccount = null;

        try {
            String sql = "SELECT * FROM " + TableName + " WHERE ID = " + key + " ";
            ResultSet result = sqlStatement.executeQuery(sql);

            if(result.next()) {
                tempAccount = new Movimento(result.getString("Movimento"),result.getInt("IdConta"));
                result.close();
            } else {
                return null;
            }

        } catch (Exception e) {
            Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }

        return tempAccount;
    }
    
    public Integer getNumeroUltimoMovimentoConta(int idConta) {
        try {
            String sql = "SELECT COUNT(IDCONTA) AS COUNTRESULT FROM " + TableName + " WHERE IDCONTA=" + idConta + " ";
            ResultSet result = sqlStatement.executeQuery(sql);
            
            if(result.next()) {
                Integer i = result.getInt("COUNTRESULT");
                result.close();
                return i;
            }
        } catch (Exception e) {
            Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
        
        return null;
    }
    
    public ArrayList<String> getMovimentosContaDesdeN(int idConta, int n) {
        ArrayList<String> res = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM ( SELECT * FROM " + TableName + " WHERE IDCONTA=" + idConta + " ) AS TMP OFFSET " + n + " ROWS";
            ResultSet result = sqlStatement.executeQuery(sql);
            
            while(result.next()) {
                res.add(result.getString("Movimento"));
            }
            result.close();
        } catch (Exception e) {
            Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
        
        return res;
    }

    @Override
    public Boolean set(Movimento data, Boolean isNew) {
        try {
            int rows = sqlStatement.executeUpdate("INSERT INTO " + TableName + "(IdConta , Movimento)" + " VALUES ( " + data.getIdConta() + " ,'" + data.getMovimento()+ "')");

            if (rows == 1) {
                System.out.println("Movimento efetuado");
                return true;
            } else {
                throw new Exception("Cannot create " + data.getIdConta());
            }
        } catch (Exception e) {
            Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
    }

    @Override
    public Boolean delete(int key) {
        try {
            int rows = sqlStatement.executeUpdate("DELETE FROM " + TableName + " WHERE ID = " + key + " ");
            
            if (rows != 1) {
                throw new Exception("Cannot delete " + key);
            }
        } catch (Exception e) {
            System.out.println("Cannot delete " + key);
            Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }
        
        System.out.println("Movimento " + key + " apagado!");
        
        return true;
    }
}
