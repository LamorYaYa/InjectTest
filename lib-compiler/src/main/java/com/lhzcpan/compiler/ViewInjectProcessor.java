package com.lhzcpan.compiler;

import com.google.auto.service.AutoService;
import com.lhzcpan.annotation.BindView;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * @author master
 * @date 2017/12/25
 */

@AutoService(Processor.class)
public class ViewInjectProcessor extends AbstractProcessor {

    /**
     * 存放同一 Class 下的所有注解
     */
    Map<String, List<VariableInfo>> classMap = new HashMap<>();

    /**
     * 存放 Class 对应的 TypeElement
     */
    Map<String, TypeElement> mTypeElement = new HashMap<>();

    private Filer filer;
    Elements elementUtils;


    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        filer = processingEnvironment.getFiler();
        elementUtils = processingEnvironment.getElementUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        collectInfo(roundEnvironment);
        writeToFile();
        return true;
    }

    void collectInfo(RoundEnvironment roundEnvironment) {
        classMap.clear();
        mTypeElement.clear();

        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            /**
             * 获取 BindView 注解的值
             */
            int viewId = element.getAnnotation(BindView.class).value();
            /**
             * 代表被注解的元素
             */
            VariableElement variableElement = (VariableElement) element;
            /**
             * 被注解元素所在的 Class
             */
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            /**
             * Class 的完整路径
             */
            String classFullName = typeElement.getQualifiedName().toString();
            /**
             * 遍历 Class 中所有被注解的元素
             */
            List<VariableInfo> variableList = classMap.get(classFullName);
            if (variableList == null) {
                variableList = new ArrayList<>();
                classMap.put(classFullName, variableList);
                // 保存 Class 对应要素(名称、完整路径。)
                mTypeElement.put(classFullName, typeElement);
            }
            VariableInfo variableInfo = new VariableInfo();
            variableInfo.setVariableElement(variableElement);
            variableInfo.setViewId(viewId);
            variableList.add(variableInfo);
        }
    }

    void writeToFile() {
        try {
            for (String classFullName : classMap.keySet()) {
                TypeElement typeElement = mTypeElement.get(classFullName);

                // 使用构造函数绑定数据
                MethodSpec.Builder constructor = MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(TypeName.get(typeElement.asType()), "activity").build());
                List<VariableInfo> variableList = classMap.get(classFullName);
                for (VariableInfo variableInfo : variableList) {
                    VariableElement variableElement = variableInfo.getVariableElement();
                    // 变量名称(比如: Textview mTextView 的 mTextView)
                    String variableName = variableElement.getSimpleName().toString();
                    // 变量类型的完整路径(例如: android.widget.TextView)
                    String variableFullName = variableElement.asType().toString();
                    // 在构造方法中赋值, 例如: activity.tv = (android.widget.TextView)activity.findViewById(215334);
                    constructor.addStatement("activity.$L = ($L) activity.findViewById($L)", variableName, variableFullName, variableInfo.getViewId());
                }
                // 构建 Class
                TypeSpec typeSpec = TypeSpec.classBuilder(typeElement.getSimpleName() + "$$ViewInjector")
                        .addModifiers(Modifier.PUBLIC)
                        .addMethod(constructor.build())
                        .build();
                // 与目标class放在同一个包下,解决Class属性的可访问性
                String packageFullName = elementUtils.getPackageOf(typeElement).getQualifiedName().toString();
                JavaFile javaFile = JavaFile.builder(packageFullName, typeSpec).build();
                // 生成 Class 文件
                javaFile.writeTo(filer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotationTypes = new LinkedHashSet<>();
        annotationTypes.add(BindView.class.getCanonicalName());
        return annotationTypes;
    }
}
