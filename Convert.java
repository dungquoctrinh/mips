import java.util.*;
/**
 * 3 methods to convert 3 group of instruction
 * 
 * @author Anh Nguyen 
 * @version 4/17/2015
 */
public class Convert
{
    // value to shift register and opcode
    private static int shift6  = (int)Math.pow(2, 6);
    private static int shift11 = (int)Math.pow(2, 11);
    private static int shift16 = (int)Math.pow(2, 16);
    private static int shift21 = (int)Math.pow(2, 21);
    private static int shift26 = (int)Math.pow(2, 26);
    private static int regMask = (int)Math.pow(2, 5);
    
    private static Set<String> ShAmt = new TreeSet<String>(
            Arrays.asList("sll", "srl", "sra"));
    private static Set<String> BrEq = new TreeSet<String>(
            Arrays.asList("beq", "bne"));        
   
    /* 2 Map below by Jun*/
    private static Map<String,Integer> opCode = new HashMap<String,Integer>();
    public static Map<String,Integer> reg = new HashMap<String,Integer>();
    
    static  
    {
           addOpcode();
           register();
       
    }
    
    public static void addOpcode()
    {
         //opcode is put in the right direction 
        opCode.put("add", 0x20);
        opCode.put("addiu", 9); //addimediate // the tail is different 
                                        //add head 
        opCode.put("addu", 0x21);
        
        opCode.put("and", 0x24);
        opCode.put("or", 0x25);
        opCode.put("sll", 0);
        opCode.put("srl", 0x2);
        opCode.put("sra", 0x3);
        
        opCode.put("sub", 0x22);
        opCode.put("sltu", 0x2B);
        opCode.put("sltiu", 11);
        opCode.put("beq", 4);
        opCode.put("bne", 5);
        opCode.put("lw", 35);
        opCode.put("sw", 43);
        opCode.put("j", 2);
        opCode.put("jr", 0x08);
        opCode.put("jal", 3);
        
        //new additional opcode for lab4
        opCode.put("addi", 8); 
        opCode.put("lui", 15);
        opCode.put("ori", 13);
        opCode.put("slt", 42);
        opCode.put("syscall", 12);
    }
    
    public static void register()
    {
        reg.put("zero", 0);
        reg.put("$zero", 0);
        reg.put("at", 1);
        reg.put("$v0", 2);
        reg.put("$v1", 3);
        reg.put("$a0", 4);
        reg.put("$a1", 5);
        reg.put("$a2", 6);
        reg.put("$a3", 7);
        reg.put("$t0", 8);
        reg.put("$t1", 9);
        reg.put("$t2", 10);
        reg.put("$t3", 11);
        reg.put("$t4", 12);
        reg.put("$t5", 13);
        reg.put("$t6", 14);
        reg.put("$t7", 15);
        reg.put("$s0", 16);
        reg.put("$s1", 17);
        reg.put("$s2", 18);
        reg.put("$s3", 19);
        reg.put("$s4", 20);
        reg.put("$s5", 21);
        reg.put("$s6", 22);
        reg.put("$s7", 23);
        reg.put("$t8", 24);
        reg.put("$t9", 25);
        reg.put("$k0", 26);
        reg.put("$k1", 27);
        reg.put("$gp", 28);
        reg.put("$sp", 29);
        reg.put("$fp", 30);
        reg.put("$ra", 31);
        //rt is needed ?

    
        reg.put("$0", 0);
        reg.put("S1", 1);
        reg.put("$2", 2);
        reg.put("$3", 3);
        reg.put("$4", 4);
        reg.put("$5", 5);
        reg.put("$6", 6);
        reg.put("$7", 7);
        reg.put("$8", 8);
        reg.put("$9", 9);
        reg.put("$10", 10);
        reg.put("$11", 11);
        reg.put("$12", 12);
        reg.put("$13", 13);
        reg.put("$14", 14);
        reg.put("$15", 15);
        reg.put("$16", 16);
        reg.put("$17", 17);
        reg.put("$18", 18);
        reg.put("$19", 19);
        reg.put("$20", 20);
        reg.put("$21", 21);
        reg.put("$22", 22);
        reg.put("$23", 23);
        reg.put("$24", 24);
        reg.put("$25", 25);
        reg.put("$26", 26);
        reg.put("$27", 27);
        reg.put("$28", 28);
        reg.put("$29", 29);
        reg.put("$30", 30);
        reg.put("$31", 31);
          
    }

    /**
     * Convert Register Instruction
     * @param input is the input. eg. "add $s0, $t0, $zero" 
     *        assuming client already deliminate any label before or comment after the instruction
     *        This is an invalid input "loop: add $s0, $0, $zero"
     *                         or this "add $s0, $0, $zero #comment"
     * @return an integer represent the instruction
     *  return 0 if invalid input such as wrong opcode, or wrong register
     */
    public static int convertRegister(String input){
        //System.out.println("Reg : "+input);
        int toReturn = 0;
        int func = 0;
        int rd = 0;
        int rs = 0;
        int rt = 0;
        int shamt = 0;
        // add a space before $ incase there was not, so later we can split with the space
        input = input.replace("$", " $");
        // eliminate all space, and comma
        String[] breakDown = input.split("[ |\\,|\\t]+");
        try{
            func = opCode.get(breakDown[0].toLowerCase());
            // special case of "jr" which only have rs
            if (breakDown[0].toLowerCase().compareTo("jr") == 0){
                rs = reg.get(breakDown[1]) * shift21;
                rd = 0;
            }
            // shift instruction which have shift amount
            else if (ShAmt.contains(breakDown[0])){
                rd    = reg.get(breakDown[1]) * shift11;
                rt    = reg.get(breakDown[2]) * shift16;
                // the shift amount is in Decimal form
                shamt = ((regMask - 1) & Integer.parseInt(breakDown[3])) * shift6; 
            }
            // normal Register instruction
            else if (breakDown.length == 4){               
                rd   = reg.get(breakDown[1]) * shift11; // first register in syntax is rd
                rs   = reg.get(breakDown[2]) * shift21; // second reg in syntax is rs
                rt   = reg.get(breakDown[3]) * shift16; // third reg in syntax is rt
            }
        }
        catch (ArrayIndexOutOfBoundsException e){
            return 0;
        }
        // invalid opcode
        catch (NullPointerException e){
            return 0;
        }
        catch (NumberFormatException e){
            // the shift amount is in Hex form
            shamt = ((regMask - 1) & Integer.decode(breakDown[3])) * shift6; 
        }
        /* Machine code has formular: op - rs - rt - rd - shamt - func */
        toReturn = rs + rt + rd + shamt + func; 
        return toReturn;
    }
    
    /**
     * Convert Immediate Instruction
     * @param input is the input. eg. "addi $t0, $t0, 100"   <--- valid
     *                            or  "lw $t1, 4($t2)"       <--- valid
     *        assuming client already deliminate any label before or comment after the instruction
     *        This is an invalid input "loop: add $s0, $t0, 100"
     *                         or this "add $s0, $t0, 100 #comment"
     *                         
     *        Also, client be responsible to calculate the offset of any branch instruction
     *        eg: Invalid parameter "beq $t0, $t2, someLabel"
     *            Valid parameter   "beq $t0, $t1, 100"
     *        where 100 is the calculated offset of the "someLabel"
     *        
     * @param offset if the calculated offset, if instruction need no offset, then offset = -1.
     *        
     * @return an integer represent the instruction
     *  return 0 if invalid input such as wrong opcode, or wrong register
     */
    public static int convertImmediate(String input, int offset){
        int toReturn = 0;
        int op = 0;
        int rt = 0;
        int rs = 0;
        int im = 0;
        String immediate = "0";
        String[] OffsetRs;
        
        String[] breakDown = input.split("[ \\,\\t\\$\\(\\)]+");
        try{
            op   = opCode.get(breakDown[0].toLowerCase()) * shift26; // get Opcode
            // lui instruction (additional of lab4)
            if (op == 15){
                rt = reg.get("$" + breakDown[1]) * shift16;
                immediate = breakDown[2];
            }
            // lw or sw using label
            else if ((op == 35* shift26 || op == 43* shift26) && !input.contains("(")){
                rt = reg.get("$" + breakDown[1]) * shift16;
                rs = 0;
                offset <<= 2;
            }
            // br and normal case
            else if (!input.contains("(")){
                if (BrEq.contains(breakDown[0])){
                    rs = reg.get("$" + breakDown[1]) * shift21;
                    rt = reg.get("$" + breakDown[2]) * shift16;
                }
                else{
                    rs = reg.get("$" + breakDown[2]) * shift21;
                    rt = reg.get("$" + breakDown[1]) * shift16;
                }
                immediate = breakDown[3]; // last field is immediate
            }
            // rs has combined with offset in case of load and store instruction
            else{
                immediate = breakDown[2]; // get offset which is the first field
                rt = reg.get("$" + breakDown[1]) * shift16;
                rs = reg.get("$" + breakDown[3]) * shift21; // get rs which is the 2nd field  
            }
        }
        // missing argument
        catch (ArrayIndexOutOfBoundsException e){
            return 0;
        }
        // invalid opcode
        catch (NullPointerException e){
            return 0;
        }  
        
        
        if (offset != 0){
            im = offset>>2;
        } else {
            try{
                im = Integer.parseInt(immediate); // the immediate is in Decimal form
            }
            catch (NumberFormatException e){
                im = Integer.decode(immediate); // the immediate is in Hex form
            }
        }
        /* Machine code has formular: op - rs - rt - im */
       
        im &= (shift16 - 1);       
        toReturn = op + rs + rt + im;
        //System.out.println(input + " " + String.format("%08X", toReturn));
        return toReturn;
    }
    
    /**
     * Convert Jump Instruction (j and jal only)
     * @param input is the input. eg. ("j aLabel" , -300)
     *                                ("jal aFunction", 50)
     *        where -300 or 50 are arealdy calculated offset by cliend
     *        
     *        "j aLabel" was the original instruction, and -300 is the offset calculated by client
     *        
     *        
     * @return an 32bit integer represent the instruction
     *  return 0 if invalid input such as wrong opcode, or wrong formular of offset
     */
    public static int convertJump(String input, int offset){
        //System.out.println("Jump: "+input);
        int toReturn = 0;
        int op = 0;
        try{
            // break down the input String to get the opcode
            String[] breakDown = input.split("[ \\,\\t]+");
            // get opcode and shift to front
            op = opCode.get(breakDown[0]) * shift26;
        }
        // invalid opcode
        catch (NullPointerException e){
            return 0;
        } 
        offset = (shift26 - 1) & (offset>>2);
        toReturn = op + offset;
        //System.out.println(String.format("%32s", Integer.toBinaryString(toReturn)).replace(" ", "0"));
        return toReturn;
    }
}