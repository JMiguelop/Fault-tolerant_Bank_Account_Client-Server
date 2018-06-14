/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.trabalho_sdc_servidor;

import dados.Contas;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 *
 * @author Miguel
 */
public class Banco {
    private static int primeiroServidor; // 0 caso seja o PRIMEIRO servidor; 1 caso NÃO seja o PRIMEIRO servidor
    
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        
        System.out.print("Primeiro Servidor? [s/n] ");
        String firstServer = br.readLine();
        if(firstServer.equals("s")) {
            System.out.println("A iniciar primeiro servidor...");
            primeiroServidor = 0;
        } else if(firstServer.equals("n")) {
            System.out.println("A iniciar servidor...");
            primeiroServidor = 1;
        }
        System.out.print("Nome servidor: ");
        String name = br.readLine();
        
        Contas contas = new Contas(name); //Base de dados
        
        BancoImpl bancoImpl = new BancoImpl(contas);
        
        BancoWorker bw = new BancoWorker(bancoImpl, contas, primeiroServidor);
        
        if(primeiroServidor == 1) bw.pedidoPartilhaEstado(); //Caso nao seja o primeiro servidor vai fazer o pedido de partilha de estado !!!
        
        while(true){
            System.out.println("\nOpcoes: vercontas | quit");
            System.out.print("-> ");
            
            String l = br.readLine();
            if(l.equals("vercontas")) {
                System.out.println(contas.toString());
            }
            if(l.equals("quit") || l.equals("q")) {
                bw.leave();
                break;
            }
        }
    }
}
