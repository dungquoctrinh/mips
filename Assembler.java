
import static org.junit.Assert.assertEquals;
//sample test

/**
 * Created by Kurt Jiang
 */
//(AN) I change the name of this class to Assembler, as its function is to assemble asm file
    //sample  didae
public class Assembler //MainDriver
{
    // and, or, add, addu, addiu, sll, srl, sra, sub, sltu, sltiu, beq, bne, lw, sw, j, jr, and jal.
    // r type: and, or, add, addu, sll, srl, sra, sub, sltu, jr
    // i type: addiu, sltiu, beq, bne, lw, sw,
    // j type: j, jal,
    private static Set<String> Rtype = new TreeSet<String>(
            Arrays.asList("and", "or", "add", "addu", "sll", "srl",
            "sra", "sub", "sltu", "jr", "slt", "syscall")
    );

    private static Set<String> Itype = new TreeSet<String>(
            Arrays.asList("addiu", "sltiu", "beq", "bne", "lw", "sw", "addi", "lui", "ori")
    );

    //chaneg here
    private static Set<String> Jtype = new TreeSet<String>(
            Arrays.asList("j", "jal")
    );


    /**
     * Symbole table w/ key: symbol(String), value: lineNumber(int)
     */
    private static HashMap<String, Integer> symbolTable = new HashMap<String, Integer>();

    /**
     * Creats symbol table.
     * @param inFile
     * @throws FileNotFoundException
     */
    private static void symbolTable(String inFile) throws FileNotFoundException
    {

        int lineNumber = 0;
        Scanner scanner = new Scanner(new FileReader(inFile));
        String thisLine = "";
        String symbol = "";

        while (scanner.hasNextLine())
        {
            thisLine = scanner.nextLine();

            /** Removes comments **/
            if (thisLine.contains("#"))
                thisLine = thisLine.substring(0, thisLine.indexOf("#"));

            if (thisLine.contains("."))
                thisLine = "";
                
            if (thisLine.contains(":"))
            {
                symbol = thisLine.substring(0, thisLine.indexOf(":"));
                symbolTable.put(symbol, lineNumber);
                //System.out.println(lineNumber + " " + symbol);
                // (AN) I case there is no code in the same line with label
                // lineNumber-- so that the following code will be the same line number with the label
                if (thisLine.split("[:\\ \\t]+").length == 1)
                    lineNumber--;
                
            }

            if (thisLine.trim().length() != 0)
                lineNumber++;
        }
        
        /**
         * Reading data's label after reading code's label
         * Anh Nguyen
         */
        Scanner scan = new Scanner(new FileReader(inFile));
        while (scan.hasNextLine())
        {
            thisLine = scan.nextLine();

            /** Removes comments **/
            if (thisLine.contains("#"))
                thisLine = thisLine.substring(0, thisLine.indexOf("#"));

            if (thisLine.contains(".data") || thisLine.contains(".text")){
                thisLine = "";
            }
            
            if (thisLine.contains(".word"))
            {
                if (thisLine.contains(":")){
                    symbol = thisLine.substring(0, thisLine.indexOf(":"));
                    symbolTable.put(symbol, lineNumber);
                    //System.out.println(lineNumber + " " + symbol);
                    if (thisLine.split("[:\\ \\t]+").length == 1)
                        lineNumber--;
                }
            }

            if (thisLine.trim().length() != 0)
                lineNumber++;
        }
    }

    /**
     * Assembles the code.
     * @param inFile
     * @param outFile
     * @throws IOException
     */
    public static void  assemble(String inFile, String outFile) throws IOException
    {
        //(AN): calling the 1st Pass in here for convenience
        symbolTable(inFile);
        
        Scanner scanner = new Scanner(new FileReader(inFile));
        FileWriter writer = new FileWriter(outFile);
        
        String thisLine = "";
        String opCode = "";
        String symbol = "";

        int output = 0;
        int lineNumber = 0;

        while (scanner.hasNextLine())
        {
            thisLine = scanner.nextLine();
            
            // AN(Add) : reset output, otherwise any blank line will duplicate previous line
            //           reset symbol, otherwise it gonna reuse the previous symbol
            output = 0; 
            symbol = "";
            
            /** look for symbol **/
            if (thisLine.contains(":"))
                // AN(Modify) : the original code will eliminate the label,
                // but there still is the ':' character, I add 1 to parameter of substring.
                // orginal code:
                //thisLine = thisLine.substring(thisLine.indexOf(":"));
                thisLine = thisLine.substring(thisLine.indexOf(":") + 1);

            /** Removes comments **/
            if (thisLine.contains("#"))
                thisLine = thisLine.substring(0, thisLine.indexOf("#"));
                
            if (thisLine.contains(".")){
                thisLine = "";
            }
            // AN(Modify) : replace tab character may trash some instruction, later you can't 
            // find the opCode
            // example:   j\tLabel <--- remove tab make it become jLabel , which you can't 
            // identify the opcode j
            // original code:
            //thisLine = thisLine.replaceAll("\t", "");
            thisLine = thisLine.trim();
            try
            {
                // AN(modify) : there are 3 possible character between opcode and the first operand
                //              which are space and tab and nothing
                // I modify the parameter of split so that it can take those 3 possible character to split
                // original code:
                //opCode = thisLine.split(" ")[0];
                opCode = thisLine.split("[ |\\t|\\$]+")[0];
                if (Rtype.contains(opCode))
                    output = Convert.convertRegister(thisLine);

                else if (Itype.contains(opCode)){
                    // AN(Modify this if block): calculate offset if there is a label
                    // If there is no label, offset = -1;
                    // Pass thisLine, and offset to the convertImmediate
                    // original code:
                    // output = Convert.convertImmediate(thisLine);
                    
                    if (thisLine.split("[ \\,\\t\\$]+").length > 3)
                        symbol = thisLine.split("[ \\,\\t\\$]+")[3];
                        // (AN) in case lw using label
                    else if (opCode.compareTo("lw") == 0 || 
                            opCode.compareTo("sw") == 0 && 
                            !thisLine.contains("(")){
                        symbol = thisLine.split("[ \\,\\t\\$]+")[2];
                    }
                    
                    if (symbolTable.containsKey(symbol)){
                        // (AN) in case lw using label
                        if (opCode.compareTo("lw") == 0 || 
                            opCode.compareTo("sw") == 0 && 
                            !thisLine.contains("(")){
                            output = Convert.convertImmediate(thisLine,4*(symbolTable.get(symbol)));
                        }
                        // AN(Modify): if thisLine appear after the label, then the offset is negative
                        // orginal code: 
                        // output = Convert.convertJump(thisLine, lineNumber - symbolTable.get(symbol));
                        else
                            output = Convert.convertImmediate(thisLine,4*(symbolTable.get(symbol) - lineNumber));
                    }
                    else
                        output = Convert.convertImmediate(thisLine, 0);
                }
                else if (Jtype.contains(opCode))
                {                    
                    //(AN): I change the value in split() method in order to make sure
                    // it work with multible tab and space
                    symbol = thisLine.split("[ \\,\\t\\$]+")[1];    
                    output = Convert.convertJump(thisLine, 4*symbolTable.get(symbol));
                }
                
                // AN (Add): I add the condition, only write the output is the output != 0,
                // because output == 0 means that line is a blank line (contains no code) in the input file
                // For this lab, professor want us to print the number out to the sreen in hex
                // So I add a line to print the number out
                // original code: First 2-line in the if statement
                if (output != 0){
                    writer.write(String.format("%32s", Integer.toBinaryString(output)).replace(" ", "0"));
                    writer.write("\n");
                    //System.out.println(String.format("0x%8s", Integer.toHexString(output)).replace(' ', '0'));
                }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                //ignore
            }
            finally
            {
                if (thisLine.trim().length() != 0)
                    lineNumber++;
            }
        }
        
        
        /**
         * writing data segment after the code segment
         */
        scanner = new Scanner(new FileReader(inFile));
        while (scanner.hasNextLine())
        {
            int value = 0;
            thisLine = scanner.nextLine();
            if (thisLine.contains(":"))
                thisLine = thisLine.substring(thisLine.indexOf(":") + 1);

            /** Removes comments **/
            if (thisLine.contains("#"))
                thisLine = thisLine.substring(0, thisLine.indexOf("#"));

            thisLine = thisLine.trim();
            
            if(thisLine.contains(".word") || thisLine.contains(".byte")){
                try{
                    value = Integer.parseInt(thisLine.split("[ \\t]+")[1]); // the immediate is in Decimal form
                }
                catch (NumberFormatException e){
                    value = Integer.decode(thisLine.split("[ \\t]+")[1]); // the immediate is in Hex form
                }
            }
            
            if (value != 0){
                    writer.write(String.format("%32s", Integer.toBinaryString(value)).replace(" ", "0"));
                    writer.write("\n");
                    //System.out.println(String.format("0x%8s", Integer.toHexString(output)).replace(' ', '0'));
                }
            
            if (thisLine.trim().length() != 0)
                    lineNumber++;
        }
        writer.flush();
        writer.close();
    }


    public static void main(String[] args) throws IOException
    {
        /**
         * sampleX should be the test file provided by the instructor
         * don't need to create myoutputX, it's going to be generated
         *
         * outputX should be the binary file generated by spim w/o line number
         */

        Scanner scanner;
        String correct = "";
        String my = "";
        System.out.println("File 1 .....");
        // test case 1
        //symbolTable("test1.asm");
        assemble("test1.asm", "myoutput1.txt");


        scanner = new Scanner(new FileReader(new File("myoutput1.txt")));
        while (scanner.hasNextLine())
            correct += scanner.nextLine() + "\n";

        scanner = new Scanner(new FileReader(new File("myoutput1.txt")));
        while (scanner.hasNextLine())
            my += scanner.nextLine() + "\n";

        assertEquals(correct, my);
        
        System.out.println("File 2 .....");

        // test case 2
        //symbolTable("test2.asm");
        assemble("test2.asm", "myoutput2.txt");
        scanner = new Scanner(new FileReader(new File("myoutput2.txt")));
        while (scanner.hasNextLine())
            correct += scanner.nextLine() + "\n";

        scanner = new Scanner(new FileReader(new File("myoutput2.txt")));
        while (scanner.hasNextLine())
            my += scanner.nextLine() + "\n";

        assertEquals(correct, my);

        System.out.println("File 3 .....");
        // test case 3
        //symbolTable("test3.asm");
        assemble("test3.asm", "myoutput3.txt");
        scanner = new Scanner(new FileReader(new File("myoutput3.txt")));
        while (scanner.hasNextLine())
            correct += scanner.nextLine() + "\n";

        scanner = new Scanner(new FileReader(new File("myoutput3.txt")));
        while (scanner.hasNextLine())
            my += scanner.nextLine() + "\n";

        assertEquals(correct, my);
        
    }

}
