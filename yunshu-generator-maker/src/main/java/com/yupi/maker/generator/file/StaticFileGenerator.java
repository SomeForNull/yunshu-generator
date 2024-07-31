package com.yupi.maker.generator.file;

import cn.hutool.core.io.FileUtil;

/**
 * 静态文件生成器
 * */
public class StaticFileGenerator {

    /**
     * 拷贝文件(hutool实现，会将输入目录完整拷贝到输出目录下)
     * @param inputPath 输入路径
     * @param outputPath 输出路径
     */
    public static void copyFileByHutool(String inputPath,String outputPath){
        FileUtil.copy(inputPath,outputPath,false);
    }

}
