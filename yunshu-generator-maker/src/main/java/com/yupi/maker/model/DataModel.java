package com.yupi.maker.model;

import lombok.Data;

/**
 * 静态模板配置
 */
@Data
public class DataModel {
    // 动态生成需求
    // 1. 在代码开头添加作者@author注解（增加代码）
    // 2. 修改程序输出的信息提示（替换代码）
    // 3. 将循环读取输入改为单词读取（可选代码）

    /**
     * 作者
     */
    private String author="云舒";
    /**
     * 输出信息
     */
    private String outputText="输出结果";
    /**
     * 是否循环
     */
    private boolean loop=true;

}
