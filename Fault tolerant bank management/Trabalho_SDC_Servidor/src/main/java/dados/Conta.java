/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dados;

import DataAccessLayer.MovimentoDAO;
import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Miguel
 */
public class Conta implements Serializable {
    private int idConta;
    private double saldo;
    private ArrayList<Movimento> movimentos;
    private transient MovimentoDAO movimentosDAO;
    
    
    
    /* Construtor */ 
    public Conta(int idconta, double saldo) {
        this.idConta = idconta;
        this.saldo = saldo;
        this.movimentos = new ArrayList<>();
        this.movimentosDAO = new MovimentoDAO();
    }
    
    public Conta(int idconta, double saldo, ArrayList<Movimento> movimentos) {
        this.idConta = idconta;
        this.saldo = saldo;
        this.movimentos = movimentos;        
        this.movimentosDAO = new MovimentoDAO();
    }
    
    /* Get's */
    public synchronized int getIdConta() {
        return this.idConta;
    }
    
    public synchronized double getSaldo() {
        return this.saldo;
    }
    
    public synchronized ArrayList<Movimento> getMovimentos() {
        return this.movimentos;
    }
    
    /* Metodos */
    public synchronized boolean credito(double valor) {
        this.saldo = this.saldo + valor;
        Movimento m = new Movimento("Credito de " + Double.toString(valor), this.idConta);
        this.movimentos.add(m);
        
        this.movimentosDAO.set(m, Boolean.TRUE);
        
        return true;
    }
    
    public synchronized boolean debito(double valor) {
        if((this.saldo - valor) < 0) return false; //Saldo vai ser negativo
        else { //Saldo positivo
            this.saldo = this.saldo - valor;
            Movimento m = new Movimento("Debito de " + Double.toString(valor), this.idConta);
            this.movimentos.add(m);
            
            movimentosDAO.set(m, Boolean.TRUE);
            
            return true;
        }
    }
    
    /* toString */
    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Conta: " + this.idConta);
        sb.append("\n");
        sb.append("     Saldo: " + this.saldo);
        sb.append("\n");
        sb.append("     Movimentos da conta " + this.idConta + ": \n");
        if(this.movimentos.isEmpty()) sb.append("          A conta nao tem movimentos\n");
        else {
            for(Movimento m : this.movimentos) {
                sb.append(m.toString());
            }
        }
        
        return sb.toString();
    }
}
