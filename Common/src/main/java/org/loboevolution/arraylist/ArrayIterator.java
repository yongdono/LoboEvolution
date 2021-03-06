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
package org.loboevolution.arraylist;

import java.util.Iterator;

/**
 * The Class ArrayIterator.
 */
public class ArrayIterator implements Iterator {
	/** The array. */
	private final Object[] array;
	/** The top. */
	private final int top;
	/** The offset. */
	private int offset;

	/**
	 * Instantiates a new array iterator.
	 *
	 * @param array
	 *            the array
	 * @param offset
	 *            the offset
	 * @param length
	 *            the length
	 */
	public ArrayIterator(Object[] array, int offset, int length) {
		this.array = array;
		this.offset = offset;
		this.top = offset + length;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return this.offset < this.top;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#next()
	 */
	@Override
	public Object next() {
		return this.array[this.offset++];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		// Method not implemented
	}
}
