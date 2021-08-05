// generated with ast extension for cup
// version 0.8
// 5/7/2021 17:50:47


package rs.ac.bg.etf.pp1.ast;

public interface Visitor { 

    public void visit(ReturnType ReturnType);
    public void visit(Mulop Mulop);
    public void visit(VarIdent VarIdent);
    public void visit(Literal Literal);
    public void visit(FormParsNext FormParsNext);
    public void visit(Relop Relop);
    public void visit(Assignop Assignop);
    public void visit(IdentInitList IdentInitList);
    public void visit(ActParsNext ActParsNext);
    public void visit(StatementList StatementList);
    public void visit(ClassDeclScope ClassDeclScope);
    public void visit(Addop Addop);
    public void visit(Addition Addition);
    public void visit(Factor Factor);
    public void visit(CondTerm CondTerm);
    public void visit(DesignatorNext DesignatorNext);
    public void visit(Term Term);
    public void visit(Condition Condition);
    public void visit(CaseList CaseList);
    public void visit(SignedAddition SignedAddition);
    public void visit(GlobalDeclList GlobalDeclList);
    public void visit(VarDeclList VarDeclList);
    public void visit(Expr Expr);
    public void visit(ActPars ActPars);
    public void visit(DesignatorStatement DesignatorStatement);
    public void visit(Statement Statement);
    public void visit(VarIdentList VarIdentList);
    public void visit(ClassDecl ClassDecl);
    public void visit(CondFact CondFact);
    public void visit(MethodDeclList MethodDeclList);
    public void visit(GlobalDecl GlobalDecl);
    public void visit(FormPars FormPars);
    public void visit(MulopPerc MulopPerc);
    public void visit(MulopDiv MulopDiv);
    public void visit(MulopMul MulopMul);
    public void visit(AddopMinus AddopMinus);
    public void visit(AddopPlus AddopPlus);
    public void visit(RelopLeq RelopLeq);
    public void visit(RelopLt RelopLt);
    public void visit(RelopGeq RelopGeq);
    public void visit(RelopGt RelopGt);
    public void visit(RelopNeq RelopNeq);
    public void visit(RelopEq RelopEq);
    public void visit(AssignopAssign AssignopAssign);
    public void visit(Type Type);
    public void visit(ReturnTypeIdent ReturnTypeIdent);
    public void visit(ReturnTypeVoid ReturnTypeVoid);
    public void visit(LiteralBool LiteralBool);
    public void visit(LiteralChar LiteralChar);
    public void visit(LiteralInt LiteralInt);
    public void visit(VarIdentArray VarIdentArray);
    public void visit(VarIdentIdent VarIdentIdent);
    public void visit(DesignatorNextEmpty DesignatorNextEmpty);
    public void visit(DesignatorNextElemTail DesignatorNextElemTail);
    public void visit(DesignatorNextFieldTail DesignatorNextFieldTail);
    public void visit(Designator Designator);
    public void visit(FactorExpr FactorExpr);
    public void visit(FactorNewArray FactorNewArray);
    public void visit(FactorNewVar FactorNewVar);
    public void visit(FactorLiteral FactorLiteral);
    public void visit(FactorDesignatorCall FactorDesignatorCall);
    public void visit(FactorDesignator FactorDesignator);
    public void visit(TermTail TermTail);
    public void visit(TermFactor TermFactor);
    public void visit(SignedAdditionTail SignedAdditionTail);
    public void visit(SignedAdditionTerm SignedAdditionTerm);
    public void visit(AdditionTermTail AdditionTermTail);
    public void visit(AdditionTail AdditionTail);
    public void visit(AdditionTerm AdditionTerm);
    public void visit(ExprTernary ExprTernary);
    public void visit(ExprAddition ExprAddition);
    public void visit(CondFactTail CondFactTail);
    public void visit(CondFactExpr CondFactExpr);
    public void visit(CondTermTail CondTermTail);
    public void visit(CondTermFact CondTermFact);
    public void visit(ConditionTail ConditionTail);
    public void visit(ConditionTerm ConditionTerm);
    public void visit(ActParsNextEmpty ActParsNextEmpty);
    public void visit(ActParsNextTail ActParsNextTail);
    public void visit(ActParsEmpty ActParsEmpty);
    public void visit(ActParsTail ActParsTail);
    public void visit(CaseListEmpty CaseListEmpty);
    public void visit(CaseListTail CaseListTail);
    public void visit(StatementListEmpty StatementListEmpty);
    public void visit(StatementListTail StatementListTail);
    public void visit(DesignatorStatementMinusminus DesignatorStatementMinusminus);
    public void visit(DesignatorStatementPlusplus DesignatorStatementPlusplus);
    public void visit(DesignatorStatementCall DesignatorStatementCall);
    public void visit(DesignatorStatementAssign DesignatorStatementAssign);
    public void visit(StatementScope StatementScope);
    public void visit(StatementPrintFormat StatementPrintFormat);
    public void visit(StatementPrint StatementPrint);
    public void visit(StatementRead StatementRead);
    public void visit(StatementReturnExpr StatementReturnExpr);
    public void visit(StatementReturn StatementReturn);
    public void visit(StatementContinue StatementContinue);
    public void visit(StatementBreak StatementBreak);
    public void visit(StatementSwitch StatementSwitch);
    public void visit(StatementDoWhile StatementDoWhile);
    public void visit(StatementIfElse StatementIfElse);
    public void visit(StatementIf StatementIf);
    public void visit(StatementDesignator StatementDesignator);
    public void visit(IdentInitListTail IdentInitListTail);
    public void visit(IdentInitListInit IdentInitListInit);
    public void visit(ConstDecl ConstDecl);
    public void visit(VarIdentListTail VarIdentListTail);
    public void visit(VarIdentListVarIdent VarIdentListVarIdent);
    public void visit(VarDecl VarDecl);
    public void visit(VarDeclListEmpty VarDeclListEmpty);
    public void visit(VarDeclListVarDecl VarDeclListVarDecl);
    public void visit(FormParsNextEmpty FormParsNextEmpty);
    public void visit(FormParsNextFormPar FormParsNextFormPar);
    public void visit(FormParsEmpty FormParsEmpty);
    public void visit(FormParsTail FormParsTail);
    public void visit(MethodDecl MethodDecl);
    public void visit(MethodDeclListEmpty MethodDeclListEmpty);
    public void visit(MethodDeclListTail MethodDeclListTail);
    public void visit(MethodDeclScope MethodDeclScope);
    public void visit(ClassDeclScopeVarsMethods ClassDeclScopeVarsMethods);
    public void visit(ClassDeclScopeVars ClassDeclScopeVars);
    public void visit(ClassDeclExtends ClassDeclExtends);
    public void visit(ClassDeclPlain ClassDeclPlain);
    public void visit(GlobalDeclClass GlobalDeclClass);
    public void visit(GlobalDeclVar GlobalDeclVar);
    public void visit(GlobalDeclConst GlobalDeclConst);
    public void visit(GlobalDeclListEmpty GlobalDeclListEmpty);
    public void visit(GlobalDeclListTail GlobalDeclListTail);
    public void visit(Program Program);

}
