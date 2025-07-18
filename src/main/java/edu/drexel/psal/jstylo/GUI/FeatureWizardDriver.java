package edu.drexel.psal.jstylo.GUI;

import com.jgaap.generics.*;
import edu.drexel.psal.jstylo.generics.CumulativeFeatureDriver;
import edu.drexel.psal.jstylo.generics.FeatureDriver;
import edu.drexel.psal.jstylo.generics.Logger;
import edu.drexel.psal.jstylo.generics.Logger.*;
import edu.drexel.psal.jstylo.generics.NormBaselineEnum;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.*;
import java.io.File;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;

/**
 * Class for populating all canonicizers, event drivers and event cullers to be
 * used by the feature configuration wizard.
 *
 * @author Ariel Stolerman
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class FeatureWizardDriver {

	/*
	 * ======================= FeatureWizard listeners =======================
	 */

	/** Initialize all feature wizard listeners. */
	public static void initListeners(final FeatureWizard fw) {

		// back, cancel and next buttons listeners
		ActionListener goBack = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln("'Back' button clicked in the feature wizard.");
				fw.mainJTabbedPane.setSelectedIndex(fw.mainJTabbedPane.getSelectedIndex() - 1);
			}
		};

		ActionListener cancel = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln("'Cancel' button clicked in the feature wizard.");
				int answer = JOptionPane.showConfirmDialog(null, "Abort feature configuration?",
						"Cancel Feature Configuration", JOptionPane.OK_CANCEL_OPTION);
				if (answer == JOptionPane.OK_OPTION) {
					Logger.logln("feature configuration aborted.");
					// reset and hide
					resetFeatureWizard(fw);
					fw.dispose();
				}
				Logger.logln("feature configuration continued.");
			}
		};

		ActionListener goNext = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Logger.logln("'Next' button clicked in the feature wizard.");
				fw.mainJTabbedPane.setSelectedIndex(fw.mainJTabbedPane.getSelectedIndex() + 1);
			}
		};

		// feature name and description tab
		// ================================

		// cancel button
		fw.nameCancelJButton.addActionListener(cancel);

		// next button
		fw.nameNextJButton.addActionListener(goNext);

		// text pre-processing tab
		// =======================

		// available list
		fw.avCanonJList.addListSelectionListener(new ListSelectionListener() {
			private int lastSelectedIndex = -2;

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if (lastSelectedIndex == fw.avCanonJList.getSelectedIndex())
					return;
				if (fw.avCanonJList.isSelectionEmpty()) {
					Logger.logln("Available text pre-processing tool list unselected");
					fw.canonDescContentJLabel.setText("");
				} else {
					Logger.logln("Available text pre-processing tool selected at index "
							+ fw.avCanonJList.getSelectedIndex());
					fw.canonDescContentJLabel.setText(
							"<html><p>" + getCanonicizer((String) fw.avCanonJList.getSelectedValue()).longDescription()
									+ "</p></html>");
				}
				lastSelectedIndex = fw.avCanonJList.getSelectedIndex();
			}
		});

		// available \ add button
		fw.avCanonAddJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln("'Add' button clicked under the text pre-processing tab.");

				if (fw.avCanonJList.isSelectionEmpty()) {
					JOptionPane.showMessageDialog(null, "You must select a pre-processing tool to be added.",
							"Add Pre-Processing Tool Error", JOptionPane.ERROR_MESSAGE);
				} else {
					fw.selCanonJListModel.addElement(fw.avCanonJList.getSelectedValue());
					fw.canonParamList.add(getConfigPanel(fw, 1, canonMap.get(fw.avCanonJList.getSelectedValue())));
					Logger.logln("Added '" + fw.avCanonJList.getSelectedValue()
							+ "' to selected text pre-processing tool list.");
				}
			}
		});

		// selected list
		fw.selCanonJList.addListSelectionListener(new ListSelectionListener() {
			private int lastSelectedIndex = -2;

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if (lastSelectedIndex == fw.selCanonJList.getSelectedIndex())
					return;
				if (fw.selCanonJList.isSelectionEmpty()) {
					Logger.logln("Selected text pre-processing tool list unselected");
					fw.canonConfigJScrollPane.setViewportView(null);
					fw.canonDescContentJLabel.setText("");
				} else {
					Logger.logln("Selected text pre-processing tool selected at index "
							+ fw.selCanonJList.getSelectedIndex());
					fw.canonConfigJScrollPane
							.setViewportView(fw.canonParamList.get(fw.selCanonJList.getSelectedIndex()));
					fw.canonDescContentJLabel.setText(
							"<html><p>" + getCanonicizer((String) fw.selCanonJList.getSelectedValue()).longDescription()
									+ "</p></html>");
				}
				lastSelectedIndex = fw.selCanonJList.getSelectedIndex();
			}
		});

		// selected \ remove button
		fw.selCanonRemoveJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln("'Remove' button clicked under the text pre-processing tab.");

				if (fw.selCanonJList.isSelectionEmpty()) {
					JOptionPane.showMessageDialog(null, "You must select a pre-processing tool to be removed.",
							"Remove Pre-Processing Tool Error", JOptionPane.ERROR_MESSAGE);
				} else {
					String toRemove = (String) fw.selCanonJList.getSelectedValue();
					fw.canonParamList.remove(fw.selCanonJList.getSelectedIndex());
					fw.selCanonJListModel.removeElementAt(fw.selCanonJList.getSelectedIndex());
					Logger.logln("Removed '" + toRemove + "' from selected text pre-processing tool list.");
				}
			}
		});

		// back button
		fw.canonBackJButton.addActionListener(goBack);

		// cancel button
		fw.canonCancelJButton.addActionListener(cancel);

		// next button
		fw.canonNextJButton.addActionListener(goNext);

		// feature extractors tab
		// ======================

		// features tree
		fw.featuresJTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent arg0) {
				if (fw.featuresJTree.getSelectionPath().getPath().length == 3) {
					String edString = ((DefaultMutableTreeNode) fw.featuresJTree.getSelectionPath().getPath()[2])
							.toString();
					EventDriver ed = getEventDriver(edString);
					fw.featureDescContentJLabel.setText("<html><p>" + ed.longDescription() + "</p></html>");
					fw.edParamList = getConfigPanel(fw, 2, ed.getClass().getName());
					fw.featuresConfigJScrollPane.setViewportView(fw.edParamList);
				}
			}
		});

		// back button
		fw.featuresBackJButton.addActionListener(goBack);

		// cancel button
		fw.featuresCancelJButton.addActionListener(cancel);

		// next button
		fw.featuresNextJButton.addActionListener(goNext);

		// features post-processing tab
		// ============================

		// available list
		fw.avCullJList.addListSelectionListener(new ListSelectionListener() {
			private int lastSelectedIndex = -2;

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if (lastSelectedIndex == fw.avCullJList.getSelectedIndex())
					return;
				if (fw.avCullJList.isSelectionEmpty()) {
					Logger.logln("Available feature post-processing tool list unselected");
					fw.cullDescContentJLabel.setText("");
				} else {
					Logger.logln("Available feature post-processing tool selected at index "
							+ fw.avCullJList.getSelectedIndex());
					fw.cullDescContentJLabel.setText("<html><p>"
							+ getCuller((String) fw.avCullJList.getSelectedValue()).longDescription() + "</p></html>");
				}
				lastSelectedIndex = fw.avCullJList.getSelectedIndex();
			}
		});

		// available \ add button
		fw.avCullAddJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln("'Add' button clicked under the feature post-processing tab.");

				if (fw.avCullJList.isSelectionEmpty()) {
					JOptionPane.showMessageDialog(null, "You must select a feature post-processing tool to be added.",
							"Add Feature Post-Processing Tool Error", JOptionPane.ERROR_MESSAGE);
				} else {
					fw.selCullJListModel.addElement(fw.avCullJList.getSelectedValue());
					fw.cullParamList.add(getConfigPanel(fw, 3, cullersMap.get(fw.avCullJList.getSelectedValue())));
					Logger.logln("Added '" + fw.avCullJList.getSelectedValue()
							+ "' to selected feature post-processing tool list.");
				}
			}
		});

		// selected list
		fw.selCullJList.addListSelectionListener(new ListSelectionListener() {
			private int lastSelectedIndex = -2;

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
				if (lastSelectedIndex == fw.selCullJList.getSelectedIndex())
					return;
				if (fw.selCullJList.isSelectionEmpty()) {
					Logger.logln("Feature post-processing tool list unselected");
					fw.cullConfigJScrollPane.setViewportView(null);
					fw.cullDescContentJLabel.setText("");
				} else {
					Logger.logln(
							"Feature post-processing tool selected at index " + fw.selCullJList.getSelectedIndex());
					fw.cullConfigJScrollPane.setViewportView(fw.cullParamList.get(fw.selCullJList.getSelectedIndex()));
					fw.cullDescContentJLabel.setText("<html><p>"
							+ getCuller((String) fw.selCullJList.getSelectedValue()).longDescription() + "</p></html>");
				}
				lastSelectedIndex = fw.selCullJList.getSelectedIndex();
			}
		});

		// selected \ remove button
		fw.selCullRemoveJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln("'Remove' button clicked under the feature post-processing tab.");

				if (fw.selCullJList.isSelectionEmpty()) {
					JOptionPane.showMessageDialog(null, "You must select a feature post-processing tool to be removed.",
							"Remove Feature Post-Processing Tool Error", JOptionPane.ERROR_MESSAGE);
				} else {
					String toRemove = (String) fw.selCullJList.getSelectedValue();
					fw.cullParamList.remove(fw.selCullJList.getSelectedIndex());
					fw.selCullJListModel.removeElementAt(fw.selCullJList.getSelectedIndex());
					Logger.logln("Removed '" + toRemove + "' from selected feature post-processing tool list.");
				}
			}
		});

		// back button
		fw.cullBackJButton.addActionListener(goBack);

		// cancel button
		fw.cullCancelJButton.addActionListener(cancel);

		// next button
		fw.cullNextJButton.addActionListener(goNext);

		// normalizaton tab
		// ================

		// normalization combo-box
		fw.normChooserJComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				NormBaselineEnum nbl = NormBaselineEnum
						.getNormBaselineFromTitle((String) fw.normChooserJComboBox.getSelectedItem());

				// set description
				fw.normDescContentJLabel.setText("<html><p>" + nbl.getDescription() + "</p></html>");
			}
		});

		// factoring
		fw.normFactorJTextField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				try {
					Double.valueOf(fw.normFactorJTextField.getText());
				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(null, "Normalization factor value must be a double.",
							"Normalization Factor Error", JOptionPane.ERROR_MESSAGE);
					fw.mainJTabbedPane.setSelectedIndex(4);
					fw.normFactorJTextField.requestFocusInWindow();
					fw.normFactorJTextField.selectAll();
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});

		// back button
		fw.normBackJButton.addActionListener(goBack);

		// cancel button
		fw.normCancelJButton.addActionListener(cancel);

		// ==================
		// = =
		// add feature button
		// = =
		// ==================
		fw.normAddFeatureJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln("'Add Feature' button clicked in the normalization tab");

				// create a new feature driver
				fw.fd = new FeatureDriver();

				// set name
				String name = fw.nameJTextField.getText();
				if (name.matches("\\s*")) {
					JOptionPane.showMessageDialog(fw, "Feature name must be non-empty.", "Feature Name Error",
							JOptionPane.ERROR_MESSAGE);
					Logger.logln("Add feature error: given empty feature name.", LogOut.STDERR);
					return;
				}
				CumulativeFeatureDriver cfd = fw.parent.cfd;

				// if new feature - check for duplicates
				if (fw.editMode == false) {
					for (int i = 0; i < cfd.numOfFeatureDrivers(); i++) {
						if (name.equals(cfd.featureDriverAt(i).getName())) {
							JOptionPane.showMessageDialog(fw, "Feature with name \"" + name + "\" already exists.",
									"Feature Name Error", JOptionPane.ERROR_MESSAGE);
							Logger.logln("Add feature error: feature name already in use.", LogOut.STDERR);
							return;
						}
					}
				}
				fw.fd.setName(name);
				Logger.logln("feature name: " + name);

				// set description
				fw.fd.setDescription(fw.descJTextPane.getText());
				Logger.logln("feature description: " + fw.descJTextPane.getText());

				// set canonicizers
				Logger.logln("canonicizers:");
				for (int i = 0; i < fw.selCanonJListModel.getSize(); i++) {
					// create canonicizer
					String canonName = (String) fw.selCanonJListModel.getElementAt(i);
					Canonicizer c = getCanonicizer(canonName);
					Logger.logln("- " + canonName);

					// set parameters
					JPanel canonParams = fw.canonParamList.get(i);
					if (canonParams != null) {
						Logger.logln("  parameters:");
						for (Component paramComp : canonParams.getComponents()) {
							JPanel canonParam = (JPanel) paramComp;
							String paramName = ((JLabel) canonParam.getComponent(0)).getText();

							String paramVal = null;
							if (canonParam.getComponent(1) instanceof JTextField) {
								paramVal = ((JTextField) canonParam.getComponent(1)).getText();
							} else if (canonParam.getComponent(1) instanceof JComboBox) {
								paramVal = (String) ((JComboBox) canonParam.getComponent(1)).getSelectedItem();
							}
							c.setParameter(paramName, paramVal);
							Logger.logln("  - " + paramName + ": " + paramVal);
						}
					}
					fw.fd.addCanonicizer(c);
				}

				// set feature
				if (fw.featuresJTree.getSelectionCount() == 0
						|| fw.featuresJTree.getSelectionPath().getPath().length != 3) {
					JOptionPane.showMessageDialog(fw, "No feature extractor selected.", "Feature Error",
							JOptionPane.ERROR_MESSAGE);
					Logger.logln("Add feature error: No feature extractor selected.", LogOut.STDERR);
					return;
				} else {
					// set event driver
					String edString = ((DefaultMutableTreeNode) fw.featuresJTree.getSelectionPath().getPath()[2])
							.toString();
					EventDriver ed = getEventDriver(edString);
					Logger.logln("event driver: " + ed.displayName());

					// set isCalcHist
					fw.fd.setCalcHist(getIsCalcHist(ed.getClass().getName()));
					Logger.logln("calculate histogram: " + fw.fd.isCalcHist());

					// set parameters
					if (fw.edParamList != null) {
						Logger.logln("  parameters:");
						for (Component paramComp : fw.edParamList.getComponents()) {
							JPanel cullParam = (JPanel) paramComp;
							String paramName = ((JLabel) cullParam.getComponent(0)).getText();

							String paramVal = null;
							if (cullParam.getComponent(1) instanceof JTextField) {
								paramVal = ((JTextField) cullParam.getComponent(1)).getText();
							} else if (cullParam.getComponent(1) instanceof JComboBox) {
								paramVal = (String) ((JComboBox) cullParam.getComponent(1)).getSelectedItem();
							}
							ed.setParameter(paramName, paramVal);
							Logger.logln("  - " + paramName + ": " + paramVal);
						}
					}

					fw.fd.setUnderlyingEventDriver(ed);
				}

				// set cullers
				Logger.logln("cullers:");
				for (int i = 0; i < fw.selCullJListModel.getSize(); i++) {
					// create culler
					String cullName = (String) fw.selCullJListModel.getElementAt(i);
					EventCuller ec = getCuller(cullName);
					Logger.logln("- " + cullName);

					// set parameters
					JPanel cullParams = fw.cullParamList.get(i);
					if (cullParams != null) {
						Logger.logln("  parameters:");
						for (Component paramComp : cullParams.getComponents()) {
							JPanel cullParam = (JPanel) paramComp;
							String paramName = ((JLabel) cullParam.getComponent(0)).getText();

							String paramVal = null;
							if (cullParam.getComponent(1) instanceof JTextField) {
								paramVal = ((JTextField) cullParam.getComponent(1)).getText();
							} else if (cullParam.getComponent(1) instanceof JComboBox) {
								paramVal = (String) ((JComboBox) cullParam.getComponent(1)).getSelectedItem();
							}
							ec.setParameter(paramName, paramVal);
							Logger.logln("  - " + paramName + ": " + paramVal);
						}
					}
					fw.fd.addEventCuller(ec);
				}

				// normalization and factoring
				fw.fd.setNormBaseline(
						NormBaselineEnum.getNormBaselineFromTitle((String) fw.normChooserJComboBox.getSelectedItem()));
				fw.fd.setNormFactor(Double.valueOf(fw.normFactorJTextField.getText()));
				Logger.logln("normalization baseline: " + fw.fd.getNormBaseline());
				Logger.logln("normalization factor: " + fw.fd.getNormFactor());

				// --------------------------------------------------------------

				// update GUIMain
				if (fw.editMode == false) {
					// add new feature
					fw.parent.cfd.addFeatureDriver(fw.fd);
					Logger.logln("Added feature driver '" + fw.fd.getName()
							+ "' to main feature set. Total feature drivers: " + fw.parent.cfd.numOfFeatureDrivers());
				} else {
					// update old feature
					fw.parent.cfd.replaceFeatureDriverAt(fw.originalIndex, fw.fd);
					Logger.logln("Updated feature driver '" + fw.fd.getName() + "' in the main feature set.");
				}

				// update GUIMain gui and dispose
				GUIUpdateInterface.updateFeatureSetView(fw.parent);
				fw.dispose();
			}
		});
	}

	/** Resets all fields of the input feature wizard. */
	public static void resetFeatureWizard(FeatureWizard fw) {
		fw.nameJTextField.setText("");
		fw.descJTextPane.setText("");

		fw.avCanonJList.clearSelection();
		fw.selCanonJListModel.removeAllElements();
		fw.canonConfigJScrollPane.removeAll();
		fw.canonDescContentJLabel.setText("");

		fw.featuresJTree.setSelectionRow(0);
		fw.featuresConfigJScrollPane.removeAll();
		fw.featureDescJLabel.setText("");

		fw.avCullJList.clearSelection();
		fw.selCullJListModel.removeAllElements();
		fw.cullConfigJScrollPane.removeAll();
		fw.cullDescContentJLabel.setText("");

		fw.normChooserJComboBox.setSelectedIndex(0);
		fw.normDescJLabel.setText("<html><p>" + NormBaselineEnum.NONE.getDescription() + "</p></html>");
		fw.normFactorJTextField.setText("1");

		fw.mainJTabbedPane.setSelectedIndex(0);
	}

	protected static int cellPadding = FeatureWizard.cellPadding;

	/*
	 * ======================== parameter GUI components ========================
	 */

	public enum ParamPanelConstraint {
		INTEGER, DOUBLE, NONE
	}

	/** Creates a text-field with constrained to accept only integer values. */
	public static JPanel getParamTextFieldPanel(final FeatureWizard fw, final int tabIndex, final String fieldName,
			String defaultValue, final ParamPanelConstraint constraint) {

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, cellPadding, cellPadding));

		// add label
		JLabel name = new JLabel(fieldName);
		panel.add(name);

		// add value text field with constraint
		final JTextField value = new JTextField(defaultValue);
		value.setPreferredSize(new Dimension(100, 20));
		value.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent arg0) {
				if (constraint == ParamPanelConstraint.INTEGER) {
					try {
						Integer.valueOf(value.getText());
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null, fieldName + " must be an integer.", "Value Error",
								JOptionPane.ERROR_MESSAGE);
						fw.mainJTabbedPane.setSelectedIndex(tabIndex);
						value.requestFocusInWindow();
						value.selectAll();
					}
				} else if (constraint == ParamPanelConstraint.DOUBLE) {
					try {
						Double.valueOf(value.getText());
					} catch (NumberFormatException e) {
						JOptionPane.showMessageDialog(null, fieldName + " must be an double.", "Value Error",
								JOptionPane.ERROR_MESSAGE);
						fw.mainJTabbedPane.setSelectedIndex(tabIndex);
						value.requestFocusInWindow();
						value.selectAll();
					}
				}
			}

			@Override
			public void focusGained(FocusEvent arg0) {
			}
		});
		panel.add(value);

		return panel;
	}

	/** Creates a text-field with file chooser. */
	public static JPanel getParamFileChooserPanel(final FeatureWizard fw, final int tabIndex, final String fieldName,
			String defaultValue, final ParamPanelConstraint constraint) {

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, cellPadding, cellPadding));

		// add label
		JLabel name = new JLabel(fieldName);
		panel.add(name);

		// add value text field with constraint
		final JTextField value = new JTextField(defaultValue);
		value.setEditable(false);
		value.setPreferredSize(new Dimension(200, 20));
		panel.add(value);

		JButton browse = new JButton("Browse...");
		browse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Logger.logln("'Browse' button clicked for parameter value.");

				int answer = 0;
				JFileChooser load = new JFileChooser(new File("."));
				answer = load.showOpenDialog(fw);

				if (answer == JFileChooser.APPROVE_OPTION) {
					try {
						value.setText(load.getSelectedFile().getCanonicalPath());
					} catch (Exception e) {
						Logger.logln("Error reading filepath for chosen file.", LogOut.STDERR);
						e.printStackTrace();
					}

				} else {
					Logger.logln("Browse operation canceled.");
				}
			}
		});
		panel.add(browse);

		return panel;
	}

	/** Creates a text-field with constrained to accept only integer values. */
	public static JPanel getParamComboBoxPanel(final FeatureWizard fw, final int tabIndex, final String fieldName,
			String[] values, final ParamPanelConstraint constraint) {

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, cellPadding, cellPadding));

		// add label
		JLabel name = new JLabel(fieldName);
		panel.add(name);

		// add value text field with constraint
		final JComboBox value = new JComboBox(values);
		panel.add(value);

		return panel;
	}

	/**
	 * main function to create all parameter panels for all canonicizers, event
	 * drivers and cullers This mathod should correspond to
	 * generics.FeatureDriver.getClassParams()
	 */
	public static JPanel getConfigPanel(FeatureWizard fw, int tabIndex, String className) {
		JPanel panel = new JPanel();
		BoxLayout bl = new BoxLayout(panel, BoxLayout.Y_AXIS);
		panel.setLayout(bl);

		// canonicizers
		// ============

		// JGAAP canonicizers

		// JStylo canonicizers
		if (className.equals("edu.drexel.psal.jstylo.canonicizers.RemoveFirstNLines")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "1", ParamPanelConstraint.INTEGER));
		}

		// event drivers
		// =============

		// JGAAP event drivers
		else if (className.equals("com.jgaap.eventDrivers.BlackListEventDriver")) {
			panel.add(getParamComboBoxPanel(fw, tabIndex, "underlyingEvents", getUnderlyingEvents(),
					ParamPanelConstraint.NONE));
			panel.add(getParamFileChooserPanel(fw, tabIndex, "filename", "", ParamPanelConstraint.NONE));
		} else if (className.equals("com.jgaap.eventDrivers.WhiteListEventDriver")) {
			panel.add(getParamComboBoxPanel(fw, tabIndex, "underlyingEvents", getUnderlyingEvents(),
					ParamPanelConstraint.NONE));
			panel.add(getParamFileChooserPanel(fw, tabIndex, "filename", "", ParamPanelConstraint.NONE));
		} else if (className.equals("com.jgaap.eventDrivers.MNLetterWordEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "M", "2", ParamPanelConstraint.INTEGER));
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "3", ParamPanelConstraint.INTEGER));
			// panel.add(getParamComboBoxPanel(fw, tabIndex, "underlyingEvents",
			// getUnderlyingEvents(),
			// ParamPanelConstraint.NONE));

		} else if (className.equals("com.jgaap.eventDrivers.CharacterNGramEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "2", ParamPanelConstraint.INTEGER));
		} else if (className.equals("com.jgaap.eventDrivers.POSNGramEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "2", ParamPanelConstraint.INTEGER));
		} else if (className.equals("com.jgaap.eventDrivers.RareWordsEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "M", "2", ParamPanelConstraint.INTEGER));
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "3", ParamPanelConstraint.INTEGER));
			panel.add(getParamComboBoxPanel(fw, tabIndex, "underlyingEvents", getUnderlyingEvents(),
					ParamPanelConstraint.NONE));
		} else if (className.equals("com.jgaap.eventDrivers.SuffixEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "length", "3", ParamPanelConstraint.INTEGER));
			panel.add(getParamTextFieldPanel(fw, tabIndex, "minimumlength", "5", ParamPanelConstraint.INTEGER));
			panel.add(getParamComboBoxPanel(fw, tabIndex, "underlyingEvents", getUnderlyingEvents(),
					ParamPanelConstraint.NONE));
		} else if (className.equals("com.jgaap.eventDrivers.SyllableTransitionEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "2", ParamPanelConstraint.INTEGER));
		} else if (className.equals("com.jgaap.eventDrivers.TruncatedEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "length", "2", ParamPanelConstraint.INTEGER));
			panel.add(getParamComboBoxPanel(fw, tabIndex, "underlyingEvents", getUnderlyingEvents(),
					ParamPanelConstraint.NONE));
		} else if (className.equals("com.jgaap.eventDrivers.TumblingNGramEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "2", ParamPanelConstraint.INTEGER));
			panel.add(getParamComboBoxPanel(fw, tabIndex, "underlyingEvents", getUnderlyingEvents(),
					ParamPanelConstraint.NONE));
		} else if (className.equals("com.jgaap.eventDrivers.VowelMNLetterWordEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "M", "2", ParamPanelConstraint.INTEGER));
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "3", ParamPanelConstraint.INTEGER));
		} else if (className.equals("com.jgaap.eventDrivers.WordNGramEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "2", ParamPanelConstraint.INTEGER));
		}

		// JStylo event drivers
		else if (className.equals("edu.drexel.psal.jstylo.eventDrivers.EventsCounterEventDriver")) {

			panel.add(getParamComboBoxPanel(fw, tabIndex, "underlyingEvents", getUnderlyingEvents(),
					ParamPanelConstraint.NONE));
		} else if (className.equals("edu.drexel.psal.jstylo.eventDrivers.FastTagPOSNGramsEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "2", ParamPanelConstraint.INTEGER));
		} else if (className.equals("edu.drexel.psal.jstylo.eventDrivers.MaxentPOSNGramsEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "2", ParamPanelConstraint.INTEGER));
		} else if (className.equals("edu.drexel.psal.jstylo.eventDrivers.LetterNGramEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "2", ParamPanelConstraint.INTEGER));
		} else if (className.equals("edu.drexel.psal.jstylo.eventDrivers.ListEventDriver")) {
			panel.add(getParamComboBoxPanel(fw, tabIndex, "sort", new String[]{"false", "true"},
					ParamPanelConstraint.NONE));
			panel.add(getParamComboBoxPanel(fw, tabIndex, "whiteList", new String[]{"false", "true"},
					ParamPanelConstraint.NONE));
			panel.add(getParamComboBoxPanel(fw, tabIndex, "keepLexiconInMem", new String[]{"false", "true"},
					ParamPanelConstraint.NONE));
			panel.add(getParamComboBoxPanel(fw, tabIndex, "underlyingEvents", getUnderlyingEvents(),
					ParamPanelConstraint.NONE));
			panel.add(getParamFileChooserPanel(fw, tabIndex, "filename", "", ParamPanelConstraint.NONE));
		} else if (className.equals("edu.drexel.psal.jstylo.eventDrivers.ListRegexpEventDriver")) {
			// panel.add(getParamComboBoxPanel(fw, tabIndex, "sort", new
			// String[]{"false","true"},
			// ParamPanelConstraint.NONE));
			panel.add(getParamComboBoxPanel(fw, tabIndex, "whiteList", new String[]{"false", "true"},
					ParamPanelConstraint.NONE));
			panel.add(getParamComboBoxPanel(fw, tabIndex, "keepLexiconInMem", new String[]{"false", "true"},
					ParamPanelConstraint.NONE));
			// panel.add(getParamComboBoxPanel(fw, tabIndex, "underlyingEvents",
			// getUnderlyingEvents(),
			// ParamPanelConstraint.NONE));
			panel.add(getParamFileChooserPanel(fw, tabIndex, "filename", "", ParamPanelConstraint.NONE));
		} else if (className.equals("edu.drexel.psal.jstylo.eventDrivers.RegexpCounterEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "regexp", "", ParamPanelConstraint.NONE));
		} else if (className.equals("edu.drexel.psal.jstylo.eventDrivers.RegexpEventDriver")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "regexp", "", ParamPanelConstraint.NONE));
		}

		// cullers
		// =======

		// JGAAP cullers
		else if (className.equals("com.jgaap.eventCullers.LeastCommonEvents")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "numEvents", "50", ParamPanelConstraint.INTEGER));
		} else if (className.equals("com.jgaap.eventCullers.MostCommonEvents")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "numEvents", "50", ParamPanelConstraint.INTEGER));
		}

		// JStylo cullers
		else if (className.equals("edu.drexel.psal.jstylo.eventCullers.LeastCommonEventsExtended")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "50", ParamPanelConstraint.INTEGER));
		} else if (className.equals("edu.drexel.psal.jstylo.eventCullers.MostCommonEventsExtended")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "50", ParamPanelConstraint.INTEGER));
		} else if (className.equals("edu.drexel.psal.jstylo.eventCullers.MinAppearances")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "10", ParamPanelConstraint.INTEGER));
		} else if (className.equals("edu.drexel.psal.jstylo.eventCullers.MaxAppearances")) {
			panel.add(getParamTextFieldPanel(fw, tabIndex, "N", "10", ParamPanelConstraint.INTEGER));
		}

		// in any other case
		else {
			panel = null;
		}

		return panel;
	}

	/** Creates list of all available event driver class names. */
	protected static String[] getUnderlyingEvents() {
		List<String> edList = new ArrayList<String>();
		for (String type : edMap.keySet()) {
			for (String ed : edMap.get(type).keySet()) {
				edList.add(ed);
			}
		}
		String[] edArr = new String[edList.size()];
		for (int i = 0; i < edArr.length; i++)
			edArr[i] = edList.get(i);

		return edArr;
	}

	/**
	 * Returns the isCalcHist value for the given event driver class name (false for
	 * counters and readability metrics).
	 */
	public static boolean getIsCalcHist(String className) {
		if (className.endsWith("eventDrivers.CharCounterEventDriver")
				|| className.endsWith("eventDrivers.LetterCounterEventDriver")
				|| className.endsWith("eventDrivers.WordCounterEventDriver")
				|| className.endsWith("eventDrivers.UniqueWordsCounterEventDriver")
				|| className.endsWith("eventDrivers.SyllableCounterEventDriver")
				|| className.endsWith("eventDrivers.SentenceCounterEventDriver")
				|| className.endsWith("eventDrivers.EventsCounterEventDriver")
				|| className.endsWith("eventDrivers.FleschReadingEaseScoreEventDriver")
				|| className.endsWith("eventDrivers.RegexpCounterEventDriver")
				|| className.endsWith("eventDrivers.GunningFogIndexEventDriver"))
			return false;
		else
			return true;
	}

	/*
	 * =========================== All feature components maps
	 * ===========================
	 */

	// canonicizers
	protected static Map<String, String> canonMap;
	protected static String[] canonClasses = new String[]{
			// JGAAP original canonicizers
			"com.jgaap.canonicizers.AddErrors", "com.jgaap.canonicizers.NormalizeWhitespace",
			"com.jgaap.canonicizers.StripComments", "com.jgaap.canonicizers.StripNonPunc",
			"com.jgaap.canonicizers.StripNullCharacters", "com.jgaap.canonicizers.StripNumbers",
			"com.jgaap.canonicizers.UnifyCase",

			// JStylo canonicizers
			"edu.drexel.psal.jstylo.canonicizers.RemoveFirstNLines",
			"edu.drexel.psal.jstylo.canonicizers.StripEdgesPunctuation",
			"edu.drexel.psal.jstylo.canonicizers.WordEndsPunctSeparator",
			"edu.drexel.psal.jstylo.canonicizers.StripSpaces", "edu.drexel.psal.jstylo.canonicizers.ApplySpecialKeys",
			"edu.drexel.psal.jstylo.canonicizers.RemoveSpecialKeys"};

	// event drivers
	protected static Map<String, Map<String, String>> edMap;
	protected static String[][] edClasses = new String[][]{

			// Basic
			new String[]{"Basic",
					// JGAAP event drivers
					"com.jgaap.eventDrivers.FirstWordInSentenceEventDriver",
					"com.jgaap.eventDrivers.CharacterEventDriver", "com.jgaap.eventDrivers.NaiveWordEventDriver",
					"com.jgaap.eventDrivers.FreqEventDriver", "com.jgaap.eventDrivers.MWFunctionWordsEventDriver",
					"com.jgaap.eventDrivers.NewLineEventDriver", "com.jgaap.eventDrivers.RareWordsEventDriver",
					"com.jgaap.eventDrivers.SentenceEventDriver",
					"com.jgaap.eventDrivers.SentenceLengthWithWordsEventDriver",
					"com.jgaap.eventDrivers.SuffixEventDriver", "com.jgaap.eventDrivers.SyllableTransitionEventDriver",
					"com.jgaap.eventDrivers.VowelInitialWordEventDriver",
					"com.jgaap.eventDrivers.VowelMNLetterWordEventDriver",
					"com.jgaap.eventDrivers.WordLengthEventDriver", "com.jgaap.eventDrivers.WordSyllablesEventDriver",

					// JStylo event drivers
					"edu.drexel.psal.jstylo.eventDrivers.RegexpEventDriver",
					"edu.drexel.psal.jstylo.eventDrivers.WordLengthEventDriver",},

			// POS
			new String[]{"Part-Of-Speech",
					// JGAAP event drivers
					"com.jgaap.eventDrivers.CoarsePOSTagger", "com.jgaap.eventDrivers.PartOfSpeechEventDriver",
					"com.jgaap.eventDrivers.POSNGramEventDriver",

					// JStylo event drivers
					"edu.drexel.psal.jstylo.eventDrivers.FastTagPOSTagsEventDriver",
					"edu.drexel.psal.jstylo.eventDrivers.FastTagPOSNGramsEventDriver",
					"edu.drexel.psal.jstylo.eventDrivers.MaxentPOSTagsEventDriver",
					"edu.drexel.psal.jstylo.eventDrivers.MaxentPOSNGramsEventDriver",},

			// grams
			new String[]{"Grams",
					// JGAAP event drivers
					"com.jgaap.eventDrivers.CharacterNGramEventDriver",
					"com.jgaap.eventDrivers.MNLetterWordEventDriver", "com.jgaap.eventDrivers.TumblingNGramEventDriver",
					"com.jgaap.eventDrivers.WordNGramEventDriver",

					// JStylo event drivers
					"edu.drexel.psal.jstylo.eventDrivers.LetterNGramEventDriver",},

			// dictionary-based
			new String[]{"Dictionary",
					// JGAAP event drivers
					"com.jgaap.eventDrivers.BlackListEventDriver", "com.jgaap.eventDrivers.WhiteListEventDriver",
					"com.jgaap.eventDrivers.DefinitionsEventDriver",

					// JStylo event drivers
					"edu.drexel.psal.jstylo.eventDrivers.ListEventDriver",
					"edu.drexel.psal.jstylo.eventDrivers.ListRegexpEventDriver"},

			// counters
			new String[]{"Counters",
					// JStylo event drivers
					"edu.drexel.psal.jstylo.eventDrivers.CharCounterEventDriver",
					"edu.drexel.psal.jstylo.eventDrivers.LetterCounterEventDriver",
					"edu.drexel.psal.jstylo.eventDrivers.WordCounterEventDriver",
					"edu.drexel.psal.jstylo.eventDrivers.UniqueWordsCounterEventDriver",
					"edu.drexel.psal.jstylo.eventDrivers.SyllableCounterEventDriver",
					"edu.drexel.psal.jstylo.eventDrivers.SentenceCounterEventDriver",
					"edu.drexel.psal.jstylo.eventDrivers.EventsCounterEventDriver",
					"edu.drexel.psal.jstylo.eventDrivers.RegexpCounterEventDriver",},

			// readability metrics
			new String[]{"Readability Metrics", "edu.drexel.psal.jstylo.eventDrivers.FleschReadingEaseScoreEventDriver",
					"edu.drexel.psal.jstylo.eventDrivers.GunningFogIndexEventDriver",},

			// Misc.
			new String[]{"Misc.",
					// JGAAP event drivers
					"com.jgaap.eventDrivers.NamingTimeEventDriver", "com.jgaap.eventDrivers.PorterStemmerEventDriver",
					"com.jgaap.eventDrivers.PorterStemmerWithIrregularEventDriver",
					"com.jgaap.eventDrivers.ReactionTimeEventDriver", "com.jgaap.eventDrivers.TruncatedEventDriver",
					"com.jgaap.eventDrivers.TruncatedFreqEventDriver",
					"com.jgaap.eventDrivers.TruncatedNamingTimeEventDriver",
					"com.jgaap.eventDrivers.TruncatedReactionTimeEventDriver",

			// JStylo event drivers
			}};

	// cullers
	protected static Map<String, String> cullersMap;
	protected static String[] cullerClasses = new String[]{
			// JGAAP original cullers
			"com.jgaap.eventCullers.ExtremeCuller", "com.jgaap.eventCullers.LeastCommonEvents",
			"com.jgaap.eventCullers.MostCommonEvents",

			// JStylo cullers
			"edu.drexel.psal.jstylo.eventCullers.LeastCommonEventsExtended",
			"edu.drexel.psal.jstylo.eventCullers.MostCommonEventsExtended",
			"edu.drexel.psal.jstylo.eventCullers.MinAppearances",
			"edu.drexel.psal.jstylo.eventCullers.MaxAppearances",};

	/*
	 * ======= setters =======
	 */

	public static void initClasses() {
	}

	/** Populates all maps - canonicizers, event drivers and cullers. */
	public static void populateAll() {
		populateEventDrivers();
		populateCanonicizers();
		populateCullers();
	}

	/** Populates all canonicizers */
	public static void populateCanonicizers() {
		Logger.logln("Populating canonicizers...");
		canonMap = new HashMap<String, String>(canonClasses.length);

		// map all canonicizers from description to class name
		Canonicizer c;
		for (String className : canonClasses) {
			try {
				c = (Canonicizer) Class.forName(className).getConstructor().newInstance();
				canonMap.put(c.displayName(), className);
			} catch (Exception e) {
				Logger.logln("- could not add: " + className);
			}
		}

		Logger.logln("done!");
	}

	/** Populates all event drivers */
	public static void populateEventDrivers() {
		Logger.logln("Populating event drivers...");
		edMap = new HashMap<String, Map<String, String>>(edClasses.length);

		// map all event drivers from description to class name
		EventDriver ed;
		String className;
		String[] set;
		Map<String, String> map;
		int i, j;
		for (i = 0; i < edClasses.length; i++) {
			set = edClasses[i];
			map = new HashMap<String, String>(set.length);
			Logger.logln("adding event drivers under " + set[0]);
			for (j = 1; j < set.length; j++) {
				className = set[j];
				try {
					ed = (EventDriver) Class.forName(className).getConstructor().newInstance();
					map.put(ed.displayName(), className);
				} catch (Exception e) {
					Logger.logln("- could not add: " + className);
				}
			}
			edMap.put(set[0], map);
		}

		Logger.logln("done!");
	}

	/** Populates all event cullers */
	public static void populateCullers() {
		Logger.logln("Populating event cullers...");
		cullersMap = new HashMap<String, String>(cullerClasses.length);

		// map all canonicizers from description to class name
		EventCuller c;
		for (String className : cullerClasses) {
			try {
				c = (EventCuller) Class.forName(className).getConstructor().newInstance();
				cullersMap.put(c.displayName(), className);
			} catch (Exception e) {
				Logger.logln("- could not add: " + className);
			}
		}

		Logger.logln("done!");
	}

	/*
	 * ======= getters =======
	 */

	/**
	 * Instantiate a canonicizer from description (that is mapped to class name).
	 *
	 * @param desc
	 *            The description of the class as set in
	 *            FeatureWizardDriver.canonMap.
	 * @return A new instance of the canonicizer given in the description, or null
	 *         if no such class exists in the canonicizers map.
	 */
	public static Canonicizer getCanonicizer(String desc) {

		try {
			return (Canonicizer) Class.forName(canonMap.get(desc)).getConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Instantiate an event driver from description (that is mapped to class name).
	 *
	 * @param desc
	 *            The description of the class as set in FeatureWizardDriver.edMap.
	 * @return A new instance of the event driver given in the description, or null
	 *         if no such class exists in the event drivers map.
	 */
	public static EventDriver getEventDriver(String desc) {

		try {
			for (String key : edMap.keySet()) {
				if (edMap.get(key).containsKey(desc))
					return (EventDriver) Class.forName(edMap.get(key).get(desc)).getConstructor().newInstance();
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Instantiate an event culler from description (that is mapped to class name).
	 *
	 * @param desc
	 *            The description of the class as set in
	 *            FeatureWizardDriver.cullersMap.
	 * @return A new instance of the event culler given in the description, or null
	 *         if no such class exists in the event cullers map.
	 */
	public static EventCuller getCuller(String desc) {

		try {
			return (EventCuller) Class.forName(cullersMap.get(desc)).getConstructor().newInstance();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns the canonicizers map.
	 *
	 * @return The canonicizers map.
	 */
	public static Map<String, String> getCanonicizers() {
		return canonMap;
	}

	/**
	 * Returns the event drivers map.
	 *
	 * @return The event drivers map.
	 */
	public static Map<String, Map<String, String>> getEventDrivers() {
		return edMap;
	}

	/**
	 * Returns the event cullers map.
	 *
	 * @return The event cullers map.
	 */
	public static Map<String, String> getCullers() {
		return cullersMap;
	}
}
