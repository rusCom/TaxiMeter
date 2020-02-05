package org.toptaxi.taximeter.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainUtils {
    public static Double round(Double d, int precise) {
        BigDecimal bigDecimal = new BigDecimal(d);
        bigDecimal = bigDecimal.setScale(precise, RoundingMode.HALF_UP);
        return bigDecimal.doubleValue();
    }
}
