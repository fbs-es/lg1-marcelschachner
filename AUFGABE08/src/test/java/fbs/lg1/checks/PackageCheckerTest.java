package fbs.lg1.checks;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import fbs.lg1.Package;

public class PackageCheckerTest {

    private final PackageChecker checker = new PackageChecker();

    @Test
    void testDangerous() {
        assertEquals(PackageResult.DANGEROUS, checker.check(new Package(10, 50, 30, 30, false, true)));
    }

    @Test
    void testTooHeavy() {
        assertEquals(PackageResult.TOO_HEAVY, checker.check(new Package(40, 50, 30, 30, false, false)));
    }

    @Test
    void testTooBig() {
        assertEquals(PackageResult.TOO_BIG, checker.check(new Package(10, 130, 30, 30, false, false)));
    }

    @Test
    void testTooBigAndTooHeavy() {
        assertEquals(PackageResult.TOO_HEAVY_AND_TOO_BIG, checker.check(new Package(40, 130, 30, 30, false, false)));
    }

    @Test
    void testAccepted() {
        assertEquals(PackageResult.ACCEPTED, checker.check(new Package(10, 50, 30, 30, false, false)));
    }

    @Test
    void testAcceptedFragile() {
        assertEquals(PackageResult.ACCEPTED, checker.check(new Package(10, 50, 30, 30, true, false)));
    }
}
