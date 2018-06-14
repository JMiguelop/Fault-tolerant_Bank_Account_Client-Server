/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.trabalho_sdc_servidor;

import dados.Conta;
import dados.Contas;
import interfac.InterfaceBanco;
import java.util.ArrayList;

/**
 *
 * @author Miguel
 */
public class BancoImpl implements InterfaceBanco {
    private Contas contas;
    
    
    
    /* Construtor */
    public BancoImpl(Contas c) {
        this.contas = c;
    }

    /* Metodos */
    @Override
    public synchronized int criarConta() {
        return this.contas.addConta();
    }

    @Override
    public synchronized boolean credito(int conta, double valor) {
        return this.contas.credito(conta, valor);
    }

    @Override
    public synchronized boolean debito(int conta, double valor) {
        return this.contas.debito(conta, valor);
    }

    @Override
    public synchronized boolean transferencia(int contaOrigem, int contaDestino, double valor) {
        return this.contas.transferencia(contaOrigem, contaDestino, valor);
    }
    
    @Override
    public synchronized String listarMovimentos(int contaDestino, int nMovimentos) {
        return this.contas.listarMovimentos(contaDestino, nMovimentos);
    }
    
    public synchronized void adicionarConta(Conta c) {
        this.contas.addConta(c);
    }
}
