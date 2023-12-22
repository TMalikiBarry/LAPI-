package sn.intouch.gu.lonaciapi.ejb.utils;

import java.text.NumberFormat;

public class Utils {
    public static String formatLabelAmount(Double amount) {
        NumberFormat format;
        if (amount == null)
            return "0";

        format = NumberFormat.getIntegerInstance();
        format.setGroupingUsed(false);
        format.setMaximumFractionDigits(2);
        return format.format(amount);
    }

    public static void main(String[] args) {
        System.out.println(formatLabelAmount(Double.valueOf("3.4739299E7")));
    }
}
