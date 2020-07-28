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
 * ResourceManager�� ��ǻ���� ���� ���ҽ����� �����ϰ� �����ϴ� Ŭ�����̴�.
 * ũ�� �װ����� ���� �ڿ� ������ �����ϰ�, �̸� ������ �� �ִ� �Լ����� �����Ѵ�.<br><br>
 * 
 * 1) ������� ���� �ܺ� ��ġ �Ǵ� device<br>
 * 2) ���α׷� �ε� �� ������ ���� �޸� ����. ���⼭�� 64KB�� �ִ밪���� ��´�.<br>
 * 3) ������ �����ϴµ� ����ϴ� �������� ����.<br>
 * 4) SYMTAB �� simulator�� ���� �������� ���Ǵ� �����͵��� ���� ������. 
 * <br><br>
 * 2���� simulator������ ����Ǵ� ���α׷��� ���� �޸𸮰����� �ݸ�,
 * 4���� simulator�� ������ ���� �޸� �����̶�� ������ ���̰� �ִ�.
 */
public class ResourceManager{
	/**
	 * ����̽��� ���� ����� ��ġ���� �ǹ� ������ ���⼭�� ���Ϸ� ����̽��� ��ü�Ѵ�.<br>
	 * ��, 'F1'�̶�� ����̽��� 'F1'�̶�� �̸��� ������ �ǹ��Ѵ�. <br>
	 * deviceManager�� ����̽��� �̸��� �Է¹޾��� �� �ش� �̸��� ���� ����� ���� Ŭ������ �����ϴ� ������ �Ѵ�.
	 * ���� ���, 'A1'�̶�� ����̽����� ������ read���� ������ ���, hashMap�� <"A1", scanner(A1)> ���� �������μ� �̸� ������ �� �ִ�.
	 * <br><br>
	 * ������ ���·� ����ϴ� �� ���� ����Ѵ�.<br>
	 * ���� ��� key������ String��� Integer�� ����� �� �ִ�.
	 * ���� ������� ���� ����ϴ� stream ���� �������� ����, �����Ѵ�.
	 * <br><br>
	 * �̰͵� �����ϸ� �˾Ƽ� �����ؼ� ����ص� �������ϴ�.
	 */
	
	//��ġ ������� ���� �迭�� ����
	ArrayList<String> devName;
	ArrayList<FileChannel> devChannel;
	
	
	String obmemory = ""; //������Ʈ ���α׷��� ���� �޸� 
	String memory ="";//������ �۵��ϴ� �޸�
	
	
	int[] register = new int[10];
	double register_F;
	
	SymbolTable symtabList;
	// �̿ܿ��� �ʿ��� ���� �����ؼ� ����� ��.
	
	//���� �̸�
	String fileName;
	
	String pName ="";
	String pAddr = "";
	String pLength ="";
	
	//��� ���ڵ� ����
	ArrayList<String> programName;
	ArrayList<String> startAddr;//���� ���ڵ忡���� ���
	ArrayList<String> length;
	
	//�޸� ��ȭ��
	int target;
	int current;
	
	//������ ��� ����
	int check = 0;
	
	ArrayList<String> end;
	String endAddr = "";
	
	//�ɰ� ������� ����
	ArrayList<String> instCode;
	ArrayList<String> relocateCode;
	
	//���ǿ� ���� ����
	ArrayList<String> sectionLength;
	int sectionCount = 0;
	/**
	 * �޸�, �������͵� ���� ���ҽ����� �ʱ�ȭ�Ѵ�.
	 */
	public void initializeResource(){
		int i;
		//�޸� �ʱ�ȭ
		this.obmemory = "";
		this.memory  ="";
		
		//�������� �� �ʱ�ȭ
		for(i = 0; i < 10; i++)
		{
			this.register[i] = 0;
		}
		this.register_F = 0;
		
		//�����̸� �ʱ�ȭ
		this.fileName = "";
		
		//��� ���ڵ� ����
		programName= new ArrayList<String>();
		startAddr = new ArrayList<String>();
		length = new ArrayList<String>();
		
		//�޸� ��ȭ�� �ʱ�ȭ
		this.target = 0;
		this.current = 0;
		
		//�ɺ� ���̺��� ����
		symtabList = new SymbolTable();
		
		//���� �ڵ� ����Ʈ�� ����
		instCode = new ArrayList<String>();
		
		//���� �������� ����Ʈ ����
		sectionLength = new ArrayList<String>();
		
		//���ġ�� ���� �ڵ带 ����
		relocateCode = new ArrayList<String>();
		
		end = new ArrayList<String>();
		
		//��ġ ������ ���� ����Ʈ �ʱ�ȭ
		devName = new ArrayList<String>();
		devChannel = new ArrayList<FileChannel>();
		
	}
	
	/**
	 * deviceManager�� �����ϰ� �ִ� ���� ����� stream���� ���� �����Ű�� ����.
	 * ���α׷��� �����ϰų� ������ ���� �� ȣ���Ѵ�.
	 * @throws IOException 
	 */
	public void closeDevice() throws IOException {
		//��ǲ ��Ʈ��, �ƿ�ǲ ��Ʈ���� ����
		for(int i = 0 ; i < devChannel.size(); i++) {
			devChannel.get(i).close();
		}
	}
	
	/**
	 * ����̽��� ����� �� �ִ� ��Ȳ���� üũ. TD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * ����� stream�� ���� deviceManager�� ���� ������Ų��.
	 * @param devName Ȯ���ϰ��� �ϴ� ����̽��� ��ȣ,�Ǵ� �̸�
	 * @throws IOException 
	 */
	//���� �ʿ�
	public int testDevice(String devName) throws IOException {
		int res = 1;
		//��ü�� ������ ������ְ� 1�� ��ȯ
		for(int i = 0 ; i < this.devName.size();i++) {
			if(this.devName.get(i).equals(devName)) {
				res = 0;
				break;
			}
		}
		
		if(res == 1) {//��ġ�� �������� ������ ������ �� ��
			this.devName.add(devName);
			String fName = devName + ".txt";
			//ä���� �־���
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
	 * ����̽��κ��� ���ϴ� ������ŭ�� ���ڸ� �о���δ�. RD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param num �������� ������ ����
	 * @return ������ ������
	 * @throws IOException 
	 */
	public String readDevice(String devName, int num) throws IOException{
		String res = "";
		int byteCount = 0;
		//ä���� ã�� ����
		FileChannel fileChannel;
		int lo = 0;
		for(int i =0;i < this.devName.size();i++) {
			if(this.devName.get(i).equals(devName)) {
				lo = i;
				break;
			}
		}
		
		fileChannel = devChannel.get(lo+lo);
		ByteBuffer buffer = ByteBuffer.allocate(num);//�ѹ��ھ� �޾Ƶ��̱� ���� ������ ����
		Charset charset = Charset.defaultCharset();
		byteCount = fileChannel.read(buffer);
		buffer.flip();
		res = charset.decode(buffer).toString();
		
		return res;
	}

	/**
	 * ����̽��� ���ϴ� ���� ��ŭ�� ���ڸ� ����Ѵ�. WD��ɾ ������� �� ȣ��Ǵ� �Լ�.
	 * @param devName ����̽��� �̸�
	 * @param data ������ ������
	 * @param num ������ ������ ����
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
		
		ByteBuffer buffer = ByteBuffer.allocate(1);//�ѹ��ھ� �޾Ƶ��̱� ���� ������ ����
		Charset charset = Charset.defaultCharset();
		fileChannel =  devChannel.get(lo+lo+1);
		res += data.substring(0,num);
		buffer = ByteBuffer.allocate(res.length());
		buffer = charset.encode(res);
		fileChannel.write(buffer);
		
		
	}
	
	/**
	 * �޸��� Ư�� ��ġ���� ���ϴ� ������ŭ�� ���ڸ� �����´�.
	 * @param location �޸� ���� ��ġ �ε���
	 * @param num ������ ����
	 * @return �������� ������
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
	 * �޸��� Ư�� ��ġ�� ���ϴ� ������ŭ�� �����͸� �����Ѵ�. 
	 * @param locate ���� ��ġ �ε���
	 * @param data �����Ϸ��� ������
	 * @param num �����ϴ� �������� ����
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
	 * ��ȣ�� �ش��ϴ� �������Ͱ� ���� ��� �ִ� ���� �����Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum �������� �з���ȣ
	 * @return �������Ͱ� ������ ��
	 */
	public int getRegister(int regNum){
		return this.register[regNum];
		
	}

	/**
	 * ��ȣ�� �ش��ϴ� �������Ϳ� ���ο� ���� �Է��Ѵ�. �������Ͱ� ��� �ִ� ���� ���ڿ��� �ƴԿ� �����Ѵ�.
	 * @param regNum ���������� �з���ȣ
	 * @param value �������Ϳ� ����ִ� ��
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
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. int���� char[]���·� �����Ѵ�.
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
	 * �ַ� �������Ϳ� �޸𸮰��� ������ ��ȯ���� ���ȴ�. String���� int���·� �����Ѵ�.
	 * @param data
	 * @return
	 */
	public int byteToInt(String data){
		int res = Integer.parseInt(data);
		return res;
	}
	
	//16������ 10������
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
			if(hex.length() >= 3) {//���̰� 3�̻��϶���
				if(hex.charAt(0) == 'F') {
					num = (int) (num -(15*Math.pow(16, 2) + 15 * Math.pow(16, 1) + 16));
				}
			}
			return num;
		}
}