/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

/**
 *
 * @author cdoersch
 */
public interface DocStore {
    public Document nextDoc();
    public int getDocCount();
    public void reset();
}
