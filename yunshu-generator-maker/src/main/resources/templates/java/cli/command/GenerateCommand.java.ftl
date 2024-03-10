package ${basePackage}.cli.command;

import cn.hutool.core.bean.BeanUtil;
import ${basePackage}.generator.MainGenerator;
import ${basePackage}.model.DataModel;
import freemarker.template.TemplateException;
import lombok.Data;
import picocli.CommandLine;
import java.io.IOException;
import java.util.concurrent.Callable;
@Data
@CommandLine.Command(name = "generate",description = "生成代码",mixinStandardHelpOptions = true)
public class GenerateCommand implements Callable<Integer> {

    <#list modelConfig.models as modelInfo>

    @CommandLine.Option(names = {<#if modelInfo.abbr??>"-${modelInfo.abbr}",</#if>"--${modelInfo.fieldName}"}, <#if modelInfo.description??>description = "${modelInfo.description}", </#if>arity = "0..1", interactive = true, echo = true)
    private ${modelInfo.type} ${modelInfo.fieldName}<#if modelInfo.defaultValue??> = ${modelInfo.defaultValue?c}</#if>;

    </#list>

    @Override
    public Integer call() throws TemplateException, IOException {
        DataModel dataModel = new DataModel();
        BeanUtil.copyProperties(this, dataModel);
        MainGenerator.doGenerate(dataModel);
        return 0;
    }
}