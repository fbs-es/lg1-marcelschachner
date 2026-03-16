package fbs.lg1;

import fbs.lg1.hashing.Hashing1;
import fbs.lg1.hashing.Hashing2;

public class Main {
        public static void main(String[] args) {
                Hashing1 hashing1 = new Hashing1();
                hashing1.run();

                Hashing2 hashing2 = new Hashing2();
                hashing2.run();
        }
        // @SuppressWarnings("deprecation")
        // public static void main(String[] args) {
        // QueueAndStack queueAndStack = new QueueAndStack();
        // queueAndStack.run();
        // // ArraysUeben arraysUeben = new ArraysUeben();
        // // ArraysUeben.bar();
        // // VariablenUeben variablenUeben = new VariablenUeben();
        // // variablenUeben.foo();
        // // MapsUeben mapsUeben = new MapsUeben();
        // // mapsUeben.foo();

        // // VendingMachineInventory inventory = new VendingMachineInventory();
        // // inventory.printStock();
        // // inventory.printCoinRegister();
        // // ListenUeben listenUeben = new ListenUeben();
        // // listenUeben.run();
        // // RekursionUeben rekursionUeben = new RekursionUeben();
        // // rekursionUeben.run();
        // // Bibliothek bibliothek = new Bibliothek();
        // // bibliothek.run();
        // // SortArrays sortArrays = new SortArrays();
        // // sortArrays.run();
        // ArrayList<Schueler> schuelerList = new ArrayList<>();
        // schuelerList.add(new Schueler("Mustermann", "Max", 20, new
        // java.util.Date(2003, 5, 15)));
        // schuelerList.add(new Schueler("Pinzer", "Nico", 20, new java.util.Date(2007,
        // 6, 8)));
        // schuelerList.add(new Schueler("Avdic", "Melissa", 24, new
        // java.util.Date(2002, 2, 25)));
        // schuelerList.add(new Schueler("Mustermann", "Anna", 18, new
        // java.util.Date(2001, 11, 30)));
        // schuelerList.add(new Schueler("Mustermann", "Lisa", 34, new
        // java.util.Date(2000, 1, 10)));

        // Collections.sort(schuelerList);
        // System.out.println("Sortiert nach Alter:");
        // schuelerList.forEach(s -> System.out
        // .println(s.getName() + " " + s.getVorname() + " " + s.getAlter() + " " +
        // s.getGeburtsdatum()));

        // Collections.sort(schuelerList, new
        // fbs.lg1.suchensortieren.VergleichSchuelerBeiNamen());
        // System.out.println();
        // System.out.println("Sortiert nach Namen:");
        // schuelerList.forEach(s -> System.out
        // .println(s.getName() + " " + s.getVorname() + " " + s.getAlter() + " " +
        // s.getGeburtsdatum()));

        // System.out.println();
        // System.out.println("Sortiert nach Nachname und Vorname:");
        // // thenComparing
        // Collections.sort(schuelerList,
        // Comparator.comparing(Schueler::getName).thenComparing(Schueler::getVorname));
        // schuelerList.forEach(s -> System.out
        // .println(s.getName() + " " + s.getVorname() + " " + s.getAlter() + " " +
        // s.getGeburtsdatum()));

        // }
}
