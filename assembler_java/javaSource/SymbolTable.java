import java.util.ArrayList;
import java.util.HashMap;

/**
 * symbol�� ���õ� �����Ϳ� ������ �����Ѵ�.
 * section ���� �ϳ��� �ν��Ͻ��� �Ҵ��Ѵ�.
 */
public class SymbolTable {
	ArrayList<String> symbolList;
	ArrayList<Integer> locationList;
	// ��Ÿ literal, external ���� �� ó������� �����Ѵ�.
	//�߰��κ�(literal,external ó��)
	ArrayList<String> literalList;
	ArrayList<String> defList;
	ArrayList<String> refList;
	
	
	/**
	 * ������ ȣ��� ����Ʈ���� ����
	 */
	
	public SymbolTable()
	{
		this.symbolList = new ArrayList<String>();//�ɺ� ����Ʈ ��ü�� �������
		this.locationList = new ArrayList<Integer>();//��ū ����Ʈ ��ü�� �������
		this.literalList = new ArrayList<String>();//���ͷ� ����Ʈ ��ü�� �������
		this.defList = new ArrayList<String>();//���� �ɺ� ����Ʈ ��ü�� �������
		this.refList = new ArrayList<String>();//���� �ɺ� ����Ʈ ��ü�� �������
	}
	
	/**
	 * ���ο� Symbol�� table�� �߰��Ѵ�.
	 * @param symbol : ���� �߰��Ǵ� symbol�� label
	 * @param location : �ش� symbol�� ������ �ּҰ�
	 * <br><br>
	 * ���� : ���� �ߺ��� symbol�� putSymbol�� ���ؼ� �Էµȴٸ� �̴� ���α׷� �ڵ忡 ������ ������ ��Ÿ����. 
	 * ��Ī�Ǵ� �ּҰ��� ������ modifySymbol()�� ���ؼ� �̷������ �Ѵ�.
	 */
	public void putSymbol(String symbol, int location) {
		symbolList.add(symbol);
		locationList.add(location);	
	}
	
	/**
	 * ���ͷ��� ���� ������ �������ش�
	 * @param literal : �ǿ����ڷ� ���� ���ͷ��� ����
	 */
	public void putLiteral(String literal) {
		int i;
		int check = 0;
		for(i = 0; i <literalList.size(); i++)
		{
			if(literalList.get(i).equals(literal))//���࿡ ���� ���ͷ��� �̹� �����ϸ� ���� ����
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
	 * ���� �ɺ��� ���� ������ �������ش�
	 * @param defSymbol : �ǿ����ڷ� ���� �ɺ��� ����
	 */
	public void putDef(String defSymbol) {
		defList.add(defSymbol);
	}
	
	/**
	 * ���� �ɺ��� ���� ������ �������ش�
	 * @param refSymbol : �ǿ����ڷ� ���� ���ͷ��� ����
	 */
	public void putRef(String refSymbol) {
		refList.add(refSymbol);
	}
	
	/**
	 * ���� �ɺ��� �ش� �ɺ��� �ִ��� �˻�
	 * @param symbol: ã���� �ϴ� �ּҰ���
	 * @return �ش� �ɺ��� ������ 1, ������ 0�� ��ȯ 
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
	 * ������ �����ϴ� symbol ���� ���ؼ� ����Ű�� �ּҰ��� �����Ѵ�.
	 * @param symbol : ������ ���ϴ� symbol�� label
	 * @param newLocation : ���� �ٲٰ��� �ϴ� �ּҰ�
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
	 * ���ڷ� ���޵� symbol�� � �ּҸ� ��Ī�ϴ��� �˷��ش�. 
	 * @param symbol : �˻��� ���ϴ� symbol�� label
	 * @return symbol�� ������ �ִ� �ּҰ�. �ش� symbol�� ���� ��� -1 ����
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
