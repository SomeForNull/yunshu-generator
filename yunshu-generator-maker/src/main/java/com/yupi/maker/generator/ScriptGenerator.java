package com.yupi.maker.generator;
import cn.hutool.core.io.FileUtil;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class ScriptGenerator {
    public static void doGenerator(String outputPath,String jarPath) {
        //Linux脚本
        StringBuilder sb=new StringBuilder();
        sb.append("#!/bin/bash").append("\n");
        sb.append(String.format("java -jar %s \"$@\"",jarPath)).append("\n");
        FileUtil.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8),outputPath);
        //添加可执行权限
        Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxrwxrwx");
        try {
            Files.setPosixFilePermissions(Paths.get(outputPath),permissions);
        } catch (Exception e) {

        }
        //Windows脚本
        sb=new StringBuilder();
        sb.append("@echo off").append("\n");
        sb.append(String.format("java -jar %s %%*",jarPath)).append("\n");
        FileUtil.writeBytes(sb.toString().getBytes(StandardCharsets.UTF_8),outputPath+".bat");
        //Windows脚本
    }
}
