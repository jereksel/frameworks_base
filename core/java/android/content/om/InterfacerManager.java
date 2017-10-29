/*
 * Copyright (c) 2017 Andrzej Ressel <jereksel@gmail.com>
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

import android.annotation.SystemService;
import android.content.Context;
import android.content.om.IInterfacerManager;
import android.content.om.IOverlayManager;
import android.os.RemoteException;

import java.util.List;

@SystemService(Context.INTERFACER_SERVICE)
public class InterfacerManager {

    private final Context context;
    //    private final IOverlayManager overlayManager;
    private final IInterfacerManager iInterfacerManager;

    public InterfacerManager(
            Context context,
            IInterfacerManager iInterfacerManager
    ) {
        this.context = context;
//        this.overlayManager = IOverlayManager.Stub.
        this.iInterfacerManager = iInterfacerManager;
    }

    public void installPackage(List<String> paths) {
        try {
            iInterfacerManager.installPackage(paths);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void uninstallPackage(List<String> packages) {
        try {
            iInterfacerManager.uninstallPackage(packages);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void enableOverlay(List<String> packages) {
        try {
            iInterfacerManager.enableOverlay(packages);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public void disableOverlay(List<String> packages) {
        try {
            iInterfacerManager.disableOverlay(packages);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    public OverlayInfo getOverlayInfo(String packageName) {
        try {
            return iInterfacerManager.getOverlayInfo(packageName);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return null;
        }
    }

    public List<OverlayInfo> getOverlayInfosForTarget(String packageName) {
        try {
            return iInterfacerManager.getOverlayInfosForTarget(packageName);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
            return null;
        }
    }
}
