package Lexical;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Lexical {
	
	private BufferedReader br;
	private String line;
	private Character c;
	private Integer currentCol;
	private Integer tkLine = -1;
	private Integer tkCol;
	private String tkValue;
	
	public Lexical(String file) {
		this.readFile(file);
	}
	
	private void readFile(String file) {
		try {
			this.br = new BufferedReader(new FileReader(file));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public Token nextToken() {
		// First line or line already explored		
		if (this.line == null || this.c == '\n') {
			this.readLine();
			if (this.line == null) return null;
			tkLine++;
			this.currentCol = -1;
			tkCol = -1;
		} 
		
		if (this.line.isEmpty()) return this.nextToken();
		
		this.tkValue = "";
		this.tkCol++;
		
		this.readChar();
		
		// Ignores empty spaces		
		while (this.c == ' ' || this.c == '\t') {
			this.tkCol++;
			this.readChar();
			if (this.c == '\n') return this.nextToken();
		}
		
		if (this.c == '$') {
			this.c = '\n';
			return this.nextToken();
		}
		 
		this.tkValue += this.c;
		
		// Id, decimal numerical constant, integer numerical constant, reserved word 
		if (!Table.symbolList.contains(this.c)) {
			while(!Table.symbolList.contains(this.c)) {
				this.readChar();
				if (!Table.symbolList.contains(this.c)) this.tkValue += this.c;
				else this.currentCol--;
			}
		}
		// String		
		else if (this.c.equals('"')) {
			this.readChar();
			this.tkValue += this.c;
			while(!this.c.equals('"')) {
				this.readChar();
				this.tkValue += this.c; 
			}
		}
		// Char		
		else if (this.c.equals('\'')) {
			String tmpTkValue = this.tkValue;
			this.readChar();
			tmpTkValue += this.c;
			if (tmpTkValue.charAt(tmpTkValue.length()-1) == '\'') this.tkValue = tmpTkValue;
			else {
				this.readChar();
				tmpTkValue += this.c;
				if (tmpTkValue.charAt(tmpTkValue.length()-1) == '\'') this.tkValue = tmpTkValue;
				else this.currentCol = this.currentCol - 2;
			}
		}
		// Other stuff		
		else {
			this.readChar();
			// >= <= ++ 			
			if (Table.operators.containsKey(this.tkValue + this.c)) this.tkValue += this.c;
			// Negative Unary
			else if (this.tkValue.equals("-") && this.c == '1' && this.currentCol >= 3) {
				String relationalOperatorCandidate = "";				
				if (this.line.charAt(this.currentCol-2) == ' ') {
					relationalOperatorCandidate += this.line.charAt(this.currentCol-4);
					relationalOperatorCandidate += this.line.charAt(this.currentCol-3);
				} 
				else {
					relationalOperatorCandidate += this.line.charAt(this.currentCol-3);
					relationalOperatorCandidate += this.line.charAt(this.currentCol-2);
				}
				if (Table.relationalOperators.contains(relationalOperatorCandidate)) {
					this.tkValue += this.c;
				}
			}
			else this.currentCol--;
		}
		
		
		return new Token(this.tkLine, this.tkCol, this.categoryze(), this.tkValue);
	}
	
	private void readLine() {
		try {
			this.line = this.br.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readChar() {
		this.currentCol++;
		if (this.currentCol <= this.line.length()-1) {
			this.c = this.line.charAt(this.currentCol);
		}
		else this.c = '\n';
	}
	
	private TokenCategory categoryze() {
		if (this.isDecNumConst()) return TokenCategory.DECNUMCONST;
		else if (this.isIntNumConst()) return TokenCategory.INTNUMCONST;
		else if (this.isReservedWord()) return Table.reservedWords.get(this.tkValue);
		else if (this.isDelimiter()) return Table.delimiters.get(this.tkValue);
		else if (this.isId()) return TokenCategory.ID;
		else if (this.isTerminator()) return TokenCategory.TERM;
		else if (this.isSeparator()) return Table.separators.get(this.tkValue);
		else if (this.isOperator()) return Table.operators.get(this.tkValue);
		else if (this.isChar()) return TokenCategory.CHARCONST;
		else if (this.isString()) return TokenCategory.STRINGCONST;
		else if (this.isNegativeUnary()) return TokenCategory.NEGUNOP;
		else return TokenCategory.UNDEFINED;
	}
	
	private boolean isDecNumConst() {
		return this.tkValue.matches("\\d+\\.\\d+");
	}
	
	private boolean isIntNumConst() {
		return this.tkValue.matches("\\d+");
	}
	
	private boolean isReservedWord() {
		return Table.reservedWords.containsKey(this.tkValue);
	}

	private boolean isId() {
		return this.tkValue.matches("([a-z])+([a-zA-Z\\_])*(\\d)*");
	}
	
	private boolean isDelimiter() {
		return Table.delimiters.containsKey(this.tkValue);
	}
	
	private boolean isTerminator() {

		return this.tkValue.contentEquals("#");
	}

	private boolean isSeparator() {
		return Table.separators.containsKey(this.tkValue);
	}
	
	private boolean isOperator() {
		return Table.operators.containsKey(this.tkValue);
	}
	
	private boolean isChar() {
		return this.tkValue.matches("\\'[a-zA-Z]{0,1}\\'") || this.tkValue.matches("\\'\\d{0,1}\\'");
	}

	private boolean isString() {
		return this.tkValue.matches("\"([a-zA-Z]*\\s*)*(\\d*\\s*)*(\\d+\\.\\d+)*\\s*\"");
	}

	private boolean isNegativeUnary() {
		return this.tkValue.matches("-1");
	}
}
