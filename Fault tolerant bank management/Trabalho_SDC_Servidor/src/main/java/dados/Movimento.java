/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dados;

import java.io.Serializable;

/**
 *
 * @author Miguel
 */
public class Movimento implements Serializable {
    private int id;
    private String movimento;
    private int idConta;
    
    
    
    /* Construtor */
    public Movimento(String mov) {
        this.movimento = mov;
    }
    
    public Movimento(String mov, int idConta) {
        this.movimento = mov;
        this.idConta = idConta;
    }
    
    
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    public String getMovimento() {
        return this.movimento;
    }
    
    /**
     * @return the idConta
     */
    public int getIdConta() {
        return idConta;
    }

    /**
     * @param idConta the idConta to set
     */
    public void setIdConta(int idConta) {
        this.idConta = idConta;
    }
    
    /* toString */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("          " + this.movimento + "\n");
        
        return sb.toString();
    }

}
