package no.nicolay.boligregnskap.util;

import java.math.BigDecimal;

public final class SkatteUtil {

    private SkatteUtil() {
    }

    /**
     * Grovt estimat: 22 % skatt på positivt overskudd (utleie av sekundærbolig).
     * NB: forenklet – ikke en faktisk skatteberegning.
     */
    public static BigDecimal estimertSkatt(BigDecimal overskudd) {
        return overskudd.signum() > 0
                ? overskudd.multiply(new BigDecimal("0.22"))
                : BigDecimal.ZERO;
    }
}
