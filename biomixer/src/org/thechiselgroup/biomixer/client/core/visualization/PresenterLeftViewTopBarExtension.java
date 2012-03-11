/*******************************************************************************
 * Copyright 2012 Lars Grammel 
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 *
 *    http://www.apache.org/licenses/LICENSE-2.0 
 *     
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.  
 *******************************************************************************/
package org.thechiselgroup.biomixer.client.core.visualization;

import org.thechiselgroup.biomixer.client.core.ui.Presenter;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Widget;

public class PresenterLeftViewTopBarExtension implements ViewTopBarExtension {

    private final Presenter presenter;

    public PresenterLeftViewTopBarExtension(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void dispose() {
        presenter.dispose();
    }

    @Override
    public void init(DockPanel configurationBar) {
        presenter.init();
        Widget widget = presenter.asWidget();

        configurationBar.add(widget, DockPanel.WEST);
        configurationBar.setCellHorizontalAlignment(widget,
                HasAlignment.ALIGN_LEFT);
    }
}