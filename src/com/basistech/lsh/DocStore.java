/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.basistech.lsh;

/**
 *
 * @author cdoersch
 */
//TODO: this interface should extend the Iterable<Document> interface, since
//a single DocStore is often used multiple times by multiple modules.
public interface DocStore {
    public Document nextDoc();
    public int getDocCount();
    public void reset();
}
