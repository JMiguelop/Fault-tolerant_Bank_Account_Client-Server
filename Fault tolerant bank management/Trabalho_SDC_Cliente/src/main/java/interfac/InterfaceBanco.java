/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package interfac;

/**
 *
 * @author Miguel
 */
public interface InterfaceBanco {
    //É igual à interface do lado do servidor
    public int criarConta();
    public boolean credito(int conta, double valor);
    public boolean debito(int conta, double valor);
    public boolean transferencia(int contaOrigem, int contaDestino, double valor);
    public String listarMovimentos(int conta, int nMovimentos);
}
