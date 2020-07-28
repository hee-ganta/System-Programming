package SP18_simulator;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * symbol과 관련된 데이터와 연산을 소유한다.
 * section 별로 하나씩 인스턴스를 할당한다.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<String> addressList;
	
	ArrayList<String> refList;
	//바꿔줘야할 리스트
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
	 * 새로운 Symbol을 table에 추가한다.
	 * @param symbol : 새로 추가되는 symbol의 label
	 * @param address : 해당 symbol이 가지는 주소값
	 * <br><br>
	 * 주의 : 만약 중복된 symbol이 putSymbol을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다. 
	 * 매칭되는 주소값의 변경은 modifySymbol()을 통해서 이루어져야 한다.
	 */
	public void putSymbol(String symbol, String address) {
		symbolList.add(symbol);
		addressList.add(address);
	}
	
	/**
	 * 기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
	 * @param symbol : 변경을 원하는 symbol의 label
	 * @param newaddress : 새로 바꾸고자 하는 주소값
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
	 * 인자로 전달된 symbol이 어떤 주소를 지칭하는지 알려준다. 
	 * @param symbol : 검색을 원하는 symbol의 label
	 * @return symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
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
	 * 참조 심볼을 저장
	 * @param symbol :참조 심볼
	 */
	public void putRef(String symbol) {
		refList.add(symbol);
	}
	
	
	
}
