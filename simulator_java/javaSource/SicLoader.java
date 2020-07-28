package SP18_simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * SicLoader는 프로그램을 해석해서 메모리에 올리는 역할을 수행한다. 이 과정에서 linker의 역할 또한 수행한다. 
 * <br><br>
 * SicLoader가 수행하는 일을 예를 들면 다음과 같다.<br>
 * - program code를 메모리에 적재시키기<br>
 * - 주어진 공간만큼 메모리에 빈 공간 할당하기<br>
 * - 과정에서 발생하는 symbol, 프로그램 시작주소, control section 등 실행을 위한 정보 생성 및 관리
 */
public class SicLoader {
	ResourceManager rMgr;
	InstTable instTab;
	int memIdx = 0;
	String locAdd = "000000";
	String addrMem = "";
	ArrayList<Integer> textStart;//시작 주소에 대한 정보 저장
	ArrayList<Integer> textLen;//텍스트 길이 저장
	
	
	public SicLoader(ResourceManager resourceManager) {
		setResourceManager(resourceManager);
		try {
			instTab = new InstTable("inst.data");
		} catch (IOException e) {//섹션에 대한 정보
			ArrayList<String> sectionLength;
			int sectionCount = 0;
			e.printStackTrace();
		}
		textStart = new ArrayList<Integer>();
		textLen = new ArrayList<Integer>();
	}

	/**
	 * Loader와 프로그램을 적재할 메모리를 연결시킨다.
	 * @param rMgr
	 */
	public void setResourceManager(ResourceManager resourceManager) {
		this.rMgr=resourceManager;
		rMgr.initializeResource();
	}
	
	/**
	 * object code를 읽어서 load과정을 수행한다. load한 데이터는 resourceManager가 관리하는 메모리에 올라가도록 한다.
	 * load과정에서 만들어진 symbol table 등 자료구조 역시 resourceManager에 전달한다.
	 * @param objectCode 읽어들인 파일
	 * @throws IOException 
	 */
	public void load(File objectCode) throws IOException{
		FileReader filereader = new FileReader(objectCode); //입력 스트림 생성
		int singleCh = 0;
		int size;
		while((singleCh = filereader.read())!=-1) {
			this.rMgr.obmemory += (char)singleCh;//메모리에다가 로드를 시킴
		}
	};
	
	public void action() {
		this.search();//분석작업
		this.search();
		this.search();
				
		
		int codeCheck = 0;
		int preLen =0;
		
		//실질적인 메모리 작업 수행
		for(int i = 0; i < rMgr.sectionCount; i++) {
			int len = trans(rMgr.sectionLength.get(i));
			int textCount = 0;
			int textIdx = 0;
			int tLen = 0;
			while(!rMgr.instCode.get(codeCheck).equals("END")) {
				if(textCount != textLen.get(textIdx)) {
					rMgr.memory += rMgr.instCode.get(codeCheck);
					textCount += rMgr.instCode.get(codeCheck).length();//길이를 누적
					tLen += rMgr.instCode.get(codeCheck).length();//계속해서 누적
				}
				else {
					textIdx++;
					if(textStart.get(textIdx)*2 > tLen) {
						//저장공간으로 채워버림
						fillBlank(tLen,textStart.get(textIdx)*2);
					}
					textCount = 0;//초기화
					rMgr.memory += rMgr.instCode.get(codeCheck);
					textCount += rMgr.instCode.get(codeCheck).length();//길이를 누적
					tLen += textCount;//계속해서 누적
				}
				codeCheck++;
			}
			//저장공간을 잡아줌
			fillBlank(rMgr.memory.length(),preLen +len*2);
			
			preLen += len*2;
			codeCheck++;
		}
		
		//수정 레코드 작업
		for(int i = 0 ; i < rMgr.symtabList.modiLoc.size();i++) {//수정할거 있는것만
			int loc = hexToDecimal(rMgr.symtabList.modiLoc.get(i));
			int num = hexToDecimal(rMgr.symtabList.modiNum.get(i));
			if(num == 5) {
				String aim ="";
				aim = rMgr.symtabList.search(rMgr.symtabList.modiName.get(i).substring(1));//위치를 찾아냄
				if(aim.charAt(0) != 'X') {//비어있지 않다면 연산을 수행
					String t1 = rMgr.getMemory(loc*2+1, 5);
					String t2 = aim;
					if(rMgr.symtabList.modiName.get(i).charAt(0) == '+') {//더하기 연산을 수행
						int r = hexToDecimal(t1) + hexToDecimal(t2);
						String result = decimalToHex(r);
						String result2 = "";
						for(int j = result.length() ; j < 5; j++) {
							result2 += '0';
						}
						result2 += result;
						rMgr.setMemory(loc*2+1, result2, 5);
					}
					else if(rMgr.symtabList.modiName.get(i).charAt(0) == '-') {//빼기 연산을 수행
						int r = hexToDecimal(t1) - hexToDecimal(t2);
						String result = decimalToHex(r);
						String result2 = "";
						for(int j = result.length() ; j < 5; j++) {
							result2 += '0';
						}
						result2 += result;
						rMgr.setMemory(loc*2+1, result2, 5);
					}
				}
				else {
					rMgr.setMemory(loc*2+1, aim.substring(1), 5);
				}
			}
			else if(num ==6){
				String aim ="";
				aim = rMgr.symtabList.search(rMgr.symtabList.modiName.get(i).substring(1));//위치를 찾아냄
				if(aim.charAt(0) != 'X') {
					String t1 = rMgr.getMemory(loc*2, 6);
					String t2 = aim;
					if(rMgr.symtabList.modiName.get(i).charAt(0) == '+') {//더하기 연산을 수행
						int r = hexToDecimal(t1) + hexToDecimal(t2);
						String result = decimalToHex(r);
						String result2 = "";
						for(int j = result.length() ; j < 6; j++) {
							result2 += '0';
						}
						result2 += result;
						rMgr.setMemory(loc*2, result2, 6);
					}
					else if(rMgr.symtabList.modiName.get(i).charAt(0) == '-') {//빼기 연산을 수행
						int r = hexToDecimal(t1) - hexToDecimal(t2);
						String result = decimalToHex(r);
						String result2 = "";
						for(int j = result.length() ; j < 6; j++) {
							result2 += '0';
						}
						result2 += result;
						rMgr.setMemory(loc*2, result2, 6);
					}
				}
				else {
					rMgr.setMemory(loc*2, aim, 6);
				}
			}
		}
		
		int p = 0;
		//재배치된 기계어 코드를 저장
		for(int i = 0 ; i < rMgr.instCode.size(); i++) {
			String c = rMgr.instCode.get(i);
			String buffer ="";
			int len = c.length();
			if(!c.equals("END")) {
				while(rMgr.memory.charAt(p) == 'x') {//저장공간이면 건너뜀
					p++;
				}
				buffer = rMgr.memory.substring(p,p+len);
				rMgr.relocateCode.add(buffer);
				p = p+len;
			}
			else {
				rMgr.relocateCode.add("END");
				if(!(i == rMgr.instCode.size()-1))
				{ 
					while(!rMgr.instCode.get(i+1).equals(rMgr.memory.substring(p,p+rMgr.instCode.get(i+1).length()))) {
						p++;
					}
				}
			}
			
		}
	}
	
	
	public void fillBlank(int start, int end) {
		for(int i = start; i < end; i++) {
			rMgr.memory += 'x';
		}
	}
	
	//16진수로된 문자열을 10진수로 변환하여 리턴
	public int trans(String str) {
		int res = 0;
		int a = 5;
		char t;
		int num = 0;
		for(int i = 0 ; i <= 5; i++) {
			t = str.charAt(i);
			if((t>=48) && (t <= 57)) {
				num = t- 48;
			}
			else {
				num = t-55;
			}
			
			res += Math.pow(16, a) * num;
			a--;
			
		}
		return res;
	}
	
	//헤드 레코드의 분석
	public void searchHead() {
		rMgr.sectionCount++;
		String pn = "";
		String sd ="";
		String l = "";
		pn = this.rMgr.obmemory.substring(memIdx+1,memIdx+7);
		pn = pn.trim();
		rMgr.programName.add(pn);
		if(rMgr.startAddr.size() == 0) {
			this.rMgr.startAddr.add(this.rMgr.obmemory.substring(memIdx+7,memIdx+13));
			sd = this.rMgr.obmemory.substring(memIdx+7,memIdx+13);
		}
		else {
			rMgr.startAddr.add(locAdd);
			sd = locAdd;
		}
		l = this.rMgr.obmemory.substring(memIdx+13,memIdx+19);
		this.rMgr.length.add(this.rMgr.obmemory.substring(memIdx+13,memIdx+19));
		rMgr.sectionLength.add(l);
		locAdd = hexAdd(l,locAdd);
		this.rMgr.symtabList.putSymbol(pn,sd);
		this.memIdx += 21;
	}
	
	//define 레코드 분석
	public void searchDefine() {
		if(this.rMgr.obmemory.charAt(this.memIdx) == 'D') {
			this.memIdx++;
			while(this.rMgr.obmemory.charAt(memIdx) != '\r') {
				String temp1 = rMgr.obmemory.substring(memIdx,memIdx+6);
				String temp2 = rMgr.obmemory.substring(memIdx+6,memIdx+12);
				this.rMgr.symtabList.putSymbol(temp1,temp2);
				memIdx = memIdx+12;
			}
			memIdx = memIdx+2;
		}
	}
	
	//reference 레코드의 분석
	public void searchRefer() {
		if(this.rMgr.obmemory.charAt(memIdx) == 'R') {
			memIdx++;
			while(rMgr.obmemory.charAt(memIdx) != '\r') {
				rMgr.symtabList.putRef(rMgr.obmemory.substring(memIdx,memIdx+6));
				memIdx += 6;
			}
			memIdx+=2;
		}
	}
	
	//text레코드의 분석
	public void searchText() {
		while((rMgr.obmemory.charAt(memIdx) !='M') && (rMgr.obmemory.charAt(memIdx) !='E')) {
			if(rMgr.obmemory.charAt(memIdx) == 'T') {
				String buffer = "";
				String buffer2 = "";
				char search;
				int len;
				memIdx++;
				//시작 주소 정보를 저장
				textStart.add(hexToDecimal(rMgr.obmemory.substring(memIdx, memIdx+6)));
				memIdx += 6;
				len = hexToDecimal(rMgr.obmemory.substring(memIdx, memIdx+2));
				textLen.add(len*2);
				len = len * 2;
				memIdx += 2;
				
				while(rMgr.obmemory.charAt(memIdx) != '\r') {
					String opcheck;
					buffer = "";
					buffer2 = "";
					buffer += rMgr.obmemory.charAt(memIdx);
					buffer2 += rMgr.obmemory.charAt(memIdx);
					memIdx++;
					search = rMgr.obmemory.charAt(memIdx);
					buffer += rMgr.obmemory.charAt(memIdx);
					memIdx++;
					
					if(checkBit(search,0) == 1) {
						int tNum;
						if((search>=48) && (search <= 57)) {
							tNum = search - 48;
						}
						else {
							tNum = search -55;
						}
						tNum--;
						if((tNum>=0) && (tNum <=9)) {
							tNum = tNum + 48;
						}
						else {
							tNum = tNum + 55;
						}
						search = (char)tNum;
					}
					
					if(checkBit(search,1) == 1) {
						int tNum;
						if((search>=48) && (search <= 57)) {
							tNum = search - 48;
						}
						else {
							tNum = search -55;
						}
						tNum -=2;
						if((tNum>=0) && (tNum <=9)) {
							tNum = tNum + 48;
						}
						else {
							tNum = tNum + 55;
						}
						search = (char)tNum;
					}
					buffer2 += search;
					opcheck = instTab.isInclude(buffer2);
					Instruction s = null;
					if(opcheck != "") {
						s = instTab.search(opcheck);
						char t;
						t = rMgr.obmemory.charAt(memIdx);
						if(s.format != 2) {
							if((checkBit(t,2) == 1) && (checkBit(t,0) == 1)) {
								opcheck ="";
							}
							else if((checkBit(t,1) == 1) && (checkBit(t,0) == 1)) {
								opcheck ="";
							}
							else if(t =='\r') {
								opcheck = "";
							}
						}
					}
					search =  rMgr.obmemory.charAt(memIdx);
					if(opcheck != "") {//명령어구가 있는 것이라 판단하고 e비트가 있는지 체크
						//2형식인지 3형식인지 판별
						Instruction temp;
						temp = instTab.search(opcheck);
						if(temp.format == 2) {//2형식일때
							buffer += rMgr.obmemory.substring(memIdx,memIdx+2);
							memIdx+=2;
						}
						else {
							search = rMgr.obmemory.charAt(memIdx);
							memIdx++;
							buffer += search;
							if(checkBit(search,0) == 1) {//확장비트가 있으면
								buffer += rMgr.obmemory.substring(memIdx,memIdx+5);
								memIdx+=5;
							}
							else {//확장 비트가 없으면
								buffer += rMgr.obmemory.substring(memIdx,memIdx+3);
								memIdx += 3;
							}
						}
						rMgr.instCode.add(buffer);
						
					}
					else {//명령어구가 없으면 포함시키지 않음
						while(rMgr.obmemory.charAt(memIdx) != '\r') {
							buffer += rMgr.obmemory.charAt(memIdx);
							memIdx++;
						}
						rMgr.instCode.add(buffer);
					}
				}
				
				memIdx += 2;
			}
			
		}
	}
	
	//16진수를 10진수로
	public int hexToDecimal(String hex) {
		int num =0;
		int size = hex.length()-1;
		char c;
		for(int i = 0 ;i < hex.length(); i++) {
			c = hex.charAt(i);
			if((c >= 48) && (c <= 57)){
				c = (char) (c -48);
			}
			else {
				c = (char)(c-55);
			}
			num += Math.pow(16, size) * c;
			size += -1;
		}
		return num;
	}
	
	/**
	 * 해당 위치에 비트가 있는지 없는지 판별
	 * @return 1,0
	 */
	public int checkBit(char num, int location) {
		int res = 0;
		int check;
		//num을 숫자로 바꿔주는 작업
		if((num >= 48) && (num <= 57)) {
			check = num - 48;
		}
		else {
			check = num - 55;
		}
		
		for(int i = 0 ; i < location; i++)
		{
			check = check >> 1;
		}
		
		//이러면 비트가 존재
		if((check % 2) == 1) {
			res = 1;
		}
		
		return res;
	}
	
	//modification레코드의 분석
	public void searchModi() {
		while(rMgr.obmemory.charAt(memIdx) != 'E') {
			String buffer ="";
			memIdx++;
			buffer = hexAdd(rMgr.startAddr.get(rMgr.startAddr.size()-1),rMgr.obmemory.substring(memIdx,memIdx+6));
			rMgr.symtabList.modiLoc.add(buffer);
			memIdx += 6;
			rMgr.symtabList.modiNum.add(rMgr.obmemory.substring(memIdx,memIdx+2));
			memIdx += 2;
			buffer = "";
			while(rMgr.obmemory.charAt(memIdx) != '\r') {
				buffer += rMgr.obmemory.charAt(memIdx);
				memIdx++;
			}
			
			rMgr.symtabList.modiName.add(buffer);
			memIdx +=2;
		}
		
	}
	
	//end레코드의 분석
	public void searchEnd() {
		if(rMgr.obmemory.charAt(memIdx) == 'E') {
			memIdx++;
		}
		rMgr.end.add(rMgr.startAddr.get(rMgr.startAddr.size()-1));
		while(rMgr.obmemory.charAt(memIdx) != '\r'){
			memIdx++;
		}
		rMgr.instCode.add("END");//섹션의 끝을 알려줌
	}
	
	//공백 공간을 없애줌
	public void removeBlank() {
		while((rMgr.obmemory.charAt(memIdx) == '\r') || (rMgr.obmemory.charAt(memIdx) == '\n')) {
			memIdx++;
			if(rMgr.obmemory.length() == memIdx) {
				break;
			}
		}
	}
	
	//분석작업
	public void search() {
		this.searchHead();
		this.searchDefine();
		this.searchRefer();
		this.searchText();
		this.searchModi();
		this.searchEnd();
		this.removeBlank();
	}
	
	/**
	 * 10진 정수를 16진수 문자열로 변환
	 * @return 16진수 문자열
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
	
	//16진수 문자열끼리 더함
	public String hexAdd(String str1, String str2) {
		int temp;
		String res = "";
		String res1 = "";
		temp =  hexToDecimal(str1) + hexToDecimal(str2);
		res = decimalToHex(temp);
		for(int i =res.length() ; i< 6;i++) {
			res1 += '0';
		}
		res1 += res;
		return res1;
	}
	

}
