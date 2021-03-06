//ECS140
//Assignment 2
//February 5, 2020AD
//Developed by Emil Tokaev(914187873), Christopher Morgan-Sudduth(916216315)

import java.util.*;

public class Parser extends Object {

    private Chario chario;
    private Scanner scanner;
    private Token token;
	private SymbolTable table;

    private Set < Integer > addingOperator,
        multiplyingOperator,
        relationalOperator,
        basicDeclarationHandles,
        statementHandles;

    public Parser(Chario c, Scanner s) {
        chario = c;
        scanner = s;
        initHandles();
        token = scanner.nextToken();
    }

    public void reset() {
        scanner.reset();
        token = scanner.nextToken();
    }

    private void initHandles() {
        addingOperator = new HashSet < Integer > ();
        addingOperator.add(Token.PLUS);
        addingOperator.add(Token.MINUS);
        multiplyingOperator = new HashSet < Integer > ();
        multiplyingOperator.add(Token.MUL);
        multiplyingOperator.add(Token.DIV);
        multiplyingOperator.add(Token.MOD);
        relationalOperator = new HashSet < Integer > ();
        relationalOperator.add(Token.EQ);
        relationalOperator.add(Token.NE);
        relationalOperator.add(Token.LE);
        relationalOperator.add(Token.GE);
        relationalOperator.add(Token.LT);
        relationalOperator.add(Token.GT);
        basicDeclarationHandles = new HashSet < Integer > ();
        basicDeclarationHandles.add(Token.TYPE);
        basicDeclarationHandles.add(Token.ID);
        basicDeclarationHandles.add(Token.PROC);
        statementHandles = new HashSet < Integer > ();
        statementHandles.add(Token.EXIT);
        statementHandles.add(Token.ID);
        statementHandles.add(Token.IF);
        statementHandles.add(Token.LOOP);
        statementHandles.add(Token.NULL);
        statementHandles.add(Token.WHILE);
    }

    void debug(String msg){
        if(false){
	    System.err.println(msg);
	}
    }

    private void accept(int expected, String errorMessage) {
        if (token.code != expected)
            fatalError(errorMessage);
        token = scanner.nextToken();
    }

    private void fatalError(String errorMessage) {
        chario.putError(errorMessage);
        throw new RuntimeException("Fatal error");
    }
	
    private SymbolEntry enterId(){
      SymbolEntry entry = null;
      if (token.code == Token.ID)
         entry = table.enterSymbol(token.string);
      else
         fatalError("identifier expected");
      token = scanner.nextToken();
      return entry;
	}

    private SymbolEntry findId(){
      SymbolEntry entry = null;
      if (token.code == Token.ID)
         entry = table.findSymbol(token.string);
      else
         fatalError("identifier expected");
      token = scanner.nextToken();
      return entry;
	}	
	
    private void initTable(){
      table = new SymbolTable(chario);
      table.enterScope();
      table.enterSymbol("BOOLEAN");
      table.enterSymbol("CHAR");
      table.enterSymbol("INTEGER");
      table.enterSymbol("TRUE");
      table.enterSymbol("FALSE");
	} 	
    public void parse() {
        subprogramBody();
        accept(Token.EOF, "extra symbols after logical end of program");
    }
     
    /*
    subprogramBody =
          subprogramSpecification "is"
          declarativePart
          "begin" sequenceOfStatements
          "end" [ <procedure>identifier ] ";"
    */
	
	
	
    private void subprogramBody() {
        debug("subprogramBody");
        subprogramSpecification();
        accept(Token.IS, "'is' expected");
        declarativePart();
        accept(Token.BEGIN, "'begin' expected");
        sequenceOfStatements();
        accept(Token.END, "'end' expected");
        if (token.code == Token.ID)
            token = scanner.nextToken();
        accept(Token.SEMI, "semicolon expected");
    }

    /*
    subprogramSpecification = "procedure" identifier [ formalPart ]
    */

    private void subprogramSpecification() {
        debug("subprogramSpecification");
        accept(Token.PROC, "'procedure' expected");
        token = scanner.nextToken();
        
        if (token.code == Token.L_PAR) {
            formalPart();
        }
    } //FIXME not sure if correct2

    /*
    formalPart = "(" parameterSpecification { ";" parameterSpecification } ")"
    */

    private void formalPart() {
        debug("formalPart");
        accept(Token.L_PAR, "'(' expected");
        parameterSpecification();
        while (token.code == Token.SEMI) {
            token = scanner.nextToken();
            parameterSpecification();
        }
        accept(Token.R_PAR, "')' expected");
    }



    /*
    parameterSpecification = identifierList ":" mode <type>
    */


    private void parameterSpecification() {
        debug("parameterSpecification");
        identifierList();
        accept(Token.COLON, "':' expected");
        if (token.code == Token.IN)
            token = scanner.nextToken();
        if (token.code == Token.OUT)
            token = scanner.nextToken();
        //token = scanner.nextToken();
        accept(Token.ID, "'146 type' expected");
        
    }

    /*
    declarativePart = { basicDeclaration }
    */

    private void declarativePart() {
        debug("declarativePart");
        while (basicDeclarationHandles.contains(token.code))
            basicDeclaration();
    }

    /*
    basicDeclaration = objectDeclaration | numberDeclaration
                     | typeDeclaration | subprogramBody
    */

    private void basicDeclaration() {
        debug("basicDeclaration");
        switch (token.code) {
            case Token.ID:
                numberOrObjectDeclaration();
                break;
            case Token.TYPE:
                typeDeclaration();
                break;
            case Token.PROC:
                subprogramBody();
                break;
            default:
                fatalError("error in declaration part");
        }
    }

    /*
    objectDeclaration =
          identifierList ":" typeDefinition ";"

    numberDeclaration =
          identifierList ":" "constant" ":=" <static>expression ";"
    */
    private void numberOrObjectDeclaration() {
        debug("numberOrObjectDeclaration");
        identifierList();
        accept(Token.COLON, "':' expected");
        if (token.code == Token.CONST) {
            token = scanner.nextToken();
            accept(Token.GETS, "':=' expected");
            expression();
        } else
            typeDefinition();
        accept(Token.SEMI, "semicolon expected");
    }

    /*
    typeDeclaration = "type" identifier "is" typeDefinition ";"
    */
    void typeDeclaration() {
        debug("typeDeclaration");
        accept(Token.TYPE, "'type' expected");
        identifierList();
        accept(Token.IS, "'is' expected");
        typeDefinition();
        accept(Token.SEMI, "semicolon expected");
    }
  

    /*
    typeDefinition = enumerationTypeDefinition | arrayTypeDefinition | range | <type>identifier
    */


    void typeDefinition() {
        debug("typeDefinition");
        switch (token.code) {
            case Token.L_PAR:
                enumerationTypeDefinition();
                break;
            case Token.ARRAY:
                arrayTypeDefinition();
                break;
            case Token.RANGE:
                range();
                break;
            case Token.ID:
                accept(Token.ID, "identifier expected");
                
                break;
		//fixme
            default:
                fatalError("error in typeDefinition part");
                break;
        }
    }



    /*
    enumerationTypeDefinition = "(" identifierList ")"
    */

    void enumerationTypeDefinition() {
        debug("enumerationTypeDefinition");
        accept(Token.L_PAR, "'(' expected");
        identifierList();
        accept(Token.R_PAR, "')' expected");

    }

    /*
    arrayTypeDefinition = "array" "(" index { "," index } ")" "of" <type>identifier
     */
    void arrayTypeDefinition() {
        debug("arrayTypeDefinition");
        accept(Token.ARRAY, "'array' expected");
        accept(Token.L_PAR, "'(' expected");
        index();
        while (token.code == Token.COMMA) {
            token = scanner.nextToken();
            index();
        }
        accept(Token.R_PAR, "')' expected");
        accept(Token.OF, "'of' expected");
        accept(Token.ID, "identifier expected");
    }

    /*
    index = range | <type>identifier
    */
    void index() {
        debug("index");
        if (token.code == Token.RANGE)
            range();
        else if (token.code == Token.ID) {
            accept(Token.ID, "identifier expected");
        } else fatalError("error in index type");
    }

    /*
    range = "range " simpleExpression ".." simpleExpression
    */
    void range() {
        debug("range");
        accept(Token.RANGE, "'range', expected");
        expression();
        accept(Token.THRU, "'..', expected");
        expression();
    }

    /*
    identifierList = identifier { "," identifier }
    */
    void identifierList() { 
        debug("identifierList");
        //accept(Token.ID, "'identifier' expected");
        token = scanner.nextToken();
        while (token.code == Token.COMMA) {
            token = scanner.nextToken();
            accept(Token.ID, "'identifier' expected");

        }

    } //FIXME if broken
    /*
    sequenceOfStatements = statement { statement }
    */

    private void sequenceOfStatements() {
        debug("sequenceOfStatements");
        statement();
        while (statementHandles.contains(token.code))
            statement();
    }

    /*
    statement = simpleStatement | compoundStatement

    simpleStatement = nullStatement | assignmentStatement
                    | procedureCallStatement | exitStatement

    compoundStatement = ifStatement | loopStatement
    */

    private void statement() {
        debug("statement");
        switch (token.code) {
            case Token.ID:
                assignmentOrCallStatement();
                break;
            case Token.EXIT:
                exitStatement();
                break;
            case Token.IF:
                ifStatement();
                break;
            case Token.NULL:
                nullStatement();
                break;
            case Token.WHILE:
            case Token.LOOP:
                loopStatement();
                break;
            default:
                fatalError("error in statement");
        }
    }

    /*
    nullStatement = "null" ";"
    */
    void nullStatement() {
        debug("nullStatement");
        accept(Token.NULL, "'null' expected");
        accept(Token.SEMI, "';' expected");
    }
    /*
    loopStatement = [ iterationScheme ] "loop" sequenceOfStatements "end" "loop" ";"

    iterationScheme = "while" condition
    */
    void loopStatement() {
        debug("loopStatement");
        if (token.code == Token.WHILE) {
            token = scanner.nextToken();
            condition();
        }
        accept(Token.LOOP, "'loop' expected");
        sequenceOfStatements();
        accept(Token.END, "'end' expected");
        accept(Token.LOOP, "'loop' expected");
        accept(Token.SEMI, "';' expected");
    }
    /*
    ifStatement =
          "if" condition "then" sequenceOfStatements
          { "elsif" condition "then" sequenceOfStatements }
          [ "else" sequenceOfStatements ]
          "end" "if" ";"
    */
    void ifStatement() {
        debug("ifStatement");
        accept(Token.IF, "'if' expected");
        condition();
        accept(Token.THEN, "'then' expected");
        sequenceOfStatements();
        while (token.code == Token.ELSIF) {
            token = scanner.nextToken();
            condition();
            accept(Token.THEN, "'then' expected");
            sequenceOfStatements();
        }
        if (token.code == Token.ELSE) {
            token = scanner.nextToken();
            sequenceOfStatements();
        }
        accept(Token.END, "'end' expected");
        accept(Token.IF, "'if' expected");
        accept(Token.SEMI, "';' expected");

    }

    /*
    exitStatement = "exit" [ "when" condition ] ";"
    */
    void exitStatement() {
        debug("exitStatement");
        accept(Token.EXIT, "'exit' expected");
        if (token.code == Token.WHEN) {
            token = scanner.nextToken();
            condition();
        }
        accept(Token.SEMI, " semicolon expected");

    }
    /*
    assignmentStatement = <variable>name ":=" expression ";"

    procedureCallStatement = <procedure>name ";"
    */
    private void assignmentOrCallStatement() {
        debug("assignmentOrCallStatement");
        name();
        if (token.code == Token.GETS) {
            token = scanner.nextToken();
            expression();
        }
        //else {}
            
        
        accept(Token.SEMI, "semicolon expected");
    } //FIXME need to except name();

    /*
    condition = <boolean>expression
    */
    private void condition() {
        debug("condition");
        expression();
    }

    /*
    relation [{ "and" relation } | {"or" relation}]
    */


    private void expression() {
        debug("expression");
        relation();
        if (token.code == Token.AND)
            while (token.code == Token.AND) {
                token = scanner.nextToken();
                relation();
            }
        else if (token.code == Token.OR)
            while (token.code == Token.OR) {
                token = scanner.nextToken();
                relation();
            }
    }

    /*
    relation = simpleExpression [ relationalOperator simpleExpression ]
    */

    void relation() {
        debug("relation");
        simpleExpression();
        if (relationalOperator.contains(token.code)) {
            token = scanner.nextToken();
            simpleExpression();
        }
    }

    /*
  simpleExpression =
         [ unaryAddingOperator ] term { binaryAddingOperator term }
   */

    private void simpleExpression() {
        debug("simpleExpression");
        if (addingOperator.contains(token.code))
            token = scanner.nextToken();
        term();
        while (addingOperator.contains(token.code)) {
            token = scanner.nextToken();
            term();
        }
    }

    /*
    term = factor { multiplyingOperator factor }
    */
    void term() {
        debug("term");
        factor();
        while (multiplyingOperator.contains(token.code)) {
            token = scanner.nextToken();
            factor();
        }
    }

    /*
    factor = primary [ "**" primary ] | "not" primary
    */
    void factor() {
        debug("factor");
        if (token.code == Token.NOT) {
            token = scanner.nextToken();
            primary();
        } else {
            primary();
            if (token.code == Token.EXPO) {
                token = scanner.nextToken();
                primary();
            }
        }

    }
    /*
    primary = numericLiteral | name | "(" expression ")"
    */
    void primary() {
        debug("primary");
        switch (token.code) {
            case Token.INT:
            case Token.CHAR:
                token = scanner.nextToken();
                break;
            case Token.ID:
                name();
                break;
            case Token.L_PAR:
                token = scanner.nextToken();
                expression();
                accept(Token.R_PAR, "')' expected");
                break;
            default:
                fatalError("error in primary");
        }
    }

    /*
    name = identifier [ indexedComponent ]
    */
    private void name() {
        debug("name");
        accept(Token.ID, "identifier expected");
        if (token.code == Token.L_PAR)
            indexedComponent();
    }

    /*
    indexedComponent = "(" expression  { "," expression } ")"
    */

    private void indexedComponent() {
        debug("indexedComponent");
        accept(Token.L_PAR, " '(' expected");
        expression();
        while (token.code == Token.COMMA) {
            token = scanner.nextToken();
            expression();
        }
        accept(Token.R_PAR, " ')' expected");
    }
}
