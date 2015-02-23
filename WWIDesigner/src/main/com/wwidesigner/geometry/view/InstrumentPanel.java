/**
 * JPanel to display and edit an instrument definition.
 * 
 * Copyright (C) 2014, Edward Kort, Antoine Lefebvre, Burton Patkau.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wwidesigner.geometry.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JFormattedTextField.AbstractFormatterFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.LineBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.JTextComponent;

import com.jidesoft.grid.JideTable;
import com.wwidesigner.geometry.BorePoint;
import com.wwidesigner.geometry.Hole;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.Mouthpiece;
import com.wwidesigner.geometry.Termination;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.gui.util.DataPopulatedEvent;
import com.wwidesigner.gui.util.DataPopulatedListener;
import com.wwidesigner.gui.util.NumberFormatTableCellRenderer;
import com.wwidesigner.gui.util.NumericTableModel;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants.LengthType;
import com.wwidesigner.util.DoubleFormatter;

public class InstrumentPanel extends JPanel implements FocusListener,
		TableModelListener, ActionListener
{
	public static final String NEW_EVENT_ID = "newData";
	public static final String SAVE_EVENT_ID = "saveData";
	public static final int HOLE_TABLE_WIDTH = 310;
	public static final int BORE_TABLE_WIDTH = 175;

	// Instrument data fields.

	protected JTextField nameField;
	protected JTextPane descriptionField;
	protected JTextField lengthTypeField;
	protected JFormattedTextField mouthpiecePosition;
	protected ButtonGroup mouthpieceTypeGroup;
	protected JRadioButton embouchureHoleButton;
	protected JRadioButton fippleButton;
	protected JFormattedTextField innerDiameter;
	protected JFormattedTextField outerDiameter;
	protected JFormattedTextField embHoleHeight;
	protected JFormattedTextField windowLength;
	protected JFormattedTextField windowWidth;
	protected JFormattedTextField windowHeight;
	protected JFormattedTextField windwayLength;
	protected JFormattedTextField windwayHeight;
	protected JFormattedTextField fippleFactor;
	protected JFormattedTextField beta;
	protected JFormattedTextField terminationFlange;
	protected JideTable holeList;
	protected JideTable boreList;
	protected int dimensionalDecimalPrecision;
	protected int dimensionlessDecimalPrecision = 5;
	protected FormatterFactory formatterFactory = new FormatterFactory();

	// State fields for this component.
	// IsPopulated flags are true when required fields contain something,
	// but do not test whether data is valid.

	protected String priorValue; // Value a field had when it gained focus.
	protected boolean nameIsPopulated;
	protected boolean mouthpieceIsPopulated;
	protected boolean holesArePopulated;
	protected boolean boreIsPopulated;
	protected boolean terminationIsPopulated;
	protected List<DataPopulatedListener> populatedListeners;

	/**
	 * Create a panel to display and edit an instrument definition.
	 */
	public InstrumentPanel()
	{
		this.nameIsPopulated = false;
		this.holesArePopulated = true;
		this.boreIsPopulated = false;
		this.mouthpieceIsPopulated = false;
		this.terminationIsPopulated = false;
		this.priorValue = "";
		setLayout(new GridBagLayout());
		setNameWidget(0, 0, 1);
		setDescriptionWidget(0, 1, 1);
		setLengthTypeWidget(0, 2, 1);
		setMouthpieceWidget(1, 0, 3);
		setTerminationWidget(1, 3, 1);
		setHoleTableWidget(0, 3, GridBagConstraints.REMAINDER);
		setBoreTableWidget(1, 4, 1);
	}

	/**
	 * Load this panel with the instrument definition from an instrument XML
	 * file.
	 * 
	 * @param file
	 *            - contains XML for an instrument
	 * @return true if the load was successful
	 */
	public boolean loadFromFile(File file)
	{
		Instrument instrument = null;

		if (file != null)
		{
			BindFactory bindery = GeometryBindFactory.getInstance();
			try
			{
				instrument = (Instrument) bindery.unmarshalXml(file, true);
				if (instrument != null)
				{
					loadData(instrument, false);
					return true;
				}
			}
			catch (Exception ex)
			{
				JOptionPane.showMessageDialog(this, "File " + file.getName()
						+ " is not a valid Instrument file.");
			}
		}

		return false;
	}

	/**
	 * Return an empty row suitable for the hole or bore table.
	 */
	protected static Object[] emptyRow(int numCols)
	{
		if (numCols == 2)
		{
			return (new Object[] { null, null });
		}
		return (new Object[] { null, null, null });
	}

	/**
	 * Load an instrument into this panel.
	 * 
	 * @param instrument
	 *            - instrument definition to load.
	 * @param suppressChangeEvent
	 *            - if true, don't fire the DataPopulated event.
	 */
	public void loadData(Instrument instrument, boolean suppressChangeEvent)
	{
		if (instrument != null)
		{
			dimensionalDecimalPrecision = instrument.getLengthType()
					.getDecimalPrecision();
			nameField.setText(instrument.getName());
			descriptionField.setText(instrument.getDescription());
			Mouthpiece mouthpiece = instrument.getMouthpiece();
			windowLength.setValue(null);
			windowWidth.setValue(null);
			windwayLength.setValue(null);
			windwayHeight.setValue(null);
			fippleFactor.setValue(null);
			outerDiameter.setValue(null);
			innerDiameter.setValue(null);
			if (mouthpiece != null)
			{
				mouthpiecePosition.setValue(mouthpiece.getPosition());
				beta.setValue(mouthpiece.getBeta());
				if (mouthpiece.getEmbouchureHole() != null)
				{
					fippleButton.setSelected(false);
					embouchureHoleButton.setSelected(true);
					outerDiameter.setValue(mouthpiece.getEmbouchureHole()
							.getOuterDiameter());
					innerDiameter.setValue(mouthpiece.getEmbouchureHole()
							.getInnerDiameter());
					embHoleHeight.setValue(mouthpiece.getEmbouchureHole()
							.getHeight());
				}
				else
				{
					fippleButton.setSelected(true);
					embouchureHoleButton.setSelected(false);
					windowLength.setValue(mouthpiece.getFipple()
							.getWindowLength());
					windowWidth.setValue(mouthpiece.getFipple()
							.getWindowWidth());
					windowHeight.setValue(mouthpiece.getFipple()
							.getWindowHeight());
					windwayLength.setValue(mouthpiece.getFipple()
							.getWindwayLength());
					windwayHeight.setValue(mouthpiece.getFipple()
							.getWindwayHeight());
					fippleFactor.setValue(mouthpiece.getFipple()
							.getFippleFactor());
				}
			}
			else
			{
				// Default to fipple mouthpiece.
				fippleButton.setSelected(true);
				embouchureHoleButton.setSelected(false);
			}
			enableMouthpieceFields();

			stopTableEditing(holeList);
			stopTableEditing(boreList);
			holeList.getModel().removeTableModelListener(this);
			boreList.getModel().removeTableModelListener(this);
			resetTableData(holeList, 0, 5);
			resetTableData(boreList, 0, 2);
			DefaultTableModel model = (DefaultTableModel) holeList.getModel();
			boolean firstHole = true;
			Double priorHolePosition = 0.;
			for (Hole hole : instrument.getHole())
			{
				Double spacing = null;
				double holePosition = hole.getBorePosition();
				if (!firstHole)
				{
					spacing = holePosition - priorHolePosition;
				}
				else
				{
					firstHole = false;
				}
				model.addRow(new Object[] { hole.getName(),
						hole.getBorePosition(), spacing, hole.getDiameter(),
						hole.getHeight() });
				priorHolePosition = holePosition;
			}
			model = (DefaultTableModel) boreList.getModel();
			for (BorePoint point : instrument.getBorePoint())
			{
				model.addRow(new Double[] { point.getBorePosition(),
						point.getBoreDiameter() });
			}

			holeList.getModel().addTableModelListener(this);
			boreList.getModel().addTableModelListener(this);
			if (instrument.getTermination() != null)
			{
				terminationFlange.setValue(instrument.getTermination()
						.getFlangeDiameter());
			}
			else
			{
				terminationFlange.setValue(null);
			}
			lengthTypeField.setText(instrument.getLengthType().name());
			isNamePopulated();
			isMouthpiecePopulated();
			holesArePopulated = isTablePopulated(holeList, 0);
			boreIsPopulated = isTablePopulated(boreList, 2);
			isTerminationPopulated();
			if (!suppressChangeEvent)
			{
				fireDataStateChanged();
			}
		}
	}

	static protected boolean isPopulated(JTextComponent field)
	{
		String text = field.getText();
		return (text != null && text.trim().length() > 0);
	}

	/**
	 * Test whether there is a name in the name field, and set nameIsPopulated
	 * accordingly.
	 */
	protected void isNamePopulated()
	{
		nameIsPopulated = isPopulated(nameField);
	}

	/**
	 * Test whether the required mouthpiece fields are populated, and set
	 * mouthpieceIsPopulated accordingly.
	 */
	protected void isMouthpiecePopulated()
	{
		mouthpieceIsPopulated = false;
		if (!isPopulated(mouthpiecePosition))
		{
			// Not populated.
			return;
		}
		if (fippleButton.isSelected())
		{
			if (!isPopulated(windowLength) || !isPopulated(windowWidth))
			{
				return;
			}
		}
		else if (embouchureHoleButton.isSelected())
		{
			if (!isPopulated(outerDiameter) || !isPopulated(innerDiameter)
					|| !isPopulated(embHoleHeight))
			{
				return;
			}
		}
		else
		{
			// Should not occur.
			return;
		}
		mouthpieceIsPopulated = true;
	}

	/**
	 * Test whether the required termination field is populated, and set
	 * terminationIsPopulated accordingly.
	 */
	protected void isTerminationPopulated()
	{
		terminationIsPopulated = isPopulated(terminationFlange);
	}

	/**
	 * Test whether all entries in the hole or bore table contain valid data,
	 * and the table contains the minimum number of rows.
	 */
	static protected boolean isTablePopulated(JideTable table, int minimumRows)
	{
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		if (model == null || model.getRowCount() < minimumRows)
		{
			return false;
		}

		for (int i = 0; i < model.getRowCount(); i++)
		{
			for (int j = 0; j < model.getColumnCount(); j++)
			{
				if (model.getValueAt(i, j) == null)
				{
					return false;
				}
			}
		}
		return true;
	}

	static protected void stopTableEditing(JideTable table)
	{
		TableCellEditor editor = table.getCellEditor();
		if (editor != null)
		{
			editor.stopCellEditing();
		}
	}

	static protected void deleteSelectedRows(JideTable table)
	{
		stopTableEditing(table);
		int[] selectedRows = table.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		for (int row = selectedRows.length - 1; row >= 0; row--)
		{
			model.removeRow(selectedRows[row]);
		}
	}

	static protected void deleteUnselectedRows(JideTable table)
	{
		stopTableEditing(table);
		int[] selectedRows = table.getSelectedRows();
		if (selectedRows.length == 0)
		{
			// If there are no selected rows, delete nothing
			// rather than deleting everything.
			return;
		}
		Arrays.sort(selectedRows);

		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int numRows = model.getRowCount();
		for (int row = numRows - 1; row >= 0; row--)
		{
			if (Arrays.binarySearch(selectedRows, row) < 0)
			{
				model.removeRow(row);
			}
		}
	}

	static protected void insertRowAboveSelection(JideTable table)
	{
		stopTableEditing(table);
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		if (model.getRowCount() <= 0)
		{
			// If table is empty, we can't select anything.
			// Insert at the top, and leave nothing selected.
			model.insertRow(0, emptyRow(model.getColumnCount()));
			return;
		}
		int[] selectedRows = table.getSelectedRows();
		if (selectedRows.length == 0)
		{
			return;
		}
		Arrays.sort(selectedRows);
		int topIndex = selectedRows[0];

		model.insertRow(topIndex, emptyRow(model.getColumnCount()));

		// Re-select the original rows.
		ListSelectionModel selModel = table.getSelectionModel();
		selModel.clearSelection();
		for (int i = 0; i < selectedRows.length; i++)
		{
			int newSelectedRow = selectedRows[i] + 1;
			selModel.addSelectionInterval(newSelectedRow, newSelectedRow);
		}
	}

	static public void insertRowBelowSelection(JideTable table)
	{
		stopTableEditing(table);
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		int bottomIndex = 0; // If table is empty, insert at the top.
		if (model.getRowCount() > 0)
		{
			int[] selectedRows = table.getSelectedRows();
			if (selectedRows.length == 0)
			{
				return;
			}
			Arrays.sort(selectedRows);
			bottomIndex = selectedRows[selectedRows.length - 1] + 1;
		}

		model.insertRow(bottomIndex, emptyRow(model.getColumnCount()));
	}

	public void saveInstrument(File file)
	{
		Instrument instrument = getData();

		BindFactory bindery = GeometryBindFactory.getInstance();
		try
		{
			bindery.marshalToXml(instrument, file);
		}
		catch (Exception ex)
		{
			JOptionPane.showMessageDialog(getParent(), "Save failed: " + ex);
		}
	}

	public Instrument getData()
	{
		stopTableEditing(holeList);
		stopTableEditing(boreList);
		// The behavior (comment out) is incorrect and corrupts the view.
		// Another approach is required.
		// if (!nameIsPopulated)
		// {
		// JOptionPane.showMessageDialog(this, "Name field is required.");
		// nameField.requestFocusInWindow();
		// return null;
		// }
		Instrument instrument = new Instrument();
		instrument.setName(nameField.getText());
		instrument.setDescription(descriptionField.getText());
		// Something really strange happening in the call to the static
		// LengthType: depending on the order the Constraints, Instrument, and
		// Tuning are loaded, running an optimization generates an
		// enum-not-found exception which is irrelevant. This exception is not
		// thrown in the debugger. The implemented try/catch block "cures" the
		// problem. Without spending hours fighting the JDAF activity thread
		// code, this band-aid will have to do.
		try
		{
			String lengthTypeName = lengthTypeField.getText();
			instrument.setLengthType(LengthType.valueOf(lengthTypeName));
		}
		catch (Exception e)
		{
		}
		Mouthpiece mouthpiece = getMouthpiece();
		if (mouthpiece == null)
		{
			return null;
		}
		instrument.setMouthpiece(mouthpiece);
		List<Hole> holes = getHoleTableData();
		if (holes == null)
		{
			return null;
		}
		instrument.setHole(holes);
		List<BorePoint> borePoints = getBoreTableData();
		if (borePoints == null)
		{
			return null;
		}
		instrument.setBorePoint(borePoints);
		Termination termination = getTermination();
		if (termination == null)
		{
			return null;
		}
		instrument.setTermination(termination);

		return instrument;
	}

	protected Mouthpiece getMouthpiece()
	{
		Mouthpiece mouthpiece = new Mouthpiece();
		Double value;
		value = (Double) mouthpiecePosition.getValue();
		if (value == null)
		{
			JOptionPane.showMessageDialog(this,
					"Mouthpiece position is required.");
			mouthpiecePosition.requestFocusInWindow();
			return null;
		}
		mouthpiece.setPosition(value);
		if (fippleButton.isSelected())
		{
			Mouthpiece.Fipple fipple = new Mouthpiece.Fipple();
			value = (Double) windowLength.getValue();
			if (value == null || value <= 0.0)
			{
				JOptionPane.showMessageDialog(this,
						"Window length must be positive.");
				windowLength.requestFocusInWindow();
				return null;
			}
			fipple.setWindowLength(value);
			value = (Double) windowWidth.getValue();
			if (value == null || value <= 0.0)
			{
				JOptionPane.showMessageDialog(this,
						"Window width must be positive.");
				windowWidth.requestFocusInWindow();
				return null;
			}
			fipple.setWindowWidth(value);
			value = (Double) windowHeight.getValue();
			if (value != null && value <= 0.0)
			{
				JOptionPane.showMessageDialog(this,
						"Window height, if specified, must be positive.");
				windowHeight.requestFocusInWindow();
				return null;
			}
			fipple.setWindowHeight(value);
			value = (Double) windwayLength.getValue();
			if (value != null && value <= 0.0)
			{
				JOptionPane.showMessageDialog(this,
						"Windway length, if specified, must be positive.");
				windwayLength.requestFocusInWindow();
				return null;
			}
			fipple.setWindwayLength(value);
			value = (Double) windwayHeight.getValue();
			if (value != null && value <= 0.0)
			{
				JOptionPane.showMessageDialog(this,
						"Windway height, if specified, must be positive.");
				windwayHeight.requestFocusInWindow();
				return null;
			}
			fipple.setWindwayHeight(value);
			value = (Double) fippleFactor.getValue();
			if (value != null && value <= 0.0)
			{
				JOptionPane.showMessageDialog(this,
						"Fipple factor, if specified, must be positive.");
				fippleFactor.requestFocusInWindow();
				return null;
			}
			fipple.setFippleFactor(value);
			mouthpiece.setFipple(fipple);
			mouthpiece.setEmbouchureHole(null);
		}
		else if (embouchureHoleButton.isSelected())
		{
			Mouthpiece.EmbouchureHole hole = new Mouthpiece.EmbouchureHole();
			value = (Double) outerDiameter.getValue();
			if (value == null || value <= 0.0)
			{
				JOptionPane.showMessageDialog(this,
						"Outer diameter must be positive.");
				outerDiameter.requestFocusInWindow();
				return null;
			}
			hole.setOuterDiameter(value);
			value = (Double) innerDiameter.getValue();
			if (value == null || value <= 0.0)
			{
				JOptionPane.showMessageDialog(this,
						"Inner diameter must be positive.");
				innerDiameter.requestFocusInWindow();
				return null;
			}
			hole.setInnerDiameter(value);
			value = (Double) embHoleHeight.getValue();
			if (value == null || value <= 0.0)
			{
				JOptionPane.showMessageDialog(this,
						"Embouchure hole height must be positive.");
				embHoleHeight.requestFocusInWindow();
				return null;
			}
			hole.setHeight(value);
			mouthpiece.setEmbouchureHole(hole);
			mouthpiece.setFipple(null);
		}
		else
		{
			// Should not occur.
			return null;
		}
		value = (Double) beta.getValue();
		if (value != null && (value <= 0.0 || value >= 1.0))
		{
			JOptionPane.showMessageDialog(this,
					"Beta, if specified, must be positive and less than 1.0.");
			beta.requestFocusInWindow();
			return null;
		}
		mouthpiece.setBeta(value);
		return mouthpiece;
	}

	protected Termination getTermination()
	{
		Termination termination = new Termination();
		Double value;
		value = (Double) terminationFlange.getValue();
		if (value == null || value <= 0.0)
		{
			JOptionPane.showMessageDialog(this,
					"Termination flange diameter must be positive.");
			terminationFlange.requestFocusInWindow();
			return null;
		}
		termination.setFlangeDiameter(value);
		return termination;
	}

	protected void setNameWidget(int gridx, int gridy, int gridheight)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Name: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		nameField = new JTextField();
		nameField.addFocusListener(this);
		nameField.setPreferredSize(new Dimension(HOLE_TABLE_WIDTH, 20));
		nameField.setMinimumSize(new Dimension(HOLE_TABLE_WIDTH - 30, 20));
		nameField.setMargin(new Insets(2, 4, 2, 4));
		nameField.setText("");
		nameIsPopulated = false;
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(nameField, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

	protected void setDescriptionWidget(int gridx, int gridy, int gridheight)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Description: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		descriptionField = new JTextPane();
		descriptionField.addFocusListener(this);
		descriptionField.setMargin(new Insets(2, 4, 2, 4));
		descriptionField.setBorder(new LineBorder(Color.BLUE));
		descriptionField.setPreferredSize(new Dimension(HOLE_TABLE_WIDTH, 65));
		descriptionField
				.setMinimumSize(new Dimension(HOLE_TABLE_WIDTH - 30, 20));
		descriptionField.setText("");
		gbc.gridy = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(descriptionField, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

	private void setLengthTypeWidget(int gridx, int gridy, int gridheight)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Length Type: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		lengthTypeField = new JTextField();
		lengthTypeField.setEnabled(false);
		lengthTypeField.setMargin(new Insets(2, 4, 2, 4));
		lengthTypeField.setText("");
		nameIsPopulated = false;
		gbc.gridx = 1;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(lengthTypeField, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

	class FormatterFactory extends AbstractFormatterFactory
	{
		@Override
		public AbstractFormatter getFormatter(JFormattedTextField tf)
		{
			if (tf.equals(mouthpiecePosition) || tf.equals(windowLength)
					|| tf.equals(outerDiameter) || tf.equals(windowWidth)
					|| tf.equals(innerDiameter) || tf.equals(embHoleHeight))
			{
				DoubleFormatter requiredDouble = new DoubleFormatter(false);
				requiredDouble.setDecimatPrecision(dimensionalDecimalPrecision);
				return requiredDouble;
			}
			if (tf.equals(windowHeight) || tf.equals(windwayLength)
					|| tf.equals(windwayHeight) || tf.equals(terminationFlange))
			{
				DoubleFormatter optionalDouble = new DoubleFormatter(true);
				optionalDouble.setDecimatPrecision(dimensionalDecimalPrecision);
				return optionalDouble;
			}
			if (tf.equals(beta) || tf.equals(fippleFactor))
			{
				DoubleFormatter optionalDouble = new DoubleFormatter(true);
				optionalDouble
						.setDecimatPrecision(dimensionlessDecimalPrecision);
				return optionalDouble;
			}
			return new DefaultFormatter();
		}
	}

	protected void setMouthpieceWidget(int gridx, int gridy, int gridheight)
	{
		createMouthpieceComponents();
		layoutMouthpieceComponents(gridx, gridy, gridheight);
	}

	protected void createMouthpieceComponents()
	{
		mouthpiecePosition = new JFormattedTextField(formatterFactory);
		mouthpiecePosition.setColumns(5);
		mouthpiecePosition.setValue(0.0);

		beta = new JFormattedTextField(formatterFactory);
		beta.setColumns(5);

		fippleButton = new JRadioButton("Fipple Mouthpiece");
		embouchureHoleButton = new JRadioButton("Embouchure Hole");
		fippleButton.setSelected(true);
		mouthpieceTypeGroup = new ButtonGroup();
		mouthpieceTypeGroup.add(fippleButton);
		mouthpieceTypeGroup.add(embouchureHoleButton);
		fippleButton.addActionListener(this);
		embouchureHoleButton.addActionListener(this);

		windowLength = new JFormattedTextField(formatterFactory);
		windowLength.setColumns(5);

		outerDiameter = new JFormattedTextField(formatterFactory);
		outerDiameter.setColumns(5);

		windowWidth = new JFormattedTextField(formatterFactory);
		windowWidth.setColumns(5);

		innerDiameter = new JFormattedTextField(formatterFactory);
		innerDiameter.setColumns(5);

		windowHeight = new JFormattedTextField(formatterFactory);
		windowHeight.setColumns(5);

		embHoleHeight = new JFormattedTextField(formatterFactory);
		embHoleHeight.setColumns(5);

		windwayLength = new JFormattedTextField(formatterFactory);
		windwayLength.setColumns(5);

		windwayHeight = new JFormattedTextField(formatterFactory);
		windwayHeight.setColumns(5);

		fippleFactor = new JFormattedTextField(formatterFactory);
		fippleFactor.setColumns(5);

		outerDiameter.setEnabled(false);
		innerDiameter.setEnabled(false);

		mouthpiecePosition.addFocusListener(this);
		embouchureHoleButton.addFocusListener(this);
		fippleButton.addFocusListener(this);
		innerDiameter.addFocusListener(this);
		outerDiameter.addFocusListener(this);
		embHoleHeight.addFocusListener(this);
		windowLength.addFocusListener(this);
		windowWidth.addFocusListener(this);
		windowHeight.addFocusListener(this);
		windwayLength.addFocusListener(this);
		windwayHeight.addFocusListener(this);
		fippleFactor.addFocusListener(this);
		beta.addFocusListener(this);
	}

	protected void layoutMouthpieceComponents(int gridx, int gridy,
			int gridheight)
	{
		GridBagConstraints gbc = new GridBagConstraints();
		JLabel label;
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridy = 0;

		label = new JLabel("Mouthpiece Position: ");
		gbc.gridx = 0;
		gbc.insets = new Insets(10, 0, 0, 0);
		panel.add(label, gbc);
		gbc.gridx = 1;
		gbc.insets = new Insets(10, 0, 0, 10);
		panel.add(mouthpiecePosition, gbc);
		gbc.insets = new Insets(10, 0, 0, 0);

		label = new JLabel("Beta Factor: ");
		gbc.gridx = 2;
		panel.add(label, gbc);
		gbc.gridx = 3;
		panel.add(beta, gbc);
		gbc.insets = new Insets(0, 0, 0, 0);

		++gbc.gridy;
		gbc.gridx = 0;
		gbc.gridwidth = 2;
		panel.add(fippleButton, gbc);
		gbc.gridx = 2;
		panel.add(embouchureHoleButton, gbc);

		gbc.gridwidth = 1;
		++gbc.gridy;
		label = new JLabel("Window Length: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(windowLength, gbc);

		label = new JLabel("Outer Diameter: ");
		gbc.gridx = 2;
		gbc.gridwidth = 1;
		panel.add(label, gbc);
		gbc.gridx = 3;
		panel.add(outerDiameter, gbc);

		++gbc.gridy;
		label = new JLabel("Window Width: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(windowWidth, gbc);

		label = new JLabel("Inner Diameter: ");
		gbc.gridx = 2;
		gbc.gridwidth = 1;
		panel.add(label, gbc);
		gbc.gridx = 3;
		panel.add(innerDiameter, gbc);

		++gbc.gridy;
		label = new JLabel("Window Height: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(windowHeight, gbc);

		label = new JLabel("Emb Hole Height: ");
		gbc.gridx = 2;
		panel.add(label, gbc);
		gbc.gridx = 3;
		panel.add(embHoleHeight, gbc);

		++gbc.gridy;
		label = new JLabel("Windway Length: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(windwayLength, gbc);

		++gbc.gridy;
		label = new JLabel("Windway Height: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(windwayHeight, gbc);

		++gbc.gridy;
		label = new JLabel("Fipple Factor: ");
		gbc.gridx = 0;
		panel.add(label, gbc);
		gbc.gridx = 1;
		panel.add(fippleFactor, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

	private void setTerminationWidget(int gridx, int gridy, int gridheight)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		JLabel label = new JLabel("Termination Flange Diameter: ");
		panel.add(label);

		terminationFlange = new JFormattedTextField(formatterFactory);
		terminationFlange.setColumns(5);
		terminationFlange.addFocusListener(this);
		panel.add(terminationFlange);

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 10, 10);
		add(panel, gbc);
	}

	class NumberFormatCellRenderer extends NumberFormatTableCellRenderer
	{
		@Override
		public int getDecimalPrecision(JTable table, int row, int col)
		{
			return dimensionalDecimalPrecision;
		}
	}

	protected void setHoleTableWidget(int gridx, int gridy, int gridheight)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Holes: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		DefaultTableModel model = new DefaultTableModel()
		{
			@Override
			public Class<?> getColumnClass(int columnIndex)
			{
				if (columnIndex == 0)
				{
					return String.class;
				}
				else
				{
					return Double.class;
				}
			}
		};
		holeList = new JideTable(model);
		resetTableData(holeList, 0, 5);
		holesArePopulated = true; // No holes is acceptable.
		holeList.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(holeList);
		scrollPane.setBorder(new LineBorder(Color.BLACK));
		scrollPane.setPreferredSize(new Dimension(HOLE_TABLE_WIDTH, 160));
		scrollPane.setMinimumSize(new Dimension(HOLE_TABLE_WIDTH, 120));
		gbc.gridy = 1;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(scrollPane, gbc);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3, 1));
		JButton button;

		button = new JButton("Add row above selection");
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				insertRowAboveSelection(holeList);
			}

		});
		buttonPanel.add(button);

		button = new JButton("Add row below selection");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				insertRowBelowSelection(holeList);
			}

		});
		buttonPanel.add(button);

		button = new JButton("Delete selected rows");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				deleteSelectedRows(holeList);
			}

		});
		buttonPanel.add(button);

		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(buttonPanel, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 0, 0);
		add(panel, gbc);
		model.addTableModelListener(this);
	}

	protected void setBoreTableWidget(int gridx, int gridy, int gridheight)
	{
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());

		JLabel label = new JLabel("Bore Points: ");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(label, gbc);

		DefaultTableModel model = new NumericTableModel(Double.class,
				Double.class);
		boreList = new JideTable(model);
		resetTableData(boreList, 2, 2);
		boreIsPopulated = false; // Bore points not entered.
		boreList.setAutoscrolls(true);
		JScrollPane scrollPane = new JScrollPane(boreList);
		scrollPane.setBorder(new LineBorder(Color.BLACK));
		scrollPane.setPreferredSize(new Dimension(BORE_TABLE_WIDTH, 140));
		scrollPane.setMinimumSize(new Dimension(BORE_TABLE_WIDTH, 100));
		gbc.gridy = 1;
		gbc.weighty = 1.0;
		gbc.insets = new Insets(0, 15, 0, 0);
		panel.add(scrollPane, gbc);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(3, 1));
		JButton button;

		button = new JButton("Add row above selection");
		button.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				insertRowAboveSelection(boreList);
			}

		});
		buttonPanel.add(button);

		button = new JButton("Add row below selection");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				insertRowBelowSelection(boreList);
			}

		});
		buttonPanel.add(button);

		button = new JButton("Delete selected rows");
		button.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				deleteSelectedRows(boreList);
			}

		});
		buttonPanel.add(button);

		gbc.anchor = GridBagConstraints.NORTHEAST;
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(buttonPanel, gbc);

		gbc.anchor = GridBagConstraints.NORTHWEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.gridheight = gridheight;
		gbc.insets = new Insets(0, 0, 0, 0);
		add(panel, gbc);
		model.addTableModelListener(this);
	}

	public void setName(String name)
	{
		String oldValue = nameField.getText();
		nameField.setText(name);
		isNamePopulated();
		if (!oldValue.equals(name))
		{
			fireDataStateChanged();
		}
	}

	public void setDescription(String description)
	{
		String oldValue = descriptionField.getText();
		descriptionField.setText(description);
		if (!oldValue.equals(description))
		{
			fireDataStateChanged();
		}
	}

	protected void enableMouthpieceFields()
	{
		windowLength.setEnabled(fippleButton.isSelected());
		windowWidth.setEnabled(fippleButton.isSelected());
		windowHeight.setEnabled(fippleButton.isSelected());
		windwayLength.setEnabled(fippleButton.isSelected());
		windwayHeight.setEnabled(fippleButton.isSelected());
		fippleFactor.setEnabled(fippleButton.isSelected());
		outerDiameter.setEnabled(embouchureHoleButton.isSelected());
		innerDiameter.setEnabled(embouchureHoleButton.isSelected());
		embHoleHeight.setEnabled(embouchureHoleButton.isSelected());
	}

	protected void resetTableData(JideTable table, int numRows, int numCols)
	{
		stopTableEditing(table);
		DefaultTableModel model = (DefaultTableModel) table.getModel();
		TableCellRenderer renderer = new NumberFormatCellRenderer();
		int firstDoubleCol;
		int lastDoubleCol;
		if (numCols == 2)
		{
			model.setDataVector(new Object[0][2], new String[] { "Position",
					"Diameter" });
			firstDoubleCol = 0;
			lastDoubleCol = 2;
		}
		else
		{
			model.setDataVector(new Object[0][4], new String[] { "Name",
					"Position", "Spacing", "Diameter", "Height" });
			firstDoubleCol = 1;
			lastDoubleCol = 5;
		}
		for (int i = firstDoubleCol; i < lastDoubleCol; i++)
		{
			TableColumn col = table.getColumn(model.getColumnName(i));
			col.setCellRenderer(renderer);
		}
		table.setFillsGrids(false);
		table.setAutoResizeMode(JideTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setFillsRight(true);
		table.setCellSelectionEnabled(true);
		if (numRows > 0)
		{
			for (int i = 0; i < numRows; i++)
			{
				model.addRow(emptyRow(model.getColumnCount()));
			}
		}
	}

	protected List<Hole> getHoleTableData()
	{
		stopTableEditing(holeList);
		DefaultTableModel model = (DefaultTableModel) holeList.getModel();
		ArrayList<Hole> data = new ArrayList<Hole>();

		for (int i = 0; i < model.getRowCount(); i++)
		{
			String holeName = (String) model.getValueAt(i, 0);
			Double position = (Double) model.getValueAt(i, 1);
			Double diameter = (Double) model.getValueAt(i, 3);
			Double height = (Double) model.getValueAt(i, 4);
			if (position == null)
			{
				JOptionPane.showMessageDialog(this, "Missing hole position.");
				holeList.requestFocusInWindow();
				holeList.editCellAt(i, 0);
				holeList.getEditorComponent().requestFocusInWindow();
				return null;
			}
			if (diameter == null || diameter <= 0.0)
			{
				JOptionPane.showMessageDialog(this,
						"All hole diameters must be positive.");
				holeList.requestFocusInWindow();
				holeList.editCellAt(i, 1);
				holeList.getEditorComponent().requestFocusInWindow();
				return null;
			}
			if (height == null || height <= 0.0)
			{
				JOptionPane.showMessageDialog(this,
						"All hole heights must be positive.");
				holeList.requestFocusInWindow();
				holeList.editCellAt(i, 2);
				holeList.getEditorComponent().requestFocusInWindow();
				return null;
			}
			Hole hole = new Hole(position, diameter, height);
			hole.setName(holeName);
			data.add(hole);
		}
		return data;
	}

	protected List<BorePoint> getBoreTableData()
	{
		stopTableEditing(boreList);
		DefaultTableModel model = (DefaultTableModel) boreList.getModel();
		ArrayList<BorePoint> data = new ArrayList<BorePoint>();
		if (model.getRowCount() < 2)
		{
			JOptionPane.showMessageDialog(this,
					"Must specify at least two bore points.");
			boreList.requestFocusInWindow();
			return null;
		}

		for (int i = 0; i < model.getRowCount(); i++)
		{
			Double position = (Double) model.getValueAt(i, 0);
			Double diameter = (Double) model.getValueAt(i, 1);
			if (position == null)
			{
				JOptionPane.showMessageDialog(this,
						"Missing bore point position.");
				boreList.editCellAt(i, 0);
				boreList.requestFocusInWindow();
				boreList.getEditorComponent().requestFocusInWindow();
				return null;
			}
			if (diameter == null || diameter <= 0.0)
			{
				JOptionPane.showMessageDialog(this,
						"All bore diameters must be positive.");
				boreList.requestFocusInWindow();
				boreList.editCellAt(i, 1);
				boreList.getEditorComponent().requestFocusInWindow();
				return null;
			}
			data.add(new BorePoint(position, diameter));
		}
		return data;
	}

	@Override
	public void focusGained(FocusEvent event)
	{
		if (event.getSource() instanceof JTextComponent)
		{
			JTextComponent field = (JTextComponent) event.getSource();
			priorValue = new String(field.getText());
		}
	}

	@Override
	public void focusLost(FocusEvent event)
	{
		boolean isDataChanged = false;

		if (event.getSource() instanceof JTextComponent)
		{
			JTextComponent field = (JTextComponent) event.getSource();
			if (!priorValue.equals(field.getText()))
			{
				isDataChanged = true;
			}
			if (event.getSource().equals(nameField))
			{
				isNamePopulated();
			}
			else if (event.getSource().equals(terminationFlange))
			{
				isTerminationPopulated();
			}
			else
			{
				isMouthpiecePopulated();
			}
		}
		if (isDataChanged)
		{
			fireDataStateChanged();
		}
	}

	@Override
	public void actionPerformed(ActionEvent event)
	{
		if (event.getSource().equals(fippleButton)
				|| event.getSource().equals(embouchureHoleButton))
		{
			enableMouthpieceFields();
			isMouthpiecePopulated();
		}
		fireDataStateChanged();
	}

	@Override
	public void tableChanged(TableModelEvent event)
	{
		holesArePopulated = isTablePopulated(holeList, 0);
		boreIsPopulated = isTablePopulated(boreList, 2);
		updateHoleSpacing(event.getSource());
		fireDataStateChanged();
	}

	private void updateHoleSpacing(Object source)
	{
		if (source instanceof DefaultTableModel)
		{
			DefaultTableModel model = (DefaultTableModel) source;
			if (model.getColumnCount() == 5)
			{
				model.removeTableModelListener(this);
				boolean firstHole = true;
				Double priorHolePosition = 0.;
				int rowCount = model.getRowCount();
				for (int row = 0; row < rowCount; row++)
				{
					Double spacing = null;
					Double holePosition = (Double) model.getValueAt(row, 1);
					// Allow for a newly created row without a hole position.
					if (holePosition == null)
					{
						continue;
					}
					if (!firstHole)
					{
						spacing = holePosition - priorHolePosition;
						model.setValueAt(spacing, row, 2);
					}
					else
					{
						firstHole = false;
					}
					priorHolePosition = holePosition;
				}
				model.addTableModelListener(this);
			}
		}
	}

	public void addDataPopulatedListener(DataPopulatedListener listener)
	{
		if (populatedListeners == null)
		{
			populatedListeners = new ArrayList<DataPopulatedListener>();
		}
		populatedListeners.add(listener);
	}

	protected void fireDataStateChanged()
	{
		if (populatedListeners == null)
		{
			return;
		}

		List<DataPopulatedEvent> events = new ArrayList<DataPopulatedEvent>();
		DataPopulatedEvent event = new DataPopulatedEvent(this, SAVE_EVENT_ID,
				nameIsPopulated && mouthpieceIsPopulated && holesArePopulated
						&& boreIsPopulated && terminationIsPopulated);
		events.add(event);
		for (DataPopulatedEvent thisEvent : events)
		{
			for (DataPopulatedListener listener : populatedListeners)
			{
				listener.dataStateChanged(thisEvent);
			}
		}
	}
}
