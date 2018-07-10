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

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.FrameSlotKind;

import edu.uci.megaguards.MGNodeOptions;
import edu.uci.megaguards.analysis.exception.MGException;
import edu.uci.megaguards.analysis.parallel.exception.PrivatizationException;
import edu.uci.megaguards.analysis.precheck.MGPreCheck;
import edu.uci.megaguards.ast.node.MGNode;
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
import edu.uci.python.nodes.expression.UnaryArithmeticNode.MinusNode;
import edu.uci.python.nodes.frame.FrameSlotNode;
import edu.uci.python.nodes.frame.ReadGlobalNode;
import edu.uci.python.nodes.frame.ReadLevelVariableNode;
import edu.uci.python.nodes.frame.ReadLocalVariableNode;
import edu.uci.python.nodes.frame.ReadVariableNode;
import edu.uci.python.nodes.frame.WriteLocalVariableNode;
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

@SuppressWarnings("rawtypes")
public class MGPythonPreCheck extends MGPreCheck<PNode> implements VisitorIF {

    @TruffleBoundary
    public MGPythonPreCheck(boolean allowReturn) {
        super(allowReturn);
    }

    @Override
    public MGPreCheck<PNode> create(boolean allowBodyReturn) {
        return new MGPythonPreCheck(allowBodyReturn);
    }

    @TruffleBoundary
    @Override
    public void check(PNode node) {
        forNodes.push(node);
        visit(node);
    }

    @TruffleBoundary
    public Object visitSubscriptStoreIndexNode(SubscriptStoreIndexNode node) throws MGException {
        visit(node.getPrimary());
        visit(node.getSlice());
        visit(node.getRight());
        return null;
    }

    @TruffleBoundary
    public Object visitSubscriptLoadIndexNode(SubscriptLoadIndexNode node) throws MGException {
        visit(node.getPrimary());
        visit(node.getSlice());
        return null;
    }

    public Object visitIndexNode(IndexNode node) throws MGException {
        return visit(node.getOperand());
    }

    public Object visitBinaryArithmeticNode(BinaryArithmeticNode node) throws MGException {
        visit(node.getLeftNode());
        visit(node.getRightNode());
        return null;
    }

    public Object visitUnaryArithmeticNode(UnaryArithmeticNode node) throws MGException {
        if (node instanceof MinusNode)
            return visitMinusNode((MinusNode) node);

        return NotSupported(node);
    }

    @TruffleBoundary
    public Object visitMinusNode(MinusNode node) throws MGException {
        visit(node.getOperand());
        return null;
    }

    @TruffleBoundary
    public Object visitOrNode(OrNode node) throws MGException {
        visit(node.getLeftNode());
        visit(node.getRightNode());
        return null;
    }

    @TruffleBoundary
    public Object visitCastToBooleanNode(CastToBooleanNode node) throws MGException {
        visit(node.getOperand());
        return null;
    }

    @TruffleBoundary
    public Object visitAndNode(AndNode node) throws MGException {
        visit(node.getLeftNode());
        visit(node.getRightNode());
        return null;
    }

    @TruffleBoundary
    public Object visitBinaryBitwiseNode(BinaryBitwiseNode node) throws MGException {
        visit(node.getLeftNode());
        visit(node.getRightNode());
        return null;
    }

    @TruffleBoundary
    public Object visitBinaryComparisonNode(BinaryComparisonNode node) throws MGException {
        visit(node.getLeftNode());
        visit(node.getRightNode());
        return null;
    }

    @TruffleBoundary
    public Object visitIntegerLiteralNode(IntegerLiteralNode node) {
        return null;
    }

    @TruffleBoundary
    public Object visitDoubleLiteralNode(DoubleLiteralNode node) {
        return null;
    }

    @TruffleBoundary
    public Object visitBooleanLiteralNode(BooleanLiteralNode node) {
        return null;
    }

    @TruffleBoundary
    public Object visitListLiteralNode(ListLiteralNode node) {
        return null;
    }

    @TruffleBoundary
    public Object visitComplexLiteralNode(ComplexLiteralNode node) {
        return null;
    }

    @TruffleBoundary
    public Object visitListAppendNode(ListAppendNode node) throws MGException {
        visit(node.getLeftNode());
        visit(node.getRightNode());
        return null;
    }

    @TruffleBoundary
    public Object visitSetAttributeNode(SetAttributeNode node) throws MGException {
        String name = node.getAttributeId();
        varTable.add(name);
        visit(node.getRhs());
        return null;
    }

    @TruffleBoundary
    public Object visitWriteLocalVariableNode(WriteLocalVariableNode node) throws MGException {
        String name = (String) node.getSlot().getIdentifier();
        if (!processInductionVariable && node.getSlot().getKind() != FrameSlotKind.Illegal)
            throw PrivatizationException.INSTANCE.message("Variable '" + name + "' has been used before entering the loop. We only allow reading variables from outside the loop");
        varTable.add(name);
        visit(node.getRhs());
        return null;
    }

    @TruffleBoundary
    public Object visitReadLocalVariableNode(ReadLocalVariableNode node) throws MGException {
// String varname = (String) node.getSlot().getIdentifier();
// varTable.add(varname);
        return null;
    }

    @TruffleBoundary
    public Object visitReadGlobalNode(ReadGlobalNode node) {
        return null;
    }

    @TruffleBoundary
    public Object visitBlockNode(BlockNode node) throws MGException {
        for (PNode n : node.getStatements()) {
            visit(n);
        }
        return null;
    }

    private MGNode processForNode(PNode target, PNode forbody, PNode iterator) throws MGException {
        visit(((GetIteratorNode) iterator).getOperand());
        processInductionVariable = true;
        visit(target);
        processInductionVariable = false;
        visit(forbody);
        final PNode node = forNodes.peek();
        final int hashCode;
        if (node instanceof MGForNode) {
            hashCode = ((MGForNode) node).getBody().hashCode();
        } else {
            hashCode = node.hashCode();
        }
        if (MGNodeOptions.hasOptions(hashCode)) {
            MGNodeOptions opt = MGNodeOptions.getOptions(hashCode);
            if (opt.isMGOff()) {
                for (PNode n : forNodes) {
                    // TODO: merge options if exists
                    if (n instanceof MGForNode) {
                        MGNodeOptions.addOptions(((MGForNode) n).getBody().hashCode(), opt);
                    } else {
                        MGNodeOptions.addOptions(n.hashCode(), opt);
                    }
                }
            }
        }
        forNodes.pop();
        return null;

    }

    public Object visitMGForNode(MGForNode node) throws Exception {
        forNodes.push(node);
        return processForNode(node.getTarget(), node.getBody(), node.getIterator());
    }

    public Object visitForNode(ForNode node) throws MGException {
        forNodes.push(node);
        return processForNode(node.getTarget(), node.getBody(), node.getIterator());

    }

    public Object visitIfNode(IfNode node) throws MGException {
        visit(node.getCondition());
        visit(node.getThen());
        if (!(node.getElse() instanceof EmptyNode)) {
            visit(node.getElse());
        }
        return null;
    }

    public Object visitElseNode(ElseNode node) throws MGException {
        visit(node.getThen());
        visit(node.getOrelse());
        return null;
    }

    public Object visitBreakTargetNode(BreakTargetNode node) throws MGException {
        return visit(node.getStatement());
    }

    public Object visitBreakNode(BreakNode node) throws MGException {
        return null;
    }

    public Object visitPythonCallNode(PythonCallNode node) throws MGException {
        for (PNode arg : node.getArgumentsNode().getArguments())
            visit(arg);
        return null;
    }

    public Object visitEmptyNode(EmptyNode node) throws Exception {
        return null;
    }

    public Object visitStringLiteralNode(StringLiteralNode node) throws Exception {
        if (node.getParent() instanceof BlockNode) {
            final PNode n = forNodes.peek();
            if (n instanceof MGForNode) {
                MGNodeOptions.processOptions(node.getValue(), ((MGForNode) n).getBody().hashCode());
            } else {
                MGNodeOptions.processOptions(node.getValue(), n.hashCode());
            }
            return null;
        }
        return NotSupported(node);
    }

    public Object visitFrameReturnNode(FrameReturnNode node) throws Exception {
        if (allowReturn) {
            visit(node.getRight());
            return null;
        }
        return NotSupported(node);
    }

    public Object visitReturnTargetNode(ReturnTargetNode node) throws Exception {
        if (allowReturn) {
            visit(node.getBody());
            return null;
        }
        return NotSupported(node);
    }

    public Object visitReadIndexedArgumentNode(ReadIndexedArgumentNode node) throws Exception {
        return null;
    }

    @SuppressWarnings("unchecked")
    @TruffleBoundary
    public Object visit(PNode node) throws MGException {
        try {
            node.accept(this);
        } catch (MGException e) {
            throw e;
        } catch (Exception e) {
            NotSupported(node);
        }
        return null;
    }

    // Not supported yet

    public Object visitContinueTargetNode(ContinueTargetNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitWhileNode(WhileNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitGetIteratorNode(GetIteratorNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitStopIterationTargetNode(StopIterationTargetNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitContinueNode(ContinueNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitPythonBuiltinNode(PythonBuiltinNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitReadGeneratorFrameVariableNode(ReadGeneratorFrameVariableNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitFrameSwappingNode(FrameSwappingNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitComprehensionNode(ComprehensionNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitGeneratorReturnTargetNode(GeneratorReturnTargetNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitGeneratorWhileNode(GeneratorWhileNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitWriteGeneratorFrameVariableNode(WriteGeneratorFrameVariableNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitGeneratorForNode(GeneratorForNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitFrameTransferNode(FrameTransferNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitYieldNode(YieldNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitDictLiteralNode(DictLiteralNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitTupleLiteralNode(TupleLiteralNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitSetLiteralNode(SetLiteralNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitObjectLiteralNode(ObjectLiteralNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitBigIntegerLiteralNode(BigIntegerLiteralNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitKeywordLiteralNode(KeywordLiteralNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitTryFinallyNode(TryFinallyNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitRaiseNode(RaiseNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitWithNode(WithNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitPrintNode(PrintNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitAssertNode(AssertNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitTryExceptNode(TryExceptNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitExceptNode(ExceptNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitClassDefinitionNode(ClassDefinitionNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitImportStarNode(ImportStarNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitDefaultParametersNode(DefaultParametersNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitImportFromNode(ImportFromNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitImportNode(ImportNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitReadLevelVariableNode(ReadLevelVariableNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitReadVariableNode(ReadVariableNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitFrameSlotNode(FrameSlotNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitReadVarKeywordsNode(ReadVarKeywordsNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitArgumentsNode(ArgumentsNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitReadKeywordNode(ReadKeywordNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitReadVarArgsNode(ReadVarArgsNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitReadDefaultArgumentNode(ReadDefaultArgumentNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitSubscriptStoreSliceNode(SubscriptStoreSliceNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitSliceNode(SliceNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitSubscriptLoadSliceNode(SubscriptLoadSliceNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitGetAttributeNode(GetAttributeNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitPeeledGeneratorLoopNode(PeeledGeneratorLoopNode node) throws Exception {
        return NotSupported(node);
    }

    public Object visitNoneNode(NoneNode node) throws Exception {
        return NotSupported(node);
    }

}
