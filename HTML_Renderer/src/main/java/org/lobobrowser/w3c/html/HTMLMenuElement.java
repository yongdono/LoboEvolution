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
 * The Interface HTMLMenuElement.
 */
public interface HTMLMenuElement extends HTMLElement {
	
	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	// HTMLMenuElement
	public String getType();

	/**
	 * Sets the type.
	 *
	 * @param type
	 *            the new type
	 */
	public void setType(String type);

	/* (non-Javadoc)
	 * @see org.lobobrowser.w3c.html.HTMLElement#getLabel()
	 */
	public String getLabel();

	/**
	 * Sets the label.
	 *
	 * @param label
	 *            the new label
	 */
	public void setLabel(String label);

	/**
	 * Gets the compact.
	 *
	 * @return the compact
	 */
	// HTMLMenuElement-21
	public boolean getCompact();

	/**
	 * Sets the compact.
	 *
	 * @param compact
	 *            the new compact
	 */
	public void setCompact(boolean compact);
}
