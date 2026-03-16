package fbs.lg1;

public class ArraysUeben {
    public void run() {
        int[] iarray;
        iarray = new int[4];

        for (int element : iarray) {
            System.out.print(element + " ");
        }
        for (int i = 0; i < iarray.length; i++) {
            iarray[i] = i + 1;
        }
        for (int i = iarray.length - 1; i >= 0; i--) {
            System.out.print(iarray[i] + " ");
        }
        String[] sarray;
        sarray = new String[4];

        sarray[0] = new String("Alexander");
        sarray[1] = new String("Melissa");
        sarray[2] = new String("Marcel");
        sarray[3] = new String("Jasmin");

        for (String element : sarray) {
            System.out.print(element + " ");
        }

        int[][] secondTwoDimensionalArray;
        secondTwoDimensionalArray = new int[2][2];
        int[][][] threeDimensionalArray;
        threeDimensionalArray = new int[2][][];

        for (int i = 0; i < threeDimensionalArray.length; i++) {
            threeDimensionalArray[i] = new int[2][];
            for (int j = 0; j < threeDimensionalArray[i].length; j++) {
                for (int k = 0; k < threeDimensionalArray[i][j].length; k++) {
                    threeDimensionalArray[i][j][k] = i + j + k;
                }
            }
        }
    }

    public void bar() {
        int[] arr01 = { 0, 1, 2, 3 };
        for (int i = 0; i < arr01.length; i++) {
            System.out.print(arr01[i]);
        }
        for (int element : arr01) {
            System.out.print(element);
        }
        String[] arr02 = { "Hallo", "Welt" };
        String[] arr03 = new String[2];
        arr03[0] = "Hallo";
        arr03[1] = "Welt";
        int[][] arr04 = new int[2][2];
        arr04[0][0] = 0;
        arr04[0][1] = 1;
        arr04[1][0] = 0;
        arr04[1][1] = 2;
        int[][] arr05 = new int[2][2];
        for (int i = 0; i < arr05.length; i++) {
            for (int j = 0; j < arr05[i].length; j++) {
                arr05[i][j] = i + j;
            }
        }
        int[][] arr06 = new int[2][];
        arr06[0] = new int[2];
        arr06[1] = new int[2];

        int[][][] arr07 = new int[2][][];
        for (int i = 0; i < arr07.length; i++) {
            arr07[i] = new int[2][];
            for (int j = 0; j < arr07[i].length; j++) {
                arr07[i][j] = new int[2];
            }
        }
    }
}
