package rs.ac.bg.etf.pp1;

import java.util.ArrayList;

public class TokenList
{
    private ArrayList<Token> tokenList = new ArrayList<>();

    public TokenList() {}



    public boolean add( Token token )
    {
        return tokenList.add( token );
    }

    public void removeLast()
    {
        if( tokenList.size() > 0 ) tokenList.remove( tokenList.size() - 1 );
    }

    public void clear()
    {
        tokenList.clear();
    }



    public int size()
    {
        return tokenList.size();
    }

    public boolean checkIndex( int index )
    {
        return index >= 0 && index < tokenList.size();
    }

    public Token get( int index )
    {
        if( !checkIndex( index ) ) throw new IndexOutOfBoundsException( "<Token list>'s index out of bounds" );
        return tokenList.get( index );
    }

    public Token getFirst()
    {
        return tokenList.get( 0 );
    }

    public Token getLast()
    {
        return tokenList.get( tokenList.size() - 1 );
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for( Token token : tokenList )
        {
            builder.append( token.getValue() );
        }
        if( builder.charAt( builder.length()-1 ) != '\n' ) builder.append( "\n" );
        return builder.toString();
    }

}
