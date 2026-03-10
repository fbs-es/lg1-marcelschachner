package fbs.lg1;

public class Package {
    public double weight;
    public double length;
    public double width;
    public double height;
    public boolean fragile;
    public boolean dangerous;

    public Package(double weight, double length, double width, double height, boolean fragile, boolean dangerous) {
        this.weight = weight;
        this.length = length;
        this.width = width;
        this.height = height;
        this.fragile = fragile;
        this.dangerous = dangerous;
    }
}
