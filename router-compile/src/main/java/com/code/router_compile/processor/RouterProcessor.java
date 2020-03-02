package com.code.router_compile.processor;

import com.code.route_annotation.hit.Extra;
import com.code.route_annotation.hit.Router;
import com.code.route_annotation.hit.SceneTransition;
import com.code.router_compile.AnnotationProcessor;
import com.code.router_compile.helper.RouterActivityModel;
import com.code.router_compile.inter.IProcessor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;

/**
 * @Author: czp
 * @Description
 * @CreateDate: 2020/1/2 10:16
 */
public class RouterProcessor implements IProcessor {
    @Override
    public void process(RoundEnvironment roundEnv, AnnotationProcessor mAbstractProcessor) {
        //生成类三部，1创建类 2变量-修饰符 3方法(构造)-方法参数-方法返回值-修饰符
        String CLASS_NAME = "TRouter";
        //创建TRouter类
        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(CLASS_NAME).addModifiers(Modifier.PUBLIC, Modifier.FINAL).addJavadoc("全局路由 此类由apt自动生成");

        //成员
        FieldSpec fieldSpec = FieldSpec.builder(ParameterizedTypeName.get(HashMap.class, String.class, Object.class), "mCurActivityExtra")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .build();
        classBuilder.addField(fieldSpec);

        /**
         * public static void go(String name,HashMap extra,View view){
         *
         * }
         * */
        MethodSpec.Builder builder = MethodSpec.methodBuilder("go")
                .addJavadoc("由apt自动生成")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(String.class, "name")
                .addParameter(HashMap.class, "extra")
                .addParameter(ClassName.get("android.view", "View"), "view");

        MethodSpec.Builder builder1 = MethodSpec.methodBuilder("bind")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ClassName.get("android.app", "Activity"), "mContext");

        ArrayList<ClassName> names = new ArrayList<>();
        CodeBlock.Builder blockBuilderGo = CodeBlock.builder();
        CodeBlock.Builder blockBuilderBind = CodeBlock.builder();
        ClassName appClassName = ClassName.get("com.code.router_code", "BaseApp");
        blockBuilderGo.addStatement("mCurActivityExtra=extra");
        blockBuilderGo.addStatement("android.app.Activity mContext=$T.getAppContext().getCurActivity()", appClassName);
        blockBuilderGo.beginControlFlow(" switch(name)");
        blockBuilderBind.addStatement("if(mCurActivityExtra==null) return");
        blockBuilderBind.beginControlFlow(" switch(mContext.getClass().getSimpleName())");

        List<RouterActivityModel> mRouterActivityModels = new ArrayList<>();
        try {
            for (TypeElement e : ElementFilter.typesIn(roundEnv.getElementsAnnotatedWith(Router.class))) {
                ClassName currentType = ClassName.get(e);
                if (names.contains(currentType)) {
                    continue;
                }
                names.add(currentType);
                RouterActivityModel model = new RouterActivityModel();
                model.setElement(e);
                model.setActionName(e.getAnnotation(Router.class).path());
                List<Element> mExtraElements = new ArrayList<>();
                ArrayList<String> mExtraElementKeys = new ArrayList<>();
                //包裹注解元素的元素, 也就是其父元素, 比如注解了成员变量或者成员函数, 其上层就是该类
                for (Element elementChild : e.getEnclosedElements()) {
                    SceneTransition sceneTransition = elementChild.getAnnotation(SceneTransition.class);
                    if (sceneTransition != null) {
                        model.setSceneTransitionElementName(sceneTransition.value());
                        model.setSceneTransitionElement(elementChild);
                    }
                    Extra mExtraAnnotation = elementChild.getAnnotation(Extra.class);
                    if (mExtraAnnotation != null) {
                        mExtraElementKeys.add(mExtraAnnotation.value());
                        mExtraElements.add(elementChild);
                    }
                }
                model.setExtraElementKeys(mExtraElementKeys);
                model.setExtraElements(mExtraElements);
                boolean isNeedBind = (mExtraElementKeys != null && mExtraElementKeys.size() > 0
                        || model.getSceneTransitionElement() != null);
                model.setNeedBind(isNeedBind);
                mRouterActivityModels.add(model);
            }
            ClassName activityCompat = ClassName.get("android.support.v4.app", "ActivityCompat");
            ClassName intent = ClassName.get("android.content", "Intent");
            ClassName activityOptionsCompat = ClassName.get("android.support.v4.app", "ActivityOptionsCompat");
            for (RouterActivityModel item :
                    mRouterActivityModels) {
                blockBuilderGo.add("case $S: \n", item.getActionName());
                blockBuilderGo.add("mContext.startActivity(" +
                        "\nnew $L(mContext," +
                        "\n$L.class));", intent, item.getElement());
                blockBuilderGo.addStatement("\nbreak");
            }
            blockBuilderGo.addStatement("default: break");
            blockBuilderGo.endControlFlow();
            builder.addCode(blockBuilderGo.build());

            classBuilder.addMethod(builder.build());

            //增重跳转方法
            classBuilder.addMethod(MethodSpec.methodBuilder("go")
                    .addJavadoc("此方法由apt自动生成")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(String.class, "name")
                    .addParameter(HashMap.class, "extra")
                    .addCode("go(name,extra,null);\n")
                    .build());

            classBuilder.addMethod(MethodSpec.methodBuilder("go")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(String.class, "name")
                    .addCode("go(name,null,null);\n")
                    .build());

            JavaFile javaFile = JavaFile.builder("com.apt", classBuilder.build()).build();// 生成源代码
            javaFile.writeTo(mAbstractProcessor.mFiler);// 在 app module/build/generated/source/apt 生成一份源代码
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
