/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dados;

import DataAccessLayer.ContaDAO;
import DataAccessLayer.DBManager;
import DataAccessLayer.MovimentoDAO;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 *
 * @author Miguel
 */
public class Contas implements Serializable, Cloneable {
    private int numeracaoContas; //Numeracao incremental do total de numero de contas criadas
    private Map<Integer, Conta> map;
    
    private transient ContaDAO contas;
    private transient MovimentoDAO movimentos;
    
    
    
    /* Construtor */
    public Contas(String name) {
        DBManager manager = DBManager.getInstance();
        manager.createDB(name);
        
        this.numeracaoContas = 0;
        this.map = new HashMap<>();
        this.contas = new ContaDAO();
        this.movimentos = new MovimentoDAO();
    }
    
    /* Get's */
    public synchronized int getNumeracaoContas() {
        return this.numeracaoContas;
    }
    
    public synchronized Map<Integer, Conta> getContas() {
        Map<Integer, Conta> temp = new HashMap<>();
        
        for (Conta co : this.contas.getAll()) {
            temp.put(co.getIdConta(), new Conta(co.getIdConta(), co.getSaldo(), this.movimentos.getAll("where IdConta = " + co.getIdConta() + " ")));
        }
        
        this.numeracaoContas = this.contas.numeracaoContas();
        
        return temp;
    }
    
    public synchronized ArrayList<Conta> getContasSemMovimentos() {
        return this.contas.getAll();
    }
    
    public synchronized Map<Integer, Conta> getContasParaTransferenciaEstado() {
        return this.map;
    }
    
    public synchronized Map<Integer, Integer> getUltimoMovimentoContas() {
        HashMap<Integer, Integer> res = new HashMap<>();
        
        for(Conta co : this.contas.getAll()) {
            Integer idConta = co.getIdConta();
            Integer ultimoMovimentoConta = this.movimentos.getNumeroUltimoMovimentoConta(idConta);
            res.put(idConta, ultimoMovimentoConta);
        }
        
        return res;
    }
    
    public synchronized ArrayList<String> getMovimentosContaDesdeN(int idConta, int n) {
        return this.movimentos.getMovimentosContaDesdeN(idConta, n);
    }
    
    public synchronized int addConta() {
        Conta c = new Conta(this.numeracaoContas, 0.0);
        this.contas.set(c, Boolean.TRUE);
        this.numeracaoContas++;
        return c.getIdConta();
    }
    
    public synchronized void addConta(Conta c) {
        this.contas.set(c, Boolean.TRUE);
        
        for(Movimento m : c.getMovimentos()) {
            this.movimentos.set(m, Boolean.TRUE);
        }
        
        this.numeracaoContas++;
    }
    
    public synchronized void addSaldoConta(int idConta, double saldo) {
        Conta aux = this.contas.get(idConta);
        
        //Verifica se houve diferença de saldo nas contas. Se houve então actualiza, se não houve então ignora.
        //Se aux vier a null quer dizer que foi criada uma nova conta que o servidor ainda não tem, logo deve ser criada.
        if(aux != null) {
            if(aux.getSaldo() != saldo) {
                Conta c = new Conta(idConta, saldo);
                this.contas.set(c, Boolean.FALSE); //Conta já existe logo atualiza o saldo
            }
        }
        else {
            Conta c = new Conta(idConta, saldo);
            this.contas.set(c, Boolean.TRUE); //Conta ainda não existe logo é criada
        }
    }
    
    public synchronized void addMovimentosConta(int idConta, ArrayList<String> movimentos) {
        for(String s : movimentos) {
            Movimento m = new Movimento(s, idConta);
            
            this.movimentos.set(m, Boolean.TRUE);
        }
    }
    
    public synchronized boolean credito(int conta, double valor) {
        boolean creditoOk = false;
        boolean updatedDB = false;
        
        Conta c = this.contas.get(conta);
        
        if(c != null) {
            if(c.getIdConta() == conta) {
                creditoOk = c.credito(valor);
                updatedDB = this.contas.set(c, Boolean.FALSE);
                
                return creditoOk && updatedDB;
            }
        }
        
        System.out.println("Conta nao existe !!!");
        
        return creditoOk;
    }
    
    public synchronized boolean debito(int conta, double valor) {
        boolean debitoOk = false;
        boolean updatedDB = false;
        
        Conta c = this.contas.get(conta);
        
        if(c != null) {
            if(c.getIdConta() == conta) {
                debitoOk = c.debito(valor);
                
                if(!debitoOk) {
                    System.out.println("Nao existe saldo suficiente para debitar a quantia necessaria");
                    return false;
                }
                
                updatedDB = this.contas.set(c, Boolean.FALSE); 
                
                if(!updatedDB) {
                    System.out.println("Erro BD!");
                    return false;
                }
                
                return debitoOk && updatedDB;
            }
        }
        
        System.out.println("Conta nao existe");
        
        return debitoOk;
    }
    
    public synchronized boolean transferencia(int contaOrigem, int contaDestino, double valor) {
        boolean transferenciaOk = false;
        
        Conta origem = this.contas.get(contaOrigem);
        Conta destino = this.contas.get(contaDestino);
        
        if((origem != null) && (destino != null)) {
            if((origem.getIdConta() == contaOrigem) && (destino.getIdConta() == contaDestino)) {
                if(origem.debito(valor)) { //Se existir saldo suficiente na conta origem
                    if(!contas.set(origem, Boolean.FALSE)){
                        System.out.println("Erro editar conta!");
                        return false;
                    }
                    else{
                        destino.credito(valor);
                        if(!contas.set(destino, Boolean.FALSE)){
                            System.out.println("Erro editar conta!");
                            return false;
                        }       
                    }
                    
                    transferenciaOk = true;
                }
                else System.out.println("Nao existe saldo suficiente na conta origem para efetuar transferencia");
            }
            else System.out.println("Uma ou ambas as contas nao existe");
        }
        else System.out.println("Uma ou ambas as contas nao existe");
        
        return transferenciaOk;
    }
    
    public synchronized String listarMovimentos(int conta, int nMovimentos) {
        String mensagemResposta;
        
        //if(this.map.containsKey(conta)) {
        
        Conta c = contas.get(conta);
        
        if(c != null) {
            //Conta c = this.contas.get(conta);
            if(c.getIdConta() == conta) {
//                ArrayList<Movimento> movimentosConta = c.getMovimentos();
                ArrayList<Movimento> movimentosConta = this.movimentos.getAll("where IdConta = " + c.getIdConta() +" ");
                ListIterator<Movimento> iter = movimentosConta.listIterator(movimentosConta.size()); //Iterador para percorrer o arraylist de movimentos do fim para o inicio
                
                if(movimentosConta.isEmpty()) mensagemResposta = "Conta nao tem movimentos";
                else if(movimentosConta.size() < nMovimentos) { //Caso tenha menos movimentos do que os movimentos pedidos apresenta todos os que tem do fim para o inicio
                    StringBuilder sb = new StringBuilder();
                    sb.append("Ultimos " + nMovimentos + " movimentos da conta " + conta + ":\n");
                    while(iter.hasPrevious()) {
                        sb.append(iter.previous().getMovimento());
                        sb.append("\n");
                    }
                    mensagemResposta = sb.toString();
                } else { //Caso tenha mais movimentos do que os movimentos pedidos apresenta do fim para o inicio a quantidade pedida
                    StringBuilder sb = new StringBuilder();
                    int i = 0;
                    sb.append("Ultimos " + nMovimentos + " movimentos da conta " + conta + ":\n");
                    while((iter.hasPrevious()) && (i < nMovimentos)) {
                        sb.append(iter.previous().getMovimento());
                        sb.append("\n");
                        i++;
                    }
                    mensagemResposta = sb.toString();
                }
            }
            else mensagemResposta = "Conta nao existe";
        }
        else mensagemResposta = "Conta nao existe";
        
        return mensagemResposta;
    }
    
    public synchronized byte[] contasToBytes() throws IOException {
        this.map = getContas();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(baos);
        oo.writeObject(this);
        return baos.toByteArray();
    }
    
    public synchronized static Contas contasFromBytes(byte[] bytesContas) throws IOException, ClassNotFoundException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytesContas);
        ObjectInput oi = new ObjectInputStream(bais);
        return (Contas) oi.readObject();
    }
    
    /* toString */
    @Override
    public synchronized String toString() {
        this.map = getContas();
        StringBuilder sb = new StringBuilder();
        sb.append("Total contas: " + this.numeracaoContas);
        sb.append("\n");
        for(Integer i : this.map.keySet()) {
            Conta c = this.map.get(i);
            sb.append(c.toString());
            sb.append("\n");
        }
        
        return sb.toString();
    }
}
