package com.yupi.maker.generator.main;

public class ZipGenerator extends GenerateTemplate{

    @Override
    protected String buildDist(String outputPath, String shellOutputFilePath, String jarPath, String sourceCopyPath) {
        String distPath = super.buildDist(outputPath, shellOutputFilePath, jarPath, sourceCopyPath);
        return buildZip(distPath);
    }
}
