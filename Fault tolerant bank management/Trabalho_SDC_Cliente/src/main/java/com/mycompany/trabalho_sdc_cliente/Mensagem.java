/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.trabalho_sdc_cliente;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

/**
 *
 * @author Miguel
 */
public class Mensagem implements Serializable {
    public enum TipoMensagem {CRIAR_CONTA, CREDITO, DEBITO, TRANSFERENCIA, N_ULTIMOS_MOVIMENTOS, PEDIDO_PARTILHA_ESTADO, RESPOSTA_PEDIDO_PARTILHA_ESTADO, PEDIDO_PARTILHA_ESTADO_INCREMENTAL, RESPOSTA_PEDIDO_PARTILHA_ESTADO_INCREMENTAL};
    public enum OrigemTipo {CLIENTE, SERVIDOR};
    
    private SocketAddress origem;
    private int idMensagem;
    private final TipoMensagem tipoMensagem;
    private OrigemTipo origemTipo;
    private int contaOrigem;
    private int contaDestino;
    private double valor;
    private int n_ultimos_movimentos;
    private String mensagemAssociada;
    private byte[] objecto;
    private Map<Integer, Integer> numeroUltimosMovimentosContas;
    private Map<Integer, Double> respostaContaSaldo;
    private Map<Integer, ArrayList<String>> respostaContaMovimentos;
    
    
    
    /* Construtores */
    /* Tipo de construtor utilizado pelos CLIENTES para CRIAR CONTAS */
    public Mensagem(TipoMensagem tipoMensagem, OrigemTipo origemTipo, SocketAddress origem, int idMensagem) {
        this.tipoMensagem = tipoMensagem;
        this.origemTipo = origemTipo;
        this.origem = origem;
        this.idMensagem = idMensagem;
    }
    
    /* Tipo de construtor utilizado pelos CLIENTES para operações de CRÉDITO / DÉBITO */
    public Mensagem(TipoMensagem tipoMensagem, OrigemTipo origemTipo, SocketAddress origem, int idMensagem, int contaDestino, double valor) {
        this.tipoMensagem = tipoMensagem;
        this.origemTipo = origemTipo;
        this.origem = origem;
        this.idMensagem = idMensagem;
        this.contaDestino = contaDestino;
        this.valor = valor;
    }
    
    /* Tipo de construtor utilizado pelos CLIENTES para operações de TRANSFERENCIA */
    public Mensagem(TipoMensagem tipoMensagem, OrigemTipo origemTipo, SocketAddress origem, int idMensagem, int contaOrigem, int contaDestino, double valor) {
        this.tipoMensagem = tipoMensagem;
        this.origemTipo = origemTipo;
        this.origem = origem;
        this.idMensagem = idMensagem;
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
        this.valor = valor;
    }
    
    /* Tipo de construtor utilizado pelos CLIENTES para operações de ver os ULTIMOS N MOVIMENTOS */
    public Mensagem(TipoMensagem tipoMensagem, OrigemTipo origemTipo, SocketAddress origem, int idMensagem, int contaDestino, int n_ultimos_movimentos) {
        this.tipoMensagem = tipoMensagem;
        this.origemTipo = origemTipo;
        this.origem = origem;
        this.idMensagem = idMensagem;
        this.contaDestino = contaDestino;
        this.n_ultimos_movimentos = n_ultimos_movimentos;
    }
    
    /* Tipo de construtor utilizado pelos SERVIDORES para responder a pedidos de CRIAR CONTAS / CREDITO / DEBITO / TRANSFERENCIA */
    public Mensagem(TipoMensagem tipoMensagem, OrigemTipo origemTipo, int idMensagem, String mensagemAssociada) {
        this.tipoMensagem = tipoMensagem;
        this.origemTipo = origemTipo;
        this.idMensagem = idMensagem;
        this.mensagemAssociada = mensagemAssociada;
    }
    
    /* Tipo de construtor utilizado pelos SERVIDORES para fazer o pedido de TRANSFERENCIA DE ESTADO */
    public Mensagem(TipoMensagem tipoMensagem) {
        this.tipoMensagem = tipoMensagem;
    }
    
    /* Tipo de construtor utilizado pelos SERVIDORES para fazer o pedido de TRANSFERENCIA DE ESTADO INCREMENTAL */
    public Mensagem(TipoMensagem tipoMensagem, Map<Integer, Integer> numeroUltimosMovimentosContas) {
        this.tipoMensagem = tipoMensagem;
        this.numeroUltimosMovimentosContas = numeroUltimosMovimentosContas;
    }
    
    /* Tipo de construtor utilizado pelos SERVIDORES para resposta ao pedido de TRANSFERENCIA DE ESTADO efetuado por outro SERVIDOR */
    public Mensagem(TipoMensagem tipoMensagem, byte[] objecto) {
        this.tipoMensagem = tipoMensagem;
        this.objecto = Arrays.copyOf(objecto, objecto.length);
    }
    
    /* Tipo de construtor utilizado pelos SERVIDORES para resposta ao pedido de TRANSFERENCIA DE ESTADO INCREMENTAL efetuado por outro SERVIDOR */
    public Mensagem(TipoMensagem tipoMensagem, Map<Integer, Double> respostaContaSaldo, Map<Integer, ArrayList<String>> respostaContaMovimentos) {
        this.tipoMensagem = tipoMensagem;
        this.respostaContaSaldo = respostaContaSaldo;
        this.respostaContaMovimentos = respostaContaMovimentos;
    }
    
    /* Get's */
    public SocketAddress getOrigemSocketAddress() {
        return this.origem;
    }
    
    public int getIdMensagem() {
        return this.idMensagem;
    }
    
    public TipoMensagem getTipoMensagem() {
        return this.tipoMensagem;
    }
    
    public OrigemTipo getOrigemTipo() {
        return this.origemTipo;
    }
    
    public int getContaOrigem() {
        return this.contaOrigem;
    }
    
    public int getContaDestino() {
        return this.contaDestino;
    }
    
    public double getValor() {
        return this.valor;
    }
    
    public int getNUltimosMovimentos() {
        return this.n_ultimos_movimentos;
    }
    
    public String getMensagemAssociada() {
        return this.mensagemAssociada;
    }
    
    public byte[] getObjecto() {
        return this.objecto;
    }
    
    public Map<Integer, Integer> getNumeroUltimosMovimentosContas() {
        return this.numeroUltimosMovimentosContas;
    }
    
    public Map<Integer, Double> getRespostaContaSaldo() {
        return this.respostaContaSaldo;
    }
    
    public Map<Integer, ArrayList<String>> getRespostaContaMovimentos() {
        return this.respostaContaMovimentos;
    }
    
    /* Métodos */
    public byte[] mensagemToBytes() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(baos);
        oo.writeObject(this);
        return baos.toByteArray();
    }
    
    public static Mensagem mensagemFromBytes(byte[] bytesMensagem) throws ClassNotFoundException, IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytesMensagem);
        ObjectInput oi = new ObjectInputStream(bais);
        return (Mensagem) oi.readObject();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if(this.mensagemAssociada != null) sb.append(this.mensagemAssociada);
        
        return sb.toString();
    }
}

