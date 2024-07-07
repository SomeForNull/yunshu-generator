package com.yupi.maker.meta;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.json.JSONUtil;

public class MetaManager {
    //
    private static volatile Meta meta;
    public static Meta getMetaObject(){
        if(meta==null){
            synchronized (MetaManager.class){
                if(meta==null){
                    meta = initMeta();
                }
            }
        }
    return meta;
    }
    public static Meta initMeta(){
        String metaJson = ResourceUtil.readUtf8Str("springboot-init-meta.json");
        Meta newMeta = JSONUtil.toBean(metaJson, Meta.class);
        //校验meta
        MetaValidator.doValidAndFill(newMeta);
        return newMeta;
    }
}
