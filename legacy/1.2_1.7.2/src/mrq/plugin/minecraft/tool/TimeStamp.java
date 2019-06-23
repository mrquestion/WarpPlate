package mrq.plugin.minecraft.tool;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeStamp {
    private static SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMddHHmmss", Locale.US);
    static { sdf.setTimeZone(TimeZone.getTimeZone("GMT+9")); }
    public static String format() {
        return sdf.format(new Date());
    }
    public static String format(String s) {
        SimpleDateFormat sdf = new SimpleDateFormat(s, Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+9"));
        return sdf.format(new Date());
    }
}