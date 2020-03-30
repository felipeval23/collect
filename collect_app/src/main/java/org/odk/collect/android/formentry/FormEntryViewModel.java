package org.odk.collect.android.formentry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.javarosawrapper.FormController;

import static org.odk.collect.android.analytics.AnalyticsEvents.ADD_REPEAT;

public class FormEntryViewModel extends ViewModel {

    private FormController formController;

    private final Analytics analytics;
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    @Nullable
    private FormIndex jumpBackIndex;

    public FormEntryViewModel(Analytics analytics) {
        this.analytics = analytics;
    }

    public void formLoaded(FormController formController) {
        this.formController = formController;
    }

    public FormIndex getCurrentIndex() {
        return getFormController().getFormIndex();
    }

    public LiveData<String> getError() {
        return error;
    }

    public void promptForNewRepeat() {
        FormIndex index = getFormController().getFormIndex();
        jumpBackIndex = index;

        getFormController().jumpToNewRepeatPrompt();
    }

    public void addRepeat(boolean fromPrompt) {
        if (jumpBackIndex != null) {
            jumpBackIndex = null;
            analytics.logEvent(ADD_REPEAT, "Inline");
        } else if (fromPrompt) {
            analytics.logEvent(ADD_REPEAT, "Prompt");
        } else {
            analytics.logEvent(ADD_REPEAT, "Hierarchy");
        }

        getFormController().newRepeat();

        if (!getFormController().indexIsInFieldList()) {
            try {
                getFormController().stepToNextScreenEvent();
            } catch (JavaRosaException exception) {
                error.setValue(exception.getCause().getMessage());
            }
        }
    }

    public void cancelRepeatPrompt() {
        analytics.logEvent(ADD_REPEAT, "InlineDecline");

        FormController formController = getFormController();

        if (jumpBackIndex != null) {
            formController.jumpToIndex(jumpBackIndex);
            jumpBackIndex = null;
        } else {
            try {
                getFormController().stepToNextScreenEvent();
            } catch (JavaRosaException exception) {
                error.setValue(exception.getCause().getMessage());
            }
        }
    }

    public void errorDisplayed() {
        error.setValue(null);
    }

    private FormController getFormController() {
        return formController;
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Analytics analytics;

        public Factory(Analytics analytics) {
            this.analytics = analytics;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new FormEntryViewModel(analytics);
        }
    }
}
