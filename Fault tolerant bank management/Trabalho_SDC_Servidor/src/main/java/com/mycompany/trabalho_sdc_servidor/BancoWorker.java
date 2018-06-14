/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.trabalho_sdc_servidor;

import com.mycompany.trabalho_sdc_cliente.Mensagem;
import dados.Conta;
import dados.Contas;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
import net.sf.jgcs.annotation.PointToPoint;
import net.sf.jgcs.jgroups.JGroupsGroup;
import net.sf.jgcs.jgroups.JGroupsProtocolFactory;
import net.sf.jgcs.jgroups.JGroupsService;

/**
 *
 * @author Miguel
 */
public class BancoWorker implements MessageListener, MembershipListener {
    private enum EstadoServidor {PEDIDO_PARTILHA_ESTADO, MENSAGENS_EM_LISTA, GRUPO_OK};
    private BancoImpl bancoImpl;
    private Contas contas;
    private JGroupsService s;
    private DataSession ds;
    private MembershipSession ms;
    private EstadoServidor estadoServidor;
    private ArrayList<Mensagem> listaMensagens;
    
    
    
    /* Construtor */
    public BancoWorker(BancoImpl bImpl, Contas contas, int flagPrimeiroServidor) throws GroupException {
        this.bancoImpl = bImpl;
        this.contas = contas;
        
        if(flagPrimeiroServidor == 0) this.estadoServidor = EstadoServidor.GRUPO_OK; //Caso seja o primeiro servidor
        if(flagPrimeiroServidor == 1) this.estadoServidor = EstadoServidor.PEDIDO_PARTILHA_ESTADO; //Caso não seja o primeiro servidor
        
        
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
    public synchronized Object onMessage(Message msg) {
        Mensagem resposta;
        Message mensagemResposta;
        String mensagem;
        
        switch(this.estadoServidor) {
            case GRUPO_OK:
                try {
                    Mensagem mensagemRecebida = Mensagem.mensagemFromBytes(msg.getPayload());
                    
                    switch(mensagemRecebida.getTipoMensagem()) {
                        case CRIAR_CONTA:
                            if(!msg.getSenderAddress().equals(this.ms.getLocalAddress())) {
                                System.out.println("Servidor recebeu pedido de criacao de conta. Enviado por: " + msg.getSenderAddress() + ". Meu endereco: " + this.ms.getLocalAddress());
                                
                                int novaContaIdentificador = this.bancoImpl.criarConta();
                                resposta = new Mensagem(Mensagem.TipoMensagem.CRIAR_CONTA, Mensagem.OrigemTipo.SERVIDOR, mensagemRecebida.getIdMensagem(), "Conta: " + Integer.toString(novaContaIdentificador) + " criada com sucesso");
                                mensagemResposta = this.ds.createMessage();
                                mensagemResposta.setPayload(resposta.mensagemToBytes());
                                this.ds.multicast(mensagemResposta, s, null, new PointToPoint(msg.getSenderAddress())); //Envia diretamente apenas para o cliente que fez o pedido !!!
                            }

                            break;
                            
                        case CREDITO:
                            System.out.println("Servidor recebeu pedido de credito. Enviado por: " + msg.getSenderAddress() + ". Meu endereco: " + this.ms.getLocalAddress());
                            
                            boolean creditoOk = this.bancoImpl.credito(mensagemRecebida.getContaDestino(), mensagemRecebida.getValor());
                            
                            if(creditoOk) mensagem = "Credito de valor: " + Double.toString(mensagemRecebida.getValor()) + " efetuado com sucesso na conta: " + Integer.toString(mensagemRecebida.getContaDestino());
                            else mensagem = "Nao foi possivel efetuar credito";
                            
                            resposta = new Mensagem(Mensagem.TipoMensagem.CREDITO, Mensagem.OrigemTipo.SERVIDOR, mensagemRecebida.getIdMensagem(), mensagem);
                            mensagemResposta = this.ds.createMessage();
                            mensagemResposta.setPayload(resposta.mensagemToBytes());
                            this.ds.multicast(mensagemResposta, s, null, new PointToPoint(msg.getSenderAddress())); //Envia diretamente apenas para o cliente que fez o pedido !!!
                            
                            break;
                            
                        case DEBITO:
                            System.out.println("Servidor recebeu pedido de debito. Enviado por: " + msg.getSenderAddress() + ". Meu endereco: " + this.ms.getLocalAddress());
                            
                            boolean debitoOk = this.bancoImpl.debito(mensagemRecebida.getContaDestino(), mensagemRecebida.getValor());
                            
                            if(debitoOk) mensagem = "Debito de valor: " + Double.toString(mensagemRecebida.getValor()) + " efetuado com sucesso na conta: " + Integer.toString(mensagemRecebida.getContaDestino());
                            else mensagem = "Nao foi possivel efetuar debito";
                            
                            resposta = new Mensagem(Mensagem.TipoMensagem.DEBITO, Mensagem.OrigemTipo.SERVIDOR, mensagemRecebida.getIdMensagem(), mensagem);
                            mensagemResposta = this.ds.createMessage();
                            mensagemResposta.setPayload(resposta.mensagemToBytes());
                            this.ds.multicast(mensagemResposta, s, null, new PointToPoint(msg.getSenderAddress())); //Envia diretamente apenas para o cliente que fez o pedido !!!
                            
                            break;
                            
                        case TRANSFERENCIA:
                            System.out.println("Servidor recebeu pedido de transferencia. Enviado por: " + msg.getSenderAddress() + ". Meu endereco: " + this.ms.getLocalAddress());
                            
                            boolean transferenciaOk = this.bancoImpl.transferencia(mensagemRecebida.getContaOrigem(), mensagemRecebida.getContaDestino(), mensagemRecebida.getValor());
                            
                            if(transferenciaOk) mensagem = "Transferencia de valor: " + Double.toString(mensagemRecebida.getValor()) + " efetuada com sucesso entre as contas " + Integer.toString(mensagemRecebida.getContaOrigem()) + " e " + Integer.toString(mensagemRecebida.getContaDestino());
                            else mensagem = "Nao foi possivel efetuar a transferencia";
                            
                            resposta = new Mensagem(Mensagem.TipoMensagem.TRANSFERENCIA, Mensagem.OrigemTipo.SERVIDOR, mensagemRecebida.getIdMensagem(), mensagem);
                            mensagemResposta = this.ds.createMessage();
                            mensagemResposta.setPayload(resposta.mensagemToBytes());
                            this.ds.multicast(mensagemResposta, s, null, new PointToPoint(msg.getSenderAddress())); //Envia diretamente apenas para o cliente que fez o pedido !!!
                            
                            break;
                            
                        case N_ULTIMOS_MOVIMENTOS:
                            System.out.println("Servidor recebeu pedido de listagem dos ultimos movimentos. Enviado por: " + msg.getSenderAddress() + ". Meu endereco: " + this.ms.getLocalAddress());
                            
                            String ultimosMovimentos = this.bancoImpl.listarMovimentos(mensagemRecebida.getContaDestino(), mensagemRecebida.getNUltimosMovimentos());
                            resposta = new Mensagem(Mensagem.TipoMensagem.N_ULTIMOS_MOVIMENTOS, Mensagem.OrigemTipo.SERVIDOR, mensagemRecebida.getIdMensagem(), ultimosMovimentos);
                            mensagemResposta = this.ds.createMessage();
                            mensagemResposta.setPayload(resposta.mensagemToBytes());
                            this.ds.multicast(mensagemResposta, s, null, new PointToPoint(msg.getSenderAddress())); //Envia diretamente apenas para o cliente que fez o pedido !!!
                            
                            break;
                            
                        case PEDIDO_PARTILHA_ESTADO:
                            System.out.println("Servidor recebeu pedido de partilha de estado.");
                            
                            resposta = new Mensagem(Mensagem.TipoMensagem.RESPOSTA_PEDIDO_PARTILHA_ESTADO, this.contas.contasToBytes());
                            mensagemResposta = this.ds.createMessage();
                            mensagemResposta.setPayload(resposta.mensagemToBytes());
                            this.ds.multicast(mensagemResposta, s, null, new PointToPoint(msg.getSenderAddress()));
                            
                            break;
                            
                        case PEDIDO_PARTILHA_ESTADO_INCREMENTAL:
                            System.out.println("Servidor recebeu pedido de parilha de estado incremental.");
                            
                            //Respostas que vou enviar numa mensagem para o servidor que fez o pedido.
                            Map<Integer, Double> respostaContaSaldo = new HashMap<>(); 
                            Map<Integer, ArrayList<String>> respostaContaMovimentos = new HashMap<>();
                            
                            //Contas e respetivo ultimo movimento de cada uma, do servidor que fez o pedido de transferencia de estado
                            Map<Integer, Integer> ultimoMovimentoContas = mensagemRecebida.getNumeroUltimosMovimentosContas();
                            
                            //Vou buscar todas as minhas contas (sem os movimentos)
                            ArrayList<Conta> minhasContas = this.contas.getContasSemMovimentos();
                            
                            //Percorro as minhas contas.
                            //Insiro o saldo de cada conta que tenho associado à respetiva conta.
                            //Quando tem conta em comum vou buscar os movimentos dessa conta a partir do movimento que o outro
                            //servidor tem. Quando não tem conta em comum vou buscar todos os movimentos dessa conta.
                            for(Conta c : minhasContas) {
                                int idConta = c.getIdConta();
                                
                                //Contas e respetivo saldo
                                respostaContaSaldo.put(idConta, c.getSaldo());
                                
                                //Se o outro servidor tiver a conta então vou buscar os movimentos a partir do ultimo movimento que esse servidor tem.
                                //Caso o outro servidor não tiver a conta vou buscar todos os movimentos desde o inicio.
                                if(ultimoMovimentoContas.containsKey(idConta)) respostaContaMovimentos.put(idConta, this.contas.getMovimentosContaDesdeN(idConta, ultimoMovimentoContas.get(idConta)));
                                else respostaContaMovimentos.put(idConta, this.contas.getMovimentosContaDesdeN(idConta, 0));
                            }
                            
                            resposta = new Mensagem(Mensagem.TipoMensagem.RESPOSTA_PEDIDO_PARTILHA_ESTADO_INCREMENTAL, respostaContaSaldo, respostaContaMovimentos);
                            mensagemResposta = this.ds.createMessage();
                            mensagemResposta.setPayload(resposta.mensagemToBytes());
                            this.ds.multicast(mensagemResposta, s, null, new PointToPoint(msg.getSenderAddress()));
                            
                            break;
                    }
                } catch (ClassNotFoundException | IOException ex) {
                    Logger.getLogger(BancoWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
            
                break;
                
            case PEDIDO_PARTILHA_ESTADO:
                try {
                    Mensagem mensagemRecebida = Mensagem.mensagemFromBytes(msg.getPayload());
                    
                    switch(mensagemRecebida.getTipoMensagem()) {
                        case PEDIDO_PARTILHA_ESTADO: case PEDIDO_PARTILHA_ESTADO_INCREMENTAL:
                            //Enquanto nao receber o meu proprio pedido de transferencia de estado ignoro todas as outras mensagens
                            if(msg.getSenderAddress().equals(this.ms.getLocalAddress())) { //Caso seja a que foi enviada pelo proprio servidor
                                System.out.println("Recebi o meu proprio pedido de partilha de estado");
                                
                                this.listaMensagens = new ArrayList<>(); //Inicia a lista de mensagens
                                this.estadoServidor = EstadoServidor.MENSAGENS_EM_LISTA; //Altera o estado do servidor para mensagens em lista
                            }
                            
                            break;
                    }
                } catch (ClassNotFoundException | IOException ex) {
                    Logger.getLogger(BancoWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                break;
                
            case MENSAGENS_EM_LISTA:
                try {
                    Mensagem mensagemRecebida = Mensagem.mensagemFromBytes(msg.getPayload());
                    
                    switch(mensagemRecebida.getTipoMensagem()) {
                        case RESPOSTA_PEDIDO_PARTILHA_ESTADO:
                            System.out.println("Recebi resposta ao pedido de partilha de estado");
                            
                            //RECONSTRUIR A CLASSE CONTAS QUE É RECEBIDA NA MENSAGEM
                            //APLICAR A CLASSE CONTAS RECEBIDA À MINHA CLASSE CONTAS
                            //SE EXISTIR MENSAGENS NO ARRAYLIST EXECUTUAS E REMOVO DO ARRAYLIST
                            //PASSO O ESTADO DO SERVIDOR PARA: GRUPO_OK
                            Contas contasAux = Contas.contasFromBytes(mensagemRecebida.getObjecto());
                            Map<Integer, Conta> contas = contasAux.getContasParaTransferenciaEstado();
                            for(Integer i : contas.keySet()) { //Efetuo a partilha de estado
                                Conta c = contas.get(i);
                                this.bancoImpl.adicionarConta(c);
                            }
                            
                            if(!this.listaMensagens.isEmpty()) { //Verifico se foram colocados pedidos na lista, se foram então são efetuados
                                //Executar as mensagens do arraylist
                                //Apagar as mensagens do arraylist depois de executadas
                                executarMensagensEmLista();
                                this.listaMensagens.clear();
                            }
                            
                            this.estadoServidor = EstadoServidor.GRUPO_OK; //Com a partilha de estado efetuada, passo o estado do servidor para "grupo_ok" para poder responder a todo o tipo de pedidos
                            
                            break;
                            
                        case RESPOSTA_PEDIDO_PARTILHA_ESTADO_INCREMENTAL:
                            System.out.println("Recebi resposta ao pedido de partilha de estado incremental");
                            
                            Map<Integer, Double> contaSaldo = mensagemRecebida.getRespostaContaSaldo();
                            Map<Integer, ArrayList<String>> contaMovimentos = mensagemRecebida.getRespostaContaMovimentos();
                            
                            //Actualizar o saldo das contas. Contas novas são criadas aqui.
                            for(Integer i : contaSaldo.keySet()) {
                                this.contas.addSaldoConta(i, contaSaldo.get(i));
                            }
                            
                            //Adiciona os movimentos às contas que sofreram alterações desde a ultima utilização.
                            for(Integer i : contaMovimentos.keySet()) {
                                if(!contaMovimentos.get(i).isEmpty()) {
                                    this.contas.addMovimentosConta(i, contaMovimentos.get(i));
                                }
                            }
                            
                            //Verifico se foram colocados pedidos na lista, se foram então são efetuados
                            if(!this.listaMensagens.isEmpty()) {
                                //Executar as mensagens do arraylist
                                //Apagar as mensagens do arraylist depois de executadas
                                executarMensagensEmLista();
                                this.listaMensagens.clear();
                            }
                            
                            //Com a partilha de estado efetuada, passo o estado do servidor para "grupo_ok" para poder responder a todo o tipo de pedidos.
                            this.estadoServidor = EstadoServidor.GRUPO_OK;
                            
                            break;
                            
                        case CRIAR_CONTA: case CREDITO: case DEBITO: case TRANSFERENCIA:
                            System.out.println("A inserir mensagem na lista de espera de mensagens");
                            //Qualquer outro tipo de pedido para além da resposta ao pedido de transferencia de estado é colocado na lista de mensagens
                            this.listaMensagens.add(mensagemRecebida);
                            
                            break;
                    }
                } catch (ClassNotFoundException | IOException ex) {
                    Logger.getLogger(BancoWorker.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                break;
        }
        
        return null;
    }
    
    /* Pedido de partilha de estado */
    public void pedidoPartilhaEstado() throws GroupException, IOException {
        System.out.println("A efetuar pedido de partilha de estado");
        
        Map<Integer, Integer> ultimoMovimentoContas = this.contas.getUltimoMovimentoContas();
        //Caso tenha contas na minha base de dados então tenho de fazer o pedido enviando qual o 
        //ultimo movimento que tenho em cada conta para que possa receber de forma incremental.
        if(!ultimoMovimentoContas.isEmpty()) {
            Mensagem pedidoEstadoIncremental = new Mensagem(Mensagem.TipoMensagem.PEDIDO_PARTILHA_ESTADO_INCREMENTAL, ultimoMovimentoContas);
            Message mensagemPedidoEstadoIncremental = this.ds.createMessage();
            mensagemPedidoEstadoIncremental.setPayload(pedidoEstadoIncremental.mensagemToBytes());
            this.ds.multicast(mensagemPedidoEstadoIncremental, s, null);
        }
        //Caso não tenha contas na minha base de dados então faço o pedido "global" 
        //para que me seja enviado tudo (em vez de ser em forma incremental).
        else {
            Mensagem pedidoEstado = new Mensagem(Mensagem.TipoMensagem.PEDIDO_PARTILHA_ESTADO);
            Message mensagemPedidoEstado = this.ds.createMessage();
            mensagemPedidoEstado.setPayload(pedidoEstado.mensagemToBytes());
            this.ds.multicast(mensagemPedidoEstado, s, null);
        }
    }
    
    public void executarMensagensEmLista() throws IOException {
        Mensagem resposta;
        Message mensagemResposta;
        String mensagem;
        
        for(Mensagem m : this.listaMensagens) {
            switch(m.getTipoMensagem()) {
                case CRIAR_CONTA:
                    int novaContaIdentificador = this.bancoImpl.criarConta();
                    resposta = new Mensagem(Mensagem.TipoMensagem.CRIAR_CONTA, Mensagem.OrigemTipo.SERVIDOR, m.getIdMensagem(), "Conta: " + Integer.toString(novaContaIdentificador) + " criada com sucesso");
                    mensagemResposta = this.ds.createMessage();
                    mensagemResposta.setPayload(resposta.mensagemToBytes());
                    this.ds.multicast(mensagemResposta, s, null, new PointToPoint(m.getOrigemSocketAddress())); //Envia diretamente apenas para o cliente que fez o pedido
                    
                    break;
                    
                case CREDITO:
                    boolean creditoOk = this.bancoImpl.credito(m.getContaDestino(), m.getValor());
                    
                    if(creditoOk) mensagem = "Credito de valor: " + Double.toString(m.getValor()) + " efetuado com sucesso na conta: " + Integer.toString(m.getContaDestino());
                    else mensagem = "Nao foi possivel efetuar credito";
                    
                    resposta = new Mensagem(Mensagem.TipoMensagem.CREDITO, Mensagem.OrigemTipo.SERVIDOR, m.getIdMensagem(), mensagem);
                    mensagemResposta = this.ds.createMessage();
                    mensagemResposta.setPayload(resposta.mensagemToBytes());
                    this.ds.multicast(mensagemResposta, s, null, new PointToPoint(m.getOrigemSocketAddress())); //Envia diretamente apenas para o cliente que fez o pedido
                    
                    break;
                    
                case DEBITO:
                    boolean debitoOk = this.bancoImpl.debito(m.getContaDestino(), m.getValor());
                    
                    if(debitoOk) mensagem = "Debito de valor: " + Double.toString(m.getValor()) + " efetuado com sucesso na conta: " + Integer.toString(m.getContaDestino());
                    else mensagem = "Nao foi possivel efetuar debito";
                    
                    resposta = new Mensagem(Mensagem.TipoMensagem.DEBITO, Mensagem.OrigemTipo.SERVIDOR, m.getIdMensagem(), mensagem);
                    mensagemResposta = this.ds.createMessage();
                    mensagemResposta.setPayload(resposta.mensagemToBytes());
                    this.ds.multicast(mensagemResposta, s, null, new PointToPoint(m.getOrigemSocketAddress())); //Envia diretamente apenas para o cliente que fez o pedido
                    
                    break;
                    
                case TRANSFERENCIA:
                    boolean transferenciaOk = this.bancoImpl.transferencia(m.getContaOrigem(), m.getContaDestino(), m.getValor());
                    
                    if(transferenciaOk) mensagem = "Transferencia de valor: " + Double.toString(m.getValor()) + " efetuada com sucesso entre as contas " + Integer.toString(m.getContaOrigem()) + " e " + Integer.toString(m.getContaDestino());
                    else mensagem = "Nao foi possivel efetuar a transferencia";
                    
                    resposta = new Mensagem(Mensagem.TipoMensagem.TRANSFERENCIA, Mensagem.OrigemTipo.SERVIDOR, m.getIdMensagem(), mensagem);
                    mensagemResposta = this.ds.createMessage();
                    mensagemResposta.setPayload(resposta.mensagemToBytes());
                    this.ds.multicast(mensagemResposta, s, null, new PointToPoint(m.getOrigemSocketAddress())); //Envia diretamente apenas para o cliente que fez o pedido
                    
                    break;
                    
                case N_ULTIMOS_MOVIMENTOS:
                    String ultimosMovimentos = this.bancoImpl.listarMovimentos(m.getContaDestino(), m.getNUltimosMovimentos());
                    
                    resposta = new Mensagem(Mensagem.TipoMensagem.N_ULTIMOS_MOVIMENTOS, Mensagem.OrigemTipo.SERVIDOR, m.getIdMensagem(), ultimosMovimentos);
                    mensagemResposta = this.ds.createMessage();
                    mensagemResposta.setPayload(resposta.mensagemToBytes());
                    this.ds.multicast(mensagemResposta, s, null, new PointToPoint(m.getOrigemSocketAddress())); //Envia diretamente apenas para o cliente que fez o pedido
                    
                    break;
            }
        }
    }
    
    public synchronized void leave() throws GroupException, IOException {
        this.ms.leave();
        this.ms.close();
    }
    
    @Override
    public void onMembershipChange() {
        try {
            System.out.println("MEMBERSHIP CHANGED: " + this.ms.getMembership().getMembershipList().toString());
        } catch (InvalidStateException ex) {
            Logger.getLogger(BancoWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void onExcluded() {
        System.out.println("Member out");
    }
}
