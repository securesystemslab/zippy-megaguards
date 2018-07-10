/*
 * Copyright (c) 2018, Regents of the University of California
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package edu.uci.megaguards.python.analysis;

import java.util.HashSet;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.nodes.Node;

import edu.uci.megaguards.analysis.exception.MGException;
import edu.uci.megaguards.analysis.precheck.MGPrivatizationCheck;
import edu.uci.megaguards.python.node.MGForNode;
import edu.uci.python.ast.VisitorIF;
import edu.uci.python.nodes.EmptyNode;
import edu.uci.python.nodes.NoneNode;
import edu.uci.python.nodes.PNode;
import edu.uci.python.nodes.argument.ArgumentsNode;
import edu.uci.python.nodes.argument.ReadDefaultArgumentNode;
import edu.uci.python.nodes.argument.ReadIndexedArgumentNode;
import edu.uci.python.nodes.argument.ReadKeywordNode;
import edu.uci.python.nodes.argument.ReadVarArgsNode;
import edu.uci.python.nodes.argument.ReadVarKeywordsNode;
import edu.uci.python.nodes.call.PythonCallNode;
import edu.uci.python.nodes.control.BlockNode;
import edu.uci.python.nodes.control.BreakNode;
import edu.uci.python.nodes.control.BreakTargetNode;
import edu.uci.python.nodes.control.ContinueNode;
import edu.uci.python.nodes.control.ContinueTargetNode;
import edu.uci.python.nodes.control.ElseNode;
import edu.uci.python.nodes.control.ForNode;
import edu.uci.python.nodes.control.GetIteratorNode;
import edu.uci.python.nodes.control.IfNode;
import edu.uci.python.nodes.control.ReturnNode.FrameReturnNode;
import edu.uci.python.nodes.control.ReturnTargetNode;
import edu.uci.python.nodes.control.StopIterationTargetNode;
import edu.uci.python.nodes.control.WhileNode;
import edu.uci.python.nodes.expression.AndNode;
import edu.uci.python.nodes.expression.BinaryArithmeticNode;
import edu.uci.python.nodes.expression.BinaryBitwiseNode;
import edu.uci.python.nodes.expression.BinaryComparisonNode;
import edu.uci.python.nodes.expression.CastToBooleanNode;
import edu.uci.python.nodes.expression.OrNode;
import edu.uci.python.nodes.expression.UnaryArithmeticNode;
import edu.uci.python.nodes.frame.FrameSlotNode;
import edu.uci.python.nodes.frame.ReadGlobalNode;
import edu.uci.python.nodes.frame.ReadLevelVariableNode;
import edu.uci.python.nodes.frame.ReadLocalVariableNode;
import edu.uci.python.nodes.frame.ReadVariableNode;
import edu.uci.python.nodes.frame.WriteLocalVariableNode;
import edu.uci.python.nodes.function.FunctionRootNode;
import edu.uci.python.nodes.function.PythonBuiltinNode;
import edu.uci.python.nodes.generator.ComprehensionNode;
import edu.uci.python.nodes.generator.FrameSwappingNode;
import edu.uci.python.nodes.generator.FrameTransferNode;
import edu.uci.python.nodes.generator.GeneratorForNode;
import edu.uci.python.nodes.generator.GeneratorReturnTargetNode;
import edu.uci.python.nodes.generator.GeneratorWhileNode;
import edu.uci.python.nodes.generator.ListAppendNode;
import edu.uci.python.nodes.generator.ReadGeneratorFrameVariableNode;
import edu.uci.python.nodes.generator.WriteGeneratorFrameVariableNode;
import edu.uci.python.nodes.generator.YieldNode;
import edu.uci.python.nodes.literal.BigIntegerLiteralNode;
import edu.uci.python.nodes.literal.BooleanLiteralNode;
import edu.uci.python.nodes.literal.ComplexLiteralNode;
import edu.uci.python.nodes.literal.DictLiteralNode;
import edu.uci.python.nodes.literal.DoubleLiteralNode;
import edu.uci.python.nodes.literal.IntegerLiteralNode;
import edu.uci.python.nodes.literal.KeywordLiteralNode;
import edu.uci.python.nodes.literal.ListLiteralNode;
import edu.uci.python.nodes.literal.ObjectLiteralNode;
import edu.uci.python.nodes.literal.SetLiteralNode;
import edu.uci.python.nodes.literal.StringLiteralNode;
import edu.uci.python.nodes.literal.TupleLiteralNode;
import edu.uci.python.nodes.object.GetAttributeNode;
import edu.uci.python.nodes.object.SetAttributeNode;
import edu.uci.python.nodes.optimize.PeeledGeneratorLoopNode;
import edu.uci.python.nodes.statement.AssertNode;
import edu.uci.python.nodes.statement.ClassDefinitionNode;
import edu.uci.python.nodes.statement.DefaultParametersNode;
import edu.uci.python.nodes.statement.ExceptNode;
import edu.uci.python.nodes.statement.ImportFromNode;
import edu.uci.python.nodes.statement.ImportNode;
import edu.uci.python.nodes.statement.ImportStarNode;
import edu.uci.python.nodes.statement.PrintNode;
import edu.uci.python.nodes.statement.RaiseNode;
import edu.uci.python.nodes.statement.TryExceptNode;
import edu.uci.python.nodes.statement.TryFinallyNode;
import edu.uci.python.nodes.statement.WithNode;
import edu.uci.python.nodes.subscript.IndexNode;
import edu.uci.python.nodes.subscript.SliceNode;
import edu.uci.python.nodes.subscript.SubscriptLoadIndexNode;
import edu.uci.python.nodes.subscript.SubscriptLoadSliceNode;
import edu.uci.python.nodes.subscript.SubscriptStoreIndexNode;
import edu.uci.python.nodes.subscript.SubscriptStoreSliceNode;

public class MGPythonPrivatizationCheck extends MGPrivatizationCheck<PNode> implements VisitorIF<Object> {

    @TruffleBoundary
    public MGPythonPrivatizationCheck(HashSet<String> localVarTable, PNode forNode) {
        super(localVarTable, forNode);
    }

    private Node getFunctionBody(Node node) {
        if (node instanceof FunctionRootNode)
            return node;

        return getFunctionBody(node.getParent());

    }

    @Override
    public MGPrivatizationCheck<PNode> create(HashSet<String> localVarTable, PNode forNode) {
        return new MGPythonPrivatizationCheck(localVarTable, forNode);
    }

    @TruffleBoundary
    @Override
    public void check() {
        FunctionRootNode node = (FunctionRootNode) getFunctionBody(cForNode);
        visit(node.getBody());
    }

    public Object visit(PNode node) throws MGException {
        if (varTable.isEmpty())
            return null;

        if (node == null)
            return null;

        Object ret = null;
        try {
            ret = node.accept(this);
        } catch (MGException e) {
            throw e;
        } catch (Exception e) {
            NotCovered(node);
        }
        return ret;
    }

    public Object visitBlockNode(BlockNode node) throws Exception {
        for (PNode n : node.getStatements()) {
            visit(n);
        }
        return null;
    }

    public Object visitContinueTargetNode(ContinueTargetNode node) throws Exception {
        visit(node.getTargetNode());
        return null;
    }

    public Object visitWhileNode(WhileNode node) throws Exception {
        visit(node.getCondition());
        visit(node.getBody());
        return null;
    }

    public Object visitGetIteratorNode(GetIteratorNode node) throws Exception {
        visit(node.getOperand());
        return null;
    }

    public Object visitReturnTargetNode(ReturnTargetNode node) throws Exception {
        visit(node.getBody());
        visit(node.getReturn());
        return null;
    }

    public Object visitFrameReturnNode(FrameReturnNode node) throws Exception {
        visit(node.getRight());
        return null;
    }

    public Object visitStopIterationTargetNode(StopIterationTargetNode node) throws Exception {
        visit(node.getTryPart());
        visit(node.getCatchPart());
        return null;
    }

    public Object visitBreakNode(BreakNode node) throws Exception {
        return null;
    }

    public Object visitBreakTargetNode(BreakTargetNode node) throws Exception {
        visit(node.getStatement());
        return null;
    }

    public Object visitForNode(ForNode node) throws Exception {
        if (cForNode.hashCode() == node.hashCode())
            return null;

        visit(node.getIterator());
        visit(node.getTarget());
        visit(node.getBody());
        return null;
    }

    public Object visitMGForNode(MGForNode node) throws Exception {
        if (cForNode.hashCode() == node.hashCode())
            return null;

        visit(node.getIterator());
        visit(node.getTarget());
        visit(node.getBody());
        return null;
    }

    public Object visitElseNode(ElseNode node) throws Exception {
        visit(node.getThen());
        visit(node.getOrelse());
        return null;
    }

    public Object visitContinueNode(ContinueNode node) throws Exception {
        return null;
    }

    public Object visitIfNode(IfNode node) throws Exception {
        visit(node.getCondition());
        visit(node.getThen());
        visit(node.getElse());
        return null;
    }

    public Object visitPythonBuiltinNode(PythonBuiltinNode node) throws Exception {
        return null;
    }

    public Object visitBinaryBitwiseNode(BinaryBitwiseNode node) throws Exception {
        visit(node.getLeftNode());
        visit(node.getRightNode());
        return null;
    }

    public Object visitCastToBooleanNode(CastToBooleanNode node) throws Exception {
        visit(node.getOperand());
        return null;
    }

    public Object visitOrNode(OrNode node) throws Exception {
        visit(node.getLeftNode());
        visit(node.getRightNode());
        return null;
    }

    public Object visitBinaryComparisonNode(BinaryComparisonNode node) throws Exception {
        visit(node.getLeftNode());
        visit(node.getRightNode());
        return null;
    }

    public Object visitUnaryArithmeticNode(UnaryArithmeticNode node) throws Exception {
        visit(node.getOperand());
        return null;
    }

    public Object visitBinaryArithmeticNode(BinaryArithmeticNode node) throws Exception {
        visit(node.getLeftNode());
        visit(node.getRightNode());
        return null;
    }

    public Object visitAndNode(AndNode node) throws Exception {
        visit(node.getLeftNode());
        visit(node.getRightNode());
        return null;
    }

    public Object visitReadGeneratorFrameVariableNode(ReadGeneratorFrameVariableNode node) throws Exception {
        // We shouldn't worry about this.. verification might be needed
        return null;
    }

    public Object visitFrameSwappingNode(FrameSwappingNode node) throws Exception {
        visit(node.getChild());
        return null;
    }

    public Object visitComprehensionNode(ComprehensionNode node) throws Exception {
        visit(node.getComprehension());
        visit(node.getWrite());
        return null;
    }

    public Object visitGeneratorReturnTargetNode(GeneratorReturnTargetNode node) throws Exception {
        visit(node.getParameters());
        visit(node.getBody());
        visit(node.getReturn());
        return null;
    }

    public Object visitGeneratorWhileNode(GeneratorWhileNode node) throws Exception {
        visit(node.getCondition());
        visit(node.getBody());
        return null;
    }

    public Object visitListAppendNode(ListAppendNode node) throws Exception {
        return null;
    }

    public Object visitWriteGeneratorFrameVariableNode(WriteGeneratorFrameVariableNode node) throws Exception {
        // We shouldn't worry about this.. verification might be needed
        return null;
    }

    public Object visitGeneratorForNode(GeneratorForNode node) throws Exception {
        visit(node.getGetIterator());
        visit(node.getBody());
        visit(node.getTarget());
        return null;
    }

    public Object visitFrameTransferNode(FrameTransferNode node) throws Exception {
        // We shouldn't worry about this.. verification might be needed
        return null;
    }

    public Object visitYieldNode(YieldNode node) throws Exception {
        visit(node.getRhs());
        return null;
    }

    public Object visitIntegerLiteralNode(IntegerLiteralNode node) throws Exception {
        return null;
    }

    public Object visitListLiteralNode(ListLiteralNode node) throws Exception {
        return null;
    }

    public Object visitComplexLiteralNode(ComplexLiteralNode node) throws Exception {
        return null;
    }

    public Object visitDictLiteralNode(DictLiteralNode node) throws Exception {
        return null;
    }

    public Object visitTupleLiteralNode(TupleLiteralNode node) throws Exception {
        return null;
    }

    public Object visitDoubleLiteralNode(DoubleLiteralNode node) throws Exception {
        return null;
    }

    public Object visitSetLiteralNode(SetLiteralNode node) throws Exception {
        return null;
    }

    public Object visitObjectLiteralNode(ObjectLiteralNode node) throws Exception {
        return null;
    }

    public Object visitStringLiteralNode(StringLiteralNode node) throws Exception {
        return null;
    }

    public Object visitBigIntegerLiteralNode(BigIntegerLiteralNode node) throws Exception {
        return null;
    }

    public Object visitBooleanLiteralNode(BooleanLiteralNode node) throws Exception {
        return null;
    }

    public Object visitKeywordLiteralNode(KeywordLiteralNode node) throws Exception {
        visit(node.getValue());
        return null;
    }

    public Object visitTryFinallyNode(TryFinallyNode node) throws Exception {
        visit(node.getBody());
        visit(node.getFinalbody());
        return null;
    }

    public Object visitRaiseNode(RaiseNode node) throws Exception {
        visit(node.getType());
        visit(node.getInst());
        return null;
    }

    public Object visitWithNode(WithNode node) throws Exception {
        visit(node.getWithContext());
        visit(node.getBody());
        for (PNode n : node.getTargetNodes())
            visit(n);
        return null;
    }

    public Object visitPrintNode(PrintNode node) throws Exception {
        for (PNode n : node.getValues())
            visit(n);
        return null;
    }

    public Object visitAssertNode(AssertNode node) throws Exception {
        visit(node.getCondition());
        visit(node.getMessage());
        return null;
    }

    public Object visitTryExceptNode(TryExceptNode node) throws Exception {
        visit(node.getBody());
        visit(node.getOrelse());
        for (PNode n : node.getExceptNodes())
            visit(n);
        return null;
    }

    public Object visitExceptNode(ExceptNode node) throws Exception {
        for (PNode n : node.getExceptType())
            visit(n);
        visit(node.getExceptName());
        visit(node.getBody());
        return null;
    }

    public Object visitClassDefinitionNode(ClassDefinitionNode node) throws Exception {
        return null;
    }

    public Object visitImportStarNode(ImportStarNode node) throws Exception {
        return null;
    }

    public Object visitDefaultParametersNode(DefaultParametersNode node) throws Exception {
        for (PNode n : node.getFunctionDefaults())
            visit(n);
        for (PNode n : node.getDefaultReads())
            visit(n);
        return null;
    }

    public Object visitImportFromNode(ImportFromNode node) throws Exception {
        return null;
    }

    public Object visitImportNode(ImportNode node) throws Exception {
        return null;
    }

    public Object visitReadLocalVariableNode(ReadLocalVariableNode node) throws Exception {
        String varname = (String) node.getSlot().getIdentifier();
        if (varTable.contains(varname))
            violation(varname);
        return null;
    }

    public Object visitWriteLocalVariableNode(WriteLocalVariableNode node) throws Exception {
        visit(node.getRhs());
        String varname = (String) node.getSlot().getIdentifier();
        if (varTable.contains(varname))
            varTable.remove(varname);
        return null;
    }

    public Object visitReadLevelVariableNode(ReadLevelVariableNode node) throws Exception {
        // We shouldn't worry about this.. verification might be needed
        return null;
    }

    public Object visitReadVariableNode(ReadVariableNode node) throws Exception {
        String varname = (String) node.getSlot().getIdentifier();
        if (varTable.contains(varname))
            violation(varname);
        return null;
    }

    public Object visitReadGlobalNode(ReadGlobalNode node) throws Exception {
        // We shouldn't worry about this..
        return null;
    }

    public Object visitFrameSlotNode(FrameSlotNode node) throws Exception {
        // We shouldn't worry about this.. verification might be needed
        return null;
    }

    public Object visitPythonCallNode(PythonCallNode node) throws Exception {
        for (PNode n : node.getArgumentsNode().getArguments())
            visit(n);
        return null;
    }

    public Object visitReadVarKeywordsNode(ReadVarKeywordsNode node) throws Exception {
        // We shouldn't worry about this.. verification might be needed
        return null;
    }

    public Object visitArgumentsNode(ArgumentsNode node) throws Exception {
        for (PNode n : node.getArguments())
            visit(n);
        return null;
    }

    public Object visitReadKeywordNode(ReadKeywordNode node) throws Exception {
        // We shouldn't worry about this.. verification might be needed
        return null;
    }

    public Object visitReadVarArgsNode(ReadVarArgsNode node) throws Exception {
        // We shouldn't worry about this.. verification might be needed
        return null;
    }

    public Object visitReadIndexedArgumentNode(ReadIndexedArgumentNode node) throws Exception {
        // We shouldn't worry about this.. verification might be needed
        return null;
    }

    public Object visitReadDefaultArgumentNode(ReadDefaultArgumentNode node) throws Exception {
        return null;
    }

    public Object visitIndexNode(IndexNode node) throws Exception {
        visit(node.getOperand());
        return null;
    }

    public Object visitSubscriptLoadIndexNode(SubscriptLoadIndexNode node) throws Exception {
        visit(node.getSlice());
        return null;
    }

    public Object visitSubscriptStoreIndexNode(SubscriptStoreIndexNode node) throws Exception {
        visit(node.getSlice());
        return null;
    }

    public Object visitSubscriptStoreSliceNode(SubscriptStoreSliceNode node) throws Exception {
        visit(node.getSlice());
        return null;
    }

    public Object visitSliceNode(SliceNode node) throws Exception {
        return null;
    }

    public Object visitSubscriptLoadSliceNode(SubscriptLoadSliceNode node) throws Exception {
        return null;
    }

    public Object visitSetAttributeNode(SetAttributeNode node) throws Exception {
        String varname = node.getAttributeId();
        if (varTable.contains(varname))
            varTable.remove(varname);
        return null;
    }

    public Object visitGetAttributeNode(GetAttributeNode node) throws Exception {
        String varname = node.getAttributeId();
        if (varTable.contains(varname))
            violation(varname);
        return null;
    }

    public Object visitPeeledGeneratorLoopNode(PeeledGeneratorLoopNode node) throws Exception {
        return null;
    }

    public Object visitEmptyNode(EmptyNode node) throws Exception {
        return null;
    }

    public Object visitNoneNode(NoneNode node) throws Exception {
        return null;
    }

}
