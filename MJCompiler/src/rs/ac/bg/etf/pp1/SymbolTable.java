package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

// import java.io.ByteArrayOutputStream;
// import java.io.IOException;
// import java.util.Stack;

import rs.ac.bg.etf.pp1.util.ScopeGuard;
// import rs.ac.bg.etf.pp1.util.SystemStreamReplacer;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;


// FIXME: make a symbol table visitor
public class SymbolTable
{
    // the number of predefined symbols in the program
    private static final int predefSymbolCnt = 11;
    // IMPORTANT: don't use the Tab's types and symbols! (for compatibility with Symbol and SymbolType classes)
    // +   actually, don't use the Tab class at all (it's deprecated), use the SymbolTable class instead
    // IMPORTANT: initialize <any type> first, since it's the root type for all other types, and its supertype is itself
    // +   also, most of these symbols are special cases, intended to be used sparingly
    // +   anyType is not intended to be used as Object (like in Java), since it is equivalent to all types (its used for preventing too many semantic errors from being reported)
    // +   voidType is used when no type is expected (void functions)
    // +   nullType is used when a null keyword is found
    public static final SymbolType anyType  = SymbolType.newPrimitive( "@any", SymbolType.ANY_TYPE );
    public static final SymbolType voidType = SymbolType.newPrimitive( "void", SymbolType.NO_TYPE  );
    public static final SymbolType nullType = SymbolType.newPrimitive( "null", SymbolType.CLASS    );
    public static final SymbolType intType  = SymbolType.newPrimitive( "int",  SymbolType.INT      );
    public static final SymbolType charType = SymbolType.newPrimitive( "char", SymbolType.CHAR     );
    public static final SymbolType boolType = SymbolType.newPrimitive( "bool", SymbolType.BOOL     );
    
    // IMPORTANT: <no symbol> is returned when the symbol table cannot find a symbol
    // +   other symbols are just there for allowing their symbol types to be saved in the symbol table
    public static final Symbol noSym   = Symbol.newType( "@noSym", anyType  );
    public static final Symbol anySym  = Symbol.newType( "@any",   anyType  );
    public static final Symbol voidSym = Symbol.newType( "void",   voidType );
    public static final Symbol nullSym = Symbol.newType( "null",   nullType );
    public static final Symbol intSym  = Symbol.newType( "int",    intType  );
    public static final Symbol charSym = Symbol.newType( "char",   charType );
    public static final Symbol boolSym = Symbol.newType( "bool",   boolType );

    // this value is copied over from the Tab.init() method
    private static int currScopeLevel = -1;
    private static Scope global = null;
    // saves all the scopes that were ever created in preorder
    // +   used for printing the symbol table
    private static ArrayList<ScopeInfo> scopeList = new ArrayList<>();
    private static class ScopeInfo
    {
        public ScopeInfo( Scope scope, int level )
        {
            this.scope = scope;
            this.level = level;
        }
        private Scope scope;
        private int level;

        public Scope _scope() { return scope; }
        public int _level() { return level; }
        public SymbolMap _symbols() { return new SymbolMap( scope.getLocals() ); }
    }

    static
    {
        init();
    }

    private static void init()
    {
        // initialize the universal scope (the first scope in the symbol table)
        // +   that way, chrObj, ordObj and lenObj can be final
        // +   defined symbols:
        //     +   types [obj]:     int, char
        //     +   constants [obj]: eol, null
        //     +   methods [obj]:   chr( i ), ord( ch ), len( arr )
        Tab.init();
        // throw away what was initialized, but keep the scope level as -2
        Tab.closeScope();
        // initialize the global scope (-1st scope)
        global = openScope();

        // add the global types to the global scope
        global.addToLocals( noSym   );
        global.addToLocals( anySym  );
        global.addToLocals( voidSym );
        global.addToLocals( nullSym );
        global.addToLocals( intSym  );
        global.addToLocals( charSym );
        global.addToLocals( boolSym );
        
        // char chr( int i );
        try( ScopeGuard guard = new ScopeGuard(); )
        {
            _local().addToLocals( Symbol.newFormalParam( "i", intType, 0, _localsLevel() ) );
            
            // IMPORTANT: set the method's formal parameters after all locals have been added to the current scope!
            // +   the method's formal parameters aren't automatically updated due to the way the _params function is implemented)
            global.addToLocals( Symbol.newFunction( "chr", charType, Symbol.NO_VALUE, _locals() ) );
        }

        // int ord( char c );
        try( ScopeGuard guard = new ScopeGuard(); )
        {
            _local().addToLocals( Symbol.newFormalParam( "c", charType, 0, _localsLevel() ) );
            global.addToLocals( Symbol.newFunction( "ord", intType, Symbol.NO_VALUE, _locals() ) );
        }

        // int len( anyType arr[] );
        try( ScopeGuard guard = new ScopeGuard(); )
        {
            SymbolType anyArrayType = SymbolType.newArray( "@anyArray", anyType );
            _local().addToLocals( Symbol.newFormalParam( "arr", anyArrayType, 0, _localsLevel() ) );
            global.addToLocals( Symbol.newFunction( "len", intType, Symbol.NO_VALUE, _locals() ) );
        }

        // '\n'
        global.addToLocals( Symbol.newConst( "eol", charType, '\n' ) );
    }

    private SymbolTable() {}



    // IMPORTANT: no need to check if the scope is not null, since the symbol table should always be initialized before use
    // +   the only way the scope can be null is if the user calls scopeClose() more times than scopeOpen()

    // get the global scope
    public static Scope _global() { return global; }
    // get the global scope's local symbols
    public static SymbolMap _globals() { return new SymbolMap( global.getLocals() ); }
    // get the global scope's size
    public static int _globalsSize() { return _globals().size(); }
    // get the global scope's size without predefined symbols
    public static int _globalsSizeNoPredef() { return _globals().size() - SymbolTable.predefSymbolCnt; }
    
    // get the current scope
    public static Scope _local() { return Tab.currentScope(); }
    // get the current scope's local symbols
    public static SymbolMap _locals() { return new SymbolMap( _local().getLocals() ); }
    // get the current scope's size
    public static int _localsSize() { return _locals().size(); }
    // get the current scope's level
    public static int _localsLevel() { return currScopeLevel; }
    
    // open a new scope
    public static Scope openScope()
    {
        Tab.openScope();
        scopeList.add( new ScopeInfo( _local(), currScopeLevel ) );
        currScopeLevel++;

        return _local();
    }
    // close the most recent scope
    // +   stop when the global scope is reached
    public static Scope closeScope()
    {
        // prevent the global scope from being closed
        if( _local() == global ) return global;
        
        Scope curr = _local();
        Tab.closeScope();
        currScopeLevel--;
        
        return curr;
    }


    // try to add the given symbol to the symbol table and return if the addition was successful
    public static boolean addSymbol( Symbol symbol )
    {
        if( symbol == null ) return false;
        Symbol existing = findSymbol( symbol._name() );

        // if a type with the given name has already been defined, and is not redefinable, this symbol cannot redefine it or hide it
        if( existing != noSym
            && existing._kind() == Symbol.TYPE
            && !existing._type().isPrimitiveType()
        ) return false;

        // if a system type with the same name already exists, prevent it from being redefined
        if(
         // existing == noSym    ||   // this symbol cannot be redefined
            existing == anySym   ||
            existing == voidSym  ||
            existing == nullSym  //
         // existing == intSym   ||   // redefinable
         // existing == charSym  ||   // redefinable
         // existing == boolSym       // redefinable
        )
        return false;

        // return if the symbol has been added to the current scope
        return _local().addToLocals( symbol );
    }

    // try to add the given symbol map to the symbol table
    // +   return the index of the element that wasn't added successfully (-1 if everything is ok)
    public static int addSymbols( SymbolMap symbols )
    {
        int i = 0;
        for( Symbol symbol : symbols )
        {
            if( !addSymbol( symbol ) )
            {
                return i;
            }
        }

        return -1;
    }

    // find the symbol with the given name in the symbol table
    // +   start the search from the most recent open scope
    // +   if the object cannot be found in the current scope, go to its parent scope and search there
    // +   return the found object, or noSymbol if the search was unsuccessful
    public static Symbol findSymbol( String name )
    {
        Symbol symbol = null;
        
        for( Scope curr = _local(); curr != null; curr = curr.getOuter() )
        {
            SymbolDataStructure locals = curr.getLocals();
            if( locals == null ) continue;

            symbol = ( Symbol )locals.searchKey( name );
            // if a match has been found, break
            if( symbol != null ) break;
        }

        return ( symbol != null ) ? symbol : noSym;
    }
    

    
    // return the symbol table as string
    public static String dump()
    {
        StringBuilder builder = new StringBuilder();

        builder.append( "=========================SYMBOL TABLE==========================\n" );
        for( ScopeInfo curr : scopeList )
        {
            // start at the -1'st (global) scope
            String scopeName = ( curr._level() > -1 ) ? String.format( "Scope[%d]\n", curr._level() ) : "Global\n";
            builder.append( "--------------------------------------------------------------- <<< " ).append( scopeName )
                .append( curr._symbols().toString( curr._level() + 1 ) );
        }

        return builder.toString();
    }

    // // return the symbol table as string
    // public static String dump_old()
    // {
    //     String output = null;
        
    //     try( ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    //          SystemStreamReplacer replacer = new SystemStreamReplacer( SystemStreamReplacer.STDOUT, buffer );
    //     )
    //     {
    //         // workaround since symbol table dump method only outputs to System.out
    //         Tab.dump();
    //         output = buffer.toString( "UTF-8" );
    //     }
    //     catch( IOException ex )
    //     {
    //         Compiler.errors.add( CompilerError.SEMANTIC_ERROR, "Error during conversion of symbol table to string", ex );
    //         return null;
    //     }
        
    //     return output;
    // }

}
