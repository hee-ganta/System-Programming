package SP18_simulator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * 시뮬레이터로서의 작업을 담당한다. VisualSimulator에서 사용자의 요청을 받으면 이에 따라
 * ResourceManager에 접근하여 작업을 수행한다.  
 * 
 * 작성중의 유의사항 : <br>
 *  1) 새로운 클래스, 새로운 변수, 새로운 함수 선언은 얼마든지 허용됨. 단, 기존의 변수와 함수들을 삭제하거나 완전히 대체하는 것은 지양할 것.<br>
 *  2) 필요에 따라 예외처리, 인터페이스 또는 상속 사용 또한 허용됨.<br>
 *  3) 모든 void 타입의 리턴값은 유저의 필요에 따라 다른 리턴 타입으로 변경 가능.<br>
 *  4) 파일, 또는 콘솔창에 한글을 출력시키지 말 것. (채점상의 이유. 주석에 포함된 한글은 상관 없음)<br>
 * 
 * <br><br>
 *  + 제공하는 프로그램 구조의 개선방법을 제안하고 싶은 분들은 보고서의 결론 뒷부분에 첨부 바랍니다. 내용에 따라 가산점이 있을 수 있습니다.
 */
public class SicSimulator {
	ResourceManager rMgr;
	SicLoader sLoder;
	
	InstTable instTable;
	//명령어의 갯수를 저장
	int instCount = 0;
	int pcCounter = 0;
	int base = 0;
	int memIdx = 0;
	int obProCount = 0;//오브젝트 프로그램이 몇개있는지 확인
	int c = 0;
	
	String target = "";
	String code = "";//실행되고 있는 기계어 코드를 저장
	
	String usingDevice ="";
	
	ArrayList<String> log;//명령어 정보를 저장
	

	public SicSimulator(ResourceManager resourceManager,SicLoader sicloader) {
		// 필요하다면 초기화 과정 추가
		this.rMgr = resourceManager;
		this.sLoder = sicloader;
		try {
			this.instTable = new InstTable("inst.data");
		} catch (IOException e) {
			e.printStackTrace();
		}
		log =new ArrayList<String>();
	}

	/**
	 * 레지스터, 메모리 초기화 등 프로그램 load와 관련된 작업 수행.
	 * 단, object code의 메모리 적재 및 해석은 SicLoader에서 수행하도록 한다. 
	 * @throws IOException 
	 */
	public void load(File program) throws IOException {
		/* 메모리 초기화, 레지스터 초기화 등*/
		//레지스터 초기화
		sLoder.load(program);
		sLoder.action();
		
	}
	

	/**
	 * 1개의 instruction이 수행된 모습을 보인다. 
	 * ResourceManager에 있는 instCode리스트에 있는 명령어들을 순차적으로 수행
	 * @throws IOException 
	 */
	public void oneStep() throws IOException {
		//앞의 두가지 비트를 읽어오고 판단
		String instName = "";
		String data ="";
		int temp;
		String searchOp ="";
		
		int n = 0,i = 0 ,x = 0 ,b = 0 ,p = 0 ,e = 0;
		code = rMgr.relocateCode.get(instCount);
		usingDevice ="";
		
		//헤드 레코드 생성
		rMgr.pName = rMgr.programName.get(obProCount);
		rMgr.pAddr = rMgr.startAddr.get(obProCount);
		rMgr.pLength = rMgr.length.get(obProCount);
		rMgr.endAddr = rMgr.end.get(obProCount);
		
		if(code.equals("END")) {//메모리에 있는 여분 공간을 건너뜀
			if(!(instCount == rMgr.instCode.size()-1))
			{
				while(!rMgr.instCode.get(instCount+1).equals(rMgr.memory.substring(memIdx,memIdx+rMgr.instCode.get(instCount+1).length()))) {
					memIdx++;
				}
			}
			obProCount++;
		}
		
		//명령어가 아님으로 건너뜀
		if(code.length() == 2) {
			instCount++;
			return;
		}
		
		//현재 명령어가 있는 주소
		rMgr.current = memIdx;
		
		//메모리 시작 단계와 pc를 설정
		if(code.length() == 4) {
			rMgr.register[8] = memIdx/2 + 2;
			//메모리의 위치 설정
			memIdx= memIdx + 4;
		}
		else if(code.length() == 6) {
			rMgr.register[8] = memIdx/2 + 3;
			//메모리의 위치 설정
			memIdx= memIdx + 6;
		}
		else {
			rMgr.register[8] = memIdx/2 + 4;
			//메모리의 위치 설정
			memIdx= memIdx + 8;
		}
		
		temp = hexToDecimal(code.substring(1,2));
		//nixbpe판별과정
		if(checkBit(code.charAt(1),1) == 1) {
			n = 1;
			temp -= 2;
		}
		if(checkBit(code.charAt(1),0) == 1) {
			i = 1;
			temp -= 1;
		}
		if(checkBit(code.charAt(2),3) == 1) {
			x = 1;
		}
		if(checkBit(code.charAt(2),2) == 1) {
			b = 1;
		}
		if(checkBit(code.charAt(2),1) == 1) {
			p = 1;
		}
		if(checkBit(code.charAt(2),0) == 1) {
			e = 1;
		}
		
		//instName찾기
		searchOp += code.charAt(0);
		searchOp += decimalToHex(temp);
		instName = instTable.isInclude(searchOp);
		if(instName == "") {//명령어가 아님으로 건너뜀
			return;
		}
		
		//명령어의 수행
		if(instName.equals("STL")) {//L레지스터에 있는 값을 메모리에 저장
			//target을 설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x비트 작업
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target에다가 L레지스터의 정보를 저장
			data = decimalToHex(rMgr.register[2]);
			rMgr.setMemory(rMgr.target*2, data, 6);
			addLog("STL");
		}
		else if(instName.equals("JSUB")) {
			//target에 있는 주소로 감
			rMgr.setRegister(2, rMgr.register[8]);
			rMgr.target = hexToDecimal(code.substring(3));
			memIdx = rMgr.target*2;
			obProCount = 0;
			
			//찾고자 하는 명령어가 어느 섹션에 있는지 검사
			int section = -1;
			for(int j = 0; j < rMgr.startAddr.size();j++) {
				if(memIdx/2 < hexToDecimal(rMgr.startAddr.get(j))){
					section = j-1;
					break;
				}
			}
			
			if(section == -1) {
				section = rMgr.startAddr.size()-1;
			}
			
			for(int j = 0 ; j < rMgr.relocateCode.size(); j++) {//명령어를 찾아줌
				int rl =rMgr.relocateCode.get(j).length();//찾고자 하는 명령어의 길이를 계산
				data =rMgr.memory.substring(memIdx, memIdx+rl);
				if(rMgr.relocateCode.get(j).equals("END")) {
					obProCount++;
				}
				if(data.equals(rMgr.relocateCode.get(j).substring(0,rl))&& section == obProCount) {
					instCount = j-1;
					break;
				}
			}
			//rMgr.setRegister(2, memIdx/2); 
			addLog("JSUB");
		}
		else if(instName.equals("CLEAR")){
			//레지스터 값을 0으로  설정
			rMgr.target = 0;
			if(code.charAt(2) == '1') {
				rMgr.setRegister(1, 0);
			}
			else if(code.charAt(2) == '0') {
				rMgr.setRegister(0, 0);
			}
			else if(code.charAt(2) == '4') {
				rMgr.setRegister(4, 0);
			}
			else if(code.charAt(2) == '5') {
				rMgr.setRegister(5, 0);
			}
			addLog("CLEAR");
		}
		else if(instName.equals("COMP")) {
			rMgr.target = 0;
			int num = 0 ;
			if((n == 0) && (i == 1)) {
				num = hexToDecimal(code.substring(3));
			}
			else {
				if(p == 1) {
					rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
				}
				else if(b == 1) {
					rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3]*2;
				}
				num = hexToDecimal(rMgr.getMemory(rMgr.target*2, 6));
			}
			if(num == rMgr.getRegister(0)) {//flag를 설정
				rMgr.setRegister(9, 0);
			}
			else if(num > rMgr.getRegister(0)){//피연산자가 더 클 경우
				rMgr.setRegister(9, -1);
			}
			else {
				rMgr.setRegister(9, 1);
			}
			addLog("COMP");
		}
		else if(instName.equals("COMPR")) {
			rMgr.target = 0;
			int a;
			int c;
			a = Integer.parseInt(code.substring(2,3));
			c = Integer.parseInt(code.substring(3));
			if(rMgr.getRegister(a) == rMgr.getRegister(c)) {//flag를 설정
				rMgr.setRegister(9, 0);
			}
			else if(rMgr.getRegister(a) > rMgr.getRegister(c)){
				rMgr.setRegister(9, 1);
			}
			else {
				rMgr.setRegister(9, -1);
			}
			addLog("COMPR");
		}
		else if(instName.equals("J")) {
			//target설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			memIdx = rMgr.target*2;
			
			if((n == 1) && (i == 0)) {
				rMgr.target = hexToDecimal(rMgr.memory.substring(memIdx, memIdx+6));
			}
			
			memIdx = rMgr.target*2;//메모리 지점 옮김
			
			int section = -1;
			for(int j = 0; j < rMgr.startAddr.size();j++) {
				if(memIdx/2 < hexToDecimal(rMgr.startAddr.get(j))){
					section = j-1;
					break;
				}
			}
			
			if(section == -1) {
				section = rMgr.startAddr.size()-1;
			}
			
			obProCount = 0;
			for(int j = 0 ; j < rMgr.relocateCode.size(); j++) {//명령어를 찾아줌
				int rl =rMgr.relocateCode.get(j).length();//찾고자 하는 명령어의 길이를 계산
				data =rMgr.memory.substring(memIdx, memIdx+rl);
				if(rMgr.relocateCode.get(j).equals("END")) {
					obProCount++;
				}
				if(data.equals(rMgr.relocateCode.get(j).substring(0,rl)) && section == obProCount) {
					instCount = j-1;
					break;
				}
			}
			addLog("J");
		}
		else if(instName.equals("JEQ")) {
			//target설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			
			if(rMgr.getRegister(9) == 0) {//flag가 0일때 점프
				memIdx = rMgr.target*2;//메모리 지점 옮김
				obProCount = 0;
				
				//찾고자 하는 명령어가 어느 섹션에 있는지 검사
				int section = -1;
				for(int j = 0; j < rMgr.startAddr.size();j++) {
					if(memIdx/2 < hexToDecimal(rMgr.startAddr.get(j))){
						section = j-1;
						break;
					}
				}
				
				if(section == -1) {
					section = rMgr.startAddr.size()-1;
				}
				
				for(int j = 0 ; j < rMgr.relocateCode.size(); j++) {//명령어를 찾아줌
					int rl =rMgr.relocateCode.get(j).length();//찾고자 하는 명령어의 길이를 계산
					data =rMgr.memory.substring(memIdx, memIdx+rl);
					if(rMgr.relocateCode.get(j).equals("END")) {
						obProCount++;
					}
					if(data.equals(rMgr.relocateCode.get(j).substring(0,rl))&& section == obProCount) {
						instCount = j-1;
						break;
					}
				}
			}
			addLog("JEQ");
		}
		else if(instName.equals("JLT")) {
			//target설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			
			if(rMgr.getRegister(9) == -1) {//flag가 -1일때 점프
				memIdx = rMgr.target*2;//메모리 지점 옮김
				data = rMgr.memory.substring(memIdx,memIdx+6);
				obProCount = 0;
						
				//찾고자 하는 명령어가 어느 섹션에 있는지 검사
				int section = -1;
				for(int j = 0; j < rMgr.startAddr.size();j++) {
					if(memIdx/2 < hexToDecimal(rMgr.startAddr.get(j))){
						section = j-1;
						break;
					}
				}
				
				if(section == -1) {
					section = rMgr.startAddr.size()-1;
				}
				
				for(int j = 0 ; j < rMgr.relocateCode.size(); j++) {//명령어를 찾아줌
					int rl =rMgr.relocateCode.get(j).length();//찾고자 하는 명령어의 길이를 계산
					data =rMgr.memory.substring(memIdx, memIdx+rl);
					if(rMgr.relocateCode.get(j).equals("END")) {
						obProCount++;
					}
					if(data.equals(rMgr.relocateCode.get(j).substring(0,rl))&& section == obProCount) {
						instCount = j-1;
						break;
					}
				}
			}
			addLog("JLT");
		}
		else if(instName.equals("LDA")) {
			//target을 설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x비트 작업
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target에 있는 정보를 가져와서 A레지스터에 저장
			if((n == 0) && (i ==1)) {
				rMgr.setRegister(0, hexToDecimal(code.substring(3)));
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else {
				data = rMgr.getMemory(rMgr.target*2, 6);
				rMgr.setRegister(0, hexToDecimal(data));
			}
			addLog("LDA");
		}
		else if(instName.equals("LDCH")) {
			//target을 설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x비트 작업
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target에 있는 정보를 가져와서 A레지스터에 저장
			if((n == 0) && (i ==1)) {
				rMgr.setRegister(0, hexToDecimal(code.substring(3)));
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else {
				data = rMgr.getMemory(rMgr.target*2, 2);
				rMgr.setRegister(0, hexToDecimal(data));
			}
			addLog("LDA");
		}
		else if(instName.equals("LDL")) {
			//target을 설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x비트 작업
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target에 있는 정보를 가져와서 L레지스터에 저장
			//target에 있는 정보를 가져와서 A레지스터에 저장
			if((n == 0) && (i ==1)) {
				rMgr.setRegister(2, hexToDecimal(code.substring(3)));
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else {
				data = rMgr.getMemory(rMgr.target*2, 6);
				rMgr.setRegister(2, hexToDecimal(data));
			}
			addLog("LDA");
		}
		else if(instName.equals("LDT")) {
			//target을 설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x비트 작업
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target에 있는 정보를 가져와서 A레지스터에 저장
			if((n == 0) && (i ==1)) {
				rMgr.setRegister(5, hexToDecimal(code.substring(3)));
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else {
				data = rMgr.getMemory(rMgr.target*2, 6);
				rMgr.setRegister(5, hexToDecimal(data));
			}
			addLog("LDT");
		}
		else if(instName.equals("LDX")) {
			//target을 설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x비트 작업
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target에 있는 정보를 가져와서 X레지스터에 저장
			if((n == 0) && (i ==1)) {
				rMgr.setRegister(1, hexToDecimal(code.substring(3)));
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else {
				data = rMgr.getMemory(rMgr.target*2, 6);
				rMgr.setRegister(1, hexToDecimal(data));
			}
			addLog("LDA");
			addLog("LDX");
		}
		else if(instName.equals("RD")) {
			String fData ="";
			//target을 설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			data = rMgr.getMemory(rMgr.target*2, 2);//장치 이름을 가져옴
			fData = rMgr.readDevice(data, 1);//한개의 데이터를 읽어옴
			rMgr.setRegister(0, hexToDecimal(fData));//읽어온 정보를 A레지스터에 로드
			usingDevice = data;
			addLog("RD");
		}
		else if(instName.equals("RSUB")) {
			//L레지스터에 있는 주소로 감
			memIdx = rMgr.getRegister(2)*2;
			rMgr.target = memIdx/2;
			obProCount = 0;
			
			//찾고자 하는 명령어가 어느 섹션에 있는지 검사
			int section = -1;
			for(int j = 0; j < rMgr.startAddr.size();j++) {
				if(memIdx/2 < hexToDecimal(rMgr.startAddr.get(j))){
					section = j-1;
					break;
				}
			}
			
			if(section == -1) {
				section = rMgr.startAddr.size()-1;
			}
			
			for(int j = 0 ; j < rMgr.relocateCode.size(); j++) {//명령어를 찾아줌
				int rl =rMgr.relocateCode.get(j).length();//찾고자 하는 명령어의 길이를 계산
				data =rMgr.memory.substring(memIdx, memIdx+rl);
				if(rMgr.relocateCode.get(j).equals("END")) {
					obProCount++;
				}
				if(data.equals(rMgr.relocateCode.get(j).substring(0,rl))&& section == obProCount) {
					instCount = j-1;
					break;
				}
			}
			addLog("RSUB");
		}
		else if(instName.equals("STA")) {
			//target을 설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x비트 작업
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target에다가 A레지스터의 정보를 저장
			data = decimalToHex(rMgr.register[0]);
			rMgr.setMemory(rMgr.target*2, data, 6);
			addLog("STA");
		}
		else if(instName.equals("STCH")) {
			//target을 설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x비트 작업
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target에다가 A레지스터의 정보를 저장
			data = decimalToHex(rMgr.register[0]);
			rMgr.setMemory(rMgr.target*2, data, 2);
			addLog("STCH");
		}
		else if(instName.equals("STX")) {
			//target을 설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//target에다가 X레지스터의 정보를 저장
			data = decimalToHex(rMgr.register[1]);
			rMgr.setMemory(rMgr.target*2, data, 6);
			addLog("STX");
		}
		else if(instName.equals("TD")) {
			//target을 설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			String devName = rMgr.getMemory(rMgr.target*2, 2);
			if(rMgr.testDevice(devName) == 1) {//준비가 되지 않음
				rMgr.setRegister(9, 0);
			}
			else {//준비가 됨
				rMgr.setRegister(9, 1);
			}
			
			addLog("TD");
		}
		else if(instName.equals("TIX")) {
			//target설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			int num = 0;
			int xNum = 0;
			data = rMgr.getMemory(rMgr.target*2, 6);
			num = hexToDecimal(data);
			xNum = rMgr.getRegister(1)+1;//x레지스터 값을 가져옴
			if(num == xNum) {
				rMgr.setRegister(9, 0);
			}
			else if(num < xNum) {
				rMgr.setRegister(9, 1);
			}
			else {
				rMgr.setRegister(9,-1);
			}
			rMgr.setRegister(1, xNum);//x레지스터 값 하나를 늘려줌
			addLog("TIX");
		}
		else if(instName.equals("TIXR")) {
			int a;
			int c;
			a = 1;
			c = Integer.parseInt(code.substring(2,3));
			int xNum = rMgr.getRegister(a)+1;
			if(xNum == rMgr.getRegister(c)) {//flag를 설정
				rMgr.setRegister(9, 0);
			}
			else if(xNum > rMgr.getRegister(c)){
				rMgr.setRegister(9, 1);
			}
			else {
				rMgr.setRegister(9, -1);
			}
			rMgr.setRegister(1, xNum);//x레지스터 값 하나를 늘려줌
			addLog("TIXR");
		}
		else if(instName.equals("WD")) {
			int fData = 0;
			//target을 설정
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			data = rMgr.getMemory(rMgr.target*2, 2);//장치 이름을 가져옴
			fData = rMgr.getRegister(0);//A레지스터에 있는 값을 읽어옴
			rMgr.writeDevice(data, decimalToHex(fData),decimalToHex(fData).length());
			usingDevice = data;
			addLog("WD");
		}
		instCount++;
					
	}
	
	/**
	 * 남은 모든 instruction이 수행된 모습을 보인다.
	 * @throws IOException 
	 */
	public void allStep() throws IOException {
		oneStep();
		while(instCount != 0) {
			oneStep();	
		}
	}
	
	/**
	 * 각 단계를 수행할 때 마다 관련된 기록을 남기도록 한다.
	 */
	public void addLog(String log) {
		this.log.add(log);
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
		if(hex.length() >= 3) {//길이가 3이상일때만
			if(hex.charAt(0) == 'F') {
				num = (int) (num -(15*Math.pow(16, 2) + 15 * Math.pow(16, 1) + 16));
			}
		}
		return num;
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
