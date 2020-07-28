package SP18_simulator;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<String> addressList;
	
	ArrayList<String> refList;
	//�ٲ������ ����Ʈ
	ArrayList<String> modiLoc;
	ArrayList<String> modiNum;
	ArrayList<String> modiName;
	
	public SymbolTable() {
		symbolList = new ArrayList<String>();
		addressList = new ArrayList<String>();
		refList = new ArrayList<String>();
		
		modiLoc = new ArrayList<String>();
		modiNum = new ArrayList<String>();
		modiName = new ArrayList<String>();
		
	}

	

	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * @param address : �ش� symbol�� ������ �ּҰ�
	 * <br><br>
	 * ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putSymbol(String symbol, String address) {
		symbolList.add(symbol);
		addressList.add(address);
	}
	
	/**
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param symbol : ������ ���ϴ� symbol�� label
	 * @param newaddress : ���� �ٲٰ��� �ϴ� �ּҰ�
	 */
	public void modifySymbol(String symbol, String newaddress) {
		int idx = 0;
		for(int i = 0 ; i < symbolList.size(); i++){
			if(this.symbolList.get(i) == symbol) {
				idx = i;
				break;
			}
		}
		addressList.add(idx,newaddress);
	}
	
	/**
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param symbol : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
	 */
	public String search(String symbol) {
		String address = "";
		int idx = -1;
		for(int i = 0 ; i < symbolList.size(); i++){
			if(symbol.equals(symbolList.get(i))) {
				idx = i;
				break;
			}
		}
		if(idx != -1) {
			address = addressList.get(idx);
		}
		return address;
	}
	
	public int com(String a, String b) {
		int res =0;
		if(a.length() != b.length()) {
			res =0;
		}
		else {
			res =1;
			for(int i = 0 ; i < a.length(); i++) {
				if(a.charAt(i) != b.charAt(i)) {
					res = 0;
					break;
				}
			
			}
		}
		return res;
	}
		
	/**
	 * ���� �ɺ��� ����
	 * @param symbol :���� �ɺ�
	 */
	public void putRef(String symbol) {
		refList.add(symbol);
	}
	
	
	
}
