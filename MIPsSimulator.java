import java.util.*;
import java.lang.Integer;
import java.lang.Short;
import java.io.*;
/**
 * MIPsSimulator will generate and run MIPS code
 * MIPsSimulator also keep track of statistics such as Clock cycle, Memory Referencing
 * 
 * @author Anh Nguyen
 * @author Dung Quoc Trinh
 * @version 4/25/2015
 */
public interface MIPsSimulator
{
    /**
     * This method will read in an .asm file, convert it into .bin form (using previous lab)
     * Then load those binary instruction into memory from starting address 0. 
     * The instruction will be layout every 4 location in memory 
     *      Ex: the first inst has address 0
     *          the first inst has address 4
     *          the first inst has address 8
     * @param inFile is the .asm file
     */
    public void readInstruction(String inFile)throws IOException ;

    /**
     * Run the whole program
     */
    public void run();
    
    /**
     * 1 step of instrucion
     * @return void
     *  However, this single executation method will attemp several change to
     *     Register(s), Memory, PC, Stats (total cycle, total instruction, total Memory Access)
     */
    public void execute();
    
    /**
     * Accessor to the table of Register
     * @return the table of register in form of an array of integer
     */
    public int[] getRegister();
    
    /**
     * Accessor to the table of Memory
     * @return the table of register in form of an array of integer
     *      The starting address in memory is 0.
     *      All instruction and data are layout correctly
     */
    public int[] getMemory();
    
    /**
     * Accessor to the occupied size in memory
     */
    public int getMemorySize();
    
    /**
     * Accessor to the PC
     * @return PC as an integer
     */
    public int getPC();
    
    /**
     * Accessor to program's stat - total number of cycles
     * @return number of cycle
     */
    public int getNumberOfCycle();
    
    /**
     * Accessor to program's stat - total number of executed instructions
     * @return number of instruction
     */
    public int getTotalInst();
    
    /**
     * Accessor to program's stat - total number of accessing Memory
     * @return number of memory referencing
     */
    public int getMemoryAccess();
    
    /**
     * reset everything
     */
    public void reset();
}
