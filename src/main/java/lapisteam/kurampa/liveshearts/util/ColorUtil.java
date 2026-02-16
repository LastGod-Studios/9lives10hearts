package lapisteam.kurampa.liveshearts.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ColorUtil {

    private ColorUtil() {}

    private static final char COLOR_CHAR = '§';
    private static final Pattern HEX = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String translateHex(String msg) {
        if (msg == null) return null;

        Matcher m = HEX.matcher(msg);
        StringBuffer out = new StringBuffer();

        while (m.find()) {
            String rgb = m.group(1);
            String replace = COLOR_CHAR + "x"
                    + COLOR_CHAR + rgb.charAt(0)
                    + COLOR_CHAR + rgb.charAt(1)
                    + COLOR_CHAR + rgb.charAt(2)
                    + COLOR_CHAR + rgb.charAt(3)
                    + COLOR_CHAR + rgb.charAt(4)
                    + COLOR_CHAR + rgb.charAt(5);
            m.appendReplacement(out, replace);
        }
        m.appendTail(out);

        // & -> §
        char[] c = out.toString().toCharArray();
        for (int i = 0; i < c.length - 1; i++) {
            if (c[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(c[i + 1]) >= 0) {
                c[i] = COLOR_CHAR;
                c[i + 1] = Character.toLowerCase(c[i + 1]);
            }
        }
        return new String(c);
    }
}
