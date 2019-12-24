package com.example.demo.base;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(callSuper = true)
public class PageResult extends Result {

    private Long count;

    public PageResult(Integer code, String msg, Object data, Long count) {
        super(code, msg, data);
        this.count = count;
    }

    public static PageResult success(Object data, Long count) {
        return new PageResult(Status.SUCCESS.getCode(), Status.SUCCESS.getMsg(), data, count);
    }
}
