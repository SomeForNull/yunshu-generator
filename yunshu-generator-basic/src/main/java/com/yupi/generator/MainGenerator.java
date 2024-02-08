package com.yupi.generator;

import com.yupi.model.MainTemplateConfig;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class MainGenerator {
    public static void main(String[] args) throws TemplateException, IOException {
        //1.静态文件生成
        String projectPath = System.getProperty("user.dir");
        System.out.println(projectPath);
        //输入路径
        String inputPath = projectPath+ File.separator+"yunshu-generator-demo-projects"+ File.separator+"acm-template";
        //输出路径
        String outputPath = projectPath;
        //复制
        StaticGenerator.copyFilesByRecursive(inputPath,outputPath);

        //2.动态文件生成

        String dynamicInputPath=projectPath+File.separator+"yunshu-generator-basic"+File.separator+"src/main/resources/templates/MainTemplate.java.ftl";
        String dynamicOutputPath=projectPath+File.separator+"acm-template/src/com/yupi/acm/MainTemplate.java";
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("云舒");
        mainTemplateConfig.setOutputText("我的输出结果：");
        mainTemplateConfig.setLoop(true);
        DynamicGenerator.doGenerator(dynamicInputPath,dynamicOutputPath,mainTemplateConfig);
    }
}
