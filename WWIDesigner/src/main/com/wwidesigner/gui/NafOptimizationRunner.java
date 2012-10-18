package com.wwidesigner.gui;

import java.awt.event.ActionEvent;

import javax.swing.Action;
import javax.swing.JMenu;

import com.jidesoft.app.framework.DataModelAdapter;
import com.jidesoft.app.framework.DataModelEvent;
import com.jidesoft.app.framework.DataView;
import com.jidesoft.app.framework.SecondaryBasicDataModel;
import com.jidesoft.app.framework.event.EventManager;
import com.jidesoft.app.framework.event.EventSubscriber;
import com.jidesoft.app.framework.event.SubscriberEvent;
import com.jidesoft.app.framework.file.TextFileFormat;
import com.jidesoft.app.framework.gui.ActionKeys;
import com.jidesoft.app.framework.gui.ApplicationMenuBarsUI;
import com.jidesoft.app.framework.gui.ApplicationWindowsUI;
import com.jidesoft.app.framework.gui.GUIApplicationAction;
import com.jidesoft.app.framework.gui.MenuBarCustomizer;
import com.jidesoft.app.framework.gui.MenuConstants;
import com.jidesoft.app.framework.gui.actions.ComponentAction;
import com.jidesoft.app.framework.gui.feature.AutoInstallActionsFeature;
import com.jidesoft.app.framework.gui.filebased.FileBasedApplication;
import com.jidesoft.app.framework.gui.framed.DockableConfiguration;
import com.jidesoft.app.framework.gui.framed.DockingApplicationFeature;
import com.jidesoft.app.framework.gui.framed.ToggleFrameAction;
import com.jidesoft.docking.DockContext;

/**
 * DockedTextEditor2.java
 * <p/>
 * This example is similar to DockedTextEditor, but uses the
 * DockingApplicationFeature. No docking content it provided, this is just to
 * show how to facilitate Dockable DataViews. Notice that the DataModels used by
 * the docking are of secondary status. This is required.
 */
public class NafOptimizationRunner extends FileBasedApplication implements
		EventSubscriber
{
	static final String FILE_OPENED_EVENT_ID = "FileOpened";
	static final String FILE_CLOSED_EVENT_ID = "FileClosed";
	static final String FILE_SAVED_EVENT_ID = "FileSaved";
	static final String TUNING_ACTIVE_EVENT_ID = "TuningActive";
	static final String OPTIMIZATION_ACTIVE_EVENT_ID = "OptimizationActive";

	static final String CONSOLE_ACTION_ID = "Console";
	static final String STUDY_ACTION_ID = "Study";
	static final String CALCULATE_TUNING_ACTION_ID = "Calculate tuning";
	static final String OPTIMIZE_INSTRUMENT_ACTION_ID = "Optimize instrument";

	public static void main(String[] args)
	{
		com.jidesoft.utils.Lm.verifyLicense("Edward Kort", "WWIDesigner",
				"DfuwPRAUR5KQYgePf:CH0LWIp63V8cs2");
		new NafOptimizationRunner().run(args);
	}

	public NafOptimizationRunner()
	{
		super("NAF Optimization Runner", TDI_APPLICATION_STYLE);

		// Set behavioe
		getApplicationUIManager().setUseJideDockingFramework(true);
		getApplicationUIManager().setUseJideActionFramework(true);
		addApplicationFeature(new AutoInstallActionsFeature());
		setExitApplicationOnLastDataView(false);
		setNewDataOnRun(false);

		// window size
		ApplicationWindowsUI windowsUI = getApplicationUIManager()
				.getWindowsUI();
		windowsUI.setPreferredWindowSize(windowsUI
				.getPreferredMaximumWindowSize());

		getApplicationUIManager().setUseJideDocumentPane(true);

		// Add my UI customizations
		addFileMapping(new TextFileFormat("xml", "XML"), CodeEditorView.class);
		addDockedViews();
		addEvents();
		addWindowMenuToggles();
		addToolMenu();

		// The stock JDAF UndoAction and RedoAction are focused on the state of
		// the UndoManager of the focused DataModel. But the CodeEditor has its
		// own Undo and Redo actions. So we use a ComponentAction which will
		// automatically delegate to the CodeEditors Undo and Redo actions in
		// its
		// ActionMap when the CodeEditor is focused
		getActionMap().put(ActionKeys.UNDO, new ComponentAction("undo"));
		getActionMap().put(ActionKeys.REDO, new ComponentAction("redo"));

		addDataModelListener(new DataModelAdapter()
		{
			@Override
			public void dataModelOpened(DataModelEvent dataModelEvent)
			{
				NafOptimizationRunner.this.getEventManager().publish(
						FILE_OPENED_EVENT_ID, dataModelEvent);
			}

			@Override
			public void dataModelClosed(DataModelEvent dataModelEvent)
			{
				NafOptimizationRunner.this.getEventManager().publish(
						FILE_CLOSED_EVENT_ID, dataModelEvent);
			}

			@Override
			public void dataModelSaving(DataModelEvent dataModelEvent)
			{
				NafOptimizationRunner.this.getEventManager().publish(
						FILE_SAVED_EVENT_ID, dataModelEvent);
			}
		});

	}

	protected void addToolMenu()
	{
		Action action;
		action = new GUIApplicationAction(CALCULATE_TUNING_ACTION_ID)
		{
			public void actionPerformedDetached(ActionEvent event)
			{
				StudyView studyView = getStudyView();
				if (studyView != null)
				{
					studyView.getTuning();
				}
			}
		};
		getActionMap().put(CALCULATE_TUNING_ACTION_ID, action);
		action.setEnabled(false);

		action = new GUIApplicationAction(OPTIMIZE_INSTRUMENT_ACTION_ID)
		{
			public void actionPerformedDetached(ActionEvent event)
			{
				StudyView studyView = getStudyView();
				if (studyView != null)
				{
					studyView.optimizeInstrument();
				}
			}
		};
		getActionMap().put(OPTIMIZE_INSTRUMENT_ACTION_ID, action);
		action.setEnabled(false);

		addMenuBarCustomizer(new MenuBarCustomizer()
		{
			public JMenu[] createApplicationMenus(
					ApplicationMenuBarsUI menuBarUI)
			{
				JMenu menu = menuBarUI.defaultMenu("Tool Menu", "Tool");
				menu.add(menuBarUI.getAction(CALCULATE_TUNING_ACTION_ID));
				menu.add(menuBarUI.getAction(OPTIMIZE_INSTRUMENT_ACTION_ID));
				return new JMenu[] { menu };
			}

			@Override
			public void customizeStandardMenu(String menuID, JMenu menu,
					ApplicationMenuBarsUI menuBarsUI)
			{
			}
		});
	}

	protected void addWindowMenuToggles()
	{
		Action action = new ToggleFrameAction(CONSOLE_ACTION_ID, true);
		action.putValue(AutoInstallActionsFeature.MENU_ID,
				MenuConstants.WINDOW_MENU_ID);
		getActionMap().put("consoleToggle", action);

		action = new ToggleFrameAction(STUDY_ACTION_ID, true);
		action.putValue(AutoInstallActionsFeature.MENU_ID,
				MenuConstants.WINDOW_MENU_ID);
		getActionMap().put("studyToggle", action);
	}

	protected void addEvents()
	{
		EventManager eventManager = getEventManager();
		eventManager.addEvent(FILE_OPENED_EVENT_ID);
		eventManager.addEvent(FILE_CLOSED_EVENT_ID);
		eventManager.addEvent(FILE_SAVED_EVENT_ID);
		eventManager.addEvent(TUNING_ACTIVE_EVENT_ID);
		eventManager.addEvent(OPTIMIZATION_ACTIVE_EVENT_ID);

		eventManager.subscribe(TUNING_ACTIVE_EVENT_ID, this);
		eventManager.subscribe(OPTIMIZATION_ACTIVE_EVENT_ID, this);
	}

	protected void addDockedViews()
	{
		DockingApplicationFeature docking = new DockingApplicationFeature();
		DockableConfiguration config = new DockableConfiguration();

		// config for "Console" model-view
		config.setFrameName("Console");
		config.setInitState(DockContext.STATE_FRAMEDOCKED);
		config.setInitSide(DockContext.DOCK_SIDE_SOUTH);
		config.setInitIndex(1);
		config.setCriteria(System.out.toString());
		config.setDataModelClass(SecondaryBasicDataModel.class);
		config.setDataViewClass(ConsoleView.class);
		docking.addDockableMapping(config);

		// config for "Study" pane
		config = new DockableConfiguration();
		config.setFrameName("Study");
		config.setInitState(DockContext.STATE_FRAMEDOCKED);
		config.setInitSide(DockContext.DOCK_SIDE_WEST);
		config.setInitIndex(0);
		config.setDataModelClass(SecondaryBasicDataModel2.class);
		config.setDataViewClass(StudyView.class);
		docking.addDockableMapping(config);

		// add feature
		addApplicationFeature(docking);
	}

	public static class SecondaryBasicDataModel2 extends
			SecondaryBasicDataModel
	{
	}

	@Override
	public void doEvent(SubscriberEvent e)
	{
		String eventName = e.getEvent();
		if (TUNING_ACTIVE_EVENT_ID.equals(eventName))
		{
			Action action = getActionMap().get(CALCULATE_TUNING_ACTION_ID);
			if (action != null)
			{
				action.setEnabled((Boolean) e.getSource());
			}
		}
		else if (OPTIMIZATION_ACTIVE_EVENT_ID.equals(eventName))
		{
			Action action = getActionMap().get(OPTIMIZE_INSTRUMENT_ACTION_ID);
			if (action != null)
			{
				action.setEnabled((Boolean) e.getSource());
			}
		}
	}

	protected StudyView getStudyView()
	{
		StudyView studyView = null;
		DataView[] views = getApplicationUIManager().getWindowsUI()
				.getDataViews();
		for (DataView view : views)
		{
			if (view instanceof StudyView)
			{
				studyView = (StudyView) view;
				break;
			}
		}

		return studyView;
	}

}
