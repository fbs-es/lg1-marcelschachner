package fbs.lg1.listen;

import java.util.ArrayList;
import java.util.LinkedList;

public class ListenUeben {
  @SuppressWarnings("removal")
  public void run() {
    ArrayList<Character> list = new ArrayList<>();
    list.add('D');
    list.add('C');
    list.add('B');
    list.add('A');
    list.forEach(c -> System.out.println(c));
    System.out.println("----");
    for (Character c : list) {
      System.out.println(c);
    }
    System.out.println("----");
    ArrayList<Character> list2 = new ArrayList<>();
    list2.add('D');
    list2.add('B');
    list2.add('A');
    list2.add(1, 'C');
    list2.forEach(c -> System.out.println(c));
    System.out.println("----");

    ArrayList<Character> list3 = new ArrayList<>();
    list3.addFirst('A');
    list3.addFirst('B');
    list3.addFirst('C');
    list3.addFirst('D');
    list3.forEach(c -> System.out.println(c));
    ArrayList<Character> list4 = new ArrayList<>();
    System.out.println("----");

    list4.addFirst('D');
    list4.addFirst('C');
    list4.addFirst('B');
    list4.addFirst('A');
    list4.forEach(c -> System.out.println(c));

    ArrayList<Character> list5 = new ArrayList<>();
    System.out.println("----");
    list5.addFirst('B');
    list5.addFirst('C');
    list5.addFirst('D');
    list5.addLast('A');
    list5.forEach(c -> System.out.println(c));

    ArrayList<Character> list6 = new ArrayList<>();
    System.out.println("----");
    list6.addFirst('A');
    list6.addFirst('B');
    list6.addFirst('X');
    list6.addFirst('C');
    list6.addFirst('D');
    list6.forEach(c -> System.out.println(c));
    System.out.println("Contains 'X': " + list6.contains('X'));
    System.out.println("Index of 'X': " + list6.indexOf('X'));
    list6.remove(2);
    list6.forEach(c -> System.out.println(c));

    ArrayList<Character> list7 = new ArrayList<>();
    System.out.println("----");
    list7.addFirst('A');
    list7.addFirst('B');
    list7.addFirst('X');
    list7.addFirst('X');
    list7.addFirst('C');
    list7.addFirst('D');
    list7.remove(new Character('X'));
    list7.forEach(c -> System.out.println(c));
    System.out.println("----");
    LinkedList<Character> stack = new LinkedList<>();
    stack.push('D');
    stack.push('C');
    stack.push('B');
    stack.push('A');
    stack.forEach(c -> System.out.println(c));
    System.out.println("Top of stack: " + stack.peek());
    stack.forEach(c -> System.out.println(c));
    System.out.println("Popping top of stack...");
    stack.pop();
    stack.forEach(c -> System.out.println(c));
  }

  // public void run() {
  // Node head = null;
  // head = new Node('D');
  // Node k = new Node('C');
  // k.next = head;
  // head = k;
  // k = new Node('B');
  // k.next = head;
  // head = k;
  // g k = new Node('A');
  // k.next = head;
  // head = k;
  // while (head != null) {
  // System.out.println(head.value);
  // head = head.next;
  // }

  // LinkedList list = new LinkedList();
  // list.add('D');
  // list.add('C');
  // list.add('B');
  // list.add('A');
  // for (Object o : list) {
  // System.out.println(o);
  // }
  // list = new LinkedList();
  // list.addFirst('D');
  // list.addFirst('C');
  // list.addFirst('B');
  // list.addFirst('A');
  // for (Object o : list) {
  // System.out.println(o);
  // }
  // System.out.println("forEach:");
  // list.forEach(o -> System.out.println(o));
  // }
}
