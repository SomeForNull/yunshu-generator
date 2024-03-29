package com.yupi.maker.generator.main;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.util.StrUtil;
import com.yupi.maker.generator.JarGenerator;
import com.yupi.maker.generator.ScriptGenerator;
import com.yupi.maker.generator.file.DynamicFileGenerator;
import com.yupi.maker.meta.Meta;
import com.yupi.maker.meta.MetaManager;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.IOException;

public abstract class GenerateTemplate {
    public void doGenerate() throws TemplateException, IOException, InterruptedException{
        Meta meta = MetaManager.getMetaObject();
        System.out.println(meta);
        //输出的根路径
        String projectPath=System.getProperty("user.dir");
        String outputPath=projectPath+ File.separator+"generated"+File.separator+meta.getName();
        if(!FileUtil.exist(outputPath)){
            FileUtil.mkdir(outputPath);
        }
        //1.从原始文件复制到生成的代码中
        String sourceCopyPath = copySource(meta, outputPath);

        //2.生成代码
        generateCode(meta, outputPath);


        //构建jar包
         String jarPath=buildJar(meta,outputPath);

        
        //封装脚本
        String shellOutputFilePath = buildScript(outputPath, jarPath);

        //构建精简版的程序（产物包）
        buildDist(outputPath, shellOutputFilePath, jarPath,sourceCopyPath);
    }

    protected void buildDist(String outputPath, String shellOutputFilePath, String jarPath,String sourceCopyPath) {
        String distOutputPath= outputPath +"-dist";
        //拷贝jar包
        String targetAbsolutePath=distOutputPath+File.separator+"target";
        FileUtil.mkdir(targetAbsolutePath);
        String jarAbsolutePath= outputPath +File.separator+ jarPath;
        FileUtil.copy(jarAbsolutePath,targetAbsolutePath,true);
        //拷贝脚本文件
        FileUtil.copy(shellOutputFilePath,distOutputPath,true);
        FileUtil.copy(shellOutputFilePath +".bat",distOutputPath,true);
        //拷贝源模板文件
        FileUtil.copy(sourceCopyPath,distOutputPath,true);
        //拷贝README.md
        FileUtil.copy(outputPath +File.separator+"README.md",distOutputPath,true);
    }

    protected String buildScript(String outputPath, String jarPath) {
        String shellOutputFilePath= outputPath +File.separator+"generator";

        ScriptGenerator.doGenerator(shellOutputFilePath,jarPath);
        return shellOutputFilePath;
    }


    protected String buildJar(Meta meta,String outputPath) throws IOException, InterruptedException {
        JarGenerator.doGenerate(outputPath);
        String jarName=String.format("%s-%s-jar-with-dependencies.jar", meta.getName(), meta.getVersion());
        String jarPath="target/"+jarName;
        return jarPath;
    }

    protected void generateCode(Meta meta, String outputPath) throws IOException, TemplateException {
        //读取resource目录
        ClassPathResource classPathResource = new ClassPathResource("");
        String inputResourcePath = classPathResource.getAbsolutePath();

        //java包的基础路径
        //com.yupi
        String outputBasePackage = meta.getBasePackage();
        //com/yupi
        String outputBasePackagePath= StrUtil.join("/",StrUtil.split(outputBasePackage,"."));
        String outputBaseJavaPackagePath= outputPath +File.separator+"src/main/java/"+outputBasePackagePath;

        String inputFilePath;
        String outputFilePath;
        //model.DataModel
        inputFilePath=inputResourcePath+File.separator+"templates/java/model/DataModel.java.ftl";
        outputFilePath=outputBaseJavaPackagePath+File.separator+"/model/DataModel.java";
        DynamicFileGenerator.doGenerate(inputFilePath,outputFilePath, meta);
        //cli.command.GenerateCommand
        inputFilePath=inputResourcePath+File.separator+"templates/java/cli/command/GenerateCommand.java.ftl";
        outputFilePath=outputBaseJavaPackagePath+File.separator+"/cli/command/GenerateCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath,outputFilePath, meta);

        //cli.command.ConfigCommand
        inputFilePath=inputResourcePath+File.separator+"templates/java/cli/command/ConfigCommand.java.ftl";
        outputFilePath=outputBaseJavaPackagePath+File.separator+"/cli/command/ConfigCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath,outputFilePath, meta);

        //cli.command.ListCommand
        inputFilePath=inputResourcePath+File.separator+"templates/java/cli/command/ListCommand.java.ftl";
        outputFilePath=outputBaseJavaPackagePath+File.separator+"/cli/command/ListCommand.java";
        DynamicFileGenerator.doGenerate(inputFilePath,outputFilePath, meta);

        //cli.CommandExecutor
        inputFilePath=inputResourcePath+File.separator+"templates/java/cli/CommandExecutor.java.ftl";
        outputFilePath=outputBaseJavaPackagePath+File.separator+"/cli/CommandExecutor.java";
        DynamicFileGenerator.doGenerate(inputFilePath,outputFilePath, meta);

        //Main
        inputFilePath=inputResourcePath+File.separator+"templates/java/Main.java.ftl";
        outputFilePath=outputBaseJavaPackagePath+File.separator+"Main.java";
        DynamicFileGenerator.doGenerate(inputFilePath,outputFilePath, meta);

        // generator.DynamicGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/DynamicGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/DynamicGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // generator.MainGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/MainGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/MainGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // generator.StaticGenerator
        inputFilePath = inputResourcePath + File.separator + "templates/java/generator/StaticGenerator.java.ftl";
        outputFilePath = outputBaseJavaPackagePath + "/generator/StaticGenerator.java";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // pom.xml
        inputFilePath = inputResourcePath + File.separator + "templates/pom.xml.ftl";
        outputFilePath = outputPath +File.separator+"pom.xml";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);

        // README.xml
        inputFilePath = inputResourcePath + File.separator + "templates/README.md.ftl";
        outputFilePath = outputPath +File.separator+"README.md";
        DynamicFileGenerator.doGenerate(inputFilePath , outputFilePath, meta);
    }

    protected String copySource(Meta meta, String outputPath) {
        String sourceRootPath= meta.getFileConfig().getSourceRootPath();
        String sourceCopyPath= outputPath +File.separator+".source";
        FileUtil.copy(sourceRootPath,sourceCopyPath,false);
        return sourceCopyPath;
    }
}
