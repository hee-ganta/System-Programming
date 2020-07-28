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
 * ��� instruction�� ������ �����ϴ� Ŭ����. instruction data���� �����Ѵ�. <br>
 * ���� instruction ���� ����, ���� ��� ����� �����ϴ� �Լ�, ���� ������ �����ϴ� �Լ� ���� ���� �Ѵ�.
 */
public class InstTable {
	/** 
	 * inst.data ������ �ҷ��� �����ϴ� ����.
	 *  ��ɾ��� �̸��� ��������� �ش��ϴ� Instruction�� �������� ������ �� �ִ�.
	 */
	HashMap<String, Instruction> instMap;
	
	//��ɾ� �̸��� �ڵ带 ����ϱ� ���� ����Ʈ ���
	ArrayList<String> instName;
	ArrayList<String> opList;
	
	
	/**
	 * Ŭ���� �ʱ�ȭ. �Ľ��� ���ÿ� ó���Ѵ�.
	 * @param instFile : instuction�� ���� ���� ����� ���� �̸�
	 * @throws IOException 
	 */
	public InstTable(String instFile) throws IOException {
		instMap = new HashMap<String, Instruction>();
		instName = new ArrayList<String>();
		opList = new ArrayList<String>();
		openFile(instFile);
	}
	
	/**
	 * �Է¹��� �̸��� ������ ���� �ش� ������ �Ľ��Ͽ� instMap�� �����Ѵ�.
	 * @throws IOException 
	 */
	public void openFile(String fileName) throws IOException {
		Path path = Paths.get(fileName);//������ ��θ� ����
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
			if(byteCount == -1) //������ ���� �о����� �ݺ��� Ż��
			{
				break;
			}
			
			if(temp.equals("\r"))//���� ������ �ڰ����� ����
			{
				
				Instruction newInst;
				newInst = new Instruction(data);
				instMap.put(newInst.instruction, newInst);//��ɾ� ���̺� ������ �߰�
				instName.add(newInst.instruction);//��ɾ� �̸��� ����
				opList.add(newInst.opcode);//��ɾ� �ڵ带 ����
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
	
	/**
	 *ã���� �ϴ� ��ɾ��� ��ü�� ��ȯ 
	 */
	public Instruction search(String name)
	{
		return instMap.get(name);	
	}
	
	/**
	 * opcode�� ����Ͽ� �ش��ϴ� ��ɾ� �̸��� ��ȯ
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
	 * �ش� ��ɾ ����Ǿ��ִ��� ����
	 * @retrun ��ɾ� �̸�
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
 * ��ɾ� �ϳ��ϳ��� ��ü���� ������ InstructionŬ������ ����.
 * instruction�� ���õ� �������� �����ϰ� �������� ������ �����Ѵ�.
 */
class Instruction {
	
	//�������� ����
	String instruction;
	String opcode = "";
	int numberOfOperand;
	
	/** instruction�� �� ����Ʈ ��ɾ����� ����. ���� ���Ǽ��� ���� */
	int format;
	
	/**
	 * Ŭ������ �����ϸ鼭 �Ϲݹ��ڿ��� ��� ������ �°� �Ľ��Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public Instruction(String line) {
		parsing(line);
	}
	
	/**
	 * �Ϲ� ���ڿ��� �Ľ��Ͽ� instruction ������ �ľ��ϰ� �����Ѵ�.
	 * @param line : instruction �����Ϸκ��� ���پ� ������ ���ڿ�
	 */
	public void parsing(String line) {
		String[] info = line.split("\t");
		
		instruction = info[0];//��ɾ��� �̸��� ����
		/*��ɾ��� ������ ����*/
		if(info[1].equals("3/4"))//���ǻ� 3���� ����
		{
			format = 3;
		}
		else if(info[1].equals("2"))//2������ 2�� ����
		{
			format = 2;
		}
		
		opcode += info[2].substring(0,2);
		
		/*�ǿ����� ������ ����*/
		numberOfOperand = Integer.parseInt(info[3]);
		
	}
	
	
}
