package com.yupi.maker;

import com.yupi.maker.generator.main.GenerateTemplate;
import com.yupi.maker.generator.main.MainGenerator;
import com.yupi.maker.generator.main.ZipGenerator;
import freemarker.template.TemplateException;

import java.io.IOException;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws TemplateException, IOException, InterruptedException {

        //GenerateTemplate generateTemplate = new MainGenerator();
        GenerateTemplate generateTemplate = new ZipGenerator();
        generateTemplate.doGenerate();
    }
}