package SP18_simulator;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * 모든 instruction의 정보를 관리하는 클래스. instruction data들을 저장한다. <br>
 * 또한 instruction 관련 연산, 예를 들면 목록을 구축하는 함수, 관련 정보를 제공하는 함수 등을 제공 한다.
 */
public class InstTable {
	/** 
	 * inst.data 파일을 불러와 저장하는 공간.
	 *  명령어의 이름을 집어넣으면 해당하는 Instruction의 정보들을 리턴할 수 있다.
	 */
	HashMap<String, Instruction> instMap;
	
	//명령어 이름과 코드를 출력하기 위해 리스트 사용
	ArrayList<String> instName;
	ArrayList<String> opList;
	
	
	/**
	 * 클래스 초기화. 파싱을 동시에 처리한다.
	 * @param instFile : instuction에 대한 명세가 저장된 파일 이름
	 * @throws IOException 
	 */
	public InstTable(String instFile) throws IOException {
		instMap = new HashMap<String, Instruction>();
		instName = new ArrayList<String>();
		opList = new ArrayList<String>();
		openFile(instFile);
	}
	
	/**
	 * 입력받은 이름의 파일을 열고 해당 내용을 파싱하여 instMap에 저장한다.
	 * @throws IOException 
	 */
	public void openFile(String fileName) throws IOException {
		Path path = Paths.get(fileName);//파일의 경로를 설정
		String data = "";//파일에서 한 문자열을 담을 변수
		
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
			if(byteCount == -1) //파일을 전부 읽었으면 반복문 탈출
			{
				break;
			}
			
			if(temp.equals("\r"))//문장 단위로 자가업을 수행
			{
				
				Instruction newInst;
				newInst = new Instruction(data);
				instMap.put(newInst.instruction, newInst);//명령어 테이블에 정보를 추가
				instName.add(newInst.instruction);//명령어 이름을 저장
				opList.add(newInst.opcode);//명령어 코드를 저장
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
	
	/**
	 *찾고자 하는 명령어의 객체를 반환 
	 */
	public Instruction search(String name)
	{
		return instMap.get(name);	
	}
	
	/**
	 * opcode를 사용하여 해당하는 명령어 이름을 반환
	 */
	public String findInst(String search) {
		String res = "";
		for(int i = 0 ; i < opList.size(); i++) {
			if(opList.get(i).equals(search)) {
				res = instName.get(i);
				break;
			}
		}
		return res;
	}
	
	/*
	 * 해당 명령어가 저장되어있는지 조사
	 * @retrun 명령어 이름
	 */
	public String isInclude(String code) {
		String name = "";
		for(int i =0 ; i < opList.size(); i++) {
			if(code.equals(opList.get(i))) {
				name = instName.get(i);
				break;
			}
		}
		return name;
	}
	

}
/**
 * 명령어 하나하나의 구체적인 정보는 Instruction클래스에 담긴다.
 * instruction과 관련된 정보들을 저장하고 기초적인 연산을 수행한다.
 */
class Instruction {
	
	//변수들의 선언
	String instruction;
	String opcode = "";
	int numberOfOperand;
	
	/** instruction이 몇 바이트 명령어인지 저장. 이후 편의성을 위함 */
	int format;
	
	/**
	 * 클래스를 선언하면서 일반문자열을 즉시 구조에 맞게 파싱한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * 일반 문자열을 파싱하여 instruction 정보를 파악하고 저장한다.
	 * @param line : instruction 명세파일로부터 한줄씩 가져온 문자열
	 */
	public void parsing(String line) {
		String[] info = line.split("\t");
		
		instruction = info[0];//명령어의 이름을 저장
		/*명령어의 형식을 저장*/
		if(info[1].equals("3/4"))//편의상 3으로 저장
		{
			format = 3;
		}
		else if(info[1].equals("2"))//2형식은 2로 저장
		{
			format = 2;
		}
		
		opcode += info[2].substring(0,2);
		
		/*피연산자 갯수를 저장*/
		numberOfOperand = Integer.parseInt(info[3]);
		
	}
	
	
}
