package cn.xyz327.generator.vo;

import lombok.Data;

/**
 * @author <a href="mailto:xyz327@outlook.com">xizhou</a>
 * @since 2020/9/16 11:22 上午
 */
@Data
public class Result<T> {
    private int resultCode;
    private String errorMsg;
    private T data;
}
