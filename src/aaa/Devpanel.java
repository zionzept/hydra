package aaa;

import java.awt.Color;
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
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
	
	private JTextArea monitor;
	
	public Devpanel() {
		super("Hydra devpanel");
		panels = new HashMap<String, JPanel>();
		updates = new HashMap<String, Runnable>();
		
		JSplitPane outer = new JSplitPane();
		
		JSplitPane split = new JSplitPane();
		JPanel pp;
		JPanel p;
		Runnable run;
		
		
		
		Dimension text_min_size = new Dimension(80, 10);
		
		{
		pp = new JPanel();
		p = new JPanel();
		GroupLayout layout = new GroupLayout(p);
		p.setLayout(layout);
		JLabel player_pos = new JLabel("position");
		JTextField player_pos_x = new JTextField();
		JTextField player_pos_y = new JTextField();
		JTextField player_pos_z = new JTextField();
		player_pos_x.setMinimumSize(text_min_size);
		player_pos_y.setMinimumSize(text_min_size);
		player_pos_z.setMinimumSize(text_min_size);
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
		view_distance_field.setMinimumSize(text_min_size);
		view_distance_field.setText(Float.toString(Hydra.view_distance));
		view_distance_field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Hydra.view_distance = Float.parseFloat(view_distance_field.getText());
			}
		});
		JLabel gravity = new JLabel("gravity");
		JTextField gravity_field = new JTextField();
		gravity_field.setMinimumSize(text_min_size);
		gravity_field.setText(Double.toString(Hydra.g));
		gravity_field.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Hydra.g = Double.parseDouble(gravity_field.getText());
			}
		});
		
		layout.setHorizontalGroup(layout.createSequentialGroup().
				addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
						addComponent(view_distance).
						addComponent(gravity)
						).
				addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
						addComponent(view_distance_field).
						addComponent(gravity_field)
						)
				);
		layout.setVerticalGroup(layout.createSequentialGroup().
				addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
						addComponent(view_distance).
						addComponent(view_distance_field)
						).
				addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
						addComponent(gravity).
						addComponent(gravity_field)
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
		
		{
			pp = new JPanel();
			p = new JPanel();
			GroupLayout layout = new GroupLayout(p);
			p.setLayout(layout);
			JLabel turn = new JLabel("turn");
			JTextField turn_field = new JTextField();
			turn_field.setMinimumSize(text_min_size);
			turn_field.setText(Double.toString(Hydra.turn_bias));
			
			JSlider turn_slider = new JSlider(JSlider.HORIZONTAL, -314, 314, 0);
			turn_field.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					double value = Double.parseDouble(turn_field.getText());
					Hydra.turn_bias = value;
					turn_slider.setValue((int)(value * 100));
				}
			});
			turn_slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					double value = turn_slider.getValue() / 100.0;
					turn_field.setText(Double.toString(value));
					Hydra.turn_bias = value;
				}
			});
			JLabel rise = new JLabel("rise");
			JTextField rise_field = new JTextField();
			rise_field.setMinimumSize(text_min_size);
			rise_field.setText(Double.toString(Hydra.rise_bias));
			JSlider rise_slider = new JSlider(JSlider.HORIZONTAL, -314, 314, 0);
			rise_field.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					double value = Double.parseDouble(rise_field.getText());
					Hydra.rise_bias = value;
					rise_slider.setValue((int)(value * 100));
				}
			});
			rise_slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					double value = rise_slider.getValue() / 100.0;
					rise_field.setText(Double.toString(value));
					Hydra.rise_bias = value;
				}
			});
			JLabel swirl = new JLabel("swirl");
			JTextField swirl_field = new JTextField();
			swirl_field.setMinimumSize(text_min_size);
			swirl_field.setText(Double.toString(Hydra.swirl_bias));
			JSlider swirl_slider = new JSlider(JSlider.HORIZONTAL, -314, 314, 0);
			swirl_field.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					double value = Double.parseDouble(swirl_field.getText());
					Hydra.swirl_bias = value;
					swirl_slider.setValue((int)(value * 100));
				}
			});
			swirl_slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					double value = swirl_slider.getValue() / 100.0;
					swirl_field.setText(Double.toString(value));
					Hydra.swirl_bias = value;
				}
			});
			
			layout.setHorizontalGroup(layout.createSequentialGroup().
					addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
							addComponent(turn).
							addComponent(rise).
							addComponent(swirl)
							).
					addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
							addComponent(turn_field).
							addComponent(turn_slider).
							addComponent(rise_field).
							addComponent(rise_slider).
							addComponent(swirl_field).
							addComponent(swirl_slider)
							)
					);
			layout.setVerticalGroup(layout.createSequentialGroup().
					addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
							addComponent(turn).
							addComponent(turn_field)
							).
					addGap(8).
					addComponent(turn_slider).
					addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
							addComponent(rise).
							addComponent(rise_field)
							).
					addGap(8).
					addComponent(rise_slider).
					addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).
							addComponent(swirl).
							addComponent(swirl_field)
							).
					addGap(8).
					addComponent(swirl_slider)
					);
			p.add(turn);
			p.add(turn_field);
			pp.add(p);
			panels.put("rails", pp);
			run = new Runnable() {
					@Override
					public void run() {
						
					}
			};
			updates.put("rails", run);
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
		
		monitor = new JTextArea();
		monitor.setBackground(new Color(21,21,21));
		monitor.setForeground(Color.YELLOW);
		
		outer.setLeftComponent(split);
		outer.setRightComponent(monitor);
		add(outer);
		setSize(800, 600);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	
	
	
	public void gib(JTextField field, Object x) {
		if (!field.hasFocus()) {
			field.setText(x.toString());
		}
	}
	
	public void update() {
		updates.get(current).run();
	}
	
	public void update_monitor(String text) {
		monitor.setText(text);
	}
	
}
