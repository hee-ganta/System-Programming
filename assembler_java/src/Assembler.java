import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * Assembler : 
 * �� ���α׷��� SIC/XE �ӽ��� ���� Assembler ���α׷��� ���� ��ƾ�̴�.
 * ���α׷��� ���� �۾��� ������ ����. <br>
 * 1) ó�� �����ϸ� Instruction ���� �о�鿩�� assembler�� �����Ѵ�. <br>
 * 2) ����ڰ� �ۼ��� input ������ �о���� �� �����Ѵ�. <br>
 * 3) input ������ ������� �ܾ�� �����ϰ� �ǹ̸� �ľ��ؼ� �����Ѵ�. (pass1) <br>
 * 4) �м��� ������ �������� ��ǻ�Ͱ� ����� �� �ִ� object code�� �����Ѵ�. (pass2) <br>
 * 
 * <br><br>
 * �ۼ����� ���ǻ��� : <br>
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� �ȵȴ�.<br>
 *  2) ���������� �ۼ��� �ڵ带 �������� ������ �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br><br>
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */

public class Assembler {
	/** instruction ���� ������ ���� */
	InstTable instTable;
	/** �о���� input ������ ������ �� �� �� �����ϴ� ����. */
	ArrayList<String> lineList;
	/** ���α׷��� section���� symbol table�� �����ϴ� ����*/
	ArrayList<SymbolTable> symtabList;
	/** ���α׷��� section���� ���α׷��� �����ϴ� ����*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, �Ǵ� ���þ ���� ������� ������Ʈ �ڵ���� ��� ���·� �����ϴ� ����. <br>
	 * �ʿ��� ��� String ��� ������ Ŭ������ �����Ͽ� ArrayList�� ��ü�ص� ������.
	 */
	ArrayList<String> codeList;
	
	/**�߰��κ� : ���α׷��� section�� ����� ������ ����*/
	static int section = 0;
	
	/**
	 * Ŭ���� �ʱ�ȭ. instruction Table�� �ʱ�ȭ�� ���ÿ� �����Ѵ�.
	 * 
	 * @param instFile : instruction ���� �ۼ��� ���� �̸�. 
	 * @throws IOException 
	 */
	public Assembler(String instFile) throws IOException {
		instTable = new InstTable(instFile);
		lineList = new ArrayList<String>();
		symtabList = new ArrayList<SymbolTable>();
		TokenList = new ArrayList<TokenTable>();
		codeList = new ArrayList<String>();
	}

	/** 
	 * ��U���� ���� ��ƾ
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		Assembler assembler = new Assembler("inst.data");
		assembler.loadInputFile("input.txt");
		
		
		
		assembler.pass1();
		assembler.printSymbolTable("symtab_20151812.txt");
		
		assembler.pass2();
		assembler.printObjectCode("output_20151812.txt");
		
	}

	/**
	 * �ۼ��� codeList�� ������¿� �°� ����Ѵ�.<br>
	 * @param fileName : ����Ǵ� ���� �̸�
	 * @throws IOException 
	 */
	private void printObjectCode(String fileName) throws IOException {
		int i,j,k;
		Path path = Paths.get(fileName);
		String data = "";
		String data2 = "";
		FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.WRITE);
		Charset charset = Charset.forName("UTF-8");
		SymbolTable searchSymbol;//�ش� ��ū ���̺��� �ɺ����̺��� �޾ƿ��� ����
		ArrayList<Token> refInfo = new ArrayList<Token>();//Modification record�ۼ��� �ʿ��� ��ū���� ������ ����
		int countSection = 0;
		int startCheck = 0;//������Ʈ �ڵ� �� ������ ���ۺκ����� �ƴ����� ����
		int codeLength = 0;//������Ʈ�ڵ� �� ���� ���̸� ����
		int startAddr = 0;//���α׷� ������ ���۵Ǵ� �ּҿ� ���� ������ ���� 
		while(countSection <= section)
		{
			startAddr = 0;
			startCheck = 0;
			codeLength = 0;
			TokenTable searchTokenTable;
			Token searchToken;
			searchTokenTable = TokenList.get(countSection);
			searchToken = searchTokenTable.getToken(0);
			searchSymbol = searchTokenTable.symTab;
			//Header record�κ�
			data2 += 'H';
			data2 += searchToken.label;
			for(i = data2.length() ; i <7; i++)//���ڸ���ŭ ���鹮�ڷ� ä��
			{
				data2 += " ";
			}
			
			if(!searchToken.operand[0].equals(""))//�����ּҰ� ������ �ش� ������ ����
			{
				startAddr = Integer.parseInt(searchToken.operand[0]);
			}
			else//�׷��� �ʴٸ� 0�� ����
			{
				startAddr = 0;
			}
			
			data = decimalToHex(startAddr);
			for(i = data.length() ; i <6; i++)//���ڸ���ŭ 0���� ä��
			{
				data2 += '0';
			}
			data2 += data;
			
			data = decimalToHex(searchTokenTable.locLength);
			for(i = data.length() ; i <6; i++)//���ڸ���ŭ 0���� ä��
			{
				data2 += '0';
			}
			data2 += data;
			data2 += "\r\n";//�� ���� �����
			codeList.add(data2); //Head record����
			data = "";//�ۼ��� ���ڿ� �ʱ�ȭ
			data2 ="";
			
			//Define record, Refer record�κ�			
			for(i = 0; i < searchTokenTable.symTab.defList.size(); i++)
			{
				if(i == 0)
				{
					data2 += 'D';
				}
				int defAddr = 0;//���� �ɺ��� ���� �ּҰ��� �޾ƿ��� ����
				data = searchTokenTable.symTab.defList.get(i);
				data2 += data;
				for(j = data.length(); j < 6; j++)//�ڸ����� ���߱� ���� �۾�
				{
					data2  += " ";
				}
				defAddr = searchTokenTable.symTab.search(data);
				data = decimalToHex(defAddr);
				for(j = data.length(); j < 6; j++)//�ڸ����� ���߱� ���� �۾�
				{
					data2  += '0';
				}
				data2 += data;
			}
			if(i >= 1)
			{
				data2 += "\r\n";//�� ���� �����
				codeList.add(data2); //Head record����
				data = "";//�ۼ��� ���ڿ� �ʱ�ȭ
				data2 = "";
			}
			
			for(i = 0; i < searchTokenTable.symTab.refList.size(); i++)
			{
				if(i ==0)
				{
					data2 += 'R';
				}
				data = searchTokenTable.symTab.refList.get(i);
				data2 += data;
				for(j = data.length(); j < 6; j++)//�ڸ����� ���߱� ���� �۾�
				{
					data2  += " ";
				}
			}
			if(i >= 1)
			{
				data2 += "\r\n";//�� ���� �����
				codeList.add(data2); //Head record����
				data = "";//�ۼ��� ���ڿ� �ʱ�ȭ
				data2 = "";
			}
			
			//Text record
			for(i = 0 ; i< searchTokenTable.tokenList.size();i++)
			{
				
				searchToken = searchTokenTable.tokenList.get(i);
				if((searchSymbol.searchRef(searchToken.operand[0]) == 1) && (!searchToken.operator.equals("EXTREF")))//�ǿ����ڰ� ���� �ɺ��̶�� �ش� ������ ����
				{
					refInfo.add(searchToken);
				}
				
				if(searchToken.objectCode.equals(""))//��� ������ ������� ����
				{
					/*Text record�� ������ ����(RESW,RESB��ɾ��� ���)*/
					if( searchToken.operator.equals("RESW") || searchToken.operator.equals("RESB"))
					{
						if(!data2.equals(""))
						{
							String temp1= "";
							String temp2 = "";
							String len = "";
							String len2 = "";
							len = decimalToHex(codeLength);
							for(j = len.length(); j < 2; j++)
							{
								len2 += '0';
							}
							data2 += "\r\n";//�ٹٲ�
							len2 += len;
							temp1 = data2.substring(0,7);
							temp2 = data2.substring(7);
							data2 = "";
							data2 = temp1 + len2 + temp2;
							codeLength = 0;//�渮 ����
							startCheck = 0;//������ �ٲ���
							codeList.add(data2);
							data = "";//�ۼ��� ���ڿ� �ʱ�ȭ
							data2 ="";
						}
					}
					continue;
				}
				if(startCheck == 0)//�ؽ�Ʈ ���ڵ��� ���� �κ�
				{
					startCheck++;
					data2 += 'T';
					data = decimalToHex(searchToken.location);//�����ּҸ� ����
					for(j = data.length(); j < 6; j++)
					{
						data2 += '0';
					}
					data2 += data;//������ �ڵ��� ����
					data2 += searchToken.objectCode;
					codeLength += searchToken.byteSize;//���̸� ī��Ʈ
				}
				else if(startCheck == 1)
				{
					
					
					codeLength += searchToken.byteSize;//���̸� ī��Ʈ
					/*Text record�� ������ ����(���̰� 30����)*/
					if(codeLength >= 30 )
					{
						String len = "";
						String len2 = "";
						String temp1 ="";
						String temp2 ="";
						len = decimalToHex(codeLength - searchToken.byteSize);
						for(j = len.length(); j < 2; j++)
						{
							len2 += '0';
						}
						len2 += len;
						data2 += "\r\n";//�ٹٲ�
						//�ڵ��� ���� �κ��� �ִ� �۾�
						temp1 = data2.substring(0,7);
						temp2 = data2.substring(7);
						data2 = "";
						data2 = temp1 + len2 + temp2;
						codeLength = 0;//�渮 ����
						startCheck = 0;//������ �ٲ���
						codeList.add(data2);
						data = "";//�ۼ��� ���ڿ� �ʱ�ȭ
						data2 ="";
						i--;
						
					}
					else
					{
						data2 += searchToken.objectCode;
					}
				}
				
			}
			if(!data2.equals(""))
			{
				String temp1= "";
				String temp2 = "";
				String len = "";
				String len2 = "";
				len = decimalToHex(codeLength);
				for(j = len.length(); j < 2; j++)
				{
					len2 += '0';
				}
				len2 += len;
				data2 += "\r\n";//�ٹٲ�
				temp1 = data2.substring(0,7);
				temp2 = data2.substring(7);
				data2 = "";
				data2 = temp1 + len2 + temp2;
				codeLength = 0;//�渮 ����
				codeList.add(data2);
				data = "";//�ۼ��� ���ڿ� �ʱ�ȭ
				data2 = "";
			}
			
			//Modification record�ۼ�
			for(i = 0 ; i < refInfo.size(); i++)
			{
				if(!refInfo.get(i).operator.equals("WORD"))
				{
					for(j = 0 ; j < 3; j++)
					{
						String check = refInfo.get(i).operand[j];
						if((!check.equals("")) &&(!check.equals("X")))
						{
							data2 += 'M';
							data = decimalToHex(refInfo.get(i).location+1);
							for(k = data.length(); k < 6; k++)
							{
								data2 += '0';
							}
							data2 += data;
							data2 += "05";
							if(j == 0)
							{
								data2 += '+';
								data2 += refInfo.get(i).operand[j];
							}
							else
							{
								data2 += refInfo.get(i).operand[j].charAt(0);
								data2 += refInfo.get(i).operand[j];
							}
							data2 += "\r\n";
							codeList.add(data2);
							data = "";//�ۼ��� ���ڿ� �ʱ�ȭ
							data2 = "";
						}
					}
				}
				else
				{
					for(j = 0 ; j < 3; j++)
					{
						String check = refInfo.get(i).operand[j];
						if((!check.equals("")) &&(!check.equals("X")))
						{
							data2 += 'M';
							data = decimalToHex(refInfo.get(i).location);
							for(k = data.length(); k < 6; k++)
							{
								data2 += '0';
							}
							data2 += data;
							data2 += "06";
							if(j == 0)
							{
								data2 += '+';
								data2 += refInfo.get(i).operand[j];
							}
							else
							{
								data2 += refInfo.get(i).operand[j].charAt(0);
								data2 += refInfo.get(i).operand[j].substring(1);
							}
							data2 += "\r\n";
							codeList.add(data2);
							data = "";//�ۼ��� ���ڿ� �ʱ�ȭ
							data2 = "";
						}
					}
				}
			}
			refInfo.clear();
			
			//End record�κ�
			if(searchTokenTable.getToken(0).operator.equals("START"))//���α׷��� ���ۺκп� START��ɾ ������ ���ڵ� �κп� ���α׷� �ּҸ� ���� ���
			{
				data2 += 'E';
				data = decimalToHex(Integer.parseInt(searchTokenTable.getToken(0).operand[0]));
				for(i = data.length(); i < 6 ; i++)//���ڸ��� 0���� ä��� �۾�
				{
					data2 += '0';
				}
				data2 += data;
				data2 += "\r\n\r\n";
				codeList.add(data2);
				data = "";//�ۼ��� ���ڿ� �ʱ�ȭ
				data2 = "";
			}
			else//���α׷��� ���ۺκп� START��ɾ ������  ������ ���α׷� ���� �ּҰ� �����Ƿ� ������� ���� 
			{
				data2 += 'E';
				data2 += data;
				data2 += "\r\n\r\n";
				codeList.add(data2);
				data = "";//�ۼ��� ���ڿ� �ʱ�ȭ
				data2 = "";
			}
			countSection++;
		}
		
		for(i = 0 ; i < codeList.size(); i++)
		{
			ByteBuffer buffer = ByteBuffer.allocateDirect(codeList.get(i).length());
			buffer = charset.encode(codeList.get(i));
			fileChannel.write(buffer);
		}
		fileChannel.close();//������ ����
	}
	
	
	/**
	 * �ۼ��� SymbolTable���� ������¿� �°� ����Ѵ�.<br>
	 * @param fileName : ����Ǵ� ���� �̸�
	 * @throws IOException 
	 */
	private void printSymbolTable(String fileName) throws IOException {
		int i,j;
		Path path = Paths.get(fileName);//������ ��θ� ����
		String data = "";//���Ͽ��� �� ���ڿ��� ���� ����
		
		FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.WRITE);
		Charset charset = Charset.forName("UTF-8");
		//���α׷� ���Ǻ��� �ɺ����̺� ������ ���Ͽ� ���	
		for(i = 0 ; i <= section; i++)
		{
			for(j = 0 ; j <symtabList.get(i).symbolList.size();j++)
			{
				//�ɺ��� �ּҰ��� ���Ͽ��ٰ� ���� �۾�(�ɺ��� ���� ������)
				if((!symtabList.get(i).symbolList.get(j).equals("")) && (symtabList.get(i).symbolList.get(j).charAt(0) != '='))
				{
					data += symtabList.get(i).symbolList.get(j);
					data += "\t\t";
					data += decimalToHex(symtabList.get(i).locationList.get(j)) + "\r\n";
					ByteBuffer buffer = ByteBuffer.allocateDirect(data.length());
					buffer = charset.encode(data);
					fileChannel.write(buffer);
					data = "";//�����͸� ���� ���� ����
				}
				
				if(j == symtabList.get(i).symbolList.size()-1)//���α׷��� ���̸� ������ �����
				{
					data += "\r\n";
					ByteBuffer buffer = ByteBuffer.allocateDirect(data.length());
					buffer = charset.encode(data);
					fileChannel.write(buffer);
					data = "";//�����͸� ���� ���� ����
				}
			}
		}
		
		
		fileChannel.close();
		
	}
	
	/**
	 * �߰� �޼ҵ� �κ�
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
	 * pass1 ������ �����Ѵ�.<br>
	 *   1) ���α׷� �ҽ��� ��ĵ�Ͽ� ��ū������ �и��� �� ��ū���̺� ����<br>
	 *   2) label�� symbolTable�� ����<br>
	 *   <br><br>
	 *    ���ǻ��� : SymbolTable�� TokenTable�� ���α׷��� section���� �ϳ��� ����Ǿ�� �Ѵ�.
	 */
	private void pass1() {
		
		
		//������ �ɺ� ���̺���  �ۼ��ϴ� ����
		SymbolTable s1 = new SymbolTable();
		SymbolTable s2 = new SymbolTable();
		SymbolTable s3 = new SymbolTable();
		symtabList.add(s1);
		symtabList.add(s2);
		symtabList.add(s3);
		
		
		//������ ��ū���̺��� �ۼ��ϴ� ����
		TokenTable t1 = new TokenTable(s1,instTable);
		TokenTable t2 = new TokenTable(s2,instTable);
		TokenTable t3 = new TokenTable(s3,instTable);
		TokenList.add(t1);
		TokenList.add(t2);
		TokenList.add(t3);
		
		
		int i,j;
		int size = lineList.size();
		int tokenIndex;
		Token tokenSearch;
		TokenTable searchTokenTable;
		int locctr = 0; // �ּҰ��� ���� ����
		//�޾ƿ� �ҽ� ���ζ��θ��� �м��� �� ��
		
		for(i = 0 ; i < size; i++)
		{
			//�����ϰ��� �ϴ� ��ū ���̺�
			searchTokenTable = TokenList.get(section);
			//�ҽ��ڵ� ���θ��� ��ūȭ�Ͽ� ����
			TokenList.get(section).putToken(lineList.get(i));
			tokenIndex = TokenList.get(section).tokenList.size();
			tokenSearch = TokenList.get(section).getToken(tokenIndex-1);//���� �޾ƿ� ��ū�� ���ؼ� ����
				
			if(tokenSearch.operator.equals("START"))//START��ɾ��� ���
			{
				symtabList.get(section).putSymbol(tokenSearch.label,Integer.parseInt(tokenSearch.operand[0]));
				tokenSearch.location = Integer.parseInt(tokenSearch.operand[0]);
				continue;
			}
			else if(tokenSearch.operator.equals("END"))//END��ɾ��� ���
			{
				int lSize = symtabList.get(section).literalList.size();
				symtabList.get(section).putSymbol(tokenSearch.label,-1);
				tokenSearch.location = -1;
				int num;//locctr�ּҰ��� ������ �� ������� ������ų�� �Ǵ� �� �� ���� ����
				for(j = 0; j < lSize; j++)//�̶����� ������ ���ͷ����� �ɺ����̺� �����ϰ� �ش� token�� ����� �ش�
				{
					String tempLine = symtabList.get(section).literalList.get(j) + "\t" + symtabList.get(section).literalList.get(j) + "\t" + "\t";
					TokenList.get(section).putToken(tempLine);
					tokenIndex = TokenList.get(section).tokenList.size();
					tokenSearch = TokenList.get(section).getToken(tokenIndex-1);//���� �޾ƿ� ��ū�� ���ؼ� ����
					symtabList.get(section).putSymbol(symtabList.get(section).literalList.get(j),locctr);
					tokenSearch.location = locctr;
					//locctr�����κ�
					if(symtabList.get(section).literalList.get(j).charAt(1) == 'X')
					{
						num = symtabList.get(section).literalList.get(j).length();
						num -= 4;
						locctr += (int)num/2;
						searchTokenTable.locLength += (int)num/2;
					}
					else if(symtabList.get(section).literalList.get(j).charAt(1) == 'C')
					{
						num = symtabList.get(section).literalList.get(j).length();
						num -= 4;
						locctr += num;
						searchTokenTable.locLength += num;
					}
				}
				symtabList.get(section).literalList.clear();//���ͷ� ����Ʈ�� �����

				continue;
			}
			else if(tokenSearch.operator.equals("EXTDEF"))//EXTDEF��ɾ��� ���
			{
				symtabList.get(section).putSymbol(tokenSearch.label,-1);//�ּҰ��� �����Ƿ� -1����
				tokenSearch.location = -1;
				if(!tokenSearch.operand[0].equals(""))//�ɺ��� ������ ���� �ɺ� ���̺� ����
				{
					symtabList.get(section).putDef(tokenSearch.operand[0]);
				}
					
				if(!tokenSearch.operand[1].equals(""))//�ɺ��� ������ ���� �ɺ� ���̺� ����
				{
					symtabList.get(section).putDef(tokenSearch.operand[1]);
				}
					
				if(!tokenSearch.operand[2].equals(""))//�ɺ��� ������ ���� �ɺ� ���̺� ����
				{
					symtabList.get(section).putDef(tokenSearch.operand[2]);
				}

				continue;
			}
			else if(tokenSearch.operator.equals("EXTREF"))//EXTREF��ɾ��� ���
			{
				symtabList.get(section).putSymbol(tokenSearch.label,-1);//�ּҰ��� �����Ƿ� -1����
				tokenSearch.location = -1;
				if(!tokenSearch.operand[0].equals(""))//�ɺ��� ������ ���� �ɺ� ���̺� ����
				{
					symtabList.get(section).putRef(tokenSearch.operand[0]);
				}
					
				if(!tokenSearch.operand[1].equals(""))//�ɺ��� ������ ���� �ɺ� ���̺� ����
				{
					symtabList.get(section).putRef(tokenSearch.operand[1]);
				}
				
				if(!tokenSearch.operand[2].equals(""))//�ɺ��� ������ ���� �ɺ� ���̺� ����
				{
					symtabList.get(section).putRef(tokenSearch.operand[2]);
				}

				continue;
			}
			else if(tokenSearch.operator.equals("RESW"))//RESW��ɾ��� ��� 
			{
				symtabList.get(section).putSymbol(tokenSearch.label,locctr);//�ش� �ּҰ� ����
				tokenSearch.location = locctr;
				locctr += 3 * Integer.parseInt(tokenSearch.operand[0]);//�ּҰ� ����
				searchTokenTable.locLength += 3* Integer.parseInt(tokenSearch.operand[0]);
				continue;
			}
			else if(tokenSearch.operator.equals("RESB"))//RESB��ɾ��� ��� 
			{
				symtabList.get(section).putSymbol(tokenSearch.label,locctr);//�ش� �ּҰ� ����
				tokenSearch.location = locctr;
				locctr += 1 * Integer.parseInt(tokenSearch.operand[0]);//�ּҰ� ����
				searchTokenTable.locLength += 1 * Integer.parseInt(tokenSearch.operand[0]);
				continue;
			}
			else if(tokenSearch.operator.equals("BYTE"))//�պ��� �� �κ� 
			{
				symtabList.get(section).putSymbol(tokenSearch.label,locctr);//�ش� �ּҰ� ����
				tokenSearch.location = locctr;
				if(tokenSearch.operand[0].charAt(0) == 'X')//16���� ���¶��
				{
					int num = tokenSearch.operand[0].length();
					num -= 3;
					locctr += (int)num/2;
					searchTokenTable.locLength += (int)num/2;
				}
				else if(tokenSearch.operand[0].charAt(0) == 'C')//���ڿ� ���¶��
				{
					int num = tokenSearch.operand[0].length();
					num -= 3;
					locctr += num;
					searchTokenTable.locLength += num;
				}
				continue;
			}
			else if(tokenSearch.operator.equals("WORD"))//WORD��ɾ��� ���
			{
				symtabList.get(section).putSymbol(tokenSearch.label,locctr);//�ش� �ּҰ� ����
				tokenSearch.location = locctr;
				locctr += 3;//�ּҰ� ����
				searchTokenTable.locLength += 3;
				continue;
			}
			else if(tokenSearch.operator.equals("LTORG"))//�պ��� �� �κ� 
			{
				int lSize = symtabList.get(section).literalList.size();
				symtabList.get(section).putSymbol(tokenSearch.label,-1);
				tokenSearch.location = -1;
				int num;//locctr�ּҰ��� ������ �� ������� ������ų�� �Ǵ� �� �� ���� ����
				for(j = 0; j < lSize; j++)//�̶����� ������ ���ͷ����� �ɺ����̺� �����ϰ� �ش� token�� ����� �ش�
				{
					String tempLine = symtabList.get(section).literalList.get(j) + "\t" + symtabList.get(section).literalList.get(j) + "\t" + "\t";
					TokenList.get(section).putToken(tempLine);
					tokenIndex = TokenList.get(section).tokenList.size();
					tokenSearch = TokenList.get(section).getToken(tokenIndex-1);//���� �޾ƿ� ��ū�� ���ؼ� ����
					symtabList.get(section).putSymbol(symtabList.get(section).literalList.get(j),locctr);
					tokenSearch.location = locctr;
					//locctr�����κ�
					if(symtabList.get(section).literalList.get(j).charAt(1) == 'X')
					{
						num = symtabList.get(section).literalList.get(j).length();
						num -= 4;
						locctr += (int)num/2;
						searchTokenTable.locLength += (int)num/2;
					}
					else if(symtabList.get(section).literalList.get(j).charAt(1) == 'C')
					{
						num = symtabList.get(section).literalList.get(j).length();
						num -= 4;
						locctr += num;
						searchTokenTable.locLength += num;
					}
					
				}
				symtabList.get(section).literalList.clear();//���ͷ� ����Ʈ�� �����
				continue;
			}
			else if(tokenSearch.operator.equals("CSECT"))//�պ��� �� �κ� 
			{
				locctr  = 0;//�ּҰ� 0���� ����
				TokenList.get(section).removeToken();
				section++;//������ ����
				TokenList.get(section).putToken(lineList.get(i));
				tokenIndex = TokenList.get(section).tokenList.size();
				tokenSearch = TokenList.get(section).getToken(tokenIndex-1);//���� �޾ƿ� ��ū�� ���ؼ� ����
				symtabList.get(section).putSymbol(tokenSearch.label,locctr);//�ش� �ּҰ� ����
				tokenSearch.location = locctr;
				continue;
			}
			else if(tokenSearch.operator.equals("EQU"))//�պ��� �� �κ� 
			{
				if(tokenSearch.operand[0].charAt(0) == '*')
				{
					symtabList.get(section).putSymbol(tokenSearch.label,locctr);//�ش� �ּҰ� ����
					tokenSearch.location = locctr;
				}
				else
				{
					String temp1, temp2;
					char op;
					temp1 = tokenSearch.operand[0];
					op = tokenSearch.operand[1].charAt(0);
					temp2 = tokenSearch.operand[1].substring(1);
					if(op == '+')
					{
						symtabList.get(section).putSymbol(tokenSearch.label,symtabList.get(section).search(temp1) + symtabList.get(section).search(temp2));//�ش� �ּҰ� ����
						tokenSearch.location = symtabList.get(section).search(temp1) + symtabList.get(section).search(temp2);
					}
					else if(op == '-')
					{
						symtabList.get(section).putSymbol(tokenSearch.label,symtabList.get(section).search(temp1) - symtabList.get(section).search(temp2));//�ش� �ּҰ� ����
						tokenSearch.location = symtabList.get(section).search(temp1) - symtabList.get(section).search(temp2);
					}
					else if(op == '*')
					{
						symtabList.get(section).putSymbol(tokenSearch.label,symtabList.get(section).search(temp1) * symtabList.get(section).search(temp2));//�ش� �ּҰ� ����
						tokenSearch.location = symtabList.get(section).search(temp1) * symtabList.get(section).search(temp2);
					}
					else if(op =='/')
					{
						symtabList.get(section).putSymbol(tokenSearch.label,symtabList.get(section).search(temp1) / symtabList.get(section).search(temp2));//�ش� �ּҰ� ����
						tokenSearch.location = symtabList.get(section).search(temp1) / symtabList.get(section).search(temp2);
					}
				}
				continue;
			}
			else
			{
				//������ �տ� '+'�� ���� ��츦 ����Ͽ� �ӽ÷� ���� ������ ����� �Ǵ�
				String judge;
				if(tokenSearch.operator.charAt(0) == '+')//'+'���ڰ� �տ� �ִٸ� ���� ���
				{
					judge = tokenSearch.operator.substring(1);
				}
				else
				{
					judge = tokenSearch.operator.substring(0);
				}
				
				if(instTable.search(judge).format == 2)//2������ ���
				{
					symtabList.get(section).putSymbol(tokenSearch.label,locctr);
					tokenSearch.location = locctr;
					locctr += 2;
					searchTokenTable.locLength += 2;
					//�ǿ����ڰ� ���ͷ��� ��츦 ���
					if(!tokenSearch.operand[0].equals(""))
					{
						if(tokenSearch.operand[0].charAt(0) == '=')
						{
							symtabList.get(section).putLiteral(tokenSearch.operand[0]);
						}
					}
				}
				else
				{
					if(tokenSearch.operator.charAt(0) != '+')//3���� �϶�
					{
						symtabList.get(section).putSymbol(tokenSearch.label,locctr);
						tokenSearch.location = locctr;
						locctr += 3;
						searchTokenTable.locLength += 3;
						//�ǿ����ڰ� ���ͷ��� ��츦 ���
						if(!tokenSearch.operand[0].equals(""))
						{
							if(tokenSearch.operand[0].charAt(0) == '=')
							{
								symtabList.get(section).putLiteral(tokenSearch.operand[0]);
							}
						}		
					}
					else//4���� �϶�
					{
						symtabList.get(section).putSymbol(tokenSearch.label,locctr);
						tokenSearch.location = locctr;
						locctr += 4;
						searchTokenTable.locLength += 4;
						//�ǿ����ڰ� ���ͷ��� ��츦 ���
						if(!tokenSearch.operand[0].equals(""))
						{
							if(tokenSearch.operand[0].charAt(0) == '=')
							{
								symtabList.get(section).putLiteral(tokenSearch.operand[0]);
							}
						}
					}
				}
				
			}			
		}
		
	}
	
	/**
	 * pass2 ������ �����Ѵ�.<br>
	 *   1) �м��� ������ �������� object code�� �����Ͽ� codeList�� ����.
	 */
	private void pass2() {
		int countSection = 0;//���α׷� ������ ����ŭ ���� �ڵ带 ����
		int size;//��ū�� ������ ����
		int i;
		Token searchToken;//�����ϰ��� �ϴ� ��ū
		TokenTable searchTokenTable;//�����ϰ��� �ϴ� ��ū ���̺�
		
		while(countSection <= section)
		{
			size = TokenList.get(countSection).tokenList.size();
			for(i = 0 ; i < size; i++)
			{
				searchTokenTable = TokenList.get(countSection);
				searchToken = TokenList.get(countSection).tokenList.get(i);//Ž���ϰ��� �ϴ� ��ū�� �޾ƿ�
				if(searchToken.operator.equals("START"))//START��ɾ��϶��� ���� �ڵ� �������� ����
				{
					//��ū�� ���� ����
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
					continue;
				}
				else if(searchToken.operator.equals("EXTDEF"))//EXTDEF��ɾ��϶��� ���� �ڵ� �������� ����
				{
					//��ū�� ���� ����
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("EXTREF"))//EXTREF��ɾ��϶��� ���� �ڵ� �������� ����
				{
					//��ū�� ���� ����
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("EQU"))//EQU��ɾ��϶��� ���� �ڵ� �������� ����
				{
					//��ū�� ���� ����
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("CSECT"))//CSECT��ɾ��϶��� ���� �ڵ� �������� ����
				{
					//��ū�� ���� ����
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("LTORG"))//LTORG��ɾ��϶��� ���� �ڵ� �������� ����
				{
					//��ū�� ���� ����
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("END"))//END��ɾ��϶��� ���� �ڵ� �������� ����
				{
					//��ū�� ���� ����
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("RESW"))//RESW��ɾ��϶��� ���� �ڵ� �������� ����
				{
					//��ū�� ���� ����
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("RESB"))//RESB��ɾ��϶��� ���� �ڵ� �������� ����
				{
					//��ū�� ���� ����
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("WORD"))//WORD��ɾ��϶��� ���� �ڵ� �������� ����
				{
					//��ū�� ���� ����
					searchToken.byteSize = -2;//WORD��ɾ����� ��Ÿ���� ���� ����
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
					
				}
				else if(searchToken.operator.equals("BYTE"))//BYTE��ɾ��϶��� ���� �ڵ� �������� ����
				{
					//��ū�� ���� ����
					searchToken.byteSize = -3;//WORD��ɾ����� ��Ÿ���� ���� ����
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
					
				}
				else
				{
					Instruction checkInst;
					String checkOperator;
					String data ="";
					int pc;//���α׷� ī���Ϳ� ���� ���ɸ� ������ ����
					int targetAddr;//�ǿ������� �ּ�
					SymbolTable searchSymbol;
					String checkOperand;//�ǿ����� �տ� �μ���ȣ�� ���ְ� ������ �ϴ� ����
					int locGap;//�ּҰ��� ����
					//��ɾ� �м��� ���� �۾�
					if(searchToken.operator.charAt(0) != '+')
					{
						checkOperator =searchToken.operator;
					}
					else
					{
						checkOperator =searchToken.operator.substring(1);
					}
					
					if(checkOperator.charAt(0) != '=')//���ͷ��� �ƴҶ��� ��ɾ� �Ǻ�
					{
						checkInst = instTable.search(checkOperator);
					}
					else
					{
						checkInst = null;
					}
					
					if(checkInst == null)//���ͷ��϶��� �۾�
					{
						searchToken.byteSize = -9;//���ͷ��϶��� ��Ÿ���� ���� ����
						searchToken.setFlag(TokenTable.nFlag,0);
						searchToken.setFlag(TokenTable.iFlag,0);
						searchToken.setFlag(TokenTable.xFlag,0);
						searchToken.setFlag(TokenTable.bFlag,0);
						searchToken.setFlag(TokenTable.pFlag,0);
						searchToken.setFlag(TokenTable.eFlag,0);
						searchTokenTable.makeObjectCode(i);
						continue;
					}
					else if(checkInst.format == 2)//2�����϶��� ����
					{
						searchToken.byteSize = -4;//3�������� ��Ÿ���� ���� ����
						searchToken.setFlag(TokenTable.nFlag,0);
						searchToken.setFlag(TokenTable.iFlag,0);
						searchToken.setFlag(TokenTable.xFlag,0);
						searchToken.setFlag(TokenTable.bFlag,0);
						searchToken.setFlag(TokenTable.pFlag,0);
						searchToken.setFlag(TokenTable.eFlag,0);
						searchTokenTable.makeObjectCode(i);
						
					}					
					else if(searchToken.operator.charAt(0) != '+')//3������ ���
					{
					
						searchToken.byteSize = -5;//3�������� ��Ÿ���� ���� ����
						//nixbpe�����ܰ�
						//n,i��Ʈ ����
						if(searchToken.operand[0].equals(""))//�ǿ����ڰ� ���� ��� ���� ����
						{
							searchToken.byteSize = -7;//immediate�� ���� ó��
							searchToken.setFlag(TokenTable.nFlag,1);
							searchToken.setFlag(TokenTable.iFlag,1);
							searchToken.setFlag(TokenTable.xFlag,0);
							searchToken.setFlag(TokenTable.bFlag,0);
							searchToken.setFlag(TokenTable.pFlag,0);
							searchToken.setFlag(TokenTable.eFlag,0);
							searchTokenTable.makeObjectCode(i);
							continue;
						}
						
						if(searchToken.operand[0].charAt(0) == '#')//immediate
						{
							searchToken.byteSize = -6;//immediate�� ���� ó��
							searchToken.setFlag(TokenTable.nFlag,0);
							searchToken.setFlag(TokenTable.iFlag,1);
							searchToken.setFlag(TokenTable.xFlag,0);
							searchToken.setFlag(TokenTable.bFlag,0);
							searchToken.setFlag(TokenTable.pFlag,0);
							searchToken.setFlag(TokenTable.eFlag,0);
							searchTokenTable.makeObjectCode(i);
							continue;
						}
						else if(searchToken.operand[0].charAt(0) == '@')//indirect
						{
							searchToken.setFlag(TokenTable.nFlag,1);
							searchToken.setFlag(TokenTable.iFlag,0);
						}
						else//simple
						{
							searchToken.setFlag(TokenTable.nFlag,1);
							searchToken.setFlag(TokenTable.iFlag,1);
						}
						
						//x��Ʈ ����
						if(searchToken.operand[1].equals("X"))
						{
							searchToken.setFlag(TokenTable.xFlag,1);			
						}
						else
						{
							searchToken.setFlag(TokenTable.xFlag,0);
						}
						
						//b,p�����ܰ�
						pc = searchToken.location+3;
						searchSymbol = searchTokenTable.symTab;
						if(searchToken.operand[0].charAt(0) == '@')
						{
							checkOperand = searchToken.operand[0].substring(1);
						}
						else
						{
							checkOperand = searchToken.operand[0];
						}
						
						targetAddr = searchSymbol.search(checkOperand);
						
						locGap = targetAddr - pc;//���̸� ����
						searchToken.locGap = locGap;
						
						if((locGap >= 4096) || (locGap <= -4096))//�̷� ��Ȳ�� ��� base relative
						{
							searchToken.setFlag(TokenTable.bFlag,1);
							searchToken.setFlag(TokenTable.pFlag,0);
						}
						else//�ƴҰ�쿡�� pc relative;
						{
							searchToken.setFlag(TokenTable.bFlag,0);
							searchToken.setFlag(TokenTable.pFlag,1);	
						}
						
						searchToken.setFlag(TokenTable.eFlag,0);
						searchTokenTable.makeObjectCode(i);
					}
					else if(searchToken.operator.charAt(0) == '+')//4������ ���
					{
						searchToken.byteSize = -8;
						searchToken.setFlag(TokenTable.nFlag,1);
						searchToken.setFlag(TokenTable.iFlag,1);
						if(searchToken.operand[1].equals("X"))
						{
							searchToken.setFlag(TokenTable.xFlag,1);
						}
						else
						{
							searchToken.setFlag(TokenTable.xFlag,0);
						}
						searchToken.setFlag(TokenTable.bFlag,0);
						searchToken.setFlag(TokenTable.pFlag,0);
						searchToken.setFlag(TokenTable.eFlag,1);
						
						pc = searchToken.location+3;
						searchSymbol = searchTokenTable.symTab;
						if(searchSymbol.searchRef(searchToken.operand[0]) == 1)//�������̺� �ִ� �ɺ��̸� 0���� ����
						{
							locGap = 0;
							searchToken.locGap = locGap;
							
						}
						else
						{
							checkOperand = searchToken.operand[0];
							targetAddr = searchSymbol.search(checkOperand);
							
							locGap = targetAddr - pc;//���̸� ����
							searchToken.locGap = locGap;
							
						}
						searchTokenTable.makeObjectCode(i);
						
					}
					
				}
				
			}
			countSection++;
		}
		
		
		
	}
	
	/**
	 * inputFile�� �о�鿩�� lineList�� �����Ѵ�.<br>
	 * @param inputFile : input ���� �̸�.
	 * @throws IOException 
	 */
	private void loadInputFile(String inputFile) throws IOException {
		Path path = Paths.get(inputFile);//������ ��θ� ����
		String data = "";//���Ͽ��� �� ���ڿ��� ���� ����
		
		FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
		ByteBuffer buffer = ByteBuffer.allocate(1);//�ѹ��ھ� �޾Ƶ��̱� ���� ������ ����
		Charset charset = Charset.defaultCharset();
		
		int byteCount;
		byteCount = fileChannel.read(buffer);
		String temp;
		temp = charset.decode(buffer).toString();
		
		//������ �о� ���̴� �۾�
		while(true)
		{
			if(byteCount == -1)
			{
				break;
			}
			
			if(temp.contentEquals("\r"))
			{
				if(data.charAt(0) != '.')//�ּ����� �ƴ� ������
				{
					lineList.add(data);//��ǲ���� �� ������ ����
				}
				data = "";//���ڿ� �ʱ�ȭ
				byteCount = fileChannel.read(buffer);//���ڸ� �б�
				buffer.flip();
				temp = charset.decode(buffer).toString();
	            buffer.clear();
			}
			
			if(temp.equals("\n"))//���� ������ �ڰ����� ����
			{
				
				byteCount = fileChannel.read(buffer);//���ڸ� �б�
				buffer.flip();
				temp = charset.decode(buffer).toString();
				if(!temp.equals("\r"))
				{
					data += temp;	
				}
				buffer.clear();
				continue;//����Ű�� ���ڿ��� ������ ��Ű�� ����
			}
			
			byteCount = fileChannel.read(buffer);//���ڸ� �б�	
			buffer.flip();
			temp = charset.decode(buffer).toString();
			if(!temp.equals("\r"))
			{
				data += temp;	
			}
            buffer.clear();
		}
		fileChannel.close();//������ ����
	}
}
