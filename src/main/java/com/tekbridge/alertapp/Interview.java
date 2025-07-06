package com.tekbridge.alertapp;

import org.springframework.core.codec.StringDecoder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class Interview {

    List<String> strings = new ArrayList<>();
   static void test(List<String> strings ){
        strings.add("Joseph");
        strings.add("Joseph");
        strings.add("Mary");
       HashMap<String , Integer> checkingMap = new HashMap<>();
        for(int i = 0; i<strings.size();i++){
            String key = strings.get(i);
            Integer index = i;
               if(!checkingMap.containsKey(key)) {
                   checkingMap.put(key, index);
               }
        }
       System.out.println(getMap(checkingMap));
    }

    static List<String> getMap(HashMap<String ,Integer> inputNoneDuplicate){
        List<String> keys = new ArrayList<>();
        Set<String> key = inputNoneDuplicate.keySet();
        for(String keyInset : key){
            String upperCaseConverted = keyInset.toUpperCase();
            keys.add(upperCaseConverted);
        }
        return keys;
    }



}

