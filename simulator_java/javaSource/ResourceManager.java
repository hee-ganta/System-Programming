package SP18_simulator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;

import javax.annotation.processing.FilerException;




/**
 * ResourceManager는 컴퓨터의 가상 리소스들을 선언하고 관리하는 클래스이다.
 * 크게 네가지의 가상 자원 공간을 선언하고, 이를 관리할 수 있는 함수들을 제공한다.<br><br>
 * 
 * 1) 입출력을 위한 외부 장치 또는 device<br>
 * 2) 프로그램 로드 및 실행을 위한 메모리 공간. 여기서는 64KB를 최대값으로 잡는다.<br>
 * 3) 연산을 수행하는데 사용하는 레지스터 공간.<br>
 * 4) SYMTAB 등 simulator의 실행 과정에서 사용되는 데이터들을 위한 변수들. 
 * <br><br>
 * 2번은 simulator위에서 실행되는 프로그램을 위한 메모리공간인 반면,
 * 4번은 simulator의 실행을 위한 메모리 공간이라는 점에서 차이가 있다.
 */
public class ResourceManager{
	/**
	 * 디바이스는 원래 입출력 장치들을 의미 하지만 여기서는 파일로 디바이스를 대체한다.<br>
	 * 즉, 'F1'이라는 디바이스는 'F1'이라는 이름의 파일을 의미한다. <br>
	 * deviceManager는 디바이스의 이름을 입력받았을 때 해당 이름의 파일 입출력 관리 클래스를 리턴하는 역할을 한다.
	 * 예를 들어, 'A1'이라는 디바이스에서 파일을 read모드로 열었을 경우, hashMap에 <"A1", scanner(A1)> 등을 넣음으로서 이를 관리할 수 있다.
	 * <br><br>
	 * 변형된 형태로 사용하는 것 역시 허용한다.<br>
	 * 예를 들면 key값으로 String대신 Integer를 사용할 수 있다.
	 * 파일 입출력을 위해 사용하는 stream 역시 자유로이 선택, 구현한다.
	 * <br><br>
	 * 이것도 복잡하면 알아서 구현해서 사용해도 괜찮습니다.
	 */
	
	//장치 입출력을 위한 배열의 선언
	ArrayList<String> devName;
	ArrayList<FileChannel> devChannel;
	
	
	String obmemory = ""; //오브젝트 프로그램을 담을 메모리 
	String memory ="";//실제로 작동하는 메모리
	
	
	int[] register = new int[10];
	double register_F;
	
	SymbolTable symtabList;
	// 이외에도 필요한 변수 선언해서 사용할 것.
	
	//파일 이름
	String fileName;
	
	String pName ="";
	String pAddr = "";
	String pLength ="";
	
	//헤더 레코드 정보
	ArrayList<String> programName;
	ArrayList<String> startAddr;//엔드 레코드에서도 사용
	ArrayList<String> length;
	
	//메모리 변화량
	int target;
	int current;
	
	//파일의 대기 상태
	int check = 0;
	
	ArrayList<String> end;
	String endAddr = "";
	
	//쪼갠 기계어들을 저장
	ArrayList<String> instCode;
	ArrayList<String> relocateCode;
	
	//섹션에 대한 정보
	ArrayList<String> sectionLength;
	int sectionCount = 0;
	/**
	 * 메모리, 레지스터등 가상 리소스들을 초기화한다.
	 */
	public void initializeResource(){
		int i;
		//메모리 초기화
		this.obmemory = "";
		this.memory  ="";
		
		//레지스터 값 초기화
		for(i = 0; i < 10; i++)
		{
			this.register[i] = 0;
		}
		this.register_F = 0;
		
		//파일이름 초기화
		this.fileName = "";
		
		//헤드 레코드 정보
		programName= new ArrayList<String>();
		startAddr = new ArrayList<String>();
		length = new ArrayList<String>();
		
		//메모리 변화량 초기화
		this.target = 0;
		this.current = 0;
		
		//심볼 테이블의 생성
		symtabList = new SymbolTable();
		
		//기계어 코드 리스트의 생성
		instCode = new ArrayList<String>();
		
		//섹션 길이정보 리스트 저장
		sectionLength = new ArrayList<String>();
		
		//재배치된 기계어 코드를 저장
		relocateCode = new ArrayList<String>();
		
		end = new ArrayList<String>();
		
		//장치 정보를 넣을 리스트 초기화
		devName = new ArrayList<String>();
		devChannel = new ArrayList<FileChannel>();
		
	}
	
	/**
	 * deviceManager가 관리하고 있는 파일 입출력 stream들을 전부 종료시키는 역할.
	 * 프로그램을 종료하거나 연결을 끊을 때 호출한다.
	 * @throws IOException 
	 */
	public void closeDevice() throws IOException {
		//인풋 스트림, 아웃풋 스트림을 종료
		for(int i = 0 ; i < devChannel.size(); i++) {
			devChannel.get(i).close();
		}
	}
	
	/**
	 * 디바이스를 사용할 수 있는 상황인지 체크. TD명령어를 사용했을 때 호출되는 함수.
	 * 입출력 stream을 열고 deviceManager를 통해 관리시킨다.
	 * @param devName 확인하고자 하는 디바이스의 번호,또는 이름
	 * @throws IOException 
	 */
	//수정 필요
	public int testDevice(String devName) throws IOException {
		int res = 1;
		//객체가 없으면 만들어주고 1을 반환
		for(int i = 0 ; i < this.devName.size();i++) {
			if(this.devName.get(i).equals(devName)) {
				res = 0;
				break;
			}
		}
		
		if(res == 1) {//장치가 존재하지 않으면 생성을 해 줌
			this.devName.add(devName);
			String fName = devName + ".txt";
			//채널을 넣어줌
			Path path= Paths.get(fName);
			FileChannel fileChannel = FileChannel.open(path,StandardOpenOption.READ);
			FileChannel fileChannel1 = FileChannel.open(path,StandardOpenOption.WRITE);
			devChannel.add(fileChannel);
			devChannel.add(fileChannel1);
			//this.devInput.add(new FileInputStream(fName));
			//this.devOutput.add(new FileOutputStream(fName));
		}
		
		return res;
	}

	/**
	 * 디바이스로부터 원하는 개수만큼의 글자를 읽어들인다. RD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param num 가져오는 글자의 개수
	 * @return 가져온 데이터
	 * @throws IOException 
	 */
	public String readDevice(String devName, int num) throws IOException{
		String res = "";
		int byteCount = 0;
		//채널을 찾는 과정
		FileChannel fileChannel;
		int lo = 0;
		for(int i =0;i < this.devName.size();i++) {
			if(this.devName.get(i).equals(devName)) {
				lo = i;
				break;
			}
		}
		
		fileChannel = devChannel.get(lo+lo);
		ByteBuffer buffer = ByteBuffer.allocate(num);//한문자씩 받아들이기 위한 버퍼의 선언
		Charset charset = Charset.defaultCharset();
		byteCount = fileChannel.read(buffer);
		buffer.flip();
		res = charset.decode(buffer).toString();
		
		return res;
	}

	/**
	 * 디바이스로 원하는 개수 만큼의 글자를 출력한다. WD명령어를 사용했을 때 호출되는 함수.
	 * @param devName 디바이스의 이름
	 * @param data 보내는 데이터
	 * @param num 보내는 글자의 개수
	 * @throws IOException 
	 */
	public void writeDevice(String devName, String data, int num) throws IOException{
		String res = "";

		FileChannel fileChannel;
		int lo = 0;
		for(int i =0;i < this.devName.size();i++) {
			if(this.devName.get(i).equals(devName)) {
				lo = i;
				break;
			}
		}
		
		ByteBuffer buffer = ByteBuffer.allocate(1);//한문자씩 받아들이기 위한 버퍼의 선언
		Charset charset = Charset.defaultCharset();
		fileChannel =  devChannel.get(lo+lo+1);
		res += data.substring(0,num);
		buffer = ByteBuffer.allocate(res.length());
		buffer = charset.encode(res);
		fileChannel.write(buffer);
		
		
	}
	
	/**
	 * 메모리의 특정 위치에서 원하는 개수만큼의 글자를 가져온다.
	 * @param location 메모리 접근 위치 인덱스
	 * @param num 데이터 개수
	 * @return 가져오는 데이터
	 */
	public String getMemory(int location, int num){
		String temp = this.memory.substring(location, location+num);
		int i;
		String res ="";
		for(i = 0 ; i < temp.length(); i++) {
			if(temp.charAt(i) == 'x') {
				break;
			}
			res += temp.charAt(i);
		}
		return res;
	}

	/**
	 * 메모리의 특정 위치에 원하는 개수만큼의 데이터를 저장한다. 
	 * @param locate 접근 위치 인덱스
	 * @param data 저장하려는 데이터
	 * @param num 저장하는 데이터의 개수
	 */
	public void setMemory(int locate, String data, int num){
		int i;
		String temp1 = null;
		String temp2 = "";
		String temp3 = null;
		temp1 = this.memory.substring(0,locate);
		temp3 = this.memory.substring(locate+num);
		for(i= 0 ; i < num-data.length(); i++) {
			temp2 += '0';
		}
		temp2 += data;
		this.memory = temp1+temp2+temp3;
	}

	/**
	 * 번호에 해당하는 레지스터가 현재 들고 있는 값을 리턴한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터 분류번호
	 * @return 레지스터가 소지한 값
	 */
	public int getRegister(int regNum){
		return this.register[regNum];
		
	}

	/**
	 * 번호에 해당하는 레지스터에 새로운 값을 입력한다. 레지스터가 들고 있는 값은 문자열이 아님에 주의한다.
	 * @param regNum 레지스터의 분류번호
	 * @param value 레지스터에 집어넣는 값
	 */
	public void setRegister(int regNum, int value){
		if(regNum != 6) {
			register[regNum] = value;
		}
		else {
			register_F = value;
		}
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. int값을 char[]형태로 변경한다.
	 * @param data
	 * @return
	 */
	public char[] intToChar(int data){
		String temp;
		temp = Integer.toString(data);
		char[] res = new char[1000];
		for(int i = 0; i < temp.length(); i++) {
			res[i] = temp.charAt(i);
		}
		return res;
	}

	/**
	 * 주로 레지스터와 메모리간의 데이터 교환에서 사용된다. String값을 int형태로 변경한다.
	 * @param data
	 * @return
	 */
	public int byteToInt(String data){
		int res = Integer.parseInt(data);
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
}