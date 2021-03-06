/*
 * (c) Kitodo. Key to digital objects e. V. <contact@kitodo.org>
 *
 * This file is part of the Kitodo project.
 *
 * It is licensed under GNU General Public License version 3 or later.
 *
 * For the full copyright and license information, please read the
 * LICENSE file that was distributed with this source code.
 */

package org.kitodo.mediaserver.core.models;

import java.util.Map;

/**
 * Dummy for an ActionControl entity.
 */
public class ActionControl {

    Map<String, String> parameter;

    public Map<String, String> getParameter() {
        return parameter;
    }

    public void setParameter(Map<String, String> parameter) {
        this.parameter = parameter;
    }

    private ActionControl() {}

    /**
     * Constructs an ActionControl object.
     *
     * @param workId the id of the work on which the action is performed
     * @param action the action name
     * @param parameter a map of parameter
     */
    public ActionControl(int workId, String action, Map<String, String> parameter) {
        // create object

        this.parameter = parameter;
    }
}
