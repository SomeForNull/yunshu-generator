# ${name}

> ${description}
>
> 作者：${author}
>
> 基于 yunshu 的 [云舒代码生成器项目](https://github.com/SomeForNull/yunshu-generator) 制作，感谢您的使用！

可以通过命令行交互式输入的方式动态生成想要的项目代码

## 使用说明

执行项目根目录下的脚本文件：

```
generator <命令> <选项参数>
```

示例命令：

```
generator generate <#list modelConfig.models as modelInfo><#if modelInfo.abbr??>"-${modelInfo.abbr}",</#if> </#list>
```

## 参数说明
<#list modelConfig.models as modelInfo>
<#if modelInfo.groupKey??>
```
一组${modelInfo.description}的参数：
<#list modelInfo.models as subModelInfo>
${subModelInfo.type} ${subModelInfo.fieldName}：${subModelInfo.description}
</#list>
<#else>
${modelInfo.type} ${modelInfo.fieldName}：${modelInfo.description}
```
</#if>
</#list>