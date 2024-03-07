package com.yupi.maker.cli.command;

import cn.hutool.core.bean.BeanUtil;
import com.yupi.maker.generator.file.FileGenerator;
import com.yupi.maker.model.DataModel;
import freemarker.template.TemplateException;
import lombok.Data;
import picocli.CommandLine;

import java.io.IOException;
import java.util.concurrent.Callable;
@Data
@CommandLine.Command(name = "generate",mixinStandardHelpOptions = true)
public class GenerateCommand implements Callable<Integer> {

    /**
     * 作者
     */
    @CommandLine.Option(names = {"-a", "--author"}, description = "改变作者", arity = "0..1", interactive = true, echo = true, prompt = "请输入作者：")
    private String author = "云舒";
    /**
     * 输出信息
     */
    @CommandLine.Option(names = {"-o", "--outputText"}, description = "改变输出结果", arity = "0..1", interactive = true, echo = true, prompt = "输出结果：")
    private String outputText = "输出结果";
    /**
     * 是否循环
     */
    @CommandLine.Option(names = {"-l", "--loop"}, description = "是否循环", arity = "0..1", interactive = true, echo = true, prompt = "是否循环：")
    private boolean loop = true;


    @Override
    public Integer call() throws TemplateException, IOException {
        DataModel dataModel = new DataModel();
        BeanUtil.copyProperties(this, dataModel);
        FileGenerator.doGenerator(dataModel);
        return 0;
    }
}
