import java.util.*;
import java.lang.Integer;
import java.lang.Short;
import java.io.*;
/**
 * Write a description of class Simulator here.
 * 
 * @author Anh Nguyen
 * @author Dung Quoc Trinh
 * @version 4/25/2015
 */
public class Simulator implements MIPsSimulator
{
    //last counter. where the last counter is 
    private int lastCounter;
    //pc counter 
    private int PC = 0;
    // instance variables - replace the example below with your own
    static int MAX_SIZE = 32;

    // register table 
    private int[] register = new int[MAX_SIZE];
    //array of instruction
    //MAX_VALUE = 2^16 -1 
    private int[] memoryTable = new int[Short.MAX_VALUE - 8];
    //data table is read first
    //reading data will be easier
    private int[] data = new int[Short.MAX_VALUE - 8];

    /**
     * Reading the instruction into the array of integer
     * @para the assemby file to read in 
     * the array of memoryTable. There will be plenty 2^31. STandard issues in a MIPS machine
     * The data path is read first (havent completed)
     * The instruction is accessed by PC counter (or in the next one: index)
     * The instruction is read afterward. Each instruction take 4 space each 
     * Ex: First instruction at index 0, the next instruction will be at index 4
     */
    public void readInstruction(String inFile) throws IOException 
    {
        //(AN): I make readInstruction take in an asm file. Then use lab3 to convert it to binary, and use here
        // for convenience
        Assembler.assemble(inFile, "binaryFile.txt");
        //read in the file 
        Scanner scanner = new Scanner(new FileReader("binaryFile.txt"));
        while (scanner.hasNextLine())
        {

            try
            {
                //will be assume in binary format 
                String instruction = scanner.nextLine();

                memoryTable[PC] = convertString(instruction);
                //increase the PC by 4
                PC = PC + 4;

            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                //doing nothing 
            }
        }
        //use this to know the end of the instruction table 
        lastCounter = PC;
        PC = 0;
    }

    public int convertString(String number)
    {
        int instruction = 0;
        double power = 31;
        //char[] charArray = number.toCharArray();
        //maxsize = 32
        for(int i = 0; i < MAX_SIZE; i++)
        {
            String convertNumber = number.charAt(i) + "";
            int value = Integer.parseInt(convertNumber);
            value = value * (int)Math.pow(2.0, power);
            power--;
            instruction += value;
        }
        //System.out.print(charArray[0]); 
        return instruction;
    }

    /**
     * Run the whole program
     */
    public void run()
    {
        while(!stop)
        {
            execute();
            if (PC >= lastCounter+4){
                System.out.println("Error: Endless program");
                break;
            }
            //System.out.println(""+ PC + " : " + String.format("0x%8s", Integer.toHexString(memoryTable[PC])).replace(' ', '0'));

            //(AN) don't modify PC, calling execute method will take care of it
            //PC += 4;
        }
    }

    /**
     * Accessor to the table of Register
     * @return the table of register in form of an array of integer
     */
    public int[] getRegister(){
        return register;
    }
    
    /**
     * Accessor to the table of Register
     * @return the table of register in form of an array of integer
     */
    public int[] getMemory(){
        return memoryTable;
    }
    
    /**
     * Accessor to the table of Memory
     * @return the table of register in form of an array of integer
     *      The starting address in memory is 0.
     *      All instruction and data are layout correctly
     */
    public int getMemorySize(){
        return lastCounter;
    }
    
    /**
     * Accessor to the PC
     * @return PC as an integer
     */
    public int getPC(){
        return PC;
    }
    
    /**
     * Accessor to program's stat - total number of cycle
     * @return number of cycle
     */
    public int getNumberOfCycle(){
        return totalCycle;
    }
    
    /**
     * Accessor to program's stat - total number of executed instruction
     * @return number of instruction
     */
    public int getTotalInst(){
        return totalInst;
    }
    
    /**
     * Accessor to program's stat - total number of accessing Memory
     * @return number of memory referencing
     */
    public int getMemoryAccess(){
        return totalMemAccess;
    }
    
    /**
     * reset everything
     */
    public void reset(){
        lastCounter = 0;
        PC = 0;
        register = new int[MAX_SIZE];
        memoryTable = new int[Short.MAX_VALUE - 8];
        stop = false;
        totalCycle = 0;
        totalInst = 0;
        totalMemAccess = 0;
    }
    private static int shift6  = 6;
    private static int shift11 = 11;
    private static int shift16 = 16;
    private static int shift21 = 21;
    private static int shift26 = 26;
    private static int regMask = (int)Math.pow(2, 5);
    private static int funMask = (int)Math.pow(2, 6);
    
    private int totalCycle;
    private int totalInst;
    private int totalMemAccess;

    private boolean stop = false;
    
    /**
     * 1 step of instrucion
     * @return void
     *  However, this single executation method will attemp several change to
     *     Register(s), Memory, PC, Stats (total cycle, total instruction, total Memory Access)
     */
    public void execute()
    {
        /**
         * FETCH
         * geting instruction in memory
         */  
        int toExecute = memoryTable[PC];
        //Register Instruction
        if ((toExecute >> shift26) == 0){
            totalCycle += executeRegister();
        }
        //Jump Instruction
        else if ((toExecute >> shift26) == 2 || (toExecute >> 26) == 3){
            totalCycle += executeJump();
        }
        //Immediate Instruction
        else{
            totalCycle += executeImm();
        }
        totalInst++;
    }

    /**
     * print register & stat to check
     */
    public void printData(){
        System.out.println("totalCycle : " + totalCycle);
        System.out.println("totalInst  : " + totalInst);
        System.out.println("Mem Acces  : " + totalMemAccess);
        System.out.println("\nPC  : " + PC);
        for(int i = 0; i <31; i++){
            if (register[i] != 0)
            System.out.println("$"+ i + " : " + register[i]);
        }
        
    }
    
    /**
     * print memory to check
     */
    public void printMem(){
        for(int i = 0; i < lastCounter; i++){
            if (memoryTable[i] > 0)
            System.out.println("Line # "+ i + " : " + String.format("0x%8s", Integer.toHexString(memoryTable[i])).replace(' ', '0'));
        }
    }
    /**
     * Helper method
     */
    private int executeRegister(){
        int toExecute = memoryTable[PC];
        int numberOfCycle = 4;
        
        /**
         * DECODE - Analysing operand
         */
        int rs    = (toExecute >> shift21) & (regMask-1);
        int rt    = (toExecute >> shift16) & (regMask-1);
        int rd    = (toExecute >> shift11) & (regMask-1);
        int shamt = (toExecute >> shift6)  & (regMask-1);
        int funct = toExecute & (funMask-1);
        
        //System.out.println("Reg[" + rd + "] = Reg[" + rs + "] +&| Reg [" + rt + "] .... F code: " + funct);
        
        PC += 4;
        /**
         * EXECUTE - MEMORY ACCESS - REGISTER FILE
         */
        switch (funct) {
            case 0:{
                register[rd] = register[rt] << shamt;
                numberOfCycle += shamt;
                break;
            }
            case 2:{
                int mask = Integer.MAX_VALUE >> (shamt -1);
                register[rd] = (register[rt]  >> shamt) & mask;;
                numberOfCycle += shamt;
                break;
            }
            case 3:{
                register[rd] = register[rt] >> shamt;
                numberOfCycle += shamt;
                break;
            }
            case 8:{
                PC = register[rs];
                break;
            }
            case 12: {

                stop = true;
                numberOfCycle = 1;
                break;
            }
            case 32: {
                register[rd] = register[rs] + register[rt];
                break;
            }
            case 33:{
                int carry = 0;
                if ((register[rt] != 0)
                    && (register[rs] != 0)
                    && (register[rs] / register[rt] < 0)){
                    carry = Integer.MIN_VALUE;
                }
                register[rd] = carry | (Integer.MAX_VALUE & register[rs]) + (Integer.MAX_VALUE & register[rt]);                
                break;
            }
            case 34:{
                register[rd] = register[rs] - register[rt];
                break;
            }
            case 36:{
                register[rd] = register[rs] & register[rt];
                break;
            }
            case 37:{
                register[rd] = register[rs] | register[rt];
                break;
            }
            case 42:{
                register[rd] = register[rs] < register[rt] ? 1 : 0;
                break;
            }
            case 43:{
                int toSet = 0;
                if ((register[rt] != 0)
                    && (register[rs] != 0)
                    && (register[rs] / register[rt] < 0)){
                    if (register[rs] < 0)
                        toSet = 1;
                    else
                        toSet = 0;
                }
                register[rd] = (register[rs] & Integer.MAX_VALUE)  < (register[rt] & Integer.MAX_VALUE) ? 1 : 0;
                register[rd] = toSet;
                break;
            }
        }
        return numberOfCycle;
    }

    private int executeJump(){
        int toExecute = memoryTable[PC];
        /**
         * DECODE
         */
        int op = (toExecute >> shift26 ) & (funMask-1);
        int address = (toExecute & ((1 << shift26)-1)) << 2;
        int numberOfCycle = 3;
        //System.out.println("op = " +op+ " ,add " +address);
        
        /**
         * PC Write
         */
        switch (op){
            case 2:{
                PC = address;
                break;
            }
            case 3:{
                register[31] = PC + 4;
                PC = address;
                break;
            }
        }
        return numberOfCycle;
    }

    private int executeImm(){
        int toExecute = memoryTable[PC];
        int numberOfCycle = 4;
        /**
         * DECODE
         */
        int op  = (toExecute >> shift26) & (funMask-1);
        int rs  = (toExecute >> shift21) & (regMask-1);
        int rt  = (toExecute >> shift16) & (regMask-1);
        int imm = toExecute & (1 << shift16)-1;
        // sign extention
        imm <<= 16;
        imm >>= 16;
        
        PC += 4;
        /**
         * EXECUTE - MEMORY ACCESS - REGISTER FILE
         */
        //System.out.println("op = " + op + ", rs = " + rs + ", rt = " + rt + ", imm = " + imm);
        switch (op){
            case 4:{
                PC = (register[rs] == register[rt])? PC + (imm<<2) - 4: PC;
                numberOfCycle--;
                break;
            }
            case 5:{
                PC = (register[rs] != register[rt])? PC + (imm<<2) - 4 : PC;
                numberOfCycle--;
                break;
            }
            case 8:{
                register[rt] = register[rs] + imm;
                break;
            }
            case 9:{
                register[rt] = register[rs] + imm;
                break;
            }
            case 11:{
                register[rt] = (register[rs] < imm)? 1 : 0;
                break;
            }
            case 15:{
                register[rt] = imm << shift16;
                break;
            }
            case 35:{
                register[rt] = memoryTable[register[rs] + imm];
                totalMemAccess++;
                break;
            }
            case 43:{
                memoryTable[register[rs] + imm] = register[rt];
                numberOfCycle = 5;
                totalMemAccess++;
                break;
            }
        }
        return numberOfCycle;
    }
}
