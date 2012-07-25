package com.wwidesigner.geometry.calculation;

import org.apache.commons.math3.complex.Complex;

import com.wwidesigner.math.TransferMatrix;
import com.wwidesigner.util.PhysicalParameters;

/**
 * Class to calculate transmission matrices for tubular waveguides.
 * @author Burton Patkau
 *
 */
public class Tube
{
	public Tube()
	{
	}

    /**
     * Calculate the impedance of an unflanged open end of a real pipe.
     * @param freq: fundamental frequency of the waveform.
     * @param radius: radius of pipe, in metres.
     * @return impedance as seen by pipe.
     */
    public static Complex calcZload( double freq, double radius, PhysicalParameters params )
    {
    	Complex zRel = new Complex(9.87 * freq * radius / params.getSpeedOfSound(), 3.84 )
    						.multiply( freq * radius / params.getSpeedOfSound() );
    	return zRel.multiply( params.calcZ0(radius) );
    }

    /**
     * Calculate the impedance of an open end of a real pipe,
     * assuming an infinite flange.
     * @param freq: fundamental frequency of the waveform.
     * @param radius: radius of pipe, in metres.
     * @return impedance as seen by pipe.
     */
    public static Complex calcZflanged( double freq, double radius, PhysicalParameters params )
    {
    	Complex zRel = new Complex(19.7 * freq * radius / params.getSpeedOfSound(), 5.33 )
    					.multiply( freq * radius / params.getSpeedOfSound() );
    	return zRel.multiply( params.calcZ0(radius) );
    }
    
	/**
	 * Calculate the transfer matrix of a cylinder.
	 * @param waveNumber: 2*pi*f/c, in radians per metre
	 * @param length: length of the cylinder, in metres.
	 * @param radius: radius of the cylinder, in metres.
	 * @param params: physical parameters
	 * @return Transfer matrix
	 */
	public static TransferMatrix calcCylinderMatrix(double waveNumber, 
			double length, double radius, PhysicalParameters params)
	{
		double Zc = params.calcZ0(radius);
		double epsilon = 1.0/(radius * Math.sqrt(waveNumber)) * params.getAlphaConstant();
		Complex gammaL = new Complex( epsilon, (1.0+epsilon) ).multiply( waveNumber * length );
		Complex coshL = gammaL.cosh();
		Complex sinhL = gammaL.sinh();
        TransferMatrix result = new TransferMatrix(coshL, sinhL.multiply(Zc), sinhL.divide(Zc), coshL);
        
		return result;
	}

	/**
	 * Calculate the transfer matrix of a conical tube.
	 * @param freq: frequency in Hz.
	 * @param length: length of the tube, in metres.
	 * @param leftRadius: radius of one end the tube, in metres.
	 * @param rightRadius: radius of other end the tube, in metres.
	 * @param params: physical parameters
	 * @return Transfer matrix
	 */
	public static TransferMatrix calcConeMatrix(double waveNumber, 
			double length, double leftRadius, double rightRadius, PhysicalParameters params)
	{
		// TODO Copied from Dan Gordon.  Not verified.
		double ZcLeft  = params.calcZ0(leftRadius);
		double ZcRight = params.calcZ0(rightRadius);

		double one_over_x_in = (rightRadius-leftRadius) / (leftRadius*length);
		double one_over_x_out = (rightRadius-leftRadius) / (rightRadius*length);

		// inverse of the equivalent radius at which we calculate the losses
		double one_over_Req = Math.log(rightRadius/leftRadius) / (rightRadius - leftRadius);

		Complex k_lossy = Complex.valueOf(1,-1).multiply(one_over_Req
				*Math.sqrt(waveNumber)*params.getAlphaConstant()).add(waveNumber);		

		Complex k_lossy_L = k_lossy.multiply(length);

		Complex A = k_lossy_L.cos().multiply(leftRadius/leftRadius).subtract( k_lossy_L.sin().multiply(one_over_x_in).divide(k_lossy) );
		Complex B = k_lossy_L.sin().multiply(Complex.I).multiply((rightRadius/leftRadius)*ZcRight);
		Complex C = Complex.valueOf(rightRadius/leftRadius).multiply(Complex.I.multiply(k_lossy_L.sin()).multiply(k_lossy.pow(-2).multiply(one_over_x_in*one_over_x_out).add(1))
				.add( k_lossy_L.cos().multiply(one_over_x_in-one_over_x_out).divide(Complex.I.multiply(k_lossy)))).divide(ZcLeft);
		Complex D = k_lossy_L.cos().multiply(leftRadius/leftRadius).add( k_lossy_L.sin().multiply(one_over_x_out).divide(k_lossy) );

		return new TransferMatrix(A, B, C, D);		
	}

}