package fbs.lg1;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

public class MapsUeben {
    public void foo() {
        // HashMap<Integer, String> telefonBuch = new HashMap<>();
        // telefonBuch.put(123456789, "Marcel");
        // telefonBuch.put(987654321, "Lisa");
        // telefonBuch.put(555555555, "Max");
        // for (Integer key : telefonBuch.keySet()) {
        // System.out.println("Key: " + key + ", Value: " + telefonBuch.get(key));
        // }
        // if (telefonBuch.containsKey(123456789)) {
        // System.out.println("Marcel's number is: " + telefonBuch.get(123456789));
        // telefonBuch.remove(123456789);
        // }
        // System.out.println("Marcel's number is: " + telefonBuch.get(123456789));
        HashMap<String, Integer> scores = new HashMap<>();
        scores.put("Marcel", 100);
        System.out.println("Marcel's score: " + scores.get("Marcel"));
        if (scores.containsKey("Marcel")) {
            int marcelScore = scores.get("Marcel");
            marcelScore += 1;
            scores.put("Marcel", marcelScore);
        }
        System.out.println("Marcel's new score: " + scores.get("Marcel"));
        scores.computeIfPresent("Marcel", (key, value) -> value + 1);
        System.out.println("Marcel's new score 2: " + scores.get("Marcel"));

    }

    public void bar() {
        // Programm, das ein dreidimensionales Array mit ganzzahligen Zufallszahlen im
        // Bereich von 1 bis 10 befüllt, und anschließend zählt wie oft jede Zahl
        // vorkommt

        int[][] cube = new int[5][5];
        Random random = new Random();
        HashMap<Integer, Integer> vorkommen = new HashMap<>();
        for (int i = 0; i < cube.length; i++) {
            for (int j = 0; j < cube[i].length; j++) {
                cube[i][j] = random.nextInt(10) + 1;
            }
        }
        System.out.println("Arrays" + Arrays.deepToString(cube));

        for (int i = 0; i < cube.length; i++) {
            for (int j = 0; j < cube[i].length; j++) {
                vorkommen.merge(cube[i][j], 1, Integer::sum);
            }
        }
        System.out.println("Vorkommen: " + vorkommen);
        vorkommen.forEach((key, value) -> System.out.println("Zahl: " + key + ", Vorkommen: " + value));
        // Arrays[
        // [4, 3, 8, 6, 8],
        // [8, 1, 2, 7, 3],
        // [4, 6, 7, 4, 10],
        // [7, 10, 5, 3, 3],
        // [4, 9, 2, 6, 6]]
        // Vorkommen: {1=1, 2=2, 3=4, 4=4, 5=1, 6=4, 7=3, 8=3, 9=1, 10=2}
        // Zahl: 1, Vorkommen: 1
        // Zahl: 2, Vorkommen: 2
        // Zahl: 3, Vorkommen: 4
        // ...
    }
}
