package com.west2.common;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class KuaishouTestResponse {

    private Integer result;
    private String message_id;

}
