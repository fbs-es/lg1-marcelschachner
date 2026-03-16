package fbs.lg1;

import java.util.LinkedList;

public class QueueAndStack {
  public void run() {
    LinkedList<String> stack = new LinkedList<>();
    stack.push("Hello");
    System.out.println("Stack top: " + stack.peek());
    stack.pop();
    System.out.println("Stack is empty: " + stack.isEmpty());

    stack.push("World");
    System.out.println("Stack top: " + stack.peek());

    stack.pop();
    System.out.println("Stack is empty: " + stack.isEmpty());

    System.out.println("---------");
    System.out.println("Using LinkedList as a stack:");
    stack.addFirst("First");
    System.out.println("Stack top: " + stack.getFirst());
    stack.removeFirst();
    System.out.println("Stack is empty: " + stack.isEmpty());

    // queue bauen mit offerFirst/offerLast
    LinkedList<String> queue = new LinkedList<>();
    queue.offerLast("First");
    System.out.println("Queue front: " + queue.peekFirst());
    queue.pollFirst();
    System.out.println("Queue is empty: " + queue.isEmpty());
    queue.offerLast("Second");
    System.out.println("Queue front: " + queue.peekFirst());
    queue.pollFirst();
    System.out.println("Queue is empty: " + queue.isEmpty());
  }
}
