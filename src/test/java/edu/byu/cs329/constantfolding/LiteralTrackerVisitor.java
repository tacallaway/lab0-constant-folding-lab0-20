package edu.byu.cs329.constantfolding;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.NumberLiteral;

public class LiteralTrackerVisitor extends ASTVisitor {
    List<String> literalList = null;

    public LiteralTrackerVisitor() {
        literalList = new ArrayList<String>();
    }

    @Override
    public boolean visit(NumberLiteral node) {
        literalList.add(node.getToken());
        return true;
    }
}