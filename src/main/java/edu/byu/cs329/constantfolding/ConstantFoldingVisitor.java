package edu.byu.cs329.constantfolding;

import java.util.Objects;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstantFoldingVisitor extends ASTVisitor {

    static final Logger log = LoggerFactory.getLogger(ConstantFoldingVisitor.class);

    class Operand {
        Expression expr = null;
        Integer value = null;

        public Operand(final Expression expr) {
            this.expr = expr;
        }

        public Operand(final Expression expr, final Integer value) {
            this.expr = expr;
            this.value = value;
        }
    }

    Operand operand = null;

    private static boolean isIntLiteralExpression(final Operand left, final Operand right) {
        if (left != null && left.value != null && right != null && right.value != null) {
            return true;
        }
        return false;
    }

    @Override
    public boolean visit(NumberLiteral node) {
        String token = node.getToken();
        NumberLiteral numLit = node.getAST().newNumberLiteral(token);
        Integer value = null;

        try {
            value = Integer.decode(token);
        } catch (NumberFormatException e) {
            log.warn("\'" + node.getToken() + "\'" + " is not an integer literal -- skipping");
        }

        operand = new Operand(numLit, value);
        return true;
    }

    @Override
    public boolean visit(InfixExpression node) {
        if (node.getOperator() != InfixExpression.Operator.PLUS) {
            throw new UnsupportedOperationException(
                    "Operator \'" + node.getOperator().toString() + "\' is not supported");
        }

        if (node.hasExtendedOperands()) {
            throw new UnsupportedOperationException(
                    "Extended operands are not supported");
        }

        node.getLeftOperand().accept(this);
        Operand left = operand;

        node.getRightOperand().accept(this);
        Operand right = operand;

        AST ast = node.getAST();
        Expression newExpr = null;
        if (!isIntLiteralExpression(left, right)) {
            newExpr = (Expression) (ASTNode.copySubtree(ast, node));
            operand = new Operand(newExpr);
        } else {
            Integer newValue = left.value + right.value;
            newExpr = ast.newNumberLiteral(newValue.toString());
            operand = new Operand(newExpr, newValue);
        }

        return false;
    }

    @Override
    public void endVisit(InfixExpression node) {
        Objects.requireNonNull(operand);
        Objects.requireNonNull(operand.expr);
        StructuralPropertyDescriptor location = node.getLocationInParent();
        Objects.requireNonNull(location);
        if (location.isChildProperty()) {
            node.getParent().setStructuralProperty(location, operand.expr);
        } else {
            throw new UnsupportedOperationException(
                    "Location \'" + location.toString() + "\' is not supported");
        }
    }
}