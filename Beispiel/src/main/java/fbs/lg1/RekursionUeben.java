// package fbs.lg1;

// public class RekursionUeben {
// public void run() {
// int n = 5;
// // System.out.println(faktorielle(n));
// // // System.out.println(treppen(n));
// // System.out.println(iterativeFaktorielle(n));
// // System.out.println(fibonacci(n));
// // System.out.println(invertString("abc"));
// // invertString("abc");
// // }

// // zuerst a auf dem stack
// // dann zweiter call, a dann b - string ist bc
// // dann dritter call, a dann c - string ist c
// // leer string mit a b c auf dem stack. rekursion auflösen. print c, dann
// print
// // b, dann print a
// // public String invertString(String s) {
// // if (s.isEmpty()) {
// // return s;
// // else {
// // firstChar = s.charAt(0);
// // return invertString(s.substring(1)) + firstChar;
// // }

// // public void invertString(String text) {
// // if (text.isEmpty()) {
// // return;
// // }
// // char erstesZeichen = text.charAt(0);
// // String restText = text.substring(1);
// // invertString(restText);
// // System.out.print(erstesZeichen);
// // }

// // public int faktorielle(int n) {
// // if (n > 0) {
// // return n * faktorielle(n - 1);
// // else {
// // return 1;
// //
// //

// // public int iterativeFaktorielle(int n) {
// // int result = 1;
// // for (int i = 1; i <= n; i++) {
// // result *= i;
// // System.out.println("Zwischenergebnis: " + result);
// //
// // return result;
// //

// // public int fibonacci(int n) {
// // if (n == 0) {
// // return 0;
// // else if (n == 1) {
// // return 1;
// // else {
// // return fibonacci(n - 1) + fibonacci(n - 2);
// //
// //
// // man geht einmal ganz durch und dann rechnet er erst das zweite komplett
// durch

// // public int treppen(int n) {
// // if (n > 0) {
// // treppen(n - 1);
// // for (int i = 0; i < n; i++) {
// // System.out.print("8");
// // }
// // System.out.println();
// // }
// // return 0;
// // }
