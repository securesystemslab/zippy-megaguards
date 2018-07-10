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
package edu.uci.megaguards.python.ast;

import java.util.ArrayList;
import java.util.List;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.frame.FrameSlotKind;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.source.SourceSection;

import edu.uci.megaguards.MGNodeOptions;
import edu.uci.megaguards.MGOptions;
import edu.uci.megaguards.analysis.bounds.node.MGBoundNodeLiteral;
import edu.uci.megaguards.analysis.exception.CoverageException;
import edu.uci.megaguards.analysis.exception.MGException;
import edu.uci.megaguards.analysis.exception.TypeException;
import edu.uci.megaguards.ast.MGTree;
import edu.uci.megaguards.ast.env.MGBaseEnv;
import edu.uci.megaguards.ast.env.MGGlobalEnv;
import edu.uci.megaguards.ast.env.MGPrivateEnv;
import edu.uci.megaguards.ast.node.LoopInfo;
import edu.uci.megaguards.ast.node.MGNode;
import edu.uci.megaguards.ast.node.MGNodeAssign;
import edu.uci.megaguards.ast.node.MGNodeAssignComplex;
import edu.uci.megaguards.ast.node.MGNodeBinOp;
import edu.uci.megaguards.ast.node.MGNodeBinOp.BinOpType;
import edu.uci.megaguards.ast.node.MGNodeBlock;
import edu.uci.megaguards.ast.node.MGNodeBreakElse;
import edu.uci.megaguards.ast.node.MGNodeBuiltinFunction;
import edu.uci.megaguards.ast.node.MGNodeBuiltinFunction.BuiltinFunctionType;
import edu.uci.megaguards.ast.node.MGNodeControl;
import edu.uci.megaguards.ast.node.MGNodeEmpty;
import edu.uci.megaguards.ast.node.MGNodeFor;
import edu.uci.megaguards.ast.node.MGNodeFunctionCall;
import edu.uci.megaguards.ast.node.MGNodeIf;
import edu.uci.megaguards.ast.node.MGNodeJumpFrom;
import edu.uci.megaguards.ast.node.MGNodeJumpTo;
import edu.uci.megaguards.ast.node.MGNodeMathFunction;
import edu.uci.megaguards.ast.node.MGNodeMathFunction.MathFunctionType;
import edu.uci.megaguards.ast.node.MGNodeOperand;
import edu.uci.megaguards.ast.node.MGNodeOperandComplex;
import edu.uci.megaguards.ast.node.MGNodeReturn;
import edu.uci.megaguards.ast.node.MGNodeUnaryOp;
import edu.uci.megaguards.ast.node.MGNodeUnaryOp.UnaryOpType;
import edu.uci.megaguards.ast.node.MGNodeUserFunction;
import edu.uci.megaguards.ast.node.MGNodeWhile;
import edu.uci.megaguards.backend.parallel.opencl.OpenCLTranslator;
import edu.uci.megaguards.log.MGLog;
import edu.uci.megaguards.object.DataType;
import edu.uci.megaguards.object.MGArray;
import edu.uci.megaguards.object.MGBoolLiteral;
import edu.uci.megaguards.object.MGComplex;
import edu.uci.megaguards.object.MGDouble;
import edu.uci.megaguards.object.MGDoubleArray;
import edu.uci.megaguards.object.MGDoubleLiteral;
import edu.uci.megaguards.object.MGInt;
import edu.uci.megaguards.object.MGIntArray;
import edu.uci.megaguards.object.MGIntLiteral;
import edu.uci.megaguards.object.MGLong;
import edu.uci.megaguards.object.MGLongArray;
import edu.uci.megaguards.object.MGLongLiteral;
import edu.uci.megaguards.object.MGObject;
import edu.uci.megaguards.object.MGStorage;
import edu.uci.megaguards.python.node.MGForNode;
import edu.uci.megaguards.python.unbox.PythonBoxed;
import edu.uci.megaguards.python.unbox.PythonUnboxerUtil;
import edu.uci.megaguards.unbox.Boxed;
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
import edu.uci.python.nodes.expression.BinaryArithmeticNode.AddNode;
import edu.uci.python.nodes.expression.BinaryArithmeticNode.DivNode;
import edu.uci.python.nodes.expression.BinaryArithmeticNode.FloorDivNode;
import edu.uci.python.nodes.expression.BinaryArithmeticNode.ModuloNode;
import edu.uci.python.nodes.expression.BinaryArithmeticNode.MulNode;
import edu.uci.python.nodes.expression.BinaryArithmeticNode.PowerNode;
import edu.uci.python.nodes.expression.BinaryArithmeticNode.SubNode;
import edu.uci.python.nodes.expression.BinaryBitwiseNode;
import edu.uci.python.nodes.expression.BinaryBitwiseNode.BitAndNode;
import edu.uci.python.nodes.expression.BinaryBitwiseNode.BitOrNode;
import edu.uci.python.nodes.expression.BinaryBitwiseNode.BitXorNode;
import edu.uci.python.nodes.expression.BinaryBitwiseNode.LeftShiftNode;
import edu.uci.python.nodes.expression.BinaryBitwiseNode.RightShiftNode;
import edu.uci.python.nodes.expression.BinaryComparisonNode;
import edu.uci.python.nodes.expression.BinaryComparisonNode.EqualNode;
import edu.uci.python.nodes.expression.BinaryComparisonNode.GreaterThanEqualNode;
import edu.uci.python.nodes.expression.BinaryComparisonNode.GreaterThanNode;
import edu.uci.python.nodes.expression.BinaryComparisonNode.LessThanEqualNode;
import edu.uci.python.nodes.expression.BinaryComparisonNode.LessThanNode;
import edu.uci.python.nodes.expression.BinaryComparisonNode.NotEqualNode;
import edu.uci.python.nodes.expression.CastToBooleanNode;
import edu.uci.python.nodes.expression.CastToBooleanNode.NotNode;
import edu.uci.python.nodes.expression.OrNode;
import edu.uci.python.nodes.expression.UnaryArithmeticNode;
import edu.uci.python.nodes.expression.UnaryArithmeticNode.MinusNode;
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
import edu.uci.python.parser.TranslationEnvironment;
import edu.uci.python.runtime.builtin.PythonBuiltinClass;
import edu.uci.python.runtime.datatype.PComplex;
import edu.uci.python.runtime.function.Arity;
import edu.uci.python.runtime.function.PBuiltinFunction;
import edu.uci.python.runtime.function.PFunction;
import edu.uci.python.runtime.sequence.PList;
import edu.uci.python.runtime.sequence.PTuple;

public class MGPythonTree extends MGTree<PNode, PFunction> implements VisitorIF<MGNode> {

    protected MGBaseEnv env;

    public static final MGPythonTree PyTREE = new MGPythonTree(null, null, null);

    public MGPythonTree(MGBaseEnv env, VirtualFrame frame, MGLog log) {
        super(frame, log, PythonBoxed.PyBOX, PythonUnboxerUtil.PyUTIL);
        this.env = env;
    }

    @Override
    public MGTree<PNode, PFunction> create(MGBaseEnv e, VirtualFrame f, MGLog l) {
        return new MGPythonTree(e, f, l);
    }

    @Override
    protected MGBaseEnv getEnv() {
        return env;
    }

    @Override
    @TruffleBoundary
    protected MGStorage getInductionVariable(PNode target) throws TypeException {
        final MGStorage inductionVariable;
        assert env.isGlobalEnv();
        if (target instanceof SetAttributeNode)
            inductionVariable = ((MGGlobalEnv) env).setIteratorVar(((SetAttributeNode) target).getAttributeId(), 0);
        else if (target instanceof WriteLocalVariableNode)
            inductionVariable = ((MGGlobalEnv) env).setIteratorVar((String) ((WriteLocalVariableNode) target).getSlot().getIdentifier(), 0);
        else
            throw TypeException.INSTANCE.message("Iterator cannot be processed: " + target.getClass());

        return inductionVariable;
    }

    @Override
    @TruffleBoundary
    public MGNode visitSubscriptStoreIndexNode(SubscriptStoreIndexNode node) throws MGException {
        storeSubscript = true;
        MGNodeOperand primary = (MGNodeOperand) visit(node.getPrimary());
        primary.addIndex(slices(node.getSlice()));
        storeSubscript = false;
        MGNode right = visit(node.getRight());
        ((MGArray) primary.getValue()).setRelatedLoopInfos(currentLoopInfos);
        env.getArrayReadWrite().addArrayAccess(primary, false);
        env.addArrayAccess(primary);
        return new MGNodeAssign(primary, right).setSource(node.getSourceSection());
    }

    @Override
    @TruffleBoundary
    public MGNode visitSubscriptLoadIndexNode(SubscriptLoadIndexNode node) throws MGException {
        MGNodeOperand primary = (MGNodeOperand) visit(node.getPrimary());
        primary.addIndex(slices(node.getSlice()));
        if (!storeSubscript) {
            ((MGArray) primary.getValue()).setRelatedLoopInfos(currentLoopInfos);
            env.getArrayReadWrite().addArrayAccess(primary, true);
            env.addArrayAccess(primary);
        }
        return primary;
    }

    @Override
    public MGNode visitIndexNode(IndexNode node) throws MGException {
        return visit(node.getOperand());
    }

    public MGNode slices(PNode node) throws MGException {
        boolean prevSliceProcessing = sliceprocessing;
        sliceprocessing = true;
        MGNode s = castInt(visit(node));
        sliceprocessing = prevSliceProcessing;
        return s;
    }

    @Override
    public MGNode visitBinaryArithmeticNode(BinaryArithmeticNode node) throws MGException {
        if (node instanceof AddNode) {
            return (visitAddNode((AddNode) node));
        } else if (node instanceof SubNode) {
            return (visitSubNode((SubNode) node));
        } else if (node instanceof MulNode) {
            return (visitMulNode((MulNode) node));
        } else if (node instanceof DivNode) {
            return (visitDivNode((DivNode) node));
        } else if (node instanceof FloorDivNode) {
            return (visitFloorDivNode((FloorDivNode) node));
        } else if (node instanceof ModuloNode) {
            return (visitModuloNode((ModuloNode) node));
        } else if (node instanceof PowerNode) {
            return (visitPowerNode((PowerNode) node));
        } else
            return NotSupported(node);
    }

    @TruffleBoundary
    public MGNode visitAddNode(AddNode node) throws MGException {
        MGNode left = visit(node.getLeftNode());
        MGNode right = visit(node.getRightNode());
        DataType kind = getDominantType(left.getExpectedType(), right.getExpectedType());

        if (kind != DataType.Complex) {
            MGNode retVal = new MGNodeBinOp(left, BinOpType.ADD, right, kind);
            if (isIntOrLong(retVal)) {
                if (sliceprocessing) {
                    retVal.setBound(new MGBoundNodeLiteral(0, DataType.Int).setRequireBoundCheck(false));
                } else {
                    env.addOFCheck(retVal);
                }
            }
            return retVal;
        } else {

            MGNode realLeft = (left.getExpectedType() == kind) ? ((MGNodeOperandComplex) left).getReal() : left;
            MGNode realRight = (right.getExpectedType() == kind) ? ((MGNodeOperandComplex) right).getReal() : right;
            MGNode imagLeft = (left.getExpectedType() == kind) ? ((MGNodeOperandComplex) left).getImag() : MGNodeOperand.ZERO;
            MGNode imagRight = (right.getExpectedType() == kind) ? ((MGNodeOperandComplex) right).getImag() : MGNodeOperand.ZERO;

            MGNode real = new MGNodeBinOp(realLeft, BinOpType.ADD, realRight, DataType.Double);
            MGNode imag = new MGNodeBinOp(imagLeft, BinOpType.ADD, imagRight, DataType.Double);
            return new MGNodeOperandComplex(real, imag);
        }
    }

    @TruffleBoundary
    public MGNode visitSubNode(SubNode node) throws MGException {
        MGNode left = visit(node.getLeftNode());
        MGNode right = visit(node.getRightNode());
        DataType kind = getDominantType(left.getExpectedType(), right.getExpectedType());

        if (kind != DataType.Complex) {
            MGNodeBinOp retVal = new MGNodeBinOp(left, BinOpType.SUB, right, kind);
            if (isIntOrLong(retVal)) {
                if (sliceprocessing) {
                    retVal.setBound(new MGBoundNodeLiteral(0, DataType.Int).setRequireBoundCheck(false));
                } else {
                    env.addOFCheck(retVal);
                }
            }
            return retVal;
        } else {

            MGNode realLeft = (left.getExpectedType() == kind) ? ((MGNodeOperandComplex) left).getReal() : left;
            MGNode realRight = (right.getExpectedType() == kind) ? ((MGNodeOperandComplex) right).getReal() : right;
            MGNode imagLeft = (left.getExpectedType() == kind) ? ((MGNodeOperandComplex) left).getImag() : MGNodeOperand.ZERO;
            MGNode imagRight = (right.getExpectedType() == kind) ? ((MGNodeOperandComplex) right).getImag() : MGNodeOperand.ZERO;

            MGNode real = new MGNodeBinOp(realLeft, BinOpType.SUB, realRight, DataType.Double);
            MGNode imag = new MGNodeBinOp(imagLeft, BinOpType.SUB, imagRight, DataType.Double);
            return new MGNodeOperandComplex(real, imag);
        }
    }

    @TruffleBoundary
    public MGNode visitMulNode(MulNode node) throws MGException {
        MGNode left = visit(node.getLeftNode());
        MGNode right = visit(node.getRightNode());
        DataType kind = getDominantType(left.getExpectedType(), right.getExpectedType());

        if (kind != DataType.Complex) {
            MGNode retVal = null;
            retVal = new MGNodeBinOp(left, BinOpType.MUL, right, kind);
            if (isIntOrLong(retVal)) {
                if (sliceprocessing) {
                    retVal.setBound(new MGBoundNodeLiteral(0, DataType.Int).setRequireBoundCheck(false));
                } else {
                    env.addOFCheck(retVal);
                }
            }
            return retVal;
        } else {

            MGNode realLeft = (left.getExpectedType() == kind) ? ((MGNodeOperandComplex) left).getReal() : left;
            MGNode realRight = (right.getExpectedType() == kind) ? ((MGNodeOperandComplex) right).getReal() : right;
            MGNode imagLeft = (left.getExpectedType() == kind) ? ((MGNodeOperandComplex) left).getImag() : left;
            MGNode imagRight = (right.getExpectedType() == kind) ? ((MGNodeOperandComplex) right).getImag() : right;

            MGNode real = null;
            MGNode imag = null;

            if (left.getExpectedType() != kind || right.getExpectedType() != kind) {
                real = new MGNodeBinOp(realLeft, BinOpType.MUL, realRight, DataType.Double);
                imag = new MGNodeBinOp(imagLeft, BinOpType.MUL, imagRight, DataType.Double);

            } else {
                real = new MGNodeBinOp(new MGNodeBinOp(realLeft, BinOpType.MUL, realRight, DataType.Double), BinOpType.SUB,
                                new MGNodeBinOp(imagLeft, BinOpType.MUL, imagRight, DataType.Double),
                                DataType.Double);
                imag = new MGNodeBinOp(new MGNodeBinOp(realLeft, BinOpType.MUL, imagRight, DataType.Double), BinOpType.ADD,
                                new MGNodeBinOp(imagLeft, BinOpType.MUL, realRight, DataType.Double),
                                DataType.Double);
            }
            return new MGNodeOperandComplex(real, imag);
        }
    }

    @Override
    public MGNode visitUnaryArithmeticNode(UnaryArithmeticNode node) throws Exception {
        if (node instanceof MinusNode)
            return visitMinusNode((MinusNode) node);

        return NotSupported(node);
    }

    @TruffleBoundary
    public MGNode visitMinusNode(MinusNode node) throws MGException {
        MGNode operand = visit(node.getOperand());
        DataType kind = operand.getExpectedType();
        MGNode minusOne = null;
        if (kind == DataType.Int)
            minusOne = new MGNodeOperand(new MGIntLiteral(-1));
        else if (kind == DataType.Long)
            minusOne = new MGNodeOperand(new MGLongLiteral(-1));
        else if (kind == DataType.Double)
            minusOne = new MGNodeOperand(new MGDoubleLiteral(-1.0));

        assert minusOne != null;
        return new MGNodeBinOp(operand, BinOpType.MUL, minusOne, kind);
    }

    @TruffleBoundary
    public MGNode visitDivNode(DivNode node) throws MGException {
        MGNode retVal = null;
        MGNode left = visit(node.getLeftNode());
        MGNode right = visit(node.getRightNode());
        DataType kind = getDominantType(left.getExpectedType(), right.getExpectedType());

        if (kind != DataType.Complex) {
            retVal = new MGNodeBinOp(left, BinOpType.DIV, right, DataType.Double);
        } else {

            MGNode realLeft = (left.getExpectedType() == kind) ? ((MGNodeOperandComplex) left).getReal() : left;
            MGNode realRight = (right.getExpectedType() == kind) ? ((MGNodeOperandComplex) right).getReal() : right;
            MGNode imagLeft = (left.getExpectedType() == kind) ? ((MGNodeOperandComplex) left).getImag() : MGNodeOperand.ZERO;
            MGNode imagRight = (right.getExpectedType() == kind) ? ((MGNodeOperandComplex) right).getImag() : MGNodeOperand.ZERO;

            // normalizer
            MGNode normalizer = new MGNodeBinOp(new MGNodeBinOp(realRight, BinOpType.MUL, realRight, DataType.Double), BinOpType.ADD,
                            new MGNodeBinOp(imagRight, BinOpType.MUL, imagRight, DataType.Double),
                            DataType.Double);

            MGNode real = new MGNodeBinOp(
                            new MGNodeBinOp(new MGNodeBinOp(realLeft, BinOpType.MUL, realRight, DataType.Double), BinOpType.ADD,
                                            new MGNodeBinOp(imagLeft, BinOpType.MUL, imagRight, DataType.Double),
                                            DataType.Double),
                            BinOpType.DIV, normalizer, DataType.Double);

            MGNode imag = new MGNodeBinOp(
                            new MGNodeBinOp(new MGNodeBinOp(imagLeft, BinOpType.MUL, realRight, DataType.Double), BinOpType.SUB,
                                            new MGNodeBinOp(realLeft, BinOpType.MUL, imagRight, DataType.Double),
                                            DataType.Double),
                            BinOpType.DIV, normalizer, DataType.Double);

            retVal = new MGNodeOperandComplex(real, imag);
        }

        return retVal;
    }

    @TruffleBoundary
    public MGNode visitFloorDivNode(FloorDivNode node) throws MGException {
        MGNode left = visit(node.getLeftNode());
        MGNode right = visit(node.getRightNode());
        DataType kind = getDominantType(left.getExpectedType(), right.getExpectedType());
        // TODO: consider Long
        return castInt(new MGNodeBinOp(left, BinOpType.DIV, right, kind));
    }

    @TruffleBoundary
    public MGNode visitModuloNode(ModuloNode node) throws MGException {
        MGNode left = visit(node.getLeftNode());
        MGNode right = visit(node.getRightNode());
        DataType kind = getDominantType(left.getExpectedType(), right.getExpectedType());
        return new MGNodeBinOp(left, BinOpType.MOD, right, kind);
    }

    @TruffleBoundary
    public MGNode visitPowerNode(PowerNode node) throws MGException {
        MGNode left = visit(node.getLeftNode());
        MGNode right = visit(node.getRightNode());
        DataType kind = getDominantType(left.getExpectedType(), right.getExpectedType());
        return new MGNodeBinOp(left, BinOpType.POW, right, kind);
    }

    @Override
    @TruffleBoundary
    public MGNode visitOrNode(OrNode node) throws MGException {
        MGNode left = visit(node.getLeftNode());
        MGNode right = visit(node.getRightNode());
        DataType kind = DataType.Bool;
        return new MGNodeBinOp(left, BinOpType.OR, right, kind);
    }

    @Override
    @TruffleBoundary
    public MGNode visitCastToBooleanNode(CastToBooleanNode node) throws MGException {
        MGNode operand = visit(node.getOperand());
        DataType kind = DataType.Bool;
        if (node instanceof NotNode)
            return new MGNodeUnaryOp(operand, UnaryOpType.Not, kind);

        return operand;
    }

    @Override
    @TruffleBoundary
    public MGNode visitAndNode(AndNode node) throws MGException {
        MGNode left = visit(node.getLeftNode());
        MGNode right = visit(node.getRightNode());
        DataType kind = DataType.Bool;
        return new MGNodeBinOp(left, BinOpType.AND, right, kind);
    }

    @Override
    @TruffleBoundary
    public MGNode visitBinaryBitwiseNode(BinaryBitwiseNode node) throws MGException {
        MGNode left = visit(node.getLeftNode());
        MGNode right = visit(node.getRightNode());
        BinOpType op = BinOpType.LeftShift;
        if (node instanceof LeftShiftNode) {
            op = BinOpType.LeftShift;
        } else if (node instanceof RightShiftNode) {
            op = BinOpType.RightShift;
        } else if (node instanceof BitAndNode) {
            op = BinOpType.BitAND;
        } else if (node instanceof BitXorNode) {
            op = BinOpType.BitXOR;
        } else if (node instanceof BitOrNode) {
            op = BinOpType.BitOR;
        }
        return new MGNodeBinOp(left, op, right, left.getExpectedType());
    }

    @Override
    @TruffleBoundary
    public MGNode visitBinaryComparisonNode(BinaryComparisonNode node) throws MGException {
        MGNode left = visit(node.getLeftNode());
        MGNode right = visit(node.getRightNode());
        BinOpType op = BinOpType.Equal;
        if (node instanceof EqualNode) {
            op = BinOpType.Equal;
        } else if (node instanceof NotEqualNode) {
            op = BinOpType.NotEqual;
        } else if (node instanceof LessThanNode) {
            op = BinOpType.LessThan;
        } else if (node instanceof LessThanEqualNode) {
            op = BinOpType.LessEqual;
        } else if (node instanceof GreaterThanNode) {
            op = BinOpType.GreaterThan;
        } else if (node instanceof GreaterThanEqualNode) {
            op = BinOpType.GreaterEqual;
        }
        return new MGNodeBinOp(left, op, right, DataType.Bool);
    }

    @Override
    @TruffleBoundary
    public MGNode visitIntegerLiteralNode(IntegerLiteralNode node) {
        return new MGNodeOperand(new MGIntLiteral((int) node.execute(frame)));
    }

    @Override
    @TruffleBoundary
    public MGNode visitDoubleLiteralNode(DoubleLiteralNode node) {
        return new MGNodeOperand(new MGDoubleLiteral((double) node.execute(frame)));
    }

    @Override
    @TruffleBoundary
    public MGNode visitBooleanLiteralNode(BooleanLiteralNode node) {
        return new MGNodeOperand(new MGBoolLiteral((boolean) node.execute(frame)));
    }

    @Override
    @TruffleBoundary
    public MGNode visitListLiteralNode(ListLiteralNode node) throws MGException {
        final PythonBoxed generic = new PythonBoxed.GenericBoxed(node);
        Object value = generic.getUnboxed(frame);
        MGStorage var = env.addArrayParameter(null, boxUtil.specialize1DArray(node, value));
        var = var.copy();
        return new MGNodeOperand(var);
    }

    @Override
    @TruffleBoundary
    public MGNode visitComplexLiteralNode(ComplexLiteralNode node) {
        PComplex complex = (PComplex) node.execute(frame);

        MGNode real = new MGNodeOperand(new MGDoubleLiteral(complex.getReal()));
        MGNode imag = new MGNodeOperand(new MGDoubleLiteral(complex.getImag()));

        return new MGNodeOperandComplex(real, imag);
    }

    @Override
    @TruffleBoundary
    public MGNode visitListAppendNode(ListAppendNode node) throws MGException {
        MGNode left = visit(node.getLeftNode());
        MGNode right = visit(node.getRightNode());
        DataType kind = getDominantType(left.getExpectedType(), right.getExpectedType());
        return new MGNodeAssign(left, right, kind).setSource(node.getSourceSection());
    }

    protected MGStorage createLocalVar(String name, MGNode right) throws MGException {
        DataType kind = null;
        kind = right.getExpectedType();
        if (right instanceof MGNodeIf) {
            MGNodeIf ifNode = (MGNodeIf) right;
            kind = ifNode.getThen().getExpectedType();
            if (kind != ifNode.getOrelse().getExpectedType())
                throw TypeException.INSTANCE.message("We couldn't determine the type of the variable '" + name + "' right hand side assigment (if/else).");
        }
        MGStorage var = null;
        if (kind == DataType.Int) {
            var = env.registerIntVar(name);
        } else if (kind == DataType.Long) {
            var = env.registerLongVar(name);
        } else if (kind == DataType.Double) {
            var = env.registerDoubleVar(name);
        } else if (kind == DataType.IntArray) {
            // Since we are not accessing an index
            var = env.registerIntArrayVar(name, new MGIntArray(name, (MGArray) ((MGNodeOperand) right).getValue())).copy().setDefine();
        } else if (kind == DataType.LongArray) {
            // Since we are not accessing an index
            var = env.registerLongArrayVar(name, new MGLongArray(name, (MGArray) ((MGNodeOperand) right).getValue())).copy().setDefine();
        } else if (kind == DataType.DoubleArray) {
            // Since we are not accessing an index
            var = env.registerDoubleArrayVar(name, new MGDoubleArray(name, (MGArray) ((MGNodeOperand) right).getValue())).copy().setDefine();
        } else if (kind == DataType.Complex) {
            var = env.registerComplexVar(name);
        } else
            throw TypeException.INSTANCE.message("We couldn't determine the type of the variable '" + name + "' right hand side assigment.");

        return var;
    }

    private static MGNode assign(MGObject var, MGNode right) throws MGException {
        MGNode retVal = null;

        if (var.getDataType() != DataType.Complex) {
            DataType kind = var.getDataType();
            if (right instanceof MGNodeIf) {
                MGNodeIf ifNode = (MGNodeIf) right;
                kind = ifNode.getThen().getExpectedType(); // from Then
                if (kind != ifNode.getOrelse().getExpectedType())
                    throw TypeException.INSTANCE.message("We couldn't determine the type of the variable '" + var.getName() + "' right hand side assigment (if/else).");

            }
            MGNode left = new MGNodeOperand(var);

            retVal = new MGNodeAssign(left, right);
        } else {
            if (right instanceof MGNodeIf)
                throw TypeException.INSTANCE.message("right hand side assigment (if/else) of the complex variable '" + var.getName() + "' is not supported.");

            MGNode realLeft = new MGNodeOperand(((MGComplex) var).getReal());
            MGNode imagLeft = new MGNodeOperand(((MGComplex) var).getImag());
            MGNodeAssign real = new MGNodeAssign(realLeft, ((MGNodeOperandComplex) right).getReal());
            MGNodeAssign imag = new MGNodeAssign(imagLeft, ((MGNodeOperandComplex) right).getImag());
            retVal = new MGNodeAssignComplex(real, imag);
        }

        return retVal;
    }

    @Override
    @TruffleBoundary
    public MGNode visitSetAttributeNode(SetAttributeNode node) throws MGException {
        String name = node.getAttributeId();
        MGStorage var = env.getVar(name);
        MGNode right = visit(node.getRhs());
        if (var == null)
            var = createLocalVar(name, right);

        env.addDefUse(name, right);
        env.getGlobalEnv().setIllegalForOpenCL(MGArray.isArray(var.getDataType()));
        MGNode assignment = assign(var, right).setSource(node.getSourceSection());
        if (MGArray.isArray(var.getDataType())) {
            assignment = arrayAssignment(assignment, var, (MGNodeOperand) right);
        }
        return assignment;
    }

    protected MGNode arrayAssignment(MGNode assignment, MGStorage var, MGNodeOperand right) {
        env.getGlobalEnv().setIllegalForOpenCL(true);
        final String varName = var.getName();
        MGArray array = (MGArray) right.getValue();
        final ArrayList<MGNode> assignments = new ArrayList<>(array.getArrayInfo().getDim() + 1);
        for (int i = 0; i < array.getArrayInfo().getDim(); i++) {
            final MGObject dimSize = env.getVar(array.getName() + MGBaseEnv.DIMSIZE + i);
            final MGNodeOperand size = new MGNodeOperand(dimSize);
            final String varDimSize = varName + MGBaseEnv.DIMSIZE + i;
            MGStorage varSize = env.getVar(varDimSize);
            if (varSize == null) {
                varSize = env.registerIntVar(varDimSize);
            }
            assignments.add(assign(varSize, size));
        }
        assignments.add(assignment);
        return new MGNodeBlock(assignments);

    }

    @Override
    @TruffleBoundary
    public MGNode visitWriteLocalVariableNode(WriteLocalVariableNode node) throws MGException {
        if (node.getRhs() instanceof ReadIndexedArgumentNode)
            return new MGNodeEmpty();

        MGNode right = visit(node.getRhs());
        String name = (String) node.getSlot().getIdentifier();
        if (name.contentEquals(TranslationEnvironment.RETURN_SLOT_ID))
            return right;

        MGStorage var = env.getVar(name);
        boolean defined = var != null;
        if (defined && var.getDataType() != right.getExpectedType()) {
            if (var.getDataType() == DataType.Long && right.getExpectedType() == DataType.Int) {
                right = castLong(right);
            } else if (var.getDataType() == DataType.Int && right.getExpectedType() == DataType.Long) {
                right = castInt(right);
            } else {
                name = env.createVarReplacementName(name);
                defined = false;
            }
        }
        if (!defined) {
            var = createLocalVar(name, right);
        }

        if (var.isPropagated())
            return new MGNodeEmpty();
        env.addDefUse(name, right);
        env.getVar(name).setReadWrite();
        MGNode assignment = assign(var, right).setSource(node.getSourceSection());
        if (MGArray.isArray(var.getDataType())) {
            assignment = arrayAssignment(assignment, var, (MGNodeOperand) right);
        }
        return assignment;
    }

    private MGNode getVar(MGObject value) {
        MGObject var = value;
        String varname = var.getName();
        if (var.getDataType() == DataType.Complex) {
            MGNode real = new MGNodeOperand(((MGComplex) var).getReal());
            MGNode imag = new MGNodeOperand(((MGComplex) var).getImag());
            return new MGNodeOperandComplex(real, imag);
        } else
            return new MGNodeOperand(((MGStorage) var).copy().setDefUseIndex(env.getDefUseIndex(varname)));
    }

    @Override
    public MGStorage specializeLocalVariable(PNode node, String varname, Object value, FrameSlotKind kind) {
        final MGStorage var;
        final Boxed<?> box;
        switch (kind) {
            case Object:
                if (value instanceof PComplex) {
                    final PythonBoxed real = new PythonBoxed.ComplexRealBoxed(node);
                    final PythonBoxed imag = new PythonBoxed.ComplexImagBoxed(node);
                    final PComplex c = (PComplex) value;
                    var = env.addComplexParameter(varname, c.getReal(), real, c.getImag(), imag).copy();
                } else if (value instanceof PList || value instanceof PTuple) {
                    box = boxUtil.specializeArray(node, value);
                    var = env.addArrayParameter(varname, box).copy();
                } else
                    throw TypeException.INSTANCE.message("Variable '" + varname + "' type is not supported: " + kind);
                break;
            case Int:
                box = new PythonBoxed.IntBoxed(node);
                var = env.addIntParameter(varname, (int) value, box).copy();
                break;
            case Long:
                box = new PythonBoxed.LongBoxed(node);
                var = env.addLongParameter(varname, (long) value, box).copy();
                break;
            case Float:
            case Double:
                box = new PythonBoxed.DoubleBoxed(node);
                var = env.addDoubleParameter(varname, (double) value, box).copy();
                break;
            case Boolean:
                box = new PythonBoxed.BooleanBoxed(node);
                var = env.addBooleanParameter(varname, (boolean) value, box).copy();
                break;
            default:
                throw TypeException.INSTANCE.message("Variable '" + varname + "' type is not supported: " + kind);
        }
        return var;

    }

    @Override
    @TruffleBoundary
    public MGNode visitReadLocalVariableNode(ReadLocalVariableNode node) throws MGException {
        String varname = (String) node.getSlot().getIdentifier();
        MGStorage var = env.getVar(varname);
        if (var != null) {
            if (storeSubscript && var instanceof MGArray)
                ((MGArray) var).setReadWrite();
            return getVar(var);
        }
        final PythonBoxed generic = new PythonBoxed.GenericBoxed(node);
        Object value = generic.getUnboxed(frame);
        var = specializeLocalVariable(node, varname, value, node.getSlot().getKind());
        final MGNode retVal = new MGNodeOperand(var);

        if (storeSubscript && var instanceof MGArray)
            ((MGArray) env.getVar(varname)).setReadWrite();
        return retVal;
    }

    @Override
    public boolean isTypeSupported(Object value) {
        return value instanceof Integer || value instanceof Long ||
                        value instanceof Double || value instanceof Boolean ||
                        value instanceof PList || value instanceof PTuple ||
                        value instanceof PComplex;
    }

    @Override
    public MGStorage specializeParameter(PNode node, String varname, Object value) {
        final MGStorage var;
        if (value instanceof Integer) {
            final PythonBoxed box = new PythonBoxed.IntBoxed(node);
            var = env.addIntParameter(varname, (int) value, box).copy();
        } else if (value instanceof Long) {
            final PythonBoxed box = new PythonBoxed.LongBoxed(node);
            var = env.addLongParameter(varname, (long) value, box).copy();
        } else if (value instanceof Double) {
            final PythonBoxed box = new PythonBoxed.DoubleBoxed(node);
            var = env.addDoubleParameter(varname, (double) value, box).copy();
        } else if (value instanceof PList || value instanceof PTuple) {
            final Boxed<?> box = boxUtil.specializeArray(node, value);
            var = env.addArrayParameter(varname, box).copy();
        } else if (value instanceof PComplex) {
            final PythonBoxed real = new PythonBoxed.ComplexRealBoxed(node);
            final PythonBoxed imag = new PythonBoxed.ComplexImagBoxed(node);
            final PComplex c = (PComplex) value;
            var = env.addComplexParameter(varname, c.getReal(), real, c.getImag(), imag).copy();
        } else
            throw TypeException.INSTANCE.message("Variable '" + varname + "' type is not supported: " + value);

        return var;
    }

    @Override
    public ArrayList<MGNode> specializeArguments(PFunction function, Object[] arguments, int prefix) {
        final List<String> parameters = function.getArity().getParameterIds();
        final ArrayList<MGNode> args = new ArrayList<>(parameters.size());
        int pid = 0;
        for (int i = prefix; i < arguments.length; i++) {
            final Object value = arguments[i];
            final String varname = parameters.get(pid);
            final PNode pythonArgNode = new ReadIndexedArgumentNode.InBoundReadArgumentNode(i);
            final MGStorage var = specializeParameter(pythonArgNode, varname, value);
            args.add(new MGNodeOperand(var));
            pid++;
        }
        return args;
    }

    @Override
    @TruffleBoundary
    public MGNode visitReadGlobalNode(ReadGlobalNode node) throws MGException {
        MGNode retVal = null;
        String varname = node.getAttributeId();
        MGStorage var = env.getVar(varname);
        if (var != null) {
            if (storeSubscript && var instanceof MGArray)
                ((MGArray) var).setReadWrite();
            return getVar(var);
        }
        final PythonBoxed generic = new PythonBoxed.GenericBoxed(node);
        Object value = generic.getUnboxed(frame);
        var = specializeParameter(node, varname, value);
        retVal = new MGNodeOperand(var);
        if (storeSubscript && var instanceof MGArray)
            ((MGArray) env.getVar(varname)).setReadWrite();
        return retVal;
    }

    @Override
    @TruffleBoundary
    public MGNode visitBlockNode(BlockNode node) throws MGException {
        ArrayList<MGNode> nodes = new ArrayList<>();
        for (PNode n : node.getStatements()) {
            MGNode apNode = visit(n);
            if (!(apNode instanceof MGNodeEmpty)) {
                if (apNode instanceof MGNodeBlock) {
                    nodes.addAll(((MGNodeBlock) apNode).getChildren());
                    if (!nodes.isEmpty())
                        apNode = nodes.get(nodes.size() - 1);
                } else {
                    nodes.add(apNode);
                }
            }
            if (apNode instanceof MGNodeJumpFrom || (apNode instanceof MGNodeControl && ((MGNodeControl) apNode).isAlwaysReturn())) {
                break;
            }
        }
        if (nodes.size() == 1) {
            return nodes.get(0);
        }
        return new MGNodeBlock(nodes);
    }

    @TruffleBoundary
    public String getName(PNode node) {
        String name = null;
        if (node instanceof ReadLocalVariableNode) {
            name = (String) ((ReadLocalVariableNode) node).getSlot().getIdentifier();
        } else if (node instanceof ReadGlobalNode) {
            name = ((ReadGlobalNode) node).getAttributeId();
        } else if (node instanceof SetAttributeNode) {
            name = ((SetAttributeNode) node).getAttributeId();
        } else if (node instanceof WriteLocalVariableNode) {
            name = (String) ((WriteLocalVariableNode) node).getSlot().getIdentifier();
        }

        return name;
    }

    @TruffleBoundary
    public MGStorage registerIterationVar(PNode target) {
        MGStorage var = null;
        String name = env.addNameTag(getName(target));

        if (name != null) {
            var = env.registerIntVar(name);
        }

        return var;
    }

    // Iterating throughout a list
    @TruffleBoundary
    @Deprecated
    public MGStorage registerVar(PNode target, MGArray right, MGObject index) {
        String name = env.addNameTag(getName(target));
        MGStorage var = null;
        if (name == null)
            return var;

        MGArray value = (MGArray) right.copy();
        value.addPerminantIndex(name, new MGNodeOperand(index));

        if (value.getDataType() == DataType.Int)
            var = new MGInt(name, value, null);
        else if (value.getDataType() == DataType.Long)
            var = new MGLong(name, value, null);
        else if (value.getDataType() == DataType.IntArray)
            var = env.registerIntArrayVar(name, value);
        else if (value.getDataType() == DataType.LongArray)
            var = env.registerLongArrayVar(name, value);
        else if (value.getDataType() == DataType.Double) {
            env.getGlobalEnv().setRequireDouble(true);
            var = new MGDouble(name, value, null);
        } else if (value.getDataType() == DataType.DoubleArray) {
            env.getGlobalEnv().setRequireDouble(true);
            var = env.registerDoubleArrayVar(name, value);
        }

        var.setDefine();
        env.registerVar(name, var);

        return var;
    }

    private MGNode processForNode(PNode target, PNode forbody, PNode iterator, SourceSection source, MGNodeOptions options) throws MGException {
        MGNodeJumpTo holdBreakVar = this.breakFlag;
        this.breakFlag = null;

        GetIteratorNode call = (GetIteratorNode) iterator;
        MGNode range = visit(call.getOperand());

        MGStorage inductionVar = registerIterationVar(target);

        LoopInfo info = null;
        // Ideal case:
        if (range instanceof MGNodeBuiltinFunction && ((MGNodeBuiltinFunction) range).getType() == BuiltinFunctionType.RANGE) {
            if (inductionVar == null)
                throw TypeException.INSTANCE.message("Iteration variable type is not supported " + target.getClass());
            info = new LoopInfo(inductionVar, ((MGNodeBuiltinFunction) range).getNodes(), options);
        }
        /*-
        // not supported yet
        else if (range instanceof MGNodeOperand) {
            /*- X = [1,2,3]
             *  for i in X
             *  for i in [1,2,3]
             * /
            DataType kind = range.getExpectedType();
            assert kind == DataType.BoolArray || kind == DataType.IntArray || kind == DataType.LongArray || kind == DataType.DoubleArray;
            inductionVar = env.registerIntVar(null);
            MGArray iterList = (MGArray) ((MGNodeOperand) range).getValue();
            MGObject targetVar = env.registerVar(target, iterList, inductionVar);
            int stop = iterList.getArrayInfo().getSize(0);
            MGNode stopNode = new MGNodeOperand(env.addParameter(null, stop, call.getOperand()));
            info = new LoopInfo(inductionVar, stopNode, targetVar, options);
        }
        */
        else
            throw TypeException.INSTANCE.message("Iteration variable type is not supported " + target.getClass());

        currentLoopInfos.put(inductionVar.getName(), info);
        MGNode body = visit(forbody);
        env.addLoopInfo(info);
        currentLoopInfos.remove(inductionVar.getName());
        final MGNode ret = new MGNodeFor(body, info, this.breakFlag).setSource(source);
        this.breakFlag = holdBreakVar;
        return ret;
    }

    @Override
    public MGNode visitMGForNode(MGForNode node) throws Exception {
        forNodes.push(node);
        MGNodeOptions options = MGNodeOptions.getOptions(node.hashCode());
        return processForNode(node.getTarget(), node.getBody(), node.getIterator(), node.getSourceSection(), options);
    }

    @Override
    public MGNode visitForNode(ForNode node) throws MGException {
        forNodes.push(node);
        MGNodeOptions options = MGNodeOptions.getOptions(node.hashCode());
        return processForNode(node.getTarget(), node.getBody(), node.getIterator(), node.getSourceSection(), options);

    }

    @Override
    public MGNode visitIfNode(IfNode node) throws MGException {
        // if condition
        if (!(node.getElse() instanceof EmptyNode)) {
            env.enterScop();
        }
        MGNode cond = visit(node.getCondition()); // 0
        MGNode then = visit(node.getThen());
        MGNode orelse = null;
        if (!(node.getElse() instanceof EmptyNode)) {
            // else
            env.addThenSkip();
            env.enterScop();
            orelse = visit(node.getElse());
            env.exitScop();
        }
        return new MGNodeIf(cond, then, orelse);
    }

    @Override
    public MGNode visitElseNode(ElseNode node) throws MGException {
        final MGNodeFor forNode = (MGNodeFor) visit(node.getThen());
        final MGNodeJumpTo forBreakVar = forNode.getjLabel();
        assert forBreakVar != null;
        MGNode orelseNode = visit(node.getOrelse());
        return new MGNodeBreakElse(forNode, orelseNode, forBreakVar);
    }

    @Override
    public MGNode visitBreakTargetNode(BreakTargetNode node) throws MGException {
        return visit(node.getStatement());
    }

    @Override
    public MGNode visitBreakNode(BreakNode node) throws MGException {
        if (this.breakFlag == null)
            this.breakFlag = new MGNodeJumpTo();

        return new MGNodeJumpFrom((MGNodeJumpTo) this.breakFlag.copy());
    }

    @Override
    public MGNode visitPythonCallNode(PythonCallNode node) throws MGException {
        final ArrayList<MGNode> nodes = new ArrayList<>();

        DataType kind = null;
        final Object callee = node.getCalleeNode().execute(frame);
        if (callee instanceof PBuiltinFunction) {
            final PBuiltinFunction pcallee = (PBuiltinFunction) callee;
            final String functionName = pcallee.getName();
            MathFunctionType kernelFuncOp = env.getMathFunction(functionName);
            if (kernelFuncOp != null) {
                env.addUsedMathFunction(kernelFuncOp);
                PNode[] args = node.getArgumentsNode().getArguments();
                if (args.length > 1)
                    throw CoverageException.INSTANCE.message("More than one argument for builtin funtion '" + pcallee.getName() + "' is not supported.");
                final MGNode arg = visit(node.getArgumentsNode().getArguments()[0]);
                if (kernelFuncOp == MathFunctionType.abs && arg.getExpectedType() == DataType.Complex) {
                    kind = DataType.Double;
                    kernelFuncOp = MathFunctionType.hypot;
                    nodes.add(((MGNodeOperandComplex) arg).getReal());
                    nodes.add(((MGNodeOperandComplex) arg).getImag());
                } else {
                    kind = arg.getExpectedType();
                    nodes.add(arg);
                }
                return new MGNodeMathFunction(nodes, kind, kernelFuncOp);
            } else if (functionName.contentEquals("min") || functionName.contentEquals("max")) {
                final MGNode arg1 = visit(node.getArgumentsNode().getArguments()[0]);
                final MGNode arg2 = visit(node.getArgumentsNode().getArguments()[1]);
                if (arg1.getExpectedType() != arg2.getExpectedType() && !intOrLong(arg1.getExpectedType(), arg2.getExpectedType()))
                    throw TypeException.INSTANCE.message(functionName + " parameters should have same data types.");
                nodes.add(arg1);
                nodes.add(arg2);
                kind = getDominantType(arg1.getExpectedType(), arg2.getExpectedType());
                return new MGNodeBuiltinFunction(nodes, kind, (functionName.contentEquals("min")) ? BuiltinFunctionType.MIN : BuiltinFunctionType.MAX);
            } else if (functionName.contentEquals("len")) {
                final PythonBoxed box = new PythonBoxed.IntLengthBoxed(node);
                int len = (int) box.getUnboxed(frame);
                MGObject variable = env.addIntParameter(null, len, box).copy();
                return new MGNodeOperand(variable);
            }
            throw CoverageException.INSTANCE.message("Builtin Function " + pcallee.getName() + " is not supported.");

        } else if (callee instanceof PythonBuiltinClass) {
            PythonBuiltinClass pcallee = (PythonBuiltinClass) callee;
            String className = pcallee.getName();
            if (className.contentEquals("int")) {
                final MGNode arg = visit(node.getArgumentsNode().getArguments()[0]);
                return castInt(arg);
            } else if (className.contentEquals("float")) {
                final MGNode arg = visit(node.getArgumentsNode().getArguments()[0]);
                return new MGNodeUnaryOp(arg, UnaryOpType.Cast, DataType.Double);
            } else if (className.contentEquals("complex")) {
                final MGNode real = visit(node.getArgumentsNode().getArguments()[0]);
                final MGNode imag = visit(node.getArgumentsNode().getArguments()[1]);
                return new MGNodeOperandComplex(real, imag);
            } else if (className.contentEquals("range")) {
                final PNode[] args = node.getArgumentsNode().getArguments();
                final MGNode startNode = (args.length > 1) ? castInt(visit(args[0])) : null;
                final MGNode stopNode = (args.length > 1) ? castInt(visit(args[1])) : castInt(visit(args[0]));
                final MGNode stepNode = (args.length > 2) ? castInt(visit(args[2])) : null;
                nodes.add(startNode);
                nodes.add(stopNode);
                nodes.add(stepNode);
                return new MGNodeBuiltinFunction(nodes, DataType.Int, BuiltinFunctionType.RANGE);
            }
        } else if (callee instanceof PFunction) {
            final ArrayList<MGNode> args = new ArrayList<>(node.getArgumentsNode().getArguments().length);
            for (PNode a : node.getArgumentsNode().getArguments()) {
                args.add(visit(a));
            }
            final MGNode call = createCall(args, (PFunction) callee);
            return call;
        }
        throw CoverageException.INSTANCE.message("Function " + node.getCalleeName() + " is not supported.");
    }

    private void tracebackDefUse(MGNodeUserFunction functionNode, ArrayList<MGNode> args) {
        final MGGlobalEnv gEnv = env.getGlobalEnv();
        final int size = functionNode.getParameters().size();
        for (int i = 0; i < size; i++) {
            final MGNode arg = args.get(i);
            if (MGArray.isArray(arg.getExpectedType())) {
                if (arg instanceof MGNodeOperand) {
                    gEnv.addArgDefUse(functionNode.getParameters().get(i).getName(), ((MGNodeOperand) arg).getValue().getName());
                }
            }
        }
    }

    @Override
    public MGNodeFunctionCall createCall(ArrayList<MGNode> passedArgs, PFunction function) {
        String functionTag = "";
        final ArrayList<MGNode> args = new ArrayList<>(passedArgs.size());
        for (MGNode arg : passedArgs) {
            args.add(arg);
            functionTag += "_" + arg.getExpectedType();
            if (MGArray.isArray(arg.getExpectedType()) && arg instanceof MGNodeOperand) {
                final MGArray array = (MGArray) ((MGNodeOperand) arg).getValue();
                for (int i = 0; i < array.getArrayInfo().getDim(); i++) {
                    final MGObject dimSize = env.getVar(array.getName() + MGBaseEnv.DIMSIZE + i);
                    final MGNodeOperand size = new MGNodeOperand(dimSize);
                    args.add(size);
                    functionTag += "_" + size.getExpectedType();
                }

            }
        }
        final MGNodeFunctionCall call = processUserFunction(function, args, functionTag);
        return call;

    }

    @Override
    public MGNodeFunctionCall processUserFunction(PFunction function, ArrayList<MGNode> args, String functionTag) throws MGException {
        final String functionID = function.getName();
        final String functionName = functionID + functionTag;
        MGNodeUserFunction functionNode = null;
        MGPrivateEnv privateEnv = env.getGlobalEnv().getPrivateEnvironment(functionID, functionTag);
        if (privateEnv == null) {
            privateEnv = env.getGlobalEnv().createPrivateEnvironment(env, functionID, functionTag);
            final MGPythonTree subTree = new MGPythonTree(privateEnv, frame, log);
            privateEnv.enterScop();
            final Arity functionParameters = function.getArity();
            if (functionParameters.isClassMethod() || functionParameters.isStaticMethod()) {
                throw CoverageException.INSTANCE.message("Class and Static methods aren't supported (yet).");
            }

            final ArrayList<MGStorage> parameters = new ArrayList<>();
            int k = 0;
            for (int i = 0; i < functionParameters.getParameterIds().size(); i++) {
                final MGNode arg = args.get(k++);
                final String pname = functionParameters.getParameterIds().get(i);
                final MGStorage p = subTree.createLocalVar(pname, arg);
                parameters.add(p);
                if (MGArray.isArray(arg.getExpectedType()) && arg instanceof MGNodeOperand) {
                    final MGArray array = (MGArray) ((MGNodeOperand) arg).getValue();
                    for (int j = 0; j < array.getArrayInfo().getDim(); j++) {
                        final String dimSize = MGBaseEnv.DIMSIZE + j;
                        final MGStorage pp = privateEnv.registerIntArrayDimSize(pname, dimSize);
                        pp.setDefined();
                        parameters.add(pp);
                        k++;
                    }
                }
            }

            final FunctionRootNode functionNodeRoot = (FunctionRootNode) function.getFunctionRootNode();
            final MGNode body = subTree.visit(functionNodeRoot.getBody());
            DataType type = DataType.None;
            MGNodeOperand returnVar = null;
            if (MGNodeControl.checkAlwaysReturn(body)) {
                type = subTree.returns.get(0).getExpectedType();
                returnVar = subTree.returns.get(0).getLeft();
                boolean match = true;
                for (MGNode r : subTree.returns) {
                    match = match && type == r.getExpectedType();
                    if (!match)
                        break;
                }
                if (!match) {
                    throw TypeException.INSTANCE.message("We couldn't determine the return type of the function '" + functionID + "'.");
                }

            } else {
                if (subTree.returns.size() > 0) {
                    boolean match = true;
                    for (MGNode r : subTree.returns) {
                        match = match && type == r.getExpectedType();
                        if (!match)
                            break;
                    }
                    if (!match) {
                        throw TypeException.INSTANCE.message("Function '" + functionID + "' does not always return a value.");
                    }
                }
            }
            functionNode = new MGNodeUserFunction(functionID, functionName, parameters, body, returnVar, type, privateEnv);
            privateEnv.setFunction(functionNode);
            env.getParameters().putAll(privateEnv.getParameters());
        } else {
            functionNode = privateEnv.getFunction();
        }
        tracebackDefUse(functionNode, args);
        final MGNodeFunctionCall call = new MGNodeFunctionCall(functionNode, args);
        return call;
    }

    @Override
    public MGNode visitReturnTargetNode(ReturnTargetNode node) throws Exception {
        if (env instanceof MGPrivateEnv) {
            return visit(node.getBody());
        }
        return NotSupported(node);
    }

    @Override
    public MGNode visitFrameReturnNode(FrameReturnNode node) throws Exception {
        if (env instanceof MGPrivateEnv) {
            if (this.returnFlag == null)
                this.returnFlag = new MGNodeJumpTo();
            final MGNode right = visit(node.getRight());
            final MGNodeOperand left = new MGNodeOperand(createLocalVar(OpenCLTranslator.RETURN_ID, right));
            final MGNodeReturn r = new MGNodeReturn(left, right, (MGNodeJumpTo) this.returnFlag.copy());
            returns.add(r);
            return r;
        }
        return NotSupported(node);
    }

    @Override
    public MGNode visitWhileNode(WhileNode node) throws Exception {
        final MGNode cond = visit(node.getCondition());
        final MGNodeJumpTo holdBreakVar = this.breakFlag;
        final MGNode body = visit(node.getBody());

        final MGNodeWhile ret = new MGNodeWhile(body, cond, this.breakFlag);
        this.breakFlag = holdBreakVar;
        return ret;
    }

    @Override
    public MGNode visitReadIndexedArgumentNode(ReadIndexedArgumentNode node) throws Exception {
        if (env instanceof MGPrivateEnv) {
            // Try to ignore it and deal with it's parent instead.
        }
        return NotSupported(node);
    }

    @Override
    public MGNode visitEmptyNode(EmptyNode node) throws Exception {
        return new MGNodeEmpty();
    }

    @Override
    public MGNode visitStringLiteralNode(StringLiteralNode node) throws Exception {
        if (node.getParent() instanceof BlockNode) {
            return new MGNodeEmpty();
        }
        return NotSupported(node);
    }

    @Override
    public MGNode visit(PNode node) throws MGException {
        Object ret = null;
        try {
            ret = node.accept(this);
        } catch (MGException e) {
            log.printException(e);
            throw e;
        } catch (Exception e) {
            if (MGOptions.Debug > 5)
                e.printStackTrace();
            NotSupported(node);
        }
        return (MGNode) ret;
    }

    // Not supported yet

    @Override
    public MGNode visitContinueTargetNode(ContinueTargetNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitGetIteratorNode(GetIteratorNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitStopIterationTargetNode(StopIterationTargetNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitContinueNode(ContinueNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitPythonBuiltinNode(PythonBuiltinNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitReadGeneratorFrameVariableNode(ReadGeneratorFrameVariableNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitFrameSwappingNode(FrameSwappingNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitComprehensionNode(ComprehensionNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitGeneratorReturnTargetNode(GeneratorReturnTargetNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitGeneratorWhileNode(GeneratorWhileNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitWriteGeneratorFrameVariableNode(WriteGeneratorFrameVariableNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitGeneratorForNode(GeneratorForNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitFrameTransferNode(FrameTransferNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitYieldNode(YieldNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitDictLiteralNode(DictLiteralNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitTupleLiteralNode(TupleLiteralNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitSetLiteralNode(SetLiteralNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitObjectLiteralNode(ObjectLiteralNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitBigIntegerLiteralNode(BigIntegerLiteralNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitKeywordLiteralNode(KeywordLiteralNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitTryFinallyNode(TryFinallyNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitRaiseNode(RaiseNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitWithNode(WithNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitPrintNode(PrintNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitAssertNode(AssertNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitTryExceptNode(TryExceptNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitExceptNode(ExceptNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitClassDefinitionNode(ClassDefinitionNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitImportStarNode(ImportStarNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitDefaultParametersNode(DefaultParametersNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitImportFromNode(ImportFromNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitImportNode(ImportNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitReadLevelVariableNode(ReadLevelVariableNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitReadVariableNode(ReadVariableNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitFrameSlotNode(FrameSlotNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitReadVarKeywordsNode(ReadVarKeywordsNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitArgumentsNode(ArgumentsNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitReadKeywordNode(ReadKeywordNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitReadVarArgsNode(ReadVarArgsNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitReadDefaultArgumentNode(ReadDefaultArgumentNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitSubscriptStoreSliceNode(SubscriptStoreSliceNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitSliceNode(SliceNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitSubscriptLoadSliceNode(SubscriptLoadSliceNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitGetAttributeNode(GetAttributeNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitPeeledGeneratorLoopNode(PeeledGeneratorLoopNode node) throws Exception {
        return NotSupported(node);
    }

    @Override
    public MGNode visitNoneNode(NoneNode node) throws Exception {
        return NotSupported(node);
    }

}
