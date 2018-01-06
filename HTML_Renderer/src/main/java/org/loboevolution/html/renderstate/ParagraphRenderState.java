/*
    GNU GENERAL LICENSE
    Copyright (C) 2014 - 2018 Lobo Evolution

    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public
    License as published by the Free Software Foundation; either
    verion 3 of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
    General License for more details.

    You should have received a copy of the GNU General Public
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
    

    Contact info: ivan.difrancesco@yahoo.it
 */
package org.loboevolution.html.renderstate;

import java.awt.FontMetrics;

import org.loboevolution.html.domimpl.HTMLElementImpl;
import org.loboevolution.html.style.HtmlInsets;

/**
 * The Class ParagraphRenderState.
 */
public class ParagraphRenderState extends AbstractMarginRenderState {

	/**
	 * Instantiates a new paragraph render state.
	 *
	 * @param prevRenderState
	 *            the prev render state
	 * @param element
	 *            the element
	 */
	public ParagraphRenderState(RenderState prevRenderState, HTMLElementImpl element) {
		super(prevRenderState, element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.loboevolution.html.renderstate.AbstractMarginRenderState#
	 * getDefaultMarginInsets ()
	 */
	@Override
	protected HtmlInsets getDefaultMarginInsets() {
		HtmlInsets insets = new HtmlInsets();
		RenderState prevRS = this.getPreviousRenderState();
		FontMetrics fm = prevRS == null ? this.getFontMetrics() : prevRS.getFontMetrics();
		insets.top = fm.getHeight();
		insets.bottom = fm.getHeight();
		insets.topType = HtmlInsets.TYPE_PIXELS;
		insets.bottomType = HtmlInsets.TYPE_PIXELS;
		return insets;
	}

}