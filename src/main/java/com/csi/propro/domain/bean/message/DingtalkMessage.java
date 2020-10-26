package com.csi.propro.domain.bean.message;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DingtalkMessage {

    //消息类型，此时固定为：text
    String msgtype = "markdown";

    Markdown markdown;

    At at;


    public DingtalkMessage(String title, String text){
        this.markdown = new Markdown(title, text);
        this.at = new At();
    }

    @Data
    public class Markdown{

        //首屏会话透出的展示内容
        String title;
        //markdown格式的消息
        String text;
        public Markdown(String title, String text){
            this.title = title;
            this.text = text;
        }

    }

    @Data
    public class At{

        //被@人的手机号(在content里添加@人的手机号)
        List<String> atMobiles = new ArrayList<>();

        //@所有人时：true，否则为：false
        boolean isAtAll = false;
    }
}
