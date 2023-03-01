package com.west2.component.delay;

import lombok.Data;

@Data
public class TaskBase {
    //任务参数，根据业务需求多少都行
    protected String identifier;

    public TaskBase(String identifier) {
        this.identifier = identifier;
    }

    public TaskBase() {
    }
}