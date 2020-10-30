package net.csibio.propro.test;

import com.alibaba.fastjson.JSON;
import net.csibio.propro.domain.db.PeptideDO;
import net.csibio.propro.ProproApplication;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.File;
import java.io.FileInputStream;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-08 19:32
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = ProproApplication.class)
@WebAppConfiguration
public class BaseTest {

    public final Logger logger = LoggerFactory.getLogger(BaseTest.class);

    public void init(){
        assert true;
    }

    public PeptideDO getJsonFromFileTest(){
        try {
            return getJsonFromFile("json/transition.json", PeptideDO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public PeptideDO getJsonFromFileTest1(){
        try {
            return getJsonFromFile("json/transition1.json", PeptideDO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public PeptideDO getJsonFromFileTest2(){
        try {
            return getJsonFromFile("json/transition2.json", PeptideDO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public PeptideDO getJsonFromFileTest3(){
        try {
            return getJsonFromFile("json/transition3.json", PeptideDO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public PeptideDO getJsonFromFileTest4(){
        try {
            return getJsonFromFile("json/transition4.json", PeptideDO.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public <T> T getJsonFromFile(String filePath, Class<T> clazz) throws Exception {
        File file = new File(getClass().getClassLoader().getResource(filePath).getPath());
        FileInputStream fis = new FileInputStream(file);
        int fileLength = fis.available();
        byte[] bytes = new byte[fileLength];
        fis.read(bytes);
        String jsonString = new String(bytes, 0, fileLength);

        return JSON.parseObject(jsonString, clazz);
    }


}
