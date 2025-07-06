package com.tekbridge.alertapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TwoSums {

    //Given an array of integers numbers
    //Given an integer target
    //return indices of the two numbers such that they add up to target
    //[2,7,11,15] target = 9;
    //Be sure that the indices are not the same
    //{3:3,2:2 }

    // 3->0 6-3 = 3
    // yes 3 is a key in our  hash map and value is 3
    //map => containsKey(String key);
    //map => containsValue(String value);
     static List<Integer> getTargetIndices(List<Integer> integerList , Integer target){
         HashMap<Integer, Integer > map = new HashMap<>();
        List<Integer> integers = new ArrayList<>();
         for(int i = 0 ; i < integerList.size() ; i++){
               map.put(integerList.get(i) , i);
         }
         System.out.println(""+map.toString());
         for(int i = 0 ; i < integerList.size();i++){
              int searchValue =  target - integerList.get(i);
              if(map.get(searchValue)!=null){
                  if(map.containsKey(searchValue) & (i != map.get(searchValue).intValue())){
                      integers.add(map.get(searchValue));
                      integers.add(i);
                      return integers;
                  }
              }
         }
       return integers;
     }

     //Implement the HashMap Class
     //Key is mapped to a value
     //HashMap is implemented with an array
     //Array Has Index and Value
     //An Issue with HashMap is Collisions
     //Incas  we generate a logic to map and has multiple value mapped to the same key we us OpenAddressing
     //Chaining


}

//To create a hashMap why need ListNode
class ListNode{
    int key;
    int val;
    ListNode next;

    public ListNode(int key , int value , ListNode next) {
        this.key = key;
        this.val =value;
        this.next= next;
    }

}

class MyHashMap {

    ArrayList<ListNode> list;
    private int size = 0;
    public MyHashMap(ArrayList<ListNode> list) {
       this.list = list;
       for(int i = 0 ; i <= 1000 ; i++){
           list.add(new ListNode(-1 , -1,null));
       }
       size = 0;
    }

    void put(int key , int value){
        int index = key % list.size();
        ListNode listNode = new ListNode(key,value,null);
        if(list.get(index)==null){
               list.add(listNode);
        }else{
            ListNode current = list.get(index);
            while(current.next!=null){
              if(current.key == key){
                  current.val = value;
                  return;
              }
              current = current.next;
            }
            if(current.key==key){
                current.val =value;
            }else {
                current.next = listNode;
            }
        }
       size++;
    }

    public int get(int key){
         int index = key % list.size();
         ListNode listNode = list.get(index);
         while (listNode!=null){
             if (listNode.key == key){
                 return listNode.val;
             }
            listNode = listNode.next;
         }
         return -1;
    }



}
