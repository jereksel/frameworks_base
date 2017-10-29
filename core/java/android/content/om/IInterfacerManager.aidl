/*
 * Copyright (c) 2017 Andrzej Ressel <jereksel@gmail.com>
 * Copyright (c) 2016-2017 Project Substratum
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package android.content.om;

import android.content.om.OverlayInfo;

interface IInterfacerManager {

    /**
     * Install a list of specified applications
     *
     * @param paths Filled in with a list of path names for packages to be installed from.
     */
    void installPackage(in List<String> paths);

    /**
     * Uninstall a list of specified applications
     *
     * @param packages  Filled in with a list of package names to uninstall.
     */
    void uninstallPackage(in List<String> packages);

    /**
     * Enable a specified list of overlays
     *
     * @param packages  Filled in with a list of package names to be enabled.
     */
    void enableOverlay(in List<String> packages);

    /**
     * Disable a specified list of overlays
     *
     * @param packages  Filled in with a list of package names to be disabled.
     */
    void disableOverlay(in List<String> packages);

    OverlayInfo getOverlayInfo(in String packageName);

    List getOverlayInfosForTarget(in String packageName);

}
