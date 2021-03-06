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
 * Created on Apr 16, 2005
 */
package org.loboevolution.html.gui;

import java.awt.Adjustable;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.loboevolution.arraylist.ArrayUtilities;
import org.loboevolution.html.HtmlRendererContext;
import org.loboevolution.html.dombl.ModelNode;
import org.loboevolution.html.dombl.UINode;
import org.loboevolution.html.domimpl.DOMNodeImpl;
import org.loboevolution.html.domimpl.HTMLElementImpl;
import org.loboevolution.html.gui.mouse.HtmlMousePanel;
import org.loboevolution.html.renderer.BoundableRenderable;
import org.loboevolution.html.renderer.DelayedPair;
import org.loboevolution.html.renderer.FrameContext;
import org.loboevolution.html.renderer.NodeRenderer;
import org.loboevolution.html.renderer.RCollection;
import org.loboevolution.html.renderer.RElement;
import org.loboevolution.html.renderer.Renderable;
import org.loboevolution.html.renderer.RenderableContainer;
import org.loboevolution.html.renderer.RenderableSpot;
import org.loboevolution.html.rendererblock.RBlock;
import org.loboevolution.html.renderstate.RenderState;
import org.loboevolution.http.UserAgentContext;
import org.loboevolution.util.Nodes;
import org.loboevolution.util.Objects;
import org.w3c.dom.Node;

/**
 * A Swing component that renders a HTML block, given by a DOM root or an
 * internal element, typically a DIV. This component <i>cannot</i> render
 * FRAMESETs. <code>HtmlBlockPanel</code> is used by {@link HtmlPanel} whenever
 * the DOM is determined <i>not</i> to be a FRAMESET.
 *
 * @author J. H. S.
 * @see HtmlPanel
 * @see FrameSetPanel
 */
public class HtmlBlockPanel extends JComponent implements NodeRenderer, RenderableContainer, ClipboardOwner {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant logger. */
	private static final Logger logger = LogManager.getLogger(HtmlBlockPanel.class);

	/** The frame context. */
	protected final transient FrameContext frameContext;

	/** The ucontext. */
	protected final transient UserAgentContext ucontext;

	/** The rcontext. */
	protected final transient HtmlRendererContext rcontext;

	/** The start selection. */
	protected transient RenderableSpot startSelection;

	/** The end selection. */
	protected transient RenderableSpot endSelection;

	/** The rblock. */
	protected transient RBlock rblock;

	/** The preferred width. */
	protected int preferredWidth = -1;

	/** The default margin insets. */
	protected Insets defaultMarginInsets = null;

	/** The processing document notification. */
	private boolean processingDocumentNotification = false;

	/** The default overflow x. */
	protected int defaultOverflowX = RenderState.OVERFLOW_AUTO;

	/** The default overflow y. */
	protected int defaultOverflowY = RenderState.OVERFLOW_SCROLL;
	
	/** The components. */
	private Set<Component> components;

	/**
	 * Instantiates a new html block panel.
	 *
	 * @param pcontext
	 *            the pcontext
	 * @param rcontext
	 *            the rcontext
	 * @param frameContext
	 *            the frame context
	 */
	public HtmlBlockPanel(UserAgentContext pcontext, HtmlRendererContext rcontext, FrameContext frameContext) {
		this(new Color(0,0,0,0), true, pcontext, rcontext, frameContext);
	}

	/**
	 * Instantiates a new html block panel.
	 *
	 * @param background
	 *            the background
	 * @param opaque
	 *            the opaque
	 * @param pcontext
	 *            the pcontext
	 * @param rcontext
	 *            the rcontext
	 * @param frameContext
	 *            the frame context
	 */
	
	public HtmlBlockPanel(Color background, boolean opaque, UserAgentContext pcontext, HtmlRendererContext rcontext, FrameContext frameContext) {
		this.frameContext = frameContext;
		this.ucontext = pcontext;
		this.rcontext = rcontext;
		createAndShowGUI(background, opaque);
	}
	
	private void createAndShowGUI(Color background, boolean opaque) {
		this.setLayout(null);
		this.setAutoscrolls(true);
		this.setOpaque(opaque);
		this.setBackground(background);
		ActionListener actionListener = e -> {
			String command = e.getActionCommand();
			if ("copy".equals(command)) {
				copy();
			}
		};
		if (!GraphicsEnvironment.isHeadless()) {
			this.registerKeyboardAction(actionListener, "copy", KeyStroke.getKeyStroke(KeyEvent.VK_COPY, 0), JComponent.WHEN_FOCUSED);
			this.registerKeyboardAction(actionListener, "copy", KeyStroke.getKeyStroke(KeyEvent.VK_C, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), JComponent.WHEN_FOCUSED);
		}
		
		HtmlMousePanel mouse = new HtmlMousePanel(this);
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				mouse.onMouseClick(e, getRblock());
			}

			@Override
			public void mouseExited(MouseEvent e) {
				mouse.onMouseExited(e);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				mouse.onMousePressed(e, getRblock(), getFrameContext());
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				mouse.onMouseReleased(e, getRblock());
			}
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				mouse.onMouseDragged(e, getRblock(), getFrameContext());
			}

			@Override
			public void mouseMoved(MouseEvent arg0) {
				mouse.onMouseMoved(arg0, getRblock());
			}
		});
		
		this.addMouseWheelListener(e -> mouse.onMouseWheelMoved(e, getRblock()));

		this.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent evt) {
				mouse.onKeyUp(evt, getRblock());
			}

			@Override
			public void keyPressed(KeyEvent evt) {
				mouse.onKeyPressed(evt, getRblock());
			}
		});
	}


	/**
	 * Scrolls the body area to the given location.
	 * <p>
	 * This method should be called from the GUI thread.
	 *
	 * @param bounds
	 *            The bounds in the scrollable block area that should become
	 *            visible.
	 * @param xIfNeeded
	 *            If this parameter is true, scrolling will only occur if the
	 *            requested bounds are not currently visible horizontally.
	 * @param yIfNeeded
	 *            If this parameter is true, scrolling will only occur if the
	 *            requested bounds are not currently visible vertically.
	 */
	public void scrollTo(Rectangle bounds, boolean xIfNeeded, boolean yIfNeeded) {
		RBlock block = this.rblock;
		if (block != null) {
			block.scrollTo(bounds, xIfNeeded, yIfNeeded);
		}
	}

	/**
	 * Scroll by.
	 *
	 * @param xOffset
	 *            the x offset
	 * @param yOffset
	 *            the y offset
	 */
	public void scrollBy(int xOffset, int yOffset) {
		RBlock block = this.rblock;
		if (block != null) {
			if (xOffset != 0) {
				block.scrollBy(Adjustable.HORIZONTAL, xOffset);
			}
			if (yOffset != 0) {
				block.scrollBy(Adjustable.VERTICAL, yOffset);
			}
		}
	}

	/**
	 * Scrolls the body area to the node given, if it is part of the current
	 * document.
	 * <p>
	 * This method should be called from the GUI thread.
	 *
	 * @param node
	 *            A DOM node.
	 */
	public void scrollTo(Node node) {
		Rectangle bounds = this.getNodeBoundsNoMargins(node, true);
		if (bounds == null) {
			return;
		}
		this.scrollTo(bounds, true, false);
	}

	/**
	 * Gets the rectangular bounds of the given node.
	 * <p>
	 * This method should be called from the GUI thread.
	 *
	 * @param node
	 *            A node in the current document.
	 * @param relativeToScrollable
	 *            Whether the bounds should be relative to the scrollable body
	 *            area. Otherwise, they are relative to the root block (which is
	 *            the essentially the same as being relative to this
	 *            <code>HtmlBlockPanel</code> minus Swing borders).
	 * @return the node bounds
	 */
	public Rectangle getNodeBounds(Node node, boolean relativeToScrollable) {
		RBlock block = this.rblock;
		if (block == null) {
			return null;
		}

		Node currentNode = node;
		UINode uiNode = getUINode(currentNode);
		if (uiNode == null) {
			return null;
		}
		RCollection relativeTo = relativeToScrollable ? (RCollection) block.getRBlockViewport() : (RCollection) block;
		if (Objects.equals(node, currentNode)) {
			BoundableRenderable br = (BoundableRenderable) uiNode;
			Point guiPoint = br.getOriginRelativeTo(relativeTo);
			Dimension size = br.getSize();
			return new Rectangle(guiPoint, size);
		} else {
			return this.scanNodeBounds((RCollection) uiNode, node, relativeTo);
		}
	}

	/**
	 * Gets the rectangular bounds of the given node with margins cut off.
	 * <p>
	 * Internally calls getNodeBounds and cuts off margins.
	 *
	 * @param node
	 *            A node in the current document.
	 * @param relativeToScrollable
	 *            see getNodeBounds.
	 * @return the node bounds no margins
	 */
	public Rectangle getNodeBoundsNoMargins(Node node, boolean relativeToScrollable) {
		RBlock block = this.rblock;
		if (block == null) {
			return null;
		}
		
		Node currentNode = node;
		UINode uiNode = getUINode(currentNode);
		if (uiNode == null) {
			return null;
		}

		Rectangle bounds;

		RCollection relativeTo = relativeToScrollable ? (RCollection) block.getRBlockViewport() : (RCollection) block;
		if (Objects.equals(node, currentNode)) {
			BoundableRenderable br = (BoundableRenderable) uiNode;
			Point guiPoint = br.getOriginRelativeTo(relativeTo);
			Dimension size = br.getSize();
			bounds = new Rectangle(guiPoint, size);
		} else {
			bounds = this.scanNodeBounds((RCollection) uiNode, node, relativeTo);
		}

		/* cut off margins */
		if (uiNode instanceof RElement) {
			RElement el = (RElement) uiNode;
			int top = el.getMarginTop();
			int left = el.getMarginLeft();
			bounds.x += left;
			bounds.y += top;
			bounds.width -= left + el.getMarginRight();
			bounds.height -= top + el.getMarginBottom();
		}

		return bounds;
	}

	/**
	 * Gets an aggregate of the bounds of renderer leaf nodes.
	 *
	 * @param root
	 *            the root
	 * @param node
	 *            the node
	 * @param relativeTo
	 *            the relative to
	 * @return the rectangle
	 */
	private Rectangle scanNodeBounds(RCollection root, Node node, RCollection relativeTo) {
		Iterator i = root.getRenderables();
		Rectangle resultBounds = null;
		BoundableRenderable prevBoundable = null;
		if (i != null) {
			while (i.hasNext()) {
				Renderable r = (Renderable) i.next();
				Rectangle subBounds = null;
				if (r instanceof RCollection) {
					RCollection rc = (RCollection) r;
					prevBoundable = rc;
					subBounds = this.scanNodeBounds(rc, node, relativeTo);
				} else if (r instanceof BoundableRenderable) {
					BoundableRenderable br = (BoundableRenderable) r;
					prevBoundable = br;
					if (Nodes.isSameOrAncestorOf(node, (Node) r.getModelNode())) {
						Point origin = br.getOriginRelativeTo(relativeTo);
						Dimension size = br.getSize();
						subBounds = new Rectangle(origin, size);
					}
				} else {
					// This would have to be a RStyleChanger. We rely on these
					// when the target node has blank content.
					if (Nodes.isSameOrAncestorOf(node, (Node) r.getModelNode())) {
						int xInRoot = prevBoundable == null ? 0 : prevBoundable.getX() + prevBoundable.getWidth();
						Point rootOrigin = root.getOriginRelativeTo(relativeTo);
						subBounds = new Rectangle(rootOrigin.x + xInRoot, rootOrigin.y, 0, root.getHeight());
					}
				}
				if (subBounds != null) {
					if (resultBounds == null) {
						resultBounds = subBounds;
					} else {
						resultBounds = subBounds.union(resultBounds);
					}
				}
			}
		}
		return resultBounds;
	}

	/**
	 * Gets the root renderable.
	 *
	 * @return the root renderable
	 */
	public BoundableRenderable getRootRenderable() {
		return this.rblock;
	}

	/**
	 * Sets the preferred width.
	 *
	 * @param width
	 *            the new preferred width
	 */
	public void setPreferredWidth(int width) {
		this.preferredWidth = width;
	}

	/**
	 * If the preferred size has been set with
	 * {@link #setPreferredSize(Dimension)}, then that size is returned.
	 * Otherwise a preferred size is calculated by rendering the HTML DOM,
	 * provided one is available and a preferred width other than
	 * <code>-1</code> has been set with {@link #setPreferredWidth(int)}. An
	 * arbitrary preferred size is returned in other scenarios.
	 *
	 * @return the preferred size
	 */
	@Override
	public Dimension getPreferredSize() {
		// Expected to be invoked in the GUI thread.
		if (this.isPreferredSizeSet()) {
			return super.getPreferredSize();
		}
		final int pw = this.preferredWidth;
		if (pw != -1) {
			final RBlock block = this.rblock;
			if (block != null) {
				// Layout should always be done in the GUI thread.
				if (SwingUtilities.isEventDispatchThread()) {
					block.layout(pw, 0, false, false, RenderState.OVERFLOW_VISIBLE, RenderState.OVERFLOW_VISIBLE, true);
				} else {
					try {
						SwingUtilities.invokeAndWait(() -> block.layout(pw, 0, false, false,
								RenderState.OVERFLOW_VISIBLE, RenderState.OVERFLOW_VISIBLE, true));
					} catch (Exception err) {
						logger.error( "Unable to do preferred size layout.", err);
					}
				}
				// Adjust for permanent vertical scrollbar.
				int newPw = Math.max(block.width + block.getVScrollBarWidth(), pw);
				return new Dimension(newPw, block.height);
			}
		}
		return new Dimension(600, 400);
	}

	@Override
	protected void finalize() throws Throwable {
		super.finalize();
	}

	/**
	 * Copy.
	 *
	 * @return true, if successful
	 */
	public boolean copy() {
		String selection = HtmlBlockPanel.this.getSelectionText();
		if (selection != null) {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(new StringSelection(selection), HtmlBlockPanel.this);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Gets the first line height.
	 *
	 * @return the first line height
	 */
	public int getFirstLineHeight() {
		RBlock block = this.rblock;
		return block == null ? 0 : block.getFirstLineHeight();
	}

	/**
	 * Sets the selection end.
	 *
	 * @param rpoint
	 *            the new selection end
	 */
	public void setSelectionEnd(RenderableSpot rpoint) {
		this.endSelection = rpoint;
	}

	/**
	 * Sets the selection start.
	 *
	 * @param rpoint
	 *            the new selection start
	 */
	public void setSelectionStart(RenderableSpot rpoint) {
		this.startSelection = rpoint;
	}

	/**
	 * Checks if is selection available.
	 *
	 * @return true, if is selection available
	 */
	public boolean isSelectionAvailable() {
		RenderableSpot start = this.startSelection;
		RenderableSpot end = this.endSelection;
		return start != null && end != null && !start.equals(end);
	}

	/**
	 * Gets the selection node.
	 *
	 * @return the selection node
	 */
	public Node getSelectionNode() {
		RenderableSpot start = this.startSelection;
		RenderableSpot end = this.endSelection;
		if (start != null && end != null) {
			return Nodes.getCommonAncestor((Node) start.getRenderable().getModelNode(),
					(Node) end.getRenderable().getModelNode());
		} else {
			return null;
		}
	}

	/**
	 * Sets the root node to render. This method should be invoked in the GUI
	 * dispatch thread.
	 *
	 * @param node
	 *            the new root node
	 */
	@Override
	public void setRootNode(DOMNodeImpl node) {
		if (node != null) {
			RBlock block = new RBlock(node, 0, this.ucontext, this.rcontext, this.frameContext, this);
			block.setDefaultMarginInsets(this.defaultMarginInsets);
			block.setDefaultOverflowX(this.defaultOverflowX);
			block.setDefaultOverflowY(this.defaultOverflowY);
			node.setUINode(block);
			this.rblock = block;
		} else {
			this.rblock = null;
		}
		this.invalidate();
		this.validateAll();
		this.repaint();
	}

	/**
	 * Validate all.
	 */
	protected void validateAll() {
		Component toValidate = this;
		while(true) {
			Container parent = toValidate.getParent();
			if (parent == null || parent.isValid()) {
				break;
			}
			toValidate = parent;
		}
		toValidate.validate();
	}

	/**
	 * Revalidate panel.
	 */
	protected void revalidatePanel() {
		// Called in the GUI thread.
		this.invalidate();
		this.validate();
		this.repaint();
	}

	/**
	 * Gets the root node.
	 *
	 * @return the root node
	 */
	public DOMNodeImpl getRootNode() {
		RBlock block = this.rblock;
		return block == null ? null : (DOMNodeImpl) block.getModelNode();
	}

	@Override
	public void paintComponent(Graphics g) {

		if (this.isOpaque()) {
			// Background not painted by default in JComponent.
			Rectangle clipBounds = g.getClipBounds();

			g.setColor(getBackground());
			g.fillRect(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
			g.setColor(getForeground());
		}
		if (g instanceof Graphics2D) {
			Graphics2D g2 = (Graphics2D) g;
			try {
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_GASP);
			} catch (NoSuchFieldError e) {
				g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			}
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		RBlock block = this.rblock;
		if (block != null) {
			block.paint(g);
			// Paint FrameContext selection

			RenderableSpot start = this.startSelection;
			RenderableSpot end = this.endSelection;
			if (start != null && end != null && !start.equals(end)) {
				block.paintSelection(g, false, start, end);
			}
		}
	}

	@Override
	public void doLayout() {
		try {
			Dimension size = this.getSize();
			
			this.clearComponents();
			RBlock block = this.rblock;
			if (block != null) {
				block.layout(size.width, size.height, true, true, null, false);
				// Only set origin
				block.setOrigin(0, 0);
				block.updateWidgetBounds(0, 0);
				this.updateGUIComponents();
			} else {
				if (this.getComponentCount() > 0) {
					this.removeAll();
				}
			}
		} catch (Throwable thrown) {
			logger.error( "Unexpected error in layout engine. Document is " + this.getRootNode(), thrown);
		}
	}

	/**
	 * Implementation of UINode.repaint().
	 *
	 * @param modelNode
	 *            the model node
	 */
	public void repaint(ModelNode modelNode) {
		// this.rblock.invalidateRenderStyle();
		this.repaint();
	}

	/**
	 * Gets the selection text.
	 *
	 * @return the selection text
	 */
	public String getSelectionText() {
		RenderableSpot start = this.startSelection;
		RenderableSpot end = this.endSelection;
		if (start != null && end != null) {
			StringBuilder buffer = new StringBuilder();
			this.rblock.extractSelectionText(buffer, false, start, end);
			return buffer.toString();
		} else {
			return null;
		}
	}

	/**
	 * Checks for selection.
	 *
	 * @return true, if successful
	 */
	public boolean hasSelection() {
		RenderableSpot start = this.startSelection;
		RenderableSpot end = this.endSelection;
		return start != null && end != null && !start.equals(end);
	}

	@Override
	protected void paintChildren(Graphics g) {
		// Overridding with NOP. For various reasons,
		// the regular mechanism for painting children
		// needs to be handled by Cobra.
	}

	@Override
	public Color getPaintedBackgroundColor() {
		return this.isOpaque() ? this.getBackground() : null;
	}

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// Method not implemented
	}

	@Override
	public void relayout() {
		// Expected to be called in the GUI thread.
		// Renderable branch should be invalidated at this
		// point, but this GUI component not necessarily.
		this.revalidatePanel();
	}

	@Override
	public void invalidateLayoutUpTree() {
		// Method not implemented
		// Called when renderable branch is invalidated.
		// We shouldn't do anything here. Changes in renderer
		// tree do not have any bearing on validity of GUI
		// component.
	}

	@Override
	public void updateAllWidgetBounds() {
		this.rblock.updateWidgetBounds(0, 0);
	}

	@Override
	public Point getGUIPoint(int clientX, int clientY) {
		// This is the GUI!
		return new Point(clientX, clientY);
	}

	@Override
	public void focus() {
		this.grabFocus();
	}

	/**
	 * Process document notifications.
	 *
	 * @param notifications
	 *            the notifications
	 */
	public void processDocumentNotifications(DocumentNotification[] notifications) {
		if (this.processingDocumentNotification) {
			throw new IllegalStateException("Recursive");
		}
		this.processingDocumentNotification = true;
		try {
			boolean topLayout = false;
			ArrayList<RElement> repainters = null;
			for (DocumentNotification dn : notifications) {
				int type = dn.type;
				switch (type) {
				case DocumentNotification.GENERIC:
				case DocumentNotification.SIZE: {
					DOMNodeImpl node = dn.node;
					if (node == null) {
						this.rblock.invalidateLayoutDeep();
					} else {
						UINode uiNode = node.findUINode();
						if (uiNode != null) {
							RElement relement = (RElement) uiNode;
							relement.invalidateLayoutUpTree();
						}
					}
					topLayout = true;
					break;
				}
				case DocumentNotification.POSITION: {
					// TODO: Could be more efficient.
					DOMNodeImpl node = dn.node;
					DOMNodeImpl parent = (DOMNodeImpl) node.getParentNode();
					if (parent != null) {
						UINode uiNode = parent.findUINode();
						if (uiNode != null) {
							RElement relement = (RElement) uiNode;
							relement.invalidateLayoutUpTree();
						}
					}
					topLayout = true;
					break;
				}
				case DocumentNotification.LOOK: {
					DOMNodeImpl node = dn.node;
					UINode uiNode = node.findUINode();
					if (uiNode != null) {
						if (repainters == null) {
							repainters = new ArrayList<RElement>(1);
						}
						RElement relement = (RElement) uiNode;
						// relement.invalidateRenderStyle();
						repainters.add(relement);
					}
					break;
				}
				default:
					break;
				}
			}
			if (topLayout) {
				this.revalidatePanel();
			} else {
				if (ArrayUtilities.isNotBlank(repainters)) {
					for (RElement element : repainters) {
						element.repaint();
					}
				}
			}
		} finally {
			this.processingDocumentNotification = false;
		}
	}

	@Override
	public void addDelayedPair(DelayedPair pair) {
		// Method not implemented
	}

	@Override
	public RenderableContainer getParentContainer() {
		return null;
	}

	@Override
	public Collection getDelayedPairs() {
		return null;
	}

	@Override
	public void clearDelayedPairs() {
		// Method not implemented
	}

	/**
	 * Clear components.
	 */
	private void clearComponents() {
		Set<Component> c = this.components;
		if (c != null) {
			c.clear();
		}
	}

	@Override
	public Component addComponent(Component component) {
		Set<Component> c = this.components;
		if (c == null) {
			c = new HashSet<Component>();
			this.components = c;
		}
		if (c.add(component)) {
			return component;
		} else {
			return null;
		}
	}

	/**
	 * Update gui components.
	 */
	private void updateGUIComponents() {
		// We use this method, instead of removing all components and
		// adding them back, because removal of components can cause
		// them to lose focus.

		Set<Component> c = this.components;
		if (c == null) {
			if (this.getComponentCount() != 0) {
				this.removeAll();
			}
		} else {
			// Remove children not in the set.
			Set<Component> workingSet = new HashSet<Component>();
			workingSet.addAll(c);
			int count = this.getComponentCount();
			for (int i = 0; i < count;) {
				Component component = this.getComponent(i);
				if (!c.contains(component)) {
					this.remove(i);
					count = this.getComponentCount();
				} else {
					i++;
					workingSet.remove(component);
				}
			}
			// Add components in set that were not previously children.
			for (Component component : workingSet) {
				this.add(component);
			}
		}
	}

	/**
	 * Gets the default margin insets.
	 *
	 * @return the default margin insets
	 */
	public Insets getDefaultMarginInsets() {
		return defaultMarginInsets;
	}

	/**
	 * Sets the default margin insets.
	 *
	 * @param defaultMarginInsets
	 *            the new default margin insets
	 */
	public void setDefaultMarginInsets(Insets defaultMarginInsets) {
		if (!Objects.equals(this.defaultMarginInsets, defaultMarginInsets)) {
			this.defaultMarginInsets = defaultMarginInsets;
			RBlock block = this.rblock;
			if (block != null) {
				block.setDefaultMarginInsets(defaultMarginInsets);
				block.relayoutIfValid();
			}
		}
	}

	/**
	 * Gets the default overflow x.
	 *
	 * @return the default overflow x
	 */
	public int getDefaultOverflowX() {
		return defaultOverflowX;
	}

	/**
	 * Sets the default overflow x.
	 *
	 * @param defaultOverflowX
	 *            the new default overflow x
	 */
	public void setDefaultOverflowX(int defaultOverflowX) {
		if (defaultOverflowX != this.defaultOverflowX) {
			this.defaultOverflowX = defaultOverflowX;
			RBlock block = this.rblock;
			if (block != null) {
				block.setDefaultOverflowX(defaultOverflowX);
				block.relayoutIfValid();
			}
		}
	}

	/**
	 * Gets the default overflow y.
	 *
	 * @return the default overflow y
	 */
	public int getDefaultOverflowY() {
		return defaultOverflowY;
	}

	/**
	 * Sets the default overflow y.
	 *
	 * @param defaultOverflowY
	 *            the new default overflow y
	 */
	public void setDefaultOverflowY(int defaultOverflowY) {
		if (this.defaultOverflowY != defaultOverflowY) {
			this.defaultOverflowY = defaultOverflowY;
			RBlock block = this.rblock;
			if (block != null) {
				block.setDefaultOverflowY(defaultOverflowY);
				block.relayoutIfValid();
			}
		}
	}

	@Override
	public Insets getInsets(final boolean hscroll, final boolean vscroll) {
		throw new UnsupportedOperationException(
				"Method added while implementing absolute positioned elements inside relative elements. But not implemented yet.");
	}
	
	private UINode getUINode(Node currentNode) {
		UINode uiNode = null;
		while (currentNode != null) {
			if (currentNode instanceof HTMLElementImpl) {
				HTMLElementImpl element = (HTMLElementImpl) currentNode;
				uiNode = element.getUINode();
				if (uiNode != null) {
					break;
				}
			}
			currentNode = currentNode.getParentNode();
		}
		return uiNode;
	}

	/**
	 * @return the rblock
	 */
	public RBlock getRblock() {
		return rblock;
	}

	/**
	 * @param rblock the rblock to set
	 */
	public void setRblock(RBlock rblock) {
		this.rblock = rblock;
	}

	/**
	 * @return the frameContext
	 */
	public FrameContext getFrameContext() {
		return frameContext;
	}

}
