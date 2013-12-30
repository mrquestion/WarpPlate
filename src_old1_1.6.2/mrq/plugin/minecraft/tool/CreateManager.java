package mrq.plugin.minecraft.tool;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;

public class CreateManager {
    public static boolean createFile(Object o, File file1) {
        boolean b = false;
        
        String[] ss = file1.getPath().split(Matcher.quoteReplacement(File.separator));
        File[] files = new File[ss.length];
        for (int i=0; i<files.length; i++) {
            String s = i > 0 ? files[i-1].getPath() + File.separator : "";
            files[i] = new File(s + ss[i]);
        }
        
        for (int i=0; i<files.length; i++) {
            if (files[i].exists()) {
                if (i == files.length-1) {
                    if (files[i].isFile()) {
                        b = true;
                    }
                    else if (files[i].isDirectory()) {
                        if (files[i].delete()) {
                            try {
                                if (files[i].createNewFile()) {
                                    b = true;
                                }
                                else {
                                    m.sg(o, files[i].getName() + " ���� ������ �����߽��ϴ�.");
                                }
                            } catch (IOException e) {
                                m.sg(o, files[i].getName() + " ���� ������ �����߽��ϴ�: " + e.getMessage());
                            }
                        }
                        else {
                            m.sg(o, files[i].getName() + " ���� ������ �����߽��ϴ�.");
                        }
                    }
                }
                else {
                    if (files[i].isFile()) {
                        if (files[i].delete()) {
                            if (files[i].mkdir()) {
                                
                            }
                            else {
                                m.sg(o, files[i].getName() + " ���� ������ �����߽��ϴ�.");
                            }
                        }
                        else {
                            m.sg(o, files[i].getName() + " ���� ������ �����߽��ϴ�.");
                        }
                    }
                }
            }
            else {
                if (i == files.length-1) {
                    try {
                        if (files[i].createNewFile()) {
                            b = true;
                        }
                        else {
                            m.sg(o, files[i].getName() + " ���� ������ �����߽��ϴ�.");
                        }
                    } catch (IOException e) {
                        m.sg(o, files[i].getName() + " ���� ������ �����߽��ϴ�: " + e.getMessage());
                    }
                }
                else {
                    if (files[i].mkdir()) {
                        
                    }
                    else {
                        m.sg(o, files[i].getName() + " ���� ������ �����߽��ϴ�.");
                    }
                }
            }
        }
        
        return b;
    }
}