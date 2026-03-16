package fbs.lg1;

import java.util.Arrays;

public class VariableUeben {
    private String name = "Hallo Klasse 1APP";
    private int alter = 22;

    public void foo() {
        int[][] matrix = { { 1, 2 }, { 3, 4 } };
        int[] zahlen = { 1, 2, 3, 4, 5 };
        String name = "Hallo Matthias";
        System.out.println("lokale Variable: " + name);
        System.out.println("Instanzvariable: " + this.name);
        System.out.println("Instanzvariable Alter: " + this.alter);
        System.out.println("Array (toString): " + Arrays.toString(zahlen));
        bar(zahlen);
        System.out.println("Array nach Methode bar (toString): " +
                Arrays.toString(zahlen));
        System.out.println("Matrix (toString): " + Arrays.deepToString(matrix));
        baz(matrix);
        foobar();
        int[][][] dreiDMatrix = { { { 1, 2 }, { 3, 4 } }, { { 5, 6 }, { 7, 8 } } };
        System.out.println("3D Matrix (toString): " +
                Arrays.deepToString(dreiDMatrix));
        int[][][][] vierDMatrix = { { { { 1, 2 }, { 3, 4 } }, { { 5, 6 }, { 7, 8 } }
        },
                { { { 9, 10 }, { 11, 12 } }, { { 13, 14 }, { 15, 16 } } } };

    }

    public void bar(int[] parameterZahlen) {
        int[] z = parameterZahlen;
        System.out.println("Parameter Array (toString): " +
                Arrays.toString(parameterZahlen));
        parameterZahlen[2] = 10;
        System.out.println("Parameter Array nach Änderung (toString): " +
                Arrays.toString(parameterZahlen));
        System.out.println("Lokale Variable z (toString): " + Arrays.toString(z));
        z = new int[] { 6, 7, 8, 9, 10 };
        System.out.println("Lokale Variable z nach Neuzuweisung (toString): " +
                Arrays.toString(z));
        parameterZahlen[3] = 20;
        System.out.println("Parameter Array nach Neuzuweisung (toString): " +
                Arrays.toString(parameterZahlen));
    }

    public void baz(int[][] matrix) {
        System.out.println("Matrix im baz (toString): " +
                Arrays.deepToString(matrix));
        matrix[0][0] = 99;
        System.out.println("Matrix nach Änderung im baz (toString): " +
                Arrays.deepToString(matrix));
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[i].length; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println();
        }
    }

    public void foobar() {
        String name = "Hallo Foobar";
        {
            System.out.println("Innerer Block: " + name);
            String name1 = "Hallo Kilian";
            System.out.println("Innerer Block (name1): " + name1);
        }
        System.out.println("Innerer Block (name1): " + name);
        {
            System.out.println(name);
            System.out.println(this.name);
        }
    }

}
