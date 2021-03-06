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
package org.loboevolution.w3c.html;

/**
 * The public interface ApplicationCache.
 */
public interface ApplicationCache {
	/** The Constant UNCACHED. */
	short UNCACHED = 0;

	/** The Constant IDLE. */
	short IDLE = 1;

	/** The Constant CHECKING. */
	short CHECKING = 2;

	/** The Constant DOWNLOADING. */
	short DOWNLOADING = 3;

	/** The Constant UPDATEREADY. */
	short UPDATEREADY = 4;

	/** The Constant OBSOLETE. */
	short OBSOLETE = 5;

	/**
	 * Gets the status.
	 *
	 * @return the status
	 */
	short getStatus();

	/**
	 * Update.
	 */
	void update();

	/**
	 * Swap cache.
	 */
	void swapCache();
}
