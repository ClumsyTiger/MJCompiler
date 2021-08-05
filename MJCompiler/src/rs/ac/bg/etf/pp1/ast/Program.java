// generated with ast extension for cup
// version 0.8
// 5/7/2021 17:50:47


package rs.ac.bg.etf.pp1.ast;

public class Program implements SyntaxNode {

    private SyntaxNode parent;
    private int line;
    private String i1;
    private GlobalDeclList GlobalDeclList;
    private MethodDeclScope MethodDeclScope;

    public Program (String i1, GlobalDeclList GlobalDeclList, MethodDeclScope MethodDeclScope) {
        this.i1=i1;
        this.GlobalDeclList=GlobalDeclList;
        if(GlobalDeclList!=null) GlobalDeclList.setParent(this);
        this.MethodDeclScope=MethodDeclScope;
        if(MethodDeclScope!=null) MethodDeclScope.setParent(this);
    }

    public String getI1() {
        return i1;
    }

    public void setI1(String i1) {
        this.i1=i1;
    }

    public GlobalDeclList getGlobalDeclList() {
        return GlobalDeclList;
    }

    public void setGlobalDeclList(GlobalDeclList GlobalDeclList) {
        this.GlobalDeclList=GlobalDeclList;
    }

    public MethodDeclScope getMethodDeclScope() {
        return MethodDeclScope;
    }

    public void setMethodDeclScope(MethodDeclScope MethodDeclScope) {
        this.MethodDeclScope=MethodDeclScope;
    }

    public SyntaxNode getParent() {
        return parent;
    }

    public void setParent(SyntaxNode parent) {
        this.parent=parent;
    }

    public int getLine() {
        return line;
    }

    public void setLine(int line) {
        this.line=line;
    }

    public void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public void childrenAccept(Visitor visitor) {
        if(GlobalDeclList!=null) GlobalDeclList.accept(visitor);
        if(MethodDeclScope!=null) MethodDeclScope.accept(visitor);
    }

    public void traverseTopDown(Visitor visitor) {
        accept(visitor);
        if(GlobalDeclList!=null) GlobalDeclList.traverseTopDown(visitor);
        if(MethodDeclScope!=null) MethodDeclScope.traverseTopDown(visitor);
    }

    public void traverseBottomUp(Visitor visitor) {
        if(GlobalDeclList!=null) GlobalDeclList.traverseBottomUp(visitor);
        if(MethodDeclScope!=null) MethodDeclScope.traverseBottomUp(visitor);
        accept(visitor);
    }

    public String toString(String tab) {
        StringBuffer buffer=new StringBuffer();
        buffer.append(tab);
        buffer.append("Program(\n");

        buffer.append(" "+tab+i1);
        buffer.append("\n");

        if(GlobalDeclList!=null)
            buffer.append(GlobalDeclList.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        if(MethodDeclScope!=null)
            buffer.append(MethodDeclScope.toString("  "+tab));
        else
            buffer.append(tab+"  null");
        buffer.append("\n");

        buffer.append(tab);
        buffer.append(") [Program]");
        return buffer.toString();
    }
}
