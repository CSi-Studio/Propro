package net.csibio.propro.domain.bean.message;

import lombok.Data;

/**
 * 钉钉的申请消息内容
 */
@Data
public class ApplyMessage {

    String username;

    String email;

    String dingtalkId;

    String organization;

    String telephone;

    public String markdown(){
        StringBuilder sb = new StringBuilder();
        sb.append("- **申请人:**");
        sb.append(this.username);
        sb.append("\n");

        sb.append("- **邮箱:**");
        sb.append(this.email);
        sb.append("\n");

        sb.append("- **钉钉ID:**");
        sb.append(this.dingtalkId);
        sb.append("\n");

        sb.append("- **电话:**");
        sb.append(this.telephone);
        sb.append("\n");

        sb.append("- **组织:**");
        sb.append(this.organization);

        return sb.toString();
    }
}
