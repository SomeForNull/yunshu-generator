package com.yupi.cli.example;

import lombok.Data;
import picocli.CommandLine;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
@Data
class Login implements Callable<Integer> {
    @CommandLine.Option(names = {"-u", "--user"}, description = "User name")
    private String user;

    @CommandLine.Option(names = {"-p", "--password"}, description = "Passphrase", arity = "0..1", interactive = true, echo = false, prompt = "请输入密码：")
    private String password;

    @CommandLine.Option(names = {"-cp", "--checkPassword"}, description = "check Password", interactive = true, prompt = "请再次输入密码：")
    private String checkPassword;

    public Integer call(){
        System.out.println("pwd = " + password);
        System.out.println("checkPassword = " + checkPassword);
        return 0;
    }

    public static void main(String[] args) throws NoSuchFieldException {
        Class loginClass = Login.class;
        Field password1 = loginClass.getDeclaredField("password");
        CommandLine.Option option = password1.getAnnotation(CommandLine.Option.class);
        List<String> list = Arrays.asList(args);
        for (String name : option.names()) {
            if (list.contains(name)) {
                new CommandLine(new Login()).execute("-u", "yunshu", "-p", "-cp");
                return;
            }
        }
        new CommandLine(new Login()).execute("-u", "yunshu", option.names()[0], "-cp");

    }
}