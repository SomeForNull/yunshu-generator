package com.yupi.generator;

import com.yupi.model.MainTemplateConfig;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * 动态文件生成器
 */
public class DynamicGenerator {
    public static void main(String[] args) throws IOException, TemplateException {
        String projectPath=System.getProperty("user.dir")+File.separator+"yunshu-generator-basic" ;
        String inputPath=projectPath+File.separator+"src/main/resources/templates/MainTemplate.java.ftl";
        String outputPath=projectPath+File.separator+"MainTemplate.java";
        MainTemplateConfig mainTemplateConfig = new MainTemplateConfig();
        mainTemplateConfig.setAuthor("云舒");
        mainTemplateConfig.setOutputText("我的输出结果");
        mainTemplateConfig.setLoop(true);
        doGenerator(inputPath,outputPath,mainTemplateConfig);

    }

    /**
     *
     * @param inputPath 模板文件输入路径
     * @param outputPath 输出路径
     * @param model 数据模型
     * @throws IOException
     * @throws TemplateException
     */
    public static void doGenerator(String inputPath,String outputPath,Object model) throws IOException, TemplateException {
        //  new出Configuration对象，参数为FreeMarker版本号。
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_22);

        File templateDir=new File(inputPath).getParentFile();
        // 指定模板文件所在的路径
        cfg.setDirectoryForTemplateLoading(templateDir);

        // 设置模板文件使用的字符集
        cfg.setDefaultEncoding("UTF-8");

        cfg.setNumberFormat("0.######");

        //创建模板对象，加载指定模板
        String templateName=new File(inputPath).getName();
        Template template = cfg.getTemplate(templateName);

        Writer out = new FileWriter(outputPath);
        //模板生成
        template.process(model, out);
        //关闭流
        out.close();
    }

}
