/**
 * Interface to a class describing an instrument's tuning pattern.
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
package com.wwidesigner.note;

import java.util.List;


public interface TuningInterface
{

	/**
	 * Gets the value of the name property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getName();

	/**
	 * Sets the value of the name property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setName(String value);

	/**
	 * Gets the value of the comment property.
	 * 
	 * @return possible object is {@link String }
	 * 
	 */
	public String getComment();

	/**
	 * Sets the value of the comment property.
	 * 
	 * @param value
	 *            allowed object is {@link String }
	 * 
	 */
	public void setComment(String value);

	/**
	 * Gets the value of the numberOfHoles property.
	 * 
	 * @return possible object is {@link int }
	 * 
	 */
	public int getNumberOfHoles();

	/**
	 * Sets the value of the numberOfHoles property.
	 * 
	 * @param value
	 *            allowed object is {@link int }
	 * 
	 */
	public void setNumberOfHoles(int value);

	/**
	 * Gets the value of the fingering property.
	 * 
	 * <p>
	 * This accessor method returns a reference to the live list, not a
	 * snapshot. Therefore any modification you make to the returned list will
	 * be present inside the JAXB object. This is why there is not a
	 * <CODE>set</CODE> method for the fingering property.
	 * 
	 * <p>
	 * For example, to add a new item, do as follows:
	 * 
	 * <pre>
	 * getFingering().add(newItem);
	 * </pre>
	 * 
	 * 
	 * <p>
	 * Objects of the following type(s) are allowed in the list
	 * {@link Fingering }
	 * 
	 * 
	 */
	public List<Fingering> getFingering();

}