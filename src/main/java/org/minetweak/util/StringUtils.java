package org.minetweak.util;

public class StringUtils {
    public static String[] dropFirstString(String[] par0ArrayOfStr) {
        String[] var1 = new String[par0ArrayOfStr.length - 1];
        System.arraycopy(par0ArrayOfStr, 1, var1, 0, par0ArrayOfStr.length - 1);
        return var1;
    }

    public static String toString(String[] array) {
        StringBuilder builder = new StringBuilder();
        int count = 1;
        for (String string : array) {
            count++;
            if (count==array.length) {
                builder.append(string);
            } else {
                builder.append(string).append(" ");
            }
        }
        return builder.toString();
    }
}
