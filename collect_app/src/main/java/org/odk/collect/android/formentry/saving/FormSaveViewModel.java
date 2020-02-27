package org.odk.collect.android.formentry.saving;

import android.net.Uri;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryController;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.FormControllerProvider;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.formentry.audit.AuditEvent;
import org.odk.collect.android.formentry.audit.AuditEventLogger;
import org.odk.collect.android.formentry.audit.AuditUtils;
import org.odk.collect.android.formentry.javarosawrapper.FormController;
import org.odk.collect.android.fragments.dialogs.ProgressDialogFragment;
import org.odk.collect.android.tasks.SaveFormToDisk;
import org.odk.collect.android.tasks.SaveToDiskResult;
import org.odk.collect.utilities.Clock;

import java.util.HashMap;

import static org.odk.collect.android.tasks.SaveFormToDisk.SAVED;
import static org.odk.collect.android.tasks.SaveFormToDisk.SAVED_AND_EXIT;
import static org.odk.collect.android.utilities.StringUtils.isBlank;

public class FormSaveViewModel extends ViewModel implements ProgressDialogFragment.Cancellable {

    private final Clock clock;
    private final FormSaver formSaver;
    private final FormControllerProvider formControllerProvider;

    private String reason = "";
    private final MutableLiveData<SaveResult> saveResult = new MutableLiveData<>(null);

    @Nullable
    private AsyncTask saveTask;

    private final Analytics analytics;

    public FormSaveViewModel(FormControllerProvider formControllerProvider, Clock clock, FormSaver formSaver, Analytics analytics) {
        this.clock = clock;
        this.formSaver = formSaver;
        this.formControllerProvider = formControllerProvider;
        this.analytics = analytics;
    }

    public void editingForm() {
        if (getAuditEventLogger() != null) {
            getAuditEventLogger().setEditing(true);
        }
    }

    public void saveAnswersForScreen(HashMap<FormIndex, IAnswerData> answers) {
        try {
            formControllerProvider.getFormController().saveAllScreenAnswers(answers, false);
        } catch (JavaRosaException ignored) {
            // ignored
        }
    }

    public void saveForm(Uri instanceContentURI, boolean shouldFinalize, String updatedSaveName, boolean viewExiting) {
        if (isSaving()) {
            return;
        }

        if (getAuditEventLogger() != null) {
            getAuditEventLogger().flush();
        }

        SaveRequest saveRequest = new SaveRequest(instanceContentURI, viewExiting, updatedSaveName, shouldFinalize);

        if (!requiresReasonToSave()) {
            this.saveResult.setValue(new SaveResult(SaveResult.State.SAVING, saveRequest));
            saveToDisk(saveRequest);
        } else {
            this.saveResult.setValue(new SaveResult(SaveResult.State.CHANGE_REASON_REQUIRED, saveRequest));
        }
    }

    public boolean isSaving() {
        return saveResult.getValue() != null && saveResult.getValue().getState().equals(SaveResult.State.SAVING);
    }

    @Override
    public boolean cancel() {
        return saveTask.cancel(true);
    }

    public void setReason(@NonNull String reason) {
        this.reason = reason;
    }

    public boolean saveReason() {
        if (reason == null || isBlank(reason)) {
            return false;
        }

        if (getAuditEventLogger() != null) {
            getAuditEventLogger().logEvent(AuditEvent.AuditEventType.CHANGE_REASON, null, true, null, clock.getCurrentTime(), reason);
        }

        if (saveResult.getValue() != null) {
            SaveRequest request = saveResult.getValue().request;
            saveResult.setValue(new SaveResult(SaveResult.State.SAVING, request));
            saveToDisk(request);
        }

        return true;
    }

    public String getReason() {
        return reason;
    }

    private void saveToDisk(SaveRequest saveRequest) {
        saveTask = new SaveTask(saveRequest, formSaver, getFormController(), new SaveTask.Listener() {
            @Override
            public void onProgressPublished(String progress) {
                saveResult.setValue(new SaveResult(SaveResult.State.SAVING, saveRequest, progress));
            }

            @Override
            public void onComplete(SaveToDiskResult saveToDiskResult) {
                handleTaskResult(saveToDiskResult, saveRequest);
            }
        }, analytics).execute();
    }

    private void handleTaskResult(SaveToDiskResult taskResult, SaveRequest saveRequest) {
        switch (taskResult.getSaveResult()) {
            case SAVED:
            case SAVED_AND_EXIT: {
                if (getAuditEventLogger() != null) {
                    getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_SAVE, false, clock.getCurrentTime());

                    if (saveRequest.viewExiting) {
                        if (saveRequest.shouldFinalize) {
                            getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_EXIT, false, clock.getCurrentTime());
                            getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_FINALIZE, true, clock.getCurrentTime());
                        } else {
                            getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_EXIT, true, clock.getCurrentTime());
                        }
                    } else {
                        AuditUtils.logCurrentScreen(getFormController(), getAuditEventLogger(), clock.getCurrentTime());
                    }
                }

                saveResult.setValue(new SaveResult(SaveResult.State.SAVED, saveRequest, taskResult.getSaveErrorMessage()));
                break;
            }

            case SaveFormToDisk.SAVE_ERROR: {
                if (getAuditEventLogger() != null) {
                    getAuditEventLogger().logEvent(AuditEvent.AuditEventType.SAVE_ERROR, true, clock.getCurrentTime());
                }

                saveResult.setValue(new SaveResult(SaveResult.State.SAVE_ERROR, saveRequest, taskResult.getSaveErrorMessage()));
                break;
            }

            case SaveFormToDisk.ENCRYPTION_ERROR: {
                if (getAuditEventLogger() != null) {
                    getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FINALIZE_ERROR, true, clock.getCurrentTime());
                }

                saveResult.setValue(new SaveResult(SaveResult.State.FINALIZE_ERROR, saveRequest, taskResult.getSaveErrorMessage()));
                break;
            }

            case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
            case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY: {
                if (getAuditEventLogger() != null) {
                    getAuditEventLogger().logEvent(AuditEvent.AuditEventType.CONSTRAINT_ERROR, true, clock.getCurrentTime());
                }

                saveResult.setValue(new SaveResult(SaveResult.State.CONSTRAINT_ERROR, saveRequest, taskResult.getSaveErrorMessage()));
                break;
            }
        }
    }

    public LiveData<SaveResult> getSavedResult() {
        return saveResult;
    }

    public void resumeFormEntry() {
        saveResult.setValue(null);
    }

    private boolean requiresReasonToSave() {
        return getAuditEventLogger() != null
                && getAuditEventLogger().isEditing()
                && getAuditEventLogger().isChangeReasonRequired()
                && getAuditEventLogger().isChangesMade();
    }

    @Nullable
    private FormController getFormController() {
        return formControllerProvider.getFormController();
    }

    @Nullable
    private AuditEventLogger getAuditEventLogger() {
        if (formControllerProvider.getFormController() != null) {
            return formControllerProvider.getFormController().getAuditEventLogger();
        } else {
            return null;
        }
    }

    public static class SaveResult {

        private final State state;
        private final String message;
        private final SaveRequest request;

        SaveResult(State state, SaveRequest request) {
            this(state, request, null);
        }

        SaveResult(State state, SaveRequest request, String message) {
            this.state = state;
            this.message = message;
            this.request = request;
        }

        public State getState() {
            return state;
        }

        public String getMessage() {
            return message;
        }

        public enum State {
            CHANGE_REASON_REQUIRED,
            SAVING,
            SAVED,
            SAVE_ERROR,
            FINALIZE_ERROR,
            CONSTRAINT_ERROR
        }

        public SaveRequest getRequest() {
            return request;
        }
    }

    public static class SaveRequest {

        private final boolean shouldFinalize;
        private final boolean viewExiting;
        private final String updatedSaveName;
        private final Uri uri;

        SaveRequest(Uri instanceContentURI, boolean viewExiting, String updatedSaveName, boolean shouldFinalize) {
            this.shouldFinalize = shouldFinalize;
            this.viewExiting = viewExiting;
            this.updatedSaveName = updatedSaveName;
            this.uri = instanceContentURI;
        }

        public boolean shouldFinalize() {
            return shouldFinalize;
        }

        public boolean viewExiting() {
            return viewExiting;
        }
    }

    private static class SaveTask extends AsyncTask<Void, String, SaveToDiskResult> {

        private final SaveRequest saveRequest;
        private final FormSaver formSaver;

        private final Listener listener;
        private final FormController formController;
        private final Analytics analytics;

        SaveTask(SaveRequest saveRequest, FormSaver formSaver, FormController formController, Listener listener, Analytics analytics) {
            this.saveRequest = saveRequest;
            this.formSaver = formSaver;
            this.listener = listener;
            this.formController = formController;
            this.analytics = analytics;
        }

        @Override
        protected SaveToDiskResult doInBackground(Void... voids) {
            return formSaver.save(saveRequest.uri, formController,
                    saveRequest.shouldFinalize,
                    saveRequest.viewExiting, saveRequest.updatedSaveName,
                    this::publishProgress, analytics
            );
        }

        @Override
        protected void onProgressUpdate(String... values) {
            listener.onProgressPublished(values[0]);
        }

        @Override
        protected void onPostExecute(SaveToDiskResult saveToDiskResult) {
            listener.onComplete(saveToDiskResult);
        }

        interface Listener {
            void onProgressPublished(String progress);

            void onComplete(SaveToDiskResult saveToDiskResult);
        }
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Analytics analytics;
        private final FormControllerProvider formControllerProvider;


        public Factory(FormControllerProvider formControllerProvider, Analytics analytics) {
            this.formControllerProvider = formControllerProvider;
            this.analytics = analytics;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new FormSaveViewModel(formControllerProvider, System::currentTimeMillis, new DiskFormSaver(), analytics);
        }
    }
}
