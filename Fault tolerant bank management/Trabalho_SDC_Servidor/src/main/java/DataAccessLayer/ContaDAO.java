/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataAccessLayer;

import dados.Conta;
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
public class ContaDAO implements InterfaceDAL<Conta> {
    private static final String TableName = "Contas";
    private Statement sqlStatement;
    
    
    
    /* Construtor */
    public ContaDAO() {
        try {
            Connection con = DBManager.getInstance().getCon();
            this.sqlStatement = con.createStatement();
            
        } catch (Exception e) {
            System.err.println("Error instanciating sqlStatement DB.");
            Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    /* Métodos */
    @Override
    public ArrayList<Conta> getAll() {
        ArrayList<Conta> values = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM " + TableName;
            ResultSet result = this.sqlStatement.executeQuery(sql);

            while (result.next()) {
                values.add(new Conta(result.getInt("ID"), result.getDouble("BALANCE")));
            }
            
            result.close();
        } catch (Exception e) {
            Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
        
        return values;
    }

    @Override
    public ArrayList<Conta> getAll(String whereClause) {
        ArrayList<Conta> values = new ArrayList<>();
        
        try {
            String sql = "SELECT * FROM " + TableName + " " + whereClause + " ";
            ResultSet result = this.sqlStatement.executeQuery(sql);

            while (result.next()) {
                values.add(new Conta(result.getInt("ID"), result.getDouble("BALANCE")));
            }
            
            result.close();
        } catch (Exception e) {
            Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
            return null;
        }
        
        return values;
    }

    @Override
    public Conta get(int key) {
        Conta tempAccount = null;

        try {
            String sql = "SELECT * FROM " + TableName + " WHERE ID =" + key + "";
            ResultSet result = this.sqlStatement.executeQuery(sql);

            if (result.next()) {
                tempAccount = new Conta(result.getInt("ID"), result.getDouble("BALANCE"));
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

    @Override
    public Boolean set(Conta data, Boolean isNew) {
        ResultSet resultSet = null;

        if(isNew) {
            //insert statement
            try {
                resultSet = this.sqlStatement.executeQuery("SELECT * from " + TableName + " WHERE ID = " + data.getIdConta() + " ");

                if(resultSet.next()) {
                    //update statement
                    try {
                        int rows = this.sqlStatement.executeUpdate("UPDATE " + TableName + " SET  Balance = " + data.getSaldo() + "  WHERE Id = " + data.getIdConta());

                        if(rows == 1) {
                            System.out.println("Conta " + data.getIdConta() + " editada");
                            return true;
                        } else {
                            throw new Exception("Erro update de conta" + data.getIdConta());
                        }
                    } catch (Exception e) {
                        Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
                        return false;
                    }
                }

                resultSet.close();

                int rows = this.sqlStatement.executeUpdate("INSERT INTO " + TableName + " VALUES ( " + data.getIdConta() + " ," + data.getSaldo() + ")");

                if (rows == 1) {
                    System.out.println("Conta " + data.getIdConta() + " criada");
                    return true;
                } else {
                    throw new Exception("Cannot create " + data.getIdConta());
                }

            } catch (Exception e) {
                Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
                return false;
            }
        } else {
            //update statement
            try {
                int rows = this.sqlStatement.executeUpdate("UPDATE " + TableName + " SET  Balance = " + data.getSaldo() + "  WHERE Id = " + data.getIdConta());

                if(rows == 1) {
                    System.out.println("Conta " + data.getIdConta() + " editada");
                    return true;
                } else {
                    throw new Exception("Erro update de conta" + data.getIdConta());
                }
            } catch (Exception e) {
                Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
                return false;
            }
        }
    }

    @Override
    public Boolean delete(int key) {
        try {
            int rows = this.sqlStatement.executeUpdate("DELETE FROM " + TableName + " WHERE ID=" + key + "");
            
            if (rows != 1) {
                throw new Exception("Cannot delete " + key);
            }
        } catch (Exception e) {
            System.out.println("Cannot delete " + key);
            Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
            
            return false;
        }
        
        System.out.println("Account " + key + " deleted!");
        
        return true;
    }

    public int numeracaoContas() {
        try {
            String sql = "SELECT Count(*) FROM " + TableName + " ";
            ResultSet result = this.sqlStatement.executeQuery(sql);

            if(result.next()) {
                int res = result.getInt(1);
                result.close();
                return res;
            } else {
                throw new Exception("Erro numeracaoContas");
            }
        } catch (Exception e) {
            Logger.getLogger(ContaDAO.class.getName()).log(Level.SEVERE, null, e);
            return 0;
        }
    }
}
