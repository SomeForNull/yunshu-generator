package com.yupi.maker.generator.file;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

/**
 *
 */
public class FileGenerator {

    public static void doGenerator(Object model) throws IOException, TemplateException {
        //1.静态文件生成
        String projectPath = System.getProperty("user.dir");
        System.out.println(projectPath);
        //输入路径
        String inputPath = projectPath+ File.separator+"yunshu-generator-demo-projects"+ File.separator+"acm-template";
        //输出路径
        String outputPath = projectPath;
        //复制
        StaticFileGenerator.copyFileByHutool(inputPath,outputPath);

        //2.动态文件生成
        String dynamicInputPath=projectPath+File.separator+"yunshu-generator-maker"+File.separator+"src/main/resources/templates/MainTemplate.java.ftl";
        String dynamicOutputPath=projectPath+File.separator+"acm-template/src/com/yupi/acm/MainTemplate.java";

        DynamicFileGenerator.doGenerate(dynamicInputPath,dynamicOutputPath,model);

    }
}
