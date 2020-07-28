package SP18_simulator;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.*;
 

/**
 * VisualSimulator�� ����ڿ��� ��ȣ�ۿ��� ����Ѵ�.<br>
 * ��, ��ư Ŭ������ �̺�Ʈ�� �����ϰ� �׿� ���� ������� ȭ�鿡 ������Ʈ �ϴ� ������ �����Ѵ�.<br>
 * �������� �۾��� SicSimulator���� �����ϵ��� �����Ѵ�.
 */
public class VisualSimulator {
	ResourceManager resourceManager = new ResourceManager();
	SicLoader sicLoader = new SicLoader(resourceManager);
	SicSimulator sicSimulator = new SicSimulator(resourceManager,sicLoader);
	JText SimulatorView;
	
	/**
	 * ���α׷� �ε� ����� �����Ѵ�.
	 * @throws IOException 
	 */
	public void load(File program) throws IOException{
		//...
		sicLoader.load(program);
		sicSimulator.load(program);
	};

	/**
	 * �ϳ��� ��ɾ ������ ���� SicSimulator�� ��û�Ѵ�.
	 * @throws IOException 
	 */
	public void oneStep() throws IOException{
		sicSimulator.oneStep();
	};

	/**
	 * �����ִ� ��� ��ɾ ������ ���� SicSimulator�� ��û�Ѵ�.
	 */
	public void allStep() throws IOException{
		sicSimulator.allStep();
	};
	
	/**
	 * ȭ���� �ֽŰ����� �����ϴ� ������ �����Ѵ�.
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
