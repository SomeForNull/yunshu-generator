package com.yupi.maker.template;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import com.yupi.maker.template.enums.FileFilterRangeEnum;
import com.yupi.maker.template.enums.FileFilterRuleEnum;
import com.yupi.maker.template.model.FileFilterConfig;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class FileFilter {
    /**
     * 某个文件或者目录进行过滤
     * @param filePath
     * @param fileFilterConfigList
     * @return 文件列表
     */
    public static List<File> doFilter(String filePath, List<FileFilterConfig> fileFilterConfigList) {
    //根据路径获取所有文件
        List<File> fileList = FileUtil.loopFiles(filePath);
       return fileList.stream().filter(file -> doSingleFileFilter(fileFilterConfigList, file)).collect(Collectors.toList());

    }

    /**
     * 单个文件过滤
     * @param fileFilterConfigList
     * @param file
     * @return
     */
    public static boolean doSingleFileFilter(List<FileFilterConfig> fileFilterConfigList, File file) {
        String fileName = file.getName();
        String fileContent = FileUtil.readUtf8String(file);

        //所有过滤器过滤结束后的结果
        boolean result=true;

        if (CollUtil.isEmpty(fileFilterConfigList)) {
            return true;
        }
        for (FileFilterConfig fileFilterConfig : fileFilterConfigList) {
            String range = fileFilterConfig.getRange();
            String rule = fileFilterConfig.getRule();
            String value = fileFilterConfig.getValue();


            FileFilterRangeEnum filterRangeEnum = FileFilterRangeEnum.getEnumByValue(range);
            if (filterRangeEnum == null) {
                continue;
            }
            //要过滤的原内容
            String content=fileName;
            switch (filterRangeEnum) {
                case FILE_NAME:
                    content=fileName;
                    break;
                case FILE_CONTENT:
                    content=fileContent;
                    break;
                default:
            }
            FileFilterRuleEnum filterRuleEnum = FileFilterRuleEnum.getEnumByValue(rule);
            if (filterRuleEnum == null) {
                continue;
            }
            switch (filterRuleEnum) {
                case CONTAINS:
                    result=content.contains(value);
                    break;
                case STARTS_WITH:
                    result=content.startsWith(value);
                    break;
                case ENDS_WITH:
                    result=content.endsWith(value);
                    break;
                case REGEX:
                    result=content.matches(value);
                    break;
                case EQUALS:
                    result=content.equals(value);
                    break;
                default:
            }
            //有一个过滤器不满足条件，则返回false
            if(!result){
                return false;
            }
        }
        //都满足
        return true;
    }
}
