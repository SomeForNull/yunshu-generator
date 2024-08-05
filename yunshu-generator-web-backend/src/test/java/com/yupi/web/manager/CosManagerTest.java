package com.yupi.web.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CosManagerTest {

    @Resource
    private CosManager cosManager;

    @Test
    void deleteObject() {
        cosManager.deleteObject("/test/PixPin_2024-06-18_13-05-30.png");
    }

    @Test
    void deleteObjects() {
        cosManager.deleteObjects(Arrays.asList("test/微信图片_20240602133029.jpg",
                "test/下载.png"
        ));
    }

    @Test
    void deleteDir() {
        cosManager.deleteDir("/test/");
    }
}
