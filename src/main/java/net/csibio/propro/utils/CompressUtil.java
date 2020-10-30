package net.csibio.propro.utils;

import me.lemire.integercompression.IntWrapper;
import me.lemire.integercompression.differential.IntegratedBinaryPacking;
import me.lemire.integercompression.differential.IntegratedVariableByte;
import me.lemire.integercompression.differential.SkippableIntegratedComposition;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CompressUtil {

    //byte[]压缩为byte[]
    public static byte[] zlibCompress(byte[] data) {
        byte[] output;

        Deflater compresser = new Deflater();

        compresser.reset();
        compresser.setInput(data);
        compresser.finish();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!compresser.finished()) {
                int i = compresser.deflate(buf);

                bos.write(buf, 0, i);
            }
            output = bos.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        compresser.end();
        return output;
    }

    public static byte[] zlibDecompress(byte[] data) {
        byte[] output = null;

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[2048];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        return output;
    }

    public static int[] compressForSortedInt(int[] target) {
        SkippableIntegratedComposition codec = new SkippableIntegratedComposition(new IntegratedBinaryPacking(), new IntegratedVariableByte());
        // output vector should be large enough...
        int[] compressed = new int[target.length + 1024];
        IntWrapper inputoffset = new IntWrapper(0);
        IntWrapper outputoffset = new IntWrapper(1);
        codec.headlessCompress(target, inputoffset, target.length, compressed, outputoffset, new IntWrapper(0));
        compressed[0] = target.length;
        compressed = Arrays.copyOf(compressed, outputoffset.intValue());
        return compressed;
    }

    public static int[] decompressForSortedInt(int[] compressed) {
        SkippableIntegratedComposition codec = new SkippableIntegratedComposition(new IntegratedBinaryPacking(), new IntegratedVariableByte());
        int size = compressed[0];
        // output vector should be large enough...
        int[] recovered = new int[size];
        IntWrapper inPoso = new IntWrapper(1);
        IntWrapper outPoso = new IntWrapper(0);
        IntWrapper recoffset = new IntWrapper(0);
        codec.headlessUncompress(compressed, inPoso, compressed.length, recovered, recoffset, size, outPoso);

        return recovered;
    }

    public static String transToString(float[] target) {
        FloatBuffer fbTarget = FloatBuffer.wrap(target);
        ByteBuffer bbTarget = ByteBuffer.allocate(fbTarget.capacity() * 4);
        bbTarget.asFloatBuffer().put(fbTarget);
        byte[] targetArray = bbTarget.array();
        byte[] compressedArray = CompressUtil.zlibCompress(targetArray);
        String targetStr = new String(new Base64().encode(compressedArray));
        return targetStr;
    }

    public static String transToString(int[] target) {
        IntBuffer ibTarget = IntBuffer.wrap(target);
        ByteBuffer bbTarget = ByteBuffer.allocate(ibTarget.capacity() * 4);
        bbTarget.asIntBuffer().put(ibTarget);
        byte[] targetArray = bbTarget.array();
        byte[] compressedArray = CompressUtil.zlibCompress(targetArray);
        String targetStr = new String(new Base64().encode(compressedArray));
        return targetStr;
    }

    public static byte[] transToByte(int[] target) {
        IntBuffer ibTarget = IntBuffer.wrap(target);
        ByteBuffer bbTarget = ByteBuffer.allocate(ibTarget.capacity() * 4);
        bbTarget.asIntBuffer().put(ibTarget);
        byte[] targetArray = bbTarget.array();
        byte[] compressedArray = CompressUtil.zlibCompress(targetArray);
        return compressedArray;
    }

    public static byte[] transToByte(float[] target) {
        FloatBuffer fbTarget = FloatBuffer.wrap(target);
        ByteBuffer bbTarget = ByteBuffer.allocate(fbTarget.capacity() * 4);
        bbTarget.asFloatBuffer().put(fbTarget);
        byte[] targetArray = bbTarget.array();
        byte[] compressedArray = CompressUtil.zlibCompress(targetArray);
        return compressedArray;
    }

    public static Float[] transToFloat(byte[] value){
        ByteBuffer byteBuffer = ByteBuffer.wrap(value);
        byteBuffer = ByteBuffer.wrap(CompressUtil.zlibDecompress(byteBuffer.array()));

        FloatBuffer floats = byteBuffer.asFloatBuffer();
        Float[] floatValues = new Float[floats.capacity()];
        for (int i = 0; i < floats.capacity(); i++) {
            floatValues[i] = floats.get(i);
        }

        byteBuffer.clear();
        return floatValues;
    }
}
