/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.trabalho_sdc_cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import net.sf.jgcs.GroupException;

/**
 *
 * @author Miguel
 */
public class Cliente {
    public static void main(String[] args) throws InterruptedException, GroupException, IOException {
        BancoStub cliente = new BancoStub();
        
        for(int i = 0; i < 150; i++) {
            cliente.criarConta();
            for(int j = 0; j < 5; j++) {
                cliente.credito(i, 10);
            }
        }
        
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        while(true){
            System.out.println("\nOpcoes: criarconta | credito | debito | transferencia | movimentos | quit");
            System.out.print("-> ");
            
            String l = br.readLine();
            if(l.equals("quit") || l.equals("q")) {
                cliente.leaveGroup();
                break;
            }
            if(l.equals("criarconta")) {
                cliente.criarConta();
            }
            if(l.equals("credito")) {
                System.out.print("Conta destino: ");
                String conta = br.readLine();
                System.out.print("Valor a creditar: ");
                String valor = br.readLine();
                cliente.credito(Integer.parseInt(conta), Double.parseDouble(valor));
            }
            if(l.equals("debito")) {
                System.out.print("Conta destino: ");
                String conta = br.readLine();
                System.out.print("Valor a debitar: ");
                String valor = br.readLine();
                cliente.debito(Integer.parseInt(conta), Double.parseDouble(valor));
            }
            if(l.equals("transferencia")) {
                System.out.print("Conta origem: ");
                String contaOrigem = br.readLine();
                System.out.print("Conta destino: ");
                String contaDestino = br.readLine();
                System.out.print("Valor a transferir: ");
                String valor = br.readLine();
                cliente.transferencia(Integer.parseInt(contaOrigem), Integer.parseInt(contaDestino), Double.parseDouble(valor));
            }
            if(l.equals("movimentos")) {
                System.out.print("Conta: ");
                String conta = br.readLine();
                System.out.print("Numero de movimentos a apresentar: ");
                String nMovimentos = br.readLine();
                cliente.listarMovimentos(Integer.parseInt(conta), Integer.parseInt(nMovimentos));
            }
        }
    }
}
