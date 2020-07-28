import java.util.ArrayList;
import java.util.HashMap;

/**
 * symbol과 관련된 데이터와 연산을 소유한다.
 * section 별로 하나씩 인스턴스를 할당한다.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	// 기타 literal, external 선언 및 처리방법을 구현한다.
	//추가부분(literal,external 처리)
	ArrayList<String> literalList;
	ArrayList<String> defList;
	ArrayList<String> refList;
	
	
	/**
	 * 생성자 호출시 리스트들을 생성
	 */
	
	public SymbolTable()
	{
		this.symbolList = new ArrayList<String>();//심볼 리스트 객체를 만들어줌
		this.locationList = new ArrayList<Integer>();//토큰 리스트 객체를 만들어줌
		this.literalList = new ArrayList<String>();//리터럴 리스트 객체를 만들어줌
		this.defList = new ArrayList<String>();//정의 심볼 리스트 객체를 만들어줌
		this.refList = new ArrayList<String>();//참조 심볼 리스트 객체를 만들어줌
	}
	
	/**
	 * 새로운 Symbol을 table에 추가한다.
	 * @param symbol : 새로 추가되는 symbol의 label
	 * @param location : 해당 symbol이 가지는 주소값
	 * <br><br>
	 * 주의 : 만약 중복된 symbol이 putSymbol을 통해서 입력된다면 이는 프로그램 코드에 문제가 있음을 나타낸다. 
	 * 매칭되는 주소값의 변경은 modifySymbol()을 통해서 이루어져야 한다.
	 */
	public void putSymbol(String symbol, int location) {
		symbolList.add(symbol);
		locationList.add(location);	
	}
	
	/**
	 * 리터럴에 대한 정보를 저장해준다
	 * @param literal : 피연산자로 나온 리터럴을 저장
	 */
	public void putLiteral(String literal) {
		int i;
		int check = 0;
		for(i = 0; i <literalList.size(); i++)
		{
			if(literalList.get(i).equals(literal))//만약에 같은 리터럴이 이미 존재하면 넣지 않음
			{
				check = 1;
				break;
			}
		}
		if(check == 0)
		{
			literalList.add(literal);
		}
	}
	
	/**
	 * 정의 심볼에 대한 정보를 저장해준다
	 * @param defSymbol : 피연산자로 나온 심볼을 저장
	 */
	public void putDef(String defSymbol) {
		defList.add(defSymbol);
	}
	
	/**
	 * 참조 심볼에 대한 정보를 저장해준다
	 * @param refSymbol : 피연산자로 나온 리터럴을 저장
	 */
	public void putRef(String refSymbol) {
		refList.add(refSymbol);
	}
	
	/**
	 * 참조 심볼에 해당 심볼이 있는지 검사
	 * @param symbol: 찾고자 하는 주소값의
	 * @return 해당 심볼이 있으면 1, 없으면 0을 반환 
	 */
	public int searchRef(String symbol) {
		int check = 0;
		int size;
		size = refList.size();
		int i;
		for(i = 0 ; i < size; i++)
		{
			if(refList.get(i).equals(symbol))
			{
				check++;
				break;
			}
		}
		
		return check;
	}
	
	
	/**
	 * 기존에 존재하는 symbol 값에 대해서 가리키는 주소값을 변경한다.
	 * @param symbol : 변경을 원하는 symbol의 label
	 * @param newLocation : 새로 바꾸고자 하는 주소값
	 */
	public void modifySymbol(String symbol, int newLocation){
		int i,size;
		size = symbolList.size();
		for(i = 0 ; i < size; i++) 
		{
			if(symbol.equals(symbolList.get(i)))
			{
				locationList.set(i, newLocation);
				break;
			}
		}		
	}
	
	/**
	 * 인자로 전달된 symbol이 어떤 주소를 지칭하는지 알려준다. 
	 * @param symbol : 검색을 원하는 symbol의 label
	 * @return symbol이 가지고 있는 주소값. 해당 symbol이 없을 경우 -1 리턴
	 */
	public int search(String symbol) {
		int address = 0;
		int i,size;
		size = symbolList.size();
		address = -1;
		for(i = 0 ; i < size; i++)
		{
			if(symbol.equals(symbolList.get(i)))
			{
				address = locationList.get(i);
				break;
			}
		}	
		return address;
	}
}
