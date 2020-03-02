package com.code.router_compile;

import com.code.route_annotation.NewRouter;
import com.code.route_annotation.model.RouterMeta;
import com.code.router_compile.processor.RouterProcessor;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * @Author: czp
 * @Description
 * @CreateDate: 2020/1/2 10:15
 */
@AutoService(Processor.class)//自动生成 javax.annotation.processing.IProcessor 文件
/**
 处理器接收的参数 替代 {@link AbstractProcessor#getSupportedOptions()} 函数
 */
@SupportedOptions(AnnotationProcessor.ARGUMENTS_NAME)
@SupportedSourceVersion(SourceVersion.RELEASE_7)//java版本支持
@SupportedAnnotationTypes("com.code.route_annotation.NewRouter")
public class AnnotationProcessor extends AbstractProcessor {

    public static final String ARGUMENTS_NAME = "moduleName";
    public static final String ACTIVITY = "android.app.Activity";
    public static final String ISERVER = "com.code.router_code.template.IServer";
    public static final String IGROUP_PATH = "com.code.router_code.template.IGroup";
    public static final String IROOT_PATH = "com.code.router_code.template.IRoot";
    public static final String GROUP_CLASS_NAME = "Router_Group_";
    public static final String ROOT_CLASS_NAME = "Router_Root_";

    private HashMap<String, List<RouterMeta>> groupMap = new HashMap<>();
    /**
     * 根路径
     * 类包名
     */
    private HashMap<String, String> routerMap = new HashMap<>();
    public Filer mFiler; //文件相关的辅助类
    public Elements mElements; //元素相关的辅助类
    public Messager mMessager; //日志相关的辅助类
    private String moduleName;
    private Types typeUtils;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        new RouterProcessor().process(roundEnv, this);
        if (!Utils.isEmpty(annotations)) {
            //rootElements 得到的就是注解的包名.类的集合
            Set<? extends Element> rootElements = roundEnv.getElementsAnnotatedWith(NewRouter.class);
            if (!Utils.isEmpty(rootElements)) {
                processorRoute(rootElements);
            }

            return true;
        }
        return false;
    }

    private void processorRoute(Set<? extends Element> rootElements) {
        //获取Activity这个类的节点信息
        TypeElement activity = mElements.getTypeElement(ACTIVITY);
        TypeElement iServer = mElements.getTypeElement(ISERVER);
        for (Element element : rootElements) {
            RouterMeta routerMeta = null;
            //包名.类名
            TypeMirror typeMirror = element.asType();
            NewRouter router = element.getAnnotation(NewRouter.class);
            //判断注解是否使用在了Activity/iServer的子类上
            if (typeUtils.isSubtype(typeMirror, activity.asType())) {
                routerMeta = new RouterMeta(RouterMeta.Type.Activity, router, element);
            } else if (typeUtils.isSubtype(typeMirror, iServer.asType())) {
                routerMeta = new RouterMeta(RouterMeta.Type.SERVICE, router, element);
            } else {
                throw new RuntimeException("Just support Activity or IService Route: " + element);
            }
            categories(routerMeta);
        }

        //1.生成对应路由根目录的class文件,并保存Activity信息 iGroupTypeElement表示IGroup接口路径
        TypeElement iGroupTypeElement = mElements.getTypeElement(IGROUP_PATH);

        //2.生成Root Class文件,并保存module中以根本目录下路由的信息 iRootTypeElement表示IRoot接口路径
        TypeElement iRootTypeElement = mElements.getTypeElement(IROOT_PATH);

        createGroupClass(iGroupTypeElement);
        createRootClass(iRootTypeElement, iGroupTypeElement);
    }

    private void createRootClass(TypeElement rootTypeElement, TypeElement iGroupTypeElement) {
        //创建参数类型 Map<String,Class<? extends IRouteGroup>> routes>
        //Wildcard 通配符
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(HashMap.class), ClassName.get(String.class),
                ParameterizedTypeName.get(ClassName.get(Class.class), WildcardTypeName.subtypeOf(ClassName.get(iGroupTypeElement))));
        ParameterSpec routes = ParameterSpec.builder(parameterizedTypeName, "routes").build();
        //实现loadInfo方法
        MethodSpec.Builder loadInfoMethod = MethodSpec.methodBuilder("loadInfo")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addParameter(routes);
        for (Map.Entry<String, String> entity : routerMap.entrySet()) {
            loadInfoMethod.addStatement("routes.put($S,$T.class)",
                    entity.getKey(), ClassName.get("com.newrouter", entity.getValue()));
        }
        //创建Router_Root_moduleName的名称，实现IRoot接口
        TypeSpec build = TypeSpec.classBuilder(ROOT_CLASS_NAME + moduleName)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(rootTypeElement))
                .addMethod(loadInfoMethod.build())
                .build();
        try {
            JavaFile.builder("com.newrouter", build).build().writeTo(mFiler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createGroupClass(TypeElement iGroupTypeElement) {
        ParameterizedTypeName typeName = ParameterizedTypeName.get(HashMap.class, String.class, RouterMeta.class);
        ParameterSpec atlas = ParameterSpec.builder(typeName, "atlas").build();
        for (Map.Entry<String, List<RouterMeta>> entry : groupMap.entrySet()) {
            //创建Router_Group_根目录名称.class，实现IGroup接口
            String groupClassName = GROUP_CLASS_NAME + entry.getKey();
            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(groupClassName)
                    .addSuperinterface(ClassName.get(iGroupTypeElement))
                    .addModifiers(Modifier.PUBLIC);
            //实现loadInfo方法
            MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("loadInfo")
                    .addModifiers(Modifier.PUBLIC)
                    .addAnnotation(Override.class)
                    .addParameter(atlas);
            for (RouterMeta entity : entry.getValue()) {
                //添加函数体，保存路由信息
                methodBuilder.addStatement("atlas.put($S,$T.build($T.$L,$T.class,$S,$S))",
                        entity.getPath(),
                        ClassName.get(RouterMeta.class),
                        ClassName.get(RouterMeta.Type.class),
                        entity.getType(),
                        ClassName.get(((TypeElement) entity.getElement())),
                        entity.getPath(),
                        entity.getGroup());
            }
            classBuilder.addMethod(methodBuilder.build());
            try {
                JavaFile.builder("com.newrouter", classBuilder.build()).build().writeTo(mFiler);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //保存分组的class类
            routerMap.put(entry.getKey(), groupClassName);
        }
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        //文件辅助类
        mFiler = processingEnv.getFiler();
        //元素辅助类
        mElements = processingEnv.getElementUtils();
        //日志赋值类
        mMessager = processingEnv.getMessager();
        //类信息赋值类
        typeUtils = processingEnv.getTypeUtils();
        //参数是模块名 为了防止多模块/组件化开发的时候 生成相同的 xx$$ROOT$$文件
        Map<String, String> options = processingEnv.getOptions();
        if (!Utils.isEmpty(options)) {
            moduleName = options.get(ARGUMENTS_NAME);
        }
        if (Utils.isEmpty(moduleName)) {
            throw new RuntimeException("Not set processor moudleName option !");
        }
    }

    /**
     * 保存各个跟目录下的路由信息
     * @param routerMeta
     */
    private void categories(RouterMeta routerMeta) {
        if (routeVerify(routerMeta)) {
            List<RouterMeta> routerMetas = groupMap.get(routerMeta.getGroup());
            if (Utils.isEmpty(routerMetas)) {
                ArrayList<RouterMeta> metas = new ArrayList<>();
                metas.add(routerMeta);
                groupMap.put(routerMeta.getGroup(), metas);
            } else {
                routerMetas.add(routerMeta);
            }
        }
    }

    /**
     * 验证路由的合法性
     *
     * @param routerMeta
     * @return
     */
    private boolean routeVerify(RouterMeta routerMeta) {
        String path = routerMeta.getPath();
        String group = routerMeta.getGroup();
        //开头必须/
        if (!path.startsWith("/")) {
            return false;
        }
        //如果group为空从path找路径
        if (Utils.isEmpty(group)) {
            String defGroup = path.substring(1, path.indexOf("/", 1));
            if (Utils.isEmpty(defGroup)) {
                return false;
            }
            routerMeta.setGroup(defGroup);
        }
        return true;
    }
}
