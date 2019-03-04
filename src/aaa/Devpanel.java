package aaa;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.GroupLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public class Devpanel extends JFrame {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		
		JFrame frame = new Devpanel();
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				frame.dispose();
			}
		}).start();
	}
	
	private HashMap<String, JPanel> panels;
	private HashMap<String, Runnable> updates;
	private String current = "player";
	
	public Devpanel() {
		super("Hydra devpanel");
		panels = new HashMap<String, JPanel>();
		updates = new HashMap<String, Runnable>();
		
		JSplitPane split = new JSplitPane();
		JPanel pp;
		JPanel p;
		Runnable run;
		
		
		
		
		{
		pp = new JPanel();
		p = new JPanel();
		GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		JLabel player_pos = new JLabel("position");
		JTextField player_pos_x = new JTextField();
		JTextField player_pos_y = new JTextField();
		JTextField player_pos_z = new JTextField();
		player_pos_x.setMinimumSize(new Dimension(60, 10));
		player_pos_y.setMinimumSize(new Dimension(60, 10));
		player_pos_z.setMinimumSize(new Dimension(60, 10));
		player_pos_x.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Hydra.view.pos().set(Float.parseFloat(player_pos_x.getText()), Hydra.view.pos().y, Hydra.view.pos().z);
			}
		});
		player_pos_y.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Hydra.view.pos().set(Hydra.view.pos().x, Float.parseFloat(player_pos_y.getText()), Hydra.view.pos().z);
			}
		});
		player_pos_z.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Hydra.view.pos().set(Hydra.view.pos().x, Hydra.view.pos().y, Float.parseFloat(player_pos_z.getText()));
			}
		});
		layout.setHorizontalGroup(layout.createSequentialGroup().
				addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
						addComponent(player_pos)
						).
				addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
						addComponent(player_pos_x)
						).
				addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
						addComponent(player_pos_y)
						).
				addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
						addComponent(player_pos_z)
						)
				);
		layout.setVerticalGroup(layout.createSequentialGroup().
				addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
						addComponent(player_pos).
						addComponent(player_pos_x).
						addComponent(player_pos_y).
						addComponent(player_pos_z)
						)
				);
		p.add(player_pos);
		p.add(player_pos_x);
		p.add(player_pos_y);
		p.add(player_pos_z);
		pp.add(p);
		panels.put("player", pp);
		run = new Runnable() {
				@Override
				public void run() {
					gib(player_pos_x, Hydra.view.pos().x);
					gib(player_pos_y, Hydra.view.pos().y);
					gib(player_pos_z, Hydra.view.pos().z);
				}
		};
		updates.put("player", run);
		}
		
		
		
		
		
		{
		pp = new JPanel();
		p = new JPanel();
		GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		JLabel view_distance = new JLabel("view distance");
		JTextField view_distance_field = new JTextField();
		view_distance_field.setMinimumSize(new Dimension(60, 10));
		view_distance_field.setText(Float.toString(Hydra.view_distance));
		view_distance_field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Hydra.view_distance = Float.parseFloat(view_distance_field.getText());
			}
		});
		layout.setHorizontalGroup(layout.createSequentialGroup().
				addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
						addComponent(view_distance)
						).
				addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
						addComponent(view_distance_field)
						)
				);
		layout.setVerticalGroup(layout.createSequentialGroup().
				addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
						addComponent(view_distance).
						addComponent(view_distance_field)
						)
				);
		p.add(view_distance);
		p.add(view_distance_field);
		pp.add(p);
		panels.put("graphics", pp);
		run = new Runnable() {
				@Override
				public void run() {
					
				}
		};
		updates.put("graphics", run);
		}
		
		JList<Object> list = new JList<Object>(panels.keySet().toArray());
		JScrollPane left = new JScrollPane(list);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					current = list.getSelectedValue().toString();
					System.out.println("current: " + current);
					JPanel panel = panels.get(current);
					split.setRightComponent(panel);					
				}
			}
		});
		list.setSelectedIndex(0);
		current = list.getSelectedValue().toString();
		
		split.setLeftComponent(left);
		split.setRightComponent(panels.get(current));
		
		add(split);
		setSize(600, 800);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	public void gib(JTextField field, Object x) {
		if (!field.hasFocus()) {
			field.setText(x.toString());
		}
	}
	
	public void update() {
		System.out.println(current);
		updates.get(current).run();
	}
	
}
