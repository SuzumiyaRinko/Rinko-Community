package suzumiya.util;

import cn.hutool.dfa.WordTree;
import org.springframework.core.io.ClassPathResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class WordTreeUtils {

    private static WordTree wordTree = null;

    /* 初始化wordTree对象 */
    static {
        /* 获取文件 */
        InputStream is = null;
        try {
            is = new ClassPathResource("data/sensitiveWord.txt").getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            /* 把单词添加到List中 */
            List<String> sensitiveWords = new ArrayList<>();
            String t = null;
            while (true) {
                try {
                    if ((t = reader.readLine()) == null) break;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                sensitiveWords.add(t);
            }

            wordTree = new WordTree();
            wordTree.addWords(sensitiveWords);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                assert is != null;
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String replaceAllSensitiveWords(String origin) {
        List<String> matchAll = wordTree.matchAll(origin);
        for (String sensitiveWord : matchAll) {
            int len = sensitiveWord.length();
            String replacement = "*".repeat(len);
            origin = origin.replaceAll(sensitiveWord, replacement);
        }
        return origin;
    }
}
