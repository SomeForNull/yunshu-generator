package com.yupi.maker.template;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yupi.maker.meta.Meta;
import com.yupi.maker.meta.enums.FileGenerateTypeEnum;
import com.yupi.maker.meta.enums.FileTypeEnum;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * 模板制作工具
 */
public class TemplateMaker {
    private static long makeTemplate(Long id) {
        // 没有 id 则生成
        if (id == null) {
            id = IdUtil.getSnowflakeNextId();
        }

        //指定原始项目路径
        String projectPath = System.getProperty("user.dir");
        String originalProjectPath = new File(projectPath).getParent() + File.separator + "yunshu-generator-demo-projects/acm-template";
        // 复制目录
        String tempDirPath = projectPath + File.separator + ".temp";

        String templatePath = tempDirPath + File.separator + id;

        if (!FileUtil.exist(templatePath)) {
            FileUtil.mkdir(templatePath);
            FileUtil.copy(originalProjectPath, templatePath, true);
        }


        //一、输入信息
        //1、项目的基本信息
        String name = "acm-template-pro-generator";
        String description = "ACM 示例模板生成器";
        //2、输入文件信息

        //要挖坑的根目录
        String sourceRootPath = templatePath + File.separator + FileUtil.getLastPathEle(Paths.get(originalProjectPath)).toString();
        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");
        //要挖坑的文件
        String fileInputPath = "src/com/yupi/acm/MainTemplate.java";

        //要输出的文件
        String fileOutputPath = fileInputPath + ".ftl";

        //3、输入模型参数信息
        Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
        modelInfo.setFieldName("outputText");
        modelInfo.setType("String");
        modelInfo.setDefaultValue("Sum: ");

        //二、使用字符串替换，生成模板文件
        String fileInputAbsolutePath = sourceRootPath + File.separator + fileInputPath;
        String fileOutputAbsolutePath = sourceRootPath + File.separator + fileOutputPath;
        String fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        //如果已有模板文件，表示不是第一次制作，则在原有模板文件上修改
        if (FileUtil.exist(fileOutputAbsolutePath)) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        }

        String replacement = String.format("${%s}", modelInfo.getFieldName());
        String newContent = StrUtil.replace(fileContent, "Sum: ", replacement);

        //输出模板文件

        FileUtil.writeUtf8String(newContent, fileOutputAbsolutePath);

        //文件配置信息
        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        fileInfo.setInputPath(fileInputPath);
        fileInfo.setOutputPath(fileOutputPath);
        fileInfo.setType(FileTypeEnum.File.getValue());
        fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());

        //三、生成配置文件
        String metaPath = sourceRootPath + File.separator + "meta.json";
        //已有meta文件，不是第一次制作
        if (FileUtil.exist(metaPath)) {
            Meta oldMeta = JSONUtil.toBean(FileUtil.readUtf8String(metaPath), Meta.class);
            //1.追加配置参数
            List<Meta.FileConfig.FileInfo> oldFiles = oldMeta.getFileConfig().getFiles();
            oldFiles.add(fileInfo);
            List<Meta.ModelConfig.ModelInfo> oldModels = oldMeta.getModelConfig().getModels();
            oldModels.add(modelInfo);

            //配置去重
            oldMeta.getModelConfig().setModels(distinctModels(oldModels));
            oldMeta.getFileConfig().setFiles(distinctFiles(oldFiles));

            //2.输出元信息文件
            FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(oldMeta), metaPath);
        } else {
            //1.构造配置参数对象
            Meta meta = new Meta();
            meta.setName(name);
            meta.setDescription(description);

            Meta.FileConfig fileConfig = new Meta.FileConfig();
            meta.setFileConfig(fileConfig);

            fileConfig.setSourceRootPath(sourceRootPath);


            List<Meta.FileConfig.FileInfo> fileInfoList = new ArrayList<>();

            fileInfoList.add(fileInfo);

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

        TemplateMaker.makeTemplate(1803405813415804928L);
    }

}
