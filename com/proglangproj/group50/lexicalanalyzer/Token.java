package com.proglangproj.group50.lexicalanalyzer;

public class Token {
    private int tokenType;
    private String tokenValue;
    private int lineNumberOfSourceWhereTokenIs;

    public int getTokenType() {
        return tokenType;
    }

    public void setTokenType(int tokenType) {
        this.tokenType = tokenType;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public void setTokenValue(String tokenValue) {
        this.tokenValue = tokenValue;
    }

    public int getLineNumberOfSourceWhereTokenIs() {
        return lineNumberOfSourceWhereTokenIs;
    }

    public void setLineNumberOfSourceWhereTokenIs(int lineNumberOfSourceWhereTokenIs) {
        this.lineNumberOfSourceWhereTokenIs = lineNumberOfSourceWhereTokenIs;
    }
}
