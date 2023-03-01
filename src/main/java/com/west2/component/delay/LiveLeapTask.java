package com.west2.component.delay;

import lombok.Data;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
public class LiveLeapTask extends TaskBase {

    public LiveLeapTask() {
    }

    public LiveLeapTask(String identifier) {
        super(identifier);
    }

    private String orderId;

    private Integer type;

    private String title;


}
