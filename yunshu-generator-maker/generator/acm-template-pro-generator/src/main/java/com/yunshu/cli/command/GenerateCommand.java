package com.yunshu.cli.command;

import cn.hutool.core.bean.BeanUtil;
import com.yunshu.generator.MainGenerator;
import com.yunshu.model.DataModel;
import freemarker.template.TemplateException;
import lombok.Data;
import picocli.CommandLine;
import java.io.IOException;
import java.util.concurrent.Callable;
@Data
@CommandLine.Command(name = "generate",description = "生成代码",mixinStandardHelpOptions = true)
public class GenerateCommand implements Callable<Integer> {


    @CommandLine.Option(names = {"-l","--loop"}, description = "是否生成循环", arity = "0..1", interactive = true, echo = true)
    private boolean loop = false;


    @CommandLine.Option(names = {"-a","--author"}, description = "作者注释", arity = "0..1", interactive = true, echo = true)
    private String author = "yupi";


    @CommandLine.Option(names = {"-o","--outputText"}, description = "输出信息", arity = "0..1", interactive = true, echo = true)
    private String outputText = "sum = ";


    @Override
    public Integer call() throws TemplateException, IOException {
        DataModel dataModel = new DataModel();
        BeanUtil.copyProperties(this, dataModel);
        MainGenerator.doGenerate(dataModel);
        return 0;
    }
}