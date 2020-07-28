import java.util.ArrayList;

/**
 * ����ڰ� �ۼ��� ���α׷� �ڵ带 �ܾ�� ���� �� ��, �ǹ̸� �м��ϰ�, ���� �ڵ�� ��ȯ�ϴ� ������ �Ѱ��ϴ� Ŭ�����̴�. <br>
 * pass2���� object code�� ��ȯ�ϴ� ������ ȥ�� �ذ��� �� ���� symbolTable�� instTable�� ������ �ʿ��ϹǷ� �̸� ��ũ��Ų��.<br>
 * section ���� �ν��Ͻ��� �ϳ��� �Ҵ�ȴ�.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit ������ �������� ���� ���� */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/*�߰��κ� : �������� ��ȣ ����*/
	public static final char X = '1';
	public static final char A ='0';
	public static final char S ='4';
	public static final char T = '5';
	
	/*�߰��κ� : ���α׷� ������ ���̸� ����*/
	public int locLength;
	
	
	/* Token�� �ٷ� �� �ʿ��� ���̺���� ��ũ��Ų��. */
	SymbolTable symTab;
	InstTable instTab;
	
	
	/** �� line�� �ǹ̺��� �����ϰ� �м��ϴ� ����. */
	ArrayList<Token> tokenList;
	
	/**
	 * �ʱ�ȭ�ϸ鼭 symTable�� instTable�� ��ũ��Ų��.
	 * @param t1 : �ش� section�� ����Ǿ��ִ� symbol table
	 * @param instTab : instruction ���� ���ǵ� instTable
	 */
	public TokenTable(SymbolTable symTab, InstTable instTab) {
		this.symTab = symTab;
		this.instTab = instTab;
		this.locLength = 0;//��ū���̺��� ������ 0���� �ʱ�ȭ
		tokenList = new ArrayList<Token>();//��ū ����Ʈ ��ü�� �������
	}
	
	/**
	 * �Ϲ� ���ڿ��� �޾Ƽ� Token������ �и����� tokenList�� �߰��Ѵ�.
	 * @param line : �и����� ���� �Ϲ� ���ڿ�
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
	}
	
	/**
	 * �߰��κ�
	 * tokenList �պκп� �ִ� ��ū�� ����
	 */
	public void removeToken() {
		int size = tokenList.size();
		tokenList.remove(size-1);
	}
	
	/**
	 * tokenList���� index�� �ش��ϴ� Token�� �����Ѵ�.
	 * @param index
	 * @return : index��ȣ�� �ش��ϴ� �ڵ带 �м��� Token Ŭ����
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 �������� ����Ѵ�.
	 * instruction table, symbol table ���� �����Ͽ� objectcode�� �����ϰ�, �̸� �����Ѵ�.
	 * @param index
	 */
	public void makeObjectCode(int index){
		Instruction  searchInst;
		Token searchToken;
		searchToken = tokenList.get(index);
		String data = "";//�����Ǵ� ���� �ڵ带 ������ ����
		String data2 = "";//�����Ǵ� ���� �ڵ带 ������ ����(�� ���ڿ��� ���ľ� �� ��츸 ��� )
		int size;
		int i;
		if(searchToken.byteSize == 0)//�̷��� ���� �ڵ尡 �������� ����
		{
			searchToken.objectCode = data;
		}
		else if(searchToken.byteSize == -2)//WORD��ɾ� ó��
		{
			if(Character.isDigit(searchToken.operand[0].charAt(0)))
			{
				data = decimalToHex(Integer.parseInt(searchToken.operand[0]));
				size = data.length();
				for(i = 0; i < 6-size;i++)
				{
					data2 += '0';
				}
				data2 += data;
				//��ū�� ���� ����
				searchToken.objectCode = data2;
				searchToken.byteSize = 3;
			}
			else
			{
				//�����Ǿ��ִ� ���� ��� ���� �ڵ�� 0���� ����
				if((symTab.searchRef(searchToken.operand[0]) == 1) && (symTab.searchRef(searchToken.operand[1].substring(1)) == 1))
				{
					size = data.length();
					for(i = 0; i < 6-size;i++)
					{
						data2 += '0';
					}
					data2 += data;
					//��ū�� ���� ����
					searchToken.objectCode = data2;
					searchToken.byteSize = 3;
				}
			}
		}
		else if(searchToken.byteSize == -3)//BYTE��ɾ� ó��
		{
			char c;
			int cNum;
			if(searchToken.operand[0].charAt(0) == 'X')//16�����̸� �״�� ����
			{
				data += searchToken.operand[0].substring(2,searchToken.operand[0].length()-1);
				searchToken.byteSize = data.length()/2;
				searchToken.objectCode = data;
			}
			else if(searchToken.operand[0].charAt(0) == 'C')//���ڿ��̸� �ƽ�Ű�ڵ� ���·� �ٲپ� ����
			{
				for(i = 2; i <searchToken.operand[0].length()-1; i++)
				{
					c = searchToken.operand[0].charAt(i);
					cNum = (int)c;
					data = decimalToHex(cNum);
					data2 += data;
					searchToken.objectCode = data2;
					searchToken.byteSize = data2.length()/2;	
				}
				
			}
		}
		else if(searchToken.byteSize == -4)//2������ ���
		{
			searchInst = instTab.search(searchToken.operator);
			data += decimalToHex(searchInst.opcode);
			//��ɾ��ڵ� ����
			for(i = data.length() ; i < 2; i++)
			{
				data2 += '0';
			}
			data2 += data;
			//�������� �κ� ���
			if(searchToken.operand[0].equals(""))
			{
				data2 += '0';
			}
			else if(searchToken.operand[0].equals("X"))
			{
				data2 += TokenTable.X;
			}
			else if(searchToken.operand[0].equals("A"))
			{
				data2 += TokenTable.A;
			}
			else if(searchToken.operand[0].equals("S"))
			{
				data2 += TokenTable.S;
			}
			else if(searchToken.operand[0].equals("T"))
			{
				data2 += TokenTable.T;
			}
			
			if(searchToken.operand[1].equals(""))
			{
				data2 += '0';
			}
			else if(searchToken.operand[1].equals("X"))
			{
				data2 += TokenTable.X;
			}
			else if(searchToken.operand[1].equals("A"))
			{
				data2 += TokenTable.A;
			}
			else if(searchToken.operand[1].equals("S"))
			{
				data2 += TokenTable.S;
			}
			else if(searchToken.operand[1].equals("T"))
			{
				data2 += TokenTable.T;
			}
			searchToken.objectCode = data2;
			searchToken.byteSize = 2;
		}
		else if(searchToken.byteSize == -5)//3������ ���
		{
			
			//���� �ڵ� ����
			int instOp = 0;//��ɾ� �ڵ尪�� ����
			int charTemp = 0;//���ڿ��� �� �� �ܾ��� ������ ����(xbpe�� ���� ����)
			int Gap= 0;//�ּҰ��� ���̸� ����
			searchInst = instTab.search(searchToken.operator);
			instOp = searchInst.opcode;
			//��ɾ��ڵ� ����
			//nixbpe��Ʈ ��ġ
			if(searchToken.getFlag(TokenTable.nFlag) != 0)
			{
				instOp += 2;
			}
			
			if(searchToken.getFlag(TokenTable.iFlag) != 0)
			{
				instOp+=1;
			}
			
			data = decimalToHex(instOp);
			for(i = data.length() ; i < 2; i++)
			{
				data2 += '0';
			}
			data2 += data;
			
			if(searchToken.getFlag(TokenTable.xFlag) != 0)
			{
				charTemp += 8;
			}		
			if(searchToken.getFlag(TokenTable.bFlag) != 0)
			{
				charTemp += 4;
			}
			if(searchToken.getFlag(TokenTable.pFlag) != 0)
			{
				charTemp += 2;
			}
			if(searchToken.getFlag(TokenTable.eFlag) != 0)
			{
				charTemp += 1;
			}
			data = decimalToHex(charTemp);
			data2 += data;
			Gap = searchToken.locGap;
			if(Gap >= 0)//�ּҰ� ���̰� 0�̻��̶��
			{
				data = decimalToHex(Gap);
				for(i = data.length() ; i < 3; i++)
				{
					data2 += '0';
				}
				data2 += data;
			}
			else//������� ����ó��
			{
				int num;
				num = (16*16*15) + (16*15) + 16;
				num = num + Gap;
				data = decimalToHex(num);
				for(i = data.length() ; i < 3; i++)
				{
					data2 += '0';
				}
				data2 += data;
			}
			searchToken.objectCode = data2;
			searchToken.byteSize = 3;
		}
		else if(searchToken.byteSize == -6)
		{
			int instOp = 0;//��ɾ� �ڵ尪�� ����
			int operandNum = 0;//�ǿ������� ���� ����
			searchInst = instTab.search(searchToken.operator);
			instOp = searchInst.opcode;
			//��ɾ��ڵ� ����
			//nixbpe��Ʈ ��ġ
			if(searchToken.getFlag(TokenTable.nFlag) != 0)
			{
				instOp += 2;
			}
			
			if(searchToken.getFlag(TokenTable.iFlag) != 0)
			{
				instOp+=1;
			}
			data = decimalToHex(instOp);
			for(i = data.length() ; i < 2; i++)
			{
				data2 += '0';
			}
			data2 += data;
			data2 += '0';
			operandNum = Integer.parseInt(searchToken.operand[0].substring(1));
			data = decimalToHex(operandNum);
			for(i = data.length() ; i < 3; i++)
			{
				data2 += '0';
			}
			data2 += data;
			searchToken.objectCode = data2;
			searchToken.byteSize = 3;		
		}
		else if(searchToken.byteSize == -7)
		{
			int instOp = 0;//��ɾ� �ڵ尪�� ����
			int operandNum = 0;//�ǿ������� ���� ����
			searchInst = instTab.search(searchToken.operator);
			instOp = searchInst.opcode;
			//��ɾ��ڵ� ����
			//nixbpe��Ʈ ��ġ
			if(searchToken.getFlag(TokenTable.nFlag) != 0)
			{
				instOp += 2;
			}
			
			if(searchToken.getFlag(TokenTable.iFlag) != 0)
			{
				instOp+=1;
			}
			data = decimalToHex(instOp);
			for(i = data.length() ; i < 2; i++)
			{
				data2 += '0';
			}
			data2 += data;
			for(i = 0 ; i < 4; i++)
			{
				data2 += '0';
			}
			searchToken.objectCode = data2;
			searchToken.byteSize = 3;
			
		}
		else if(searchToken.byteSize == -8)
		{
			int instOp = 0;//��ɾ� �ڵ尪�� ����
			int charTemp = 0;//���ڿ��� �� �� �ܾ��� ������ ����(xbpe�� ���� ����)
			searchInst = instTab.search(searchToken.operator.substring(1));
			instOp = searchInst.opcode;
			int Gap= 0;//�ּҰ��� ���̸� ����
			//��ɾ��ڵ� ����
			//nixbpe��Ʈ ��ġ
			if(searchToken.getFlag(TokenTable.nFlag) != 0)
			{
				instOp += 2;
			}
			
			if(searchToken.getFlag(TokenTable.iFlag) != 0)
			{
				instOp+=1;
			}
			data = decimalToHex(instOp);
			for(i = data.length() ; i < 2; i++)
			{
				data2 += '0';
			}
			data2 += data;
			if(searchToken.getFlag(TokenTable.xFlag) != 0)
			{
				charTemp += 8;
			}		
			if(searchToken.getFlag(TokenTable.bFlag) != 0)
			{
				charTemp += 4;
			}
			if(searchToken.getFlag(TokenTable.pFlag) != 0)
			{
				charTemp += 2;
			}
			if(searchToken.getFlag(TokenTable.eFlag) != 0)
			{
				charTemp += 1;
			}
			data = decimalToHex(charTemp);
			data2 += data;
			
			//������ ���� �ּҿ� ���� ������ ����
			Gap = searchToken.locGap;
			if(Gap >= 0)//�ּҰ� ���̰� 0�̻��̶��
			{
				data = decimalToHex(Gap);
				for(i = data.length() ; i < 5; i++)
				{
					data2 += '0';
				}
				data2 += data;
			}
			else//������� ����ó��
			{
				int num;
				num = (16*16*15) + (16*15) + 16;
				num = num + Gap;
				data = decimalToHex(num);
				for(i = data.length() ; i < 5; i++)
				{
					data2 += '0';
				}
				data2 += data;
			}
			searchToken.objectCode = data2;
			searchToken.byteSize = 4;		
		}
		else if(searchToken.byteSize == -9)
		{
			String temp;
			if(searchToken.operator.charAt(1) == 'C')//���ڿ������� ���ͷ��� ���
			{
				temp = searchToken.operator.substring(3,searchToken.operator.length()-1);
				for(i = 0 ; i < temp.length(); i++)//���ڿ��� �ִ� ���� �ϳ��ϳ����� �˻�
				{
					data = decimalToHex(temp.charAt(i));
					data2 +=data; 
				}
				searchToken.objectCode = data2;
				searchToken.byteSize = data2.length()/2;
				
			}
			else if(searchToken.operator.charAt(1) == 'X')//16���� ������ ���ͷ��� ���
			{
				data = searchToken.operator.substring(3,searchToken.operator.length()-1);
				searchToken.objectCode = data;
				searchToken.byteSize = data.length()/2;
			}
		}
	}
	

	/**
	 * �ش� �ּҰ��� 16���� ���·� ���
	 * @param addr : 16���� ���·� �ٲ� 10���� �ּҰ� 
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
	
	/** 
	 * index��ȣ�� �ش��ϴ� object code�� �����Ѵ�.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
}

/**
 * �� ���κ��� ����� �ڵ带 �ܾ� ������ ������ ��  �ǹ̸� �ؼ��ϴ� ���� ���Ǵ� ������ ������ �����Ѵ�. 
 * �ǹ� �ؼ��� ������ pass2���� object code�� �����Ǿ��� ���� ����Ʈ �ڵ� ���� �����Ѵ�.
 */
class Token{
	//�ǹ� �м� �ܰ迡�� ���Ǵ� ������
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code ���� �ܰ迡�� ���Ǵ� ������ 
	String objectCode;
	int byteSize;
	
	//�߰��κ�
	int locGap;//���� ��ū�� pc���¿� ��ǥ�ּҰ��� ���̸� ����(������Ʈ �ڵ� ������ ���)
	
	/**
	 * Ŭ������ �ʱ�ȭ �ϸ鼭 �ٷ� line�� �ǹ� �м��� �����Ѵ�. 
	 * @param line ��������� ����� ���α׷� �ڵ�
	 */
	public Token(String line) {
		//initialize �߰�
		this.label = "";
		this.operator ="";
		this.operand = new String[TokenTable.MAX_OPERAND];
		this.operand[0] = "";
		this.operand[1] = "";
		this.operand[2] = "";
		this.comment = "";
		this.nixbpe = 0;
		this.location = 0;
		this.objectCode = "";
		this.byteSize = -1;
		
		this.locGap = 0;
		
		parsing(line);
	}
	
	/**
	 * line�� �������� �м��� �����ϴ� �Լ�. Token�� �� ������ �м��� ����� �����Ѵ�.
	 * @param line ��������� ����� ���α׷� �ڵ�.
	 */
	public void parsing(String line) {
		int i;
		int length;
		int size;
		String[] s = line.split("\t");
		size = s.length;
		
		if(size >= 1)
		{
			if(!s[0].equals("\t"))//label�κ� ���� 
			{
				this.label = s[0];
			}
		}
		
		if(size >= 2)
		{
			if(!s[1].equals("\t"))//������ �κ� ����
			{
				this.operator = s[1];
			}
		}
		
		if(size >= 3)
		{
			if(!s[2].equals("\t"))//�ǿ����ڸ� ����
			{
				int check = 0;
				char temp;
				String buff = "";
				length = s[2].length();
				for(i = 0 ; i < length; i++)//�ǿ������� ������ŭ �и����� �ֱ� ���ؼ� ���ڰ� ','Ȥ�� �ٸ� ���������� Ȯ�����ִ� �۾�
				{
					temp = s[2].charAt(i);
					if(s[2].charAt(i) == ',')
					{
						this.operand[check++] = buff;
						buff = "";
					}
					else if(s[2].charAt(i) == '-')
					{
						this.operand[check++] = buff;
						buff = "";
						buff += temp;
					}
					else
					{
						buff += temp;
					}
				}
				this.operand[check] = buff;
			}
		}
		
		if(size >= 4)
		{
			if(!s[3].equals("\t"))//COMMENT�κ� ���� 
			{
				this.comment = s[3];
			}
		}
	}
	
	/** 
	 * n,i,x,b,p,e flag�� �����Ѵ�. <br><br>
	 * 
	 * ��� �� : setFlag(nFlag, 1); <br>
	 *   �Ǵ�     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : ���ϴ� ��Ʈ ��ġ
	 * @param value : ����ְ��� �ϴ� ��. 1�Ǵ� 0���� �����Ѵ�.
	 */
	public void setFlag(int flag, int value) {
		if(value == 1)
		{
			this.nixbpe += flag;
		}
		else
		{
			this.nixbpe += 0;
		}
	}
	
	/**
	 * ���ϴ� flag���� ���� ���� �� �ִ�. flag�� ������ ���� ���ÿ� �������� �÷��׸� ��� �� ���� �����ϴ� <br><br>
	 * 
	 * ��� �� : getFlag(nFlag) <br>
	 *   �Ǵ�     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : ���� Ȯ���ϰ��� �ϴ� ��Ʈ ��ġ
	 * @return : ��Ʈ��ġ�� �� �ִ� ��. �÷��׺��� ���� 32, 16, 8, 4, 2, 1�� ���� ������ ����.
	 */
	public int getFlag(int flags) {
		return this.nixbpe & flags;
	}
	
	
}
