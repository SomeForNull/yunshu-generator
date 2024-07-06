package com.yupi.maker.template;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.yupi.maker.meta.Meta;
import com.yupi.maker.meta.enums.FileGenerateTypeEnum;
import com.yupi.maker.meta.enums.FileTypeEnum;
import com.yupi.maker.template.enums.FileFilterRangeEnum;
import com.yupi.maker.template.enums.FileFilterRuleEnum;
import com.yupi.maker.template.model.*;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 模板制作工具
 */
public class TemplateMaker {
    /**
     * 模版制作方法
     * @return
     */
    public static long makeTemplate(TemplateMakerConfig templateMakerConfig){
        Long id = templateMakerConfig.getId();
        Meta meta = templateMakerConfig.getMeta();
        String originProjectPath = templateMakerConfig.getOriginProjectPath();
        TemplateMakerFileConfig fileConfig = templateMakerConfig.getFileConfig();
        TemplateMakerModelConfig modelConfig = templateMakerConfig.getModelConfig();
        TemplateMakerOutputConfig outputConfig = templateMakerConfig.getOutputConfig();
        return makeTemplate(originProjectPath, fileConfig, modelConfig,outputConfig, meta, id);
    }
    /**
     * 模版制作方法
     * @param originalProjectPath
     * @param templateMakerFileConfig
     * @param templateMakerModelConfig
     * @param templateMakerOutputConfig
     * @param meta
     * @param id
     * @return
     */
    public static long makeTemplate(String originalProjectPath, TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, TemplateMakerOutputConfig templateMakerOutputConfig, Meta meta, Long id) {
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
            //ResourceUtil.getResource(normalPath, baseClass);相对于资源路径获取的
            FileUtil.copy(originalProjectPath, templatePath, true);
        }


        //输入信息
        //一、处理模型信息
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = makeModelTempaltes(templateMakerModelConfig);

        //2、输入文件信息
        //要挖坑的根目录
        String  sourceRootPath=FileUtil.loopFiles(new File(templatePath),1,null)
                .stream()
                .filter(File::isDirectory)
                .findFirst()
                .orElseThrow(RuntimeException::new)
                .getAbsolutePath();

        sourceRootPath = sourceRootPath.replaceAll("\\\\", "/");
        //二、生成文件模板
        List<Meta.FileConfig.FileInfo> newFileInfos = makeFileTempaltes(templateMakerFileConfig, templateMakerModelConfig, sourceRootPath);
        //三、生成配置文件
        String metaPath = templatePath + File.separator + "meta.json";
        //已有meta文件，不是第一次制作
        if (FileUtil.exist(metaPath)) {
            meta = JSONUtil.toBean(FileUtil.readUtf8String(metaPath), Meta.class);
            //1.追加配置参数
            List<Meta.FileConfig.FileInfo> oldFiles = meta.getFileConfig().getFiles();
            oldFiles.addAll(newFileInfos);
            List<Meta.ModelConfig.ModelInfo> oldModels = meta.getModelConfig().getModels();
            oldModels.addAll(newModelInfoList);

            //配置去重
            meta.getModelConfig().setModels(distinctModels(oldModels));
            meta.getFileConfig().setFiles(distinctFiles(oldFiles));


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
            modelInfoList.addAll(newModelInfoList);
            modelConfig.setModels(modelInfoList);
        }
        //额外的输出配置
        if(templateMakerOutputConfig!=null){
            //文件外层和分组去重
            if(templateMakerOutputConfig.isRemoveGroupFilesFromRoot()){
                List<Meta.FileConfig.FileInfo> fileInfoList = meta.getFileConfig().getFiles();
                meta.getFileConfig().setFiles(TemplateMakerUtils.removeGroupFilesFromRoot(fileInfoList));

            }
        }
        //输出元信息文件
        FileUtil.writeUtf8String(JSONUtil.toJsonPrettyStr(meta), metaPath);
        return id;
    }

    /**
     * 生成模型
     * @param templateMakerModelConfig
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> makeModelTempaltes(TemplateMakerModelConfig templateMakerModelConfig) {
        // 本次新增的模型配置列表
        List<Meta.ModelConfig.ModelInfo> newModelInfoList = new ArrayList<>();
        //非空校验
        if(templateMakerModelConfig == null){
            return newModelInfoList;
        }
        List<TemplateMakerModelConfig.ModelInfoConfig> models = templateMakerModelConfig.getModels();
        if(CollUtil.isEmpty(models)){
            return newModelInfoList;
        }
        // 转换为配置接受的 ModelInfo 对象
        List<Meta.ModelConfig.ModelInfo> inputModelInfoList = models.stream().map(modelInfoConfig -> {
            Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
            BeanUtil.copyProperties(modelInfoConfig, modelInfo);
            return modelInfo;
        }).collect(Collectors.toList());

        //如果是模型組
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        if(modelGroupConfig != null){
            String condition = modelGroupConfig.getCondition();
            String groupKey = modelGroupConfig.getGroupKey();
            String groupName = modelGroupConfig.getGroupName();
            Meta.ModelConfig.ModelInfo modelInfo = new Meta.ModelConfig.ModelInfo();
            modelInfo.setCondition(condition);
            modelInfo.setGroupKey(groupKey);
            modelInfo.setGroupName(groupName);
            //模型全放到一个分组内
            modelInfo.setModels(inputModelInfoList);
            newModelInfoList = new ArrayList<>();
            newModelInfoList.add(modelInfo);
        }else {
            newModelInfoList.addAll(inputModelInfoList);
        }
        return newModelInfoList;
    }

    /**
     * 生成多个文件
     * @param templateMakerFileConfig
     * @param templateMakerModelConfig
     * @param sourceRootPath
     * @return
     */
    private static List<Meta.FileConfig.FileInfo> makeFileTempaltes(TemplateMakerFileConfig templateMakerFileConfig, TemplateMakerModelConfig templateMakerModelConfig, String sourceRootPath) {
        //支持遍历多个文件
        List<Meta.FileConfig.FileInfo> newFileInfos = new ArrayList<>();
        //非空校验
        if(templateMakerFileConfig == null){
            return newFileInfos;
        }
        List<TemplateMakerFileConfig.FileInfoConfig> fileInfoConfig = templateMakerFileConfig.getFiles();
        if(CollUtil.isEmpty(fileInfoConfig)){
            return newFileInfos;
        }
        for ( TemplateMakerFileConfig.FileInfoConfig fileConfig : fileInfoConfig) {
            String inputFileAbsolutePath = sourceRootPath + File.separator + fileConfig.getPath();
            //传入绝对路径
            List<File> fileList = FileFilter.doFilter(inputFileAbsolutePath, fileConfig.getFileFilterConfigList());
            //不处理已经生成的模板文件
            fileList = fileList.stream()
                    .filter(file -> !file.getAbsolutePath().endsWith(".ftl"))
                    .collect(Collectors.toList());
            for (File file : fileList) {
                Meta.FileConfig.FileInfo fileInfo = makeFileTemplate(templateMakerModelConfig, file, sourceRootPath);
                newFileInfos.add(fileInfo);
            }
        }
        //如果是文件組
        TemplateMakerFileConfig.FileGroupConfig fileGroupConfig = templateMakerFileConfig.getFileGroupConfig();
        if(fileGroupConfig != null){
            String condition = fileGroupConfig.getCondition();
            String groupKey = fileGroupConfig.getGroupKey();
            String groupName = fileGroupConfig.getGroupName();
            Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
            fileInfo.setCondition(condition);
            fileInfo.setGroupKey(groupKey);
            fileInfo.setGroupName(groupName);
            //文件全放到一个分组内
            fileInfo.setFiles(newFileInfos);
            newFileInfos = new ArrayList<>();
            newFileInfos.add(fileInfo);
        }
        return newFileInfos;
    }

    /**
     * 制作模板文件
     * @param templateMakerModelConfig
     * @param inputFilePath
     * @param sourceRootPath
     * @return
     */
    private static Meta.FileConfig.FileInfo makeFileTemplate(TemplateMakerModelConfig templateMakerModelConfig, File inputFilePath,String sourceRootPath) {
        String fileInputPath = inputFilePath.getAbsolutePath().replaceAll("\\\\","/").replace(sourceRootPath+"/","");

        //要输出的文件
        String fileOutputPath = fileInputPath + ".ftl";

        //二、使用字符串替换，生成模板文件
        String fileInputAbsolutePath = inputFilePath.getAbsolutePath();
        String fileOutputAbsolutePath = inputFilePath.getAbsolutePath()+".ftl";
        String fileContent = FileUtil.readUtf8String(fileInputAbsolutePath);
        //如果已有模板文件，表示不是第一次制作，则在原有模板文件上修改
        boolean hasTemplateFile = FileUtil.exist(fileOutputAbsolutePath);
        if (hasTemplateFile) {
            fileContent = FileUtil.readUtf8String(fileOutputAbsolutePath);
        }
        // 支持多个模型：对同一个文件的内容，遍历模型进行多轮替换
        TemplateMakerModelConfig.ModelGroupConfig modelGroupConfig = templateMakerModelConfig.getModelGroupConfig();
        String newFileContent = fileContent;
        String replacement;
        for (TemplateMakerModelConfig.ModelInfoConfig modelInfoConfig : templateMakerModelConfig.getModels()) {
            // 不是分组
            if (modelGroupConfig == null) {
                replacement = String.format("${%s}", modelInfoConfig.getFieldName());
            } else {
                // 是分组
                String groupKey = modelGroupConfig.getGroupKey();
                // 注意挖坑要多一个层级
                replacement = String.format("${%s.%s}", groupKey, modelInfoConfig.getFieldName());
            }
            // 多次替换
            newFileContent = StrUtil.replace(newFileContent, modelInfoConfig.getReplaceText(), replacement);
        }

        //文件配置信息
        Meta.FileConfig.FileInfo fileInfo = new Meta.FileConfig.FileInfo();
        fileInfo.setInputPath(fileOutputPath);
        fileInfo.setOutputPath(fileInputPath);
        fileInfo.setType(FileTypeEnum.File.getValue());
        fileInfo.setGenerateType(FileGenerateTypeEnum.DYNAMIC.getValue());

        //是否更改了文件内容
        boolean contentEquals = newFileContent.equals(fileContent);
        // 之前不存在模板文件，并且没有更改文件内容，则为静态生成
        if (!hasTemplateFile) {
            if (contentEquals) {
                //将inputPath设置为原始文件路径 不是ftl文件
                fileInfo.setInputPath(fileInputPath);
                fileInfo.setGenerateType(FileGenerateTypeEnum.STATIC.getValue());
            } else {
                // 没有模板文件，需要挖坑，生成模板文件
                FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
            }
        } else if (!contentEquals) {
            // 有模板文件，且增加了新坑，生成模板文件
            FileUtil.writeUtf8String(newFileContent, fileOutputAbsolutePath);
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
        //将文件分为有分组和无分组
        //1.有分组
        Map<String, List<Meta.FileConfig.FileInfo>> groupKeyFileInfoListMap = fileInfoList
                .stream()
                .filter(fileInfo -> StrUtil.isNotBlank(fileInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.FileConfig.FileInfo::getGroupKey)
                );
        //将同组文件合并
        // 保存每个组对应的合并后的对象 map
        Map<String, Meta.FileConfig.FileInfo> groupKeyMergedFileInfoMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.FileConfig.FileInfo>> entry : groupKeyFileInfoListMap.entrySet()) {
            List<Meta.FileConfig.FileInfo> tempFileInfoList = entry.getValue();
            List<Meta.FileConfig.FileInfo> newFileInfoList=new ArrayList<>(tempFileInfoList.stream().flatMap(fileInfo -> fileInfo.getFiles().stream())
                    .collect(
                            Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, fileInfo -> fileInfo, (e, r) -> r))
                    .values()
            );
            //每个组取最新的fileInfo
            Meta.FileConfig.FileInfo newFileInfo = CollUtil.getLast(tempFileInfoList);
            //设置files
            newFileInfo.setFiles(newFileInfoList);
            groupKeyMergedFileInfoMap.put(entry.getKey(), newFileInfo);
        }
        ArrayList<Meta.FileConfig.FileInfo> resultList = new ArrayList<>(groupKeyMergedFileInfoMap.values());

        resultList.addAll(new ArrayList<>(fileInfoList.stream()
                .filter(fileInfo -> StrUtil.isBlank(fileInfo.getGroupKey()))
                .collect(
                        Collectors.toMap(Meta.FileConfig.FileInfo::getOutputPath, fileInfo -> fileInfo, (e, r) -> r)
                ).values()));
        return resultList;
    }
    /**
     * 模型去重
     * @param modelInfoList
     * @return
     */
    private static List<Meta.ModelConfig.ModelInfo> distinctModels(List<Meta.ModelConfig.ModelInfo> modelInfoList) {
        //将模型分为有分组和无分组
        //1.有分组
        Map<String, List<Meta.ModelConfig.ModelInfo>> groupKeyModelInfoListMap = modelInfoList
                .stream()
                .filter(modelInfo -> StrUtil.isNotBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.groupingBy(Meta.ModelConfig.ModelInfo::getGroupKey)
                );
        //将同组文件合并
        // 保存每个组对应的合并后的对象 map
        Map<String, Meta.ModelConfig.ModelInfo> groupKeyMergedModelInfoMap = new HashMap<>();
        for (Map.Entry<String, List<Meta.ModelConfig.ModelInfo>> entry : groupKeyModelInfoListMap.entrySet()) {
            List<Meta.ModelConfig.ModelInfo> tempFileInfoList = entry.getValue();
            List<Meta.ModelConfig.ModelInfo> newModelInfoList=new ArrayList<>(tempFileInfoList.stream().flatMap(modelInfo -> modelInfo.getModels().stream())
                    .collect(
                            Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, modelInfo -> modelInfo, (e, r) -> r))
                    .values()
            );
            //每个组取最新的modelInfo
            Meta.ModelConfig.ModelInfo newModelInfo = CollUtil.getLast(tempFileInfoList);
            //设置files
            newModelInfo.setModels(newModelInfoList);
            groupKeyMergedModelInfoMap.put(entry.getKey(), newModelInfo);
        }
        ArrayList<Meta.ModelConfig.ModelInfo> resultList = new ArrayList<>(groupKeyMergedModelInfoMap.values());

        resultList.addAll(new ArrayList<>(modelInfoList.stream()
                .filter(modelInfo -> StrUtil.isBlank(modelInfo.getGroupKey()))
                .collect(
                        Collectors.toMap(Meta.ModelConfig.ModelInfo::getFieldName, modelInfo -> modelInfo, (e, r) -> r)
                ).values()));
        return resultList;
    }

}
