import javax.swing.*;
import javax.swing.border.EtchedBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

/**
 * by Kurt Jiang
 *
 * to Alex and Jun,
 * i need a reset method from the simulator.
 *
 */
public class GUI
{
    private Simulator simulator;

    private JPanel mainPanel, mipsPanel, regPanel, memPanel, titlePanel,
                              mipsHeader, regHeader, memHeader,
                                           regTextPanel, hexDecPanel;

    private JTextArea memArea, mipsArea;
    private JScrollPane memScrollPane;
    private JButton loadButton, clearButton, lookupButton, stepButton, runButton, resetButton, hexButton, decButton;
    // (AN) I add 1 more textField, pc2TextFiled, which is the one in the register panel. It's difference with 
    // the pcTextField in Memory panel
    private JTextField pcTextField, cycleTextField, instTextField, stallTextField, pc2TextField;
    private String format;
    private String[] regString = {"r0/zero", "r1/at", "r2/v0", "r3/v1", "r4/a0",
        "r5/a1", "r6/a2", "r7/a3", "r8/t0", "r9/t1",
        "r10/t2", "r11/t3", "r12/t4", "r13/t5", "r14/t6",
        "r15/t7", "r16/s0", "r17/s1", "r18/s2", "r19/s3",
        "r20/s4", "r21/s5", "r22/s6", "r23/s7", "r24/t8",
        "r25/t9", "r26/k0", "r27/k1", "r28/gp", "r29/sp",
        "r30/fp", "r31/ra"};
    private JTextField[] regTextField = new JTextField[32];




    public GUI()
    {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(new Dimension(1000, 600));
        frame.setResizable(false);

        simulator = new Simulator();

        createMainPanel();
        frame.setContentPane(mainPanel);
        frame.setVisible(true);
    }

    private void createMainPanel()
    {
        mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        format = "0x%08x";

        createTitlePanel();
        createMipsPanel();
        createRegPanel();
        createMemPanel();

        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(mipsPanel, BorderLayout.WEST);
        mainPanel.add(memPanel, BorderLayout.CENTER);
        mainPanel.add(regPanel, BorderLayout.EAST);
    }

    private void createTitlePanel()
    {
        titlePanel = new JPanel();
        titlePanel.setLayout(new GridLayout(1, 3));
        titlePanel.setBorder(new EtchedBorder());

        titlePanel.add(new JLabel("Assembly Source"));
        titlePanel.add(new JLabel("Memory"));
        titlePanel.add(new JLabel("CPU Status"));

    }

    private void createMipsPanel()
    {
        mipsPanel = new JPanel();
        mipsHeader = new JPanel();
        mipsArea= new JTextArea();

        loadButton = new JButton("Load");
        loadButton.addActionListener(new LoadButtonListener());
        clearButton = new JButton("Clear");
        clearButton.addActionListener(new ClearButtonListener());

        mipsHeader.setLayout(new FlowLayout(FlowLayout.LEFT));
        mipsHeader.add(loadButton);
        mipsHeader.add(clearButton);


        mipsArea.setBorder(new EtchedBorder());

        mipsPanel.setLayout(new BorderLayout());
        mipsPanel.setPreferredSize(new Dimension(300, 600));
        mipsPanel.add(mipsHeader, BorderLayout.NORTH);
        mipsPanel.add(mipsArea, BorderLayout.CENTER);
    }

    private void createRegPanel()
    {
        regPanel = new JPanel();
        regHeader = new JPanel();
        regTextPanel = new JPanel();
        hexDecPanel = new JPanel();

        stepButton = new JButton("Step");
        stepButton.addActionListener(new StepButtonListener());

        runButton = new JButton("Run");
        runButton.addActionListener(new RunButtonListener());

        resetButton = new JButton("Reset");
        resetButton.addActionListener(new ResetButtonListener());

        hexButton = new JButton("Hex");
        hexButton.addActionListener(new HexButtonListener());

        decButton = new JButton("Dec");
        decButton.addActionListener(new DecButtonListener());

        regHeader.setLayout(new FlowLayout(FlowLayout.LEFT));
        regHeader.add(stepButton);
        regHeader.add(runButton);
        regHeader.add(resetButton);

        hexDecPanel.setLayout(new FlowLayout());
        hexDecPanel.add(new JLabel("Register Value Format: "));
        hexDecPanel.add(hexButton);
        hexDecPanel.add(decButton);

        initRegister();

        regPanel.setLayout(new BorderLayout());
        regPanel.setPreferredSize(new Dimension(400, 600));
        regPanel.add(regHeader, BorderLayout.NORTH);
        regPanel.add(regTextPanel, BorderLayout.CENTER);
        regPanel.add(hexDecPanel, BorderLayout.SOUTH);
    }

    private void createMemPanel()
    {
        memPanel = new JPanel();
        memHeader = new JPanel();
        memArea = new JTextArea();

        lookupButton = new JButton("Lookup");

        pcTextField = new JTextField(String.format(format, simulator.getPC()));

        memHeader.setLayout(new FlowLayout(FlowLayout.LEFT));
        memHeader.add(pcTextField);
        memHeader.add(lookupButton);

        initMemory();

        memPanel.setLayout(new BorderLayout());
        memPanel.add(memHeader, BorderLayout.NORTH);
    }

    private void initMemory()
    {
        String memString = "";

        for (int i = 0; i < 36; i++)
        {
            memString += String.format("0x%08x:   %016x\n", i * 4, 0);
        }

        memArea.setText(memString);
        memArea.setEditable(false);

        memScrollPane = new JScrollPane(memArea);
        memScrollPane.setVerticalScrollBar(new JScrollBar(JScrollBar.VERTICAL));

        memPanel.add(memScrollPane, BorderLayout.CENTER);
    }

    private void updateMemory()
    {
        int index = 0;
        String text = "";
        String thisLine;

        int[] memCode = simulator.getMemory();
        //(AN) : for each loop, i increment by +4.
        for (int i = 0; i < simulator.getMemorySize(); i+=4)
        {

            if (index == simulator.getPC())
            {
                thisLine = String.format("--------->:   %016x\n", memCode[i]);
            }
            else
            {
                // Since i increment by 4 on the for loop, the i address no need to *4
                thisLine = String.format("0x%08x:   %016x\n", i, memCode[i]);
            }
            text += thisLine;
            index += 4;
        }
        
        //(AN) I add the following line to update the pcTextFiled on top of Memory Panel
        pcTextField.setText(String.format("0x%08x", simulator.getPC()));
        memArea.setText(text);
    }

    private void initRegister()
    {
        regTextPanel.removeAll();


        JLabel pcLabel = new JLabel("PC");
        pcLabel.setBorder(new EtchedBorder());

        JLabel cycLabel = new JLabel("Cycle");
        cycLabel.setBorder(new EtchedBorder());

        JLabel instLabel = new JLabel("Inst");
        instLabel.setBorder(new EtchedBorder());

        // (AN) I change the stall to MemAccess
        JLabel stallLabel = new JLabel("Mem");
        stallLabel.setBorder(new EtchedBorder());

        pc2TextField = new JTextField();
        pc2TextField.setEditable(false);
        // (AN) initiate with value 0
        pc2TextField.setText(String.format("0x%08x", 0));

        cycleTextField = new JTextField();
        cycleTextField.setEditable(false);
        // (AN) initiate with value 0
        cycleTextField.setText(String.format("%d", 0));

        /** I need a method to get current instruction. **/
        // (AN) We don't need to display the current instruction.
        // The requirement is to display the number of instruction already been executed
        // Therefore, the method is getTotalInst()
        instTextField = new JTextField();
        instTextField.setEditable(false);
        // (AN) initiate with value 0
        instTextField.setText(String.format("%d", 0));

        stallTextField = new JTextField();
        stallTextField.setEditable(false);
        // I am not clear what the stall stands for.
        // (AN) I don't know either, but we do need 1 text field to show the number of Memory Access
        // so I make this stall to display the number of memory access
        stallTextField.setText(String.format("%d", 0));

        regTextPanel.setLayout(new GridLayout(18, 4));
        regTextPanel.setBorder(new EtchedBorder());

        regTextPanel.add(pcLabel);
        regTextPanel.add(pc2TextField);
        regTextPanel.add(cycLabel);
        regTextPanel.add(cycleTextField);
        regTextPanel.add(instLabel);
        regTextPanel.add(instTextField);
        regTextPanel.add(stallLabel);
        regTextPanel.add(stallTextField);

        int [] registers = simulator.getRegister();
        for (int i = 0; i < 32; i++)
        {
            JLabel regName = new JLabel(regString[i]);
            regName.setBorder(new EtchedBorder());

            regTextField[i] = new JTextField(String.format(format, registers[i]));
            regTextPanel.add(regName);
            regTextPanel.add(regTextField[i]);
        }

    }

    private void updateRegister()
    {
        int[] registers = simulator.getRegister();
        for (int i = 0; i < registers.length; i++)
        {
            regTextField[i].setText(String.format(format, registers[i]));
        }
        
        // (AN) : I add 4 more line to update the statistics after updating all register
        pc2TextField.setText(String.format("0x%08x", simulator.getPC()));
        cycleTextField.setText(String.format("%d", simulator.getNumberOfCycle()));
        instTextField.setText(String.format("%d", simulator.getTotalInst()));
        stallTextField.setText(String.format("%d", simulator.getMemoryAccess()));
    }

    private class LoadButtonListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            try
            {
                Scanner scanner = new Scanner(new File("lab4test2.asm"));
                String text = "";

                while (scanner.hasNextLine())
                {
                    text += scanner.nextLine() + "\n";
                }
                mipsArea.setText(text);
                simulator.readInstruction("lab4test2.asm");
                updateMemory();
            }
            catch (FileNotFoundException e)
            {
                mipsArea.setText("File not found.");
            }
            catch (IOException e)
            {
                mipsArea.setText("Error");
            }


        }
    }

    private class ClearButtonListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            mipsArea.setText("");
            simulator.reset();
            initMemory();
            initRegister();
        }
    }
    private class StepButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            simulator.execute();
            updateMemory();
            updateRegister();

        }
    }

    private class RunButtonListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            simulator.run();
            updateMemory();
            updateRegister();
        }
    }

    /**
     * I hope you guys can write a reset method for me to call.
     */
    private class ResetButtonListener implements ActionListener
    {
        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            mipsArea.setText("");
            // I provide an additional method call reset to reset everything
            // initMemory() and initRegister() only print out zero, but the actual 
            // register value and memory value in the simulator are still there.
            // reset method will reset everything in the register table and memory table.
            simulator.reset();
            initMemory();
            initRegister();
        }
    }

    private class HexButtonListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            format = "0x%08x";
            updateMemory();
            updateRegister();
        }
    }

    private class DecButtonListener implements ActionListener
    {

        @Override
        public void actionPerformed(ActionEvent actionEvent)
        {
            format = "%d";
            updateMemory();
            updateRegister();
        }
    }
}
