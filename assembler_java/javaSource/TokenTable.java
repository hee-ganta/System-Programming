import java.util.ArrayList;

/**
 * 사용자가 작성한 프로그램 코드를 단어별로 분할 한 후, 의미를 분석하고, 최종 코드로 변환하는 과정을 총괄하는 클래스이다. <br>
 * pass2에서 object code로 변환하는 과정은 혼자 해결할 수 없고 symbolTable과 instTable의 정보가 필요하므로 이를 링크시킨다.<br>
 * section 마다 인스턴스가 하나씩 할당된다.
 *
 */
public class TokenTable {
	public static final int MAX_OPERAND=3;
	
	/* bit 조작의 가독성을 위한 선언 */
	public static final int nFlag=32;
	public static final int iFlag=16;
	public static final int xFlag=8;
	public static final int bFlag=4;
	public static final int pFlag=2;
	public static final int eFlag=1;
	
	/*추가부분 : 레지스터 번호 저장*/
	public static final char X = '1';
	public static final char A ='0';
	public static final char S ='4';
	public static final char T = '5';
	
	/*추가부분 : 프로그램 섹션의 길이를 저장*/
	public int locLength;
	
	
	/* Token을 다룰 때 필요한 테이블들을 링크시킨다. */
	SymbolTable symTab;
	InstTable instTab;
	
	
	/** 각 line을 의미별로 분할하고 분석하는 공간. */
	ArrayList<Token> tokenList;
	
	/**
	 * 초기화하면서 symTable과 instTable을 링크시킨다.
	 * @param t1 : 해당 section과 연결되어있는 symbol table
	 * @param instTab : instruction 명세가 정의된 instTable
	 */
	public TokenTable(SymbolTable symTab, InstTable instTab) {
		this.symTab = symTab;
		this.instTab = instTab;
		this.locLength = 0;//토큰테이블의 길이응 0으로 초기화
		tokenList = new ArrayList<Token>();//토큰 리스트 객체를 만들어줌
	}
	
	/**
	 * 일반 문자열을 받아서 Token단위로 분리시켜 tokenList에 추가한다.
	 * @param line : 분리되지 않은 일반 문자열
	 */
	public void putToken(String line) {
		tokenList.add(new Token(line));
	}
	
	/**
	 * 추가부분
	 * tokenList 앞부분에 있는 토큰을 빼냄
	 */
	public void removeToken() {
		int size = tokenList.size();
		tokenList.remove(size-1);
	}
	
	/**
	 * tokenList에서 index에 해당하는 Token을 리턴한다.
	 * @param index
	 * @return : index번호에 해당하는 코드를 분석한 Token 클래스
	 */
	public Token getToken(int index) {
		return tokenList.get(index);
	}
	
	/**
	 * Pass2 과정에서 사용한다.
	 * instruction table, symbol table 등을 참조하여 objectcode를 생성하고, 이를 저장한다.
	 * @param index
	 */
	public void makeObjectCode(int index){
		Instruction  searchInst;
		Token searchToken;
		searchToken = tokenList.get(index);
		String data = "";//생성되는 기계어 코드를 저장할 공간
		String data2 = "";//생성되는 기계어 코드를 저장할 공간(두 문자열을 합쳐야 할 경우만 사용 )
		int size;
		int i;
		if(searchToken.byteSize == 0)//이러면 기계어 코드가 존재하지 않음
		{
			searchToken.objectCode = data;
		}
		else if(searchToken.byteSize == -2)//WORD명령어 처리
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
				//토큰의 값을 설정
				searchToken.objectCode = data2;
				searchToken.byteSize = 3;
			}
			else
			{
				//참조되어있는 값일 경우 기계어 코드는 0으로 설정
				if((symTab.searchRef(searchToken.operand[0]) == 1) && (symTab.searchRef(searchToken.operand[1].substring(1)) == 1))
				{
					size = data.length();
					for(i = 0; i < 6-size;i++)
					{
						data2 += '0';
					}
					data2 += data;
					//토큰의 값을 설정
					searchToken.objectCode = data2;
					searchToken.byteSize = 3;
				}
			}
		}
		else if(searchToken.byteSize == -3)//BYTE명령어 처리
		{
			char c;
			int cNum;
			if(searchToken.operand[0].charAt(0) == 'X')//16진법이면 그대로 저장
			{
				data += searchToken.operand[0].substring(2,searchToken.operand[0].length()-1);
				searchToken.byteSize = data.length()/2;
				searchToken.objectCode = data;
			}
			else if(searchToken.operand[0].charAt(0) == 'C')//문자열이면 아스키코드 형태로 바꾸어 저장
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
		else if(searchToken.byteSize == -4)//2형식일 경우
		{
			searchInst = instTab.search(searchToken.operator);
			data += decimalToHex(searchInst.opcode);
			//명령어코드 생성
			for(i = data.length() ; i < 2; i++)
			{
				data2 += '0';
			}
			data2 += data;
			//레지스터 부분 고려
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
		else if(searchToken.byteSize == -5)//3형식일 경우
		{
			
			//기계어 코드 생성
			int instOp = 0;//명령어 코드값을 저장
			int charTemp = 0;//문자열에 들어갈 한 단어의 정보를 저장(xbpe의 값을 저장)
			int Gap= 0;//주소값의 차이를 저장
			searchInst = instTab.search(searchToken.operator);
			instOp = searchInst.opcode;
			//명령어코드 생성
			//nixbpe비트 배치
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
			if(Gap >= 0)//주소값 차이가 0이상이라면
			{
				data = decimalToHex(Gap);
				for(i = data.length() ; i < 3; i++)
				{
					data2 += '0';
				}
				data2 += data;
			}
			else//음수라면 따로처리
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
			int instOp = 0;//명령어 코드값을 저장
			int operandNum = 0;//피연산자의 값을 저장
			searchInst = instTab.search(searchToken.operator);
			instOp = searchInst.opcode;
			//명령어코드 생성
			//nixbpe비트 배치
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
			int instOp = 0;//명령어 코드값을 저장
			int operandNum = 0;//피연산자의 값을 저장
			searchInst = instTab.search(searchToken.operator);
			instOp = searchInst.opcode;
			//명령어코드 생성
			//nixbpe비트 배치
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
			int instOp = 0;//명령어 코드값을 저장
			int charTemp = 0;//문자열에 들어갈 한 단어의 정보를 저장(xbpe의 값을 저장)
			searchInst = instTab.search(searchToken.operator.substring(1));
			instOp = searchInst.opcode;
			int Gap= 0;//주소값의 차이를 저장
			//명령어코드 생성
			//nixbpe비트 배치
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
			
			//나머지 목적 주소에 대한 연산을 수행
			Gap = searchToken.locGap;
			if(Gap >= 0)//주소값 차이가 0이상이라면
			{
				data = decimalToHex(Gap);
				for(i = data.length() ; i < 5; i++)
				{
					data2 += '0';
				}
				data2 += data;
			}
			else//음수라면 따로처리
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
			if(searchToken.operator.charAt(1) == 'C')//문자열형태의 리터럴일 경우
			{
				temp = searchToken.operator.substring(3,searchToken.operator.length()-1);
				for(i = 0 ; i < temp.length(); i++)//문자열에 있는 문자 하나하나마다 검사
				{
					data = decimalToHex(temp.charAt(i));
					data2 +=data; 
				}
				searchToken.objectCode = data2;
				searchToken.byteSize = data2.length()/2;
				
			}
			else if(searchToken.operator.charAt(1) == 'X')//16진수 형태의 리터럴일 경우
			{
				data = searchToken.operator.substring(3,searchToken.operator.length()-1);
				searchToken.objectCode = data;
				searchToken.byteSize = data.length()/2;
			}
		}
	}
	

	/**
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
	 * index번호에 해당하는 object code를 리턴한다.
	 * @param index
	 * @return : object code
	 */
	public String getObjectCode(int index) {
		return tokenList.get(index).objectCode;
	}
	
}

/**
 * 각 라인별로 저장된 코드를 단어 단위로 분할한 후  의미를 해석하는 데에 사용되는 변수와 연산을 정의한다. 
 * 의미 해석이 끝나면 pass2에서 object code로 변형되었을 때의 바이트 코드 역시 저장한다.
 */
class Token{
	//의미 분석 단계에서 사용되는 변수들
	int location;
	String label;
	String operator;
	String[] operand;
	String comment;
	char nixbpe;

	// object code 생성 단계에서 사용되는 변수들 
	String objectCode;
	int byteSize;
	
	//추가부분
	int locGap;//현재 토큰의 pc상태와 목표주소간의 차이를 저장(오브젝트 코드 생성시 사용)
	
	/**
	 * 클래스를 초기화 하면서 바로 line의 의미 분석을 수행한다. 
	 * @param line 문장단위로 저장된 프로그램 코드
	 */
	public Token(String line) {
		//initialize 추가
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
	 * line의 실질적인 분석을 수행하는 함수. Token의 각 변수에 분석한 결과를 저장한다.
	 * @param line 문장단위로 저장된 프로그램 코드.
	 */
	public void parsing(String line) {
		int i;
		int length;
		int size;
		String[] s = line.split("\t");
		size = s.length;
		
		if(size >= 1)
		{
			if(!s[0].equals("\t"))//label부분 저장 
			{
				this.label = s[0];
			}
		}
		
		if(size >= 2)
		{
			if(!s[1].equals("\t"))//연산자 부분 저장
			{
				this.operator = s[1];
			}
		}
		
		if(size >= 3)
		{
			if(!s[2].equals("\t"))//피연산자를 저장
			{
				int check = 0;
				char temp;
				String buff = "";
				length = s[2].length();
				for(i = 0 ; i < length; i++)//피연산자의 갯수만큼 분리시켜 주기 위해서 문자가 ','혹은 다른 연산자인지 확인해주는 작업
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
			if(!s[3].equals("\t"))//COMMENT부분 저장 
			{
				this.comment = s[3];
			}
		}
	}
	
	/** 
	 * n,i,x,b,p,e flag를 설정한다. <br><br>
	 * 
	 * 사용 예 : setFlag(nFlag, 1); <br>
	 *   또는     setFlag(TokenTable.nFlag, 1);
	 * 
	 * @param flag : 원하는 비트 위치
	 * @param value : 집어넣고자 하는 값. 1또는 0으로 선언한다.
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
	 * 원하는 flag들의 값을 얻어올 수 있다. flag의 조합을 통해 동시에 여러개의 플래그를 얻는 것 역시 가능하다 <br><br>
	 * 
	 * 사용 예 : getFlag(nFlag) <br>
	 *   또는     getFlag(nFlag|iFlag)
	 * 
	 * @param flags : 값을 확인하고자 하는 비트 위치
	 * @return : 비트위치에 들어가 있는 값. 플래그별로 각각 32, 16, 8, 4, 2, 1의 값을 리턴할 것임.
	 */
	public int getFlag(int flags) {
		return this.nixbpe & flags;
	}
	
	
}
