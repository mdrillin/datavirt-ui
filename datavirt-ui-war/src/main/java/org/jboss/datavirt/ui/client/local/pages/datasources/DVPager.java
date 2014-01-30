/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.datavirt.ui.client.local.pages.datasources;

import org.overlord.sramp.ui.client.local.widgets.common.UnorderedListPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SimpleHtmlSanitizer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.InlineLabel;

/**
 * Implements a bootstrap style pager.
 *
 * @author mdrillin@redhat.com
 */
public class DVPager extends FlowPanel implements HasValueChangeHandlers<Integer>, HasValue<Integer> {

	private UnorderedListPanel ul = new UnorderedListPanel();
	private int currentPage = 1;
	private int numPages;
	private int pgSize;
	private long totalItems;
	private Anchor goToBeginning;
	private Anchor goFwdOnePage;
	private Anchor goBackOnePage;
	private Anchor goToEnd;
	private InlineLabel rangeText;

	/**
	 * Constructor.
	 */
	public DVPager() {
		goToBeginning = new Anchor(SimpleHtmlSanitizer.sanitizeHtml("&#x00AB;")); //$NON-NLS-1$
		goToBeginning.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updatePageAndFireEvent(1);
			}
		});
		goBackOnePage = new Anchor(SimpleHtmlSanitizer.sanitizeHtml("&#x2039;")); //$NON-NLS-1$
		goBackOnePage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int newPage = (currentPage-1 >= 1) ? currentPage-1 : 1;
				updatePageAndFireEvent(newPage);
			}
		});
		
        rangeText = new InlineLabel();
		rangeText.setWidth("80px");
		
		goFwdOnePage = new Anchor(SimpleHtmlSanitizer.sanitizeHtml("&#x203A;")); //$NON-NLS-1$
		goFwdOnePage.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				int newPage = (currentPage+1 <= numPages) ? currentPage+1 : numPages;
				updatePageAndFireEvent(newPage);
			}
		});
		goToEnd = new Anchor(SimpleHtmlSanitizer.sanitizeHtml("&#x00BB;")); //$NON-NLS-1$
		goToEnd.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				updatePageAndFireEvent(numPages);
			}
		});

		this.add(ul);
		getElement().setClassName("pagination"); //$NON-NLS-1$
	}

	/**
	 * Updates the current page and fires a value-change-event, if
	 * appropriate.  Also re-renders the Pager.
	 * @param newValue
	 */
	protected void updatePageAndFireEvent(int newValue) {
		int oldValue = this.currentPage;
		currentPage = newValue;
		render();
		fireValueChangeEvent(oldValue, newValue);
	}

	/**
	 * Fires a value change event.  This is called when the user clicks
	 * on one of the pages in the pager.
	 * @param oldValue
	 * @param newValue
	 */
	protected void fireValueChangeEvent(int oldValue, int newValue) {
		ValueChangeEvent.fireIfNotEqual(this, oldValue, newValue);
	}

	/**
	 * Gets the current page (that this pager thinks is selected).
	 */
	public int getPage() {
		return this.currentPage;
	}

	/**
	 * Sets the Pager to the given page number.
	 * @param pageNum
	 */
	public void setPage(int pageNum) {
		this.currentPage = pageNum;
		this.render();
	}

	/**
	 * Sets the number of pages in the result set.
	 * @param numPages
	 */
	public void setNumPages(int numPages) {
		this.numPages = numPages;
	}

	/**
	 * Sets the page size
	 * @param pgSize items per page
	 */
	public void setPageSize(int pgSize) {
		this.pgSize = pgSize;
	}

	/**
	 * Sets the total items
	 * @param totalItems
	 */
	public void setTotalItems(long totalItems) {
		this.totalItems = totalItems;
	}

//	/**
//	 * Called to render the Pager.  This will update the range of pages
//	 * available to the user, based on the current page selection and the
//	 * number of possible pages.
//	 */
//	private void render() {
//		this.ul.clear();
//		this.ul.add(goToBeginning);
//		if (this.currentPage == 1)
//			this.ul.setLiClass(goToBeginning, "disabled"); //$NON-NLS-1$
//		int fromPage = 1;
//		int toPage = this.numPages;
//		for (int page = fromPage; page <= toPage; page++) {
//			Anchor a = new Anchor(String.valueOf(page));
//			final int p = page;
//			a.addClickHandler(new ClickHandler() {
//				@Override
//				public void onClick(ClickEvent event) {
//					updatePageAndFireEvent(p);
//				}
//			});
//			this.ul.add(a);
//			if (this.currentPage == p) {
//				this.ul.setLiClass(a, "disabled"); //$NON-NLS-1$
//			}
//		}
//		if (toPage != this.numPages) {
//			this.ul.add(goToMore);
//			this.ul.setLiClass(goToMore, "disabled"); //$NON-NLS-1$
//		}
//		this.ul.add(goToEnd);
//		if (this.currentPage == this.numPages) {
//			this.ul.setLiClass(goToEnd, "disabled"); //$NON-NLS-1$
//		}
//	}
	
	/**
	 * Called to render the Pager.  This will update the range of pages
	 * available to the user, based on the current page selection and the
	 * number of possible pages.
	 */
	private void render() {
		this.ul.clear();

		this.ul.add(goToBeginning);
		if (this.currentPage == 1)
			this.ul.setLiClass(goToBeginning, "disabled"); //$NON-NLS-1$
		
		this.ul.add(goBackOnePage);
		if (this.currentPage == 1)
			this.ul.setLiClass(goBackOnePage, "disabled"); //$NON-NLS-1$

		this.ul.add(rangeText);
		rangeText.setText(getRangeText());
		
		this.ul.add(goFwdOnePage);
		if (this.currentPage == this.numPages)
			this.ul.setLiClass(goFwdOnePage, "disabled"); //$NON-NLS-1$

		this.ul.add(goToEnd);
		if (this.currentPage == this.numPages) {
			this.ul.setLiClass(goToEnd, "disabled"); //$NON-NLS-1$
		}
		
	}
	
	private String getRangeText() {		
        // Start and End Index for this page
        long page_startIndex = (this.currentPage - 1) * this.pgSize + 1;
        long page_endIndex = page_startIndex + (this.pgSize-1);
        // If page endIndex greater than total rows, reset to end
        if(page_endIndex > this.totalItems-1) {
        	page_endIndex = this.totalItems;
        }
        
        if(this.totalItems==0) page_startIndex = 0;
        
		StringBuffer sb = new StringBuffer(page_startIndex+" - "+page_endIndex);
		sb.append(" of ");
		sb.append(this.totalItems);
		return sb.toString();
	}

	/**
	 * @see com.google.gwt.event.logical.shared.HasValueChangeHandlers#addValueChangeHandler(com.google.gwt.event.logical.shared.ValueChangeHandler)
	 */
	@Override
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Integer> handler) {
		return this.addHandler(handler, ValueChangeEvent.getType());
	}

	/**
	 * @see com.google.gwt.user.client.ui.HasValue#getValue()
	 */
	@Override
	public Integer getValue() {
		return getPage();
	}

	/**
	 * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object)
	 */
	@Override
	public void setValue(Integer value) {
		this.setValue(value, false);
	}

	/**
	 * @see com.google.gwt.user.client.ui.HasValue#setValue(java.lang.Object, boolean)
	 */
	@Override
	public void setValue(Integer value, boolean fireEvents) {
		if (fireEvents) {
			updatePageAndFireEvent(value);
		} else {
			this.currentPage = value;
			render();
		}
	}

}
