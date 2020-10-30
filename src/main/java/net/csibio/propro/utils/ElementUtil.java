package net.csibio.propro.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-12 15:13
 */
public class ElementUtil {

    public static final Logger logger = LoggerFactory.getLogger(ElementUtil.class);

    public static HashMap<String, Integer> getElementMap(String formula) {

        if (formula == null || formula.isEmpty()) {
            return null;
        }
        HashMap<String, Integer> elementMap = new HashMap<>();
        try {
            String[] kvPairs = formula.split(",");
            for (String kvPair : kvPairs) {
                String[] kv = kvPair.split(":");
                elementMap.put(kv[0], Integer.parseInt(kv[1]));
            }
        } catch (Exception e) {
            logger.error("Formula Parse Error:", e);
        }

        return elementMap;
    }
}
