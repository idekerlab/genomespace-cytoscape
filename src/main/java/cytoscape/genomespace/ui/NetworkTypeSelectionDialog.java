package cytoscape.genomespace.ui;


import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;


public final class NetworkTypeSelectionDialog extends JDialog {
	private String networkType;
	private final JRadioButton sifRadioButton;
//	private final JRadioButton nnfRadioButton;
	private final JRadioButton xgmmlRadioButton;
	private final JRadioButton gmlRadioButton;
	private final JButton okButton;
	private final JButton cancelButton;

	public NetworkTypeSelectionDialog(final Frame owner) {
		super(owner, /* modal = */ true);

		networkType = null;

		final JPanel topPane = new JPanel();
		final JLabel label = new JLabel("Network type:");
		topPane.add(label);
		getContentPane().add(topPane, BorderLayout.NORTH);

		final JPanel radioButtonPane = new JPanel();
		ButtonGroup group = new ButtonGroup();
		sifRadioButton = new JRadioButton("SIF", true);
		group.add(sifRadioButton);
		radioButtonPane.add(sifRadioButton);
//		nnfRadioButton = new JRadioButton("NNF");
//		group.add(nnfRadioButton);
//		radioButtonPane.add(nnfRadioButton);
		xgmmlRadioButton = new JRadioButton("XGMML");
		group.add(xgmmlRadioButton);
		radioButtonPane.add(xgmmlRadioButton);
		gmlRadioButton = new JRadioButton("GML");
		group.add(gmlRadioButton);
		radioButtonPane.add(gmlRadioButton);
		getContentPane().add(radioButtonPane, BorderLayout.CENTER);

		final JPanel buttonPane = new JPanel();

		okButton = new JButton("Ok");
		okButton.setSelected(true);
		okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					if (NetworkTypeSelectionDialog.this.sifRadioButton.isSelected())
						NetworkTypeSelectionDialog.this.networkType = "SIF";
//					else if (NetworkTypeSelectionDialog.this.nnfRadioButton.isSelected())
//						NetworkTypeSelectionDialog.this.networkType = "NNF";
					else if (NetworkTypeSelectionDialog.this.xgmmlRadioButton.isSelected())
						NetworkTypeSelectionDialog.this.networkType = "XGMML";
					else if (NetworkTypeSelectionDialog.this.gmlRadioButton.isSelected())
						NetworkTypeSelectionDialog.this.networkType = "GML";
					NetworkTypeSelectionDialog.this.dispose();
				}
			});
		buttonPane.add(okButton);

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent actionEvent) {
					NetworkTypeSelectionDialog.this.networkType = null;
					NetworkTypeSelectionDialog.this.dispose();
				}
			});
		buttonPane.add(cancelButton);

		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		pack();
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(getParent());
		setVisible(true);
	}

	public String getNetworkType() {
		return networkType;
	}
}
