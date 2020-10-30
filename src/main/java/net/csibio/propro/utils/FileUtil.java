package net.csibio.propro.utils;

import com.alibaba.fastjson.JSONArray;
import net.csibio.propro.constants.SuffixConst;
import net.csibio.propro.constants.SymbolConst;
import net.csibio.propro.domain.bean.file.TableFile;
import net.csibio.propro.domain.db.AnalyseDataDO;
import net.csibio.propro.domain.bean.analyse.RtIntensityPairsDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-28 21:45
 */
public class FileUtil {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    public static String readFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        int fileLength = fis.available();
        byte[] bytes = new byte[fileLength];
        fis.read(bytes);
        return new String(bytes, 0, fileLength);
    }

    public static String readFile(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);
        int fileLength = fis.available();
        byte[] bytes = new byte[fileLength];
        fis.read(bytes);
        return new String(bytes, 0, fileLength);
    }

    public static TableFile readTableFile(String filePath) throws IOException {
        File file = new File(filePath);
        InputStreamReader isr = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
        BufferedReader reader = new BufferedReader(isr);
        String line = reader.readLine();
        if (line == null) {
            return null;
        }
        String splitter = SymbolConst.TAB;
        String[] columns = line.split(splitter);
        if (columns.length == 1) {
            splitter = SymbolConst.COMMA;
            columns = line.split(splitter);
        }
        HashMap<String, Integer> columnMap = new HashMap<>();
        List<String[]> fileData = new ArrayList<>();
        for (int i = 0; i < columns.length; i++) {
            columnMap.put(columns[i].toLowerCase(), i);
        }
        while ((line = reader.readLine()) != null) {
            String[] lineSplit = line.split(splitter);
            fileData.add(lineSplit);
        }
        return new TableFile(columnMap, fileData);
    }

    public static String readFileFromSource(String filePath) throws IOException {
        File file = new File(FileUtil.class.getClassLoader().getResource(filePath).getPath());
        FileInputStream fis = new FileInputStream(file);
        int fileLength = fis.available();
        byte[] bytes = new byte[fileLength];
        fis.read(bytes);
        return new String(bytes, 0, fileLength);
    }

    public static List<AnalyseDataDO> readAnalyseDataFromJsonFile(String filePath) throws IOException {
        File file = new File(filePath);
        FileInputStream fis = new FileInputStream(file);
        BufferedReader br = new BufferedReader(new InputStreamReader(fis));
        String line = br.readLine();
        List<AnalyseDataDO> dataList = new ArrayList<>();
        while (line != null) {
            dataList.addAll(JSONArray.parseArray(line, AnalyseDataDO.class));
            line = br.readLine();
        }
        br.close();
        return dataList;
    }

    //根据Aird文件获取同名同目录下的Aird索引文件的文件路径
    public static String getAirdIndexFilePath(String airdFilePath) {
        return airdFilePath.substring(0, airdFilePath.lastIndexOf(".")) + SuffixConst.JSON;
    }

    public static boolean isAirdFile(String airdFilePath) {
        return airdFilePath.toLowerCase().endsWith(SuffixConst.AIRD);
    }

    public static boolean isAirdIndexFile(String airdIndexFilePath) {
        return airdIndexFilePath.toLowerCase().endsWith(SuffixConst.JSON);
    }

    public static boolean isMzXMLFile(String mzXMLFilePath) {
        return mzXMLFilePath.toLowerCase().endsWith(SuffixConst.MZXML);
    }

    public static List<File> readChunks(File chunkDir) {
        // 读取分片文件
        File[] chunks = null;
        if (chunkDir.exists()) {
            chunks = chunkDir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname.isDirectory()) {
                        return false;
                    }
                    return true;
                }
            });
        }
        // 分片文件排序
        List<File> chunkList = null;
        if (chunks != null && chunks.length > 0) {
            chunkList = Arrays.asList(chunks);
            Collections.sort(chunkList, new Comparator<File>() {
                @Override
                public int compare(File o1, File o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }
        return chunkList;
    }

    public static void randomAccessFile(File in, File out, Long seek) throws IOException {
        RandomAccessFile raFile = null;
        BufferedInputStream inputStream = null;
        try {
            // 以读写的方式打开目标文件
            raFile = new RandomAccessFile(out, "rw");
            raFile.seek(seek);
            inputStream = new BufferedInputStream(new FileInputStream(in));
            byte[] buf = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buf)) != -1) {
                raFile.write(buf, 0, length);
            }
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (raFile != null) {
                    raFile.close();
                }
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }
        }
    }

    public static List<File> scanFiles(String projectName) {
        String directoryPath = RepositoryUtil.getProjectRepo(projectName);
        File directory = new File(directoryPath);

        List<File> newFileList = new ArrayList<>();
        File[] fileArray = directory.listFiles();
        if (fileArray != null) {
            for (File file : fileArray) {
                if (file.isFile()) {
                    newFileList.add(file);
                }
            }
        }
        return newFileList;
    }

    /**
     * 删除单个文件
     *
     * @param sPath 被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param sPath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String sPath) {
        // 如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        // 删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } // 删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            }
        }
        if (!flag) {
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public static String getFileSuffix(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public static List<AnalyseDataDO> getAnalyseDataList(String filePath) throws IOException {
        String content = readFile(filePath);
        List<AnalyseDataDO> dataList = JSONArray.parseArray(content, AnalyseDataDO.class);
        return dataList;
    }

    public static RtIntensityPairsDouble txtReader(BufferedReader reader, String divide, int column1, int column2) throws IOException {
        String line = reader.readLine();
        List<Double> rtList = new ArrayList<>();
        List<Double> intensityList = new ArrayList<>();
        while (line != null) {
            String[] item = line.split(divide);
            rtList.add(Double.parseDouble(item[column1]));
            intensityList.add(Double.parseDouble(item[column2]));
            line = reader.readLine();
        }
        Double[] rtArray = new Double[rtList.size()];
        Double[] intArray = new Double[intensityList.size()];
        for (int i = 0; i < rtArray.length; i++) {
            rtArray[i] = rtList.get(i);
            intArray[i] = intensityList.get(i);
        }
        return new RtIntensityPairsDouble(rtArray, intArray);

    }

    public static void writeFile(String filePath, String content, boolean isOverride) throws IOException {
        File file = new File(filePath);
        if (isOverride) {
            file.createNewFile();
        } else {
            if (!file.exists()) {
                file.createNewFile();
            }
        }

        byte[] b = content.getBytes();
        int l = b.length;
        OutputStream os = new FileOutputStream(file);
        os.write(b, 0, l);
        os.close();
    }

    public static void writeFile(String filePath, List list, boolean isOverride) throws IOException {
        File file = new File(filePath);
        if (isOverride) {
            file.createNewFile();
        } else {
            if (!file.exists()) {
                file.createNewFile();
            }
        }

        String content = JSONArray.toJSONString(list);
        byte[] b = content.getBytes();
        int l = b.length;
        OutputStream os = new FileOutputStream(file);
        os.write(b, 0, l);
        os.close();
    }

    public static void fileInputStreamSkip(FileInputStream inputStream, long skip) throws IOException {
        //避免IO错误
        while (skip > 0) {
            long amt = inputStream.skip(skip);
            if (amt == -1) {
                throw new RuntimeException(inputStream + ": unexpected EOF");
            }
            skip -= amt;
        }
    }

    public static void close(RandomAccessFile raf) {
        if (raf != null) {
            try {
                raf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(FileWriter fw) {
        if (fw != null) {
            try {
                fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(BufferedWriter bw) {
        if (bw != null) {
            try {
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(FileOutputStream fos) {
        if (fos != null) {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(BufferedOutputStream bos) {
        if (bos != null) {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
