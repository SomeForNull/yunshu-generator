package com.yupi.generator;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * 静态文件生成器
 * */
public class StaticGenerator {
    public static void main(String[] args) {
        String projectPath = System.getProperty("user.dir");
        System.out.println(projectPath);
        //输入路径
        String inputPath = projectPath+File.separator+"yunshu-generator-demo-projects"+ File.separator+"acm-template";
        //输出路径
        String outputPath = projectPath;
        // 方法一
        // copyFileByHutool(inputPath,outputPath);
        //方法二
        copyFilesByRecursive(inputPath,outputPath);
    }

    /**
     * 拷贝文件(hutool实现，会将输入目录完整拷贝到输出目录下)
     * @param inputPath 输入路径
     * @param outputPath 输出路径
     */
    public static void copyFileByHutool(String inputPath,String outputPath){
        FileUtil.copy(inputPath,outputPath,false);
    }

    /**
     * 通过输入目标路径 复制到 目标路径位置
     * @param inputPath
     * @param outputPath
     */
    public static void copyFilesByRecursive(String inputPath,String outputPath){
        File inputFile=new File(inputPath);
        File outputFile=new File(outputPath);
        try {
            copyFileByRecursive(inputFile,outputFile);
        } catch (IOException e) {
            System.err.println("文件复制失败！");
            e.printStackTrace();
        }
    }



    /**
     * 用递归复制文件或者目录
     * @param inputFile 源文件
     * @param outputFile 目标文件
     * @throws IOException
     */
    public static void copyFileByRecursive(File inputFile,File outputFile) throws IOException {
            //区分是文件还是目录
            if(inputFile.isDirectory()){
                File destOutputFile=new File(outputFile,inputFile.getName());
                //如果是目录，首先创建目录
                if(!destOutputFile.exists()){
                    destOutputFile.mkdirs();
                }
                //获取子目录和文件
                File[] files=inputFile.listFiles();
                //如果没有直接返回
                if(ArrayUtil.isEmpty(files)){
                    return;
                }
                //递归遍历
                for (File file : files) {
                    copyFileByRecursive(file,destOutputFile);
                }
            }else
            //不是目录直接复制
            {
                Path destPath = outputFile.toPath().resolve(inputFile.getName());//resolve 路径连接
                Files.copy(inputFile.toPath(),destPath, StandardCopyOption.REPLACE_EXISTING);
            }
    }

}
