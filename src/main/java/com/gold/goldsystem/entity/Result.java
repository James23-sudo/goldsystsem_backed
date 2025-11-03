package com.gold.goldsystem.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.Accessors;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Result {

    private Integer code;
    private String msg;  //响应信息 描述字符串
    private Object data; //返回的数据

    //增删改 成功响应
    public static Result success(Integer code, String msg){
        return new Result(code,msg,null);
    }
    public static Result error(Integer code, String msg){
        return new Result(code,msg,null);
    }

    //查询 成功响应
    public static Result success(Object data){
        return new Result(200,"success",data);
    }
}

