package com.zl.test;

import org.junit.Test;

public class Test1 {
    @Test
    public void test1(){
        Person person1 = new Person();
        Person person2=person1;
        int a = person1.getA();
        Adr adr=person1.getAdr();
        adr.c=100;
        System.out.println(person1.getAdr().c);
    }

}
class Person{
    private int a=1;
    private int b=2;
    private Adr adr=new Adr();

    public Adr getAdr() {
        return adr;
    }

    public void setAdr(Adr adr) {
        this.adr = adr;
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }
}
class Adr{
    public int c=2;
}