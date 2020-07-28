package SP18_simulator;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

class JText extends JFrame{
	JLabel lb;
	
	//�������� 
	JPanel north,north1;
	JButton nButton;
	
	//��������
	JPanel left,left1,left2,left3;	
	JTextField fileName,programName,startAddr,programLen;
	JTextField A1,A2,X1,X2,L1,L2,PC1,PC2,SW1;
	JTextField B1,B2,S1,S2,T1,T2,F1;
	
	//�߰�����
	JPanel center,center1,center2,center3,center4;
	JTextField firstAddr,memStart,memTarget,machine;
	JTextArea instruction;
	JScrollPane scroll;
	JButton b1,b2,b3;

	
	//���� ����
	JPanel right;
	JTextArea log;
	JScrollPane scroll2;
	
	//GridBagLayout ���� ����
	GridBagLayout gbl;
	GridBagConstraints gbc;
	
	JLabel temp;
	
	VisualSimulator v;
	
	JMenuBar mb;
	JMenu m0;
	JMenuItem m1;
	
	int stepCheck = 0;
	int allCheck = 0;
	
	String fName= "";
	
	public void getVisualSimulator(VisualSimulator vv) {
		v = vv;
	}
	
	//�̺�Ʈ�� ����
	public void frameAction() {
		//���� ���� ��� ��ư �̺���
		ActionListener event1 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == nButton) {
					String fName = fileName.getText();
					File input = new File(fName);
					try {
						v.sicSimulator.load(input);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					
					//�ؽ�Ʈ ��ġ
					//programName,startAddr,programLen
					programName.setText(v.resourceManager.programName.get(0));
					startAddr.setText(v.resourceManager.startAddr.get(0));
					programLen.setText(v.resourceManager.length.get(0));
				}
			}
		};
		
		nButton.addActionListener(event1);
		
		//������ ��� ��ư �̺�Ʈ
		ActionListener event2 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == b1) {
					//������ ��� ����
					try {
						if((stepCheck == 1) && (v.sicSimulator.instCount == 0)) {//������ ����
							;
						}
						else {
							v.oneStep();
							stepCheck = 1;
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					//������ ����
					//���
					programName.setText(v.resourceManager.pName);
					startAddr.setText(v.resourceManager.pAddr);
					programLen.setText(v.resourceManager.pLength);
					
					//������������
					A1.setText(Integer.toString(v.resourceManager.register[0]));
					X1.setText(Integer.toString(v.resourceManager.register[1]));
					L1.setText(Integer.toString(v.resourceManager.register[2]));
					B1.setText(Integer.toString(v.resourceManager.register[3]));
					S1.setText(Integer.toString(v.resourceManager.register[4]));
					T1.setText(Integer.toString(v.resourceManager.register[5]));
					F1.setText(Double.toString(v.resourceManager.register_F));
					PC1.setText(Integer.toString(v.resourceManager.register[8]));
					SW1.setText(Integer.toString(v.resourceManager.register[9]));
					
					A2.setText(decimalToHex(v.resourceManager.register[0]));
					X2.setText(decimalToHex(v.resourceManager.register[1]));
					L2.setText(decimalToHex(v.resourceManager.register[2]));
					B2.setText(decimalToHex(v.resourceManager.register[3]));
					S2.setText(decimalToHex(v.resourceManager.register[4]));
					T2.setText(decimalToHex(v.resourceManager.register[5]));
					PC2.setText(decimalToHex(v.resourceManager.register[8]));
					
					//����
					firstAddr.setText(v.resourceManager.pAddr);
					memStart.setText(decimalToHex(v.resourceManager.current));
					memTarget.setText(decimalToHex(v.resourceManager.target));
					
					//������� ��ġ
					machine.setText(v.sicSimulator.usingDevice);
					
					//log
					String info="";
					for(int i = 0 ; i < v.sicSimulator.log.size(); i++) {
						info += v.sicSimulator.log.get(i);
						info += "\r\n";
					}
					log.setText(info);
					
					//instruction
					int obc = 0;
					info ="";
					for(int i = 0 ; i < v.resourceManager.relocateCode.size(); i++) {
						if(v.resourceManager.relocateCode.get(i).equals("END")) {
							obc++;
							continue;
						}
						else if((v.resourceManager.relocateCode.get(i).equals(v.sicSimulator.code)) && (v.sicSimulator.obProCount == obc)) {
							info += "��" + v.resourceManager.relocateCode.get(i) + "\r\n";
						}
						else {
							info +=v.resourceManager.relocateCode.get(i) + "\r\n";
						}
					}
					
					instruction.setText(info);
					
				}
			}
		};
		b1.addActionListener(event2);
		
		//all���� ��� ��ư �̺�Ʈ
		ActionListener event3 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == b2) {
					//������ ��� ����
					try {
						if(allCheck == 1) {//������ ����
							;
						}
						else {
							v.allStep();
							allCheck = 1;
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					//������ ����
					//���
					programName.setText(v.resourceManager.pName);
					startAddr.setText(v.resourceManager.pAddr);
					programLen.setText(v.resourceManager.pLength);
					
					//������������
					A1.setText(Integer.toString(v.resourceManager.register[0]));
					X1.setText(Integer.toString(v.resourceManager.register[1]));
					L1.setText(Integer.toString(v.resourceManager.register[2]));
					B1.setText(Integer.toString(v.resourceManager.register[3]));
					S1.setText(Integer.toString(v.resourceManager.register[4]));
					T1.setText(Integer.toString(v.resourceManager.register[5]));
					F1.setText(Double.toString(v.resourceManager.register_F));
					PC1.setText(Integer.toString(v.resourceManager.register[8]));
					SW1.setText(Integer.toString(v.resourceManager.register[9]));
					
					A2.setText(decimalToHex(v.resourceManager.register[0]));
					X2.setText(decimalToHex(v.resourceManager.register[1]));
					L2.setText(decimalToHex(v.resourceManager.register[2]));
					B2.setText(decimalToHex(v.resourceManager.register[3]));
					S2.setText(decimalToHex(v.resourceManager.register[4]));
					T2.setText(decimalToHex(v.resourceManager.register[5]));
					PC2.setText(decimalToHex(v.resourceManager.register[8]));
					
					//����
					firstAddr.setText(v.resourceManager.pAddr);
					memStart.setText(decimalToHex(v.resourceManager.current));
					memTarget.setText(decimalToHex(v.resourceManager.target));
					
					//������� ��ġ
					machine.setText(v.sicSimulator.usingDevice);
					
					//log
					String info="";
					for(int i = 0 ; i < v.sicSimulator.log.size(); i++) {
						info += v.sicSimulator.log.get(i);
						info += "\r\n";
					}
					log.setText(info);
					
					//instruction
					int obc = 0;
					info ="";
					for(int i = 0 ; i < v.resourceManager.relocateCode.size(); i++) {
						if(v.resourceManager.relocateCode.get(i).equals("END")) {
							obc++;
							continue;
						}
						else if((v.resourceManager.relocateCode.get(i).equals(v.sicSimulator.code)) && (v.sicSimulator.obProCount == obc)) {
							info += "��" + v.resourceManager.relocateCode.get(i) + "\r\n";
						}
						else {
							info +=v.resourceManager.relocateCode.get(i) + "\r\n";
						}
					}
					
					instruction.setText(info);
					
				}
			}
		};
		
		b2.addActionListener(event3);
		
		//�����ư
		ActionListener event4 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == b3) {
					try {
						v.resourceManager.closeDevice();//��ġ�� �ݾ���
					} catch (IOException e1) {
						e1.printStackTrace();
					}
					System.exit(0);
					
				}
			}
		};
		
		b3.addActionListener(event4);
		
		//���� ���� ��ư �̺�Ʈ ����
		ActionListener event5 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(e.getSource() == m1) {
					JFileChooser chooser = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter("text file","txt");
					chooser.setFileFilter(filter);
					
					int ret = chooser.showOpenDialog(null);
					if(ret != JFileChooser.APPROVE_OPTION) {
						JOptionPane.showMessageDialog(null, "������ �������� �ʾҽ��ϴ�.","���",JOptionPane.WARNING_MESSAGE);
					}
					fName = chooser.getSelectedFile().getPath();
					fileName.setText(fName);
				}
			}
		};
		
		m1.addActionListener(event5);
	}
	
	/**
	 * 10�� ������ 16���� ���ڿ��� ��ȯ
	 * @return 16���� ���ڿ�
	 */
	private String decimalToHex(int addr)
	{
		String res = "";
		String reres = "";
		int temp;
		int i;
		char c;
		while(addr != 0)
		{
			temp = addr % 16;
			if((temp >= 0) && (temp < 10))
			{
				c = (char)(48 + temp);
				res += c;
			}
			else
			{
				c = (char)(55 + temp);
				res += c;
			}
			addr = addr >> 4;
		}
		if(res.equals(""))
		{
			reres = "0";
		}
		else
		{
			for(i = res.length()-1; i >= 0; i--)
			{
				reres += res.charAt(i);
			}
		}
		return reres;
	}
	
	public JText(){
		super("SIC/XE Simulator");
		lb = new JLabel();//���� �̸� �κ�
		add(lb);
		
		mb = new JMenuBar();
		m0 = new JMenu("File");
		m1 = new JMenuItem("Open");
		m0.add(m1);
		mb.add(m0);
		setJMenuBar(mb);
		
		gbl = new GridBagLayout();
		gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;//������ �������� �÷���
		
		
		//�ؽ�Ʈ �ʵ� ����
		fileName = new JTextField(10);
		
		programName = new JTextField(10);
		startAddr = new JTextField(10);
		programLen = new JTextField(10);
		firstAddr = new JTextField(10);
		
		A1 =  new JTextField(6);
		A2 =  new JTextField(6);
		X1 =  new JTextField(6);
		X2 =  new JTextField(6);
		L1 =  new JTextField(6);
		L2 =  new JTextField(6);
		PC1 =  new JTextField(6);
		PC2 =  new JTextField(6);
		SW1 =  new JTextField(6);
		B1 =  new JTextField(6);
		B2 =  new JTextField(6);
		S1 =  new JTextField(6);
		S2 =  new JTextField(6);
		T1 =  new JTextField(6);
		T2 =  new JTextField(6);
		F1 =  new JTextField(6);
		
		memStart = new JTextField(6);
		memTarget  = new JTextField(6);
		instruction = new JTextArea(1,500);
		scroll  = new JScrollPane(instruction);
		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		
		machine = new JTextField(4);
		
		log = new JTextArea(1,500);
		log.setSize(300,300);
		scroll2  = new JScrollPane(log);
		scroll2.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		north =  new JPanel();//�� ���� ���� �ǳ�
		north1 =  new JPanel();
		nButton = new JButton("open");
		
		
		left = new JPanel();//���� ���� �ǳ�
		left1 = new JPanel();
		left2 = new JPanel();
		left3 = new JPanel();
		
		center = new JPanel();//�߰� ���� �ǳ�
		center1 = new JPanel();
		center2 = new JPanel();
		center3 = new JPanel();
		center4 = new JPanel();
		
		right = new JPanel();//���� ���� �ǳ� ����
		
		
		
		left.setLayout(new GridLayout(6,1));//��ġ ����
		
		left1.setLayout(new GridLayout(3,2));
		left2.setLayout(new GridLayout(5,5));
		left3.setLayout(new GridLayout(4,5));
		
		north.setLayout(new GridLayout(1,1));
		
		center.setLayout(null);
		center1.setLayout(new GridLayout(1,2));
		center2.setLayout(new GridLayout(2,2));
		center3.setLayout(new GridLayout(1,1));
		center4.setLayout(new GridLayout(1,1));
		
		right.setLayout(new GridLayout(2,1));
		
		
		
		//north����
		north1.add(new JLabel("FileName"));
		north1.add(fileName);
		north1.add(nButton);
		north.add(north1);
		
		//left1 ����
		left1.add(new JLabel("ProgramName:"));
		left1.add(programName);
		left1.add(new JLabel("StartAddress:"));
		left1.add(startAddr);
		left1.add(new JLabel("Program Length:"));
		left1.add(programLen);
		
		//left2����
		left2.add(new JLabel("A(#0)"));
		left2.add(new JLabel("DEC"));
		left2.add(A1);
		left2.add(new JLabel("HEX"));
		left2.add(A2);		
		left2.add(new JLabel("X(#1)"));
		left2.add(new JLabel("DEC"));
		left2.add(X1);
		left2.add(new JLabel("HEX"));
		left2.add(X2);
		left2.add(new JLabel("L(#2)"));
		left2.add(new JLabel("DEC"));
		left2.add(L1);
		left2.add(new JLabel("HEX"));
		left2.add(L2);
		left2.add(new JLabel("PC(#8)"));
		left2.add(new JLabel("DEC"));
		left2.add(PC1);
		left2.add(new JLabel("HEX"));
		left2.add(PC2);
		left2.add(new JLabel("SW(#9)"));
		left2.add(new JLabel("DEC"));
		left2.add(SW1);
		
		//left3����
		left3.add(new JLabel("B(#3)"));
		left3.add(new JLabel("DEC"));
		left3.add(B1);
		left3.add(new JLabel("HEX"));
		left3.add(B2);
		left3.add(new JLabel("S(#4)"));
		left3.add(new JLabel("DEC"));
		left3.add(S1);
		left3.add(new JLabel("HEX"));
		left3.add(S2);
		left3.add(new JLabel("T(#5)"));
		left3.add(new JLabel("DEC"));
		left3.add(T1);
		left3.add(new JLabel("HEX"));
		left3.add(T2);
		left3.add(new JLabel("F(#6)"));
		left3.add(new JLabel("DEC"));
		left3.add(F1);
			
		//center1�� ����
		center1.add(new JLabel("Address of First Instruction"));
		center1.add(firstAddr);
		
		//center2�� ����
		center2.add(new JLabel("Start Address in Memory"));
		center2.add(memStart);
		center2.add(new JLabel("Target Address"));
		center2.add(memTarget);
		
		//cneter3�� ����
		center3.add(scroll);
		
		//center4�� ����
		center4.add(new JLabel("������� ��ġ"));
		center4.add(machine);
		
		
		//���� �ǳ��� ����
		left.add(new JLabel("H (Header Record)"));
		left.add(left1);
		left.add(new JLabel("Register"));
		left.add(left2);
		left.add(new JLabel("Register(for XE)"));
		left.add(left3);
		
		//�߰� �ǳ��� ����
		temp = new JLabel("E(End Record)");
		temp.setBounds(50,0,100,100);
		center.add(temp);
		center1.setBounds(50,70,465,50);
		center.add(center1);
		temp = new JLabel("Address");
		temp.setBounds(50,150,100,100);
		center.add(temp);
		center2.setBounds(50,210,465,100);
		center.add(center2);
		temp = new JLabel("Instruction");
		temp.setBounds(50,330,100,100);
		center.add(temp);
		center3.setBounds(50,400,200,300);
		center.add(center3);
		center4.setBounds(280,400,220,50);
		center.add(center4);
		b1 = new JButton("����(1 step)");
		b2 = new JButton("����(All)");
		b3 = new JButton("����");
		b1.setBounds(310,500,140,40);
		center.add(b1);
		b2.setBounds(310,580,140,40);
		center.add(b2);
		b3.setBounds(310,660,140,40);
		center.add(b3);
		temp = new JLabel("Log(��ɾ� ���� ����)");
		temp.setBounds(600,0,200,100);
		center.add(temp);
		scroll2.setBounds(600,100,300,500);
		center.add(scroll2);
		
		
		//�ǳ��� ��ġ
		add(left,BorderLayout.WEST);
		add(north,BorderLayout.NORTH);
		add(center,BorderLayout.CENTER);
		
		//���̱� ����
		setSize(1300,900);
		setVisible(true);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
	}
	
}
