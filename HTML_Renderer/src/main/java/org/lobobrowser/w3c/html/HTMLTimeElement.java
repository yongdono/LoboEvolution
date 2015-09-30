/*
    GNU GENERAL LICENSE
    Copyright (C) 2006 The Lobo Project. Copyright (C) 2014 - 2015 Lobo Evolution

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    verion 2 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    General License for more details.

    You should have received a copy of the GNU General Public
    License along with this library; if not, write to the Free Software
    Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

    Contact info: lobochief@users.sourceforge.net; ivan.difrancesco@yahoo.it
 */

package org.lobobrowser.w3c.html;


/**
 * The Interface HTMLTimeElement.
 */
public interface HTMLTimeElement extends HTMLElement {
	
	/**
	 * Gets the date time.
	 *
	 * @return the date time
	 */
	// HTMLTimeElement
	public String getDateTime();

	/**
	 * Sets the date time.
	 *
	 * @param dateTime
	 *            the new date time
	 */
	public void setDateTime(String dateTime);

	/**
	 * Gets the pub date.
	 *
	 * @return the pub date
	 */
	public boolean getPubDate();

	/**
	 * Sets the pub date.
	 *
	 * @param pubDate
	 *            the new pub date
	 */
	public void setPubDate(boolean pubDate);

	/**
	 * Gets the value as date.
	 *
	 * @return the value as date
	 */
	public long getValueAsDate();
}
