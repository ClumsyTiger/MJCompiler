package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import java_cup.runtime.Symbol;
import rs.ac.bg.etf.pp1.ast.SyntaxNode;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.symboltable.Tab;

public class ParserTest
{
    static
    {
        DOMConfigurator.configure( Log4JUtils.instance().findLoggerConfigFile() );
        Log4JUtils.instance().prepareLogFile( Logger.getRootLogger() );
    }

    public static void main( String[] args ) throws Exception
    {
        Logger log = Logger.getLogger( ParserTest.class );
        if( args.length < 2 )
        {
            log.error( "Not enough arguments supplied! Usage: MJParser <source-file> <obj-file> " );
            return;
        }

        File sourceCode = new File( args[ 0 ] );
        if( !sourceCode.exists() )
        {
            log.error( "Source file [" + sourceCode.getAbsolutePath() + "] not found!" );
            return;
        }

        log.info( "Compiling source file: " + sourceCode.getAbsolutePath() );

        try( BufferedReader br = new BufferedReader( new FileReader( sourceCode ) ) )
        {
            Yylex lexer = new Yylex( br );
            MJParser p = new MJParser( lexer );
            Symbol s = p.parse();  //pocetak parsiranja
            SyntaxNode prog = ( SyntaxNode )(s.value);

            Tab.init(); // Universe scope
            SemanticPass semanticCheck = new SemanticPass();
            prog.traverseBottomUp( semanticCheck );

            log.info( "Print calls = " + semanticCheck.printCallCount );
            Tab.dump();

            if( !p.errorDetected && semanticCheck.passed() )
            {
                log.info( "Parsiranje uspesno zavrseno!" );
            }
            else
            {
                log.error( "Parsiranje NIJE uspesno zavrseno!" );
            }
        }
    }
}
