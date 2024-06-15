package com.yupi.maker.generator.main;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public class MainGenerator extends GenerateTemplate{
    @Override
    protected void buildDist(String outputPath, String shellOutputFilePath, String jarPath, String sourceCopyPath) {
        System.out.println("不要输出dist");

    }
}
