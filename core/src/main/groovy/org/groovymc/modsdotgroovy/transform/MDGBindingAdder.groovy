package org.groovymc.modsdotgroovy.transform

import groovy.transform.CompileStatic
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

@CompileStatic
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
@GroovyASTTransformationClass('org.groovymc.modsdotgroovy.transform.MDGBindingAdderASTTransformation')
@interface MDGBindingAdder {
    String className() default 'ModsDotGroovy'
    String methodName() default 'make'
}
