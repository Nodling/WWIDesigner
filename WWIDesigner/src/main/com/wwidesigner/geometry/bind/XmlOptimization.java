//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.06.02 at 10:20:05 AM MDT 
//

package com.wwidesigner.geometry.bind;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for XmlOptimization.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * <p>
 * 
 * <pre>
 * &lt;simpleType name="XmlOptimization">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}token">
 *     &lt;enumeration value="FIXED"/>
 *     &lt;enumeration value="FREE"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "XmlOptimization")
@XmlEnum
public enum XmlOptimization
{

	FIXED, FREE;

	public String value()
	{
		return name();
	}

	public static XmlOptimization fromValue(String v)
	{
		return valueOf(v);
	}

}
