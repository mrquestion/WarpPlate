package mrq.plugin.minecraft.tool;

public class m {
    public static void sg(Object... os) {
        StringBuilder sb = new StringBuilder("*");
        for (Object o: os) sb.append(String.format(" %s", String.valueOf(o)));
        System.out.println(sb.toString());
    }
}