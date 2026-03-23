package fbs.lg1.checks;

import fbs.lg1.Package;

public class PackageChecker {

    static final double MAX_WEIGHT = 31.5;
    static final double MAX_LENGTH = 120.0;
    static final double MAX_WIDTH = 60.0;
    static final double MAX_HEIGHT = 60.0;

    public PackageResult check(Package pkg) {
        if (pkg.dangerous)
            return PackageResult.DANGEROUS;

        boolean tooHeavy = pkg.weight >= MAX_WEIGHT;
        boolean tooBig = pkg.length >= MAX_LENGTH || pkg.width >= MAX_WIDTH || pkg.height >= MAX_HEIGHT;

        if (tooHeavy && tooBig)
            return PackageResult.TOO_HEAVY_AND_TOO_BIG;
        if (tooHeavy)
            return PackageResult.TOO_HEAVY;
        if (tooBig)
            return PackageResult.TOO_BIG;
        return PackageResult.ACCEPTED;
    }
}
