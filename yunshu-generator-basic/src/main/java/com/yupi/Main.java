package com.yupi;

import com.yupi.cli.CommandExecutor;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) {
        //args=new String[]{"generate","-l","-o","-a"};
        args=new String[]{"generate","--help"};
        //args=new String[]{"list"};
        //args=new String[]{"config"};
        CommandExecutor commandExecutor = new CommandExecutor();
        commandExecutor.doExecutor(args);
    }
}