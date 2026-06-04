package nezz.dreambot.unusualmoney.gui;

import javax.swing.*;
import java.awt.*;

public class UnusualMoneyGui extends JFrame {

	private final ScriptVars sv;

	public UnusualMoneyGui(ScriptVars sv) {
		this.sv = sv;
		setTitle("Unusual Money Makers");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new GridLayout(0, 1, 5, 5));
		setPreferredSize(new Dimension(350, 250));

		JLabel methodLabel = new JLabel("Select Method:");
		add(methodLabel);

		JComboBox<ScriptVars.MoneyMethod> methodBox = new JComboBox<>(ScriptVars.MoneyMethod.values());
		methodBox.setSelectedItem(sv.method);
		add(methodBox);

		JLabel jadesLabel = new JLabel("Jades per world (Jade method only):");
		add(jadesLabel);

		JSpinner jadesSpinner = new JSpinner(new SpinnerNumberModel(20, 5, 50, 5));
		add(jadesSpinner);

		JButton startButton = new JButton("Start");
		startButton.addActionListener(e -> {
			sv.method = (ScriptVars.MoneyMethod) methodBox.getSelectedItem();
			sv.jadesPerWorld = (int) jadesSpinner.getValue();
			sv.started = true;
			dispose();
		});
		add(startButton);

		pack();
		setLocationRelativeTo(null);
	}
}
