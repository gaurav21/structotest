/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.structotest;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 *
 * @author gauravsharma
 */
public class TestAsyncController {
    


public static void main(String[] args) {
    Integer[] numbers = { 0, 1, 2, 3, 4, 5 };

    Observable numberObservable = Observable.from(numbers);    
    numberObservable.subscribe(
        new Action1<Integer>() {
            @Override
            public void call(Integer incomingNumber) {
                System.out.println(incomingNumber);
            }
        },
        new Action1<Throwable>() {
            @Override
            public void call(Throwable error) {
                System.out.println("Error in synchronous observable");
            }
        },
        new Action0() {
            @Override
            public void call() {
                System.out.println("This observable is finished");
            }

        }
);
}



    
}
