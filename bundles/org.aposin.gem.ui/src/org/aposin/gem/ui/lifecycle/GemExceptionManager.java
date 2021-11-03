package org.aposin.gem.ui.lifecycle;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;

import javax.inject.Inject;

import org.aposin.gem.core.GemException;
import org.aposin.gem.ui.Activator;
import org.aposin.gem.ui.BundleProperties;
import org.aposin.gem.ui.message.Messages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window.IExceptionHandler;

/**
 * Exception manager for GEM.
 */
@Creatable
public final class GemExceptionManager implements IExceptionHandler {

    @Inject
    @Translation
    private static BundleProperties bundleProperties;

    @Inject
    @Translation
    private static Messages messages;

    @Inject
    private IEclipseContext eclipseContext;

    /**
     * Handle the exception
     */
    @Override
    public void handleException(final Throwable t) {
        final Throwable e = unwrapException(t);
        handleExceptionInternal(e.getLocalizedMessage(), e);
    }

    /**
     * Handle the exception with a message.
     * 
     * @param msg
     * @param e
     */
    public void handleException(final String msg, final Throwable e) {
        handleExceptionInternal(msg, unwrapException(e));
    }

    private void handleExceptionInternal(final String msg, final Throwable e) {
        if (isFatal(e)) {
            final String fatalMsg = MessageFormat.format(messages.gemExceptionManager_messageFormat_fatalError, msg);
            ErrorDialog.openError(null, getErrorDialogTitle(e), fatalMsg, getStatus(e));
            eclipseContext.getActive(IWorkbench.class).close();
        } else {
            ErrorDialog.openError(null, getErrorDialogTitle(e), msg, getStatus(e));
        }
    }

    private Throwable unwrapException(final Throwable e) {
        if (e instanceof InvocationTargetException || e instanceof InterruptedException) {
            if (e.getCause() != null) {
                return e.getCause();
            }
        }
        return e;
    }

    private boolean isFatal(final Throwable e) {
        if (e instanceof GemException) {
            return ((GemException) e).isFatal();
        }
        return true;
    }

    private String getErrorDialogTitle(final Throwable e) {
        if (e instanceof GemException) {
            return MessageFormat.format(messages.gemExceptionManager_titleFormat_gemError,
                    bundleProperties.application_name_short);
        } else {
            return messages.gemExceptionManager_title_unexpectedError;
        }
    }

    private IStatus getStatus(final Throwable e) {
        // get the multi status with the stacktrace elements
        final IStatus status;
        if (e.getCause() == null) {
            status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage());
        } else {
            final Throwable toDetails = e.getCause();
            toDetails.fillInStackTrace();
            status = new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR, toDetails.getLocalizedMessage(), e);
            for (final StackTraceElement stackTrace : toDetails.getStackTrace()) {
                ((MultiStatus) status).add(new Status(IStatus.ERROR, Activator.PLUGIN_ID, stackTrace.toString()));
            }
        }
        return status;
    }

}
