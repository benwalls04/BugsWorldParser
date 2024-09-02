import components.queue.Queue;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.statement.Statement;
import components.statement.Statement1;
import components.utilities.Reporter;
import components.utilities.Tokenizer;

/**
 * Layered implementation of secondary methods {@code parse} and
 * {@code parseBlock} for {@code Statement}.
 *
 * @author Ben Walls, Matthew Chandran
 *
 */
public final class Statement1Parse1 extends Statement1 {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Converts {@code c} into the corresponding {@code Condition}.
     *
     * @param c
     *            the condition to convert
     * @return the {@code Condition} corresponding to {@code c}
     * @requires [c is a condition string]
     * @ensures parseCondition = [Condition corresponding to c]
     */
    private static Condition parseCondition(String c) {
        assert c != null : "Violation of: c is not null";
        assert Tokenizer
                .isCondition(c) : "Violation of: c is a condition string";
        return Condition.valueOf(c.replace('-', '_').toUpperCase());
    }

    /**
     * Parses an IF or IF_ELSE statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires <pre>
     * [<"IF"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [an if string is a proper prefix of #tokens] then
     *  s = [IF or IF_ELSE Statement corresponding to if string at start of #tokens]  and
     *  #tokens = [if string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static void parseIf(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0 && tokens.front().equals("IF") : ""
                + "Violation of: <\"IF\"> is proper prefix of tokens";

        // consume start tokens
        tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isCondition(tokens.front()),
                "Error: Violated of valid condition after IF token");
        Condition condition = parseCondition(tokens.dequeue());
        String thenToken = tokens.dequeue();
        Reporter.assertElseFatalError(thenToken.equals("THEN"),
                "Error: Keyword" + " \"" + "THEN" + "\" " + "expected, found: "
                        + "\"" + thenToken + "\"");

        // parse if block
        Statement ifStatement = s.newInstance();
        ifStatement.parseBlock(tokens);
        if (tokens.front().equals("ELSE")) {
            // consume "ELSE" token
            tokens.dequeue();
            // parse else block
            Statement elseStatement = s.newInstance();
            elseStatement.parseBlock(tokens);
            // assemble if else statement
            s.assembleIfElse(condition, ifStatement, elseStatement);
        } else {
            // assemble if statement
            s.assembleIf(condition, ifStatement);
        }

        // consume end tokens
        String endToken = tokens.dequeue();
        Reporter.assertElseFatalError(endToken.equals("END"),
                "Error: Keyword" + " \"" + "END" + "\" " + "expected, found: "
                        + "\"" + endToken + "\"");
        String ifToken = tokens.dequeue();
        Reporter.assertElseFatalError(ifToken.equals("IF"),
                "Error: Keyword" + " \"" + "IF" + "\" " + "expected, found: "
                        + "\"" + ifToken + "\"");

    }

    /**
     * Parses a WHILE statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires <pre>
     * [<"WHILE"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [a while string is a proper prefix of #tokens] then
     *  s = [WHILE Statement corresponding to while string at start of #tokens]  and
     *  #tokens = [while string at start of #tokens] * tokens
     * else
     *  [reports an appropriate error message to the console and terminates client]
     * </pre>
     */
    private static void parseWhile(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0 && tokens.front().equals("WHILE") : ""
                + "Violation of: <\"WHILE\"> is proper prefix of tokens";

        // consume start tokens
        tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isCondition(tokens.front()),
                "Error: Violated of valid condition after WHILE token");
        Condition condition = parseCondition(tokens.dequeue());
        String doToken = tokens.dequeue();
        Reporter.assertElseFatalError(doToken.equals("DO"),
                "Error: Keyword" + " \"" + "DO" + "\" " + "expected, found: "
                        + "\"" + doToken + "\"");
        // parse block
        Statement ns = s.newInstance();
        ns.parseBlock(tokens);

        // consume end tokens
        String endToken = tokens.dequeue();
        Reporter.assertElseFatalError(endToken.equals("END"),
                "Error: Keyword" + " \"" + "END" + "\" " + "expected, found: "
                        + "\"" + endToken + "\"");
        String whileToken = tokens.dequeue();
        Reporter.assertElseFatalError(whileToken.equals("WHILE"),
                "Error: Keyword" + " \"" + "WHILE" + "\" " + "expected, found: "
                        + "\"" + whileToken + "\"");

        // assemble if statement
        s.assembleWhile(condition, ns);

    }

    /**
     * Parses a CALL statement from {@code tokens} into {@code s}.
     *
     * @param tokens
     *            the input tokens
     * @param s
     *            the parsed statement
     * @replaces s
     * @updates tokens
     * @requires [identifier string is a proper prefix of tokens]
     * @ensures <pre>
     * s =
     *   [CALL Statement corresponding to identifier string at start of #tokens]  and
     *  #tokens = [identifier string at start of #tokens] * tokens
     * </pre>
     */
    private static void parseCall(Queue<String> tokens, Statement s) {
        assert tokens != null : "Violation of: tokens is not null";
        assert s != null : "Violation of: s is not null";
        assert tokens.length() > 0
                && Tokenizer.isIdentifier(tokens.front()) : ""
                        + "Violation of: identifier string is proper prefix of tokens";

        String instruction = tokens.dequeue();
        Reporter.assertElseFatalError(
                Tokenizer.isIdentifier(instruction)
                        || Tokenizer.isKeyword(instruction),
                "Error: identifier or keyword expected, found: " + "\""
                        + instruction + "\"");
        s.assembleCall(instruction);
    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public Statement1Parse1() {
        super();
    }

    /*
     * Public methods ---------------------------------------------------------
     */

    @Override
    public void parse(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        // parse statement
        Statement tmpStatement = this.newInstance();
        switch (tokens.front()) {
            case "IF": {
                parseIf(tokens, tmpStatement);
                break;
            }
            case "IF_ELSE": {
                parseIf(tokens, tmpStatement);
                break;
            }
            case "WHILE": {
                parseWhile(tokens, tmpStatement);
                break;
            }
            default: {
                Reporter.assertElseFatalError(
                        Tokenizer.isIdentifier(tokens.front()) || tokens.front()
                                .equals(Tokenizer.END_OF_INPUT),
                        "Error: Expect an Identifier, \"IF\", \"IF_ELSE\", \"WHILE\", \"### END OF INPUT ### \" found: \""
                                + tokens.front() + "\"");
                parseCall(tokens, tmpStatement);
                break;
            }
        }

        // replace this with temporary statement
        this.transferFrom(tmpStatement);
    }

    @Override
    public void parseBlock(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        Statement tmpBlock = this.newInstance();
        int counter = 0;
        while (!tokens.front().equals(Tokenizer.END_OF_INPUT)
                && !tokens.front().equals("END")
                && !tokens.front().equals("ELSE")) {
            Statement child = this.newInstance();
            // parse child
            child.parse(tokens);
            // add child statement to block
            tmpBlock.addToBlock(counter, child);
            counter++;
        }

        // replace this with temporary block
        this.transferFrom(tmpBlock);

    }

    /*
     * Main test method -------------------------------------------------------
     */

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        /*
         * Get input file name
         */
        out.print("Enter valid BL statement(s) file name: ");
        String fileName = in.nextLine();
        /*
         * Parse input file
         */
        out.println("*** Parsing input file ***");
        Statement s = new Statement1Parse1();
        SimpleReader file = new SimpleReader1L(fileName);
        Queue<String> tokens = Tokenizer.tokens(file);
        file.close();
        s.parse(tokens); // replace with parseBlock to test other method
        /*
         * Pretty print the statement(s)
         */
        out.println("*** Pretty print of parsed statement(s) ***");
        s.prettyPrint(out, 0);

        in.close();
        out.close();
    }

}
