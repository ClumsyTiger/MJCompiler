package rs.ac.bg.etf.pp1;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class SymbolCode implements ISymbolCode
{
    private static ArrayList<String> symbolNameList = null;

    private static void initSymbolNameList()
    {
        Field[] fieldList = SymbolCode.class.getFields();
        symbolNameList = new ArrayList<>( fieldList.length );

        SymbolCode symbolInstance = new SymbolCode();
        int wantedModifiers = Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL;

        for( Field field : fieldList )
        {
            try
            {
                int fieldModifiers = field.getModifiers();
                if( field.getType() == int.class && ( fieldModifiers & wantedModifiers ) == wantedModifiers )
                {
                    int fieldValue = field.getInt( symbolInstance );
                    String fieldName = field.getName();

                    while( fieldValue >= symbolNameList.size() )
                    {
                        symbolNameList.add( null );
                    }

                    symbolNameList.set( fieldValue, fieldName );
                }
            }
            catch( IllegalAccessException ex )
            {
            }
        }
    }


    public static String getSymbolName( int symbolCode )
    {
        if( symbolNameList == null )
        {
            initSymbolNameList();
        }
        
        return symbolNameList.get( symbolCode );
    }

    public static boolean isIgnored( int symbolCode )
    {
        return symbolCode == SymbolCode.newline
            || symbolCode == SymbolCode.whitespace
            || symbolCode == SymbolCode.line_comment
            || symbolCode == SymbolCode.multi_comment;
    }

    public static boolean isInvalid( int symbolCode )
    {
        return symbolCode == SymbolCode.error
            || symbolCode == SymbolCode.invalid;
    }

    public static boolean containsNewline( Symbol symbol )
    {
        if( symbol == null ) return false;
        int symbolCode = symbol.getCode();
        
        if( symbolCode == SymbolCode.newline ) return true;

        if( symbolCode == SymbolCode.line_comment    // possibly at the end of file, so not containing newline
         || symbolCode == SymbolCode.multi_comment   // possibly inline
        )
        {
            String value = ( String )symbol.getValue();
            // find if there is at least one newline in the symbol
            boolean newlineFound = Pattern.compile( "\r|\n|\r\n" ).matcher( value ).find();
            return newlineFound;
        }

        return false;
    }

    public static boolean isEOF( int symbolCode )
    {
        return symbolCode == SymbolCode.EOF;
    }

}
