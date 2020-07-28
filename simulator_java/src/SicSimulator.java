package SP18_simulator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * �ùķ����ͷμ��� �۾��� ����Ѵ�. VisualSimulator���� ������� ��û�� ������ �̿� ����
 * ResourceManager�� �����Ͽ� �۾��� �����Ѵ�.  
 * 
 * �ۼ����� ���ǻ��� : <br>
 *  1) ���ο� Ŭ����, ���ο� ����, ���ο� �Լ� ������ �󸶵��� ����. ��, ������ ������ �Լ����� �����ϰų� ������ ��ü�ϴ� ���� ������ ��.<br>
 *  2) �ʿ信 ���� ����ó��, �������̽� �Ǵ� ��� ��� ���� ����.<br>
 *  3) ��� void Ÿ���� ���ϰ��� ������ �ʿ信 ���� �ٸ� ���� Ÿ������ ���� ����.<br>
 *  4) ����, �Ǵ� �ܼ�â�� �ѱ��� ��½�Ű�� �� ��. (ä������ ����. �ּ��� ���Ե� �ѱ��� ��� ����)<br>
 * 
 * <br><br>
 *  + �����ϴ� ���α׷� ������ ��������� �����ϰ� ���� �е��� ������ ��� �޺κп� ÷�� �ٶ��ϴ�. ���뿡 ���� �������� ���� �� �ֽ��ϴ�.
 */
public class SicSimulator {
	ResourceManager rMgr;
	SicLoader sLoder;
	
	InstTable instTable;
	//��ɾ��� ������ ����
	int instCount = 0;
	int pcCounter = 0;
	int base = 0;
	int memIdx = 0;
	int obProCount = 0;//������Ʈ ���α׷��� ��ִ��� Ȯ��
	int c = 0;
	
	String target = "";
	String code = "";//����ǰ� �ִ� ���� �ڵ带 ����
	
	String usingDevice ="";
	
	ArrayList<String> log;//��ɾ� ������ ����
	

	public SicSimulator(ResourceManager resourceManager,SicLoader sicloader) {
		// �ʿ��ϴٸ� �ʱ�ȭ ���� �߰�
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
	 * ��������, �޸� �ʱ�ȭ �� ���α׷� load�� ���õ� �۾� ����.
	 * ��, object code�� �޸� ���� �� �ؼ��� SicLoader���� �����ϵ��� �Ѵ�. 
	 * @throws IOException 
	 */
	public void load(File program) throws IOException {
		/* �޸� �ʱ�ȭ, �������� �ʱ�ȭ ��*/
		//�������� �ʱ�ȭ
		sLoder.load(program);
		sLoder.action();
		
	}
	

	/**
	 * 1���� instruction�� ����� ����� ���δ�. 
	 * ResourceManager�� �ִ� instCode����Ʈ�� �ִ� ��ɾ���� ���������� ����
	 * @throws IOException 
	 */
	public void oneStep() throws IOException {
		//���� �ΰ��� ��Ʈ�� �о���� �Ǵ�
		String instName = "";
		String data ="";
		int temp;
		String searchOp ="";
		
		int n = 0,i = 0 ,x = 0 ,b = 0 ,p = 0 ,e = 0;
		code = rMgr.relocateCode.get(instCount);
		usingDevice ="";
		
		//��� ���ڵ� ����
		rMgr.pName = rMgr.programName.get(obProCount);
		rMgr.pAddr = rMgr.startAddr.get(obProCount);
		rMgr.pLength = rMgr.length.get(obProCount);
		rMgr.endAddr = rMgr.end.get(obProCount);
		
		if(code.equals("END")) {//�޸𸮿� �ִ� ���� ������ �ǳʶ�
			if(!(instCount == rMgr.instCode.size()-1))
			{
				while(!rMgr.instCode.get(instCount+1).equals(rMgr.memory.substring(memIdx,memIdx+rMgr.instCode.get(instCount+1).length()))) {
					memIdx++;
				}
			}
			obProCount++;
		}
		
		//��ɾ �ƴ����� �ǳʶ�
		if(code.length() == 2) {
			instCount++;
			return;
		}
		
		//���� ��ɾ �ִ� �ּ�
		rMgr.current = memIdx;
		
		//�޸� ���� �ܰ�� pc�� ����
		if(code.length() == 4) {
			rMgr.register[8] = memIdx/2 + 2;
			//�޸��� ��ġ ����
			memIdx= memIdx + 4;
		}
		else if(code.length() == 6) {
			rMgr.register[8] = memIdx/2 + 3;
			//�޸��� ��ġ ����
			memIdx= memIdx + 6;
		}
		else {
			rMgr.register[8] = memIdx/2 + 4;
			//�޸��� ��ġ ����
			memIdx= memIdx + 8;
		}
		
		temp = hexToDecimal(code.substring(1,2));
		//nixbpe�Ǻ�����
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
		
		//instNameã��
		searchOp += code.charAt(0);
		searchOp += decimalToHex(temp);
		instName = instTable.isInclude(searchOp);
		if(instName == "") {//��ɾ �ƴ����� �ǳʶ�
			return;
		}
		
		//��ɾ��� ����
		if(instName.equals("STL")) {//L�������Ϳ� �ִ� ���� �޸𸮿� ����
			//target�� ����
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x��Ʈ �۾�
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target���ٰ� L���������� ������ ����
			data = decimalToHex(rMgr.register[2]);
			rMgr.setMemory(rMgr.target*2, data, 6);
			addLog("STL");
		}
		else if(instName.equals("JSUB")) {
			//target�� �ִ� �ּҷ� ��
			rMgr.setRegister(2, rMgr.register[8]);
			rMgr.target = hexToDecimal(code.substring(3));
			memIdx = rMgr.target*2;
			obProCount = 0;
			
			//ã���� �ϴ� ��ɾ ��� ���ǿ� �ִ��� �˻�
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
			
			for(int j = 0 ; j < rMgr.relocateCode.size(); j++) {//��ɾ ã����
				int rl =rMgr.relocateCode.get(j).length();//ã���� �ϴ� ��ɾ��� ���̸� ���
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
			//�������� ���� 0����  ����
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
			if(num == rMgr.getRegister(0)) {//flag�� ����
				rMgr.setRegister(9, 0);
			}
			else if(num > rMgr.getRegister(0)){//�ǿ����ڰ� �� Ŭ ���
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
			if(rMgr.getRegister(a) == rMgr.getRegister(c)) {//flag�� ����
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
			//target����
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
			
			memIdx = rMgr.target*2;//�޸� ���� �ű�
			
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
			for(int j = 0 ; j < rMgr.relocateCode.size(); j++) {//��ɾ ã����
				int rl =rMgr.relocateCode.get(j).length();//ã���� �ϴ� ��ɾ��� ���̸� ���
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
			//target����
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			
			if(rMgr.getRegister(9) == 0) {//flag�� 0�϶� ����
				memIdx = rMgr.target*2;//�޸� ���� �ű�
				obProCount = 0;
				
				//ã���� �ϴ� ��ɾ ��� ���ǿ� �ִ��� �˻�
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
				
				for(int j = 0 ; j < rMgr.relocateCode.size(); j++) {//��ɾ ã����
					int rl =rMgr.relocateCode.get(j).length();//ã���� �ϴ� ��ɾ��� ���̸� ���
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
			//target����
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			
			if(rMgr.getRegister(9) == -1) {//flag�� -1�϶� ����
				memIdx = rMgr.target*2;//�޸� ���� �ű�
				data = rMgr.memory.substring(memIdx,memIdx+6);
				obProCount = 0;
						
				//ã���� �ϴ� ��ɾ ��� ���ǿ� �ִ��� �˻�
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
				
				for(int j = 0 ; j < rMgr.relocateCode.size(); j++) {//��ɾ ã����
					int rl =rMgr.relocateCode.get(j).length();//ã���� �ϴ� ��ɾ��� ���̸� ���
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
			//target�� ����
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x��Ʈ �۾�
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target�� �ִ� ������ �����ͼ� A�������Ϳ� ����
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
			//target�� ����
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x��Ʈ �۾�
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target�� �ִ� ������ �����ͼ� A�������Ϳ� ����
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
			//target�� ����
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x��Ʈ �۾�
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target�� �ִ� ������ �����ͼ� L�������Ϳ� ����
			//target�� �ִ� ������ �����ͼ� A�������Ϳ� ����
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
			//target�� ����
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x��Ʈ �۾�
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target�� �ִ� ������ �����ͼ� A�������Ϳ� ����
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
			//target�� ����
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x��Ʈ �۾�
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target�� �ִ� ������ �����ͼ� X�������Ϳ� ����
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
			//target�� ����
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			data = rMgr.getMemory(rMgr.target*2, 2);//��ġ �̸��� ������
			fData = rMgr.readDevice(data, 1);//�Ѱ��� �����͸� �о��
			rMgr.setRegister(0, hexToDecimal(fData));//�о�� ������ A�������Ϳ� �ε�
			usingDevice = data;
			addLog("RD");
		}
		else if(instName.equals("RSUB")) {
			//L�������Ϳ� �ִ� �ּҷ� ��
			memIdx = rMgr.getRegister(2)*2;
			rMgr.target = memIdx/2;
			obProCount = 0;
			
			//ã���� �ϴ� ��ɾ ��� ���ǿ� �ִ��� �˻�
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
			
			for(int j = 0 ; j < rMgr.relocateCode.size(); j++) {//��ɾ ã����
				int rl =rMgr.relocateCode.get(j).length();//ã���� �ϴ� ��ɾ��� ���̸� ���
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
			//target�� ����
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x��Ʈ �۾�
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target���ٰ� A���������� ������ ����
			data = decimalToHex(rMgr.register[0]);
			rMgr.setMemory(rMgr.target*2, data, 6);
			addLog("STA");
		}
		else if(instName.equals("STCH")) {
			//target�� ����
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//x��Ʈ �۾�
			if(x ==1) {
				rMgr.target += rMgr.getRegister(1);
			}
			//target���ٰ� A���������� ������ ����
			data = decimalToHex(rMgr.register[0]);
			rMgr.setMemory(rMgr.target*2, data, 2);
			addLog("STCH");
		}
		else if(instName.equals("STX")) {
			//target�� ����
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			//target���ٰ� X���������� ������ ����
			data = decimalToHex(rMgr.register[1]);
			rMgr.setMemory(rMgr.target*2, data, 6);
			addLog("STX");
		}
		else if(instName.equals("TD")) {
			//target�� ����
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
			if(rMgr.testDevice(devName) == 1) {//�غ� ���� ����
				rMgr.setRegister(9, 0);
			}
			else {//�غ� ��
				rMgr.setRegister(9, 1);
			}
			
			addLog("TD");
		}
		else if(instName.equals("TIX")) {
			//target����
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
			xNum = rMgr.getRegister(1)+1;//x�������� ���� ������
			if(num == xNum) {
				rMgr.setRegister(9, 0);
			}
			else if(num < xNum) {
				rMgr.setRegister(9, 1);
			}
			else {
				rMgr.setRegister(9,-1);
			}
			rMgr.setRegister(1, xNum);//x�������� �� �ϳ��� �÷���
			addLog("TIX");
		}
		else if(instName.equals("TIXR")) {
			int a;
			int c;
			a = 1;
			c = Integer.parseInt(code.substring(2,3));
			int xNum = rMgr.getRegister(a)+1;
			if(xNum == rMgr.getRegister(c)) {//flag�� ����
				rMgr.setRegister(9, 0);
			}
			else if(xNum > rMgr.getRegister(c)){
				rMgr.setRegister(9, 1);
			}
			else {
				rMgr.setRegister(9, -1);
			}
			rMgr.setRegister(1, xNum);//x�������� �� �ϳ��� �÷���
			addLog("TIXR");
		}
		else if(instName.equals("WD")) {
			int fData = 0;
			//target�� ����
			if(e == 1) {
				rMgr.target = hexToDecimal(code.substring(3));
			}
			else if(p == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[8];
			}
			else if(b == 1) {
				rMgr.target = hexToDecimal(code.substring(3)) + rMgr.register[3];
			}
			data = rMgr.getMemory(rMgr.target*2, 2);//��ġ �̸��� ������
			fData = rMgr.getRegister(0);//A�������Ϳ� �ִ� ���� �о��
			rMgr.writeDevice(data, decimalToHex(fData),decimalToHex(fData).length());
			usingDevice = data;
			addLog("WD");
		}
		instCount++;
					
	}
	
	/**
	 * ���� ��� instruction�� ����� ����� ���δ�.
	 * @throws IOException 
	 */
	public void allStep() throws IOException {
		oneStep();
		while(instCount != 0) {
			oneStep();	
		}
	}
	
	/**
	 * �� �ܰ踦 ������ �� ���� ���õ� ����� ���⵵�� �Ѵ�.
	 */
	public void addLog(String log) {
		this.log.add(log);
	}
	
	/**
	 * �ش� ��ġ�� ��Ʈ�� �ִ��� ������ �Ǻ�
	 * @return 1,0
	 */
	public int checkBit(char num, int location) {
		int res = 0;
		int check;
		//num�� ���ڷ� �ٲ��ִ� �۾�
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
		
		//�̷��� ��Ʈ�� ����
		if((check % 2) == 1) {
			res = 1;
		}
		
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
	
	/**
	 * 10�� ������ 16���� ���ڿ��� ��ȯ
	 * @return 16���� ���ڿ�
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
	
	//16���� ���ڿ����� ����
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
