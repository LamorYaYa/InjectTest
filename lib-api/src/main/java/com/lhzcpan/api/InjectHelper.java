package com.lhzcpan.api;

import android.app.Activity;

import java.lang.reflect.Constructor;

/**
 * @author master
 * @date 2017/12/25
 */

public class InjectHelper {

    public static void init(Activity host) {
        String classFullName = host.getClass().getName() + "$$ViewInjector";
        try{
            Class proxy = Class.forName(classFullName);
            Constructor constructor = proxy.getConstructor(host.getClass());
            constructor.newInstance(host);
        }catch (Exception e){
        }
    }


}
