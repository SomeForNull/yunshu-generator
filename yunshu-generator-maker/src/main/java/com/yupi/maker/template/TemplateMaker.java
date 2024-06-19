package com.yupi.maker.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yupi.maker.meta.Meta;
import com.yupi.maker.meta.enums.FileGenerateTypeEnum;
import com.yupi.maker.meta.enums.FileTypeEnum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 模板制作工具
 */
public class TemplateMaker {
    public static void main(String[] args) {
        //一、输入信息
        //1、项目的基本信息
        String name="acm-template-pro-generator";
        String description="ACM 示例模板生成器";
        //2、输入文件信息
        String projectPath=System.getProperty("user.dir");
        //要挖坑的根目录
        String sourceRootPath=new File(projectPath).getParent()+File.separator+"yunshu-generator-demo-projects/acm-template";
        //要挖坑的文件
        String fileInputPath="src/com/yupi/acm/MainTemplate.java";

        //要输出的文件
        String fileOutputPath=fileInputPath+".ftl";

        //3、输入模型参数信息
        Meta.ModelConfig.ModelInfo modelInfo=new Meta.ModelConfig.ModelInfo();
        modelInfo.setFieldName("outputText");
        modelInfo.setType("String");
        modelInfo.setDefaultValue("sum= ");

        //二、使用字符串替换，生成模板文件
        String fileInputAbsolutePath=sourceRootPath+File.separator+fileInputPath;
        String fileContent= FileUtil.readUtf8String(fileInputAbsolutePath);
        String replacement=String.format("${%s}",modelInfo.getFieldName());
        String newContent= StrUtil.replace(fileContent,"test",replacement);

        //输出模板文件
        String fileOutputAbsolutePath=sourceRootPath+File.separator+fileOutputPath;
        FileUtil.writeUtf8String(newContent,fileOutputAbsolutePath);

        //三、生成配置文件
        String metaPath=sourceRootPath+File.separator+"meta.json";

        //1.构造配置参数对象
        Meta meta = new Meta();
        meta.setName(name);
        meta.setDescription(description);

        Meta.FileConfig fileConfig = new Meta.FileConfig();
        meta.setFileConfig(fileConfig);

        fileConfig.setSourceRootPath(sourceRootPath);


        List<Meta.FileConfig.FileInfo> fileInfoList=new ArrayList<>();

        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        fileInfo.setInputPath(fileInputPath);
        fileInfo.setOutputPath(fileOutputPath);
        fileInfo.setType(FileTypeEnum.File.getText());


        fileInfoList.add(fileInfo);

        fileConfig.setFiles(fileInfoList);

        Meta.ModelConfig modelConfig = new Meta.ModelConfig();
        meta.setModelConfig(modelConfig);

        List<Meta.ModelConfig.ModelInfo> modelInfoList=new ArrayList<>();
        modelInfoList.add(modelInfo);
        modelConfig.setModels(modelInfoList);

        //2.生成元信息文件
       FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(meta),metaPath);


    }

}
