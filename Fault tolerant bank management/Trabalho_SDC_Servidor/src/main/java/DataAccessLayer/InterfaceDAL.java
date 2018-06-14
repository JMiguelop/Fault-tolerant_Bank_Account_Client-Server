/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DataAccessLayer;

import java.util.ArrayList;

/**
 *
 * @param <T>
 */

public interface InterfaceDAL<T> {
    public ArrayList<T> getAll();
    public ArrayList<T> getAll(String whereClause);
    public T get(int key);
    public Boolean set(T data, Boolean isNew);
    public Boolean delete(int key);
}
