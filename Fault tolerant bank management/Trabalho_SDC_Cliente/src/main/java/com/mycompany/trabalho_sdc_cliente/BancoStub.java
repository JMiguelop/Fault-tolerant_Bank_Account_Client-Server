/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.trabalho_sdc_cliente;

import interfac.InterfaceBanco;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.sf.jgcs.DataSession;
import net.sf.jgcs.GroupConfiguration;
import net.sf.jgcs.GroupException;
import net.sf.jgcs.InvalidStateException;
import net.sf.jgcs.MembershipListener;
import net.sf.jgcs.MembershipSession;
import net.sf.jgcs.Message;
import net.sf.jgcs.MessageListener;
import net.sf.jgcs.Protocol;
import net.sf.jgcs.ProtocolFactory;
import net.sf.jgcs.jgroups.JGroupsGroup;
import net.sf.jgcs.jgroups.JGroupsProtocolFactory;
import net.sf.jgcs.jgroups.JGroupsService;

/**
 *
 * @author Miguel
 */
public class BancoStub implements InterfaceBanco, MessageListener, MembershipListener {
    private Message resposta;
    private JGroupsService s;
    private DataSession ds;
    private MembershipSession ms;
    private int contadorMensagens;
    
    
    
    /* Construtor */
    public BancoStub() throws GroupException {
        this.resposta = null;
        this.contadorMensagens = 0;
        
        ProtocolFactory pf = new JGroupsProtocolFactory();
        GroupConfiguration gc = new JGroupsGroup("trabalhoSDC");
        this.s = new JGroupsService();
        
        Protocol p = pf.createProtocol();
        this.ms = (MembershipSession) p.openControlSession(gc);
        this.ms.setMembershipListener(this);
        this.ds = p.openDataSession(gc);
        this.ds.setMessageListener((MessageListener) this);
        this.ms.join();
    }

    @Override
    public int criarConta() {
        try {
            System.out.println("A enviar pedido de criacao de conta");
            Mensagem mensagemPedido = new Mensagem(Mensagem.TipoMensagem.CRIAR_CONTA, Mensagem.OrigemTipo.CLIENTE, this.ms.getLocalAddress(), this.contadorMensagens);
            Message m = this.ds.createMessage();
            m.setPayload(mensagemPedido.mensagemToBytes());
            this.ds.multicast(m, s, null);
            
            synchronized(this){
                //Espera por uma resposta
                while(this.resposta == null) {
                    this.wait();
                }
            }
            
            this.resposta = null;
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(BancoStub.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return 0;
    }

    @Override
    public boolean credito(int conta, double valor) {
        try {
            Mensagem mensagemPedido = new Mensagem(Mensagem.TipoMensagem.CREDITO, Mensagem.OrigemTipo.CLIENTE, this.ms.getLocalAddress(), this.contadorMensagens, conta, valor);
            Message m = this.ds.createMessage();
            m.setPayload(mensagemPedido.mensagemToBytes());
            this.ds.multicast(m, s, null);
            
            synchronized(this){
                //Espera por uma resposta
                while(this.resposta == null) {
                    this.wait();
                }
            }
            
            this.resposta = null;
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(BancoStub.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }

    @Override
    public boolean debito(int conta, double valor) {
        try {
            Mensagem mensagemPedido = new Mensagem(Mensagem.TipoMensagem.DEBITO, Mensagem.OrigemTipo.CLIENTE, this.ms.getLocalAddress(), this.contadorMensagens, conta, valor);
            Message m = this.ds.createMessage();
            m.setPayload(mensagemPedido.mensagemToBytes());
            this.ds.multicast(m, s, null);
            
            synchronized(this){
                //Espera por uma resposta
                while(this.resposta == null) {
                    this.wait();
                }
            }
            
            this.resposta = null;
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(BancoStub.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }

    @Override
    public boolean transferencia(int contaOrigem, int contaDestino, double valor) {
        try {
            Mensagem mensagemPedido = new Mensagem(Mensagem.TipoMensagem.TRANSFERENCIA, Mensagem.OrigemTipo.CLIENTE, this.ms.getLocalAddress(), this.contadorMensagens, contaOrigem, contaDestino, valor);
            Message m = this.ds.createMessage();
            m.setPayload(mensagemPedido.mensagemToBytes());
            this.ds.multicast(m, s, null);
            
            synchronized(this){
                //Espera por uma resposta
                while(this.resposta == null) {
                    this.wait();
                }
            }
            
            this.resposta = null;
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(BancoStub.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return true;
    }
    
    @Override
    public String listarMovimentos(int conta, int nMovimentos) {
        try {
            Mensagem mensagemPedido = new Mensagem(Mensagem.TipoMensagem.N_ULTIMOS_MOVIMENTOS, Mensagem.OrigemTipo.CLIENTE, this.ms.getLocalAddress(), this.contadorMensagens, conta, nMovimentos);
            Message m = this.ds.createMessage();
            m.setPayload(mensagemPedido.mensagemToBytes());
            this.ds.multicast(m, s, null);
            
            synchronized(this){
                //Espera por uma resposta
                while(this.resposta == null) {
                    this.wait();
                }
            }
            
            this.resposta = null;
        } catch (IOException | InterruptedException ex) {
            Logger.getLogger(BancoStub.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    @Override
    public synchronized Object onMessage(Message msg) {        
        try {
            Mensagem mensagemRecebida = Mensagem.mensagemFromBytes(msg.getPayload());
            
            switch(mensagemRecebida.getTipoMensagem()) {
                //Ignoro pedidos de transferencia de estado entre servidores
                case CRIAR_CONTA: case CREDITO: case DEBITO: case TRANSFERENCIA: case N_ULTIMOS_MOVIMENTOS:
                    /* Ignoro os meus próprios pedidos && Caso a mensagem recebida nao venha de um Cliente && Caso ainda nao tenha recebido resposta ao pedido */
                    if((!msg.getSenderAddress().equals(this.ms.getLocalAddress())) && (!mensagemRecebida.getOrigemTipo().equals(Mensagem.OrigemTipo.CLIENTE)) && (mensagemRecebida.getIdMensagem() == this.contadorMensagens)) {
                        this.resposta = msg;
                        this.contadorMensagens++;
                        System.out.println("Resposta (enviada por " + msg.getSenderAddress() + "): " + mensagemRecebida.toString());
                        this.notifyAll();
                    }
                    
                    break;
            }
        } catch (ClassNotFoundException | IOException ex) {
            Logger.getLogger(BancoStub.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }

    public void leaveGroup() throws IOException {
        this.ms.leave();
        this.ms.close();
        //this.ds.close();
    }
    
    @Override
    public void onMembershipChange() {
        try {
            System.out.println("MEMBERSHIP CHANGED: " + this.ms.getMembership().getMembershipList().toString());
        } catch (InvalidStateException ex) {
            Logger.getLogger(BancoStub.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onExcluded() {
        System.out.println("Member out !!!");
        //Tentar voltar a inserilo -> this.ms.join() ????
    }
}
