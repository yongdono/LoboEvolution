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
/*
 * Created on Oct 22, 2005
 */
package org.loboevolution.html.test;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The Class NodeTreeModel.
 */
public class NodeTreeModel implements TreeModel {

	/** The root node. */
	private final Node rootNode;

	/**
	 * Instantiates a new node tree model.
	 *
	 * @param node
	 *            the node
	 */
	public NodeTreeModel(Node node) {
		super();
		rootNode = node;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getRoot()
	 */
	@Override
	public Object getRoot() {
		return this.rootNode;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(Object parent, int index) {
		Node parentNode = (Node) parent;
		return parentNode == null ? null : parentNode.getChildNodes().item(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object parent) {
		Node parentNode = (Node) parent;
		return parentNode == null ? 0 : parentNode.getChildNodes().getLength();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node) {
		if (node == this.rootNode) {
			return false;
		}
		Node domNode = (Node) node;
		return domNode == null ? true : domNode.getChildNodes().getLength() == 0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.tree.TreeModel#valueForPathChanged(javax.swing.tree.TreePath,
	 * java.lang.Object)
	 */
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// Method not implemented
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#getIndexOfChild(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		Node parentNode = (Node) parent;
		NodeList nodeList = parentNode == null ? null : parentNode.getChildNodes();
		if (nodeList == null) {
			return -1;
		}
		int length = nodeList.getLength();
		for (int i = 0; i < length; i++) {
			if (nodeList.item(i) == child) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.tree.TreeModel#addTreeModelListener(javax.swing.event.
	 * TreeModelListener)
	 */
	@Override
	public void addTreeModelListener(TreeModelListener l) {
		// Method not implemented
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.swing.tree.TreeModel#removeTreeModelListener(javax.swing.event.
	 * TreeModelListener)
	 */
	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		// Method not implemented
	}
}
