package com.easymeeting.enums;

public enum ResponseCodeEnum {
    CODE_200(200, "请求成功"),
    CODE_404(404, "请求资源不存在"),
    CODE_500(500, "服务器异常"),
    CODE_600(600, "请求参数异常"),
    CODE_601(601, "信息已经存在"),
    CODE_603(603, "数据转化失败"),
    CODE_901(901, "登录超时，请重新登陆");

    private Integer code;
    private String msg;

    ResponseCodeEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Integer getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }
}
