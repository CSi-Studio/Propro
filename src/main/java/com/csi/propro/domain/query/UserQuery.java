package com.csi.propro.domain.query;

import lombok.Data;

@Data
public class UserQuery extends PageQuery {

    String id;

    String username;

    String email;

    String nick;

    String telephone;

    String organization;
}
