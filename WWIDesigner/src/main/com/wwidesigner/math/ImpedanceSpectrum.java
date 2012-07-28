/**
 * 
 */
package com.wwidesigner.math;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.apache.commons.math3.complex.Complex;

import com.jidesoft.chart.Chart;
import com.jidesoft.chart.Legend;
import com.jidesoft.chart.model.DefaultChartModel;
import com.jidesoft.chart.style.ChartStyle;
import com.wwidesigner.geometry.InstrumentInterface;
import com.wwidesigner.note.Fingering;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Representation of a complex spectrum, along with information about its
 * extreme points.
 */
public class ImpedanceSpectrum
{

	private Map<Double, Complex> mSpectrum = new TreeMap<Double, Complex>();
	private List<Double> mMinima = new ArrayList<Double>();
	private List<Double> mMaxima = new ArrayList<Double>();

	/**
	 * Add or replace a point in the spectrum.
	 */
	public void setDataPoint(double frequency, Complex impedance)
	{
		mSpectrum.put(frequency, impedance);
	}

	public void calcImpedance(InstrumentInterface flute, double freqStart,
			double freqEnd, int nfreq, Fingering fingering,
			PhysicalParameters physicalParams)
	{
		Complex prevZ = Complex.ZERO;
		double absPrevPrevZ = 0;
		double prevFreq = 0;
		double freqStep = (freqEnd - freqStart) / (nfreq - 1);
		for (int i = 0; i < nfreq; ++i)
		{
			double freq = freqStart + i * freqStep;
			Complex zAc = flute.calcZ(freq, fingering, physicalParams);
			double absZAc = zAc.abs();

			setDataPoint(freq, zAc);

			double absPrevZ = prevZ.abs();

			if ((i >= 2) && (absPrevZ < absZAc) && (absPrevZ < absPrevPrevZ))
			{
				// We have found an impedance minimum.
				getMinima().add(prevFreq);
			}

			if ((i >= 2) && (absPrevZ > absZAc) && (absPrevZ > absPrevPrevZ))
			{
				// We have found an impedance maximum.
				getMaxima().add(prevFreq);
			}

			absPrevPrevZ = absPrevZ;
			prevZ = zAc;
			prevFreq = freq;
		}
	}

	public List<Double> getMaxima()
	{
		return mMaxima;
	}

	public void setMaxima(List<Double> maxima)
	{
		mMaxima = maxima;
	}

	public List<Double> getMinima()
	{
		return mMinima;
	}

	public void setMinima(List<Double> minima)
	{
		mMinima = minima;
	}

	public Map<Double, Complex> getSpectrum()
	{
		return mSpectrum;
	}

	public void setSpectrum(Map<Double, Complex> spectrum)
	{
		mSpectrum = spectrum;
	}

	public Double getClosestMinimumFrequency(double frequency)
	{
		Double closestFreq = null;
		double deviation = Double.MAX_VALUE;
		for (double minVal : mMinima)
		{
			double thisDeviation = Math.abs(frequency - minVal);
			if (thisDeviation < deviation)
			{
				closestFreq = minVal;
				deviation = thisDeviation;
			}
		}

		return closestFreq;
	}

	public Double getClosestMaximumFrequency(double frequency)
	{
		Double closestFreq = null;
		double deviation = Double.MAX_VALUE;
		for (double maxVal : mMaxima)
		{
			double thisDeviation = Math.abs(frequency - maxVal);
			if (thisDeviation < deviation)
			{
				closestFreq = maxVal;
				deviation = thisDeviation;
			}
		}

		return closestFreq;
	}

	public void plotSpectrum()
	{
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JFrame frame = new JFrame("Impedance Spectrum");
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setSize(800, 600);
				DefaultChartModel model1 = new DefaultChartModel("Absolute Value");
				DefaultChartModel model2 = new DefaultChartModel("Absolute value, imaginary");
				double minX = Double.MAX_VALUE;
				double maxX = Double.NEGATIVE_INFINITY;
				double minY = Double.MAX_VALUE;
				double maxY = Double.NEGATIVE_INFINITY;
				for (Map.Entry<Double, Complex> point : mSpectrum.entrySet())
				{
					double x = point.getKey();
					double y = point.getValue().abs();
					double i = Math.abs(point.getValue().getImaginary());
					if (x < minX)
					{
						minX = x;
					}
					if (x > maxX)
					{
						maxX = x;
					}
					if (y < minY)
					{
						minY = y;
					}
					if (y > maxY)
					{
						maxY = y;
					}
					model1.addPoint(x, y);
					model2.addPoint(x, i);
				}
				Chart chart = new Chart();
				ChartStyle style1 = new ChartStyle(Color.black, false, true);
				ChartStyle style2 = new ChartStyle(Color.red, false, true);
				chart.addModel(model1, style1);
				chart.addModel(model2, style2);
				chart.getXAxis().setRange(minX, maxX);
				chart.getXAxis().setLabel("Frequency");
				chart.getYAxis().setRange(minY, maxY);
				chart.getYAxis().setLabel("Impedance");
				chart.setTitle("Impedance Spectrum");
				Legend legend = new Legend(chart);
				chart.addDrawable(legend);
				legend.setLocation(200, 50);
				frame.setContentPane(chart);
				frame.setVisible(true);
			}
		});
	}
}
