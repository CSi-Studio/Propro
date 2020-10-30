package net.csibio.propro.algorithm.parser.xml;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.AnyTypePermission;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.OutputStream;
import java.io.Writer;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-07-19 16:50
 *
 * 这个类的这种写法是有问题的,因为AirXStream在被不同的类调用的时候,所使用的初始化函数不同,在SpringBean的环境下会有线程安全问题
 * 例如在MzXmlParser类中的初始化函数,这个在后面需要被重构修改
 *  airXStream.processAnnotations(classes);
 *  airXStream.allowTypes(classes);
 *  airXStream.registerConverter(new PrecursorMzConverter());
 */
@Component
public class AirXStream extends XStream {

    /**
     * xml版本号，默认1.0
     */
    private String version;
    /**
     * xml编码，默认UTF-8
     */
    private String encoding;

    public AirXStream() {
        this("1.0","UTF-8");
    }

    @PostConstruct
    public void init(){
        addPermission(new AnyTypePermission());
//        AirXStream.setupDefaultSecurity(this);
    }

    //XML的声明
    public String getDeclaration() {
        return "< ?xml version=\"" + this.version + "\" encoding=\"" + this.encoding + "\"? >\n";
    }

    public AirXStream(String version, String encoding) {
        this.version = version;
        this.encoding = encoding;
    }

    /**
     * 覆盖父类的方法，然后调用父类的，输出的时候先输出这个XML的声明
     * @param obj
     * @param output
     */
    @Override
    public void toXML(Object obj, OutputStream output){
        try {
            String dec = this.getDeclaration();
            byte[] bytesOfDec = dec.getBytes("UTF-8");
            output.write(bytesOfDec);
        } catch (Exception e) {
            throw new RuntimeException("error", e);
        }
        super.toXML(obj, output);
    }

    @Override
    public void toXML(Object obj, Writer writer) {
        try {
            writer.write(getDeclaration());
        } catch (Exception e) {
            throw new RuntimeException("error", e);
        }
        super.toXML(obj, writer);
    }
}
