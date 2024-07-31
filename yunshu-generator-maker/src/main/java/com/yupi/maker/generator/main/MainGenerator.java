package com.yupi.maker.generator.main;

public class MainGenerator extends GenerateTemplate{
    @Override
    protected String buildDist(String outputPath, String shellOutputFilePath, String jarPath, String sourceCopyPath) {
        System.out.println("不要输出dist");
        return "";
    }
}
