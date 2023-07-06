package org.groovymc.modsdotgroovy.transform

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotatedNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * This AST transformation adds a Binding parameter to the ModsDotGroovy.make(Closure) method call.
 *
 * Example in:
 * ModsDotGroovy.make {}
 *
 * Example out:
 * ModsDotGroovy.make({}, this.getBinding())
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class MDGBindingAdderASTTransformation extends AbstractASTTransformation {

    @Override
    void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source)

        final AnnotationNode annotation = nodes[0] as AnnotationNode
        final AnnotatedNode targetNode = nodes[1] as AnnotatedNode

        // make sure this annotation is only applied to classes
        final ClassNode targetClass
        if (targetNode instanceof ClassNode) {
            targetClass = targetNode as ClassNode
        } else {
            targetClass = null // to make the compiler happy
            addError('The @MDGBindingAdder annotation can only be used on classes.', targetNode)
            return
        }

        // and only applied to scripts
        final MethodNode scriptBody = targetClass.getMethod('run', Parameter.EMPTY_ARRAY)
        if (scriptBody === null) {
            addError('The @MDGBindingAdder annotation can only be used on classes with a run() method.', targetNode)
            return
        } else if (!scriptBody.isScriptBody()) {
            addError('The @MDGBindingAdder annotation can only be used on script classes.', targetNode)
            return
        }

        final String className = getMemberStringValue(annotation, 'className', 'ModsDotGroovy')
        final String methodName = getMemberStringValue(annotation, 'methodName', 'make')

        scriptBody.code.visit(new ScriptTransformer(source, className, methodName))
    }

    private static class ScriptTransformer extends ClassCodeExpressionTransformer {
        private final SourceUnit sourceUnit
        private final String className
        private final String methodName

        ScriptTransformer(final SourceUnit sourceUnit, final String className, final String methodName) {
            this.sourceUnit = sourceUnit
            this.className = className
            this.methodName = methodName
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return this.sourceUnit
        }

        @Override
        Expression transform(Expression expr) {
            if (expr instanceof MethodCallExpression) {
                final MethodCallExpression methodCallExpr = expr as MethodCallExpression

                if (methodCallExpr.receiver.text == this.className && methodCallExpr.methodAsString == this.methodName) {
                    final ArgumentListExpression arguments = methodCallExpr.arguments as ArgumentListExpression
                    if (arguments.expressions.size() === 1) {
                        final ClosureExpression closureExpr = arguments.expressions[0] as ClosureExpression
                        final ArgumentListExpression newArgs = new ArgumentListExpression()
                        newArgs.addExpression(closureExpr)
                        newArgs.addExpression(new MethodCallExpression(VariableExpression.THIS_EXPRESSION, 'getBinding', ArgumentListExpression.EMPTY_ARGUMENTS))
                        methodCallExpr.arguments = newArgs
                        return methodCallExpr
                    }
                }
            }
            return super.transform(expr)
        }
    }
}
