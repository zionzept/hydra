package aaa;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Scanner;

public class TESTLIST {
	public static void main(String[] args) {
		LinkedList<File> ws_list = new LinkedList<File>();
		ws_list.add(new File("C:\\Users\\Shiinon\\DocumentsS\\git"));
		ws_list.add(new File("C:\\Users\\Shiinon\\DocumentsS\\Code\\Java\\workspace"));
		for (File ws : ws_list) {
			loc_ws(ws);			
		}
	}
	
	private static void loc_ws(File workspace) {
		int total = 0;
		for (File project : workspace.listFiles()) {
			if (project.isDirectory()) {
				int lines = loc(project);
				System.out.println(lines + " in " + project.getName());
				total += lines;
			}
		}
		System.out.println(total);
	}
	
	private static int loc(File project) {
		int lines = 0;
		LinkedList<File> stack = new LinkedList<File>();
		stack.add(project);
		while (!stack.isEmpty()) {
			File current = stack.removeLast();
			if (current.isDirectory()) {
				stack.addAll(Arrays.asList(current.listFiles()));
			} else if (current.getName().matches("(.*)java")) {
				try (Scanner sc = new Scanner(current)) {
					int c = 0;
					while (sc.hasNextLine()) {
						sc.nextLine();
						c++;
					}
					lines += c;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return lines;
	}
}