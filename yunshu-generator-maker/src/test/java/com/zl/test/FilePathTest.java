package com.zl.test;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;
import com.yupi.maker.template.model.TemplateMakerConfig;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FilePathTest {
    @Test
    public void test1(){
        String configStr = ResourceUtil.readUtf8Str("templateMaker.json");
        TemplateMakerConfig templateMakerConfig = JSONUtil.toBean(configStr, TemplateMakerConfig.class);
        System.out.println(templateMakerConfig.getOriginProjectPath());
        System.out.println(System.getProperty("user.dir"));

    }
}
