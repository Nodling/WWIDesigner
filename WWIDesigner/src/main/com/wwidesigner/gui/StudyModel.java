/**
 * 
 */
package com.wwidesigner.gui;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jidesoft.app.framework.file.FileDataModel;
import com.wwidesigner.geometry.Instrument;
import com.wwidesigner.geometry.bind.GeometryBindFactory;
import com.wwidesigner.modelling.GordonCalculator;
import com.wwidesigner.modelling.InstrumentCalculator;
import com.wwidesigner.modelling.NAFCalculator;
import com.wwidesigner.modelling.SimpleInstrumentTuner;
import com.wwidesigner.optimization.FippleFactorOptimizer;
import com.wwidesigner.optimization.HoleGroupSpacingOptimizer;
import com.wwidesigner.optimization.HolePosAndDiamImpedanceOptimizer;
import com.wwidesigner.optimization.HoleSizeOptimizer;
import com.wwidesigner.optimization.InstrumentOptimizer;
import com.wwidesigner.optimization.SingleTaperHoleGroupingOptimizer;
import com.wwidesigner.optimization.run.BaseOptimizationRunner;
import com.wwidesigner.optimization.run.HoleGroupSpacingOptimizationRunnner;
import com.wwidesigner.util.BindFactory;
import com.wwidesigner.util.Constants.TemperatureType;
import com.wwidesigner.util.PhysicalParameters;

/**
 * @author kort
 * 
 */
public class StudyModel
{
	public static final String INSTRUMENT_CATEGORY_ID = "Instrument";
	public static final String TUNING_CATEGORY_ID = "Tuning";
	public static final String OPTIMIZER_CATEGORY_ID = "Optimizer";
	public static final String CONSTRAINT_CATEGORY_ID = "Constraint set";
	public static final String CALCULATOR_CATEGORY_ID = "Instrument calculator";

	public static final String GORDON_CALC_SUB_CATEGORY_ID = "Gordon calculator";
	public static final String NAF_CALC_SUB_CATEGORY_ID = "NAF calculator";

	public static final String FIPPLE_OPT_SUB_CATEGORY_ID = "Fipple-factor Optimizer";
	public static final String HOLESIZE_OPT_SUB_CATEGORY_ID = "Hole-size Optimizer";
	public static final String NO_GROUP_OPT_SUB_CATEGORY_ID = "No-hole-grouping Optimizer";
	public static final String GROUP_OPT_SUB_CATEGORY_ID = "Hole-grouping Optimizer";
	public static final String TAPER_GROUP_OPT_SUB_CATEGORY_ID = "Taper, hole-grouping Optimizer";

	public static final String HOLE_0_CONS_SUB_CATEGORY_ID = "0 holes";
	public static final String HOLE_6_CONS_SUB_CATEGORY_ID = "6 holes";
	public static final String HOLE_7_CONS_SUB_CATEGORY_ID = "7 holes";

	private List<Category> categories;

	public StudyModel()
	{
		setCategories();
	}

	protected void setCategories()
	{
		categories = new ArrayList<Category>();
		categories.add(new Category(INSTRUMENT_CATEGORY_ID));
		categories.add(new Category(TUNING_CATEGORY_ID));
		Category calculators = new Category(CALCULATOR_CATEGORY_ID);
		calculators.addSub(GORDON_CALC_SUB_CATEGORY_ID, null);
		calculators.addSub(NAF_CALC_SUB_CATEGORY_ID, null);
		categories.add(calculators);
		Category optimizers = new Category(OPTIMIZER_CATEGORY_ID);
		optimizers.addSub(FIPPLE_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(NO_GROUP_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(HOLESIZE_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(GROUP_OPT_SUB_CATEGORY_ID, null);
		optimizers.addSub(TAPER_GROUP_OPT_SUB_CATEGORY_ID, null);
		categories.add(optimizers);
		Category constraints = new Category(CONSTRAINT_CATEGORY_ID);
		constraints.addSub(HOLE_0_CONS_SUB_CATEGORY_ID, null);
		constraints.addSub(HOLE_6_CONS_SUB_CATEGORY_ID, null);
		constraints.addSub(HOLE_7_CONS_SUB_CATEGORY_ID, null);
		categories.add(constraints);
	}

	public List<Category> getCategories()
	{
		return categories;
	}

	public Category getCategory(String name)
	{
		Category category = null;

		for (Category thisCategory : categories)
		{
			if (thisCategory.toString().equals(name))
			{
				category = thisCategory;
				break;
			}
		}

		return category;
	}

	public void setCategorySelection(Category category, String subCategoryName)
	{
		for (Category thisCategory : categories)
		{
			if (thisCategory.name.equals(category.name))
			{
				thisCategory.setSelectedSub(subCategoryName);
			}
		}
	}

	public static class Category
	{
		private String name;
		private Map<String, Object> subs;
		private String selectedSub;

		public Category(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			return name;
		}

		public void addSub(String name, Object sub)
		{
			if (subs == null)
			{
				subs = new TreeMap<String, Object>();
			}
			subs.put(name, sub);
		}

		public void removeSub(String name)
		{
			if (name.equals(selectedSub))
			{
				selectedSub = null;
			}
			subs.remove(name);
		}

		public Map<String, Object> getSubs()
		{
			return subs == null ? new TreeMap<String, Object>() : subs;
		}

		public void setSelectedSub(String key)
		{
			selectedSub = key;
		}

		public String getSelectedSub()
		{
			return selectedSub;
		}

		public Object getSelectedSubValue()
		{
			return subs.get(selectedSub);
		}

		public void replaceSub(String newName, FileDataModel source)
		{
			// Find sub by matching data
			String data = (String) source.getData();
			String oldName = null;
			boolean isSelected = false;
			for (Map.Entry<String, Object> entry : subs.entrySet())
			{
				FileDataModel model = (FileDataModel) entry.getValue();
				String modelData = (String) model.getData();
				if (data.equals(modelData))
				{
					oldName = entry.getKey();
					break;
				}
			}
			if (oldName != null)
			{
				if (oldName.equals(selectedSub))
				{
					isSelected = true;
				}
				removeSub(oldName);
			}
			addSub(newName, source);
			if (isSelected)
			{
				setSelectedSub(newName);
			}
		}
	}

	public boolean canTune()
	{
		Category tuningCategory = getCategory(TUNING_CATEGORY_ID);
		String tuningSelected = tuningCategory.getSelectedSub();

		Category instrumentCategory = getCategory(INSTRUMENT_CATEGORY_ID);
		String instrumentSelected = instrumentCategory.getSelectedSub();

		Category calculatorCategory = getCategory(CALCULATOR_CATEGORY_ID);
		String calculatorSelected = calculatorCategory.getSelectedSub();

		return tuningSelected != null && instrumentSelected != null
				&& calculatorSelected != null;
	}

	public boolean canOptimize()
	{
		Category category = getCategory(OPTIMIZER_CATEGORY_ID);
		String optimizerSelected = category.getSelectedSub();

		category = getCategory(CONSTRAINT_CATEGORY_ID);
		String constraintsSelected = category.getSelectedSub();

		return optimizerSelected != null && constraintsSelected != null
				&& canTune();
	}

	public void calculateTuning(String title) throws Exception
	{
		SimpleInstrumentTuner tuner = new SimpleInstrumentTuner();

		Category category = this.getCategory(INSTRUMENT_CATEGORY_ID);
		String instrumentName = category.getSelectedSub();
		FileDataModel model = (FileDataModel) category.getSelectedSubValue();
		model.getApplication().getDataView(model).updateModel(model);
		tuner.setInstrument((String) model.getData());

		category = getCategory(TUNING_CATEGORY_ID);
		String tuningName = category.getSelectedSub();
		model = (FileDataModel) category.getSelectedSubValue();
		model.getApplication().getDataView(model).updateModel(model);
		tuner.setTuning((String) model.getData());

		tuner.setCalculator(getCalculator());

		tuner.setParams(new PhysicalParameters(72.0, TemperatureType.F));

		tuner.showTuning(title + ": " + instrumentName + "/" + tuningName,
				false);
	}

	public String optimizeInstrument() throws Exception
	{
		BaseOptimizationRunner runner = setOptimizationRunner();
		runner.setCalculator(getCalculator());
		setConstraints(runner);

		String xmlString = getSelectedXmlString(INSTRUMENT_CATEGORY_ID);
		runner.setInputInstrumentXML(xmlString, false);

		xmlString = getSelectedXmlString(TUNING_CATEGORY_ID);
		runner.setInputTuningXML(xmlString, false);

		runner.setParams(new PhysicalParameters(72.0, TemperatureType.F));

		Instrument instrument = runner.doInstrumentOptimization(null);
		xmlString = marshal(instrument);

		return xmlString;
	}

	private InstrumentCalculator getCalculator()
	{
		Category calculatorCategory = getCategory(CALCULATOR_CATEGORY_ID);
		String calculatorSelected = calculatorCategory.getSelectedSub();
		InstrumentCalculator calculator = null;

		switch (calculatorSelected)
		{
			case GORDON_CALC_SUB_CATEGORY_ID:
				calculator = new GordonCalculator();
				break;
			case NAF_CALC_SUB_CATEGORY_ID:
				calculator = new NAFCalculator();
				break;
		}

		return calculator;
	}

	private String marshal(Instrument instrument) throws Exception
	{
		BindFactory binder = GeometryBindFactory.getInstance();
		StringWriter writer = new StringWriter();
		binder.marshalToXml(instrument, writer);

		return writer.toString();
	}

	private String getSelectedXmlString(String categoryName) throws Exception
	{
		String xmlString = null;

		Category category = getCategory(categoryName);
		FileDataModel model = (FileDataModel) category.getSelectedSubValue();
		model.getApplication().getDataView(model).updateModel(model);
		xmlString = (String) model.getData();

		return xmlString;
	}

	protected void setConstraints(BaseOptimizationRunner runner)
	{
		Category constraintCategory = getCategory(CONSTRAINT_CATEGORY_ID);
		String constraint = constraintCategory.getSelectedSub();
		Category optimizerCategory = getCategory(OPTIMIZER_CATEGORY_ID);
		String optimizer = optimizerCategory.getSelectedSub();
		switch (optimizer)
		{
			case FIPPLE_OPT_SUB_CATEGORY_ID:
				runner.setOptimizerClass(FippleFactorOptimizer.class);
				runner.setLowerBound(new double[] { 0.2 });
				runner.setUpperBound(new double[] { 1.5 });
				runner.setOptimizerType(InstrumentOptimizer.OptimizerType.CMAESOptimizer);
				runner.setNumberOfInterpolationPoints(2);
				break;
			case HOLESIZE_OPT_SUB_CATEGORY_ID:
				runner.setOptimizerClass(HoleSizeOptimizer.class);
				switch (constraint)
				{
					case HOLE_0_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[0]);
						runner.setUpperBound(new double[0]);
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.CMAESOptimizer);
						runner.setNumberOfInterpolationPoints(1);
						break;
					case HOLE_6_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[] { 0.1, 0.15, 0.15,
								0.15, 0.15, 0.15 });
						runner.setUpperBound(new double[] { 0.5, 0.5, 0.5, 0.5,
								0.5, 0.6 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(12);
						break;
					case HOLE_7_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[] { 0.1, 0.1, 0.1, 0.1,
								0.1, 0.05, 0.05 });
						runner.setUpperBound(new double[] { 0.7, 0.7, 0.7, 0.7,
								0.7, 0.4, 0.4 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(14);
						break;
				}
				break;
			case NO_GROUP_OPT_SUB_CATEGORY_ID:
				runner.setOptimizerClass(HolePosAndDiamImpedanceOptimizer.class);
				switch (constraint)
				{
					case HOLE_0_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[] { 0.25 });
						runner.setUpperBound(new double[] { 0.4 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.CMAESOptimizer);
						runner.setNumberOfInterpolationPoints(2);
						break;
					case HOLE_6_CONS_SUB_CATEGORY_ID:
						// runner.setLowerBound(new double[] { 0.28, 0.01, 0.01,
						// 0.01, 0.01, 0.01, 0.05, 0.1, 0.15, 0.15, 0.15,
						// 0.15, 0.15 });
						// runner.setUpperBound(new double[] { 0.5, 0.03, 0.03,
						// 0.035, 0.035, 0.035, 0.15, 0.5, 0.5, 0.5, 0.5,
						// 0.5, 0.6 });
						runner.setLowerBound(new double[] { 0.25, 0.01, 0.01,
								0.01, 0.01, 0.01, 0.01, 0.1, 0.15, 0.15, 0.15,
								0.15, 0.15 });
						runner.setUpperBound(new double[] { 0.6, 0.05, 0.05,
								0.07, 0.05, 0.05, 0.3, 0.5, 0.5, 0.5, 0.5, 0.5,
								0.6 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(26);
						break;
					case HOLE_7_CONS_SUB_CATEGORY_ID:
						runner.setLowerBound(new double[] { 0.2, 0.012, 0.012,
								0.012, 0.012, 0.012, 0.0005, 0.05, 0.1, 0.1,
								0.1, 0.1, 0.1, 0.05, 0.05 });
						runner.setUpperBound(new double[] { 0.5, 0.05, 0.05,
								0.1, 0.05, 0.05, 0.003, 0.2, 0.7, 0.7, 0.7,
								0.7, 0.7, 0.4, 0.4 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(30);
						break;
				}
				break;
			case GROUP_OPT_SUB_CATEGORY_ID:
				switch (constraint)
				{
					case HOLE_0_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(HolePosAndDiamImpedanceOptimizer.class);
						runner.setLowerBound(new double[] { 0.25 });
						runner.setUpperBound(new double[] { 0.4 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.CMAESOptimizer);
						runner.setNumberOfInterpolationPoints(2);
						break;
					case HOLE_6_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(HoleGroupSpacingOptimizer.class);
						// runner.setLowerBound(new double[] { 0.2, 0.012,
						// 0.012,
						// 0.012, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 });
						// runner.setUpperBound(new double[] { 0.5, 0.05, 0.1,
						// 0.05, 0.2, 0.7, 0.7, 0.7, 0.7, 0.7, 0.7 });
						runner.setLowerBound(new double[] { 0.2, 0.012, 0.012,
								0.012, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1 });
						runner.setUpperBound(new double[] { 0.8, 0.05, 0.1,
								0.05, 0.2, 0.7, 0.7, 0.7, 0.7, 0.7, 0.7 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(22);
						((HoleGroupSpacingOptimizationRunnner) runner)
								.setHoleGroups(new int[][] { { 0, 1, 2 },
										{ 3, 4, 5 } });
						break;
					case HOLE_7_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(HoleGroupSpacingOptimizer.class);
						runner.setLowerBound(new double[] { 0.2, 0.012, 0.012,
								0.012, 0.0005, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1,
								0.05, 0.05 });
						runner.setUpperBound(new double[] { 0.5, 0.05, 0.05,
								0.1, 0.003, 0.2, 0.7, 0.7, 0.7, 0.7, 0.7, 0.4,
								0.4 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(26);
						((HoleGroupSpacingOptimizationRunnner) runner)
								.setHoleGroups(new int[][] { { 0, 1, 2 },
										{ 3, 4, 5 }, { 6 } });
						break;
				}
				break;
			case TAPER_GROUP_OPT_SUB_CATEGORY_ID:
				switch (constraint)
				{
					case HOLE_0_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(HolePosAndDiamImpedanceOptimizer.class);
						runner.setLowerBound(new double[] { 0.25 });
						runner.setUpperBound(new double[] { 0.4 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.CMAESOptimizer);
						runner.setNumberOfInterpolationPoints(2);
						break;
					case HOLE_6_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(SingleTaperHoleGroupingOptimizer.class);
						// runner.setLowerBound(new double[] { 0.2, 0.012,
						// 0.012,
						// 0.012, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.5,
						// 0.0, 0.0 });
						// runner.setUpperBound(new double[] { 0.5, 0.032, 0.1,
						// 0.032, 0.2, 0.7, 0.7, 0.7, 0.7, 0.7, 0.7, 2.0,
						// 0.5, 0.5 });
						runner.setLowerBound(new double[] { 0.2, 0.01, 0.01,
								0.01, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.5,
								0.0, 0.0 });
						runner.setUpperBound(new double[] { 0.8, 0.032, 0.1,
								0.032, 0.3, 0.7, 0.7, 0.7, 0.7, 0.7, 0.7, 2.0,
								0.8, 0.8 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(28);
						((HoleGroupSpacingOptimizationRunnner) runner)
								.setHoleGroups(new int[][] { { 0, 1, 2 },
										{ 3, 4, 5 } });
						break;
					case HOLE_7_CONS_SUB_CATEGORY_ID:
						runner.setOptimizerClass(SingleTaperHoleGroupingOptimizer.class);
						runner.setLowerBound(new double[] { 0.2, 0.012, 0.012,
								0.012, 0.0005, 0.05, 0.1, 0.1, 0.1, 0.1, 0.1,
								0.05, 0.05, 0.5, 0.0, 0.0 });
						runner.setUpperBound(new double[] { 0.5, 0.032, 0.05,
								0.032, 0.003, 0.2, 0.7, 0.7, 0.7, 0.7, 0.7,
								0.4, 0.4, 2.0, 0.5, 0.5 });
						runner.setOptimizerType(InstrumentOptimizer.OptimizerType.BOBYQAOptimizer);
						runner.setNumberOfInterpolationPoints(32);
						((HoleGroupSpacingOptimizationRunnner) runner)
								.setHoleGroups(new int[][] { { 0, 1, 2 },
										{ 3, 4, 5 }, { 6 } });
						break;
				}
				break;
		}
	}

	protected BaseOptimizationRunner setOptimizationRunner()
	{
		Category optimizerCategory = getCategory(OPTIMIZER_CATEGORY_ID);
		Category constraintCategory = getCategory(CONSTRAINT_CATEGORY_ID);

		if ((GROUP_OPT_SUB_CATEGORY_ID.equals(optimizerCategory
				.getSelectedSub()) || TAPER_GROUP_OPT_SUB_CATEGORY_ID
				.equals(optimizerCategory.getSelectedSub()))
				&& !HOLE_0_CONS_SUB_CATEGORY_ID.equals(constraintCategory
						.getSelectedSub()))
		{
			return new HoleGroupSpacingOptimizationRunnner();
		}

		return new BaseOptimizationRunner();
	}
}
