package SP18_simulator;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.*;
 

/**
 * VisualSimulator는 사용자와의 상호작용을 담당한다.<br>
 * 즉, 버튼 클릭등의 이벤트를 전달하고 그에 따른 결과값을 화면에 업데이트 하는 역할을 수행한다.<br>
 * 실제적인 작업은 SicSimulator에서 수행하도록 구현한다.
 */
public class VisualSimulator {
	ResourceManager resourceManager = new ResourceManager();
	SicLoader sicLoader = new SicLoader(resourceManager);
	SicSimulator sicSimulator = new SicSimulator(resourceManager,sicLoader);
	JText SimulatorView;
	
	/**
	 * 프로그램 로드 명령을 전달한다.
	 * @throws IOException 
	 */
	public void load(File program) throws IOException{
		//...
		sicLoader.load(program);
		sicSimulator.load(program);
	};

	/**
	 * 하나의 명령어만 수행할 것을 SicSimulator에 요청한다.
	 * @throws IOException 
	 */
	public void oneStep() throws IOException{
		sicSimulator.oneStep();
	};

	/**
	 * 남아있는 모든 명령어를 수행할 것을 SicSimulator에 요청한다.
	 */
	public void allStep() throws IOException{
		sicSimulator.allStep();
	};
	
	/**
	 * 화면을 최신값으로 갱신하는 역할을 수행한다.
	 */
	public void update(){
		SimulatorView.frameAction();
	};
		
	
	
	public static void main(String[] args) throws IOException {
			VisualSimulator v = new VisualSimulator();
			v.SimulatorView = new JText();
			v.SimulatorView.getVisualSimulator(v);
			v.update();
			
	}
}
