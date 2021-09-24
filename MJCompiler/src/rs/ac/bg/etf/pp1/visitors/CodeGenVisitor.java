package rs.ac.bg.etf.pp1.visitors;

import rs.ac.bg.etf.pp1.CodeGen;
import rs.ac.bg.etf.pp1.Compiler;
import rs.ac.bg.etf.pp1.CompilerError;
import rs.ac.bg.etf.pp1.Symbol;
import rs.ac.bg.etf.pp1.SymbolMap;
import rs.ac.bg.etf.pp1.SymbolTable;
import rs.ac.bg.etf.pp1.SymbolType;
import rs.ac.bg.etf.pp1.TokenCode;
import rs.ac.bg.etf.pp1.ast.*;
import rs.ac.bg.etf.pp1.props.*;
import rs.ac.bg.etf.pp1.props.JumpProp.JumpRecord;


public class CodeGenVisitor extends VisitorAdaptor
{
    private Context context = new Context();
    
    private static class Context
    {
        public final BoolProp errorDetected = new BoolProp();

        public final StackProp<SyntaxNode> syntaxNodeStack = new StackProp<>();
    }


    public boolean hasErrors() { return context.errorDetected.get(); }

    // only have a single underscore point at the start of the error tokens
    private void report_basic( SyntaxNode node, String message )
    {
        // FIX: restore the original behaviour when the error reporting prints lines and underlines their errors (instead of just printing errors)
     // report_error( node, message, false, false );
        report_error( node, message, true, false );
    }

    // underscore all error tokens
    private void report_verbose( SyntaxNode node, String message )
    {
        report_error( node, message, true, false );
    }

    // report verbose and throw an exception
    private void report_fatal( SyntaxNode node, String message )
    {
        report_error( node, message, true, true );
    }

    private void report_error( SyntaxNode node, String message, boolean entireScope, boolean throwError )
    {
        context.errorDetected.set();

        ScopeVisitor scopeVisitor = new ScopeVisitor();
        node.accept( scopeVisitor );

        int tokenFromIdx = scopeVisitor.getTokenFromIdx();
        int tokenToIdx = ( entireScope ) ? scopeVisitor.getTokenToIdx() : tokenFromIdx + 1;

        Compiler.errors.add( CompilerError.SEMANTIC_ERROR, message, tokenFromIdx, tokenToIdx );
        if( throwError ) throw Compiler.errors.getLast();
    }





    // ________________________________________________________________________________________________
    // visitor methods

    ////// program ident { }
    ////// program ident { method method method }
    ////// program ident constdl constdl vardl vardl classdl { }
    ////// program ident constdl constdl vardl vardl classdl { method method method }
    // Program ::= (Program_Plain) ProgramType GlobalDeclList lbrace MethodDeclList rbrace;
    @Override
    public void visit( Program_Plain curr )
    {
        // if the symbol table has more or less scopes open than expected
        // +   global -> program
        if( SymbolTable._localsLevel() != 0 )
        {
            report_verbose( curr.getProgramType(), String.format( "Unexpected symbol table scope level: %d (expected 0)", SymbolTable._localsLevel() ) );
        }
        // close the program's scope
        SymbolTable.closeScope();
    }

    ////// program my_program
    // ProgramType ::= (ProgramType_Plain) PROGRAM_K ident:ProgramName;
    @Override
    public void visit( ProgramType_Plain curr )
    {
        // open the program's scope and add the program's symbols to it
        SymbolTable.openScope();
        SymbolTable.addSymbols( curr.symbol._locals() );

        // initialize the predefined methods' addresses and add their code to the code segment
        {
            ////// char chr( int i );
            SymbolTable.findSymbol( "chr" )._address( CodeGen._pc32() );
            CodeGen.i_enter( 1, 1 );
            {
                // load the given int to the expression stack
                CodeGen.i_load_0();
                // calculate the int's remainder with 256
                CodeGen.loadConst( 256 );
                CodeGen.i_rem();
                // voila -- the resulting character is on the expression stack
            }
            CodeGen.i_exit();
            CodeGen.i_return();
            
            ////// int ord( char c );
            SymbolTable.findSymbol( "ord" )._address( CodeGen._pc32() );
            CodeGen.i_enter( 1, 1 );
            {
                // load the zeroth method parameter to the expression stack
                CodeGen.i_load_0();
                // that's it, the character's index is on the expression stack
            }
            CodeGen.i_exit();
            CodeGen.i_return();

            ////// int len( anyType arr[] );
            SymbolTable.findSymbol( "len" )._address( CodeGen._pc32() );
            CodeGen.i_enter( 1, 1 );
            {
                // load the array pointer to the expression stack
                CodeGen.i_load_0();
                // put the array length on the expression stack
                CodeGen.i_arraylength();
                // return
            }
            CodeGen.i_exit();
            CodeGen.i_return();
        }
    }

    ////// <epsilon>
    ////// constdl constdl vardl vardl classdl
    // GlobalDeclList ::= (GlobalDeclList_Tail ) GlobalDeclList GlobalDecl;
    // GlobalDeclList ::= (GlobalDeclList_Empty) ;

    ////// constdl
    ////// vardl
    ////// classdl
    // GlobalDecl ::= (GlobalDecl_Const) ConstDecl;
    // GlobalDecl ::= (GlobalDecl_Var  ) VarDecl;
    // GlobalDecl ::= (GlobalDecl_Class) ClassDecl;



    ////// class A { }
    ////// class A { { method method method } }
    ////// class A extends B { vardl vardl vardl vardl }
    ////// class A extends B { vardl vardl vardl vardl { method method method } }
    // ClassDecl ::= (ClassDecl_Plain) ClassDeclType lbrace ClassDeclBody rbrace;
    @Override
    public void visit( ClassDecl_Plain curr )
    {
        // close the class's scope
        SymbolTable.closeScope();
    }

    ////// class A
    ////// class A extends B
    // ClassDeclType ::= (ClassDeclType_Plain  ) CLASS_K ident:ClassName;
    @Override
    public void visit( ClassDeclType_Plain curr )
    {
        visit_ClassDeclType( curr );
    }
    // ClassDeclType ::= (ClassDeclType_Extends) CLASS_K ident:ClassName EXTENDS_K Type;
    @Override
    public void visit( ClassDeclType_Extends curr )
    {
        visit_ClassDeclType( curr );
    }
    // ClassDeclType ::= (ClassDeclType_Err    ) CLASS_K error {: parser.report_error( "Bad class declaration", null ); :};
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_ClassDeclType( ClassDeclType curr )
    {
        // the class symbol has already been added to the symbol table in the SemanticVisitor
     // SymbolTable.addSymbol( curr.symbol );
        SymbolType classType = curr.symbol._type();

        // open the class scope and add the class's members to the scope
        SymbolTable.openScope();
        SymbolTable.addSymbols( classType._members() );

        // add a dummy 'this' field to the symbol table scope with the index -1
        // +   set its value to be 0 (equal to the null constant)
        Symbol thisSymbol = Symbol.newConst( "this", classType, 0 );
        SymbolTable.addSymbol( thisSymbol );
    }

    ////// <epsilon>
    ////// { method method method }
    ////// vardl vardl vardl vardl
    ////// vardl vardl vardl vardl { method method method }
    // ClassDeclBody ::= (ClassDeclBody_Vars       ) VarDeclList;
    // ClassDeclBody ::= (ClassDeclBody_VarsMethods) VarDeclList lbrace MethodDeclList rbrace;

    ////// <epsilon>
    ////// method method method
    // MethodDeclList ::= (MethodDeclList_Tail ) MethodDeclList MethodDecl;
    // MethodDeclList ::= (MethodDeclList_Empty) ;



    ////// void foo() { }
    ////// void foo() { statement statement }
    ////// void foo() vardl vardl { }
    ////// void foo() vardl vardl { statement statement }
    ////// void foo( int a, char c, Node Array[] ) { }
    ////// void foo( int a, char c, Node Array[] ) { statement statement }
    ////// void foo( int a, char c, Node Array[] ) vardl vardl { }
    ////// void foo( int a, char c, Node Array[] ) vardl vardl { statement statement }
    // MethodDecl ::= (MethodDecl_Plain) MethodDeclType lparen FormPars rparen MethodDeclBody VarDeclList lbrace StatementList rbrace;
    @Override
    public void visit( MethodDecl_Plain curr )
    {
        // close the method's scope
        SymbolTable.closeScope();

        // get the method's symbol
        Symbol method = curr.getMethodDeclType().symbol;

        // if this method is the main method, or the method doesn't return anything
        if( method.isMain() || method._type().isVoidType() )
        {
            // add an exit instruction at the end of the method, followed by a return instruction
            // +    used to gracefully stop the program execution for main
            CodeGen.i_exit(); CodeGen.i_return();
        }
        else
        {
            // add a trap instruction at the end of the method, to catch in runtime! any program paths that don't return a value in the method but should
            // +    weird, but according to the microjava specification
            CodeGen.i_trap( 1 );
        }
    }

    ////// void foo
    ////// A foo
    // MethodDeclType ::= (MethodDeclType_Plain) ReturnType ident:MethodName;
    @Override
    public void visit( MethodDeclType_Plain curr )
    {
        // open the function/method scope and add the function/method's members to the scope
        SymbolTable.openScope();
        SymbolTable.addSymbols( curr.symbol._params() );

        // get the method's symbol
        Symbol method = curr.symbol;
        // set the method's address
        method._address( CodeGen._pc32() );

        // if this method is the main method
        if( method.isMain() )
        {
            // set the program entry point
            CodeGen._mainAddr32Set();
        }
    }

    ////// action symbol for opening a new scope
    // MethodDeclBody ::= (MethodDeclBody_Plain) ;

    ////// action symbol for the beginning of the method code
    // MethodDeclCode ::= (MethodDeclCode_Plain) ;
    @Override
    public void visit( MethodDeclCode_Plain curr )
    {
        // get the method's symbol
        Symbol method = ( ( MethodDecl_Plain )curr.getParent() ).getMethodDeclType().symbol;

        int thisParamInc = ( method.isMethod() ) ? 1 : 0;
        // initialize the method's stack frame
        CodeGen.i_enter( thisParamInc + method._paramCount(), thisParamInc + SymbolTable._localsSize() );

        // if this method is the main method
        if( method.isMain() )
        {
            // close the method scope so that the outer program scope can be accessed
            SymbolMap methodLocals = SymbolTable._locals();
            SymbolTable.closeScope();

            // initialize all class's virtual tables in the main method
            // +   the main method is located in the program scope (global -> program)
            // +   that's why we can filter these symbols for class definitions (classes are defined in the program scope)
            for( Symbol classDef : SymbolTable._locals() )
            {
                // skip any non-class definitions
                if( !classDef.isType() || !classDef._type().isClass() ) continue;

                // initialize the class's virtual table in the static segment
                CodeGen.initVirtualTable( classDef );
            }

            // reopen the method scope and add the symbols that were previously there
            SymbolTable.openScope();
            SymbolTable.addSymbols( methodLocals );
        }
    }

    ////// <epsilon>
    ////// int ident, Node Array[], char c
    // FormPars ::= (FormPars_List ) FormParsScope FormParsList;
    // FormPars ::= (FormPars_Empty) FormParsScope ;

    ////// action symbol for opening a new scope
    // FormParsScope ::= (FormParsScope_Plain) ;

    ////// int a, char c, Node Array[]
    // FormParsList ::= (FormParsList_Init)                    FormParam;
    // FormParsList ::= (FormParsList_Tail) FormParsList comma FormParam;

    ////// int a, char c, Node Array[]
    // FormParam ::= (FormParam_Plain) FormParamType VarIdent;
    // FormParam ::= (FormParam_Err  ) error {: parser.report_error( "Bad formal parameter", null ); :};

    ////// int
    ////// Node
    // FormParamType ::= (FormParamType_Plain) Type;


    ////// <epsilon>
    ////// vardl vardl vardl vardl
    // VarDeclList ::= (VarDeclList_VarDecl) VarDeclList VarDecl;
    // VarDeclList ::= (VarDeclList_Empty  ) ;



    ////// int a, b[], c;
    ////// A a1, a2;
    ////// static int a, b[], c;   // the static keyword is only allowed inside a class declaration!
    ////// static A a1, a2;
    // VarDecl ::= (VarDecl_Plain) VarDeclType VarIdentList semicol;

    ////// int
    ////// static A
    // VarDeclType ::= (VarDeclType_Plain )          Type;
    // VarDeclType ::= (VarDeclType_Static) STATIC_K Type;
    // VarDeclType ::= (VarDeclType_Err   ) error {: parser.report_error( "Bad class declaration", null ); :};

    ////// a
    ////// a, b[], c
    // VarIdentList ::= (VarIdentList_VarIdent)                    VarIdent;
    // VarIdentList ::= (VarIdentList_Tail    ) VarIdentList comma VarIdent;

    ////// a
    ////// b[]
    // VarIdent ::= (VarIdent_Ident) ident:VarName;
    @Override
    public void visit( VarIdent_Ident curr )
    {
        visit_VarIdent( curr, curr.getVarName(), false );
    }
    // VarIdent ::= (VarIdent_Array) ident:VarName lbracket rbracket;
    @Override
    public void visit( VarIdent_Array curr )
    {
        visit_VarIdent( curr, curr.getVarName(), true );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_VarIdent( VarIdent curr, String varName, boolean isArray )
    {
        // if the current symbol is a local variable
        if( curr.symbol.isVar() && !curr.symbol.isGlobal() )
        {
            // add the symbol to the current symbol table scope
            SymbolTable.addSymbol( curr.symbol );
        }
    }
    // VarIdent ::= (VarIdent_Err  ) error {: parser.report_error( "Bad variable declaration", null ); :};



    ////// const int a = 5, b = 6, c = 11;
    // ConstDecl ::= (ConstDecl_Plain) ConstDeclType ConstInitList semicol;

    ////// const int
    // ConstDeclType ::= (ConstDeclType_Plain) CONST_K Type;
    // ConstDeclType ::= (ConstDeclType_Err  ) CONST_K error {: parser.report_error( "Bad constant type", null ); :};

    ////// a = 5, b = 6, c = 11
    // ConstInitList ::= (ConstInitList_Init)                     ConstInit;
    // ConstInitList ::= (ConstInitList_Tail) ConstInitList comma ConstInit;

    ////// a = 5
    // ConstInit ::= (ConstInit_Plain) ident:IdentName Assignop Literal;
    // ConstInit ::= (ConstInit_Err  ) error {: parser.report_error( "Bad initialization", null ); :};






    ////// ident.ident[ expr ] = expr;
    ////// ident.ident[ expr ]( );
    ////// ident.ident[ expr ]( expr, expr, expr );
    ////// ident.ident[ expr ]++;
    ////// ident.ident[ expr ]--;
    //
    ////// if( condition ) statement
    ////// if( condition ) statement else statement
    ////// do statement while( condition );
    ////// switch( expr ) { }
    ////// switch( expr ) { case 1: statement statement statement   case 2: statement statement }
    ////// break;
    ////// continue;
    ////// return;
    ////// return expr;
    //
    ////// read( ident.ident[ expr ] );
    ////// print( ident.ident[ expr ], 2 );
    //
    ////// {}
    ////// { statement statement statement }
    // Statement ::= (Statement_Designator ) DesignatorStatement semicol;
    // Statement ::= (Statement_If         ) IF_K lparen IfCondition rparen IfStatement;
    @Override
    public void visit( Statement_If curr )
    {
        int pointA = curr.getIfCondition().integer;
        int pointC = curr.getIfStatement().integer;

        // set the if-condition's jump to point after the if-statement
        CodeGen.fixJumpOffset( pointA, pointC );
        // remove the unconditional jump instruction from the code (by restoring the pc's value before it was added)
        CodeGen._pc32( pointC );
    }
    // Statement ::= (Statement_IfElse     ) IF_K lparen IfCondition rparen IfStatement ElseScope ElseStatement;
    @Override
    public void visit( Statement_IfElse curr )
    {
        int pointA = curr.getIfCondition().integer;
        int pointB = curr.getIfStatement().integer;
        int pointC = curr.getElseScope().integer;
        int pointD = curr.getElseStatement().integer;

        // set the if-condition's jump to point to the else-statement
        CodeGen.fixJumpOffset( pointA, pointC );
        // set the if-condition's unconditional jump to point after the else-statement
        CodeGen.fixJumpOffset( pointB, pointD );
    }
    // Statement ::= (Statement_DoWhile    ) DoWhileScope Statement WHILE_K lparen DoWhileCondition rparen semicol;
    @Override
    public void visit( Statement_DoWhile curr )
    {
        context.syntaxNodeStack.remove();
    }
    // Statement ::= (Statement_Switch     ) SWITCH_K lparen SwitchExpr rparen lbrace CaseList rbrace;
    @Override
    public void visit( Statement_Switch curr )
    {
        context.syntaxNodeStack.remove();
        SwitchExpr scope = curr.getSwitchExpr();

        // set the switch-statement's exit point here
        // +    the entry! point has been set in the SwitchExpr
        scope.jumpprop.get( "exit" )._pointAddress( CodeGen._pc32() );
    }
    // Statement ::= (Statement_Break      ) BREAK_K       semicol;
    @Override
    public void visit( Statement_Break curr )
    {
        // find the surrounding do-while or switch statement
        SyntaxNode scope = context.syntaxNodeStack.find(
            elem -> ( elem instanceof DoWhileScope_Plain
                   || elem instanceof SwitchExpr_Plain )
        );

        JumpProp jumpMap = null;
        if     ( scope instanceof DoWhileScope     ) { jumpMap = ( ( DoWhileScope     )scope ).jumpprop; }
        else if( scope instanceof SwitchExpr_Plain ) { jumpMap = ( ( SwitchExpr_Plain )scope ).jumpprop; }
        
        // unconditionally jump to the end of the do-while or switch statement
        // +    get the jump-instruction's starting address
        int pointA = CodeGen.jump( CodeGen.NO_ADDRESS );

        // mark the jump instruction's offset to be fixed later
        jumpMap.get( "exit" )._addAddressToFix( pointA );
    }
    // Statement ::= (Statement_Continue   ) CONTINUE_K    semicol;
    @Override
    public void visit( Statement_Continue curr )
    {
        // find the surrounding do-while statement
        DoWhileScope_Plain scope = ( DoWhileScope_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof DoWhileScope_Plain )
        );

        // unconditionally jump to the beginning of the do-while-statement's condition
        // +    get the jump-instruction's starting address
        int pointA = CodeGen.jump( CodeGen.NO_ADDRESS );

        // mark the jump instruction's offset to be fixed later
        scope.jumpprop.get( "condition" )._addAddressToFix( pointA );
    }
    // Statement ::= (Statement_Return     ) RETURN_K      semicol;
    @Override
    public void visit( Statement_Return curr )
    {
        CodeGen.i_exit();
        CodeGen.i_return();
    }
    // Statement ::= (Statement_ReturnExpr ) RETURN_K Expr semicol;
    @Override
    public void visit( Statement_ReturnExpr curr )
    {
        CodeGen.i_exit();
        CodeGen.i_return();
    }
    // Statement ::= (Statement_Read       ) READ_K lparen Designator rparen semicol;
    @Override
    public void visit( Statement_Read curr )
    {
        // read the value from the standard input
        CodeGen.i_read();
        // store the read value in the symbol
        CodeGen.storeSymbolValue( curr.getDesignator().symbol );
    }
    // Statement ::= (Statement_Print      ) PRINT_K lparen Expr                        rparen semicol;
    @Override
    public void visit( Statement_Print curr )
    {
        CodeGen.i_const_0();
        CodeGen.print( curr.getExpr().symbol._type().isChar() );
    }
    // Statement ::= (Statement_PrintFormat) PRINT_K lparen Expr comma int_lit:MinWidth rparen semicol;
    @Override
    public void visit( Statement_PrintFormat curr )
    {
        CodeGen.loadConst( curr.getMinWidth() );
        CodeGen.print( curr.getExpr().symbol._type().isChar() );
    }
    // Statement ::= (Statement_Scope      ) lbrace StatementList rbrace;
    // Statement ::= (Statement_Semicolon  ) semicol;
    // Statement ::= (Statement_Err        ) error {: parser.report_error( "Bad statement", null ); :};

    ////// action symbols for opening a new scope and the if-statement's jump instructions
    // IfCondition ::= (IfCondition_Plain) Condition;
    @Override
    public void visit( IfCondition_Plain curr )
    {
        // add the 'true' constant's value (1) to the expression stack
        CodeGen.loadConst( CodeGen.TRUE );
        // initialize the jump instruction's address
        // +    jump if the condition is not true
        curr.integer = CodeGen.jumpIfNot( TokenCode.eq, CodeGen.NO_ADDRESS );
    }
    // IfStatement ::= (IfStatement_Plain) Statement;
    @Override
    public void visit( IfStatement_Plain curr )
    {
        // initialize the jump instruction's address
        // +    unconditionally jump over the entire else-statement
        curr.integer = CodeGen.jump( CodeGen.NO_ADDRESS );
    }
    // ElseScope ::= (ElseScope_Plain) ELSE_K;
    @Override
    public void visit( ElseScope_Plain curr )
    {
        // get the address of the first instruction after the if-statement
        curr.integer = CodeGen._pc32();
    }
    // ElseStatement ::= (ElseStatement_Plain) Statement;
    @Override
    public void visit( ElseStatement_Plain curr )
    {
        // get the address of the first instruction after the entire if-else-statement
        curr.integer = CodeGen._pc32();
    }

    ////// action symbols for opening a new scope and the do-while-statement's jump instructions
    // DoWhileScope ::= (DoWhileScope_Plain) DO_K;
    @Override
    public void visit( DoWhileScope_Plain curr )
    {
        context.syntaxNodeStack.add( curr );

        // initialize the jump map
        curr.jumpprop.add( "enter" );
        curr.jumpprop.add( "condition" );
        curr.jumpprop.add( "exit" );

        // get the address of the first instruction in the do-while-statement
        curr.jumpprop.get( "enter" )._pointAddress( CodeGen._pc32() );
    }
    // DoWhileCondition ::= (DoWhileCondition_Plain) DoWhileConditionScope Condition;
    @Override
    public void visit( DoWhileCondition_Plain curr )
    {
        // find the surrounding do-while statement
        DoWhileScope_Plain scope = ( DoWhileScope_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof DoWhileScope_Plain )
        );

        // add the 'true' constant's value (1) to the expression stack
        CodeGen.loadConst( CodeGen.TRUE );
        // initialize the jump instruction's address
        // +    jump to the beginning of the loop if the condition is true
        curr.integer = CodeGen.jumpIf( TokenCode.eq, CodeGen.NO_ADDRESS );
        scope.jumpprop.get( "enter" )._addAddressToFix( curr.integer );

        // set the do-while-statement's exit point here
        // +    the entry! point has been set in the DoWhileScope
        scope.jumpprop.get( "exit" )._pointAddress( CodeGen._pc32() );
    }
    // DoWhileConditionScope ::= (DoWhileConditionScope_Plain) ;
    @Override
    public void visit( DoWhileConditionScope_Plain curr )
    {
        // find the surrounding do-while statement
        DoWhileScope_Plain scope = ( DoWhileScope_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof DoWhileScope_Plain )
        );

        // set the do-while-condition's entry point
        curr.integer = CodeGen._pc32();
        scope.jumpprop.get( "condition" )._pointAddress( curr.integer );
    }

    ////// action symbols for opening a new scope and the switch-statement's jump instructions
    // SwitchExpr ::= (SwitchExpr_Plain) Expr;
    @Override
    public void visit( SwitchExpr_Plain curr )
    {
        context.syntaxNodeStack.add( curr );

        // for all the switch's cases in the order they appeared (default not yet supported)
        for( JumpRecord caseRecord : curr.jumpprop )
        {
            // duplicate the switch-expression's result, so that it is there for other conditional jumps as well
            // +    (the expression stack's top duplicate gets consumed whenever the conditional jump's condition is evaluated)
            CodeGen.i_dup();

            // add the case statement's value to the expression stack
            CodeGen.loadConst( Integer.parseInt( caseRecord._pointName() ) );

            // initialize the jump instruction's address
            // +    jump if the condition is not true
            int pointCaseX = CodeGen.jumpIf( TokenCode.eq, CodeGen.NO_ADDRESS );
            curr.jumpprop.get( caseRecord._pointName() )._addAddressToFix( pointCaseX );
        }

        // remove the last duplicate, since there are no more cases left (default not yet supported)
        CodeGen.i_epop();
        // jump unconditionally to the first instruction after the switch-statement
        int pointSkipAllCases = CodeGen.jump( CodeGen.NO_ADDRESS );

        // add the switch's exit point
        curr.jumpprop.add( "exit" );
        curr.jumpprop.get( "exit" )._addAddressToFix( pointSkipAllCases );
    }

    ////// ident.ident[ expr ] = expr
    ////// ident.ident[ expr ]( )
    ////// ident.ident[ expr ]( expr, expr, expr )
    ////// ident.ident[ expr ]++
    ////// ident.ident[ expr ]--
    // DesignatorStatement ::= (DesignatorStatement_Assign    ) Designator Assignop Expr;
    @Override
    public void visit( DesignatorStatement_Assign curr )
    {
        visit_UpdateDesignatorValue( curr, curr.getDesignator().symbol );
    }
    // DesignatorStatement ::= (DesignatorStatement_Call      ) MethodCall lparen ActPars rparen;
    @Override
    public void visit( DesignatorStatement_Call curr )
    {
        visit_MethodOrFunctionCall( curr, curr.getMethodCall().symbol, false );
    }
    // DesignatorStatement ::= (DesignatorStatement_Plusplus  ) Designator plusplus;
    @Override
    public void visit( DesignatorStatement_Plusplus curr )
    {
        visit_UpdateDesignatorValue( curr, curr.getDesignator().symbol );
    }
    // DesignatorStatement ::= (DesignatorStatement_Minusminus) Designator minusminus;
    @Override
    public void visit( DesignatorStatement_Minusminus curr )
    {
        visit_UpdateDesignatorValue( curr, curr.getDesignator().symbol );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_UpdateDesignatorValue( DesignatorStatement curr, Symbol designator )
    {
        if( curr instanceof DesignatorStatement_Assign )
        {
            // store the value of the expression to the symbol
            // +   the symbol's address is present on the expression stack (if needed), followed by the expression value
            CodeGen.storeSymbolValue( designator );
            return;
        }

        // if the symbol needs designation by another symbol (whose value must be placed onto the expression stack in order for this symbol to be accessed)
        if( CodeGen.needsPrevDesignatorValue( designator ) )
        {
            // duplicate that previous value (so that we don't lose it)
            CodeGen.i_dup();
        }

        // load the designator's value to the expression stack
        // +    this uses up one of the duplicate designator's addresses
        // +    (if they are present on the expression stack, that means that they were needed to load and store the designator's value)
        CodeGen.loadSymbolValue( designator );

        // load the constant 1 to the expression stack and add/sub it from the designator
        CodeGen.i_const_1();
        if     ( curr instanceof DesignatorStatement_Plusplus   ) CodeGen.i_add();
        else if( curr instanceof DesignatorStatement_Minusminus ) CodeGen.i_sub();

        // store the updated value back to the designator
        CodeGen.storeSymbolValue( designator );
    }

    ////// <epsilon>
    ////// statement statement statement statement
    // StatementList ::= (StatementList_Tail ) StatementList Statement;
    // StatementList ::= (StatementList_Empty) ;

    ////// <epsilon>
    ////// case 1: statement statement statement   case 2: statement statement
    // CaseList ::= (CaseList_Tail ) CaseList Case;
    // CaseList ::= (CaseList_Empty) ;

    ////// case 1: statement statement statement
    ////// case 2: 
    ////// case 3: {}
    // Case ::= (Case_Plain) CaseScope StatementList;
    @Override
    public void visit( Case_Plain curr )
    {
        // add a random constant on the stack, which will be removed by the next case
        // +   this supports cases that fall through to the next case (there is a code path that doesn't hit a break/continue/return statement in the case)
        // +   the next case will remove this constant at the beginning (as if it were a switch-expression's value duplicate)
        CodeGen.i_const_0();
    }

    ////// action symbols for opening a new scope and the case-statement's jump instructions
    // CaseScope ::= (CaseScope_Plain) CASE_K int_lit:CaseNum colon;
    @Override
    public void visit( CaseScope_Plain curr )
    {
        // find the switch scope surrounding this symbol
        SwitchExpr_Plain scope = ( SwitchExpr_Plain )context.syntaxNodeStack.find(
            elem -> ( elem instanceof SwitchExpr_Plain )
        );

        // remove the last switch-expression's result (last duplicate)
        int pointCase = CodeGen.i_epop();

        // update the case's starting address
        scope.jumpprop.get( curr.getCaseNum().toString() )._pointAddress( pointCase );
    }



    ////// <epsilon>
    ////// expr
    ////// expr, expr, expr
    // ActPars ::= (ActPars_Plain) ActParsScope ActParsList;
    // ActPars ::= (ActPars_Empty) ActParsScope;

    ////// action symbol for opening a new scope
    // ActParsScope ::= (ActParsScope_Plain) ;


    ////// expr
    ////// expr, expr, expr
    // ActParsList ::= (ActParsList_Expr)                   ActParam;
    // ActParsList ::= (ActParsList_Tail) ActParsList comma ActParam;

    ////// expr
    // ActParam ::= (ActParam_Plain) Expr;
    @Override
    public void visit( ActParam_Plain curr )
    {
        // HACK: swap the expression's value and the class instance pointer on the expression stack
        // +    that way, the class instance pointer always ends up at the top of the stack (similar to how bubble sort works)
        CodeGen.i_dup_x1();   // ExprStack=[...,valA, valB -> ...,valB, valA, valB]
        CodeGen.i_epop();     // ExprStack=[..., val -> ...]
    }



    ////// expr   or   expr < expr and expr >= expr  or  expr != expr   // 'and' has greater priority than 'or'!
    // Condition ::= (Condition_Term)              CondTerm;
    // Condition ::= (Condition_Or  ) Condition or CondTerm;
    @Override
    public void visit( Condition_Or curr )
    {
        // HACK: if any of the inputs is one (true), their addition + one, when int divided by two ( (a+b+1)/2 ) result in a one (true)
        CodeGen.i_add();
        CodeGen.i_const_1();
        CodeGen.i_add();
        CodeGen.i_const_2();
        CodeGen.i_div();
    }

    ////// expr < expr and expr >= expr
    // CondTerm ::= (CondTerm_Fact)              CondFact;
    // CondTerm ::= (CondTerm_And ) CondTerm and CondFact;
    @Override
    public void visit( CondTerm_And curr )
    {
        // HACK: only if both inputs are one (true) their multiplication results in a one (true)
        CodeGen.i_mul();
    }

    ////// expr < expr and expr >= expr
    // CondFact ::= (CondFact_Expr ) Expr;
    // CondFact ::= (CondFact_Relop) Expr Relop Expr;
    @Override
    public void visit( CondFact_Relop curr )
    {
        int pointA = CodeGen.jumpIf( curr.getRelop().symbol._value(), CodeGen.NO_ADDRESS );   // jump to C
                     CodeGen.loadConst( CodeGen.FALSE );
        int pointB = CodeGen.jump( CodeGen.NO_ADDRESS );   // jump to D
        int pointC = CodeGen.loadConst( CodeGen.TRUE );
        
        int pointD = CodeGen._pc32();
                     
        // fix the jump addresses for the jump instructions
        CodeGen.fixJumpOffset( pointA, pointC );
        CodeGen.fixJumpOffset( pointB, pointD );
    }



    ////// +term - term + term + term
    // Expr ::= (Expr_Addition) Addition;
    // Expr ::= (Expr_Err     ) error {: parser.report_error( "Bad expression", null ); :};

    ////// term
    ////// +term
    ////// -term
    ////// term + term - term + term
    ////// -term + term - term + term
    ////// +term + term + term + term
    // Addition ::= (Addition_Term )                Term;
    // Addition ::= (Addition_STerm)          Addop Term;
    @Override
    public void visit( Addition_STerm curr )
    {
        if( curr.getAddop().symbol._value() == TokenCode.minus ) CodeGen.i_neg();
    }
    // Addition ::= (Addition_Tail ) Addition Addop Term;
    @Override
    public void visit( Addition_Tail curr )
    {
        switch( curr.getAddop().symbol._value() )
        {
            case TokenCode.plus:  CodeGen.i_add(); break;
            case TokenCode.minus: CodeGen.i_sub(); break;
            default: report_fatal( curr, "Unsupported addition operator code" );
        }
    }

    ////// factor
    ////// factor*factor*factor
    // Term ::= (Term_Factor)            Factor;
    // Term ::= (Term_Tail  ) Term Mulop Factor;
    @Override
    public void visit( Term_Tail curr )
    {
        switch( curr.getMulop().symbol._value() )
        {
            case TokenCode.mul:  CodeGen.i_mul(); break;
            case TokenCode.div:  CodeGen.i_div(); break;
            case TokenCode.perc: CodeGen.i_rem(); break;
            default: report_fatal( curr, "Unsupported multiplication operator code" );
        }
    }



    ////// ident.ident[ expr ]
    ////// ident.ident[ expr ]( )
    ////// ident.ident[ expr ]( expr, expr, expr )
    ////// 1202 | 'c' | true
    ////// new Object
    ////// new Array[ expr ]
    ////// ( expr )
    // Factor ::= (Factor_Designator ) Designator;
    @Override
    public void visit( Factor_Designator curr )
    {
        // load the designator's value on the expression stack (given the previous designator where the designator evalueation stopped)
        // +   IMPORTANT: this loads the null constant's value
        CodeGen.loadSymbolValue( curr.symbol );
    }
    // Factor ::= (Factor_MethodCall ) MethodCall lparen ActPars rparen;
    @Override
    public void visit( Factor_MethodCall curr )
    {
        visit_MethodOrFunctionCall( curr, curr.getMethodCall().symbol, true );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_MethodOrFunctionCall( SyntaxNode curr, Symbol function, boolean saveReturnValueIfItExists )
    {
        // if the function is a method (class member)
        if( function.isMethod() )
        {
            // push the virtual table pointer to the expression stack (#0 field in the class)
            CodeGen.i_getfield( 0 );
            // call the virtual method
            CodeGen.i_invokevirtual( function._name() );
        }
        // if the function is a function (in the global scope)
        else if( function.isFunction() )
        {
            // remove the random constant (quasi class instance pointer) from the expression stack
            CodeGen.i_epop();
            // call the method starting at the given address
            int pointA = CodeGen.i_call( CodeGen.NO_ADDRESS );
            CodeGen.fixJumpOffset( pointA, function._address() );
        }

        // if the return value should not be saved and the function/method returns something, remove the result from the expression stack
        if( !saveReturnValueIfItExists && !function._type().isVoidType() ) CodeGen.i_epop();
    }
    // Factor ::= (Factor_Literal    ) Literal;
    @Override
    public void visit( Factor_Literal curr )
    {
        // load the literal's value onto the expression stack
        CodeGen.loadConst( curr.symbol._value() );
    }
    // Factor ::= (Factor_NewVar     ) NEW_K Type;
    @Override
    public void visit( Factor_NewVar curr )
    {
        // allocate space on the heap for the class instance and add the starting address to the expression stack
        CodeGen.i_new( curr.symbol._type()._fieldCount()*4/*B*/ );
        // initialize the virtual table pointer, but leave the class instance's address on the expression stack
        CodeGen.i_dup();
        CodeGen.loadConst( curr.symbol._address() );
        CodeGen.i_putfield( 0 );
    }
    // Factor ::= (Factor_NewArray   ) NEW_K Type lbracket Expr rbracket;
    @Override
    public void visit( Factor_NewArray curr )
    {
        // allocate space on the heap for the array and add the array starting address to the expression stack
        CodeGen.i_newarray( curr.symbol._type().isChar() );
    }
    // Factor ::= (Factor_Expr       ) lparen Expr rparen;

    ////// ident.ident[ expr ]( expr, expr, expr )
    // MethodCall ::= (MethodCall_Plain) Designator;
    @Override
    public void visit( MethodCall_Plain curr )
    {
        // if the current symbol is a method
        if( curr.symbol.isMethod() )
        {
            // duplicate the method address on the expression stack (one address will be used to get the virtual table pointer)
            CodeGen.i_dup();
        }
        // if the current symbol is a function
        else if( curr.symbol.isFunction() )
        {
            // load a random constant on the expression stack (quasi class instace pointer)
            // so that both the method's and the function's activation parameters are handled in the same way
            CodeGen.i_const_0();
        }
    }

    ////// null
    ////// ident
    ////// ident.ident
    ////// ident[ expr ]
    ////// ident.ident.ident[ expr ].ident
    ////// ident.ident.ident[ expr ].ident[ expr ]
    // Designator ::= (Designator_Ident  ) ident:Name;
    @Override
    public void visit( Designator_Ident curr )
    {
        visit_Designator( curr );
    }
    // Designator ::= (Designator_Null   ) NULL_K;
    @Override
    public void visit( Designator_Null curr )
    {
        visit_Designator( curr );
    }
    // Designator ::= (Designator_Field  ) Designator dot ident:Name;
    @Override
    public void visit( Designator_Field curr )
    {
        visit_Designator( curr );
    }
    // Designator ::= (Designator_ArrElem) Designator lbracket Expr rbracket;
    @Override
    public void visit( Designator_ArrElem curr )
    {
        visit_Designator( curr );
    }
    // IMPORTANT: helper method, not intended to be used elsewhere
    private void visit_Designator( Designator curr )
    {
        // if the current designator is a class member, but does not start with 'this' (this.field)
        if( curr instanceof Designator_Ident && curr.symbol.isClassMember() )
        {
            CodeGen.loadSymbolValue( SymbolTable.findSymbol( "this" ) );
        }

        // if the designator is not the last one in the sequence
        boolean hasNext = curr.getParent() instanceof Designator;
        if( hasNext )
        {
            // load its value on the expression stack
            CodeGen.loadSymbolValue( curr.symbol );
        }
    }





    ////// void | type
    // ReturnType ::= (ReturnType_Void ) VOID_K:ReturnType;
    // ReturnType ::= (ReturnType_Ident) ident :ReturnType;

    ////// int | bool | char | ident
    // Type ::= (Type_Ident) ident:Type;

    ////// 1202 | 'c' | true
    // Literal ::= (Literal_Int ) int_lit :Literal;
    // Literal ::= (Literal_Char) char_lit:Literal;
    // Literal ::= (Literal_Bool) bool_lit:Literal;

    ////// =
    // Assignop ::= (Assignop_Assign) assign:Assignop;

    ////// ==  |  !=  |  >  |  >=  |  <  |  <=
    // Relop ::= (Relop_Eq) eq:Relop;
    // Relop ::= (Relop_Ne) ne:Relop;
    // Relop ::= (Relop_Gt) gt:Relop;
    // Relop ::= (Relop_Ge) ge:Relop;
    // Relop ::= (Relop_Lt) lt:Relop;
    // Relop ::= (Relop_Le) le:Relop;

    ////// +  |  -
    // Addop ::= (Addop_Plus ) plus :Addop;
    // Addop ::= (Addop_Minus) minus:Addop;

    ////// *  |  /  |  %
    // Mulop ::= (Mulop_Mul ) mul :Mulop;
    // Mulop ::= (Mulop_Div ) div :Mulop;
    // Mulop ::= (Mulop_Perc) perc:Mulop;

}
