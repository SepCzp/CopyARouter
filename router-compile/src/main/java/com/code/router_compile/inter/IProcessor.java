package com.code.router_compile.inter;

import com.code.router_compile.AnnotationProcessor;

import javax.annotation.processing.RoundEnvironment;

/**
 * @Author: czp
 * @Description 注解处理器接口
 * @CreateDate: 2020/1/2 10:14
 */
public interface IProcessor {
    void process(RoundEnvironment roundEnv, AnnotationProcessor mAbstractProcessor);

}
