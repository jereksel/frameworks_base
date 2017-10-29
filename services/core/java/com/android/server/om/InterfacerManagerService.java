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


package com.android.server.om;

import android.Manifest;
import android.annotation.NonNull;
import android.app.PackageInstallObserver;
import android.content.Context;
import android.content.om.IInterfacerManager;
import android.content.om.IOverlayManager;
import android.content.om.OverlayInfo;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;

import com.android.server.SystemService;

import java.io.File;
import java.util.List;

final public class InterfacerManagerService extends SystemService {

    private final PackageManager packageManager;

    private IOverlayManager overlayManager;

    private static final String TAG = "InterfacerManager";

    /**
     * Initializes the system service.
     * <p>
     * Subclasses must define a single argument constructor that accepts the context
     * and passes it to super.
     * </p>
     *
     * @param context The system server context.
     * @param mPackageManager
     */
    public InterfacerManagerService(
            Context context,
            PackageManager mPackageManager) {
        super(context);
        this.packageManager = mPackageManager;
    }

    @Override
    public void onStart() {
        publishBinderService(Context.INTERFACER_SERVICE, new InterfacerManagerServiceStub());
//        getLocalService()
    }

    private void ensureOverlayService() {
        if (overlayManager == null) {
            overlayManager = IOverlayManager.Stub.asInterface(getBinderService(Context.OVERLAY_SERVICE));
        }
    }

    private final class InterfacerManagerServiceStub extends IInterfacerManager.Stub {

        @Override
        public void installPackage(List<String> paths) throws RemoteException {
            enforceModifyAppsPermission("installPackage");
            final long ident = Binder.clearCallingIdentity();
            Log.d(TAG, "Installing packages: " + paths.toString());
            paths.stream()
                    .map(File::new)
                    .forEach(path -> {
                        if (!path.exists()) {
                            Log.w(TAG, path.getAbsolutePath() + " doesn't exists");
                            return;
                        }

                        Uri uri = Uri.fromFile(path);

                        final Object lock = new Object();

                        PackageInstallObserver observer = new PackageInstallObserver() {
                            @Override
                            public void onPackageInstalled(String basePackageName, int returnCode, String msg, Bundle extras) {
                                super.onPackageInstalled(basePackageName, returnCode, msg, extras);
                                synchronized (lock) {
                                    lock.notify();
                                }
                            }
                        };

                        packageManager.installPackage(
                                uri, observer,
                                PackageManager.INSTALL_REPLACE_EXISTING,
                                null
                        );

                        synchronized (lock) {
                            try {
                                lock.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
            Binder.restoreCallingIdentity(ident);
        }

        @Override
        public void uninstallPackage(List<String> packages) throws RemoteException {
            enforceModifyAppsPermission("uninstallPackage");
            final long ident = Binder.clearCallingIdentity();
            Log.d(TAG, "Uninstalling packages: " + packages.toString());

            for (String packag : packages) {

                final Object lock = new Object();

                IPackageDeleteObserver observer = new IPackageDeleteObserver.Stub() {
                    @Override
                    public void packageDeleted(String packageName, int returnCode) throws RemoteException {
                        synchronized (lock) {
                            lock.notify();
                        }
                    }
                };

                packageManager.deletePackage(packag, observer, 0);

                synchronized (lock) {
                    try {
                        lock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }


            Binder.restoreCallingIdentity(ident);
        }

        @Override
        public void enableOverlay(List<String> packages) throws RemoteException {
            enforceModifyOverlayPermission("enableOverlay");
            final long ident = Binder.clearCallingIdentity();
            ensureOverlayService();
            try {
                for (String packag : packages) {
                    overlayManager.setEnabled(packag, true, UserHandle.USER_SYSTEM);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
//            packages.forEach(overlay -> {
//                try {
//                    overlayManager.setEnabled(overlay, true, UserHandle.USER_SYSTEM);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            });
        }

        @Override
        public void disableOverlay(List<String> packages) throws RemoteException {
            enforceModifyOverlayPermission("disableOverlay");
            final long ident = Binder.clearCallingIdentity();
            ensureOverlayService();
//            packages.forEach(overlay -> {
//                try {
//                    overlayManager.setEnabled(overlay, false, UserHandle.USER_SYSTEM);
//                } catch (RemoteException e) {
//                    e.printStackTrace();
//                }
//            });
            try {
                for (String packag : packages) {
                    overlayManager.setEnabled(packag, false, UserHandle.USER_SYSTEM);
                }
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
//            overlayManager.setEnabled()
//            throw new RuntimeException("Disable overlay not implemented");
        }

        @Override
        public OverlayInfo getOverlayInfo(String packageName) throws RemoteException {
            final long ident = Binder.clearCallingIdentity();
            ensureOverlayService();
            try {
                return overlayManager.getOverlayInfo(packageName, UserHandle.USER_SYSTEM);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        @Override
        public List<OverlayInfo> getOverlayInfosForTarget(
                String targetPackageName
        ) throws RemoteException {
            final long ident = Binder.clearCallingIdentity();
            ensureOverlayService();
            try {
                //noinspection unchecked
                return (List<OverlayInfo>) overlayManager.getOverlayInfosForTarget(targetPackageName, UserHandle.USER_SYSTEM);
            } finally {
                Binder.restoreCallingIdentity(ident);
            }
        }

        private void enforceModifyOverlayPermission(@NonNull final String message) {
            getContext().enforceCallingPermission(
                    "interfacer.permission.MODIFY_OVERLAY", message);
        }

        private void enforceModifyAppsPermission(@NonNull final String message) {
            getContext().enforceCallingPermission(
                    "interfacer.permission.MODIFY_OVERLAY", message);
        }

    }
}
