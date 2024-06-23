package com.yupi.maker.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yupi.maker.meta.Meta;
import com.yupi.maker.meta.enums.FileGenerateTypeEnum;
import com.yupi.maker.meta.enums.FileTypeEnum;
import com.yupi.maker.template.enums.FileFilterRangeEnum;
import com.yupi.maker.template.enums.FileFilterRuleEnum;
import com.yupi.maker.template.model.FileFilterConfig;
import com.yupi.maker.template.model.TemplateMakerFileConfig;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * 模板制作工具
 */
public class TemplateMaker {
    private static long makeTemplate(String originalProjectPath, TemplateMakerFileConfig templateMakerFileConfig, String searchStr, Meta meta, Meta.ModelConfig.ModelInfo modelInfo, Long id) {
        // 没有 id 则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }

       String projectPath = System.getProperty("user.dir");
        // 复制目录
        String tempDirPath = projectPath + File.separator + ".temp";

        String templatePath = tempDirPath + File.separator + id;

        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originalProjectPath, templatePath, true);
        }


        //一、输入信息

        //2、输入文件信息

        //要挖坑的根目录
        String sourceRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originalProjectPath)).toString();

        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");
        //支持遍历多个文件
        List<Meta.FileConfig.FileInfo> newFileInfos = new ArrayList<>();
        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfig = templateMakerFileConfig.getFiles();
        for ( TemplateMakerFileConfig.FileInfoConfig fileConfig : fileInfoConfig) {
            String inputFileAbsolutePath = sourceRootPath + File.separator + fileConfig.getPath();
            //传入绝对路径
            List<File> fileList = FileFilter.doFilter(inputFileAbsolutePath, fileConfig.getFileFilterConfigList());
            for (File file : fileList) {
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(searchStr, modelInfo, file,sourceRootPath);
                newFileInfos.add(fileInfo);
            }
        }

        //三、生成配置文件
        String metaPath = sourceRootPath + File.separator + "meta.json";
        //已有meta文件，不是第一次制作
        if (FileUtil.exist(metaPath)) {
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaPath), Meta.class);
            //1.追加配置参数
            List<Meta.FileConfig.FileInfo> oldFiles = oldMeta.getFileConfig().getFiles();
            oldFiles.addAll(newFileInfos);
            List<Meta.ModelConfig.ModelInfo> oldModels = oldMeta.getModelConfig().getModels();
            oldModels.add(modelInfo);

            //配置去重
            oldMeta.getModelConfig().setModels(distinctModels(oldModels));
            oldMeta.getFileConfig().setFiles(distinctFiles(oldFiles));

            //2.输出元信息文件
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(oldMeta), metaPath);
        } else {
            //1.构造配置参数对象

            Meta.FileConfig fileConfig = new Meta.FileConfig();
            meta.setFileConfig(fileConfig);

            fileConfig.setSourceRootPath(sourceRootPath);


            List<Meta.FileConfig.FileInfo> fileInfoList = new ArrayList<>();

            fileInfoList.addAll(newFileInfos);

            fileConfig.setFiles(fileInfoList);

            Meta.ModelConfig modelConfig = new Meta.ModelConfig();
            meta.setModelConfig(modelConfig);

            List<Meta.ModelConfig.ModelInfo> modelInfoList = new ArrayList<>();
            modelInfoList.add(modelInfo);
            modelConfig.setModels(modelInfoList);

            //2.生成元信息文件
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(meta), metaPath);

        }

        return id;
    }

    private static Meta.FileConfig.FileInfo makeFileTemplate(String searchStr, Meta.ModelConfig.ModelInfo modelInfo, File inputFilePath,String sourceRootPath) {
        String fileInputPath = inputFilePath.getAbsolutePath().replaceAll("\\\\","/").replace(sourceRootPath+"/","");

        //要输出的文件
        String fileOutputPath = fileInputPath + ".ftl";

        //二、使用字符串替换，生成模板文件
        String fileInputAbsolutePath = inputFilePath.getAbsolutePath();
        String fileOutputAbsolutePath = inputFilePath.getAbsolutePath()+".ftl";
        String fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        //如果已有模板文件，表示不是第一次制作，则在原有模板文件上修改
        if (FileUtil.exist(fileOutputAbsolutePath)) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        }

        String replacement = String.format("${%s}", modelInfo.getFieldName());
        String newContent = StrUtil.replace(fileContent, searchStr, replacement);



        //文件配置信息
        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        fileInfo.setInputPath(fileInputPath);
        fileInfo.setOutputPath(fileOutputPath);
        fileInfo.setType(FileTypeEnum.File.getValue());
        fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
        // 和原文件一致，没有挖坑，则为静态生成
        if (newContent.equals(fileContent)) {
            // 输出路径 = 输入路径
            fileInfo.setOutputPath(fileInputPath);
            fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
        } else {
            // 生成模板文件
            fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());
            //输出模板文件
            FileUtil.writeUtf8String(newContent, fileOutputAbsolutePath);
        }
        return fileInfo;
    }

    /**
     * 去重文件
     * 根据输入路径去重
     *
     * @param fileInfoList
     * @return
     */
    private static List<Meta.FileConfig.FileInfo> distinctFiles(List<Meta.FileConfig.FileInfo> fileInfoList) {
        ArrayList<Meta.FileConfig.FileInfo> newFileInfos = new ArrayList<>(fileInfoList.stream().collect(
                Collectors.toMap(Meta.FileConfig.FileInfo::getInputPath, fileInfo -> fileInfo, (e, r) -> r)
        ).values());
        return newFileInfos;
    }
    /**
     * 模型去重
     * @param modelInfoList
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList) {
        ArrayList<Meta.ModelConfig.ModelInfo> newModelInfos = new ArrayList<>(modelInfoList.stream().collect(
                Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, modelInfo -> modelInfo, (e, r) -> r)
        ).values());
        return newModelInfos;
    }

    public static void main(String[] args) {
        //指定原始项目路径
        String projectPath = System.getProperty("user.dir");
        String originalProjectPath = new File(projectPath).getParent() + File.separator + "yunshu-generator-demo-projects/springboot-init";
        //1、项目的基本信息
        Meta meta = new Meta();
        meta.setName( "acm-template-pro-generator");
        meta.setDescription("ACM 示例模板生成器");
        //要挖坑的文件
        String fileInputPath1 = "src/main/java/com/yupi/springbootinit/common";
        String fileInputPath2 = "src/main/java/com/yupi/springbootinit/controller";
        //3、输入模型参数信息
        //3.1第一次制作
/*        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
        modelInfo.setFieldName("outputText");
        modelInfo.setType("String");*/
        //3.2第二次制作
        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
        modelInfo.setFieldName("className");
        modelInfo.setType("String");
        //替换的字符串
        //第一次替换
//        String searchStr = "Sum: ";
        //第二次替换
        String searchStr = "BaseResponse";

        //文件过滤配置
        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig1 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig1.setPath(fileInputPath1);
        List<FileFilterConfig> fileFilterConfigList = new ArrayList<>();
        FileFilterConfig fileFilterConfig = FileFilterConfig.builder()
                .range(FileFilterRangeEnum.FILE_NAME.getValue())
                .rule(FileFilterRuleEnum.CONTAINS.getValue())
                .value("Base")
                .build();
        fileFilterConfigList.add(fileFilterConfig);
        fileInfoConfig1.setFileFilterConfigList(fileFilterConfigList);

        TemplateMakerFileConfig.FileInfoConfig fileInfoConfig2 = new TemplateMakerFileConfig.FileInfoConfig();
        fileInfoConfig2.setPath(fileInputPath2);

        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfigList = Arrays.asList(fileInfoConfig1, fileInfoConfig2);
        TemplateMakerFileConfig templateMakerFileConfig = new TemplateMakerFileConfig();
        templateMakerFileConfig.setFiles(fileInfoConfigList);

        long id = TemplateMaker.makeTemplate(originalProjectPath, templateMakerFileConfig, searchStr, meta, modelInfo, 1803405813415804928L);
        System.out.println(id);
    }

}
