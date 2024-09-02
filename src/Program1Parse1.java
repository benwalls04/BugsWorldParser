import components.map.Map;
import components.program.Program;
import components.program.Program1;
import components.queue.Queue;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.statement.Statement;
import components.utilities.Reporter;
import components.utilities.Tokenizer;

/**
 * Layered implementation of secondary method {@code parse} for {@code Program}.
 *
 * @author Ben Walls, Matthew Chandran
 *
 */
public final class Program1Parse1 extends Program1 {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Parses a single BL instruction from {@code tokens} returning the
     * instruction name as the value of the function and the body of the
     * instruction in {@code body}.
     *
     * @param tokens
     *            the input tokens
     * @param body
     *            the instruction body
     * @return the instruction name
     * @replaces body
     * @updates tokens
     * @requires <pre>
     * [<"INSTRUCTION"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [an instruction string is a proper prefix of #tokens]  and
     *    [the beginning name of this instruction equals its ending name]  and
     *    [the name of this instruction does not equal the name of a primitive
     *     instruction in the BL language] then
     *  parseInstruction = [name of instruction at start of #tokens]  and
     *  body = [Statement corresponding to the block string that is the body of
     *          the instruction string at start of #tokens]  and
     *  #tokens = [instruction string at start of #tokens] * tokens
     * else
     *  [report an appropriate error message to the console and terminate client]
     * </pre>
     */
    private static String parseInstruction(Queue<String> tokens,
            Statement body) {
        assert tokens != null : "Violation of: tokens is not null";
        assert body != null : "Violation of: body is not null";
        assert tokens.length() > 0 && tokens.front().equals("INSTRUCTION") : ""
                + "Violation of: <\"INSTRUCTION\"> is proper prefix of tokens";

        // consume header tokens
        String instToken = tokens.dequeue();
        Reporter.assertElseFatalError(instToken.equals("INSTRUCTION"),
                "Error: Keyword" + " \"" + "INSTRUCTION" + "\" "
                        + "expected, found: " + "\"" + instToken + "\"");
        String name = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isIdentifier(name),
                "Error: Instruction name must not be a primitive instruction");
        String isToken = tokens.dequeue();
        Reporter.assertElseFatalError(isToken.equals("IS"),
                "Error: Keyword" + " \"" + "IS" + "\" " + "expected, found: "
                        + "\"" + isToken + "\" ");

        // parse instruction body
        body.parseBlock(tokens);

        // consume end tokens
        String endToken = tokens.dequeue();
        Reporter.assertElseFatalError(endToken.equals("END"),
                "Error: Keyword" + " \"" + "END" + "\" " + "expected, found: "
                        + "\"" + endToken + "\" ");
        String endName = tokens.dequeue();
        Reporter.assertElseFatalError(endName.equals(name),
                "Error: Keyword" + " \"" + name + "\" " + "expected, found: "
                        + "\"" + endName + "\"");

        return name;
    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public Program1Parse1() {
        super();
    }

    /*
     * Public methods ---------------------------------------------------------
     */

    @Override
    public void parse(SimpleReader in) {
        assert in != null : "Violation of: in is not null";
        assert in.isOpen() : "Violation of: in.is_open";
        Queue<String> tokens = Tokenizer.tokens(in);
        this.parse(tokens);
    }

    @Override
    public void parse(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";
        Program program = new Program1Parse1();

        // consume header tokens
        String programToken = tokens.dequeue();
        Reporter.assertElseFatalError(programToken.equals("PROGRAM"),
                "Error: Keyword" + " \"" + "PROGRAM" + "\" "
                        + "expected, found: " + "\"" + programToken + "\" ");
        String name = tokens.dequeue();
        String isToken = tokens.dequeue();
        Reporter.assertElseFatalError(isToken.equals("IS"),
                "Error: Keyword" + " \"" + "IS" + "\" " + "expected, found: "
                        + "\"" + isToken + "\"");

        // parse context
        Map<String, Statement> context = program.newContext();
        while (!tokens.front().equals("BEGIN")) {
            Statement instruction = program.newBody();
            String instructionName = parseInstruction(tokens, instruction);
            Reporter.assertElseFatalError(!context.hasKey(instructionName),
                    "Error: There must be no repeat instruction names");
            context.add(instructionName, instruction);
        }

        // consume "BEGIN" token
        String beginToken = tokens.dequeue();
        Reporter.assertElseFatalError(beginToken.equals("BEGIN"),
                "Error: Keyword" + " \"" + "BEGIN" + "\" " + "expected, found: "
                        + "\"" + beginToken + "\"");

        // parse body as a block
        Statement body = program.newBody();
        body.parseBlock(tokens);

        // consume end tokens
        String endToken = tokens.dequeue();
        Reporter.assertElseFatalError(endToken.equals("END"),
                "Error: Keyword" + " \"" + "END" + "\" " + "expected, found: "
                        + "\"" + endToken + "\"");
        String endName = tokens.dequeue();
        Reporter.assertElseFatalError(endName.equals(name),
                "Error: Keyword" + " \"" + name + "\" " + "expected, found: "
                        + "\"" + endName + "\"");

        // check end of input token
        Reporter.assertElseFatalError(
                tokens.front().equals(Tokenizer.END_OF_INPUT),
                "Error: Keyword" + " \"" + Tokenizer.END_OF_INPUT + "\" "
                        + "expected, found: " + "\"" + tokens.front() + "\"");

        this.swapContext(context);
        this.swapBody(body);
        this.setName(name);

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
        out.print("Enter valid BL program file name: ");
        String fileName = in.nextLine();
        /*
         * Parse input file
         */
        out.println("*** Parsing input file ***");
        Program p = new Program1Parse1();
        SimpleReader file = new SimpleReader1L(fileName);
        Queue<String> tokens = Tokenizer.tokens(file);
        file.close();
        p.parse(tokens);
        /*
         * Pretty print the program
         */
        out.println("*** Pretty print of parsed program ***");
        p.prettyPrint(out);

        in.close();
        out.close();
    }

}
