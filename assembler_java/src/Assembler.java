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
 * 이 프로그램은 SIC/XE 머신을 위한 Assembler 프로그램의 메인 루틴이다.
 * 프로그램의 수행 작업은 다음과 같다. <br>
 * 1) 처음 시작하면 Instruction 명세를 읽어들여서 assembler를 세팅한다. <br>
 * 2) 사용자가 작성한 input 파일을 읽어들인 후 저장한다. <br>
 * 3) input 파일의 문장들을 단어별로 분할하고 의미를 파악해서 정리한다. (pass1) <br>
 * 4) 분석된 내용을 바탕으로 컴퓨터가 사용할 수 있는 object code를 생성한다. (pass2) <br>
 * 
 * <br><br>
 * 작성중의 유의사항 : <br>
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 안된다.<br>
 *  2) 마찬가지로 작성된 코드를 삭제하지 않으면 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br><br>
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */

public class Assembler {
	/** instruction 명세를 저장한 공간 */
	InstTable instTable;
	/** 읽어들인 input 파일의 내용을 한 줄 씩 저장하는 공간. */
	ArrayList<String> lineList;
	/** 프로그램의 section별로 symbol table을 저장하는 공간*/
	ArrayList<SymbolTable> symtabList;
	/** 프로그램의 section별로 프로그램을 저장하는 공간*/
	ArrayList<TokenTable> TokenList;
	/** 
	 * Token, 또는 지시어에 따라 만들어진 오브젝트 코드들을 출력 형태로 저장하는 공간. <br>
	 * 필요한 경우 String 대신 별도의 클래스를 선언하여 ArrayList를 교체해도 무방함.
	 */
	ArrayList<String> codeList;
	
	/**추가부분 : 프로그램의 section이 몇개인지 저장을 해줌*/
	static int section = 0;
	
	/**
	 * 클래스 초기화. instruction Table을 초기화와 동시에 세팅한다.
	 * 
	 * @param instFile : instruction 명세를 작성한 파일 이름. 
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
	 * 어셐블러의 메인 루틴
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
	 * 작성된 codeList를 출력형태에 맞게 출력한다.<br>
	 * @param fileName : 저장되는 파일 이름
	 * @throws IOException 
	 */
	private void printObjectCode(String fileName) throws IOException {
		int i,j,k;
		Path path = Paths.get(fileName);
		String data = "";
		String data2 = "";
		FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.WRITE);
		Charset charset = Charset.forName("UTF-8");
		SymbolTable searchSymbol;//해당 토큰 테이블의 심볼테이블을 받아오는 변수
		ArrayList<Token> refInfo = new ArrayList<Token>();//Modification record작성시 필요한 토큰들의 정보를 저장
		int countSection = 0;
		int startCheck = 0;//오브젝트 코드 한 라인의 시작부분인지 아닌지를 저장
		int codeLength = 0;//오브젝트코드 한 줄의 길이를 저장
		int startAddr = 0;//프로그램 섹션이 시작되는 주소에 대한 정보를 저장 
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
			//Header record부분
			data2 += 'H';
			data2 += searchToken.label;
			for(i = data2.length() ; i <7; i++)//빈자리만큼 공백문자로 채움
			{
				data2 += " ";
			}
			
			if(!searchToken.operand[0].equals(""))//시작주소가 있으면 해당 정보를 저장
			{
				startAddr = Integer.parseInt(searchToken.operand[0]);
			}
			else//그렇지 않다면 0을 저장
			{
				startAddr = 0;
			}
			
			data = decimalToHex(startAddr);
			for(i = data.length() ; i <6; i++)//빈자리만큼 0으로 채움
			{
				data2 += '0';
			}
			data2 += data;
			
			data = decimalToHex(searchTokenTable.locLength);
			for(i = data.length() ; i <6; i++)//빈자리만큼 0으로 채움
			{
				data2 += '0';
			}
			data2 += data;
			data2 += "\r\n";//한 줄을 띄워줌
			codeList.add(data2); //Head record저장
			data = "";//작성할 문자열 초기화
			data2 ="";
			
			//Define record, Refer record부분			
			for(i = 0; i < searchTokenTable.symTab.defList.size(); i++)
			{
				if(i == 0)
				{
					data2 += 'D';
				}
				int defAddr = 0;//정의 심볼에 대한 주소값을 받아오는 변수
				data = searchTokenTable.symTab.defList.get(i);
				data2 += data;
				for(j = data.length(); j < 6; j++)//자리수를 맞추기 위한 작업
				{
					data2  += " ";
				}
				defAddr = searchTokenTable.symTab.search(data);
				data = decimalToHex(defAddr);
				for(j = data.length(); j < 6; j++)//자리수를 맞추기 위한 작업
				{
					data2  += '0';
				}
				data2 += data;
			}
			if(i >= 1)
			{
				data2 += "\r\n";//한 줄을 띄워줌
				codeList.add(data2); //Head record저장
				data = "";//작성할 문자열 초기화
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
				for(j = data.length(); j < 6; j++)//자리수를 맞추기 위한 작업
				{
					data2  += " ";
				}
			}
			if(i >= 1)
			{
				data2 += "\r\n";//한 줄을 띄워줌
				codeList.add(data2); //Head record저장
				data = "";//작성할 문자열 초기화
				data2 = "";
			}
			
			//Text record
			for(i = 0 ; i< searchTokenTable.tokenList.size();i++)
			{
				
				searchToken = searchTokenTable.tokenList.get(i);
				if((searchSymbol.searchRef(searchToken.operand[0]) == 1) && (!searchToken.operator.equals("EXTREF")))//피연산자가 참조 심볼이라면 해당 정보를 저장
				{
					refInfo.add(searchToken);
				}
				
				if(searchToken.objectCode.equals(""))//기계어가 없으면 고려하지 않음
				{
					/*Text record를 끊어줄 조건(RESW,RESB명령어일 경우)*/
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
							data2 += "\r\n";//줄바꿈
							len2 += len;
							temp1 = data2.substring(0,7);
							temp2 = data2.substring(7);
							data2 = "";
							data2 = temp1 + len2 + temp2;
							codeLength = 0;//길리 리셋
							startCheck = 0;//조건을 바꿔줌
							codeList.add(data2);
							data = "";//작성할 문자열 초기화
							data2 ="";
						}
					}
					continue;
				}
				if(startCheck == 0)//텍스트 레코드의 시작 부분
				{
					startCheck++;
					data2 += 'T';
					data = decimalToHex(searchToken.location);//시작주소를 저장
					for(j = data.length(); j < 6; j++)
					{
						data2 += '0';
					}
					data2 += data;//목적어 코드의 생성
					data2 += searchToken.objectCode;
					codeLength += searchToken.byteSize;//길이를 카운트
				}
				else if(startCheck == 1)
				{
					
					
					codeLength += searchToken.byteSize;//길이를 카운트
					/*Text record를 끊어줄 조건(길이가 30이하)*/
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
						data2 += "\r\n";//줄바꿈
						//코드의 길이 부분을 넣는 작업
						temp1 = data2.substring(0,7);
						temp2 = data2.substring(7);
						data2 = "";
						data2 = temp1 + len2 + temp2;
						codeLength = 0;//길리 리셋
						startCheck = 0;//조건을 바꿔줌
						codeList.add(data2);
						data = "";//작성할 문자열 초기화
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
				data2 += "\r\n";//줄바꿈
				temp1 = data2.substring(0,7);
				temp2 = data2.substring(7);
				data2 = "";
				data2 = temp1 + len2 + temp2;
				codeLength = 0;//길리 리셋
				codeList.add(data2);
				data = "";//작성할 문자열 초기화
				data2 = "";
			}
			
			//Modification record작성
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
							data = "";//작성할 문자열 초기화
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
							data = "";//작성할 문자열 초기화
							data2 = "";
						}
					}
				}
			}
			refInfo.clear();
			
			//End record부분
			if(searchTokenTable.getToken(0).operator.equals("START"))//프로그램의 시작부분에 START명령어가 있으면 레코드 부분에 프로그램 주소를 같이 출력
			{
				data2 += 'E';
				data = decimalToHex(Integer.parseInt(searchTokenTable.getToken(0).operand[0]));
				for(i = data.length(); i < 6 ; i++)//빈자리를 0으로 채우는 작업
				{
					data2 += '0';
				}
				data2 += data;
				data2 += "\r\n\r\n";
				codeList.add(data2);
				data = "";//작성할 문자열 초기화
				data2 = "";
			}
			else//프로그램의 시작부분에 START명령어가 없으면  정해진 프로그램 시작 주소가 없으므로 출력하지 않음 
			{
				data2 += 'E';
				data2 += data;
				data2 += "\r\n\r\n";
				codeList.add(data2);
				data = "";//작성할 문자열 초기화
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
		fileChannel.close();//파일을 닫음
	}
	
	
	/**
	 * 작성된 SymbolTable들을 출력형태에 맞게 출력한다.<br>
	 * @param fileName : 저장되는 파일 이름
	 * @throws IOException 
	 */
	private void printSymbolTable(String fileName) throws IOException {
		int i,j;
		Path path = Paths.get(fileName);//파일의 경로를 설정
		String data = "";//파일에서 한 문자열을 담을 변수
		
		FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.WRITE);
		Charset charset = Charset.forName("UTF-8");
		//프로그램 섹션별로 심볼테이블 정보를 파일에 출력	
		for(i = 0 ; i <= section; i++)
		{
			for(j = 0 ; j <symtabList.get(i).symbolList.size();j++)
			{
				//심볼과 주소값을 파일에다가 쓰는 작업(심볼이 있을 때에만)
				if((!symtabList.get(i).symbolList.get(j).equals("")) && (symtabList.get(i).symbolList.get(j).charAt(0) != '='))
				{
					data += symtabList.get(i).symbolList.get(j);
					data += "\t\t";
					data += decimalToHex(symtabList.get(i).locationList.get(j)) + "\r\n";
					ByteBuffer buffer = ByteBuffer.allocateDirect(data.length());
					buffer = charset.encode(data);
					fileChannel.write(buffer);
					data = "";//데이터를 담을 공간 리셋
				}
				
				if(j == symtabList.get(i).symbolList.size()-1)//프로그램의 끝이면 한줄을 띄워줌
				{
					data += "\r\n";
					ByteBuffer buffer = ByteBuffer.allocateDirect(data.length());
					buffer = charset.encode(data);
					fileChannel.write(buffer);
					data = "";//데이터를 담을 공간 리셋
				}
			}
		}
		
		
		fileChannel.close();
		
	}
	
	/**
	 * 추가 메소드 부분
	 * 해당 주소값을 16진수 형태로 출력
	 * @param addr : 16진수 형태로 바꿀 10진수 주소값 
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
	 * pass1 과정을 수행한다.<br>
	 *   1) 프로그램 소스를 스캔하여 토큰단위로 분리한 뒤 토큰테이블 생성<br>
	 *   2) label을 symbolTable에 정리<br>
	 *   <br><br>
	 *    주의사항 : SymbolTable과 TokenTable은 프로그램의 section별로 하나씩 선언되어야 한다.
	 */
	private void pass1() {
		
		
		//각각의 심볼 테이블을  작성하는 과정
		SymbolTable s1 = new SymbolTable();
		SymbolTable s2 = new SymbolTable();
		SymbolTable s3 = new SymbolTable();
		symtabList.add(s1);
		symtabList.add(s2);
		symtabList.add(s3);
		
		
		//각각의 토큰테이블을 작성하는 과정
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
		int locctr = 0; // 주소값을 담을 변수
		//받아온 소스 라인라인마다 분석을 해 줌
		
		for(i = 0 ; i < size; i++)
		{
			//조사하고자 하는 토큰 테이블
			searchTokenTable = TokenList.get(section);
			//소스코드 라인마다 토큰화하여 저장
			TokenList.get(section).putToken(lineList.get(i));
			tokenIndex = TokenList.get(section).tokenList.size();
			tokenSearch = TokenList.get(section).getToken(tokenIndex-1);//현재 받아온 토큰에 대해서 조사
				
			if(tokenSearch.operator.equals("START"))//START명령어일 경우
			{
				symtabList.get(section).putSymbol(tokenSearch.label,Integer.parseInt(tokenSearch.operand[0]));
				tokenSearch.location = Integer.parseInt(tokenSearch.operand[0]);
				continue;
			}
			else if(tokenSearch.operator.equals("END"))//END명령어일 경우
			{
				int lSize = symtabList.get(section).literalList.size();
				symtabList.get(section).putSymbol(tokenSearch.label,-1);
				tokenSearch.location = -1;
				int num;//locctr주소값을 갱신할 때 어느정도 증가시킬시 판단 할 때 쓰는 변수
				for(j = 0; j < lSize; j++)//이때까지 저장한 리터럴들을 심볼테이블에 저장하고 해당 token도 만들어 준다
				{
					String tempLine = symtabList.get(section).literalList.get(j) + "\t" + symtabList.get(section).literalList.get(j) + "\t" + "\t";
					TokenList.get(section).putToken(tempLine);
					tokenIndex = TokenList.get(section).tokenList.size();
					tokenSearch = TokenList.get(section).getToken(tokenIndex-1);//현재 받아온 토큰에 대해서 조사
					symtabList.get(section).putSymbol(symtabList.get(section).literalList.get(j),locctr);
					tokenSearch.location = locctr;
					//locctr조정부분
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
				symtabList.get(section).literalList.clear();//리터럴 리스트를 비워줌

				continue;
			}
			else if(tokenSearch.operator.equals("EXTDEF"))//EXTDEF명령어일 경우
			{
				symtabList.get(section).putSymbol(tokenSearch.label,-1);//주소값이 없으므로 -1저장
				tokenSearch.location = -1;
				if(!tokenSearch.operand[0].equals(""))//심볼이 있으면 정의 심볼 테이블에 저장
				{
					symtabList.get(section).putDef(tokenSearch.operand[0]);
				}
					
				if(!tokenSearch.operand[1].equals(""))//심볼이 있으면 정의 심볼 테이블에 저장
				{
					symtabList.get(section).putDef(tokenSearch.operand[1]);
				}
					
				if(!tokenSearch.operand[2].equals(""))//심볼이 있으면 정의 심볼 테이블에 저장
				{
					symtabList.get(section).putDef(tokenSearch.operand[2]);
				}

				continue;
			}
			else if(tokenSearch.operator.equals("EXTREF"))//EXTREF명령어일 경우
			{
				symtabList.get(section).putSymbol(tokenSearch.label,-1);//주소값이 없으므로 -1저장
				tokenSearch.location = -1;
				if(!tokenSearch.operand[0].equals(""))//심볼이 있으면 정의 심볼 테이블에 저장
				{
					symtabList.get(section).putRef(tokenSearch.operand[0]);
				}
					
				if(!tokenSearch.operand[1].equals(""))//심볼이 있으면 정의 심볼 테이블에 저장
				{
					symtabList.get(section).putRef(tokenSearch.operand[1]);
				}
				
				if(!tokenSearch.operand[2].equals(""))//심볼이 있으면 정의 심볼 테이블에 저장
				{
					symtabList.get(section).putRef(tokenSearch.operand[2]);
				}

				continue;
			}
			else if(tokenSearch.operator.equals("RESW"))//RESW명령어일 경우 
			{
				symtabList.get(section).putSymbol(tokenSearch.label,locctr);//해당 주소값 저장
				tokenSearch.location = locctr;
				locctr += 3 * Integer.parseInt(tokenSearch.operand[0]);//주소값 증가
				searchTokenTable.locLength += 3* Integer.parseInt(tokenSearch.operand[0]);
				continue;
			}
			else if(tokenSearch.operator.equals("RESB"))//RESB명령어일 경우 
			{
				symtabList.get(section).putSymbol(tokenSearch.label,locctr);//해당 주소값 저장
				tokenSearch.location = locctr;
				locctr += 1 * Integer.parseInt(tokenSearch.operand[0]);//주소값 증가
				searchTokenTable.locLength += 1 * Integer.parseInt(tokenSearch.operand[0]);
				continue;
			}
			else if(tokenSearch.operator.equals("BYTE"))//손봐야 할 부분 
			{
				symtabList.get(section).putSymbol(tokenSearch.label,locctr);//해당 주소값 저장
				tokenSearch.location = locctr;
				if(tokenSearch.operand[0].charAt(0) == 'X')//16진수 형태라면
				{
					int num = tokenSearch.operand[0].length();
					num -= 3;
					locctr += (int)num/2;
					searchTokenTable.locLength += (int)num/2;
				}
				else if(tokenSearch.operand[0].charAt(0) == 'C')//문자열 형태라면
				{
					int num = tokenSearch.operand[0].length();
					num -= 3;
					locctr += num;
					searchTokenTable.locLength += num;
				}
				continue;
			}
			else if(tokenSearch.operator.equals("WORD"))//WORD명령어일 경우
			{
				symtabList.get(section).putSymbol(tokenSearch.label,locctr);//해당 주소값 저장
				tokenSearch.location = locctr;
				locctr += 3;//주소값 증가
				searchTokenTable.locLength += 3;
				continue;
			}
			else if(tokenSearch.operator.equals("LTORG"))//손봐야 할 부분 
			{
				int lSize = symtabList.get(section).literalList.size();
				symtabList.get(section).putSymbol(tokenSearch.label,-1);
				tokenSearch.location = -1;
				int num;//locctr주소값을 갱신할 때 어느정도 증가시킬시 판단 할 때 쓰는 변수
				for(j = 0; j < lSize; j++)//이때까지 저장한 리터럴들을 심볼테이블에 저장하고 해당 token도 만들어 준다
				{
					String tempLine = symtabList.get(section).literalList.get(j) + "\t" + symtabList.get(section).literalList.get(j) + "\t" + "\t";
					TokenList.get(section).putToken(tempLine);
					tokenIndex = TokenList.get(section).tokenList.size();
					tokenSearch = TokenList.get(section).getToken(tokenIndex-1);//현재 받아온 토큰에 대해서 조사
					symtabList.get(section).putSymbol(symtabList.get(section).literalList.get(j),locctr);
					tokenSearch.location = locctr;
					//locctr조정부분
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
				symtabList.get(section).literalList.clear();//리터럴 리스트를 비워줌
				continue;
			}
			else if(tokenSearch.operator.equals("CSECT"))//손봐야 할 부분 
			{
				locctr  = 0;//주소값 0으로 리셋
				TokenList.get(section).removeToken();
				section++;//섹션을 분할
				TokenList.get(section).putToken(lineList.get(i));
				tokenIndex = TokenList.get(section).tokenList.size();
				tokenSearch = TokenList.get(section).getToken(tokenIndex-1);//현재 받아온 토큰에 대해서 조사
				symtabList.get(section).putSymbol(tokenSearch.label,locctr);//해당 주소값 저장
				tokenSearch.location = locctr;
				continue;
			}
			else if(tokenSearch.operator.equals("EQU"))//손봐야 할 부분 
			{
				if(tokenSearch.operand[0].charAt(0) == '*')
				{
					symtabList.get(section).putSymbol(tokenSearch.label,locctr);//해당 주소값 저장
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
						symtabList.get(section).putSymbol(tokenSearch.label,symtabList.get(section).search(temp1) + symtabList.get(section).search(temp2));//해당 주소값 저장
						tokenSearch.location = symtabList.get(section).search(temp1) + symtabList.get(section).search(temp2);
					}
					else if(op == '-')
					{
						symtabList.get(section).putSymbol(tokenSearch.label,symtabList.get(section).search(temp1) - symtabList.get(section).search(temp2));//해당 주소값 저장
						tokenSearch.location = symtabList.get(section).search(temp1) - symtabList.get(section).search(temp2);
					}
					else if(op == '*')
					{
						symtabList.get(section).putSymbol(tokenSearch.label,symtabList.get(section).search(temp1) * symtabList.get(section).search(temp2));//해당 주소값 저장
						tokenSearch.location = symtabList.get(section).search(temp1) * symtabList.get(section).search(temp2);
					}
					else if(op =='/')
					{
						symtabList.get(section).putSymbol(tokenSearch.label,symtabList.get(section).search(temp1) / symtabList.get(section).search(temp2));//해당 주소값 저장
						tokenSearch.location = symtabList.get(section).search(temp1) / symtabList.get(section).search(temp2);
					}
				}
				continue;
			}
			else
			{
				//연산자 앞에 '+'가 있을 경우를 대비하여 임시로 변수 공간을 만들어 판단
				String judge;
				if(tokenSearch.operator.charAt(0) == '+')//'+'문자가 앞에 있다면 빼고 고려
				{
					judge = tokenSearch.operator.substring(1);
				}
				else
				{
					judge = tokenSearch.operator.substring(0);
				}
				
				if(instTable.search(judge).format == 2)//2형식일 경우
				{
					symtabList.get(section).putSymbol(tokenSearch.label,locctr);
					tokenSearch.location = locctr;
					locctr += 2;
					searchTokenTable.locLength += 2;
					//피연산자가 리터럴일 경우를 고려
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
					if(tokenSearch.operator.charAt(0) != '+')//3형식 일때
					{
						symtabList.get(section).putSymbol(tokenSearch.label,locctr);
						tokenSearch.location = locctr;
						locctr += 3;
						searchTokenTable.locLength += 3;
						//피연산자가 리터럴일 경우를 고려
						if(!tokenSearch.operand[0].equals(""))
						{
							if(tokenSearch.operand[0].charAt(0) == '=')
							{
								symtabList.get(section).putLiteral(tokenSearch.operand[0]);
							}
						}		
					}
					else//4형식 일때
					{
						symtabList.get(section).putSymbol(tokenSearch.label,locctr);
						tokenSearch.location = locctr;
						locctr += 4;
						searchTokenTable.locLength += 4;
						//피연산자가 리터럴일 경우를 고려
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
	 * pass2 과정을 수행한다.<br>
	 *   1) 분석된 내용을 바탕으로 object code를 생성하여 codeList에 저장.
	 */
	private void pass2() {
		int countSection = 0;//프로그램 섹션의 수만큼 기계어 코드를 생성
		int size;//토큰의 갯수를 저장
		int i;
		Token searchToken;//조사하고자 하는 토큰
		TokenTable searchTokenTable;//조사하고자 하는 토큰 테이블
		
		while(countSection <= section)
		{
			size = TokenList.get(countSection).tokenList.size();
			for(i = 0 ; i < size; i++)
			{
				searchTokenTable = TokenList.get(countSection);
				searchToken = TokenList.get(countSection).tokenList.get(i);//탐색하고자 하는 토큰을 받아옴
				if(searchToken.operator.equals("START"))//START명령어일때는 기계어 코드 존재하지 않음
				{
					//토큰의 값을 세팅
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
				else if(searchToken.operator.equals("EXTDEF"))//EXTDEF명령어일때는 기계어 코드 존재하지 않음
				{
					//토큰의 값을 세팅
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("EXTREF"))//EXTREF명령어일때는 기계어 코드 존재하지 않음
				{
					//토큰의 값을 세팅
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("EQU"))//EQU명령어일때는 기계어 코드 존재하지 않음
				{
					//토큰의 값을 세팅
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("CSECT"))//CSECT명령어일때는 기계어 코드 존재하지 않음
				{
					//토큰의 값을 세팅
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("LTORG"))//LTORG명령어일때는 기계어 코드 존재하지 않음
				{
					//토큰의 값을 세팅
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("END"))//END명령어일때는 기계어 코드 존재하지 않음
				{
					//토큰의 값을 세팅
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("RESW"))//RESW명령어일때는 기계어 코드 존재하지 않음
				{
					//토큰의 값을 세팅
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("RESB"))//RESB명령어일때는 기계어 코드 존재하지 않음
				{
					//토큰의 값을 세팅
					searchToken.byteSize = 0;
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
				}
				else if(searchToken.operator.equals("WORD"))//WORD명령어일때는 기계어 코드 존재하지 않음
				{
					//토큰의 값을 세팅
					searchToken.byteSize = -2;//WORD명령어임을 나타내기 위해 설정
					searchToken.setFlag(TokenTable.nFlag,0);
					searchToken.setFlag(TokenTable.iFlag,0);
					searchToken.setFlag(TokenTable.xFlag,0);
					searchToken.setFlag(TokenTable.bFlag,0);
					searchToken.setFlag(TokenTable.pFlag,0);
					searchToken.setFlag(TokenTable.eFlag,0);
					searchTokenTable.makeObjectCode(i);
					
				}
				else if(searchToken.operator.equals("BYTE"))//BYTE명령어일때는 기계어 코드 존재하지 않음
				{
					//토큰의 값을 세팅
					searchToken.byteSize = -3;//WORD명령어임을 나타내기 위해 설정
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
					int pc;//프로그램 카운터에 대한 저옵를 저장할 변수
					int targetAddr;//피연산자의 주소
					SymbolTable searchSymbol;
					String checkOperand;//피연산자 앞에 부수기호를 빼주고 저장을 하는 변수
					int locGap;//주소값의 차이
					//명령어 분석을 위한 작업
					if(searchToken.operator.charAt(0) != '+')
					{
						checkOperator =searchToken.operator;
					}
					else
					{
						checkOperator =searchToken.operator.substring(1);
					}
					
					if(checkOperator.charAt(0) != '=')//리터럴이 아닐때만 명령어 판별
					{
						checkInst = instTable.search(checkOperator);
					}
					else
					{
						checkInst = null;
					}
					
					if(checkInst == null)//리터럴일때의 작업
					{
						searchToken.byteSize = -9;//리터럴일때를 나타내기 위한 설정
						searchToken.setFlag(TokenTable.nFlag,0);
						searchToken.setFlag(TokenTable.iFlag,0);
						searchToken.setFlag(TokenTable.xFlag,0);
						searchToken.setFlag(TokenTable.bFlag,0);
						searchToken.setFlag(TokenTable.pFlag,0);
						searchToken.setFlag(TokenTable.eFlag,0);
						searchTokenTable.makeObjectCode(i);
						continue;
					}
					else if(checkInst.format == 2)//2형식일때의 설정
					{
						searchToken.byteSize = -4;//3형식임을 나타내기 위한 설정
						searchToken.setFlag(TokenTable.nFlag,0);
						searchToken.setFlag(TokenTable.iFlag,0);
						searchToken.setFlag(TokenTable.xFlag,0);
						searchToken.setFlag(TokenTable.bFlag,0);
						searchToken.setFlag(TokenTable.pFlag,0);
						searchToken.setFlag(TokenTable.eFlag,0);
						searchTokenTable.makeObjectCode(i);
						
					}					
					else if(searchToken.operator.charAt(0) != '+')//3형식일 경우
					{
					
						searchToken.byteSize = -5;//3형식임을 나타내기 위한 설정
						//nixbpe설정단계
						//n,i비트 설정
						if(searchToken.operand[0].equals(""))//피연산자가 없는 경우 따로 설정
						{
							searchToken.byteSize = -7;//immediate는 따로 처리
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
							searchToken.byteSize = -6;//immediate는 따로 처리
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
						
						//x비트 설정
						if(searchToken.operand[1].equals("X"))
						{
							searchToken.setFlag(TokenTable.xFlag,1);			
						}
						else
						{
							searchToken.setFlag(TokenTable.xFlag,0);
						}
						
						//b,p설정단계
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
						
						locGap = targetAddr - pc;//차이를 저장
						searchToken.locGap = locGap;
						
						if((locGap >= 4096) || (locGap <= -4096))//이런 상황일 경우 base relative
						{
							searchToken.setFlag(TokenTable.bFlag,1);
							searchToken.setFlag(TokenTable.pFlag,0);
						}
						else//아닐경우에는 pc relative;
						{
							searchToken.setFlag(TokenTable.bFlag,0);
							searchToken.setFlag(TokenTable.pFlag,1);	
						}
						
						searchToken.setFlag(TokenTable.eFlag,0);
						searchTokenTable.makeObjectCode(i);
					}
					else if(searchToken.operator.charAt(0) == '+')//4형식일 경우
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
						if(searchSymbol.searchRef(searchToken.operand[0]) == 1)//참조테이블에 있는 심볼이면 0으로 설정
						{
							locGap = 0;
							searchToken.locGap = locGap;
							
						}
						else
						{
							checkOperand = searchToken.operand[0];
							targetAddr = searchSymbol.search(checkOperand);
							
							locGap = targetAddr - pc;//차이를 저장
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
	 * inputFile을 읽어들여서 lineList에 저장한다.<br>
	 * @param inputFile : input 파일 이름.
	 * @throws IOException 
	 */
	private void loadInputFile(String inputFile) throws IOException {
		Path path = Paths.get(inputFile);//파일의 경로를 설정
		String data = "";//파일에서 한 문자열을 잠을 변수
		
		FileChannel fileChannel = FileChannel.open(path, StandardOpenOption.READ);
		ByteBuffer buffer = ByteBuffer.allocate(1);//한문자씩 받아들이기 위한 버퍼의 선언
		Charset charset = Charset.defaultCharset();
		
		int byteCount;
		byteCount = fileChannel.read(buffer);
		String temp;
		temp = charset.decode(buffer).toString();
		
		//파일을 읽어 들이는 작업
		while(true)
		{
			if(byteCount == -1)
			{
				break;
			}
			
			if(temp.contentEquals("\r"))
			{
				if(data.charAt(0) != '.')//주석문이 아닐 때에만
				{
					lineList.add(data);//인풋파일 한 내용을 저장
				}
				data = "";//문자열 초기화
				byteCount = fileChannel.read(buffer);//문자를 읽기
				buffer.flip();
				temp = charset.decode(buffer).toString();
	            buffer.clear();
			}
			
			if(temp.equals("\n"))//문장 단위로 자가업을 수행
			{
				
				byteCount = fileChannel.read(buffer);//문자를 읽기
				buffer.flip();
				temp = charset.decode(buffer).toString();
				if(!temp.equals("\r"))
				{
					data += temp;	
				}
				buffer.clear();
				continue;//엔터키는 문자열에 포함을 시키지 않음
			}
			
			byteCount = fileChannel.read(buffer);//문자를 읽기	
			buffer.flip();
			temp = charset.decode(buffer).toString();
			if(!temp.equals("\r"))
			{
				data += temp;	
			}
            buffer.clear();
		}
		fileChannel.close();//파일을 닫음
	}
}
