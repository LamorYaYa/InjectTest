package com.lhzcpan.compiler;

import javax.lang.model.element.VariableElement;

/**
 * @author master
 */
public class VariableInfo {
    /**
     * 被注解的 View 的 Id 值
     */
    int viewId;
    /**
     * 被注解 View 的信息：变量名称 类型
     */
    VariableElement variableElement;

    public int getViewId() {
        return viewId;
    }

    public void setViewId(int viewId) {
        this.viewId = viewId;
    }

    public VariableElement getVariableElement() {
        return variableElement;
    }

    public void setVariableElement(VariableElement variableElement) {
        this.variableElement = variableElement;
    }
}
