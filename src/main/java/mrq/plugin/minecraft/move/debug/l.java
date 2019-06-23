package mrq.plugin.minecraft.move.debug;

import java.util.Arrays;

public class l {
    public static String z(Object... os) {
        return String.join(" ", Arrays.stream(os).map(String::valueOf).toArray(String[]::new));
    }
    public static void og(Object... os) {
        System.out.println(String.format("* %s", z(os)));
    }
}
